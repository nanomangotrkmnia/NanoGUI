package dev.nanomango.gui.animation;

/**
 * A single animated value.
 *
 * <p>The animation is driven by delta time, which makes it independent of the
 * frame rate. Retargeting an animation restarts it from the current value,
 * which produces the smooth transitions the UI relies on.</p>
 */
public final class Animation {

    private float current;
    private float from;
    private float target;
    private float duration;
    private float elapsed;
    private Easing easing;
    private boolean running;

    public Animation(float initial) {
        this(initial, 0.2F, Easing.CUBIC_OUT);
    }

    public Animation(float initial, float duration, Easing easing) {
        this.current = initial;
        this.from = initial;
        this.target = initial;
        this.duration = duration;
        this.easing = easing;
    }

    public float value() {
        return current;
    }

    public float target() {
        return target;
    }

    public boolean isRunning() {
        return running;
    }

    public float progress() {
        if (duration <= 0.0F) {
            return 1.0F;
        }
        return Math.min(1.0F, elapsed / duration);
    }

    /**
     * Starts an animation towards a new value.
     *
     * @param newTarget the value to animate to
     * @param newDuration duration in seconds
     */
    public void animateTo(float newTarget, float newDuration) {
        animateTo(newTarget, newDuration, easing);
    }

    public void animateTo(float newTarget, float newDuration, Easing newEasing) {
        if (newDuration <= 0.0F) {
            set(newTarget);
            return;
        }
        this.from = current;
        this.target = newTarget;
        this.duration = newDuration;
        this.easing = newEasing;
        this.elapsed = 0.0F;
        this.running = true;
    }

    /**
     * Jumps to a value and stops any running animation.
     */
    public void set(float value) {
        this.current = value;
        this.from = value;
        this.target = value;
        this.elapsed = 0.0F;
        this.running = false;
    }

    /**
     * Advances the animation.
     *
     * @param deltaSeconds time since the last frame in seconds
     */
    public void update(float deltaSeconds) {
        if (!running) {
            return;
        }
        elapsed += deltaSeconds;
        float t = duration <= 0.0F ? 1.0F : Math.min(1.0F, elapsed / duration);
        current = from + (target - from) * easing.apply(t);
        if (t >= 1.0F) {
            current = target;
            running = false;
        }
    }
}
