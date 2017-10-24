package ch.fhnw.edu.stec.util;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

public interface Glyphs {

    GlyphFont FONT_AWESOME = GlyphFontRegistry.font("FontAwesome");

    Glyph REFRESH = FONT_AWESOME.create(FontAwesome.Glyph.REFRESH);
    Glyph CAMERA = FONT_AWESOME.create(FontAwesome.Glyph.CAMERA);
    Glyph PLUS = FONT_AWESOME.create(FontAwesome.Glyph.PLUS);
    Glyph SAVE = FONT_AWESOME.create(FontAwesome.Glyph.SAVE);
    Glyph RESET = FONT_AWESOME.create(FontAwesome.Glyph.UNDO);

}