package com.maurimdev.statssystem.init;

import com.maurimdev.statssystem.core.domain.equipment.EquipmentRequirement;
import com.maurimdev.statssystem.core.domain.equipment.EquipmentTier;
import com.maurimdev.statssystem.core.domain.stats.StatType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/**
 * Central registry that maps Minecraft items to their stat requirements.
 * This registry must be initialized during mod startup.
 */
public class EquipmentRequirementRegistry {
    private static final Map<Item, EquipmentRequirement> REGISTRY = new HashMap<>();
    private static boolean initialized = false;

    /**
     * Registers a requirement for a specific item.
     */
    public static void registerRequirement(Item item, EquipmentRequirement requirement) {
        REGISTRY.put(item, requirement);
    }

    /**
     * Gets the requirement for a specific item.
     */
    public static EquipmentRequirement getRequirement(Item item) {
        return REGISTRY.getOrDefault(item, EquipmentRequirement.none());
    }

    /**
     * Gets the requirement for an ItemStack.
     */
    public static EquipmentRequirement getRequirement(ItemStack stack) {
        return getRequirement(stack.getItem());
    }

    /**
     * Checks if an item has any requirements.
     */
    public static boolean hasRequirement(Item item) {
        return REGISTRY.containsKey(item) && !REGISTRY.get(item).isEmpty();
    }

