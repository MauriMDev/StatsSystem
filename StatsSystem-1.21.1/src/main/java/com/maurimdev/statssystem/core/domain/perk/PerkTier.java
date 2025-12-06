package com.maurimdev.statssystem.core.domain.perk;

/**
 * Representa los diferentes tiers de perks disponibles.
 * Cada tier se desbloquea al alcanzar cierto nivel en una stat.
 */
public enum PerkTier {
    /**
     * Tier 1 - Se desbloquea al nivel 24 de la stat
     * Perks básicos e introductorios
     */
    TIER_1(24, "Tier I"),

    /**
     * Tier 2 - Se desbloquea al nivel 40 de la stat
     * Perks intermedios con efectos más potentes
     */
    TIER_2(40, "Tier II"),

    /**
     * Tier 3 - Se desbloquea al nivel 56 de la stat
     * Perks avanzados
     */
    TIER_3(56, "Tier III"),

    /**
     * Tier Combo - Para perks que requieren múltiples stats
     * No tiene nivel de desbloqueo fijo, depende de los requisitos específicos
     */
    COMBO(0, "Combo");

    private final int unlockLevel;
    private final String displayName;

    PerkTier(int unlockLevel, String displayName) {
        this.unlockLevel = unlockLevel;
        this.displayName = displayName;
    }

    /**
     * Obtiene el nivel mínimo requerido para desbloquear este tier
     */
    public int getUnlockLevel() {
        return unlockLevel;
    }

    /**
     * Obtiene el nombre para mostrar del tier
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Verifica si el tier está desbloqueado al nivel dado de stat
     * @param statLevel Nivel actual de la stat del jugador
     * @return true si el tier está desbloqueado, false en caso contrario
     */
    public boolean isUnlockedAt(int statLevel) {
        return statLevel >= unlockLevel;
    }

    /**
     * Obtiene el tier correspondiente para un nivel de stat dado
     * @param statLevel Nivel de la stat
     * @return El tier más alto que puede acceder el jugador, o null si ninguno
     */
    public static PerkTier getTierForLevel(int statLevel) {
        PerkTier highest = null;
        for (PerkTier tier : values()) {
            if (tier.isUnlockedAt(statLevel)) {
                highest = tier;
            }
        }
        return highest;
    }

    /**
     * Obtiene todos los tiers desbloqueados para un nivel de stat dado
     * @param statLevel Nivel de la stat
     * @return Array de tiers desbloqueados
     */
    public static PerkTier[] getUnlockedTiers(int statLevel) {
        return java.util.Arrays.stream(values())
                .filter(tier -> tier.isUnlockedAt(statLevel))
                .toArray(PerkTier[]::new);
    }
}
