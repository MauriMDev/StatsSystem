package com.maurimdev.statssystem.core.domain.equipment;

import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.core.domain.stats.StatType;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the stat requirements needed to use a piece of equipment.
 * Can store multiple stat requirements that must all be met.
 */
public class EquipmentRequirement {
    private final Map<StatType, Integer> requiredStats;

    /**
     * Creates an empty equipment requirement (no requirements).
     */
    public EquipmentRequirement() {
        this.requiredStats = new HashMap<>();
    }

    /**
     * Creates an equipment requirement with the specified stat requirements.
     * @param requiredStats Map of StatType to minimum level required
     */
    public EquipmentRequirement(Map<StatType, Integer> requiredStats) {
        this.requiredStats = new HashMap<>(requiredStats);
    }

    /**
     * Checks if the player meets all requirements to use this equipment.
     * @param playerStats The player's current stats
     * @return true if all requirements are met, false otherwise
     */
    public boolean meetsRequirements(PlayerStats playerStats) {
        for (Map.Entry<StatType, Integer> entry : requiredStats.entrySet()) {
            StatType statType = entry.getKey();
            int requiredLevel = entry.getValue();
            int playerLevel = playerStats.getLevel(statType);

            if (playerLevel < requiredLevel) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the required stats map.
     * @return Unmodifiable map of stat requirements
     */
    public Map<StatType, Integer> getRequiredStats() {
        return Map.copyOf(requiredStats);
    }

    /**
     * Checks if this requirement has any stat requirements.
     * @return true if there are no requirements, false otherwise
     */
    public boolean isEmpty() {
        return requiredStats.isEmpty();
    }

    /**
     * Gets a formatted string describing which requirements are not met.
     * @param playerStats The player's current stats
     * @return Human-readable string of missing requirements, or empty string if all met
     */
    public String getMissingRequirementsMessage(PlayerStats playerStats) {
        StringBuilder message = new StringBuilder();
        boolean first = true;

        for (Map.Entry<StatType, Integer> entry : requiredStats.entrySet()) {
            StatType statType = entry.getKey();
            int requiredLevel = entry.getValue();
            int playerLevel = playerStats.getLevel(statType);

            if (playerLevel < requiredLevel) {
                if (!first) {
                    message.append(", ");
                }
                message.append("§e")
                       .append(statType.getDisplayName())
                       .append(" ")
                       .append(requiredLevel)
                       .append(" §7(tienes ")
                       .append(playerLevel)
                       .append(")");
                first = false;
            }
        }

        return message.toString();
    }

    /**
     * Builder class for creating EquipmentRequirements fluently.
     */
    public static class Builder {
        private final Map<StatType, Integer> requiredStats = new HashMap<>();

        /**
         * Adds a stat requirement.
         * @param stat The stat type
         * @param level The minimum level required
         * @return this builder for chaining
         */
        public Builder require(StatType stat, int level) {
            requiredStats.put(stat, level);
            return this;
        }

        /**
         * Builds the EquipmentRequirement.
         * @return A new EquipmentRequirement instance
         */
        public EquipmentRequirement build() {
            return new EquipmentRequirement(requiredStats);
        }
    }

    /**
     * Creates a new Builder for constructing EquipmentRequirements.
     * @return A new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates an empty requirement (no stat requirements).
     * @return An EquipmentRequirement with no requirements
     */
    public static EquipmentRequirement none() {
        return new EquipmentRequirement();
    }
}
