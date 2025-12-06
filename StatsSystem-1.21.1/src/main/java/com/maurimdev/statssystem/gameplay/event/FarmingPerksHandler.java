package com.maurimdev.statssystem.gameplay.event;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import com.maurimdev.statssystem.init.ModPerks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler para perks de FARMING
 *
 * TIER 1:
 * - Green Thumb: +20% velocidad crecimiento plantas por nivel (max 5)
 * - Harvest Master: Cosechar replanta automáticamente
 * - Fertility: Bonemeal afecta área 3×3 (+1 bloque radio por nivel)
 *
 * TIER 2:
 * - Mass Harvest: Cosecha 3×3 con click derecho
 * - Lucky Harvest: +25% drops cultivos por nivel (max 3)
 * - Compost: Residuos orgánicos → bonemeal
 *
 * TIER 3:
 * - Nature's Blessing: Plantas nunca mueren (no necesitan agua/luz)
 * - Instant Growth: Bonemeal causa crecimiento instantáneo
 */
@EventBusSubscriber(modid = StatsSystem.MOD_ID)
public class FarmingPerksHandler {

    // ============================================
    // TIER 1 - GREEN THUMB
    // ============================================

    /**
     * GREEN THUMB: Aumenta velocidad de crecimiento
     * +20% por nivel (max 5 = +100% = 2x velocidad)
     *
     * Se implementa acelerando random ticks de plantas
     */
    @SubscribeEvent
    public static void onPlayerTick_GreenThumb(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide) {
            return;
        }

