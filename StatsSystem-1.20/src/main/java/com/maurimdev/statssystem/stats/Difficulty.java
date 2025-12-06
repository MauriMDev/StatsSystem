package com.maurimdev.statssystem.stats;

public enum Difficulty {
    EASY("Fácil", 1.12),
    NORMAL("Normal", 1.15),
    HARD("Difícil", 1.18),
    EXTREME("Extremo", 1.22);

    private final String displayName;
    private final double multiplier;

    Difficulty(String displayName, double multiplier) {
        this.displayName = displayName;
        this.multiplier = multiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getMultiplier() {
        return multiplier;
    }

    /**
     * Calcula el costo de XP para subir un nivel
     * @param currentLevel Nivel actual de la stat
     * @return Cantidad de XP necesaria
     */
    public int calculateXPCost(int currentLevel) {
        return (int) (10 * Math.pow(multiplier, currentLevel));
    }
}