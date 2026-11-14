package dev.nanomango.nanogui;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Client entry point of NanoGUI for Minecraft 26.3.
 */
public final class NanoGUIClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        NanoGUICore core = NanoGUICore.get();
        core.initialize(new NanoGUIPlatform263());

        ClientTickEvents.END_CLIENT_TICK.register(client -> core.onClientTick());
    }
}
