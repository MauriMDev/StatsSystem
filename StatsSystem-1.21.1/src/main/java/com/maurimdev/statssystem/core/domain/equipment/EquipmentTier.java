package com.maurimdev.statssystem.core.domain.equipment;

import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.core.domain.stats.StatType;

/**
 * Represents the different material tiers in Minecraft.
 * Each tier has different stat requirements depending on the equipment type.
 */
public enum EquipmentTier {
    LEATHER("Leather", 0),
    WOOD("Wood", 0),
    STONE("Stone", 1),
    CHAINMAIL("Chainmail", 2),
    IRON("Iron", 2),
    GOLD("Gold", 3),
    DIAMOND("Diamond", 4),
    NETHERITE("Netherite", 5);

    private final String displayName;
    private final int tierLevel;

    EquipmentTier(String displayName, int tierLevel) {
        this.displayName = displayName;
        this.tierLevel = tierLevel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getTierLevel() {
        return tierLevel;
    }

    /**
     * Gets the armor requirements for this tier.
     * @return EquipmentRequirement for armor pieces of this tier
     */
    public EquipmentRequirement getArmorRequirement() {
        return switch (this) {
            case LEATHER, CHAINMAIL -> EquipmentRequirement.none();
            case IRON -> EquipmentRequirement.builder()
                    .require(StatType.VITALITY, 8)
                    .build();
            case GOLD -> EquipmentRequirement.builder()
                    .require(StatType.VITALITY, 10)
                    .require(StatType.STRENGTH, 10)
                    .build();
            case DIAMOND -> EquipmentRequirement.builder()
                    .require(StatType.VITALITY, 20)
                    .require(StatType.STRENGTH, 15)
                    .build();
            case NETHERITE -> EquipmentRequirement.builder()
                    .require(StatType.VITALITY, 32)
                    .require(StatType.STRENGTH, 25)
                    .build();
            default -> EquipmentRequirement.none();
        };
    }

    /**
     * Gets the mining tool requirements (pickaxe, shovel) for this tier.
     * @return EquipmentRequirement for mining tools of this tier
     */
    public EquipmentRequirement getMiningToolRequirement() {
        return switch (this) {
            case WOOD -> EquipmentRequirement.none();
            case STONE -> EquipmentRequirement.builder()
                    .require(StatType.MINING, 5)
                    .build();
            case IRON -> EquipmentRequirement.builder()
                    .require(StatType.MINING, 12)
                    .build();
            case GOLD -> EquipmentRequirement.builder()
                    .require(StatType.MINING, 15)
                    .require(StatType.DEXTERITY, 10)
                    .build();
            case DIAMOND -> EquipmentRequirement.builder()
                    .require(StatType.MINING, 25)
                    .require(StatType.DEXTERITY, 15)
                    .build();
            case NETHERITE -> EquipmentRequirement.builder()
                    .require(StatType.MINING, 40)
                    .require(StatType.DEXTERITY, 25)
                    .build();
            default -> EquipmentRequirement.none();
        };
    }

    /**
     * Gets the combat tool requirements (sword, axe) for this tier.
     * @return EquipmentRequirement for combat tools of this tier
     */
    public EquipmentRequirement getCombatToolRequirement() {
        return switch (this) {
            case WOOD -> EquipmentRequirement.none();
            case STONE -> EquipmentRequirement.builder()
                    .require(StatType.STRENGTH, 5)
                    .build();
            case IRON -> EquipmentRequirement.builder()
                    .require(StatType.STRENGTH, 12)
                    .require(StatType.DEXTERITY, 8)
                    .build();
            case GOLD -> EquipmentRequirement.builder()
                    .require(StatType.STRENGTH, 15)
                    .require(StatType.DEXTERITY, 12)
                    .build();
            case DIAMOND -> EquipmentRequirement.builder()
                    .require(StatType.STRENGTH, 25)
                    .require(StatType.DEXTERITY, 18)
                    .build();
            case NETHERITE -> EquipmentRequirement.builder()
                    .require(StatType.STRENGTH, 40)
                    .require(StatType.DEXTERITY, 30)
                    .build();
            default -> EquipmentRequirement.none();
        };
    }

    /**
     * Gets the axe requirements (combat tool + mining requirement).
     * Las hachas requieren MINING porque se usan para talar árboles (misión secundaria)
     * @return EquipmentRequirement for axes of this tier
     */
    public EquipmentRequirement getAxeRequirement() {
        return switch (this) {
            case WOOD -> EquipmentRequirement.none();
            case STONE -> EquipmentRequirement.builder()
                    .require(StatType.STRENGTH, 5)
                    .require(StatType.MINING, 3)
                    .build();
            case IRON -> EquipmentRequirement.builder()
                    .require(StatType.STRENGTH, 12)
                    .require(StatType.DEXTERITY, 8)
                    .require(StatType.MINING, 8)
                    .build();
            case GOLD -> EquipmentRequirement.builder()
                    .require(StatType.STRENGTH, 15)
                    .require(StatType.DEXTERITY, 12)
                    .require(StatType.MINING, 10)
                    .build();
            case DIAMOND -> EquipmentRequirement.builder()
                    .require(StatType.STRENGTH, 25)
                    .require(StatType.DEXTERITY, 18)
                    .require(StatType.MINING, 18)
                    .build();
            case NETHERITE -> EquipmentRequirement.builder()
                    .require(StatType.STRENGTH, 40)
                    .require(StatType.DEXTERITY, 30)
                    .require(StatType.MINING, 30)
                    .build();
            default -> EquipmentRequirement.none();
        };
    }

    /**
     * Gets the farming tool requirements (hoe) for this tier.
     * @return EquipmentRequirement for farming tools of this tier
     */
    public EquipmentRequirement getFarmingToolRequirement() {
        return switch (this) {
            case WOOD -> EquipmentRequirement.none();
            case STONE -> EquipmentRequirement.builder()
                    .require(StatType.FARMING, 5)
                    .build();
            case IRON -> EquipmentRequirement.builder()
                    .require(StatType.FARMING, 12)
                    .build();
            case GOLD -> EquipmentRequirement.builder()
                    .require(StatType.FARMING, 15)
                    .build();
            case DIAMOND -> EquipmentRequirement.builder()
                    .require(StatType.FARMING, 25)
                    .build();
            case NETHERITE -> EquipmentRequirement.builder()
                    .require(StatType.FARMING, 40)
                    .build();
            default -> EquipmentRequirement.none();
        };
    }

    /**
     * Checks if player meets requirements for armor of this tier.
     */
    public boolean meetsArmorRequirements(PlayerStats playerStats) {
        return getArmorRequirement().meetsRequirements(playerStats);
    }

    /**
     * Checks if player meets requirements for mining tools of this tier.
     */
    public boolean meetsMiningToolRequirements(PlayerStats playerStats) {
        return getMiningToolRequirement().meetsRequirements(playerStats);
    }

    /**
     * Checks if player meets requirements for combat tools of this tier.
     */
    public boolean meetsCombatToolRequirements(PlayerStats playerStats) {
        return getCombatToolRequirement().meetsRequirements(playerStats);
    }

    /**
     * Checks if player meets requirements for axes of this tier.
     */
    public boolean meetsAxeRequirements(PlayerStats playerStats) {
        return getAxeRequirement().meetsRequirements(playerStats);
    }

    /**
     * Checks if player meets requirements for farming tools of this tier.
     */
    public boolean meetsFarmingToolRequirements(PlayerStats playerStats) {
        return getFarmingToolRequirement().meetsRequirements(playerStats);
    }
}
