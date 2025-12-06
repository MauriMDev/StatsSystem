package com.maurimdev.statssystem.network;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Packet para resetear todas las estadísticas
 * Cliente → Servidor
 */
public record ResetStatsPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ResetStatsPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(StatsSystem.MOD_ID, "reset_stats"));

    public static final StreamCodec<ByteBuf, ResetStatsPacket> CODEC = StreamCodec.unit(new ResetStatsPacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                // ✅ Usar Data Attachments
                PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

                // Resetear stats
                stats.reset();

                StatsSystem.LOGGER.info("✅ Stats reseteadas para " + player.getName().getString());

                // ✅ El guardado es automático con Data Attachments

                // Sincronizar con el cliente
                ModMessages.sendToPlayer(new SyncStatsPacket(stats), player);
            }
        });
    }
}