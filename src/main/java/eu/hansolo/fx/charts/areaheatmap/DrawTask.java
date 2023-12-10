package eu.hansolo.fx.charts.areaheatmap;

import eu.hansolo.fx.charts.data.DataPoint;
import eu.hansolo.fx.charts.tools.Helper;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

public class DrawTask implements Callable<Canvas> {

    private final int startRow;
    private final double width;
    private final double height;
    private final int ROW_CHUNK;
    private final List<DataPoint> polygon;
    private final int LIMIT;
    private final double RESOLUTION;
    private final List<DataPoint> points;
    private final double heatmapOpacity;
    private final boolean useColorMapping;
    private final boolean isDiscreteColors;

    public DrawTask(int startRow, double width, double height, int rowChunk, List<DataPoint> polygon, int limit, double resolution, List<DataPoint> points, double heatmapOpacity, boolean useColorMapping, boolean isDiscreteColors) {

        this.startRow = startRow;
        this.width = width;
        this.height = height;
        ROW_CHUNK = rowChunk;
        this.polygon = polygon;
        LIMIT = limit;
        RESOLUTION = resolution;
        this.points = points;
        this.heatmapOpacity = heatmapOpacity;
        this.useColorMapping = useColorMapping;
        this.isDiscreteColors = isDiscreteColors;
    }
    @Override
    public Canvas call() throws Exception {
        int limit        = LIMIT > points.size() ? points.size() : LIMIT + 1;
        double pixelSize = 2 * RESOLUTION;
        Canvas offScreenCanvas = new Canvas(400, 400);
        GraphicsContext offScreenGC = offScreenCanvas.getGraphicsContext2D();
        try {
            var start = Instant.now();

            System.out.println("starting calculation");
            offScreenGC.clearRect(0, 0, width, height);
            for (double y = startRow ; y < startRow + ROW_CHUNK ; y += RESOLUTION) {
                for (double x = 0 ; x < width ; x += RESOLUTION) {
                    if (Helper.isInPolygon(x, y, polygon)) {

                        double value = getValueAt(limit, x, y);
                        if (value != -255) {
                            Color color    = useColorMapping ? getColorForValue(value,false) : getColorForValue(value, isDiscreteColors);
                            RadialGradient gradient = new RadialGradient(0, 0, x, y, RESOLUTION,
                                    false, CycleMethod.NO_CYCLE,
                                    new Stop(0, Color.color(color.getRed(), color.getGreen(), color.getBlue(), heatmapOpacity)),
                                    new Stop(1, Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.0)));
                            offScreenGC.setFill(gradient);
                            offScreenGC.fillOval(x - RESOLUTION, y - RESOLUTION, pixelSize, pixelSize);
                        }
                    }
                }
            }
            System.out.println("calculation ended");
            var end = Instant.now();
            System.out.println("Start row: " + startRow + " Finished row: " + (startRow + ROW_CHUNK) +" iteration: " + startRow / 75 + " elapsed time: " + Duration.between(start, end).toMillis());
            return offScreenCanvas;
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
            return offScreenCanvas;
        }
    }

        private double getValueAt(final int LIMIT, final double X , final double Y) {
        List<Number[]> arr = new ArrayList<>();
        double         t   = 0.0;
        double         b   = 0.0;
        if(Helper.isInPolygon(X, Y, polygon)) {
            for (int counter = 0 ; counter < points.size() ; counter++) {
                DataPoint point = points.get(counter);
                double distance = Helper.squareDistance(X, Y, point.getX(), point.getY());
                if (Double.compare(distance, 0) == 0) { return point.getValue(); }
                arr.add(counter, new Number[] { distance, counter });
            }
            arr.sort(Comparator.comparingInt(n -> n[0].intValue()));
            for (int counter = 0 ; counter < LIMIT ; counter++) {
                Number[] ptr = arr.get(counter);
                double inv = 1 / Math.pow(ptr[0].intValue(), 2);
                t = t + inv * points.get(ptr[1].intValue()).getValue();
                b = b + inv;
            }
            return t / b;
        } else {
            return -255;
        }
    }

    private Color getColorForValue(final double VALUE, final boolean LEVELS) {
        double limit  = 0.55;
        double min    = -30;
        double max    = 50;
        double delta  = max - min;
        double levels = 25;
        double value  = Helper.clamp(min, max, VALUE);
        double tmp    = 1 - (1 - limit) - (((value - min) * limit) / delta);
        if (LEVELS) {
            tmp = Math.round(tmp * levels) / levels;
        }
        return Helper.hslToRGB(tmp, 1, 0.5);
    }
}
