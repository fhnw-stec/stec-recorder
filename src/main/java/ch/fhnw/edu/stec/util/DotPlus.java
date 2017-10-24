package ch.fhnw.edu.stec.util;

import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

public class DotPlus extends Group {

    private static final double GLYPH_SCALE_FACTOR = 0.08;

    public DotPlus(double radius) {

        Glyph plus = Glyphs.FONT_AWESOME.create(FontAwesome.Glyph.PLUS);

        Circle circle = new Circle(radius);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.BLACK);

        plus.translateXProperty().bind(circle.translateXProperty().subtract(plus.widthProperty().divide(2)));
        plus.translateYProperty().bind(circle.translateYProperty().subtract(plus.heightProperty().divide(2)));
        plus.scaleXProperty().bind(circle.radiusProperty().multiply(GLYPH_SCALE_FACTOR));
        plus.scaleYProperty().bind(circle.radiusProperty().multiply(GLYPH_SCALE_FACTOR));

        setCursor(Cursor.HAND);

        getChildren().addAll(circle, plus);
    }

}