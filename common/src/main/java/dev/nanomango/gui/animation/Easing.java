package dev.nanomango.gui.animation;

/**
 * The easing functions supported by the animation system.
 *
 * <p>All functions map the normalised time {@code t} in {@code [0, 1]} onto an
 * eased progress value. They never leave the {@code [0, 1]} range.</p>
 */
public enum Easing {

    LINEAR {
        @Override
        public float apply(float t) {
            return t;
        }
    },
    QUAD_IN {
        @Override
        public float apply(float t) {
            return t * t;
        }
    },
    QUAD_OUT {
        @Override
        public float apply(float t) {
            return 1.0F - (1.0F - t) * (1.0F - t);
        }
    },
    QUAD_IN_OUT {
        @Override
        public float apply(float t) {
            return t < 0.5F ? 2.0F * t * t : 1.0F - 2.0F * (1.0F - t) * (1.0F - t);
        }
    },
    CUBIC_IN {
        @Override
        public float apply(float t) {
            return t * t * t;
        }
    },
    CUBIC_OUT {
        @Override
        public float apply(float t) {
            float inv = 1.0F - t;
            return 1.0F - inv * inv * inv;
        }
    },
    CUBIC_IN_OUT {
        @Override
        public float apply(float t) {
            return t < 0.5F ? 4.0F * t * t * t : 1.0F - 4.0F * (1.0F - t) * (1.0F - t) * (1.0F - t);
        }
    },
    QUART_IN {
        @Override
        public float apply(float t) {
            return t * t * t * t;
        }
    },
    QUART_OUT {
        @Override
        public float apply(float t) {
            float inv = 1.0F - t;
            return 1.0F - inv * inv * inv * inv;
        }
    },
    QUART_IN_OUT {
        @Override
        public float apply(float t) {
            return t < 0.5F ? 8.0F * t * t * t * t : 1.0F - 8.0F * (1.0F - t) * (1.0F - t) * (1.0F - t) * (1.0F - t);
        }
    },
    SINE_IN {
        @Override
        public float apply(float t) {
            return 1.0F - (float) Math.cos(t * Math.PI * 0.5F);
        }
    },
    SINE_OUT {
        @Override
        public float apply(float t) {
            return (float) Math.sin(t * Math.PI * 0.5F);
        }
    },
    SINE_IN_OUT {
        @Override
        public float apply(float t) {
            return -(float) (Math.cos(Math.PI * t) - 1.0F) * 0.5F;
        }
    },
    EXPO_IN {
        @Override
        public float apply(float t) {
            return t <= 0.0F ? 0.0F : (float) Math.pow(2.0F, 10.0F * (t - 1.0F));
        }
    },
    EXPO_OUT {
        @Override
        public float apply(float t) {
            return t >= 1.0F ? 1.0F : 1.0F - (float) Math.pow(2.0F, -10.0F * t);
        }
    },
    CIRC_IN {
        @Override
        public float apply(float t) {
            return 1.0F - (float) Math.sqrt(Math.max(0.0F, 1.0F - t * t));
        }
    },
    CIRC_OUT {
        @Override
        public float apply(float t) {
            return (float) Math.sqrt(Math.max(0.0F, 1.0F - (1.0F - t) * (1.0F - t)));
        }
    },
    BACK_OUT {
        @Override
        public float apply(float t) {
            float c1 = 1.70158F;
            float c3 = c1 + 1.0F;
            float inv = t - 1.0F;
            return 1.0F + c3 * inv * inv * inv + c1 * inv * inv;
        }
    },
    ELASTIC_OUT {
        @Override
        public float apply(float t) {
            if (t <= 0.0F) {
                return 0.0F;
            }
            if (t >= 1.0F) {
                return 1.0F;
            }
            float c4 = (2.0F * (float) Math.PI) / 3.0F;
            return (float) Math.pow(2.0F, -10.0F * t) * (float) Math.sin((t * 10.0F - 0.75F) * c4) + 1.0F;
        }
    },
    BOUNCE_OUT {
        @Override
        public float apply(float t) {
            float n1 = 7.5625F;
            float d1 = 2.75F;
            if (t < 1.0F / d1) {
                return n1 * t * t;
            }
            if (t < 2.0F / d1) {
                float inv = t - 1.5F / d1;
                return n1 * inv * inv + 0.75F;
            }
            if (t < 2.5F / d1) {
                float inv = t - 2.25F / d1;
                return n1 * inv * inv + 0.9375F;
            }
            float inv = t - 2.625F / d1;
            return n1 * inv * inv + 0.984375F;
        }
    };

    /**
     * Maps the normalised time onto the eased progress.
     *
     * @param t normalised time in {@code [0, 1]}
     */
    public abstract float apply(float t);
}
