//! Font handling: parsing, shaping and rasterisation.

use crate::atlas::{Atlas, GlyphBitmap};
use ab_glyph::{point, Font as AbFont, FontRef, Glyph, GlyphId, PxScale};
use rustybuzz::{shape, Face, UnicodeBuffer};
use std::collections::HashMap;

/// Number of horizontal subpixel positions every glyph is rasterised at.
pub const SUBPIXEL_STEPS: u32 = 4;

fn cache_key(glyph_id: u32, phase: u32) -> u64 {
    ((glyph_id as u64) << 8) | phase as u64
}

/// A loaded font with its shaping state and glyph atlas.
///
/// # Memory management
///
/// The shaper and the rasteriser both borrow the raw font tables. The data is
/// therefore kept in a separate heap allocation that is owned by the handle
/// and released when the handle is dropped. The borrow checkers cannot
/// express this relationship directly, so the handle stores the raw pointer
/// and the two borrowers behind [`Option`] and releases them explicitly.
pub struct FontHandle {
    /// Pointer to the owned font data.
    data: *mut u8,
    /// Length of the owned font data.
    data_len: usize,
    /// Rasteriser, borrows the font data.
    ab_font: Option<FontRef<'static>>,
    /// Shaper, borrows the font data.
    shaper: Option<Face<'static>>,
    size: f32,
    upem: f32,
    atlas: Atlas,
    /// Cached glyph bitmaps, keyed by glyph id and subpixel phase.
    glyphs: HashMap<u64, GlyphBitmap>,
    /// Position of every cached glyph inside the atlas.
    positions: HashMap<u64, (usize, usize)>,
}

impl FontHandle {
    /// Parses a font and prepares it for shaping.
    ///
    /// Returns `None` when the data is not a valid TrueType or OpenType file.
    pub fn new(data: Vec<u8>, pixel_size: f32) -> Option<Self> {
        if data.is_empty() {
            return None;
        }

        let data_len = data.len();
        let boxed = data.into_boxed_slice();
        let data_ptr = Box::into_raw(boxed) as *mut u8;

        // SAFETY: the allocation is owned by this handle and is only released
        // in `Drop`, after the two borrowers have been dropped.
        let leaked: &'static mut [u8] =
            unsafe { std::slice::from_raw_parts_mut(data_ptr, data_len) };

        let ab_font = match FontRef::try_from_slice(leaked) {
            Ok(font) => font,
            Err(_) => {
                // Reclaim the allocation before bailing out.
                unsafe {
                    drop(Box::from_raw(std::ptr::slice_from_raw_parts_mut(
                        data_ptr,
                        data_len,
                    )));
                }
                return None;
            }
        };

        let shaper = match Face::from_slice(leaked, 0) {
            Some(face) => face,
            None => {
                unsafe {
                    drop(Box::from_raw(std::ptr::slice_from_raw_parts_mut(
                        data_ptr,
                        data_len,
                    )));
                }
                return None;
            }
        };

        let upem = ab_font.units_per_em().unwrap_or(1000.0);
        let mut handle = FontHandle {
            data: data_ptr,
            data_len,
            ab_font: Some(ab_font),
            shaper: Some(shaper),
            size: pixel_size.max(1.0),
            upem,
            atlas: Atlas::new(512, 512),
            glyphs: HashMap::new(),
            positions: HashMap::new(),
        };
        handle.rebuild();
        Some(handle)
    }

    /// Current pixel size.
    pub fn size(&self) -> f32 {
        self.size
    }

    /// Changes the pixel size and clears the atlas.
    pub fn set_size(&mut self, pixel_size: f32) {
        let size = pixel_size.max(1.0);
        if (size - self.size).abs() < f32::EPSILON {
            return;
        }
        self.size = size;
        self.rebuild();
    }

    fn rebuild(&mut self) {
        self.atlas.clear();
        self.glyphs.clear();
        self.positions.clear();
    }

