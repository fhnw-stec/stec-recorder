package ch.fhnw.edu.stec.util;

import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

public interface Glyphs {

    GlyphFont FONT_AWESOME = GlyphFontRegistry.font("FontAwesome");

    // Glyphs are mutable – don't be tempted to cache them here...

}