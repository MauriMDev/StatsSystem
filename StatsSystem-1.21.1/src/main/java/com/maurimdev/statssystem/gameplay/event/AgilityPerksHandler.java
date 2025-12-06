package com.maurimdev.statssystem.gameplay.event;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import com.maurimdev.statssystem.init.ModPerks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handler para perks de AGILITY
 *
 * TIER 1:
 * - Sprint Master: -20% consumo hambre al correr por nivel (max 5)
 * - Feather Fall: -30% daño caída por nivel (max 3)
 * - Leap: +25% altura salto por nivel (max 3)
 *
 * TIER 2:
 * - Wall Jump: Saltar contra muros permite segundo salto
 * - Air Dash: Dash aéreo (cooldown 3s)
 * - Safe Landing: Sin daño caída hasta 20 bloques
 *
 * TIER 3:
 * - Wind Runner: Sprint no consume hambre
 * - Double Jump: Doble salto en el aire
 */
@EventBusSubscriber(modid = StatsSystem.MOD_ID)
public class AgilityPerksHandler {

    // Double Jump tracking
    private static final Map<UUID, Boolean> HAS_DOUBLE_JUMPED = new HashMap<>();
    private static final Map<UUID, Boolean> WAS_ON_GROUND = new HashMap<>();

    // Air Dash cooldown
    private static final Map<UUID, Long> AIR_DASH_COOLDOWNS = new HashMap<>();
    private static final long AIR_DASH_COOLDOWN_MS = 3000; // 3 segundos

    // Wall Jump tracking
    private static final Map<UUID, Long> LAST_WALL_JUMP = new HashMap<>();
    private static final long WALL_JUMP_COOLDOWN_MS = 500; // 500ms entre wall jumps

    // ============================================
    // TIER 1 - SPRINT MASTER
    // ============================================

    /**
     * SPRINT MASTER: Reduce consumo de hambre al correr
     * -20% por nivel (max 5 = -100% con Wind Runner)
     *
     * Se implementa mediante FoodData exhaustion manipulation
     */
    @SubscribeEvent
    public static void onPlayerTick_SprintMaster(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide) {
            return;
        }

        if (!player.isSprinting()) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            // Verificar Wind Runner primero (elimina todo el consumo)
            if (stats.isPerkActive(ModPerks.WIND_RUNNER.getId())) {
                // Reducir exhaustion acumulado por sprint
                player.getFoodData().setExhaustion(Math.max(0, player.getFoodData().getExhaustionLevel() - 0.1f));
                return;
            }

            // Sprint Master normal
            if (!stats.isPerkActive(ModPerks.SPRINT_MASTER.getId())) {
                return;
            }

            int perkLevel = stats.getPerkLevel(ModPerks.SPRINT_MASTER.getId());
            if (perkLevel <= 0) {
                return;
            }

