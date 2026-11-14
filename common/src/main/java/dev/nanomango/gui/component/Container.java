package dev.nanomango.gui.component;

import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.layout.Layout;
import dev.nanomango.gui.layout.VerticalLayout;
import dev.nanomango.gui.util.MathUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A component that owns other components.
 *
 * <p>Containers arrange their children with a {@link Layout}, forward mouse
 * and keyboard events to them and optionally clip and scroll their content.</p>
 */
public class Container extends Component {

    private final List<Component> children = new ArrayList<>();
    private Layout layout = new VerticalLayout();

    private boolean clip = true;
    private boolean scrollable;
    private float scrollOffset;
    private float contentHeight;
    private float scrollVelocity;
    private boolean scrolling;

    public Container() {
    }

    public Container(Layout layout) {
        this.layout = layout;
    }

    public Container layout(Layout layout) {
        this.layout = layout;
        return this;
    }

    public Layout layout() {
        return layout;
    }

    /**
     * Adds a child component.
     *
     * @return the added component, so calls can be chained
     */
    public <T extends Component> T add(T component) {
        component.setParent(this);
        children.add(component);
        return component;
    }

    public <T extends Component> T add(T component, int index) {
        component.setParent(this);
        children.add(Math.max(0, Math.min(index, children.size())), component);
        return component;
    }

    public boolean remove(Component component) {
        boolean removed = children.remove(component);
        if (removed) {
            component.setParent(null);
        }
        return removed;
    }

    public void clear() {
        for (Component child : children) {
            child.setParent(null);
        }
        children.clear();
    }

    public List<Component> children() {
        return Collections.unmodifiableList(children);
    }

    public int childCount() {
        return children.size();
    }

    public Component child(int index) {
        return children.get(index);
    }

