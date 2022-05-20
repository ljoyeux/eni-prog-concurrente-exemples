package fr.eni.concurrent.examples.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by ljoyeux on 13/07/2017.
 */
public class MandelbrotThreadPool {
    private final MandelBrotImage mandelbrotImage;

    private ExecutorService executor;
    public MandelbrotThreadPool(MandelBrotImage mandelbrotImage) {
        this.mandelbrotImage = mandelbrotImage;
    }

    public void render(double centerX, double centerY, double zoom, final int numIter) {
        final int height = mandelbrotImage.getHeight();
        final int width = mandelbrotImage.getWidth();

        Rectangle clipBounds = new Rectangle(0, 0, width, height);
        double left = (clipBounds.x - width / 2) * zoom + centerX;
        double right = (clipBounds.x + clipBounds.width - width / 2) * zoom + centerX;

        double bottom = (clipBounds.height + clipBounds.y - height / 2) * zoom + centerY;
        double top = (clipBounds.y - height / 2) * zoom + centerY;

        double deltaX = (right - left) / clipBounds.width;
        double deltaY = (bottom - top) / clipBounds.height;

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        List<Mandelbrot.MandelbrotLine<Integer>> lines = new ArrayList<>();

        Integer[] indexes = new Integer[height];
        for (int i = 0; i < height; i++) {
            indexes[i] = i;
            lines.add(new Mandelbrot.MandelbrotLine(left, top + deltaY * i, deltaX, 0, i));
        }

        List<Integer> indexesList = new ArrayList<>(Arrays.asList(indexes));
        Random r = new Random(System.nanoTime());
        List<Mandelbrot.MandelbrotLine<Integer>> randLines = new ArrayList<>();
        while(indexesList.size()>0) {
            randLines.add(lines.get((int)indexesList.remove(r.nextInt(indexesList.size()))));
        }
        lines = randLines;

        if(executor!=null) {
            executor.shutdownNow();
        }

        executor = new ThreadPoolExecutor(availableProcessors, availableProcessors, 1_000_000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(height));

        for(final Mandelbrot.MandelbrotLine<Integer> line: lines) {

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    final int[] pixels = Mandelbrot.mandelbrot(line.getX(), line.getY(), line.getDeltaX(), line.getDeltaY(), numIter, 2, new int[width]);
                    double invNumIter = 1.0/numIter;
                    for (int i = 0; i < pixels.length; i++) {
                        int v = pixels[i];

                        pixels[i] = 0xff_00_00_00 | mandelbrotImage.iterToColor(v*invNumIter);
                    }

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            final BufferedImage img = new BufferedImage(width, 1, BufferedImage.TYPE_INT_ARGB);
                            img.setRGB(0, 0, width, 1, pixels, 0, width);
                            mandelbrotImage.getGraphics().drawImage(img, 0, line.getRef(), mandelbrotImage);
                        }
                    });
                }
            });
        }
    }
}