            // Reducir exhaustion: -20% por nivel
            float reduction = perkLevel * 0.02f; // 0.1 exhaustion por tick de sprint * 20%
            player.getFoodData().setExhaustion(Math.max(0, player.getFoodData().getExhaustionLevel() - reduction));

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Sprint Master perk", e);
        }
    }

    // ============================================
    // TIER 1 - FEATHER FALL
    // ============================================

    /**
     * FEATHER FALL: Reduce daño de caída
     * -30% por nivel (max 3 = -90%)
     */
    @SubscribeEvent
    public static void onLivingFall_FeatherFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.FEATHER_FALL.getId())) {
                return;
            }

            int perkLevel = stats.getPerkLevel(ModPerks.FEATHER_FALL.getId());
            if (perkLevel <= 0) {
                return;
            }

            // Reducir daño de caída: -30% por nivel
            float reduction = perkLevel * 0.30f;
            float newDamage = event.getDamageMultiplier() * (1.0f - reduction);

            event.setDamageMultiplier(Math.max(0, newDamage));

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Feather Fall perk", e);
        }
    }

    // ============================================
    // TIER 1 - LEAP
    // ============================================

    /**
     * LEAP: Aumenta altura de salto
     * +25% por nivel (max 3 = +75%)
     *
     * Se implementa en ModEvents como atributo de jump boost
     */
    // Implementado en ModEvents.java

    // ============================================
    // TIER 2 - WALL JUMP
    // ============================================

    /**
     * WALL JUMP: Saltar contra muros permite segundo salto
     */
    @SubscribeEvent
    public static void onPlayerTick_WallJump(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.WALL_JUMP.getId())) {
                return;
            }

            UUID playerId = player.getUUID();

            // Verificar si el jugador está contra una pared y en el aire
            if (!player.onGround() && player.horizontalCollision && player.getDeltaMovement().y < 0) {

                // Verificar cooldown
                Long lastWallJump = LAST_WALL_JUMP.get(playerId);
                long currentTime = System.currentTimeMillis();

                if (lastWallJump != null && (currentTime - lastWallJump) < WALL_JUMP_COOLDOWN_MS) {
                    return;
                }

                // Detectar intento de salto (cambio en Y)
                // Si el jugador intenta nadar hacia arriba o presiona espacio, su delta Y cambia
                // WALL JUMP automático cuando toca pared
                Vec3 motion = player.getDeltaMovement();

                // Wall jump activado automáticamente al tocar pared mientras cae
                player.setDeltaMovement(motion.x, 0.6, motion.z); // Boost hacia arriba

                // Pequeño empuje alejándose de la pared
                Vec3 lookVec = player.getLookAngle();
                player.setDeltaMovement(
                    player.getDeltaMovement().add(
                        -lookVec.x * 0.3,
                        0,
                        -lookVec.z * 0.3
                    )
                );

                LAST_WALL_JUMP.put(playerId, currentTime);
                player.sendSystemMessage(Component.literal("§b⬆ WALL JUMP!"));
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Wall Jump perk", e);
        }
    }

    // ============================================
    // TIER 2 - AIR DASH
    // ============================================

    /**
     * AIR DASH: Dash aéreo en dirección que miras
     * Cooldown: 3 segundos
     *
     * Se activa presionando shift doble en el aire
     */
    @SubscribeEvent
    public static void onPlayerTick_AirDash(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.AIR_DASH.getId())) {
                return;
            }

            UUID playerId = player.getUUID();

            // Verificar cooldown
            Long lastDash = AIR_DASH_COOLDOWNS.get(playerId);
            long currentTime = System.currentTimeMillis();

            if (lastDash != null && (currentTime - lastDash) < AIR_DASH_COOLDOWN_MS) {
                return;
            }

            // Detectar shift en el aire (por simplicidad, usar sneak + not on ground)
            if (!player.onGround() && player.isCrouching() && !player.isInWater()) {

                // AIR DASH!
                Vec3 lookVec = player.getLookAngle();
                double dashStrength = 1.5;

                player.setDeltaMovement(
                    lookVec.x * dashStrength,
                    Math.max(lookVec.y * dashStrength, 0.2), // Mínimo boost vertical
                    lookVec.z * dashStrength
                );

                AIR_DASH_COOLDOWNS.put(playerId, currentTime);
                player.sendSystemMessage(Component.literal("§b⚡ AIR DASH!"));
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Air Dash perk", e);
        }
    }

    // ============================================
    // TIER 2 - SAFE LANDING
    // ============================================

    /**
     * SAFE LANDING: Sin daño de caída hasta 20 bloques
     */
    @SubscribeEvent
    public static void onLivingFall_SafeLanding(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.SAFE_LANDING.getId())) {
                return;
            }

            // Calcular altura de caída
            float fallDistance = event.getDistance();

            // Sin daño hasta 20 bloques
            if (fallDistance <= 20.0f) {
                event.setDamageMultiplier(0.0f);
                event.setCanceled(true);
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Safe Landing perk", e);
        }
    }

    // ============================================
    // TIER 3 - WIND RUNNER
    // ============================================

    /**
     * WIND RUNNER: Sprint no consume hambre
     *
     * Implementado en Sprint Master (verifica Wind Runner primero)
     */

    // ============================================
    // TIER 3 - DOUBLE JUMP
    // ============================================

    /**
     * DOUBLE JUMP: Doble salto en el aire
     */
    @SubscribeEvent
    public static void onPlayerTick_DoubleJump(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.DOUBLE_JUMP.getId())) {
                return;
            }

            UUID playerId = player.getUUID();

            // Resetear double jump cuando toca el suelo
            if (player.onGround()) {
                HAS_DOUBLE_JUMPED.put(playerId, false);
                WAS_ON_GROUND.put(playerId, true);
                return;
            }

            // Verificar si acaba de salir del suelo
            Boolean wasOnGround = WAS_ON_GROUND.getOrDefault(playerId, true);
            if (wasOnGround) {
                WAS_ON_GROUND.put(playerId, false);
            }

            // Detectar intento de salto en el aire
            Boolean hasDoubleJumped = HAS_DOUBLE_JUMPED.getOrDefault(playerId, false);

            if (!hasDoubleJumped && !player.onGround() && !wasOnGround) {

                // Detectar caída (delta Y negativo significa que está cayendo)
                // Double jump se activa automáticamente cuando empieza a caer
                if (player.getDeltaMovement().y < -0.1 && player.getDeltaMovement().y > -0.5) {

                    // DOUBLE JUMP!
                    Vec3 motion = player.getDeltaMovement();
                    player.setDeltaMovement(motion.x, 0.5, motion.z);

                    HAS_DOUBLE_JUMPED.put(playerId, true);
                    player.sendSystemMessage(Component.literal("§b⬆⬆ DOUBLE JUMP!"));
                }
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Double Jump perk", e);
        }
    }
}
