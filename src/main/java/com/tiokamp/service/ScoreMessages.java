package com.tiokamp.service;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Playful one-liners shown on the "latest score" banner, chosen by the placement
 * that fresh score reached in its event (1st / 2nd / 3rd / outside the podium).
 * The pick is deterministic for a given seed, so the line stays put until a new
 * score arrives. Edit the lists below to change or add lines.
 */
@Component
public class ScoreMessages {

    private static final List<String> FIRST = List.of(
            "Rakt upp i topp",
            "Ingen har gjort bättre än!",
            "Ledningen i grenen är tagen!",
            "Bäst hittills",
            "Ny etta!!!"
    );

    private static final List<String> SECOND = List.of(
            "Uppflugen på en andraplats!",
            "Nästan bäst",
            "Tvåa i grenen",
            "Tvåa är första förlorare",
            "Etta bland de icke-vinnande",
            "En hårsmån från förstaplatsen."
    );

    private static final List<String> THIRD = List.of(
            "Petar sig in på pallen — trea!",
            "Precis med bland de bästa!",
            "Pallplats i grenen, för stunden!",
            "Stolpe in",
            "Trea!! det räknas!"
    );

    private static final List<String> OUTSIDE = List.of(
            "Med i leken!",
            "Otur i spel, tur i kärlek",
            "Stolpe ut",
            "Övning ger färdighet",
            "När skjuter ingen hare",
            "Inte pallen den här gången",
            "Nu är den gjord",
            "Kämpa på",
            "Ingen pallplats, en så kallad 'erik'",
            "Bra kämpat!"
    );

    /**
     * A line for the placement a fresh score reached (1/2/3, else "outside").
     * @param placement 1-based placement in the event, or null if it can't be ranked
     * @param seed      stable per score so the line doesn't flicker between refreshes
     */
    public String forPlacement(Integer placement, long seed) {
        List<String> pool = switch (placement == null ? 0 : placement) {
            case 1 -> FIRST;
            case 2 -> SECOND;
            case 3 -> THIRD;
            default -> OUTSIDE;
        };
        return pool.get((int) Math.floorMod(seed, pool.size()));
    }
}
