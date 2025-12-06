package com.maurimdev.statssystem.gameplay.event;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import com.maurimdev.statssystem.init.ModPerks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

/**
 * Handler para perks de MINING
 *
 * TIER 1:
 * - Efficient Mining: +15% velocidad minado por nivel (max 5)
 * - Vein Finder: Resalta ores cercanos (8 bloques, toggle)
 * - Preservation: -15% desgaste herramientas por nivel (max 3)
 *
 * TIER 2:
 * - Fortune I: Fortuna I sin encantamiento
 * - Tunnel Vision: Minar en línea recta +10% speed por nivel (max 3)
 * - Unbreakable Tools: Herramientas se detienen al 1% durabilidad
 *
 * TIER 3:
 * - Fortune II: Fortuna II sin encantamiento
 * - Excavator: Minado 3×3 con pico (toggle)
 */
@EventBusSubscriber(modid = StatsSystem.MOD_ID)
public class MiningPerksHandler {

    // Vein Finder toggle
    private static final Set<UUID> VEIN_FINDER_ENABLED = new HashSet<>();

    // Excavator toggle
    private static final Set<UUID> EXCAVATOR_ENABLED = new HashSet<>();

    // Tunnel Vision tracking
    private static final Map<UUID, TunnelVisionData> TUNNEL_VISION_TRACKING = new HashMap<>();

    private static class TunnelVisionData {
        BlockPos lastPos;
        BlockPos direction;
        int consecutiveBlocks;

        TunnelVisionData(BlockPos lastPos, BlockPos direction, int consecutiveBlocks) {
            this.lastPos = lastPos;
            this.direction = direction;
            this.consecutiveBlocks = consecutiveBlocks;
        }
    }

    // ============================================
    // TIER 1 - EFFICIENT MINING
    // ============================================

    /**
     * EFFICIENT MINING: Aumenta velocidad de minado
     * +15% por nivel (max 5 = +75%)
     *
     * Se aplica via atributo en ModEvents para persistencia
     */
    // Implementado en ModEvents.java como atributo de mining speed

    // ============================================
    // TIER 1 - VEIN FINDER
    // ============================================

    /**
     * VEIN FINDER: Resalta ores cercanos (8 bloques)
     * Activable con toggle
     */
    @SubscribeEvent
    public static void onPlayerTick_VeinFinder(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide) {
            return;
        }

