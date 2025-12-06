package com.maurimdev.statssystem.core.domain.stats;

public enum StatType {
    VITALITY("❤", "Vitalidad", 40, 2.0),
    STRENGTH("💪", "Fuerza", 30, 0.5),
    DEXTERITY("🎯", "Destreza", 25, 0.02),
    MINING("⛏", "Minería", 45, 0.05),
    AGILITY("👟", "Agilidad", 25, 0.01),
    FARMING("🌾", "Agricultura", 50, 0.03);

    private final String symbol;
    private final String displayName;
    private final int softCap;
    private final double bonusPerLevel;

    StatType(String symbol, String displayName, int softCap, double bonusPerLevel) {
        this.symbol = symbol;
        this.displayName = displayName;
        this.softCap = softCap;
        this.bonusPerLevel = bonusPerLevel;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSoftCap() {
        return softCap;
    }

    public double getBonusPerLevel() {
        return bonusPerLevel;
    }

    public int getHardCap() {
        return 64; // Todas las stats tienen hard cap de 64
    }
}