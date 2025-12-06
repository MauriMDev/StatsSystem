package com.maurimdev.statssystem.gameplay.event;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import com.maurimdev.statssystem.network.ModMessages;
import com.maurimdev.statssystem.network.SyncStatsPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Eventos principales del mod
 * ✅ Actualizado para NeoForge 1.21.1 con Data Attachments
 */
@EventBusSubscriber(modid = StatsSystem.MOD_ID)
public class ModEvents {

    // ============================================
    // EVENTO 1: COPIAR AL MORIR
    // ============================================

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        try {
            if (event.isWasDeath()) {
                // ✅ NUEVO: Usar Data Attachments
                PlayerStats oldStats = event.getOriginal().getData(ModAttachments.PLAYER_STATS);
                PlayerStats newStats = event.getEntity().getData(ModAttachments.PLAYER_STATS);

                // DEBUG: Log antes de copiar
                StatsSystem.LOGGER.info("=== PLAYER CLONE (DEATH) ===");
                StatsSystem.LOGGER.info("Old player perks: {}", oldStats.getTotalPerksUnlocked());
                StatsSystem.LOGGER.info("Old player stats - VIT: {}, STR: {}",
                    oldStats.getVitality(), oldStats.getStrength());

                // Copiar datos del jugador anterior al nuevo
                newStats.copyFrom(oldStats);

                // DEBUG: Log después de copiar
                StatsSystem.LOGGER.info("New player perks after copy: {}", newStats.getTotalPerksUnlocked());
                StatsSystem.LOGGER.info("New player stats - VIT: {}, STR: {}",
                    newStats.getVitality(), newStats.getStrength());
                StatsSystem.LOGGER.info("=== CLONE COMPLETED ===");
            }
        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en PlayerClone", e);
        }
    }

    // ============================================
    // EVENTO 2: JUGADOR ENTRA (SINCRONIZAR)
    // ============================================

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        try {
            if (!(event.getEntity() instanceof ServerPlayer player)) {
                return;
            }

            // Esperar un tick antes de procesar
            player.getServer().execute(() -> {
                try {
                    // ✅ NUEVO: Obtener stats con Data Attachments
                    PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

                    // Sincronizar con cliente
                    ModMessages.sendToPlayer(new SyncStatsPacket(stats), player);

                } catch (Exception e) {
                    StatsSystem.LOGGER.error("Error crítico en login", e);
                }
            });

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en PlayerLoggedIn", e);
        }
    }

    // ============================================
    // EVENTO 3: CAMBIO DE DIMENSIÓN
    // ============================================

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        try {
            if (event.getEntity() instanceof ServerPlayer player) {
                // ✅ NUEVO: Usar Data Attachments
                PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
                ModMessages.sendToPlayer(new SyncStatsPacket(stats), player);
            }
        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en PlayerChangedDimension", e);
        }
    }

    // ============================================
    // EVENTO: RESPAWN DEL JUGADOR
    // ============================================

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        try {
            if (!(event.getEntity() instanceof ServerPlayer player)) {
                return;
            }

            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            // Sincronizar stats después del respawn
            ModMessages.sendToPlayer(new SyncStatsPacket(stats), player);

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en PlayerRespawn", e);
        }
    }
}