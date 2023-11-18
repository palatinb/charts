//package eu.hansolo.fx.charts.areaheatmap;
//
//import javafx.concurrent.Task;
//
//import java.time.Duration;
//import java.time.Instant;
//import java.util.concurrent.Callable;
//
//public class RedrawTask implements Callable<Void> {
//
//    private final int _currentIteration;
//    private final AreaHeatMap _heatMap;
//
//    public RedrawTask(int currentIteration, AreaHeatMap heatmap) {
//        this._currentIteration = currentIteration;
//        _heatMap = heatmap;
//    }
//    @Override
//    public Void call() throws Exception {
//        Instant start = Instant.now();
//        this._heatMap.draw(_currentIteration * 75, _heatMap.getNoOfCloserInfluentPoints(), _heatMap.getQuality());
//        Instant end = Instant.now();
//        long time = Duration.between(start, end).toMillis();
//        System.out.println();
//        System.out.println("method running time" + _currentIteration + ": " + time+" Milli seconds");
//        return null;
//    }
//}
