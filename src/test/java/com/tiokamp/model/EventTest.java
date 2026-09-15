package com.tiokamp.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    @Test
    void highestDirectionKeepsRawValue() {
        assertEquals(42.0, Event.DART.rankingValue(42.0, null));
    }

    @Test
    void lowestDirectionInvertsOrder() {
        // Boule: 12 cm beats 80 cm -> its ranking value must be HIGHER
        Double close = Event.BOULE.rankingValue(12.0, null);
        Double far   = Event.BOULE.rankingValue(80.0, null);
        assertTrue(close > far);
    }

    @Test
    void closestDirectionRanksByDistanceToFacit() {
        double facit = 500.0;
        // guess 510 (off by 10) beats guess 450 (off by 50), over- and undershooting alike
        Double near = Event.MAKARONIGISSNING.rankingValue(510.0, facit);
        Double far  = Event.MAKARONIGISSNING.rankingValue(450.0, facit);
        Double exact = Event.MAKARONIGISSNING.rankingValue(500.0, facit);
        assertTrue(near > far);
        assertTrue(exact > near);
    }

    @Test
    void closestDirectionWithoutFacitThrows() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> Event.MAKARONIGISSNING.rankingValue(500.0, null));
        assertTrue(e.getMessage().contains("facit"));
    }

    @Test
    void missingRawValueStaysNull() {
        assertNull(Event.BOULE.rankingValue(null, null));
        assertNull(Event.MAKARONIGISSNING.rankingValue(null, null)); // no facit check for absent results
    }

    @Test
    void klippaSnoreScoresBySmallestDifference() {
        // entered value = cm away from 30; smaller difference must rank better
        Double closer  = Event.KLIPPA_SNORE.rankingValue(1.0, null); // 1 cm off
        Double farther = Event.KLIPPA_SNORE.rankingValue(5.0, null); // 5 cm off
        assertTrue(closer > farther);
        assertEquals(10, Event.KLIPPA_SNORE.getNumber());
    }

    @Test
    void eventNumbersAreSequentialFromOne() {
        Event[] events = Event.values();
        for (int i = 0; i < events.length; i++) {
            assertEquals(i + 1, events[i].getNumber());
            assertSame(events[i], Event.byNumber(i + 1));
        }
    }
}