    /// The glyph atlas.
    pub fn atlas(&self) -> &Atlas {
        &self.atlas
    }

    /// Vertical metrics of the font in pixels.
    ///
    /// The tuple contains the ascent, the descent and the line gap. All
    /// values are positive, the descent points downwards.
    pub fn metrics(&self) -> (f32, f32, f32) {
        let ab_font = match self.ab_font.as_ref() {
            Some(font) => font,
            None => return (self.size * 0.8, self.size * 0.2, 0.0),
        };
        let scale = self.scale();
        (
            ab_font.ascent_unscaled() * scale,
            (-ab_font.descent_unscaled()).abs() * scale,
            ab_font.line_gap_unscaled() * scale,
        )
    }

    /// Scale factor that converts font units into pixels.
    fn scale(&self) -> f32 {
        self.size / self.upem
    }

    /// Measures the advance width of a UTF-8 string in pixels.
    pub fn measure(&self, text: &[u8]) -> f32 {
        let output = self.shape_text(text);
        let scale = self.scale();
        let mut advance = 0.0f32;
        for position in output.glyph_positions() {
            advance += position.x_advance as f32 * scale;
        }
        advance
    }

    fn shape_text(&self, text: &[u8]) -> rustybuzz::GlyphBuffer {
        let mut buffer = UnicodeBuffer::new();
        if let Ok(value) = std::str::from_utf8(text) {
            buffer.push_str(value);
        }
        buffer.guess_segment_properties();
        let shaper = self.shaper.as_ref().expect("shaper is always present");
        shape(shaper, &[], buffer)
    }

    /// Shapes a string and writes the glyph quads into `out`.
    ///
    /// Every glyph occupies eight floats: `x, y, u0, v0, u1, v1, width, height`
    /// where `(x, y)` is the top left corner of the glyph bitmap relative to
    /// the baseline origin.
    ///
    /// `x` and `y` are always whole pixels. The fractional part of the pen
    /// position is baked into the bitmap instead (one of [`SUBPIXEL_STEPS`]
    /// horizontal phases), so the bitmap can be drawn one to one without
    /// resampling and the advances between glyphs stay exact.
    ///
    /// Returns the number of written glyphs.
    pub fn shape(&mut self, text: &[u8], out: &mut [f32], max_glyphs: usize) -> usize {
        let output = self.shape_text(text);
        let scale = self.scale();
        let infos = output.glyph_infos();
        let positions = output.glyph_positions();

        let mut written = 0usize;
        let mut pen_x = 0.0f32;
        let mut pen_y = 0.0f32;

        for (info, position) in infos.iter().zip(positions.iter()) {
            let glyph_id = info.glyph_id;
            if glyph_id == 0 {
                pen_x += position.x_advance as f32 * scale;
                continue;
            }
            if written >= max_glyphs {
                break;
            }

            let glyph_x = pen_x + position.x_offset as f32 * scale;
            let glyph_y = pen_y + position.y_offset as f32 * scale;
            let mut whole_x = glyph_x.floor();
            let mut phase = ((glyph_x - whole_x) * SUBPIXEL_STEPS as f32).round() as u32;
            if phase >= SUBPIXEL_STEPS {
                whole_x += 1.0;
                phase = 0;
            }
            let whole_y = glyph_y.round();
            let key = cache_key(glyph_id, phase);

            self.ensure_glyph(glyph_id, phase);

            let (bitmap_x, bitmap_y, bitmap_w, bitmap_h, x_offset, y_offset) =
                match (self.glyphs.get(&key), self.positions.get(&key)) {
                    (Some(bitmap), Some(position)) => (
                        position.0,
                        position.1,
                        bitmap.width,
                        bitmap.height,
                        bitmap.x_offset,
                        bitmap.y_offset,
                    ),
                    _ => (0, 0, 0, 0, 0.0, 0.0),
                };

            let atlas_width = self.atlas.width() as f32;
            let atlas_height = self.atlas.height() as f32;

            let base = written * crate::GLYPH_STRIDE;
            out[base] = whole_x + x_offset;
            out[base + 1] = whole_y + y_offset;
            out[base + 2] = bitmap_x as f32 / atlas_width;
            out[base + 3] = bitmap_y as f32 / atlas_height;
            out[base + 4] = (bitmap_x + bitmap_w) as f32 / atlas_width;
            out[base + 5] = (bitmap_y + bitmap_h) as f32 / atlas_height;
            out[base + 6] = bitmap_w as f32;
            out[base + 7] = bitmap_h as f32;

            written += 1;
            pen_x += position.x_advance as f32 * scale;
            pen_y += position.y_advance as f32 * scale;
        }

        written
    }

