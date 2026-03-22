package com.dsar.model;

/**
 * GDPR/CCPA data subject request types.
 */
public enum RequestType {
    ACCESS("Access my data"),
    DELETE("Delete my data"),
    CORRECT("Correct my data");

    private final String label;

    RequestType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
