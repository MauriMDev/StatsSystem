package com.maurimdev.statssystem.gameplay.event;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import com.maurimdev.statssystem.init.ModPerks;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handler para perks de STRENGTH
 *
 * TIER 1:
 * - Power Strike: +10% daño melee por nivel (max 5)
 * - Knockback Mastery: +15% knockback por nivel (max 3)
 * - Heavy Hitter: 5% chance de stun por 1 segundo
 *
 * TIER 2:
 * - Critical Strike: +10% chance crítico por nivel (+50% dmg)
 * - Armor Breaker: Ignora 20% armadura por nivel (max 3)
 * - Cleave: Golpes afectan área 2 bloques
 *
 * TIER 3:
 * - Execute: +100% daño a enemigos <20% HP
 * - Titan's Fury: +5% daño por kill (stack 5x, 10s)
 */
@EventBusSubscriber(modid = StatsSystem.MOD_ID)
public class StrengthPerksHandler {

    // Titan's Fury stacks (UUID -> {stacks, timestamp})
    private static final Map<UUID, TitansFuryData> TITANS_FURY_STACKS = new HashMap<>();
    private static final long TITANS_FURY_DURATION_MS = 10000; // 10 segundos
    private static final int MAX_TITANS_FURY_STACKS = 5;

    private static class TitansFuryData {
        int stacks;
        long timestamp;

        TitansFuryData(int stacks, long timestamp) {
            this.stacks = stacks;
            this.timestamp = timestamp;
        }
    }

    // ============================================
    // TIER 1 - POWER STRIKE
    // ============================================

    /**
     * POWER STRIKE: Aumenta daño melee
     * +10% por nivel (max 5 = +50% daño)
     */
    @SubscribeEvent
    public static void onLivingDamage_PowerStrike(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        // Solo daño melee (sin proyectiles)
        if (!event.getSource().isDirect()) {
            return;
        }

        try {
            PlayerStats stats = attacker.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.POWER_STRIKE.getId())) {
                return;
            }

            int perkLevel = stats.getPerkLevel(ModPerks.POWER_STRIKE.getId());
            if (perkLevel <= 0) {
                return;
            }

            // +10% daño por nivel
            float damageBonus = perkLevel * 0.10f;
            float newDamage = event.getAmount() * (1.0f + damageBonus);

