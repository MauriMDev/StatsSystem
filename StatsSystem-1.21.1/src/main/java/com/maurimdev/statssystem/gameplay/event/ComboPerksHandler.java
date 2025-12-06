package com.maurimdev.statssystem.gameplay.event;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import com.maurimdev.statssystem.init.ModPerks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handler para COMBO PERKS (perks que requieren múltiples stats)
 *
 * COMBO PERKS:
 * - Ore Breaker: Minar ores con manos (STR 40 + DEX 40 + MINING 56)
 * - Berserker Mode: +50% dmg y +30% atk speed a <30% HP (STR 56 + VIT 56)
 * - Deadly Precision: Flechas cargadas = crítico + pierce 2 (DEX 48)
 * - Lightning Miner: Speed boost por bloque (MINING 48 + AGILITY 32)
 * - Iron Fortress: Más resistencia a menor HP (VIT 56 + STR 40)
 * - Nature Warrior: Cerca plantas da regen y speed (FARMING 40 + AGILITY 32)
 * - Assassin's Mark: Atacar espalda = +100% dmg (DEX 48 + STR 32)
 * - Master Craftsman: Crafting no gasta durabilidad (MINING 32 + FARMING 32 + DEX 32)
 */
@EventBusSubscriber(modid = StatsSystem.MOD_ID)
public class ComboPerksHandler {

    // Berserker Mode cooldown
    private static final Map<UUID, Long> BERSERKER_COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Long> BERSERKER_ACTIVE_UNTIL = new HashMap<>();
    private static final long BERSERKER_COOLDOWN_MS = 5 * 60 * 1000; // 5 minutos
    private static final long BERSERKER_DURATION_MS = 10 * 1000; // 10 segundos

    // Lightning Miner stacks
    private static final Map<UUID, LightningMinerData> LIGHTNING_MINER_STACKS = new HashMap<>();
    private static final long LIGHTNING_MINER_DURATION_MS = 5000; // 5 segundos
    private static final int MAX_LIGHTNING_MINER_STACKS = 5;

    private static class LightningMinerData {
        int stacks;
        long timestamp;

        LightningMinerData(int stacks, long timestamp) {
            this.stacks = stacks;
            this.timestamp = timestamp;
        }
    }

    // ============================================
    // ORE BREAKER
    // ============================================

