//! Glyph atlas used by the font engine.

/// A single channel coverage bitmap of a rasterised glyph.
#[derive(Clone)]
pub struct GlyphBitmap {
    /// Width of the bitmap in pixels.
    pub width: usize,
    /// Height of the bitmap in pixels.
    pub height: usize,
    /// Horizontal offset of the bitmap relative to the pen position.
    pub x_offset: f32,
    /// Vertical offset of the bitmap relative to the baseline.
    pub y_offset: f32,
    /// Anti aliased coverage values, one byte per pixel.
    pub coverage: Vec<u8>,
}

/// Texture atlas that stores the rasterised glyphs.
///
/// The atlas is a single channel coverage map. The renderer multiplies it
/// with the text colour, which keeps the memory footprint at one byte per
/// pixel and allows the same atlas to be used for every text colour.
pub struct Atlas {
    width: usize,
    height: usize,
    pixels: Vec<u8>,
    pen_x: usize,
    pen_y: usize,
    row_height: usize,
    full: bool,
    revision: u32,
}

impl Atlas {
    /// Creates an empty atlas.
    pub fn new(width: usize, height: usize) -> Self {
        Atlas {
            width,
            height,
            pixels: vec![0u8; width * height],
            pen_x: 1,
            pen_y: 1,
            row_height: 0,
            full: false,
            revision: 1,
        }
    }

    /// Width of the atlas in pixels.
    pub fn width(&self) -> usize {
        self.width
    }

    /// Height of the atlas in pixels.
    pub fn height(&self) -> usize {
        self.height
    }

    /// Counter that changes whenever the pixel data changed.
    pub fn revision(&self) -> u32 {
        self.revision
    }

    /// Raw coverage data of the atlas.
    pub fn pixels(&self) -> &[u8] {
        &self.pixels
    }

    /// True when the atlas cannot hold any more glyphs.
    pub fn is_full(&self) -> bool {
        self.full
    }

    /// Reserves space for a glyph and returns its position in the atlas.
    ///
    /// Returns `None` when the atlas is full.
    pub fn allocate(&mut self, width: usize, height: usize) -> Option<(usize, usize)> {
        if self.full || width == 0 || height == 0 {
            return None;
        }
        if self.pen_x + width + 1 > self.width {
            self.pen_x = 1;
            self.pen_y += self.row_height + 1;
            self.row_height = 0;
        }
        if self.pen_y + height + 1 > self.height {
            self.full = true;
            return None;
        }
        let x = self.pen_x;
        let y = self.pen_y;
        self.pen_x += width + 1;
        self.row_height = self.row_height.max(height);
        Some((x, y))
    }

    /// Copies a coverage bitmap into the atlas.
    pub fn blit(&mut self, x: usize, y: usize, bitmap: &GlyphBitmap) {
        for row in 0..bitmap.height {
            let target = (y + row) * self.width + x;
            let source = row * bitmap.width;
            let length = bitmap.width.min(self.width.saturating_sub(x));
            self.pixels[target..target + length]
                .copy_from_slice(&bitmap.coverage[source..source + length]);
        }
        self.revision = self.revision.wrapping_add(1);
    }

    /// Removes every glyph from the atlas.
    pub fn clear(&mut self) {
        self.pixels.iter_mut().for_each(|pixel| *pixel = 0);
        self.pen_x = 1;
        self.pen_y = 1;
        self.row_height = 0;
        self.full = false;
        self.revision = self.revision.wrapping_add(1);
    }
}
