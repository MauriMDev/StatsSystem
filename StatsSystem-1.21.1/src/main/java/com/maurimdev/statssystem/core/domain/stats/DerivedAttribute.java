package com.maurimdev.statssystem.core.domain.stats;

/**
 * Enum que define todos los atributos derivados del sistema de estadísticas.
 * Cada atributo derivado se calcula a partir de una o más estadísticas principales.
 *
 * Estos atributos se muestran en el HUD y representan las consecuencias reales
 * de subir las 6 estadísticas principales.
 */
public enum DerivedAttribute {

    // ========== VITALITY ==========
    MAX_HEALTH("💚", "Vida Máxima", "HP", StatType.VITALITY),
    HEALTH_REGEN("🔋", "Regeneración de Vida", "HP/s", StatType.VITALITY),
    ARMOR("🛡", "Armadura Total", "🛡", StatType.VITALITY),

    // ========== STRENGTH ==========
    ATTACK_DAMAGE("⚔️", "Daño de Ataque", "DMG", StatType.STRENGTH),
    KNOCKBACK_RESISTANCE("💪", "Resistencia al Knockback", "%", StatType.STRENGTH),

    // ========== DEXTERITY ==========
    CRIT_CHANCE("🎲", "Probabilidad de Crítico", "%", StatType.DEXTERITY),
    CRIT_DAMAGE("💥", "Daño Crítico", "%", StatType.DEXTERITY),
    PROJECTILE_RANGE("🏹", "Alcance de Proyectiles", "%", StatType.DEXTERITY),

    // ========== MINING ==========
    MINING_SPEED("⛏️", "Velocidad de Minado", "%", StatType.MINING),

    // ========== AGILITY ==========
    FALL_RESISTANCE("🪂", "Resistencia a Caída", "%", StatType.AGILITY),
    HUNGER_REDUCTION("🍖", "Reducción de Hambre", "%", StatType.AGILITY),
    MAX_ENERGY("⚡", "Energía Máxima", "pts", StatType.AGILITY),

    // ========== FARMING ==========
    CROP_DROPS("🌾", "Drops Extra de Cultivos", "%", StatType.FARMING);

    private final String symbol;
    private final String displayName;
    private final String unit;
    private final StatType primaryStat;

    DerivedAttribute(String symbol, String displayName, String unit, StatType primaryStat) {
        this.symbol = symbol;
        this.displayName = displayName;
        this.unit = unit;
        this.primaryStat = primaryStat;
    }

    /**
     * @return Símbolo visual del atributo (emoji)
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * @return Nombre completo del atributo en español
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return Unidad de medida del atributo (HP, %, DMG, etc.)
     */
    public String getUnit() {
        return unit;
    }

    /**
     * @return La estadística principal que afecta este atributo
     */
    public StatType getPrimaryStat() {
        return primaryStat;
    }

    /**
     * @return Nombre formateado con símbolo para el HUD
     */
    public String getFormattedName() {
        return symbol + " " + displayName;
    }
}
