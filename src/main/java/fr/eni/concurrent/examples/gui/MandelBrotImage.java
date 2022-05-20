package fr.eni.concurrent.examples.gui;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;

/**
 * @author ljoyeux
 */
public class MandelBrotImage extends JPanel implements MouseListener {
    double centerX = 0;
    double centerY = 0;
    private double zoom = .005;
    public static boolean canDraw = false;
    private int nbIter;

    private final MandelbrotForkJoinPool computeMandelbrot;
    private final MandelbrotExecutor computeMandelbrotExecutor;
    private final int[] colormap;


    public MandelBrotImage() {
        addMouseListener(this);
        computeMandelbrot = new MandelbrotForkJoinPool(this);
        computeMandelbrotExecutor = new MandelbrotExecutor(this);
        colormap = initColorMap();
        nbIter = 20;
    }

    @Override
    public void paintComponent(Graphics g) {
        if (!canDraw) {
            return;
        }

        computeMandelbrotExecutor.render(centerX, centerY, zoom, nbIter);
    }

    public void setCenter(double x, double y) {
        centerX = x;
        centerY = y;
    }

    public void setCenter(int x, int y) {
        centerX += zoom * (x - getWidth() / 2);
        centerY += zoom * (y - getHeight() / 2);
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = 1.0 / zoom;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        setCenter(e.getX(), e.getY());
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public int iterToColor(double r) {
        return colormap[(int) (r * colormap.length)];
    }

    private int[] initColorMap() {
        int[] map = new int[1024];
        for (int i = 0; i < map.length; i++) {
            double r = i / (double) map.length;
            map[i] = new HSV(0.5 - r / 2, 1, r).toRGB().toInt();
        }

        return map;
    }

    public void setNbIter(int nbIter) {
        this.nbIter = nbIter;
    }

}
