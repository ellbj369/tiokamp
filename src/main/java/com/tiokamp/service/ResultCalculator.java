package com.tiokamp.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * One-time placement scoring at the end of the game.
 *
 * Per event: rank all participants by raw value, highest first. Best gets 1 point,
 * next 2, ... worst gets N (N = number of participants). Ties share the average of
 * the positions they span (two tied for best: 1.5 each) so the total points handed
 * out per event is always the same regardless of ties. Participants without a value
 * in an event are tied at the very bottom of that event.
 *
 * Final total = sum of the 10 event points; lowest total wins. Equal totals share
 * the same final rank and the next rank is skipped (1, 2, 2, 4).
 */
public final class ResultCalculator {

    public record Standing(double[] eventPoints, double totalPoints, int rank) {}

    private ResultCalculator() {}

    /**
     * @param rawByPlayer one entry per participant, each an array with one slot per
     *                    event (values may contain nulls); the map size defines N,
     *                    the points given to the worst placement. Values must already
     *                    be oriented so that higher = better (see Event#rankingValue).
     */
    public static <K> Map<K, Standing> calculate(Map<K, Double[]> rawByPlayer) {
        int eventCount = rawByPlayer.isEmpty() ? 0
                : rawByPlayer.values().iterator().next().length;
        Map<K, double[]> pointsByPlayer = new LinkedHashMap<>();
        rawByPlayer.keySet().forEach(k -> pointsByPlayer.put(k, new double[eventCount]));

        for (int e = 0; e < eventCount; e++) {
            TreeMap<Double, List<K>> byValueDesc = new TreeMap<>(Comparator.reverseOrder());
            List<K> missing = new ArrayList<>();
            for (Map.Entry<K, Double[]> entry : rawByPlayer.entrySet()) {
                Double value = entry.getValue()[e];
                if (value == null) {
                    missing.add(entry.getKey());
                } else {
                    byValueDesc.computeIfAbsent(value, v -> new ArrayList<>()).add(entry.getKey());
                }
            }

            int position = 1;
            for (List<K> tied : byValueDesc.values()) {
                double sharedPoints = position + (tied.size() - 1) / 2.0;
                for (K player : tied) {
                    pointsByPlayer.get(player)[e] = sharedPoints;
                }
                position += tied.size();
            }
            if (!missing.isEmpty()) {
                double sharedPoints = position + (missing.size() - 1) / 2.0;
                for (K player : missing) {
                    pointsByPlayer.get(player)[e] = sharedPoints;
                }
            }
        }

        Map<K, Double> totals = new LinkedHashMap<>();
        pointsByPlayer.forEach((player, points) -> {
            double total = 0;
            for (double p : points) total += p;
            totals.put(player, total);
        });

        List<K> byTotalAsc = new ArrayList<>(totals.keySet());
        byTotalAsc.sort(Comparator.comparingDouble(totals::get));

        Map<K, Standing> result = new LinkedHashMap<>();
        int rank = 0;
        Double previousTotal = null;
        for (int i = 0; i < byTotalAsc.size(); i++) {
            K player = byTotalAsc.get(i);
            double total = totals.get(player);
            if (previousTotal == null || Double.compare(total, previousTotal) != 0) {
                rank = i + 1;
            }
            previousTotal = total;
            result.put(player, new Standing(pointsByPlayer.get(player), total, rank));
        }
        return result;
    }
}
