package com.maurimdev.statssystem.stats;

public enum ProgressionMode {
    XP("Manual (XP)", "Gasta experiencia para subir estadísticas de tu elección"),
    PRACTICE("Automático (Práctica)", "Las estadísticas suben automáticamente al usarlas");

    private final String displayName;
    private final String description;

    ProgressionMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isManual() {
        return this == XP;
    }

    public boolean isPractice() {
        return this == PRACTICE;
    }
}