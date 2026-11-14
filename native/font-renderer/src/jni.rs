//! JNI bindings for the native font engine.
//!
//! The Java class `dev.nanomango.gui.font.NativeFont` declares the native
//! methods that are implemented here. Every entry point catches panics so
//! that a failure in the renderer can never take the whole game down.

use crate::font::FontHandle;
use jni::objects::{JByteArray, JClass, JFloatArray, JIntArray};
use jni::sys::{jfloat, jint, jlong};
use jni::{errors::Result as JResult, JNIEnv};

/// Throws a Java exception.
fn throw(env: &mut JNIEnv, class: &str, message: &str) -> JResult<()> {
    let class = env.find_class(class)?;
    env.throw_new(class, message)
}

/// Runs a closure and converts a panic into a Java exception.
fn guarded<T>(
    env: &mut JNIEnv,
    class: &str,
    action: impl FnOnce(&mut JNIEnv) -> JResult<T>,
) -> JResult<T> {
    match std::panic::catch_unwind(std::panic::AssertUnwindSafe(|| action(env))) {
        Ok(result) => result,
        Err(_) => {
            let _ = throw(env, class, "native font renderer panicked");
            Err(jni::errors::Error::NullPtr("native font renderer panicked"))
        }
    }
}

unsafe fn font<'a>(raw: jlong) -> Option<&'a mut FontHandle> {
    if raw == 0 {
        None
    } else {
        Some(&mut *(raw as *mut FontHandle))
    }
}

/// Creates a font from the raw contents of a TrueType or OpenType file.
///
/// Returns `0` when the data cannot be parsed.
#[no_mangle]
pub extern "system" fn Java_dev_nanomango_gui_font_NativeFont_nCreate(
    mut env: JNIEnv,
    _class: JClass,
    data: JByteArray,
    pixel_size: jfloat,
) -> jlong {
    guarded(
        &mut env,
        "java/lang/IllegalArgumentException",
        |env| {
            let length = env.get_array_length(&data)?;
            if length <= 0 {
                return Ok(0);
            }
            // SAFETY: the array is not modified while the elements are read.
            let bytes = unsafe { env.get_array_elements(&data, jni::objects::ReleaseMode::NoCopyBack) }?;
            // SAFETY: the pointer comes from a live JNI array of `length`
            // elements and is only read while the guard is alive.
            let slice = unsafe {
                std::slice::from_raw_parts(bytes.as_ptr() as *const u8, length as usize)
            };
            let owned = slice.to_vec();
            Ok(match FontHandle::new(owned, pixel_size) {
                Some(handle) => Box::into_raw(Box::new(handle)) as jlong,
                None => 0,
            })
        },
    )
    .unwrap_or(0)
}

/// Destroys a font.
#[no_mangle]
pub extern "system" fn Java_dev_nanomango_gui_font_NativeFont_nDestroy(
    mut env: JNIEnv,
    _class: JClass,
    raw: jlong,
) {
    let _ = guarded(&mut env, "java/lang/RuntimeException", |_| {
        if raw != 0 {
            // SAFETY: the pointer was produced by `Box::into_raw` and is only
            // released here.
            unsafe {
                drop(Box::from_raw(raw as *mut FontHandle));
            }
        }
        Ok(())
    });
}

/// Changes the pixel size of a font.
#[no_mangle]
pub extern "system" fn Java_dev_nanomango_gui_font_NativeFont_nSetSize(
    mut env: JNIEnv,
    _class: JClass,
    raw: jlong,
    pixel_size: jfloat,
) {
    let _ = guarded(&mut env, "java/lang/RuntimeException", |_| {
        if let Some(handle) = unsafe { font(raw) } {
            handle.set_size(pixel_size);
        }
        Ok(())
    });
}

/// Measures the advance width of a UTF-8 encoded string.
///
/// The length is passed explicitly because the Java side reuses a buffer that
/// can be larger than the encoded text.
#[no_mangle]
pub extern "system" fn Java_dev_nanomango_gui_font_NativeFont_nMeasure(
    mut env: JNIEnv,
    _class: JClass,
    raw: jlong,
    text: JByteArray,
    length: jint,
) -> jfloat {
    guarded(&mut env, "java/lang/RuntimeException", |env| {
        let handle = match unsafe { font(raw) } {
            Some(handle) => handle,
            None => return Ok(0.0),
        };
        let count = length.max(0) as usize;
        if count == 0 {
            return Ok(0.0);
        }
        // SAFETY: the array is not modified while the elements are read.
        let bytes = unsafe { env.get_array_elements(&text, jni::objects::ReleaseMode::NoCopyBack) }?;
        // SAFETY: the pointer comes from a live JNI array with at least
        // `count` elements and is only read while the guard is alive.
        let slice = unsafe {
            std::slice::from_raw_parts(bytes.as_ptr() as *const u8, count.min(bytes.len()))
        };
        Ok(handle.measure(slice))
    })
    .unwrap_or(0.0)
}

