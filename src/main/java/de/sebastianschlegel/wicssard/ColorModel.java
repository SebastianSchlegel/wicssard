package de.sebastianschlegel.wicssard;

import javafx.scene.paint.Color;
import org.w3c.dom.css.CSSValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public class ColorModel {

    private final static Logger LOGGER = Logger.getLogger("ColorModel");

    final int id;

    final RgbColor rgbColor;

    final Color original;

    final List<String> info = new ArrayList<>();

    final List<CSSValue> references = new ArrayList<>();

    public ColorModel (final RgbColor rgbColor) {
        this.rgbColor = rgbColor;
        this.original = rgbColor.getColor();
        this.id = this.getRgbColor().hashCode();
    }

    public RgbColor getRgbColor () {
        return this.rgbColor;
    }

    public Color getOriginal () {
        return this.original;
    }

    public List<String> getInfo () {
        return this.info;
    }

    public List<CSSValue> getReferences () {
        return this.references;
    }

    @Override
    public boolean equals (final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        final ColorModel that = (ColorModel) o;

        if (this.id != that.id) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode () {
        return this.id;
    }

    public Collection<CSSValue> updateReferences () {
        final Collection<CSSValue> changes = new ArrayList<>();
        final String rgb = this.rgbColor.toString();
        for (final CSSValue reference : this.references) {
            if (!rgb.equals(reference.getCssText().trim())) {
                LOGGER.info("Updating " + reference.getCssText() + " with " + rgb);
                reference.setCssText(rgb);
                changes.add(reference);
            }
        }
        return changes;
    }
}
