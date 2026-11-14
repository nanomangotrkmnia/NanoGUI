package dev.nanomango.gui.animation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnimationTest {

    @Test
    @DisplayName("a fresh animation is not running and holds its initial value")
    void initialState() {
        Animation animation = new Animation(0.25F);
        assertEquals(0.25F, animation.value());
        assertFalse(animation.isRunning());
        assertEquals(0.25F, animation.target());
    }

    @Test
    @DisplayName("the animation reaches its target after the duration")
    void reachesTarget() {
        Animation animation = new Animation(0.0F, 0.2F, Easing.LINEAR);
        animation.animateTo(1.0F, 0.2F, Easing.LINEAR);

        animation.update(0.1F);
        assertTrue(animation.isRunning());
        assertEquals(0.5F, animation.value(), 0.0001F);

        animation.update(0.1F);
        assertFalse(animation.isRunning());
        assertEquals(1.0F, animation.value(), 0.0001F);
    }

    @Test
    @DisplayName("a zero duration jumps to the target")
    void zeroDuration() {
        Animation animation = new Animation(0.0F);
        animation.animateTo(1.0F, 0.0F);
        assertEquals(1.0F, animation.value());
        assertFalse(animation.isRunning());
    }

    @Test
    @DisplayName("retargeting continues from the current value")
    void retarget() {
        Animation animation = new Animation(0.0F, 1.0F, Easing.LINEAR);
        animation.animateTo(1.0F, 1.0F, Easing.LINEAR);
        animation.update(0.5F);
        float halfway = animation.value();

        animation.animateTo(0.0F, 1.0F, Easing.LINEAR);
        assertEquals(halfway, animation.value(), 0.0001F);
        animation.update(0.5F);
        assertEquals(halfway * 0.5F, animation.value(), 0.0001F);
    }

    @Test
    @DisplayName("set stops a running animation")
    void setStops() {
        Animation animation = new Animation(0.0F);
        animation.animateTo(1.0F, 1.0F);
        animation.set(0.75F);
        assertFalse(animation.isRunning());
        assertEquals(0.75F, animation.value());

        animation.update(1.0F);
        assertEquals(0.75F, animation.value());
    }

    @Test
    @DisplayName("the progress reports the fraction of the duration")
    void progress() {
        Animation animation = new Animation(0.0F, 0.4F, Easing.LINEAR);
        animation.animateTo(1.0F, 0.4F, Easing.LINEAR);
        animation.update(0.1F);
        assertEquals(0.25F, animation.progress(), 0.0001F);
    }
}
