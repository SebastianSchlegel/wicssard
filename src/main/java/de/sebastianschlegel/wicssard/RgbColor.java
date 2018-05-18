package de.sebastianschlegel.wicssard;

import com.steadystate.css.dom.CSSValueImpl;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.RGBColor;

public class RgbColor implements RGBColor {

    private CSSPrimitiveValue red;

    private CSSPrimitiveValue green;

    private CSSPrimitiveValue blue;

    public RgbColor (final CSSValue value) {
        final RGBColor other = ((CSSValueImpl) value).getRGBColorValue();
        this.setBlue(other.getBlue());
        this.setGreen(other.getGreen());
        this.setRed(other.getRed());
    }

    @Override
    public CSSPrimitiveValue getRed () {
        return this.red;
    }

    public void setRed (final CSSPrimitiveValue red) {
        this.red = red;
    }

    @Override
    public CSSPrimitiveValue getGreen () {
        return this.green;
    }

    public void setGreen (final CSSPrimitiveValue green) {
        this.green = green;
    }

    @Override
    public CSSPrimitiveValue getBlue () {
        return this.blue;
    }

    public void setBlue (final CSSPrimitiveValue blue) {
        this.blue = blue;
    }

    public int getRedInt () {
        return Double.valueOf(this.red.toString()).intValue();
    }

    public int getGreenInt () {
        return Double.valueOf(this.green.toString()).intValue();
    }

    public int getBlueInt () {
        return Double.valueOf(this.blue.toString()).intValue();
    }

    public void applyColor (final Color color) {
        this.red.setFloatValue(CSSPrimitiveValue.CSS_NUMBER, (float) color.getRed() * 255);
        this.green.setFloatValue(CSSPrimitiveValue.CSS_NUMBER, (float) color.getGreen() * 255);
        this.blue.setFloatValue(CSSPrimitiveValue.CSS_NUMBER, (float) color.getBlue() * 255);
    }

    @Override
    public boolean equals (final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        final RgbColor rgbColor = (RgbColor) o;

        if (this.red != null ? !this.red.toString().equals(rgbColor.red.toString()) : rgbColor.red != null) {
            return false;
        }
        if (this.green != null ? !this.green.toString().equals(rgbColor.green.toString()) : rgbColor.green != null) {
            return false;
        }
        if (this.blue != null ? !this.blue.toString().equals(rgbColor.blue.toString()) : rgbColor.blue != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode () {
        int result = this.red != null ? this.red.toString().hashCode() : 0;
        result = 31 * result + (this.green != null ? this.green.toString().hashCode() : 0);
        result = 31 * result + (this.blue != null ? this.blue.toString().hashCode() : 0);
        return result;
    }

    @Override
    public String toString () {
        return "rgb(" + this.getRedInt() + ", " + this.getGreenInt() + ", " + this.getBlueInt() + ')';
    }

    public Paint getPaint () {
        return Paint.valueOf(this.toString());
    }

    public Color getColor () {
        return Color.rgb((int) this.red.getFloatValue(CSSPrimitiveValue.CSS_NUMBER), (int) this.green.getFloatValue(CSSPrimitiveValue.CSS_NUMBER), (int) this.blue.getFloatValue(CSSPrimitiveValue.CSS_NUMBER));
    }
}