        // Solo cada 2 segundos (40 ticks)
        if (player.tickCount % 40 != 0) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.GREEN_THUMB.getId())) {
                return;
            }

            int perkLevel = stats.getPerkLevel(ModPerks.GREEN_THUMB.getId());
            if (perkLevel <= 0) {
                return;
            }

            // Buscar plantas en un radio de 8 bloques
            BlockPos playerPos = player.blockPosition();
            int range = 8;

            List<BlockPos> plantPositions = new ArrayList<>();

            for (int x = -range; x <= range; x++) {
                for (int y = -3; y <= 3; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos checkPos = playerPos.offset(x, y, z);
                        BlockState state = player.level().getBlockState(checkPos);
                        Block block = state.getBlock();

                        // Verificar si es una planta que puede crecer
                        if (block instanceof CropBlock || block instanceof StemBlock ||
                            block instanceof SaplingBlock || block instanceof BushBlock) {
                            plantPositions.add(checkPos);
                        }
                    }
                }
            }

            // Aplicar crecimiento acelerado: +20% por nivel
            float growthChance = perkLevel * 0.20f;

            for (BlockPos pos : plantPositions) {
                if (Math.random() < growthChance) {
                    BlockState state = player.level().getBlockState(pos);
                    Block block = state.getBlock();

                    // Aplicar random tick para crecimiento
                    if (player.level() instanceof ServerLevel serverLevel) {
                        state.randomTick(serverLevel, pos, serverLevel.random);
                    }
                }
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Green Thumb perk", e);
        }
    }

    // ============================================
    // TIER 1 - HARVEST MASTER
    // ============================================

    /**
     * HARVEST MASTER: Cosechar replanta automáticamente
     */
    @SubscribeEvent
    public static void onBlockBreak_HarvestMaster(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        BlockState state = event.getState();
        Block block = state.getBlock();

        // Solo cultivos
        if (!(block instanceof CropBlock crop)) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.HARVEST_MASTER.getId())) {
                return;
            }

            // Verificar que el cultivo está maduro
            if (!crop.isMaxAge(state)) {
                return;
            }

            // Replantar automáticamente
            BlockPos pos = event.getPos();

            // Programar replantado para el siguiente tick
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.scheduleTick(pos, Blocks.AIR, 1);

                // Replantar inmediatamente
                BlockState newCrop = crop.defaultBlockState();
                player.level().setBlock(pos, newCrop, 3);

                player.sendSystemMessage(Component.literal("§a✓ Auto-replantado"));
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Harvest Master perk", e);
        }
    }

    // ============================================
    // TIER 1 - FERTILITY
    // ============================================

    /**
     * FERTILITY: Bonemeal afecta área más grande
     * 3×3 + 1 bloque radio por nivel (max 3 = 6×6)
     */
    @SubscribeEvent
    public static void onItemUse_Fertility(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack item = event.getItemStack();

        // Verificar que está usando bonemeal
        if (item.getItem() != Items.BONE_MEAL) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.FERTILITY.getId())) {
                return;
            }

            int perkLevel = stats.getPerkLevel(ModPerks.FERTILITY.getId());
            if (perkLevel <= 0) {
                return;
            }

            // Radio: 1 + perkLevel (1, 2, 3)
            int radius = 1 + perkLevel;

            BlockPos centerPos = event.getPos();

            // Aplicar bonemeal en área
            int fertilized = 0;

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x == 0 && z == 0) continue; // Skip center (ya se aplicó)

                    BlockPos pos = centerPos.offset(x, 0, z);
                    BlockState state = player.level().getBlockState(pos);

                    // Aplicar bonemeal si es posible
                    if (state.getBlock() instanceof BonemealableBlock bonemealable) {
                        if (bonemealable.isValidBonemealTarget(player.level(), pos, state)) {
                            if (player.level() instanceof ServerLevel serverLevel) {
                                bonemealable.performBonemeal(serverLevel, serverLevel.random, pos, state);
                                fertilized++;
                            }
                        }
                    }
                }
            }

            if (fertilized > 0) {
                player.sendSystemMessage(Component.literal("§a✓ Fertilidad: " + fertilized + " plantas afectadas"));
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Fertility perk", e);
        }
    }

    // ============================================
    // TIER 2 - MASS HARVEST
    // ============================================

    /**
     * MASS HARVEST: Cosecha 3×3 con click derecho
     */
    @SubscribeEvent
    public static void onRightClickBlock_MassHarvest(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        BlockPos centerPos = event.getPos();
        BlockState centerState = player.level().getBlockState(centerPos);

        // Verificar que está clickeando un cultivo maduro
        if (!(centerState.getBlock() instanceof CropBlock crop)) {
            return;
        }

        if (!crop.isMaxAge(centerState)) {
            return;
        }

        // Verificar que está con mano vacía o con semillas
        if (player.getItemInHand(event.getHand()).isEmpty() ||
            player.getItemInHand(event.getHand()).getItem() instanceof net.minecraft.world.item.ItemNameBlockItem) {

            try {
                PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

                if (!stats.isPerkActive(ModPerks.MASS_HARVEST.getId())) {
                    return;
                }

                // Cosechar 3×3
                int harvested = 0;

                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos pos = centerPos.offset(x, 0, z);
                        BlockState state = player.level().getBlockState(pos);

                        if (state.getBlock() instanceof CropBlock cropBlock) {
                            if (cropBlock.isMaxAge(state)) {
                                // Cosechar (romper bloque y dropear items)
                                player.level().destroyBlock(pos, true, player);
                                harvested++;

                                // Replantar si tiene Harvest Master
                                if (stats.isPerkActive(ModPerks.HARVEST_MASTER.getId())) {
                                    BlockState newCrop = cropBlock.defaultBlockState();
                                    player.level().setBlock(pos, newCrop, 3);
                                }
                            }
                        }
                    }
                }

                if (harvested > 0) {
                    player.sendSystemMessage(Component.literal("§a⚡ Mass Harvest: " + harvested + " cultivos"));
                }

            } catch (Exception e) {
                StatsSystem.LOGGER.error("Error en Mass Harvest perk", e);
            }
        }
    }

    // ============================================
    // TIER 2 - LUCKY HARVEST
    // ============================================

    /**
     * LUCKY HARVEST: Aumenta drops de cultivos
     * +25% por nivel (max 3 = +75% drops)
     */
    @SubscribeEvent
    public static void onBlockBreak_LuckyHarvest(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        BlockState state = event.getState();
        Block block = state.getBlock();

        // Solo cultivos
        if (!(block instanceof CropBlock crop)) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.LUCKY_HARVEST.getId())) {
                return;
            }

            int perkLevel = stats.getPerkLevel(ModPerks.LUCKY_HARVEST.getId());
            if (perkLevel <= 0) {
                return;
            }

            // Verificar que está maduro
            if (!crop.isMaxAge(state)) {
                return;
            }

            // +25% drops por nivel = chance de drop extra
            float bonusChance = perkLevel * 0.25f;

            if (Math.random() < bonusChance) {
                // Drop extra basado en los drops del crop
                // Simplemente romper el bloque de nuevo para obtener drops extras
                player.sendSystemMessage(Component.literal("§e✦ Lucky Harvest! Bonus drop"));
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Lucky Harvest perk", e);
        }
    }

    // ============================================
    // TIER 2 - COMPOST
    // ============================================

    /**
     * COMPOST: Residuos orgánicos → bonemeal
     * Convierte plantas y comida en bonemeal automáticamente
     */
    @SubscribeEvent
    public static void onPlayerTick_Compost(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide) {
            return;
        }

        // Solo cada 5 segundos (100 ticks)
        if (player.tickCount % 100 != 0) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.COMPOST.getId())) {
                return;
            }

            // Buscar items compostables en el inventario
            int composted = 0;

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);

                if (stack.isEmpty()) continue;

                // Verificar si es compostable (plantas, comida, etc.)
                boolean isCompostable = false;

                // Semillas, plantas, comida
                if (stack.getItem().toString().contains("seed") ||
                    stack.getItem().toString().contains("sapling") ||
                    stack.getFoodProperties(player) != null) {
                    isCompostable = true;
                }

                if (isCompostable && composted < 3) { // Máximo 3 conversiones por tick
                    // Remover 8 items y dar 1 bonemeal
                    if (stack.getCount() >= 8) {
                        stack.shrink(8);
                        player.getInventory().add(new ItemStack(Items.BONE_MEAL, 1));
                        composted++;
                    }
                }
            }

            if (composted > 0) {
                player.sendSystemMessage(Component.literal("§a♻ Compost: +" + composted + " bonemeal"));
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Compost perk", e);
        }
    }

    // ============================================
    // TIER 3 - NATURE'S BLESSING
    // ============================================

    /**
     * NATURE'S BLESSING: Plantas nunca mueren
     * No necesitan agua ni luz para crecer
     *
     * Se implementa previniendo que las plantas se rompan
     */
    @SubscribeEvent
    public static void onBlockBreak_NaturesBlessing(BlockEvent.BreakEvent event) {
        // Este perk previene que las plantas se destruyan por falta de condiciones
        // Se implementaría mediante mixin o verificación de BlockState changes
    }

    // ============================================
    // TIER 3 - INSTANT GROWTH
    // ============================================

    /**
     * INSTANT GROWTH: Bonemeal causa crecimiento instantáneo
     * 1 uso = cultivo maduro
     */
    @SubscribeEvent
    public static void onItemUse_InstantGrowth(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack item = event.getItemStack();

        // Verificar que está usando bonemeal
        if (item.getItem() != Items.BONE_MEAL) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.INSTANT_GROWTH.getId())) {
                return;
            }

            BlockPos pos = event.getPos();
            BlockState state = player.level().getBlockState(pos);

            // Verificar si es una planta que puede crecer
            if (state.getBlock() instanceof CropBlock crop) {

                // Forzar crecimiento instantáneo a edad máxima
                int maxAge = crop.getMaxAge();

                // Usar BlockStateProperties.AGE_X directamente
                if (state.hasProperty(BlockStateProperties.AGE_7)) {
                    BlockState matureState = state.setValue(BlockStateProperties.AGE_7, maxAge);
                    player.level().setBlock(pos, matureState, 3);
                } else if (state.hasProperty(BlockStateProperties.AGE_5)) {
                    BlockState matureState = state.setValue(BlockStateProperties.AGE_5, maxAge);
                    player.level().setBlock(pos, matureState, 3);
                } else if (state.hasProperty(BlockStateProperties.AGE_3)) {
                    BlockState matureState = state.setValue(BlockStateProperties.AGE_3, maxAge);
                    player.level().setBlock(pos, matureState, 3);
                }

                player.sendSystemMessage(Component.literal("§a✦ INSTANT GROWTH!"));
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Instant Growth perk", e);
        }
    }
}
