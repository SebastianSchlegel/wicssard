package de.sebastianschlegel.wicssard;

import javafx.scene.paint.Color;

import java.util.Collection;

public class ColorMixer {

    public static void followTheLeader (final ColorModel leader, final Collection<ColorModel> followers) {
        final Color original = leader.getOriginal();
        final Color current = leader.getRgbColor().getColor();

        final double hueChange = current.getHue() - original.getHue();
        final double brightnessChange = current.getBrightness() - original.getBrightness();
        final double saturationChange = current.getSaturation() - original.getSaturation();

        for (final ColorModel follower : followers) {
            if (follower.equals(leader)) {
                continue;
            }
            final Color followerColor = follower.getOriginal();
            final double hue = followerColor.getHue() + hueChange % 360.0;
            final double brightness = limitZeroToOne(followerColor.getBrightness() + brightnessChange);
            final double saturation = limitZeroToOne(followerColor.getSaturation() + saturationChange);
            final Color adapted = Color.hsb(hue, saturation, brightness);
            follower.getRgbColor().applyColor(adapted);

        }

    }

    private static double limitZeroToOne (final double d) {
        if (d < 0) {
            return 0;
        }
        if (d > 1) {
            return 1;
        }
        return d;
    }

}
