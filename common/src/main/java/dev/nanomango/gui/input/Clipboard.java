package dev.nanomango.gui.input;

/**
 * Abstraction over the system clipboard.
 *
 * <p>The version modules bind this to the real system clipboard; the default
 * implementation keeps the text in memory which keeps the framework usable in
 * headless tests.</p>
 */
public interface Clipboard {

    /**
     * Current clipboard contents, never {@code null}.
     */
    String contents();

    /**
     * Replaces the clipboard contents.
     */
    void setContents(String contents);

    /**
     * Simple in-memory clipboard used when no system clipboard is available.
     */
    final class Memory implements Clipboard {

        private String contents = "";

        @Override
        public String contents() {
            return contents;
        }

        @Override
        public void setContents(String contents) {
            this.contents = contents == null ? "" : contents;
        }
    }
}