        // Solo cada segundo (20 ticks)
        if (player.tickCount % 20 != 0) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.VEIN_FINDER.getId())) {
                return;
            }

            UUID playerId = player.getUUID();

            if (!VEIN_FINDER_ENABLED.contains(playerId)) {
                return;
            }

            // Buscar ores cercanos
            BlockPos playerPos = player.blockPosition();
            int range = 8;

            List<BlockPos> orePositions = new ArrayList<>();

            for (int x = -range; x <= range; x++) {
                for (int y = -range; y <= range; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos checkPos = playerPos.offset(x, y, z);
                        BlockState state = player.level().getBlockState(checkPos);
                        String blockName = state.getBlock().toString().toLowerCase();

                        // Detectar ores
                        if (blockName.contains("ore") || blockName.contains("ancient_debris")) {
                            orePositions.add(checkPos);
                        }
                    }
                }
            }

            // Enviar información al jugador
            if (!orePositions.isEmpty()) {
                player.sendSystemMessage(Component.literal("§e⛏ Vein Finder: " + orePositions.size() + " ores cercanos"));
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Vein Finder perk", e);
        }
    }

    /**
     * Toggle Vein Finder
     */
    public static void toggleVeinFinder(Player player) {
        UUID playerId = player.getUUID();

        if (VEIN_FINDER_ENABLED.contains(playerId)) {
            VEIN_FINDER_ENABLED.remove(playerId);
            player.sendSystemMessage(Component.literal("§cVein Finder: DESACTIVADO"));
        } else {
            VEIN_FINDER_ENABLED.add(playerId);
            player.sendSystemMessage(Component.literal("§aVein Finder: ACTIVADO"));
        }
    }

    // ============================================
    // TIER 1 - PRESERVATION
    // ============================================

    /**
     * PRESERVATION: Reduce desgaste de herramientas
     * -15% por nivel (max 3 = -45%)
     */
    @SubscribeEvent
    public static void onBlockBreak_Preservation(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        ItemStack tool = player.getMainHandItem();

        if (!(tool.getItem() instanceof DiggerItem)) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.PRESERVATION.getId())) {
                return;
            }

            int perkLevel = stats.getPerkLevel(ModPerks.PRESERVATION.getId());
            if (perkLevel <= 0) {
                return;
            }

            // -15% desgaste por nivel
            // Chance de no gastar durabilidad
            float preservationChance = perkLevel * 0.15f;

            if (Math.random() < preservationChance) {
                // Restaurar 1 de durabilidad para compensar el desgaste
                if (tool.isDamageableItem() && tool.getDamageValue() > 0) {
                    tool.setDamageValue(tool.getDamageValue() - 1);
                }
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Preservation perk", e);
        }
    }

    // ============================================
    // TIER 2 - FORTUNE I
    // ============================================

    /**
     * FORTUNE I: Fortuna I en herramientas de minado
     *
     * Se implementa en el evento de BlockEvent.BreakEvent
     * aplicando drops adicionales
     */
    @SubscribeEvent
    public static void onBlockBreak_Fortune(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack tool = player.getMainHandItem();

        if (!(tool.getItem() instanceof DiggerItem)) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            int fortuneLevel = 0;

            // Verificar Fortune I
            if (stats.isPerkActive(ModPerks.FORTUNE_I.getId())) {
                fortuneLevel = 1;
            }

            // Verificar Fortune II (sobrescribe)
            if (stats.isPerkActive(ModPerks.FORTUNE_II.getId())) {
                fortuneLevel = 2;
            }

            if (fortuneLevel == 0) {
                return;
            }

            // Verificar si la herramienta ya tiene Fortune
            // Si ya tiene, no aplicar el perk
            // TODO: Implementar lógica de Fortune manual si es necesario

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Fortune perk", e);
        }
    }

    // ============================================
    // TIER 2 - TUNNEL VISION
    // ============================================

    /**
     * TUNNEL VISION: Minar en línea recta da speed boost
     * +10% por nivel (max 3 = +30%) si mina 3+ bloques seguidos
     */
    @SubscribeEvent
    public static void onBlockBreak_TunnelVision(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.TUNNEL_VISION.getId())) {
                return;
            }

            int perkLevel = stats.getPerkLevel(ModPerks.TUNNEL_VISION.getId());
            if (perkLevel <= 0) {
                return;
            }

            UUID playerId = player.getUUID();
            BlockPos currentPos = event.getPos();

            TunnelVisionData data = TUNNEL_VISION_TRACKING.get(playerId);

            if (data == null) {
                // Primer bloque
                TUNNEL_VISION_TRACKING.put(playerId, new TunnelVisionData(currentPos, BlockPos.ZERO, 1));
                return;
            }

            // Calcular dirección
            BlockPos direction = currentPos.subtract(data.lastPos);

            // Verificar si es la misma dirección
            if (direction.equals(data.direction) || data.direction.equals(BlockPos.ZERO)) {
                // Misma dirección, incrementar contador
                int newCount = data.consecutiveBlocks + 1;
                TUNNEL_VISION_TRACKING.put(playerId, new TunnelVisionData(currentPos, direction, newCount));

                // Si tiene 3 o más bloques consecutivos, aplicar bonus
                if (newCount >= 3) {
                    float speedBonus = perkLevel * 0.10f;
                    player.sendSystemMessage(Component.literal("§e⛏ TUNNEL VISION: +" + (int)(speedBonus * 100) + "% speed (" + newCount + " bloques)"));
                }
            } else {
                // Dirección diferente, resetear
                TUNNEL_VISION_TRACKING.put(playerId, new TunnelVisionData(currentPos, direction, 1));
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Tunnel Vision perk", e);
        }
    }

    /**
     * Obtener bonus de Tunnel Vision
     */
    public static float getTunnelVisionBonus(Player player) {
        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.TUNNEL_VISION.getId())) {
                return 0.0f;
            }

            int perkLevel = stats.getPerkLevel(ModPerks.TUNNEL_VISION.getId());

            TunnelVisionData data = TUNNEL_VISION_TRACKING.get(player.getUUID());

            if (data != null && data.consecutiveBlocks >= 3) {
                return perkLevel * 0.10f;
            }

            return 0.0f;

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error obteniendo Tunnel Vision bonus", e);
            return 0.0f;
        }
    }

    // ============================================
    // TIER 2 - UNBREAKABLE TOOLS
    // ============================================

    /**
     * UNBREAKABLE TOOLS: Herramientas no se rompen
     * Se detienen al 1% durabilidad
     */
    @SubscribeEvent
    public static void onBlockBreak_UnbreakableTools(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        ItemStack tool = player.getMainHandItem();

        if (!(tool.getItem() instanceof DiggerItem)) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.UNBREAKABLE_TOOLS.getId())) {
                return;
            }

            // Verificar si la herramienta está por romperse
            if (tool.isDamageableItem()) {
                int maxDurability = tool.getMaxDamage();
                int currentDamage = tool.getDamageValue();
                int remainingDurability = maxDurability - currentDamage;

                // Si le queda menos del 1% de durabilidad, prevenir el uso
                if (remainingDurability <= Math.max(1, maxDurability / 100)) {
                    event.setCanceled(true);
                    player.sendSystemMessage(Component.literal("§c⚠ Herramienta sin durabilidad! Repárala antes de continuar."));
                }
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Unbreakable Tools perk", e);
        }
    }

    // ============================================
    // TIER 3 - FORTUNE II
    // ============================================

    /**
     * FORTUNE II: Fortuna II en herramientas de minado
     *
     * Implementado junto con Fortune I
     */

    // ============================================
    // TIER 3 - EXCAVATOR
    // ============================================

    /**
     * EXCAVATOR: Minado 3×3 con pico (toggle)
     */
    @SubscribeEvent
    public static void onBlockBreak_Excavator(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack tool = player.getMainHandItem();

        if (!(tool.getItem() instanceof PickaxeItem)) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.EXCAVATOR.getId())) {
                return;
            }

            UUID playerId = player.getUUID();

            if (!EXCAVATOR_ENABLED.contains(playerId)) {
                return;
            }

            // Minar 3×3
            BlockPos centerPos = event.getPos();
            BlockState centerState = event.getState();

            // Determinar plano de minado basado en la dirección que mira el jugador
            List<BlockPos> positionsToBreak = new ArrayList<>();

            // Obtener dirección del jugador
            float pitch = player.getXRot();
            float yaw = player.getYRot();

            // Minado horizontal (mirando al frente)
            if (Math.abs(pitch) < 45) {
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        if (x == 0 && y == 0) continue; // Skip center (ya se rompió)

                        BlockPos pos = centerPos.offset(x, y, 0);
                        positionsToBreak.add(pos);
                    }
                }
            }
            // Minado vertical (mirando arriba/abajo)
            else {
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && z == 0) continue;

                        BlockPos pos = centerPos.offset(x, 0, z);
                        positionsToBreak.add(pos);
                    }
                }
            }

            // Romper bloques adicionales
            for (BlockPos pos : positionsToBreak) {
                BlockState state = player.level().getBlockState(pos);

                // Verificar que el bloque sea mineable con la herramienta
                if (tool.isCorrectToolForDrops(state)) {
                    player.level().destroyBlock(pos, true, player);
                }
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Excavator perk", e);
        }
    }

    /**
     * Toggle Excavator
     */
    public static void toggleExcavator(Player player) {
        UUID playerId = player.getUUID();

        if (EXCAVATOR_ENABLED.contains(playerId)) {
            EXCAVATOR_ENABLED.remove(playerId);
            player.sendSystemMessage(Component.literal("§cExcavator: DESACTIVADO"));
        } else {
            EXCAVATOR_ENABLED.add(playerId);
            player.sendSystemMessage(Component.literal("§aExcavator: ACTIVADO"));
        }
    }
}