    /**
     * Initializes the registry with all vanilla Minecraft items.
     * This must be called during mod initialization.
     */
    public static void init() {
        if (initialized) {
            return;
        }

        // ===== ARMOR =====

        // Leather Armor - No requirements
        registerRequirement(Items.LEATHER_HELMET, EquipmentTier.LEATHER.getArmorRequirement());
        registerRequirement(Items.LEATHER_CHESTPLATE, EquipmentTier.LEATHER.getArmorRequirement());
        registerRequirement(Items.LEATHER_LEGGINGS, EquipmentTier.LEATHER.getArmorRequirement());
        registerRequirement(Items.LEATHER_BOOTS, EquipmentTier.LEATHER.getArmorRequirement());

        // Chainmail Armor - No requirements
        registerRequirement(Items.CHAINMAIL_HELMET, EquipmentTier.CHAINMAIL.getArmorRequirement());
        registerRequirement(Items.CHAINMAIL_CHESTPLATE, EquipmentTier.CHAINMAIL.getArmorRequirement());
        registerRequirement(Items.CHAINMAIL_LEGGINGS, EquipmentTier.CHAINMAIL.getArmorRequirement());
        registerRequirement(Items.CHAINMAIL_BOOTS, EquipmentTier.CHAINMAIL.getArmorRequirement());

        // Iron Armor - VITALITY 8
        registerRequirement(Items.IRON_HELMET, EquipmentTier.IRON.getArmorRequirement());
        registerRequirement(Items.IRON_CHESTPLATE, EquipmentTier.IRON.getArmorRequirement());
        registerRequirement(Items.IRON_LEGGINGS, EquipmentTier.IRON.getArmorRequirement());
        registerRequirement(Items.IRON_BOOTS, EquipmentTier.IRON.getArmorRequirement());

        // Gold Armor - VITALITY 10, STRENGTH 10
        registerRequirement(Items.GOLDEN_HELMET, EquipmentTier.GOLD.getArmorRequirement());
        registerRequirement(Items.GOLDEN_CHESTPLATE, EquipmentTier.GOLD.getArmorRequirement());
        registerRequirement(Items.GOLDEN_LEGGINGS, EquipmentTier.GOLD.getArmorRequirement());
        registerRequirement(Items.GOLDEN_BOOTS, EquipmentTier.GOLD.getArmorRequirement());

        // Diamond Armor - VITALITY 20, STRENGTH 15
        registerRequirement(Items.DIAMOND_HELMET, EquipmentTier.DIAMOND.getArmorRequirement());
        registerRequirement(Items.DIAMOND_CHESTPLATE, EquipmentTier.DIAMOND.getArmorRequirement());
        registerRequirement(Items.DIAMOND_LEGGINGS, EquipmentTier.DIAMOND.getArmorRequirement());
        registerRequirement(Items.DIAMOND_BOOTS, EquipmentTier.DIAMOND.getArmorRequirement());

        // Netherite Armor - VITALITY 32, STRENGTH 25
        registerRequirement(Items.NETHERITE_HELMET, EquipmentTier.NETHERITE.getArmorRequirement());
        registerRequirement(Items.NETHERITE_CHESTPLATE, EquipmentTier.NETHERITE.getArmorRequirement());
        registerRequirement(Items.NETHERITE_LEGGINGS, EquipmentTier.NETHERITE.getArmorRequirement());
        registerRequirement(Items.NETHERITE_BOOTS, EquipmentTier.NETHERITE.getArmorRequirement());

        // ===== MINING TOOLS (Pickaxes, Shovels) =====

        // Pickaxes
        registerRequirement(Items.WOODEN_PICKAXE, EquipmentTier.WOOD.getMiningToolRequirement());
        registerRequirement(Items.STONE_PICKAXE, EquipmentTier.STONE.getMiningToolRequirement());
        registerRequirement(Items.IRON_PICKAXE, EquipmentTier.IRON.getMiningToolRequirement());
        registerRequirement(Items.GOLDEN_PICKAXE, EquipmentTier.GOLD.getMiningToolRequirement());
        registerRequirement(Items.DIAMOND_PICKAXE, EquipmentTier.DIAMOND.getMiningToolRequirement());
        registerRequirement(Items.NETHERITE_PICKAXE, EquipmentTier.NETHERITE.getMiningToolRequirement());

        // Shovels
        registerRequirement(Items.WOODEN_SHOVEL, EquipmentTier.WOOD.getMiningToolRequirement());
        registerRequirement(Items.STONE_SHOVEL, EquipmentTier.STONE.getMiningToolRequirement());
        registerRequirement(Items.IRON_SHOVEL, EquipmentTier.IRON.getMiningToolRequirement());
        registerRequirement(Items.GOLDEN_SHOVEL, EquipmentTier.GOLD.getMiningToolRequirement());
        registerRequirement(Items.DIAMOND_SHOVEL, EquipmentTier.DIAMOND.getMiningToolRequirement());
        registerRequirement(Items.NETHERITE_SHOVEL, EquipmentTier.NETHERITE.getMiningToolRequirement());

        // ===== COMBAT TOOLS (Swords, Axes) =====

        // Swords
        registerRequirement(Items.WOODEN_SWORD, EquipmentTier.WOOD.getCombatToolRequirement());
        registerRequirement(Items.STONE_SWORD, EquipmentTier.STONE.getCombatToolRequirement());
        registerRequirement(Items.IRON_SWORD, EquipmentTier.IRON.getCombatToolRequirement());
        registerRequirement(Items.GOLDEN_SWORD, EquipmentTier.GOLD.getCombatToolRequirement());
        registerRequirement(Items.DIAMOND_SWORD, EquipmentTier.DIAMOND.getCombatToolRequirement());
        registerRequirement(Items.NETHERITE_SWORD, EquipmentTier.NETHERITE.getCombatToolRequirement());

        // Axes (combat tools + mining requirement)
        // Requieren MINING además de STR/DEX para forzar "misión secundaria"
        registerRequirement(Items.WOODEN_AXE, EquipmentTier.WOOD.getAxeRequirement());
        registerRequirement(Items.STONE_AXE, EquipmentTier.STONE.getAxeRequirement());
        registerRequirement(Items.IRON_AXE, EquipmentTier.IRON.getAxeRequirement());
        registerRequirement(Items.GOLDEN_AXE, EquipmentTier.GOLD.getAxeRequirement());
        registerRequirement(Items.DIAMOND_AXE, EquipmentTier.DIAMOND.getAxeRequirement());
        registerRequirement(Items.NETHERITE_AXE, EquipmentTier.NETHERITE.getAxeRequirement());

        // ===== FARMING TOOLS (Hoes) =====

        registerRequirement(Items.WOODEN_HOE, EquipmentTier.WOOD.getFarmingToolRequirement());
        registerRequirement(Items.STONE_HOE, EquipmentTier.STONE.getFarmingToolRequirement());
        registerRequirement(Items.IRON_HOE, EquipmentTier.IRON.getFarmingToolRequirement());
        registerRequirement(Items.GOLDEN_HOE, EquipmentTier.GOLD.getFarmingToolRequirement());
        registerRequirement(Items.DIAMOND_HOE, EquipmentTier.DIAMOND.getFarmingToolRequirement());
        registerRequirement(Items.NETHERITE_HOE, EquipmentTier.NETHERITE.getFarmingToolRequirement());

        // ===== RANGED WEAPONS =====

        // Bow - DEXTERITY 10
        registerRequirement(Items.BOW, EquipmentRequirement.builder()
                .require(StatType.DEXTERITY, 10)
                .build());

        // Crossbow - DEXTERITY 12
        registerRequirement(Items.CROSSBOW, EquipmentRequirement.builder()
                .require(StatType.DEXTERITY, 12)
                .build());

        // Trident - STRENGTH 15, DEXTERITY 12
        registerRequirement(Items.TRIDENT, EquipmentRequirement.builder()
                .require(StatType.STRENGTH, 15)
                .require(StatType.DEXTERITY, 12)
                .build());

        initialized = true;
    }

    /**
     * Clears the registry. Useful for testing.
     */
    public static void clear() {
        REGISTRY.clear();
        initialized = false;
    }

    /**
     * Gets the number of registered items.
     */
    public static int getRegistrySize() {
        return REGISTRY.size();
    }
}