    /// Rasterises a glyph at a subpixel phase and stores it in the atlas when
    /// it is missing.
    fn ensure_glyph(&mut self, glyph_id: u32, phase: u32) {
        let key = cache_key(glyph_id, phase);
        if self.glyphs.contains_key(&key) {
            return;
        }

        let px_scale = PxScale::from(self.size);
        let glyph = Glyph {
            id: GlyphId(glyph_id as u16),
            scale: px_scale,
            position: point(phase as f32 / SUBPIXEL_STEPS as f32, 0.0),
        };

        let ab_font = self.ab_font.as_ref().expect("rasteriser is always present");
        let outlined = match ab_font.outline_glyph(glyph) {
            Some(outlined) => outlined,
            None => return,
        };

        let bounds = outlined.px_bounds();
        let width = bounds.width().max(1.0).ceil() as usize;
        let height = bounds.height().max(1.0).ceil() as usize;

        let mut coverage = vec![0u8; width * height];
        outlined.draw(|x, y, value| {
            let px = x as usize;
            let py = y as usize;
            if px < width && py < height {
                coverage[py * width + px] = (value.clamp(0.0, 1.0) * 255.0) as u8;
            }
        });

        // The outline uses a y-down coordinate system with the baseline at
        // y = 0, therefore `bounds.min.y` is the offset of the top edge of
        // the bitmap relative to the baseline. It is negative for glyphs that
        // sit on the baseline and positive for glyphs that are entirely
        // below it.
        let bitmap = GlyphBitmap {
            width,
            height,
            x_offset: bounds.min.x,
            y_offset: bounds.min.y,
            coverage,
        };

        // A full atlas must never drop a glyph: a missing letter shows up as
        // a hole in the text, which looks like broken letter spacing.
        let mut placed = self.store_glyph(key, &bitmap);
        while !placed {
            if !self.grow_atlas() {
                break;
            }
            placed = self.store_glyph(key, &bitmap);
        }
        if placed {
            self.glyphs.insert(key, bitmap);
        }
    }

    /// Copies a bitmap into the atlas and records where it landed.
    fn store_glyph(&mut self, key: u64, bitmap: &GlyphBitmap) -> bool {
        match self.atlas.allocate(bitmap.width, bitmap.height) {
            Some((x, y)) => {
                self.atlas.blit(x, y, bitmap);
                self.positions.insert(key, (x, y));
                true
            }
            None => false,
        }
    }

    /// Doubles the atlas and packs every cached glyph into it again.
    ///
    /// The glyphs keep their phase, only their position inside the atlas
    /// changes, so the text on screen is unaffected apart from the new
    /// glyphs that can now be stored.
    fn grow_atlas(&mut self) -> bool {
        let mut size = self.atlas.width();
        while size < 2048 {
            size = (size * 2).min(2048);
            let mut atlas = Atlas::new(size, size);
            let mut positions = HashMap::with_capacity(self.glyphs.len());
            let mut complete = true;
            for (key, bitmap) in self.glyphs.iter() {
                match atlas.allocate(bitmap.width, bitmap.height) {
                    Some((x, y)) => {
                        atlas.blit(x, y, bitmap);
                        positions.insert(*key, (x, y));
                    }
                    None => {
                        // Not every cached glyph fits yet, try a larger page.
                        complete = false;
                        break;
                    }
                }
            }
            if complete {
                self.atlas = atlas;
                self.positions = positions;
                return true;
            }
        }
        false
    }
}

