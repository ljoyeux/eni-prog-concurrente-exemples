package fr.eni.concurrent.examples.gui;

/**
 * @author ljoyeux
 */
public final class RGB {
    public double r;
    public double g;
    public double b;

    public RGB() {
    }

    public RGB(double r, double g, double b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public RGB(double v[]) {
        this.r = v[0];
        this.g = v[1];
        this.b = v[2];
    }

    public RGB(int rgb) {
        this.b = rgb & 0xff;
        rgb >>= 8;
        this.g = rgb & 0xff;
        rgb >>= 8;
        this.r = rgb & 0xff;
    }

    public int toInt() {
        int v = (int) (r * 255);
        v <<= 8;
        v |= (int) (g * 255);
        v <<= 8;
        v |= (int) (b * 255);

        return v;
    }

}
