package com.tiokamp.service;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResultCalculatorTest {

    // Tests use 10 event slots; the calculator itself takes any length.
    private static Double[] row(Double... values) {
        Double[] full = new Double[10];
        System.arraycopy(values, 0, full, 0, values.length);
        return full;
    }

    @Test
    void bestRawValueGetsOnePoint_worstGetsN() {
        Map<String, Double[]> raw = new LinkedHashMap<>();
        raw.put("anna", row(30.0));  // best in event 1
        raw.put("bert", row(20.0));
        raw.put("cia",  row(10.0));  // worst

        var standings = ResultCalculator.calculate(raw);

        assertEquals(1.0, standings.get("anna").eventPoints()[0]);
        assertEquals(2.0, standings.get("bert").eventPoints()[0]);
        assertEquals(3.0, standings.get("cia").eventPoints()[0]);
    }

    @Test
    void tiesShareTheAverageOfTheirPositions() {
        Map<String, Double[]> raw = new LinkedHashMap<>();
        raw.put("anna", row(30.0)); // tied best -> positions 1,2 -> 1.5 each
        raw.put("bert", row(30.0));
        raw.put("cia",  row(20.0)); // position 3
        raw.put("dave", row(10.0)); // position 4

        var standings = ResultCalculator.calculate(raw);

        assertEquals(1.5, standings.get("anna").eventPoints()[0]);
        assertEquals(1.5, standings.get("bert").eventPoints()[0]);
        assertEquals(3.0, standings.get("cia").eventPoints()[0]);
        assertEquals(4.0, standings.get("dave").eventPoints()[0]);
    }

    @Test
    void missingValuesAreTiedAtTheBottom() {
        Map<String, Double[]> raw = new LinkedHashMap<>();
        raw.put("anna", row(30.0));         // position 1
        raw.put("bert", row(20.0));         // position 2
        raw.put("cia",  row((Double) null)); // missing -> positions 3,4 -> 3.5 each
        raw.put("dave", row((Double) null));

        var standings = ResultCalculator.calculate(raw);

        assertEquals(1.0, standings.get("anna").eventPoints()[0]);
        assertEquals(2.0, standings.get("bert").eventPoints()[0]);
        assertEquals(3.5, standings.get("cia").eventPoints()[0]);
        assertEquals(3.5, standings.get("dave").eventPoints()[0]);
    }

    @Test
    void totalIsSumOfAllTenEvents_lowestTotalWins() {
        Map<String, Double[]> raw = new LinkedHashMap<>();
        // anna best in both filled events, bert worst; events 3-10 missing for both (tied)
        raw.put("anna", row(30.0, 12.0));
        raw.put("bert", row(10.0, 5.0));

        var standings = ResultCalculator.calculate(raw);

        // events 3-10: both missing -> tied at positions 1,2 -> 1.5 each, 8 events
        assertEquals(1 + 1 + 8 * 1.5, standings.get("anna").totalPoints());
        assertEquals(2 + 2 + 8 * 1.5, standings.get("bert").totalPoints());
        assertEquals(1, standings.get("anna").rank());
        assertEquals(2, standings.get("bert").rank());
    }

    @Test
    void equalTotalsShareRankAndNextRankIsSkipped() {
        Map<String, Double[]> raw = new LinkedHashMap<>();
        raw.put("anna", row(30.0)); // 1 point
        raw.put("bert", row(20.0)); // tied 2.5
        raw.put("cia",  row(20.0)); // tied 2.5
        raw.put("dave", row(5.0));  // 4 points

        var standings = ResultCalculator.calculate(raw);

        assertEquals(1, standings.get("anna").rank());
        assertEquals(2, standings.get("bert").rank());
        assertEquals(2, standings.get("cia").rank());
        assertEquals(4, standings.get("dave").rank());
    }

    @Test
    void pointsHandedOutPerEventAreIndependentOfTies() {
        // With N players the sum of points in an event is always N*(N+1)/2
        Map<String, Double[]> withTies = new LinkedHashMap<>();
        withTies.put("a", row(10.0));
        withTies.put("b", row(10.0));
        withTies.put("c", row(10.0));
        withTies.put("d", row(2.0));
        withTies.put("e", row((Double) null));

        var standings = ResultCalculator.calculate(withTies);
        double sum = standings.values().stream().mapToDouble(s -> s.eventPoints()[0]).sum();
        assertEquals(15.0, sum); // 5*6/2
    }

    @Test
    void playerWithNoScoresAtAllStillGetsAStanding() {
        Map<String, Double[]> raw = new LinkedHashMap<>();
        raw.put("anna", row(30.0));
        raw.put("ghost", row()); // never entered anything

        var standings = ResultCalculator.calculate(raw);

        assertNotNull(standings.get("ghost"));
        // event 1: anna 1p, ghost 2p; events 2-10: both missing -> tied 1.5 each
        assertEquals(1 + 9 * 1.5, standings.get("anna").totalPoints());
        assertEquals(2 + 9 * 1.5, standings.get("ghost").totalPoints());
        assertEquals(1, standings.get("anna").rank());
        assertEquals(2, standings.get("ghost").rank());
    }
}