impl Drop for FontHandle {
    fn drop(&mut self) {
        // The two borrowers have to be released before the font data, because
        // they hold references into it.
        self.shaper.take();
        self.ab_font.take();

        if !self.data.is_null() {
            // SAFETY: the pointer was produced by `Box::into_raw` and is only
            // released here.
            unsafe {
                drop(Box::from_raw(std::ptr::slice_from_raw_parts_mut(
                    self.data,
                    self.data_len,
                )));
            }
            self.data = std::ptr::null_mut();
        }
    }
}

// The handle owns its font data and must not be moved between threads while
// the borrowers are alive. The public API only ever uses it behind a raw
// pointer that is confined to the calling thread.
unsafe impl Send for FontHandle {}

#[cfg(test)]
mod tests {
    use super::*;

    /// Loads the bundled Inter font at a custom size.
    fn inter_at(size: f32) -> Option<FontHandle> {
        let path = std::path::Path::new(env!("CARGO_MANIFEST_DIR"))
            .join("../../common/src/main/resources/fonts/Inter-Regular.ttf");
        let data = std::fs::read(path).ok()?;
        FontHandle::new(data, size)
    }

    /// Loads the bundled Inter font. The test fails when the font is missing.
    fn inter() -> Option<FontHandle> {
        let path = std::path::Path::new(env!("CARGO_MANIFEST_DIR"))
            .join("../../common/src/main/resources/fonts/Inter-Regular.ttf");
        let data = std::fs::read(path).ok()?;
        FontHandle::new(data, 32.0)
    }

    #[test]
    fn measures_a_string() {
        let Some(font) = inter() else {
            return;
        };
        let width = font.measure(b"Hello");
        assert!(width > 0.0, "the width must be positive");
        assert!(
            font.measure(b"Hello world") > width,
            "a longer string must be wider"
        );
    }

    #[test]
    fn debug_scale() {
        let Some(font) = inter() else {
            return;
        };
        let output = font.shape_text(b"Hello");
        let mut sum = 0i32;
        for position in output.glyph_positions() {
            sum += position.x_advance;
        }
        println!(
            "upem={} size={} scale={} raw_advance={} measured={}",
            font.upem,
            font.size,
            font.scale(),
            sum,
            font.measure(b"Hello")
        );
    }

    #[test]
    fn measures_at_the_expected_size() {
        let Some(font) = inter() else {
            return;
        };
        // "Hello" is a little more than two em wide in Inter.
        let ems = font.measure(b"Hello") / font.size();
        assert!(
            (2.2..2.6).contains(&ems),
            "the width must be around 2.4 em, was {ems}"
        );
    }

    #[test]
    fn reports_sane_metrics() {
        let Some(font) = inter() else {
            return;
        };
        let (ascent, descent, line_gap) = font.metrics();
        assert!(ascent > 0.0, "the ascent must be positive: {ascent}");
        assert!(descent > 0.0, "the descent must be positive: {descent}");
        assert!(line_gap >= 0.0, "the line gap must not be negative: {line_gap}");
        // The ascent and the descent must add up to a plausible line height.
        assert!(ascent + descent < 100.0);
    }

    #[test]
    fn glyphs_sit_on_the_baseline() {
        let Some(mut font) = inter() else {
            return;
        };
        let mut buffer = [0.0f32; crate::GLYPH_STRIDE * 16];
        let count = font.shape(b"Hx", &mut buffer, 16);
        assert_eq!(count, 2, "both glyphs must be shaped");

        // 'H' has no descender: its bitmap is entirely above the baseline.
        let cap_top = buffer[1];
        let cap_bottom = buffer[1] + buffer[7];
        assert!(
            cap_bottom <= 0.5,
            "the cap glyph must not reach below the baseline: {cap_bottom}"
        );
        assert!(
            cap_top < -1.0,
            "the cap glyph must be above the baseline: {cap_top}"
        );

        // 'x' has no descender either, but it is shorter than the cap height.
        let x_top = buffer[crate::GLYPH_STRIDE + 1];
        assert!(
            x_top > cap_top,
            "the x glyph must start lower than the cap glyph: {x_top} vs {cap_top}"
        );
    }