/// Shapes a UTF-8 encoded string and writes the glyph quads into `out`.
///
/// The length is passed explicitly because the Java side reuses a buffer that
/// can be larger than the encoded text.
///
/// Returns the number of written glyphs.
#[no_mangle]
pub extern "system" fn Java_dev_nanomango_gui_font_NativeFont_nShape(
    mut env: JNIEnv,
    _class: JClass,
    raw: jlong,
    text: JByteArray,
    length: jint,
    out: JFloatArray,
    max_glyphs: jint,
) -> jint {
    guarded(&mut env, "java/lang/RuntimeException", |env| {
        let handle = match unsafe { font(raw) } {
            Some(handle) => handle,
            None => return Ok(0),
        };
        if max_glyphs <= 0 {
            return Ok(0);
        }

        let count = length.max(0) as usize;
        let mut utf8 = Vec::with_capacity(count);
        if count > 0 {
            // SAFETY: the array is not modified while the elements are read.
            let bytes = unsafe { env.get_array_elements(&text, jni::objects::ReleaseMode::NoCopyBack) }?;
            // SAFETY: the pointer comes from a live JNI array with at least
            // `count` elements and is only read while the guard is alive.
            utf8.extend_from_slice(unsafe {
                std::slice::from_raw_parts(bytes.as_ptr() as *const u8, count.min(bytes.len()))
            });
        }

        let mut buffer = vec![0.0f32; max_glyphs as usize * crate::GLYPH_STRIDE];
        let written = handle.shape(&utf8, &mut buffer, max_glyphs as usize);

        if written > 0 {
            let count = written * crate::GLYPH_STRIDE;
            env.set_float_array_region(&out, 0, &buffer[..count])?;
        }
        Ok(written as jint)
    })
    .unwrap_or(0)
}

/// Width of the glyph atlas.
#[no_mangle]
pub extern "system" fn Java_dev_nanomango_gui_font_NativeFont_nAtlasWidth(
    mut env: JNIEnv,
    _class: JClass,
    raw: jlong,
) -> jint {
    guarded(&mut env, "java/lang/RuntimeException", |_| {
        Ok(match unsafe { font(raw) } {
            Some(handle) => handle.atlas().width() as jint,
            None => 0,
        })
    })
    .unwrap_or(0)
}

/// Height of the glyph atlas.
#[no_mangle]
pub extern "system" fn Java_dev_nanomango_gui_font_NativeFont_nAtlasHeight(
    mut env: JNIEnv,
    _class: JClass,
    raw: jlong,
) -> jint {
    guarded(&mut env, "java/lang/RuntimeException", |_| {
        Ok(match unsafe { font(raw) } {
            Some(handle) => handle.atlas().height() as jint,
            None => 0,
        })
    })
    .unwrap_or(0)
}

/// Revision counter of the glyph atlas.
#[no_mangle]
pub extern "system" fn Java_dev_nanomango_gui_font_NativeFont_nAtlasRevision(
    mut env: JNIEnv,
    _class: JClass,
    raw: jlong,
) -> jint {
    guarded(&mut env, "java/lang/RuntimeException", |_| {
        Ok(match unsafe { font(raw) } {
            Some(handle) => handle.atlas().revision() as jint,
            None => 0,
        })
    })
    .unwrap_or(0)
}

/// Copies the glyph atlas into a Java `int[]` as packed ARGB values.
///
/// The atlas stores the anti aliased coverage in a single channel. The colour
/// is applied by the renderer, therefore every pixel is written as white with
/// the coverage in the alpha channel.
#[no_mangle]
pub extern "system" fn Java_dev_nanomango_gui_font_NativeFont_nAtlasPixels(
    mut env: JNIEnv,
    _class: JClass,
    raw: jlong,
    out: JIntArray,
) {
    let _ = guarded(&mut env, "java/lang/RuntimeException", |env| {
        let handle = match unsafe { font(raw) } {
            Some(handle) => handle,
            None => return Ok(()),
        };
        let atlas = handle.atlas();
        let coverage = atlas.pixels();
        let expected = atlas.width() * atlas.height();
        let length = env.get_array_length(&out)?;
        let count = expected.min(length.max(0) as usize);

        let mut pixels = vec![0i32; count];
        for (index, value) in pixels.iter_mut().enumerate() {
            let alpha = coverage[index] as i32;
            *value = (alpha << 24) | 0x00FF_FFFF;
        }
        if count > 0 {
            env.set_int_array_region(&out, 0, &pixels)?;
        }
        Ok(())
    });
}

/// Version of the native library.
#[no_mangle]
pub extern "system" fn Java_dev_nanomango_gui_font_NativeFont_nVersion(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    crate::NATIVE_VERSION
}

/// Vertical metrics of the font, written into the supplied array.
///
/// The array receives the ascent, the descent and the line gap in pixels.
#[no_mangle]
pub extern "system" fn Java_dev_nanomango_gui_font_NativeFont_nMetrics(
    mut env: JNIEnv,
    _class: JClass,
    raw: jlong,
    out: JFloatArray,
) {
    let _ = guarded(&mut env, "java/lang/RuntimeException", |env| {
        let handle = match unsafe { font(raw) } {
            Some(handle) => handle,
            None => return Ok(()),
        };
        let (ascent, descent, line_gap) = handle.metrics();
        env.set_float_array_region(&out, 0, &[ascent, descent, line_gap])?;
        Ok(())
    });
}

/// Name of the platform the library was built for, for diagnostics.
#[no_mangle]
pub extern "system" fn Java_dev_nanomango_gui_font_NativeFont_nPlatform(
    env: JNIEnv,
    _class: JClass,
) -> jni::sys::jstring {
    match env.new_string(std::env::consts::OS) {
        Ok(value) => value.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}
