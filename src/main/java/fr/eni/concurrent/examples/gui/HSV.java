package fr.eni.concurrent.examples.gui;

/**
 * @author ljoyeux
 */
public final class HSV {
    public double h;
    public double s;
    public double v;

    public HSV() {
    }

    public HSV(final double h, final double s, final double v) {
        this.h = h;
        this.s = s;
        this.v = v;
    }

    public HSV(double v[]) {
        this.h = v[0];
        this.s = v[1];
        this.v = v[2];
    }

    public RGB toRGB(RGB rgb) {
        if (rgb == null)
            rgb = new RGB();

        double r, g, b;
        if (s == 0)                       //HSV from 0 to 1
        {
            r = v;
            g = v;
            b = v;
        } else {
            double var_h = h * 6;
            if (var_h == 6)
                var_h = 0;      //H must be < 1
            double var_i = Math.floor(var_h);             //Or ... var_i = floor( var_h )
            double var_1 = v * (1 - s);
            double var_2 = v * (1 - s * (var_h - var_i));
            double var_3 = v * (1 - s * (1 - (var_h - var_i)));

            double var_r, var_g, var_b;

            if (var_i == 0) {
                var_r = v;
                var_g = var_3;
                var_b = var_1;
            } else if (var_i == 1) {
                var_r = var_2;
                var_g = v;
                var_b = var_1;
            } else if (var_i == 2) {
                var_r = var_1;
                var_g = v;
                var_b = var_3;
            } else if (var_i == 3) {
                var_r = var_1;
                var_g = var_2;
                var_b = v;
            } else if (var_i == 4) {
                var_r = var_3;
                var_g = var_1;
                var_b = v;
            } else {
                var_r = v;
                var_g = var_1;
                var_b = var_2;
            }

            r = var_r;                  //RGB results from 0 to 255
            g = var_g;
            b = var_b;
        }

        rgb.r = r;
        rgb.g = g;
        rgb.b = b;

        return rgb;
    }

    public RGB toRGB() {
        return toRGB(null);
    }

    @Override
    public String toString() {
        return "HSV(" + (int) (360 * h) + ", " + s + ", " + v + ")";
    }
}
