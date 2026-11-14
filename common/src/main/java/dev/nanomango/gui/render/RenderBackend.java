package dev.nanomango.gui.render;

import dev.nanomango.gui.font.FontRegistry;
import dev.nanomango.gui.font.TextSink;

/**
 * Replays a {@link DrawList} against a concrete graphics API.
 *
 * <p>Implementations live in the version modules (Minecraft 1.21.11 and 26.x
 * differ in their rendering APIs) and in the optional NanoVG / Dear ImGui
 * backends. The shared framework only ever talks to this interface, which is
 * what keeps the component code version independent.</p>
 */
public interface RenderBackend extends TextSink {

    /**
     * Called once before the first command of a frame is replayed.
     *
     * @param context frame information
     */
    void beginFrame(RenderContext context);

    /**
     * Replays all recorded commands.
     */
    void render(RenderContext context, DrawList commands, FontRegistry fonts);

    /**
     * Called once after the last command of a frame was replayed. Backends
     * restore any state they changed here.
     */
    void endFrame(RenderContext context);

    /**
     * Releases all GPU resources owned by this backend.
     */
    default void close() {
    }
}
