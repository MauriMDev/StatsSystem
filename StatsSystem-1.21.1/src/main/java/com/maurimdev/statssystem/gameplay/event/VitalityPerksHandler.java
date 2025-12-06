package com.maurimdev.statssystem.gameplay.event;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import com.maurimdev.statssystem.init.ModPerks;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handler que implementa la lógica de perks de VITALIDAD
 *
 * TIER 1:
 * - Iron Skin: Reducción de daño +5% por nivel (max 5 = 25% reducción)
 * - Regeneration: Regeneración pasiva por niveles (1-3)
 *   · Nivel 1: +0.5 HP cada 5 segundos
 *   · Nivel 2: +1.0 HP cada 3 segundos
 *   · Nivel 3: +2.0 HP cada 2 segundos
 *
 * TIER 2:
 * - Second Wind: Cooldown de comida -20% por nivel + saturación +10% por nivel (max 3)
 *
 * TIER 3:
 * - Phoenix Heart: Auto-revive 1 vez/día con 50% HP + Resistencia II por 10s
 */
@EventBusSubscriber(modid = StatsSystem.MOD_ID)
public class VitalityPerksHandler {

    // Cooldowns de perks (UUID -> timestamp en ms)
    private static final Map<UUID, Long> PHOENIX_HEART_COOLDOWNS = new HashMap<>();

    // Constantes de cooldowns
    private static final long PHOENIX_HEART_COOLDOWN_MS = 24 * 60 * 60 * 1000; // 24 horas

    // ============================================
    // IRON SKIN - Reducción de daño
    // ============================================

    /**
     * IRON SKIN: Otorga armadura adicional
     * +2 puntos de armadura por nivel (max 5 = +10 armor)
     *
     * NOTA: Este perk se aplica mediante el modificador de atributo ARMOR
     * en StatBonusHandler.updateIronSkinBonus(), no como reducción de daño.
     * Este evento ha sido DESACTIVADO.
     */
    // @SubscribeEvent - DESACTIVADO: Iron Skin ahora usa modificador de atributo
    public static void onLivingDamage_IronSkin_OBSOLETE(LivingDamageEvent.Pre event) {
        // Este método ya no se usa
        // Iron Skin ahora aplica un modificador de ARMOR directamente
        // Ver: StatBonusHandler.updateIronSkinBonus()
    }

    // ============================================
    // REGENERATION - Regeneración pasiva con niveles
    // ============================================

