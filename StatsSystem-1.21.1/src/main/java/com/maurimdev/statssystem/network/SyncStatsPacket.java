package com.maurimdev.statssystem.network;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Paquete para sincronizar estadísticas del servidor al cliente
 * Servidor → Cliente
 */
public record SyncStatsPacket(CompoundTag statsData) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncStatsPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(StatsSystem.MOD_ID, "sync_stats"));

    public static final StreamCodec<ByteBuf, SyncStatsPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            SyncStatsPacket::statsData,
            SyncStatsPacket::new
    );

    /**
     * Constructor desde PlayerStats
     */
    public SyncStatsPacket(PlayerStats stats) {
        this(stats.saveNBTData());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Solo ejecutar en el cliente
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                // ✅ Usar Data Attachments
                PlayerStats stats = minecraft.player.getData(ModAttachments.PLAYER_STATS);

                // Cargar los datos desde el NBT
                stats.loadNBTData(statsData);

                StatsSystem.LOGGER.info("Estadísticas sincronizadas desde el servidor");
            }
        });
    }
}