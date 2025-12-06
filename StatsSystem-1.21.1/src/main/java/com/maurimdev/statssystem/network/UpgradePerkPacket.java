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
 * Packet para mejorar el nivel de un perk
 * Cliente → Servidor
 */
public record UpgradePerkPacket(String perkId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UpgradePerkPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(StatsSystem.MOD_ID, "upgrade_perk"));

    public static final StreamCodec<ByteBuf, UpgradePerkPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            UpgradePerkPacket::perkId,
            UpgradePerkPacket::new
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
                    StatsSystem.LOGGER.warn("Jugador {} intentó mejorar perk inexistente: {}",
                            player.getName().getString(), perkId);
                    return;
                }

                // Verificar que el perk está desbloqueado
                if (!stats.hasPerk(perkId)) {
                    player.sendSystemMessage(Component.literal("§cDebes desbloquear este perk primero"));
                    return;
                }

                // Verificar que el perk es upgradeable
                if (!perk.isUpgradeable()) {
                    player.sendSystemMessage(Component.literal("§cEste perk no se puede mejorar"));
                    return;
                }

                // Obtener nivel actual
                int currentLevel = stats.getPerkLevel(perkId);
                int maxLevel = perk.getMaxLevel();

                StatsSystem.LOGGER.info("=== UPGRADE PERK {} ===", perkId);
                StatsSystem.LOGGER.info("Nivel actual: {}", currentLevel);
                StatsSystem.LOGGER.info("Nivel máximo: {}", maxLevel);

                // Verificar que no está en nivel máximo
                if (currentLevel >= maxLevel) {
                    player.sendSystemMessage(Component.literal("§eEste perk ya está en nivel máximo"));
                    StatsSystem.LOGGER.warn("Perk {} ya está en nivel máximo ({}/{})", perkId, currentLevel, maxLevel);
                    return;
                }

                // Calcular costo de mejora (costo base del perk)
                int upgradeCost = perk.getRequirement().getXpLevelCost();

                // Verificar que tiene suficiente XP
                int playerXpLevel = player.experienceLevel;
                if (playerXpLevel < upgradeCost) {
                    player.sendSystemMessage(Component.literal("§cNo tienes suficiente XP. Necesitas: " + upgradeCost + " niveles"));
                    return;
                }

                // Calcular nuevo nivel
                int newLevel = currentLevel + 1;
                StatsSystem.LOGGER.info("Calculando nuevo nivel: {} + 1 = {}", currentLevel, newLevel);

                // Mejorar el perk usando el método correcto
                boolean success = stats.upgradePerk(perkId, newLevel);
                StatsSystem.LOGGER.info("Resultado de upgradePerk(): {}", success);

                if (!success) {
                    player.sendSystemMessage(Component.literal("§cError al mejorar el perk"));
                    StatsSystem.LOGGER.error("Error al mejorar perk {} para {}", perkId, player.getName().getString());
                    return;
                }

                // Verificar el nivel después del upgrade
                int verifyLevel = stats.getPerkLevel(perkId);
                StatsSystem.LOGGER.info("Nivel después de upgrade: {}", verifyLevel);

                // Consumir niveles de XP
                player.giveExperienceLevels(-upgradeCost);

                // Mensaje de éxito
                player.sendSystemMessage(Component.literal("§a✓ Perk mejorado: §6" + perk.getName() + " §7[Nivel " + newLevel + "/" + maxLevel + "]"));
                StatsSystem.LOGGER.info("✓ {} mejoró el perk {} de nivel {} a nivel {}", player.getName().getString(), perkId, currentLevel, newLevel);

                // Sincronizar con el cliente
                ModMessages.sendToPlayer(new SyncPerksPacket(stats), player);
            }
        });
    }
}
