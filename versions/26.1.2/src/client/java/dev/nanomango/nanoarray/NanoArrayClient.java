package dev.nanomango.nanoarray;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Client entry point of NanoArray for Minecraft 26.1.2.
 */
public final class NanoArrayClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        NanoArrayCore core = NanoArrayCore.get();
        core.initialize(new NanoArrayPlatform2612());

        ClientTickEvents.END_CLIENT_TICK.register(client -> core.onClientTick());
    }
}
