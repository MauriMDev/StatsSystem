package com.maurimdev.statssystem.gameplay.event;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.core.domain.equipment.EquipmentRequirement;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import com.maurimdev.statssystem.init.EquipmentRequirementRegistry;
import com.maurimdev.statssystem.init.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Event handler that enforces stat requirements for equipment usage.
 * Players need to meet minimum stat levels to use certain equipment.
 * Restrictions only apply if the player has the Soul Book.
 */
@EventBusSubscriber(modid = StatsSystem.MOD_ID)
public class EquipmentRestrictionHandler {

    /**
     * Checks if a player has the Soul Book in inventory or equipped in Curios.
     */
    private static boolean hasSoulBook(Player player) {
        // Check inventory
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.SOUL_BOOK.get())) {
                return true;
            }
        }

        // Check Curios slots
        return CuriosApi.getCuriosInventory(player)
                .map(curiosInventory -> curiosInventory.findFirstCurio(ModItems.SOUL_BOOK.get()).isPresent())
                .orElse(false);
    }

    /**
     * Checks if a player meets the requirements to use an item.
     * @return true if can use, false if cannot
     */
    private static boolean canUseItem(Player player, ItemStack itemStack) {
        // If player doesn't have Soul Book, no restrictions apply
        if (!hasSoulBook(player)) {
            return true;
        }

        // Get the requirement for this item
        EquipmentRequirement requirement = EquipmentRequirementRegistry.getRequirement(itemStack);

        // If no requirements, allow usage
        if (requirement.isEmpty()) {
            return true;
        }

        // Check if player meets requirements
        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
        return requirement.meetsRequirements(stats);
    }

    /**
     * Sends an error message to the player about unmet requirements.
     */
    private static void sendRequirementError(Player player, ItemStack itemStack) {
        EquipmentRequirement requirement = EquipmentRequirementRegistry.getRequirement(itemStack);
        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

        String missingReqs = requirement.getMissingRequirementsMessage(stats);

        player.displayClientMessage(
            Component.literal("§c¡Requisitos no cumplidos! Necesitas: " + missingReqs),
            true
        );
    }

    // ============================================
    // EVENT 1: EQUIPPING ARMOR/ITEMS
    // ============================================

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Only check on server side
        if (player.level().isClientSide) {
            return;
        }

        ItemStack newItem = event.getTo();

        // Ignore if removing equipment
        if (newItem.isEmpty()) {
            return;
        }

        // Check if the item has requirements
        if (!EquipmentRequirementRegistry.hasRequirement(newItem.getItem())) {
            return;
        }

        // Check if player can use this item
        if (!canUseItem(player, newItem)) {
            EquipmentSlot slot = event.getSlot();

            // SOLO quitar armadura (HEAD, CHEST, LEGS, FEET)
            // NO quitar herramientas/armas (MAINHAND, OFFHAND)
            if (slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST ||
                slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET) {

                // Return item to inventory or drop it
                if (!player.getInventory().add(newItem.copy())) {
                    player.drop(newItem.copy(), false);
                }

                // Clear the armor slot
                player.setItemSlot(slot, ItemStack.EMPTY);

                StatsSystem.LOGGER.debug("Player {} tried to equip armor {} but doesn't meet requirements",
                    player.getName().getString(), newItem.getDisplayName().getString());
            }

            // Send error message (para armadura y herramientas)
            sendRequirementError(player, newItem);
        }
    }

    // ============================================
    // EVENT 2: USING TOOLS (Right Click)
    // ============================================

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack itemStack = event.getItemStack();

        // Only check on server side
        if (player.level().isClientSide) {
            return;
        }

        // Check if the item has requirements
        if (!EquipmentRequirementRegistry.hasRequirement(itemStack.getItem())) {
            return;
        }

        // Check if player can use this item
        if (!canUseItem(player, itemStack)) {
            event.setCanceled(true);
            sendRequirementError(player, itemStack);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack itemStack = event.getItemStack();

        // Only check on server side
        if (player.level().isClientSide) {
            return;
        }

        // Check if the item has requirements (for hoes, etc.)
        if (!EquipmentRequirementRegistry.hasRequirement(itemStack.getItem())) {
            return;
        }

        // Check if player can use this item
        if (!canUseItem(player, itemStack)) {
            event.setCanceled(true);
            sendRequirementError(player, itemStack);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        ItemStack itemStack = player.getMainHandItem();

        // Only check on server side
        if (player.level().isClientSide) {
            return;
        }

        // Check if the item has requirements (for mining tools)
        if (!EquipmentRequirementRegistry.hasRequirement(itemStack.getItem())) {
            return;
        }

        // Check if player can use this item
        if (!canUseItem(player, itemStack)) {
            event.setCanceled(true);
            sendRequirementError(player, itemStack);
        }
    }

    // ============================================
    // EVENT 3: ATTACKING WITH WEAPONS
    // ============================================

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttack(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        // Only check on server side
        if (player.level().isClientSide) {
            return;
        }

        ItemStack weapon = player.getMainHandItem();

        // Check if the weapon has requirements
        if (!EquipmentRequirementRegistry.hasRequirement(weapon.getItem())) {
            return;
        }

        // Check if player can use this weapon
        if (!canUseItem(player, weapon)) {
            // Cancel the damage by setting it to 0
            event.setNewDamage(0);

            sendRequirementError(player, weapon);

            StatsSystem.LOGGER.debug("Player {} tried to attack with {} but doesn't meet requirements",
                player.getName().getString(), weapon.getDisplayName().getString());
        }
    }

    // ============================================
    // EVENT 4: MINING/BREAKING BLOCKS
    // ============================================

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ItemStack tool = player.getMainHandItem();

        // Only check on server side
        if (player.level().isClientSide) {
            return;
        }

        // Check if the tool has requirements
        if (!EquipmentRequirementRegistry.hasRequirement(tool.getItem())) {
            return;
        }

        // Check if player can use this tool
        if (!canUseItem(player, tool)) {
            // Set break speed to 0 to prevent mining
            event.setNewSpeed(0.0f);

            // Only send message occasionally to avoid spam
            if (player.tickCount % 40 == 0) { // Every 2 seconds
                sendRequirementError(player, tool);
            }

            StatsSystem.LOGGER.debug("Player {} tried to mine with {} but doesn't meet requirements",
                player.getName().getString(), tool.getDisplayName().getString());
        }
    }
}
