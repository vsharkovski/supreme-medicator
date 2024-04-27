package com.suprememedicator.suprememedicator.domain;

/*
 * Types to ignore (maybe):
 * each
 * g
 * ml
 */

/*
Credits: https://www.baeldung.com/java-enum-values
 */

import java.util.HashMap;
import java.util.Map;

public enum EDosageType {
    BOTTLE("bottle"),
    CAPLET("caplet"),
    CAPSULE("capsule"),
    SUPPOSITORY("suppository"),
    TABLET("tablet"),
    POWDER("powder");

    public final String label;

    private static final Map<String, EDosageType> BY_LABEL = new HashMap<>();

    static {
        for (EDosageType e: values()) {
            BY_LABEL.put(e.label, e);
        }
    }

    EDosageType(String label) {
        this.label = label;
    }

    public static EDosageType valueOfLabel(String label) {
        return BY_LABEL.get(label);
    }
}
