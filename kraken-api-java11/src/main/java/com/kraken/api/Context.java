package com.kraken.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kraken.api.core.ClientThreadException;
import com.kraken.api.core.ClientThreadGateway;
import com.kraken.api.core.hooks.HooksLoader;
import com.kraken.api.core.interaction.InteractionManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Client handle used by [VJ]Prayer Flicker.
 *
 * <p>One Context exists per game client. This build keeps the client, the client thread, and the
 * interaction manager the flicker clicks through. Bank, shop, query, and virtual-mouse bindings
 * from the upstream context are not constructed.</p>
 */
@Slf4j
@Singleton
public class Context {

    /**
     * How long a caller will wait for the client thread before giving up.
     *
     * <p>The client thread services this queue once per frame, so anything approaching this budget
     * means it is blocked or the client is loading — not that the work is slow.</p>
     */
    public static final long CLIENT_THREAD_TIMEOUT_MS = 3_000L;

    @Getter
    private final Client client;

    @Getter
    private final ClientThread clientThread;

    @Getter
    private final InteractionManager interactionManager;

    private final ClientThreadGateway commandGateway;

    /**
     * Creates the context the prayer flicker uses. Constructing it loads {@code hooks.json} and
     * warns when the running client revision does not match the mapped revision.
     *
     * @param client The RuneLite client
     * @param clientThread The thread that game state must be read and changed on
     * @param interactionManager The manager that performs the quick-prayer widget clicks
     */
    @Inject
    public Context(final Client client, final ClientThread clientThread, final InteractionManager interactionManager) {
        this.client = client;
        this.clientThread = clientThread;
        this.commandGateway = new ClientThreadGateway(client, clientThread, CLIENT_THREAD_TIMEOUT_MS);
        this.interactionManager = interactionManager;

        // RuneLite injects some logging into doAction when a menu action can't be found by the client but is still being
        // invoked with coordinates where the menu action should appear. This simply mutes those verbose logs.
        try {
            Field loggerField = client.getClass().getDeclaredField(HooksLoader.getSecurityHooks().getClientLogFieldName());
            loggerField.setAccessible(true);
            Object loggerInstance = loggerField.get(null);

            if (loggerInstance instanceof ch.qos.logback.classic.Logger) {
                ((ch.qos.logback.classic.Logger) loggerInstance).setLevel(ch.qos.logback.classic.Level.ERROR);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.warn("Failed modify log level for RuneLite doAction method. You may encounter more verbose logging.", e);
        }

        HooksLoader.warnOnRevisionMismatch(client.getRevision());

        log.info("Game context initialized successfully, loaded {} packet definitions", HooksLoader.getPackets().size());
    }

    /**
     * Retrieves a Widget from the RuneLite client. This method is
     *  thread-safe and will run on the client thread to retrieve the Widget.
     * @param widgetId int The widget id
     * @return Widget
     */
    public Widget getWidget(int widgetId) {
        return runOnClientThread(() -> client.getWidget(widgetId));
    }

    /**
     * Run a method on the client thread, returning the result directly.
     *
     * <p>A {@code null} return means the work genuinely produced {@code null} (an absent widget, for
     * example). If the work could not be performed at all — the client thread did not answer within
     * {@link #CLIENT_THREAD_TIMEOUT_MS}, the callable threw, or this thread was interrupted — a
     * {@link ClientThreadException} is thrown instead. Both execution paths behave identically in this
     * respect. Pending work is revoked on timeout or interruption. If execution already
     * began, timeout/interruption reports an unknown outcome and cannot undo its effects.</p>
     *
     * @param method The method to call
     * @param <T> The type of the method's return value
     * @return The result from the called method, which may be {@code null}
     * @throws ClientThreadException if the work could not be completed on the client thread
     */
    public <T> T runOnClientThread(Callable<T> method) {
        return commandGateway.call(method);
    }

    /**
     * Run a method on the client thread, falling back to a supplied value when the work could not be
     * completed.
     *
     * <p>Failures use the fallback unless execution started before a timeout or interruption;
     * an unknown outcome is propagated as {@link ClientThreadException}. A genuine {@code null}
     * result is returned as {@code null}, not replaced by the
     * fallback — the fallback stands in only for a failed hand-off.</p>
     *
     * @param method The method to call
     * @param fallback The value to return if the client thread could not complete the work
     * @param <T> The type of the method's return value
     * @return The result from the called method, or {@code fallback} on failure
     */
    public <T> T runOnClientThread(Callable<T> method, T fallback) {
        try {
            return runOnClientThread(method);
        } catch (ClientThreadException e) {
            if (e.isOutcomeUnknown()) {
                throw e;
            }
            log.debug("Falling back after client-thread failure: {}", e.getMessage());
            return fallback;
        }
    }

    /**
     * Runs inline on the client thread, otherwise queues work without waiting. Pending work expires
     * after the client-thread timeout; asynchronous failures are logged.
     * @param method Runnable method to execute
     */
    public void runOnClientThread(Runnable method) {
        commandGateway.execute(method);
    }

    /**
     * Run a method on the client thread, returning an optional of the result.
     *
     * <p>A failed hand-off and a genuine {@code null} result both yield an empty
     * {@link Optional}. Unknown outcomes after execution starts are thrown, so callers do not
     * mistake an in-flight action for a safe-to-retry failure.</p>
     *
     * @param method The method to call
     * @param <T> The type of the method's return value
     * @return The result from the called method, or empty if it was null or could not be obtained
     */
    public <T> Optional<T> runOnClientThreadOptional(Callable<T> method) {
        try {
            return Optional.ofNullable(runOnClientThread(method));
        } catch (ClientThreadException e) {
            if (e.isOutcomeUnknown()) {
                throw e;
            }
            log.debug("Client-thread work did not complete: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
