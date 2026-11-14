package dev.nanomango.gui.component;

import dev.nanomango.gui.components.Button;
import dev.nanomango.gui.components.Panel;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.layout.VerticalLayout;
import dev.nanomango.gui.test.TestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContainerInputTest {

    @Test
    @DisplayName("a click hits the child at the position it was drawn at")
    void clickUsesTheContentOffset() {
        GuiContext context = TestSupport.context();
        AtomicInteger clicks = new AtomicInteger();

        Container container = new Container(new VerticalLayout()) {
            @Override
            protected float contentOffset(GuiContext context) {
                return 20.0F;
            }
        };
        container.setBounds(0.0F, 0.0F, 200.0F, 200.0F);

        Button button = new Button("Press");
        button.onClick(clicks::incrementAndGet);
        container.add(button);

        container.setContext(context);
        container.update(context);
        container.render(context);

        // The button is drawn 20 pixels below its layout position, so the
        // click has to use the same shift.
        assertTrue(container.mouseClicked(50.0, 25.0, 0),
                "the click must hit the button at its drawn position");
        container.mouseReleased(50.0, 25.0, 0);
        assertEquals(1, clicks.get());
    }

    @Test
    @DisplayName("a panel insets its children by the theme padding")
    void panelInsetsChildren() {
        GuiContext context = TestSupport.context();

        Panel panel = new Panel("Title");
        panel.setBounds(0.0F, 0.0F, 300.0F, 300.0F);
        Button child = new Button("Child");
        panel.add(child);

        panel.setContext(context);
        panel.update(context);
        panel.render(context);

        assertTrue(child.x() >= 10.0F, "the child must be inset: " + child.x());
        assertTrue(child.width() <= 280.0F, "the child must not touch the border: " + child.width());
    }
}
