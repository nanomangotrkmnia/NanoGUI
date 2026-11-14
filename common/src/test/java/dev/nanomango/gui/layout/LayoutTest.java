package dev.nanomango.gui.layout;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.component.Container;
import dev.nanomango.gui.components.Button;
import dev.nanomango.gui.components.Separator;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.test.TestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LayoutTest {

    private GuiContext context;

    @BeforeEach
    void setUp() {
        context = TestSupport.context();
    }

    private static Component fixed(float width, float height) {
        Component component = new Component() {
            @Override
            public void render(GuiContext context) {
            }

            @Override
            public float preferredWidth(GuiContext context) {
                return width;
            }

            @Override
            public float preferredHeight(GuiContext context) {
                return height;
            }
        };
        return component;
    }

    @Test
    @DisplayName("the vertical layout stacks the children from top to bottom")
    void vertical() {
        Container container = new Container(new VerticalLayout(4.0F));
        container.setBounds(0, 0, 100, 200);
        container.add(fixed(20, 10));
        container.add(fixed(20, 30));

        container.layout().layout(container, context);

        assertEquals(0.0F, container.child(0).y(), 0.001F);
        assertEquals(14.0F, container.child(1).y(), 0.001F);
    }

    @Test
    @DisplayName("the vertical layout stretches the children to the full width")
    void verticalStretch() {
        Container container = new Container(new VerticalLayout());
        container.setBounds(0, 0, 100, 200);
        container.add(fixed(20, 10));

        container.layout().layout(container, context);

        assertEquals(100.0F, container.child(0).width(), 0.001F);
    }

    @Test
    @DisplayName("the vertical layout can centre the children")
    void verticalCenter() {
        Container container = new Container(new VerticalLayout(0.0F).alignment(
                VerticalLayout.Alignment.CENTER));
        container.setBounds(0, 0, 100, 200);
        container.add(fixed(40, 10));

        container.layout().layout(container, context);

        assertEquals(30.0F, container.child(0).x(), 0.001F);
        assertEquals(40.0F, container.child(0).width(), 0.001F);
    }

    @Test
    @DisplayName("the horizontal layout places the children next to each other")
    void horizontal() {
        Container container = new Container(new HorizontalLayout(6.0F));
        container.setBounds(0, 0, 200, 40);
        container.add(fixed(20, 10));
        container.add(fixed(30, 10));

        container.layout().layout(container, context);

        assertEquals(0.0F, container.child(0).x(), 0.001F);
        assertEquals(26.0F, container.child(1).x(), 0.001F);
    }

    @Test
    @DisplayName("the grid layout fills the rows from left to right")
    void grid() {
        Container container = new Container(new GridLayout(2, 5.0F));
        container.setBounds(0, 0, 100, 100);
        container.add(fixed(10, 10));
        container.add(fixed(10, 10));
        container.add(fixed(10, 10));

        container.layout().layout(container, context);

        // Two columns with five pixels of spacing: each column is 47.5 wide.
        assertEquals(0.0F, container.child(0).x(), 0.001F);
        assertEquals(52.5F, container.child(1).x(), 0.001F);
        assertEquals(0.0F, container.child(2).x(), 0.001F);
        assertTrue(container.child(2).y() > container.child(0).y());
    }

    @Test
    @DisplayName("the stack layout places every child on top of each other")
    void stack() {
        Container container = new Container(new StackLayout());
        container.setBounds(0, 0, 100, 50);
        container.add(fixed(10, 10));
        container.add(fixed(10, 10));

        container.layout().layout(container, context);

        assertEquals(container.child(0).x(), container.child(1).x(), 0.001F);
        assertEquals(container.child(0).y(), container.child(1).y(), 0.001F);
        assertEquals(100.0F, container.child(1).width(), 0.001F);
    }

    @Test
    @DisplayName("invisible children are skipped by the layout")
    void invisibleChildren() {
        Container container = new Container(new VerticalLayout(10.0F));
        container.setBounds(0, 0, 100, 200);
        Component hidden = fixed(20, 10);
        hidden.setVisible(false);
        container.add(hidden);
        container.add(fixed(20, 30));

        container.layout().layout(container, context);

        assertEquals(0.0F, container.child(1).y(), 0.001F);
    }

    @Test
    @DisplayName("the preferred height accounts for the spacing")
    void preferredHeight() {
        Container container = new Container(new VerticalLayout(8.0F));
        container.add(fixed(20, 10));
        container.add(fixed(20, 30));

        assertEquals(48.0F, container.preferredHeight(context), 0.001F);
    }

    @Test
    @DisplayName("a container finds its descendants by id")
    void findById() {
        Container container = new Container();
        Button button = new Button("Hello");
        button.id("hello");
        container.add(button);

        assertEquals(button, container.find("hello"));
        assertEquals(null, container.find("missing"));
    }
}
