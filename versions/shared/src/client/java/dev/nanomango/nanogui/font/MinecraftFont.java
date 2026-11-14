package dev.nanomango.nanogui.font;

import dev.nanomango.gui.font.Font;
import dev.nanomango.gui.font.TextSink;

/**
 * Font implementation backed by the Minecraft font atlas.
 */
public final class MinecraftFont implements Font {

    private final int id;
    private final String name;
    private final net.minecraft.client.gui.Font minecraftFont;

    public MinecraftFont(int id, String name, net.minecraft.client.gui.Font minecraftFont) {
        this.id = id;
        this.name = name;
        this.minecraftFont = minecraftFont;
    }

    /**
     * The wrapped Minecraft font.
     */
    public net.minecraft.client.gui.Font minecraftFont() {
        return minecraftFont;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public float height() {
        return minecraftFont.lineHeight;
    }

    @Override
    public float ascent() {
        return minecraftFont.lineHeight * 0.75F;
    }

    @Override
    public float width(String text) {
        if (text == null || text.isEmpty()) {
            return 0.0F;
        }
        return minecraftFont.width(text);
    }

    @Override
    public float width(CharSequence text, int start, int end) {
        if (text == null || start >= end) {
            return 0.0F;
        }
        return minecraftFont.width(text.subSequence(start, end).toString());
    }

    @Override
    public boolean supportsUnicode() {
        return minecraftFont.isBidirectional() || minecraftFont.width("\u4f60\u597d") > 0;
    }

    @Override
    public void draw(TextSink sink, String text, float x, float y, int argb) {
        if (text == null || text.isEmpty()) {
            return;
        }
        sink.builtin(id, text, x, y, argb);
    }
}
