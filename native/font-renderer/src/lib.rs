//! High performance font engine for the NanoGUI GUI framework.
//!
//! The crate exposes a small C ABI so that it can be used from Java through
//! JNI without any additional glue code. Everything is implemented in pure
//! Rust: the font parsing uses `ttf-parser`, the text shaping uses
//! `rustybuzz` (a pure Rust port of HarfBuzz) and the glyph rasterisation
//! uses `ab_glyph`. No system libraries are required, which keeps the
//! binaries small and makes them portable across every supported platform.
//!
//! # Layout
//!
//! * [`font`] parses, shapes and rasterises fonts.
//! * [`atlas`] packs the rasterised glyphs into a texture atlas.
//! * [`jni`] exposes the whole engine to Java (enabled by the `jni` feature).
//!
//! # Example
//!
//! ```no_run
//! let bytes = std::fs::read("font.ttf").unwrap();
//! let mut font = nanogui_font::FontHandle::new(bytes, 16.0).unwrap();
//! let mut quads = [0.0f32; 8 * 64];
//! let count = font.shape(b"Hello", &mut quads, 64);
//! assert!(count > 0);
//! ```

mod atlas;
mod font;

#[cfg(feature = "jni")]
mod jni;

pub use atlas::Atlas;
pub use font::FontHandle;

/// Number of floats that the shape buffer uses per glyph.
pub const GLYPH_STRIDE: usize = 8;

/// Version of the native library. The Java side checks it against the
/// expected version and refuses to load a mismatching library.
pub const NATIVE_VERSION: i32 = 1;

/// Creates a font from the raw contents of a TrueType or OpenType file.
///
/// Returns a null pointer when the data cannot be parsed.
#[no_mangle]
pub extern "C" fn nano_font_create(data: *const u8, len: usize, pixel_size: f32) -> *mut FontHandle {
    if data.is_null() || len == 0 {
        return std::ptr::null_mut();
    }
    let slice = unsafe { std::slice::from_raw_parts(data, len) };
    match FontHandle::new(slice.to_vec(), pixel_size) {
        Some(handle) => Box::into_raw(Box::new(handle)),
        None => std::ptr::null_mut(),
    }
}

/// Destroys a font.
#[no_mangle]
pub extern "C" fn nano_font_destroy(font: *mut FontHandle) {
    if font.is_null() {
        return;
    }
    unsafe {
        drop(Box::from_raw(font));
    }
}

/// Changes the pixel size of a font. The glyph atlas is rebuilt lazily.
#[no_mangle]
pub extern "C" fn nano_font_set_size(font: *mut FontHandle, pixel_size: f32) {
    if font.is_null() {
        return;
    }
    unsafe {
        (*font).set_size(pixel_size);
    }
}

/// Returns the current pixel size.
#[no_mangle]
pub extern "C" fn nano_font_get_size(font: *mut FontHandle) -> f32 {
    if font.is_null() {
        return 0.0;
    }
    unsafe { (*font).size() }
}

/// Measures the advance width of a UTF-8 encoded string.
#[no_mangle]
pub extern "C" fn nano_font_measure(font: *mut FontHandle, text: *const u8, len: usize) -> f32 {
    if font.is_null() || text.is_null() {
        return 0.0;
    }
    let slice = unsafe { std::slice::from_raw_parts(text, len) };
    unsafe { (*font).measure(slice) }
}

/// Shapes a UTF-8 encoded string and writes the glyph quads into `out`.
///
/// Every glyph occupies [`GLYPH_STRIDE`] floats:
/// `x, y, u0, v0, u1, v1, width, height` where `(x, y)` is the top left
/// corner of the glyph bitmap relative to the baseline origin.
///
/// Returns the number of glyphs that were written.
#[no_mangle]
pub extern "C" fn nano_font_shape(
    font: *mut FontHandle,
    text: *const u8,
    len: usize,
    out: *mut f32,
    max_glyphs: usize,
) -> usize {
    if font.is_null() || text.is_null() || out.is_null() || max_glyphs == 0 {
        return 0;
    }
    let slice = unsafe { std::slice::from_raw_parts(text, len) };
    let buffer = unsafe { std::slice::from_raw_parts_mut(out, max_glyphs * GLYPH_STRIDE) };
    unsafe { (*font).shape(slice, buffer, max_glyphs) }
}

/// Returns a pointer to the single channel coverage data of the glyph atlas.
///
/// The width, height and revision are written to the supplied pointers. The
/// revision changes whenever the atlas content changed, which allows the
/// Java side to upload the texture only when necessary.
#[no_mangle]
pub extern "C" fn nano_font_atlas_coverage(
    font: *mut FontHandle,
    width: *mut u32,
    height: *mut u32,
    revision: *mut u32,
) -> *const u8 {
    if font.is_null() {
        return std::ptr::null();
    }
    unsafe {
        let atlas = (*font).atlas();
        if !width.is_null() {
            *width = atlas.width() as u32;
        }
        if !height.is_null() {
            *height = atlas.height() as u32;
        }
        if !revision.is_null() {
            *revision = atlas.revision();
        }
        atlas.pixels().as_ptr()
    }
}

/// Returns the version of the native library.
#[no_mangle]
pub extern "C" fn nano_font_version() -> i32 {
    NATIVE_VERSION
}

/// Writes the vertical metrics of a font into `out`.
///
/// The three values are the ascent, the descent and the line gap, all in
/// pixels. Returns `false` when the font handle is invalid.
#[no_mangle]
pub extern "C" fn nano_font_metrics(font: *mut FontHandle, out: *mut f32) -> bool {
    if font.is_null() || out.is_null() {
        return false;
    }
    let (ascent, descent, line_gap) = unsafe { (*font).metrics() };
    unsafe {
        *out = ascent;
        *out.add(1) = descent;
        *out.add(2) = line_gap;
    }
    true
}
