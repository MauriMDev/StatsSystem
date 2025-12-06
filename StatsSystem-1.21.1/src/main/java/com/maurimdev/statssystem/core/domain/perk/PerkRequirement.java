package com.maurimdev.statssystem.core.domain.perk;

import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.core.domain.stats.StatType;

import java.util.HashMap;
import java.util.Map;

/**
 * Representa los requisitos para desbloquear un perk.
 * Puede requerir niveles en stats específicas y tiene un costo en niveles de XP de Minecraft.
 */
public class PerkRequirement {
    private final Map<StatType, Integer> requiredStats;
    private final int xpLevelCost;
    private final PerkTier tier;

    /**
     * Constructor para perks normales (solo tier y costo)
     * @param tier Tier del perk
     * @param xpLevelCost Costo en niveles de XP de Minecraft
     */
    public PerkRequirement(PerkTier tier, int xpLevelCost) {
        this.tier = tier;
        this.xpLevelCost = xpLevelCost;
        this.requiredStats = new HashMap<>();
    }

    /**
     * Constructor para perks de combinación (requiere múltiples stats)
     * @param tier Tier del perk
     * @param xpLevelCost Costo en niveles de XP de Minecraft
     * @param requiredStats Map de stats requeridas con sus niveles mínimos
     */
    public PerkRequirement(PerkTier tier, int xpLevelCost, Map<StatType, Integer> requiredStats) {
        this.tier = tier;
        this.xpLevelCost = xpLevelCost;
        this.requiredStats = new HashMap<>(requiredStats);
    }

    /**
     * Verifica si el jugador cumple todos los requisitos para desbloquear el perk
     * @param playerStats Stats del jugador
     * @param playerXpLevel Niveles de XP del jugador
     * @return true si cumple todos los requisitos, false en caso contrario
     */
    public boolean meetsRequirements(PlayerStats playerStats, int playerXpLevel) {
        // Verificar que tiene suficientes niveles de XP
        if (playerXpLevel < xpLevelCost) {
            return false;
        }

        // Verificar que el tier está desbloqueado
        // Si es un perk de combinación, verificar cada stat requerida
        if (isComboRequirement()) {
            for (Map.Entry<StatType, Integer> entry : requiredStats.entrySet()) {
                StatType stat = entry.getKey();
                int requiredLevel = entry.getValue();
                int playerLevel = playerStats.getLevel(stat);

                if (playerLevel < requiredLevel) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Verifica si este requisito es para un perk de combinación
     * @return true si requiere más de 1 stat
     */
    public boolean isComboRequirement() {
        return requiredStats.size() > 1;
    }

    /**
     * Obtiene el tier del perk
     */
    public PerkTier getTier() {
        return tier;
    }

    /**
     * Obtiene el costo en niveles de XP
     */
    public int getXpLevelCost() {
        return xpLevelCost;
    }

    /**
     * Obtiene las stats requeridas y sus niveles mínimos
     */
    public Map<StatType, Integer> getRequiredStats() {
        return Map.copyOf(requiredStats);
    }

    /**
     * Obtiene una descripción legible de los requisitos
     * @param playerStats Stats del jugador (para mostrar si cumple o no)
     * @param playerXpLevel Nivel de XP del jugador
     * @return String con los requisitos formateados
     */
    public String getRequirementsDescription(PlayerStats playerStats, int playerXpLevel) {
        StringBuilder sb = new StringBuilder();

        // XP requirement
        sb.append("§7Costo: ");
        if (playerXpLevel >= xpLevelCost) {
            sb.append("§a");
        } else {
            sb.append("§c");
        }
        sb.append(xpLevelCost).append(" niveles XP");

        // Stats requirements (si es combo)
        if (isComboRequirement()) {
            sb.append("\n§7Requiere:");
            for (Map.Entry<StatType, Integer> entry : requiredStats.entrySet()) {
                StatType stat = entry.getKey();
                int required = entry.getValue();
                int current = playerStats.getLevel(stat);

                sb.append("\n  ");
                if (current >= required) {
                    sb.append("§a✓ ");
                } else {
                    sb.append("§c✗ ");
                }
                sb.append(stat.getDisplayName())
                  .append(" ")
                  .append(required)
                  .append(" §7(tienes ")
                  .append(current)
                  .append(")");
            }
        }

        return sb.toString();
    }

    /**
     * Builder para crear PerkRequirements de forma fluida
     */
    public static class Builder {
        private PerkTier tier;
        private int xpLevelCost;
        private final Map<StatType, Integer> requiredStats = new HashMap<>();

        public Builder tier(PerkTier tier) {
            this.tier = tier;
            return this;
        }

        public Builder cost(int xpLevelCost) {
            this.xpLevelCost = xpLevelCost;
            return this;
        }

        /**
         * Alias para cost() - establece el costo en niveles de XP
         */
        public Builder xpLevelCost(int xpLevelCost) {
            return cost(xpLevelCost);
        }

        public Builder requireStat(StatType stat, int level) {
            this.requiredStats.put(stat, level);
            return this;
        }

        /**
         * Alias para requireStat() - añade un requisito de stat
         */
        public Builder requiredStat(StatType stat, int level) {
            return requireStat(stat, level);
        }

        /**
         * Establece múltiples stats requeridas a la vez
         * @param stats Map de StatType -> nivel requerido
         */
        public Builder requiredStats(Map<StatType, Integer> stats) {
            this.requiredStats.putAll(stats);
            return this;
        }

        public PerkRequirement build() {
            if (tier == null) {
                throw new IllegalStateException("Tier must be set");
            }
            return new PerkRequirement(tier, xpLevelCost, requiredStats);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