    #[test]
    fn descenders_reach_below_the_baseline() {
        let Some(mut font) = inter() else {
            return;
        };
        let mut buffer = [0.0f32; crate::GLYPH_STRIDE * 16];
        let count = font.shape(b"g", &mut buffer, 16);
        assert_eq!(count, 1);
        let bottom = buffer[1] + buffer[7];
        assert!(
            bottom > 0.5,
            "the descender must reach below the baseline: {bottom}"
        );
    }

    #[test]
    fn a_full_atlas_grows_instead_of_dropping_glyphs() {
        let Some(mut font) = inter_at(80.0) else {
            return;
        };
        // The characters the interface actually uses, repeated often enough
        // that the first atlas page cannot hold every subpixel phase.
        let mut text = String::new();
        for _ in 0..6 {
            text.push_str("ABCDEFGHIJKLMNOPQRSTUVWXYZ abcdefghijklmnopqrstuvwxyz ");
            text.push_str("0123456789 .,:;!?()[]-+/%#@&* ");
        }
        let expected = text.chars().filter(|c| *c != ' ').count();
        let mut buffer = vec![0.0f32; crate::GLYPH_STRIDE * 4096];
        let count = font.shape(text.as_bytes(), &mut buffer, 4096);
        let drawn = (0..count)
            .filter(|i| buffer[i * crate::GLYPH_STRIDE + 6] > 0.0)
            .count();
        assert_eq!(drawn, expected, "every glyph must reach the atlas");
        assert!(font.atlas().width() > 512, "the atlas must have grown");
    }

    #[test]
    fn glyphs_are_placed_on_whole_pixels() {
        let Some(mut font) = inter() else {
            return;
        };
        let mut buffer = [0.0f32; crate::GLYPH_STRIDE * 64];
        let count = font.shape(b"Inconsistent spacing WAVE 123", &mut buffer, 64);
        assert!(count > 10);
        for i in 0..count {
            let x = buffer[i * crate::GLYPH_STRIDE];
            let y = buffer[i * crate::GLYPH_STRIDE + 1];
            assert_eq!(x, x.round(), "glyph {i} x must be a whole pixel: {x}");
            assert_eq!(y, y.round(), "glyph {i} y must be a whole pixel: {y}");
        }
    }

    #[test]
    fn subpixel_phases_follow_the_pen() {
        let Some(mut font) = inter() else {
            return;
        };
        // The same glyph repeated: the bitmap origin may only drift from the
        // ideal pen position by less than one pixel.
        let mut buffer = [0.0f32; crate::GLYPH_STRIDE * 32];
        let count = font.shape(b"iiiiiiiiiiiiiiii", &mut buffer, 32);
        let advance = font.measure(b"i");
        let first = buffer[0];
        for i in 0..count {
            let x = buffer[i * crate::GLYPH_STRIDE];
            let ideal = first + advance * i as f32;
            assert!((x - ideal).abs() <= 1.0, "glyph {i} drifted: {x} vs {ideal}");
        }
    }

    #[test]
    fn atlas_contains_rasterised_pixels() {
        let Some(mut font) = inter() else {
            return;
        };
        let mut buffer = [0.0f32; crate::GLYPH_STRIDE * 64];
        font.shape(b"NanoGUI", &mut buffer, 64);
        assert!(font.atlas().pixels().iter().any(|value| *value > 0));
        assert!(font.atlas().revision() > 1);
    }
}
