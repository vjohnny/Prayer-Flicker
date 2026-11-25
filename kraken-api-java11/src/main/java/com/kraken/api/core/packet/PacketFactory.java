package com.kraken.api.core.packet;

import com.kraken.api.core.hooks.HooksLoader;
import com.kraken.api.core.packet.model.PacketDefinition;

/**
 * Looks up the mouse-click packet definition from the local hooks resource.
 *
 * <p>Quick-prayer clicks send {@code EVENT_MOUSE_CLICK} before {@code doAction}. Other packet
 * names from the upstream factory are not used by this build.</p>
 */
public class PacketFactory {

    /**
     * Helper method to safely retrieve a packet and fail fast if the JSON is missing definitions.
     */
    private static PacketDefinition getPacket(String packetName) {
        PacketDefinition def = HooksLoader.getPackets().get(packetName);
        if (def == null) {
            throw new IllegalStateException("Packet definition not found in mapped JSON for: " + packetName);
        }
        return def;
    }

    /**
     * Returns the definition of the packet sent for any mouse click on the client.
     * @return The EVENT_MOUSE_CLICK packet definition.
     */
    public static PacketDefinition getEventMouseClick() {
        return getPacket("EVENT_MOUSE_CLICK");
    }
}
