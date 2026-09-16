package com.tiokamp.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Central definition of the events — names, units, icons, input prompts,
 * descriptions and scoring direction. Change the events for the party here;
 * templates, services and the placement calculation all read from this.
 * An event with an empty description gets no ?-button.
 *
 * Directions:
 *   HIGHEST — highest raw value wins the event (points, hits)
 *   LOWEST  — lowest raw value wins (seconds, centimeters)
 *   CLOSEST — closest to a target wins (the target comes from the
 *             app.makaroni-facit property / APP_MAKARONI_FACIT env var)
 */
@Getter
@RequiredArgsConstructor
public enum Event {

    DART(1, "Dart", "p", "🎯", "Ange poäng", Direction.HIGHEST,
            "Varje deltagare kastar 6 pilar mot piltavlan (1–10 poäng, bullseye 10 p). "
          + "Summan av pilarna som sitter fast registreras. Höjd till bullseye 173 cm, "
          + "avstånd till tavlan 237 cm — stå som närmast vid plankans framkant."),

    BOULE(2, "Boule", "cm", "⚪", "Ange cm", Direction.LOWEST,
            "Varje deltagare kastar 4 klot mot pinnen. Avståndet mellan närmsta klot och "
          + "pinnen mäts och registreras i centimeter — kortast avstånd vinner. "
          + "Avstånd från plankans framkant till pinnen: 5 m."),

    MAKARONIGISSNING(3, "Makaronigissning", "st", "🍝", "Ange gissning", Direction.CLOSEST,
            "Gissa hur många makaroner det är i glasburken och registrera din gissning. "
          + "Närmast rätt antal vinner."),

    RINGAR(4, "Ringar", "p", "⭕", "Ange poäng", Direction.HIGHEST,
            "Fem ringar kastas mot korset med fem pinnar, värda 10–50 poäng "
          + "(10 p närmast kastaren). Avstånd från plankan till 10-poängspinnen: 150 cm. "
          + "Registrera din sammanlagda poäng."),

    CORN_HOLE(5, "Corn hole", "p", "🌽", "Ange poäng", Direction.HIGHEST,
            "Varje deltagare kastar 6 påsar mot den lutande plattan: 2 p för påse i hålet, "
          + "1 p för påse som ligger kvar på plattan utan att nudda gräset. "
          + "Avstånd från kastaren till plattans framkant: 5 m. Räkna ihop och registrera poängen."),

    PENNA_I_FLASKA(6, "Penna i flaska", "sek", "✏️", "Ange sekunder", Direction.LOWEST,
            "Med ett snöre fäst vid midjan och en penna i änden bakom ryggen: gå fram till "
          + "flaskan (du startar 1 m bort) och sänk ner pennan helt i flaskan utan att röra "
          + "snöre eller penna. Justera gärna snörets längd innan start. "
          + "Tiden i sekunder registreras — snabbast vinner."),

    PA_MINUTEN(7, "På minuten", "sek", "⏱️", "Ange fel (sek)", Direction.LOWEST,
            "Håll tidtagaruret bakom ryggen, starta med ett tryck och stoppa med nästa när "
          + "du tror att exakt 60 sekunder har gått. Registrera hur många sekunder fel du var "
          + "(positivt tal, avrundat till hel sekund) — minst fel vinner."),

    KROCKET(8, "Krocket", "p", "🏑", "Ange poäng", Direction.HIGHEST,
            "Fem bågar står i rad. Från avståndspinnen slår du iväg 4 klot: genom den mittersta "
          + "bågen ger 3 p, bågen direkt till höger eller vänster om mitten ger 2 p, och de "
          + "yttersta bågarna 1 p. Räkna ihop poängen och registrera."),

    STEGGOLF(9, "Steggolf", "p", "🪜", "Ange poäng", Direction.HIGHEST,
            "Kasta 6 par bollar mot stegen. Bollar som blir hängande på översta pinnen ger "
          + "3 p, mellersta 2 p och nedersta 1 p. Räkna ihop poängen och registrera."),

    // You enter the error in millimetres from 30 cm; smallest error wins (LOWEST).
    KLIPPA_SNORE(10, "Klippa snöre", "mm", "✂️", "Ange fel i mm", Direction.LOWEST,
            "Klipp med sax ett snöre som är exakt 30 cm långt. En kontrollant mäter snöret "
          + "och noterar felet i millimeter — minst fel vinner.");

    public enum Direction { HIGHEST, LOWEST, CLOSEST }

    private final int number;
    private final String displayName;
    private final String unit;
    private final String icon;
    private final String prompt;
    private final Direction direction;
    private final String description;

    public static Event byNumber(int number) {
        if (number < 1 || number > values().length) {
            throw new IllegalArgumentException("Ogiltigt grennummer: " + number);
        }
        return values()[number - 1];
    }

    public static Event byDisplayName(String displayName) {
        for (Event e : values()) {
            if (e.displayName.equals(displayName)) return e;
        }
        return null;
    }

    /**
     * A guessing event whose entered values must NOT be shown publicly on the
     * leaderboard (it would spoil the game). Still counts in the final scoring
     * and is visible to admins.
     */
    public boolean isHiddenOnLeaderboard() {
        return this == MAKARONIGISSNING;
    }

    public static int count() {
        return values().length;
    }

    /**
     * Maps a raw result to a value where HIGHER always means BETTER, so the
     * placement calculation can stay direction-agnostic. For CLOSEST events the
     * target must be set; returns null for missing raw values.
     */
    public Double rankingValue(Double raw, Double target) {
        if (raw == null) return null;
        return switch (direction) {
            case HIGHEST -> raw;
            case LOWEST -> -raw;
            case CLOSEST -> {
                if (target == null) {
                    throw new IllegalStateException(
                            "Grenen '" + displayName + "' rankas mot ett facit som inte är satt "
                          + "(app.makaroni-facit / miljövariabeln APP_MAKARONI_FACIT).");
                }
                yield -Math.abs(raw - target);
            }
        };
    }
}