    /**
     * Finds the first descendant with the given id.
     */
    public Component find(String id) {
        for (Component child : children) {
            if (id.equals(child.getId())) {
                return child;
            }
            if (child instanceof Container container) {
                Component found = container.find(id);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Scrolling
    // ------------------------------------------------------------------

    public boolean scrollable() {
        return scrollable;
    }

    public Container scrollable(boolean scrollable) {
        this.scrollable = scrollable;
        return this;
    }

    public boolean clip() {
        return clip;
    }

    public Container clip(boolean clip) {
        this.clip = clip;
        return this;
    }

    public float scrollOffset() {
        return scrollOffset;
    }

    public float contentHeight() {
        return contentHeight;
    }

    public float maxScroll() {
        return Math.max(0.0F, contentHeight - height());
    }

    /**
     * Scrolls by a number of pixels. The value is clamped to the scrollable
     * range.
     */
    public void scrollBy(float amount) {
        if (!scrollable) {
            return;
        }
        scrollOffset = MathUtil.clamp(scrollOffset + amount, 0.0F, maxScroll());
    }

    public void setScrollOffset(float offset) {
        this.scrollOffset = MathUtil.clamp(offset, 0.0F, maxScroll());
    }

    // ------------------------------------------------------------------
    // Frame lifecycle
    // ------------------------------------------------------------------

    @Override
    public void update(GuiContext context) {
        super.update(context);
        for (Component child : children) {
            child.setContext(context);
            child.update(context);
        }
        if (scrollable) {
            // Smooth the scroll offset towards the target so that the wheel
            // feels natural instead of jumping.
            float target = scrollOffset;
            float smoothed = MathUtil.lerp(scrollOffset, target, 1.0F);
            scrollOffset = smoothed;
            scrollVelocity = 0.0F;
        }
    }

    @Override
    public void render(GuiContext context) {
        layout.layout(this, context);

        float offset = contentOffset(context);
        if (offset != 0.0F) {
            translateChildren(offset);
        }

        contentHeight = 0.0F;
        for (Component child : children) {
            if (child.isVisible()) {
                contentHeight = Math.max(contentHeight, child.y() + child.height() - (y() + offset));
            }
        }

        boolean clipped = clip || scrollable;
        if (clipped) {
            context.draw().clipPush(x(), y() + offset, width(), Math.max(0.0F, height() - offset));
        }
        if (scrollable) {
            translateChildren(-scrollOffset);
        }
        for (Component child : children) {
            if (child.isVisible()) {
                child.setContext(context);
                child.render(context);
            }
        }
        if (scrollable) {
            translateChildren(scrollOffset);
        }
        if (offset != 0.0F) {
            translateChildren(-offset);
        }
        if (clipped) {
            context.draw().clipPop();
        }
        renderScrollbar(context);
    }

    /**
     * Vertical space that is reserved above the children, for example for a
     * panel title or a collapsible header.
     */
    protected float contentOffset(GuiContext context) {
        return 0.0F;
    }

    private void translateChildren(float dy) {
        for (Component child : children) {
            child.setY(child.y() + dy);
        }
    }

    /**
     * Vertical offset between the positions that the layout assigned to the
     * children and the positions they are drawn at.
     *
     * <p>Rendering shifts the children by the content offset (for example a
     * panel title) and by the scroll offset. Hit testing has to apply exactly
     * the same shift, otherwise clicks land on the widget above the cursor.</p>
     */
    private float inputOffset(GuiContext context) {
        float offset = context == null ? 0.0F : contentOffset(context);
        if (scrollable) {
            offset -= scrollOffset;
        }
        return offset;
    }

    private void renderScrollbar(GuiContext context) {
        if (!scrollable || maxScroll() <= 0.0F) {
            return;
        }
        float trackHeight = height();
        float thumbHeight = Math.max(24.0F, trackHeight * (trackHeight / contentHeight));
        float thumbY = y() + (trackHeight - thumbHeight) * (scrollOffset / maxScroll());
        float barWidth = context.theme().scrollbarWidth();
        float barX = x() + width() - barWidth - 1.0F;
        context.draw().fillRounded(barX, thumbY, barWidth, thumbHeight, barWidth * 0.5F, context.theme().scrollbar());
    }

    // ------------------------------------------------------------------
    // Input
    // ------------------------------------------------------------------

    @Override
    public void clearFocus() {
        super.clearFocus();
        for (Component child : children) {
            child.clearFocus();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isVisible() || !isEnabled()) {
            return false;
        }
        if (isMouseOver(mouseX, mouseY)) {
            boolean consumed = false;
            float offset = inputOffset(context);
            if (offset != 0.0F) {
                translateChildren(offset);
            }
            try {
                for (int i = children.size() - 1; i >= 0; i--) {
                    Component child = children.get(i);
                    child.setContext(context);
                    if (child.isVisible() && child.mouseClicked(mouseX, mouseY, button)) {
                        consumed = true;
                        break;
                    }
                }
            } finally {
                if (offset != 0.0F) {
                    translateChildren(-offset);
                }
            }
            if (!consumed) {
                return super.mouseClicked(mouseX, mouseY, button);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean consumed = super.mouseReleased(mouseX, mouseY, button);
        float offset = inputOffset(context);
        if (offset != 0.0F) {
            translateChildren(offset);
        }
        try {
            for (Component child : children) {
                consumed |= child.mouseReleased(mouseX, mouseY, button);
            }
        } finally {
            if (offset != 0.0F) {
                translateChildren(-offset);
            }
        }
        return consumed;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        boolean consumed = false;
        float offset = inputOffset(context);
        if (offset != 0.0F) {
            translateChildren(offset);
        }
        try {
            for (Component child : children) {
                consumed |= child.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            }
        } finally {
            if (offset != 0.0F) {
                translateChildren(-offset);
            }
        }
        return consumed || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
        float offset = inputOffset(context);
        if (offset != 0.0F) {
            translateChildren(offset);
        }
        try {
            for (Component child : children) {
                child.mouseMoved(mouseX, mouseY);
            }
        } finally {
            if (offset != 0.0F) {
                translateChildren(-offset);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (scrollable && isMouseOver(mouseX, mouseY)) {
            scrollBy((float) -deltaY * 24.0F);
            return true;
        }
        float offset = inputOffset(context);
        if (offset != 0.0F) {
            translateChildren(offset);
        }
        try {
            for (int i = children.size() - 1; i >= 0; i--) {
                Component child = children.get(i);
                if (child.isVisible() && child.mouseScrolled(mouseX, mouseY, deltaX, deltaY)) {
                    return true;
                }
            }
        } finally {
            if (offset != 0.0F) {
                translateChildren(-offset);
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int modifiers) {
        for (int i = children.size() - 1; i >= 0; i--) {
            Component child = children.get(i);
            if (child.isVisible() && child.keyPressed(keyCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int modifiers) {
        boolean consumed = false;
        for (Component child : children) {
            consumed |= child.keyReleased(keyCode, modifiers);
        }
        return consumed;
    }

    @Override
    public boolean charTyped(int codePoint) {
        for (int i = children.size() - 1; i >= 0; i--) {
            Component child = children.get(i);
            if (child.isVisible() && child.charTyped(codePoint)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public float preferredWidth(GuiContext context) {
        float max = 0.0F;
        for (Component child : children) {
            if (child.isVisible()) {
                max = Math.max(max, child.preferredWidth(context));
            }
        }
        return max;
    }

    @Override
    public float preferredHeight(GuiContext context) {
        float total = 0.0F;
        boolean first = true;
        for (Component child : children) {
            if (!child.isVisible()) {
                continue;
            }
            float height = child.preferredHeight(context);
            if (height <= 0.0F) {
                continue;
            }
            if (!first) {
                total += layout.spacing();
            }
            first = false;
            total += height;
        }
        return total;
    }
}
