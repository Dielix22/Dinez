package com.adres.urgencias.shared.domain;

/**
 * Triage classification levels 1 to 5 (REQ-04, REQ-10, BR-TRG-03).
 * Only levels 2 to 5 have a queue, wait limit, and daily counter.
 */
public enum TriageLevel {
    LEVEL_1(1),
    LEVEL_2(2),
    LEVEL_3(3),
    LEVEL_4(4),
    LEVEL_5(5);

    private final int level;

    TriageLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean hasQueue() {
        return level >= 2 && level <= 5;
    }

    public static TriageLevel fromValue(int value) {
        for (TriageLevel triageLevel : values()) {
            if (triageLevel.level == value) {
                return triageLevel;
            }
        }
        throw new InvalidDataException("Triage level must be between 1 and 5: " + value);
    }
}