    /**
     * ORE BREAKER: Minar ores con las manos
     * Requisitos: STRENGTH 40 + DEXTERITY 40 + MINING 56
     */
    @SubscribeEvent
    public static void onBlockBreak_OreBreaker(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        // Verificar que está minando con las manos
        ItemStack tool = player.getMainHandItem();
        if (!tool.isEmpty()) {
            return;
        }

        BlockState state = event.getState();

        // Verificar si es un ore
        if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE) ||
            !state.getBlock().toString().toLowerCase().contains("ore")) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.ORE_BREAKER.getId())) {
                // Sin el perk, cancelar (no se puede minar ore con manos)
                event.setCanceled(true);
                return;
            }

            // Con el perk, permitir minado pero más lento
            player.sendSystemMessage(Component.literal("§6⚒ ORE BREAKER: Minando con manos"));

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Ore Breaker perk", e);
        }
    }

    // ============================================
    // BERSERKER MODE
    // ============================================

    /**
     * BERSERKER MODE: +50% daño y +30% velocidad ataque a <30% HP
     * Duración: 10s, Cooldown: 5 minutos
     * Requisitos: STRENGTH 56 + VITALITY 56
     */
    @SubscribeEvent
    public static void onPlayerTick_BerserkerMode(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.BERSERKER_MODE.getId())) {
                return;
            }

            UUID playerId = player.getUUID();
            long currentTime = System.currentTimeMillis();

            // Verificar si está activo
            Long activeUntil = BERSERKER_ACTIVE_UNTIL.get(playerId);
            if (activeUntil != null && currentTime < activeUntil) {
                // Todavía activo
                return;
            }

            // Verificar HP
            float healthPercent = player.getHealth() / player.getMaxHealth();

            if (healthPercent < 0.30f) {
                // Verificar cooldown
                Long lastUsed = BERSERKER_COOLDOWNS.get(playerId);

                if (lastUsed != null && (currentTime - lastUsed) < BERSERKER_COOLDOWN_MS) {
                    // En cooldown
                    return;
                }

                // ACTIVAR BERSERKER MODE
                BERSERKER_ACTIVE_UNTIL.put(playerId, currentTime + BERSERKER_DURATION_MS);
                BERSERKER_COOLDOWNS.put(playerId, currentTime);

                player.sendSystemMessage(Component.literal("§4§l⚔ BERSERKER MODE ACTIVADO!"));
                player.sendSystemMessage(Component.literal("§c+50% daño | +30% velocidad ataque | 10s"));
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Berserker Mode perk", e);
        }
    }

    /**
     * Aplicar bonus de daño de Berserker Mode
     */
    @SubscribeEvent
    public static void onDamage_BerserkerMode(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        try {
            PlayerStats stats = attacker.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.BERSERKER_MODE.getId())) {
                return;
            }

            UUID playerId = attacker.getUUID();
            long currentTime = System.currentTimeMillis();

            Long activeUntil = BERSERKER_ACTIVE_UNTIL.get(playerId);

            if (activeUntil != null && currentTime < activeUntil) {
                // Berserker activo, aplicar bonus
                float newDamage = event.getAmount() * 1.5f;
                event.setAmount(newDamage);
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error aplicando Berserker Mode damage", e);
        }
    }

    // ============================================
    // DEADLY PRECISION
    // ============================================

    /**
     * DEADLY PRECISION: Flechas cargadas = crítico garantizado + atraviesan 2 enemigos
     * Requisitos: DEXTERITY 48
     */
    @SubscribeEvent
    public static void onArrowDamage_DeadlyPrecision(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof AbstractArrow arrow)) {
            return;
        }

        if (!(arrow.getOwner() instanceof Player shooter)) {
            return;
        }

        try {
            PlayerStats stats = shooter.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.DEADLY_PRECISION.getId())) {
                return;
            }

            // Verificar si la flecha está cargada completamente
            if (arrow.getDeltaMovement().length() > 2.0) {
                // Crítico garantizado
                event.setAmount(event.getAmount() * 1.5f);

                // Piercing ya se maneja con arrow.setPierceLevel(2) en EntityJoinLevel
                shooter.sendSystemMessage(Component.literal("§6⚡ DEADLY PRECISION!"));
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Deadly Precision perk", e);
        }
    }

    // ============================================
    // LIGHTNING MINER
    // ============================================

    /**
     * LIGHTNING MINER: Speed boost por bloque minado (stack 5x)
     * Requisitos: MINING 48 + AGILITY 32
     */
    @SubscribeEvent
    public static void onBlockBreak_LightningMiner(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.LIGHTNING_MINER.getId())) {
                return;
            }

            UUID playerId = player.getUUID();
            long currentTime = System.currentTimeMillis();

            // Obtener stacks actuales
            LightningMinerData data = LIGHTNING_MINER_STACKS.get(playerId);

            int currentStacks = 0;
            if (data != null && (currentTime - data.timestamp) < LIGHTNING_MINER_DURATION_MS) {
                currentStacks = data.stacks;
            }

            // Incrementar stacks (max 5)
            int newStacks = Math.min(currentStacks + 1, MAX_LIGHTNING_MINER_STACKS);

            // Guardar nuevos stacks
            LIGHTNING_MINER_STACKS.put(playerId, new LightningMinerData(newStacks, currentTime));

            // Mensaje al jugador
            int bonusPercent = newStacks * 10;
            player.sendSystemMessage(Component.literal("§e⚡ LIGHTNING MINER: " + newStacks + "x (+" + bonusPercent + "% speed)"));

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Lightning Miner perk", e);
        }
    }

    /**
     * Obtener bonus de Lightning Miner
     */
    public static float getLightningMinerBonus(Player player) {
        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.LIGHTNING_MINER.getId())) {
                return 0.0f;
            }

            UUID playerId = player.getUUID();
            long currentTime = System.currentTimeMillis();

            LightningMinerData data = LIGHTNING_MINER_STACKS.get(playerId);

            if (data == null || (currentTime - data.timestamp) >= LIGHTNING_MINER_DURATION_MS) {
                return 0.0f;
            }

            // +10% por stack
            return data.stacks * 0.10f;

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error obteniendo Lightning Miner bonus", e);
            return 0.0f;
        }
    }

    // ============================================
    // IRON FORTRESS
    // ============================================

    /**
     * IRON FORTRESS: Más resistencia a menor HP
     * Hasta +40% reducción daño a 20% HP
     * Requisitos: VITALITY 56 + STRENGTH 40
     */
    @SubscribeEvent
    public static void onDamage_IronFortress(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.IRON_FORTRESS.getId())) {
                return;
            }

            // Calcular resistencia basada en HP
            float healthPercent = player.getHealth() / player.getMaxHealth();

            // A menor HP, mayor resistencia
            // 100% HP = 0% reducción
            // 20% HP = 40% reducción
            float damageReduction = 0.0f;

            if (healthPercent <= 0.20f) {
                damageReduction = 0.40f; // Máximo 40%
            } else if (healthPercent <= 0.50f) {
                // Escala lineal entre 20% y 50% HP
                damageReduction = 0.40f * (0.50f - healthPercent) / 0.30f;
            }

            if (damageReduction > 0) {
                float newDamage = event.getNewDamage() * (1.0f - damageReduction);
                event.setNewDamage(newDamage);

                int reductionPercent = (int) (damageReduction * 100);
                player.sendSystemMessage(Component.literal("§7🛡 Iron Fortress: -" + reductionPercent + "% daño"));
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Iron Fortress perk", e);
        }
    }

    // ============================================
    // NATURE WARRIOR
    // ============================================

    /**
     * NATURE WARRIOR: Cerca de plantas da regeneración y velocidad
     * +0.5 HP/s y +20% speed en 5 bloques de plantas
     * Requisitos: FARMING 40 + AGILITY 32
     */
    @SubscribeEvent
    public static void onPlayerTick_NatureWarrior(PlayerTickEvent.Post event) {
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

            if (!stats.isPerkActive(ModPerks.NATURE_WARRIOR.getId())) {
                return;
            }

            // Buscar plantas cercanas
            BlockPos playerPos = player.blockPosition();
            int range = 5;
            int plantCount = 0;

            for (int x = -range; x <= range; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos checkPos = playerPos.offset(x, y, z);
                        BlockState state = player.level().getBlockState(checkPos);
                        Block block = state.getBlock();

                        // Contar plantas
                        if (block instanceof net.minecraft.world.level.block.CropBlock ||
                            block instanceof net.minecraft.world.level.block.SaplingBlock ||
                            block instanceof net.minecraft.world.level.block.BushBlock ||
                            block instanceof net.minecraft.world.level.block.FlowerBlock) {
                            plantCount++;

                            if (plantCount >= 5) break;
                        }
                    }
                    if (plantCount >= 5) break;
                }
                if (plantCount >= 5) break;
            }

            // Si hay suficientes plantas, aplicar bonus
            if (plantCount >= 5) {
                // Regeneración +0.5 HP/s
                if (player.getHealth() < player.getMaxHealth()) {
                    player.heal(0.5f);
                }

                // Speed bonus se aplica via atributos en ModEvents
                player.sendSystemMessage(Component.literal("§a🌿 Nature Warrior activo"));
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Nature Warrior perk", e);
        }
    }

    // ============================================
    // ASSASSIN'S MARK
    // ============================================

    /**
     * ASSASSIN'S MARK: Atacar por la espalda = +100% daño
     * Requisitos: DEXTERITY 48 + STRENGTH 32
     */
    @SubscribeEvent
    public static void onDamage_AssassinsMark(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }

        try {
            PlayerStats stats = attacker.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.ASSASSINS_MARK.getId())) {
                return;
            }

            // Verificar si está atacando por la espalda
            Vec3 attackerPos = attacker.position();
            Vec3 targetPos = target.position();
            Vec3 targetLook = target.getLookAngle();

            // Vector del target al atacante
            Vec3 toAttacker = attackerPos.subtract(targetPos).normalize();

            // Producto punto para verificar si está detrás (180° = -1.0)
            double dotProduct = targetLook.dot(toAttacker);

            // Si dotProduct < -0.5, está en los 180° traseros
            if (dotProduct < -0.5) {
                // BACKSTAB!
                float newDamage = event.getAmount() * 2.0f;
                event.setAmount(newDamage);

                attacker.sendSystemMessage(Component.literal("§4⚔ ASSASSIN'S MARK! +100% DAÑO"));
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Assassin's Mark perk", e);
        }
    }

    // ============================================
    // MASTER CRAFTSMAN
    // ============================================

    /**
     * MASTER CRAFTSMAN: Crafting no consume durabilidad
     * +25% velocidad de crafteo
     * Requisitos: MINING 32 + FARMING 32 + DEXTERITY 32
     *
     * Se implementa en el evento de crafting
     */
    // La implementación completa requiere hooks en el sistema de crafting
}
