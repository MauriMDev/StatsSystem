package com.maurimdev.statssystem.network;

import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet para activar/desactivar perks desde el cliente al servidor
 */
public record TogglePerkPacket(String perkId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TogglePerkPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("statssystem", "toggle_perk"));

    public static final StreamCodec<ByteBuf, TogglePerkPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            TogglePerkPacket::perkId,
            TogglePerkPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Maneja el packet en el servidor
     */
    public static void handle(TogglePerkPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

                // Toggle el perk
                boolean isNowActive = stats.togglePerk(packet.perkId);

                // Sincronizar con el cliente
                ModMessages.sendToPlayer(new SyncPerksPacket(stats.getPerksMap(), stats.getDisabledPerks()), player);

                // Mensaje al jugador
                String perkName = packet.perkId.replace("_", " ");
                if (isNowActive) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a✓ Perk activado: " + perkName));
                } else {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c✗ Perk desactivado: " + perkName));
                }
            }
        });
    }
}