    /**
     * REGENERATION: Regeneración de salud natural
     * Nivel 1: +0.5 HP cada 5 segundos (100 ticks)
     * Nivel 2: +1.0 HP cada 3 segundos (60 ticks)
     * Nivel 3: +2.0 HP cada 2 segundos (40 ticks)
     */
    @SubscribeEvent
    public static void onPlayerTick_Regeneration(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Solo en servidor
        if (player.level().isClientSide) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            // Verificar si tiene el perk Regeneration
            if (!stats.isPerkActive(ModPerks.REGENERATION.getId())) {
                return;
            }

            // Obtener nivel del perk (1-3)
            int perkLevel = stats.getPerkLevel(ModPerks.REGENERATION.getId());
            if (perkLevel <= 0) {
                return;
            }

            // Determinar frecuencia y cantidad según nivel
            int tickInterval;
            float healAmount;

            switch (perkLevel) {
                case 1:
                    tickInterval = 100; // 5 segundos
                    healAmount = 0.5f;
                    break;
                case 2:
                    tickInterval = 60;  // 3 segundos
                    healAmount = 1.0f;
                    break;
                case 3:
                default:
                    tickInterval = 40;  // 2 segundos
                    healAmount = 2.0f;
                    break;
            }

            // Verificar si es momento de regenerar
            if (player.tickCount % tickInterval != 0) {
                return;
            }

            // Regenerar si no está en salud completa
            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(healAmount);

                // Efecto visual opcional (partículas de regeneración)
                // player.level().broadcastEntityEvent(player, (byte) 35); // Hearts particles
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Regeneration perk", e);
        }
    }

    // ============================================
    // SECOND WIND - Cooldown de comida reducido
    // ============================================

    /**
     * SECOND WIND: Reduce el cooldown de comida
     * -20% cooldown por nivel (max 3 = 60% reducción)
     *
     * Nota: Este perk afecta la velocidad de consumo de comida.
     * TODO: Reimplementar usando mixins o un approach diferente.
     * La implementación actual causaba un loop infinito en la animación de comer.
     * Por ahora, este perk está desactivado.
     */
    @SubscribeEvent
    public static void onPlayerTick_SecondWind(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Solo en servidor
        if (player.level().isClientSide) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            // Verificar si tiene el perk Second Wind
            if (!stats.isPerkActive(ModPerks.SECOND_WIND.getId())) {
                return;
            }

            // TODO: Implementar Second Wind correctamente
            // La implementación anterior causaba problemas con la animación de comer
            // Se necesita usar mixins o un método alternativo para modificar la velocidad de consumo

            /* CÓDIGO PROBLEMÁTICO COMENTADO:
            // Verificar si está comiendo
            if (!player.isUsingItem()) {
                return;
            }

            // Verificar si está comiendo comida
            if (player.getUseItem().getFoodProperties(player) == null) {
                return;
            }

            // Obtener nivel del perk (1-3)
            int perkLevel = stats.getPerkLevel(ModPerks.SECOND_WIND.getId());
            if (perkLevel <= 0) {
                return;
            }

            // PROBLEMA: Este código causaba un loop infinito
            if (player.tickCount % 2 == 0) {
                player.releaseUsingItem();  // ← Esto resetea el progreso
                player.startUsingItem(player.getUsedItemHand());  // ← Loop infinito
            }
            */

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Second Wind perk", e);
        }
    }



    // ============================================
    // TIER 3 - PHOENIX HEART
    // ============================================

    /**
     * PHOENIX HEART: Auto-revive una vez por día
     * Te revive con 50% HP + Resistencia II por 10 segundos
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath_PhoenixHeart(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.PHOENIX_HEART.getId())) {
                return;
            }

            // Verificar cooldown
            long currentTime = System.currentTimeMillis();
            Long lastUsed = PHOENIX_HEART_COOLDOWNS.get(player.getUUID());

            if (lastUsed != null && (currentTime - lastUsed) < PHOENIX_HEART_COOLDOWN_MS) {
                // En cooldown
                long remainingMs = PHOENIX_HEART_COOLDOWN_MS - (currentTime - lastUsed);
                long remainingHours = remainingMs / (60 * 60 * 1000);
                player.sendSystemMessage(Component.literal("§cPhoenix Heart en cooldown: " + remainingHours + " horas"));
                return;
            }

            // ACTIVAR PHOENIX HEART
            event.setCanceled(true);

            // Revivir con 50% HP
            float maxHealth = player.getMaxHealth();
            player.setHealth(maxHealth * 0.5f);

            // Otorgar Resistencia II por 10 segundos (200 ticks)
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,
                200, // 10 segundos
                1,   // Nivel II (índice 1 = nivel II)
                false,
                true
            ));

            // Guardar cooldown
            PHOENIX_HEART_COOLDOWNS.put(player.getUUID(), currentTime);

            // Efectos visuales y mensaje
            player.sendSystemMessage(Component.literal("§6§l✧ ✦ ✧ PHOENIX HEART ✧ ✦ ✧"));
            player.sendSystemMessage(Component.literal("§e¡Has renacido de las cenizas!"));
            player.sendSystemMessage(Component.literal("§7Resistencia II por 10 segundos"));

            StatsSystem.LOGGER.info("Phoenix Heart activado para {}", player.getName().getString());

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Phoenix Heart perk", e);
        }
    }

}