            event.setAmount(newDamage);

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Power Strike perk", e);
        }
    }

    // ============================================
    // TIER 1 - KNOCKBACK MASTERY
    // ============================================

    /**
     * KNOCKBACK MASTERY: Aumenta knockback
     * +15% por nivel (max 3 = +45% knockback)
     *
     * Nota: Se implementa aumentando la velocidad del knockback
     */
    @SubscribeEvent
    public static void onLivingDamage_KnockbackMastery(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        if (!event.getSource().isDirect()) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }

        try {
            PlayerStats stats = attacker.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.KNOCKBACK_MASTERY.getId())) {
                return;
            }

            int perkLevel = stats.getPerkLevel(ModPerks.KNOCKBACK_MASTERY.getId());
            if (perkLevel <= 0) {
                return;
            }

            // Aumentar knockback aplicando velocidad adicional
            float knockbackMultiplier = 1.0f + (perkLevel * 0.15f);

            double dx = target.getX() - attacker.getX();
            double dz = target.getZ() - attacker.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);

            if (distance > 0) {
                double strength = 0.4 * knockbackMultiplier;
                target.setDeltaMovement(
                    target.getDeltaMovement().add(
                        (dx / distance) * strength,
                        0.1,
                        (dz / distance) * strength
                    )
                );
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Knockback Mastery perk", e);
        }
    }

    // ============================================
    // TIER 1 - HEAVY HITTER
    // ============================================

    /**
     * HEAVY HITTER: 5% chance de stun
     * Stun = Slowness IV por 1 segundo
     */
    @SubscribeEvent
    public static void onLivingDamage_HeavyHitter(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        if (!event.getSource().isDirect()) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }

        try {
            PlayerStats stats = attacker.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.HEAVY_HITTER.getId())) {
                return;
            }

            // 5% chance
            if (Math.random() > 0.05) {
                return;
            }

            // Aplicar stun (Slowness IV por 1 segundo)
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 3, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 20, 3, false, false));

            // Mensaje al atacante
            attacker.sendSystemMessage(Component.literal("§6⚡ STUN!"));

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Heavy Hitter perk", e);
        }
    }

    // ============================================
    // TIER 2 - CRITICAL STRIKE
    // ============================================

    /**
     * CRITICAL STRIKE: +10% chance de crítico por nivel
     * Crítico = +50% daño adicional
     */
    @SubscribeEvent
    public static void onLivingDamage_CriticalStrike(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        if (!event.getSource().isDirect()) {
            return;
        }

        try {
            PlayerStats stats = attacker.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.CRITICAL_STRIKE.getId())) {
                return;
            }

            int perkLevel = stats.getPerkLevel(ModPerks.CRITICAL_STRIKE.getId());
            if (perkLevel <= 0) {
                return;
            }

            // Chance de crítico: 10% por nivel
            double critChance = perkLevel * 0.10;

            if (Math.random() < critChance) {
                // Aplicar daño crítico (+50%)
                float newDamage = event.getAmount() * 1.5f;
                event.setAmount(newDamage);

                // Mensaje visual
                attacker.sendSystemMessage(Component.literal("§c§l⚔ CRÍTICO! §r§c+" + String.format("%.1f", newDamage - event.getAmount()) + " daño"));
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Critical Strike perk", e);
        }
    }

    // ============================================
    // TIER 2 - ARMOR BREAKER
    // ============================================

    /**
     * ARMOR BREAKER: Ignora armadura enemiga
     * 20% por nivel (max 3 = 60% ignorado)
     *
     * Implementado aumentando el daño que bypass armor
     */
    @SubscribeEvent
    public static void onLivingDamage_ArmorBreaker(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        if (!event.getSource().isDirect()) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }

        try {
            PlayerStats stats = attacker.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.ARMOR_BREAKER.getId())) {
                return;
            }

            int perkLevel = stats.getPerkLevel(ModPerks.ARMOR_BREAKER.getId());
            if (perkLevel <= 0) {
                return;
            }

            // Calcular bonus de daño basado en la armadura del enemigo
            float armorValue = target.getArmorValue();

            if (armorValue > 0) {
                // Por cada punto de armadura, aumentamos el daño en proporción al perk level
                float armorPenetration = perkLevel * 0.20f; // 20%, 40%, 60%
                float bonusDamage = armorValue * armorPenetration * 0.1f; // Factor de conversión

                event.setAmount(event.getAmount() + bonusDamage);
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Armor Breaker perk", e);
        }
    }

    // ============================================
    // TIER 2 - CLEAVE
    // ============================================

    /**
     * CLEAVE: Golpes afectan área
     * Radio de 2 bloques
     */
    @SubscribeEvent
    public static void onLivingDamage_Cleave(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        if (!event.getSource().isDirect()) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity mainTarget)) {
            return;
        }

        try {
            PlayerStats stats = attacker.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.CLEAVE.getId())) {
                return;
            }

            // Buscar entidades cercanas al target principal
            AABB searchBox = mainTarget.getBoundingBox().inflate(2.0);
            List<LivingEntity> nearbyEntities = mainTarget.level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity != mainTarget && entity != attacker && entity.isAlive()
            );

            // Aplicar 50% del daño original a enemigos cercanos
            float cleaveDamage = event.getNewDamage() * 0.5f;

            for (LivingEntity target : nearbyEntities) {
                target.hurt(event.getSource(), cleaveDamage);
            }

            if (!nearbyEntities.isEmpty()) {
                attacker.sendSystemMessage(Component.literal("§6⚔ Cleave: " + nearbyEntities.size() + " enemigos adicionales"));
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Cleave perk", e);
        }
    }

    // ============================================
    // TIER 3 - EXECUTE
    // ============================================

    /**
     * EXECUTE: +100% daño a enemigos con <20% HP
     */
    @SubscribeEvent
    public static void onLivingDamage_Execute(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }

        try {
            PlayerStats stats = attacker.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.EXECUTE.getId())) {
                return;
            }

            // Verificar si el target tiene <20% HP
            float healthPercent = target.getHealth() / target.getMaxHealth();

            if (healthPercent < 0.20f) {
                // Doble daño
                float newDamage = event.getAmount() * 2.0f;
                event.setAmount(newDamage);

                attacker.sendSystemMessage(Component.literal("§4§l⚔ EXECUTE! §r§c+100% DAÑO"));
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Execute perk", e);
        }
    }

    // ============================================
    // TIER 3 - TITAN'S FURY
    // ============================================

    /**
     * TITAN'S FURY: Cada kill aumenta daño
     * +5% daño por kill (stack max 5x, dura 10s)
     */
    @SubscribeEvent
    public static void onLivingDeath_TitansFury(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        try {
            PlayerStats stats = attacker.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.TITANS_FURY.getId())) {
                return;
            }

            UUID playerId = attacker.getUUID();
            long currentTime = System.currentTimeMillis();

            // Obtener stacks actuales
            TitansFuryData data = TITANS_FURY_STACKS.get(playerId);

            int currentStacks = 0;
            if (data != null && (currentTime - data.timestamp) < TITANS_FURY_DURATION_MS) {
                currentStacks = data.stacks;
            }

            // Incrementar stacks (max 5)
            int newStacks = Math.min(currentStacks + 1, MAX_TITANS_FURY_STACKS);

            // Guardar nuevos stacks
            TITANS_FURY_STACKS.put(playerId, new TitansFuryData(newStacks, currentTime));

            // Mensaje al jugador
            int bonusPercent = newStacks * 5;
            attacker.sendSystemMessage(Component.literal("§6⚡ TITAN'S FURY: " + newStacks + "x (+" + bonusPercent + "% daño)"));

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Titan's Fury perk (death)", e);
        }
    }

    /**
     * Aplicar bonus de Titan's Fury al daño
     */
    @SubscribeEvent
    public static void onLivingDamage_TitansFuryBonus(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        try {
            PlayerStats stats = attacker.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.TITANS_FURY.getId())) {
                return;
            }

            UUID playerId = attacker.getUUID();
            long currentTime = System.currentTimeMillis();

            TitansFuryData data = TITANS_FURY_STACKS.get(playerId);

            if (data == null || (currentTime - data.timestamp) >= TITANS_FURY_DURATION_MS) {
                return; // Sin stacks activos
            }

            // Aplicar bonus: +5% por stack
            float damageBonus = data.stacks * 0.05f;
            float newDamage = event.getAmount() * (1.0f + damageBonus);

            event.setAmount(newDamage);

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Titan's Fury perk (damage)", e);
        }
    }
}
