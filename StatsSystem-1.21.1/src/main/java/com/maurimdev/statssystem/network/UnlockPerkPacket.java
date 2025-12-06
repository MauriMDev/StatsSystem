package com.maurimdev.statssystem.network;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.core.domain.perk.Perk;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.core.service.PerkService;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Packet para desbloquear un perk
 * Cliente → Servidor
 */
public record UnlockPerkPacket(String perkId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UnlockPerkPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(StatsSystem.MOD_ID, "unlock_perk"));

    public static final StreamCodec<ByteBuf, UnlockPerkPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            UnlockPerkPacket::perkId,
            UnlockPerkPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
                PerkService service = PerkService.getInstance();

                // Validar que el perk existe
                Perk perk = service.getPerkById(perkId);
                if (perk == null) {
                    player.sendSystemMessage(Component.literal("§cError: Perk no encontrado"));
                    StatsSystem.LOGGER.warn("Jugador {} intentó desbloquear perk inexistente: {}",
                            player.getName().getString(), perkId);
                    return;
                }

                // Verificar si ya está desbloqueado
                if (stats.hasPerk(perkId)) {
                    player.sendSystemMessage(Component.literal("§eYa tienes este perk desbloqueado"));
                    return;
                }

                // Verificar requisitos
                int playerXpLevel = player.experienceLevel;
                if (!service.canUnlock(stats, playerXpLevel, perkId)) {
                    player.sendSystemMessage(Component.literal("§cNo cumples los requisitos para este perk"));
                    return;
                }

                // Desbloquear el perk
                stats.unlockPerk(perkId);

                // Consumir niveles de XP
                int cost = perk.getRequirement().getXpLevelCost();
                player.giveExperienceLevels(-cost);

                // Mensaje de éxito
                player.sendSystemMessage(Component.literal("§a✓ Perk desbloqueado: §6" + perk.getName()));
                StatsSystem.LOGGER.info("✓ {} desbloqueó el perk: {}", player.getName().getString(), perkId);

                // Sincronizar con el cliente
                ModMessages.sendToPlayer(new SyncPerksPacket(stats), player);
            }
        });
    }
}
