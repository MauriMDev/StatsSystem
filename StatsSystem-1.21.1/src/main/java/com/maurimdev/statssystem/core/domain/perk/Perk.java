package com.maurimdev.statssystem.core.domain.perk;

import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.core.domain.stats.StatType;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un perk individual que el jugador puede desbloquear.
 * Los perks otorgan habilidades pasivas o activas que mejoran el gameplay.
 */
public class Perk {
    private final String id;
    private final String name;
    private final List<String> description;
    private final StatType associatedStat;
    private final PerkRequirement requirement;
    private final PerkEffect effectType;
    private final int maxLevel;
    private final boolean isCombo;

    /**
     * Constructor privado - usar Builder para crear instancias
     */
    private Perk(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = new ArrayList<>(builder.description);
        this.associatedStat = builder.associatedStat;
        this.requirement = builder.requirement;
        this.effectType = builder.effectType;
        this.maxLevel = builder.maxLevel;
        this.isCombo = builder.isCombo;
    }

    // ============================================
    // GETTERS
    // ============================================

    /**
     * ID único del perk (usado para persistencia y registro)
     */
    public String getId() {
        return id;
    }

    /**
     * Nombre del perk para mostrar al jugador
     */
    public String getName() {
        return name;
    }

    /**
     * Descripción del perk (puede ser multi-línea)
     */
    public List<String> getDescription() {
        return List.copyOf(description);
    }

    /**
     * Stat principal asociada al perk (puede ser null para perks de combinación)
     */
    public StatType getAssociatedStat() {
        return associatedStat;
    }

    /**
     * Requisitos para desbloquear el perk
     */
    public PerkRequirement getRequirement() {
        return requirement;
    }

    /**
     * Tipo de efecto del perk
     */
    public PerkEffect getEffectType() {
        return effectType;
    }

    /**
     * Nivel máximo del perk (1 = no upgradeable)
     */
    public int getMaxLevel() {
        return maxLevel;
    }

    /**
     * Si el perk requiere múltiples stats (perk de combinación)
     */
    public boolean isCombo() {
        return isCombo;
    }

    /**
     * Obtiene el tier del perk
     */
    public PerkTier getTier() {
        return requirement.getTier();
    }

    // ============================================
    // MÉTODOS DE UTILIDAD
    // ============================================

    /**
     * Verifica si el jugador puede desbloquear este perk
     */
    public boolean canUnlock(PlayerStats playerStats, int playerXpLevel) {
        // Primero verificar requisitos base (XP y stats combinadas)
        if (!requirement.meetsRequirements(playerStats, playerXpLevel)) {
            return false;
        }

        // Para perks normales (no combo), verificar nivel en la stat asociada
        if (associatedStat != null && !isCombo) {
            int requiredLevel = getTier().getUnlockLevel();
            int playerLevel = playerStats.getLevel(associatedStat);
            if (playerLevel < requiredLevel) {
                return false;
            }
        }

        return true;
    }

    /**
     * Verifica si el perk es upgradeable (tiene más de 1 nivel)
     */
    public boolean isUpgradeable() {
        return maxLevel > 1;
    }

    /**
     * Obtiene la descripción formateada para tooltips
     */
    public List<String> getFormattedDescription() {
        List<String> formatted = new ArrayList<>();

        // Nombre con color
        formatted.add("§6§l" + name);

        // Tipo de efecto
        formatted.add("§7Tipo: §e" + effectType.getDisplayName());

        // Descripción
        formatted.add("§r");
        description.forEach(line -> formatted.add("§7" + line));

        // Levels info si es upgradeable
        if (isUpgradeable()) {
            formatted.add("§r");
            formatted.add("§7Niveles máximos: §e" + maxLevel);
        }

        // Combo indicator
        if (isCombo) {
            formatted.add("§r");
            formatted.add("§d§l[COMBO PERK]");
        }

        return formatted;
    }

    @Override
    public String toString() {
        return "Perk{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", tier=" + getTier() +
                ", isCombo=" + isCombo +
                '}';
    }

    // ============================================
    // BUILDER PATTERN
    // ============================================

    /**
     * Builder para construir Perks de forma fluida
     */
    public static class Builder {
        private String id;
        private String name;
        private final List<String> description = new ArrayList<>();
        private StatType associatedStat;
        private PerkRequirement requirement;
        private PerkEffect effectType;
        private int maxLevel = 1;
        private boolean isCombo = false;

        /**
         * ID único del perk (requerido)
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * Nombre del perk (requerido)
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Añade una línea de descripción
         */
        public Builder description(String line) {
            this.description.add(line);
            return this;
        }

        /**
         * Stat asociada (puede ser null para perks combo)
         */
        public Builder associatedStat(StatType stat) {
            this.associatedStat = stat;
            return this;
        }

        /**
         * Requisitos del perk (requerido)
         */
        public Builder requirement(PerkRequirement requirement) {
            this.requirement = requirement;
            return this;
        }

        /**
         * Tipo de efecto (requerido)
         */
        public Builder effectType(PerkEffect effectType) {
            this.effectType = effectType;
            return this;
        }

        /**
         * Nivel máximo (default 1)
         */
        public Builder maxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
            return this;
        }

        /**
         * Marca como perk de combinación
         */
        public Builder combo(boolean isCombo) {
            this.isCombo = isCombo;
            return this;
        }

        /**
         * Construye el Perk
         */
        public Perk build() {
            // Validaciones
            if (id == null || id.isEmpty()) {
                throw new IllegalStateException("Perk ID is required");
            }
            if (name == null || name.isEmpty()) {
                throw new IllegalStateException("Perk name is required");
            }
            if (requirement == null) {
                throw new IllegalStateException("Perk requirement is required");
            }
            if (effectType == null) {
                throw new IllegalStateException("Perk effect type is required");
            }
            if (description.isEmpty()) {
                throw new IllegalStateException("Perk must have at least one description line");
            }
            if (maxLevel < 1) {
                throw new IllegalStateException("Max level must be at least 1");
            }

            return new Perk(this);
        }
    }

    /**
     * Crea un nuevo Builder para construir un Perk
     */
    public static Builder builder() {
        return new Builder();
    }
}
