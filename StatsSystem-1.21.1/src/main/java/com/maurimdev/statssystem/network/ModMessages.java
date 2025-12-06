package com.maurimdev.statssystem.network;

import com.maurimdev.statssystem.StatsSystem;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registro y gestión de packets de red
 * Compatible con NeoForge 1.21.1
 */
public class ModMessages {

    /**
     * Registra todos los packets del mod
     * Este método debe ser llamado desde FMLCommonSetupEvent
     */
    public static void register() {
        // Este método ahora es llamado desde el evento RegisterPayloadHandlersEvent
        // que se dispara automáticamente durante la inicialización del mod
    }

    /**
     * Registra los packets usando el evento de NeoForge
     */
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(StatsSystem.MOD_ID)
                .versioned("1.0")
                .optional();

        // Cliente → Servidor
        registrar.playToServer(
                ResetStatsPacket.TYPE,
                ResetStatsPacket.CODEC,
                ResetStatsPacket::handle
        );

        registrar.playToServer(
                UnlockPerkPacket.TYPE,
                UnlockPerkPacket.CODEC,
                UnlockPerkPacket::handle
        );

        registrar.playToServer(
                TogglePerkPacket.TYPE,
                TogglePerkPacket.CODEC,
                TogglePerkPacket::handle
        );

        registrar.playToServer(
                UpgradePerkPacket.TYPE,
                UpgradePerkPacket.CODEC,
                UpgradePerkPacket::handle
        );

        // Servidor → Cliente
        registrar.playToClient(
                SyncStatsPacket.TYPE,
                SyncStatsPacket.CODEC,
                SyncStatsPacket::handle
        );

        registrar.playToClient(
                SyncPerksPacket.TYPE,
                SyncPerksPacket.CODEC,
                SyncPerksPacket::handle
        );

        StatsSystem.LOGGER.info("Packets de red registrados (Stats + Perks)");
    }

    /**
     * Envía ResetStatsPacket al servidor
     */
    public static void sendToServer(ResetStatsPacket message) {
        PacketDistributor.sendToServer(message);
    }

    /**
     * Envía UnlockPerkPacket al servidor
     */
    public static void sendToServer(UnlockPerkPacket message) {
        PacketDistributor.sendToServer(message);
    }

    /**
     * Envía TogglePerkPacket al servidor
     */
    public static void sendToServer(TogglePerkPacket message) {
        PacketDistributor.sendToServer(message);
    }

    /**
     * Envía UpgradePerkPacket al servidor
     */
    public static void sendToServer(UpgradePerkPacket message) {
        PacketDistributor.sendToServer(message);
    }

    /**
     * Envía SyncStatsPacket a un jugador específico
     */
    public static void sendToPlayer(SyncStatsPacket message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }

    /**
     * Envía SyncPerksPacket a un jugador específico
     */
    public static void sendToPlayer(SyncPerksPacket message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }
}