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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Packet para sincronizar perks del servidor al cliente
 * Servidor → Cliente
 */
public record SyncPerksPacket(CompoundTag perksData) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncPerksPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(StatsSystem.MOD_ID, "sync_perks"));

    public static final StreamCodec<ByteBuf, SyncPerksPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            SyncPerksPacket::perksData,
            SyncPerksPacket::new
    );

    /**
     * Constructor desde PlayerStats
     */
    public SyncPerksPacket(PlayerStats stats) {
        this(createPerksTag(stats));
    }

    /**
     * Constructor directo con mapa de perks y set de disabled
     */
    public SyncPerksPacket(Map<String, Integer> unlockedPerks, Set<String> disabledPerks) {
        this(createPerksTag(unlockedPerks, disabledPerks));
    }

    /**
     * Crea un CompoundTag con solo los perks (para sincronización más eficiente)
     */
    private static CompoundTag createPerksTag(PlayerStats stats) {
        return createPerksTag(stats.getPerksMap(), stats.getDisabledPerks());
    }

    private static CompoundTag createPerksTag(Map<String, Integer> perks, Set<String> disabledPerks) {
        CompoundTag tag = new CompoundTag();

        // Guardar perks desbloqueados
        CompoundTag unlockedTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : perks.entrySet()) {
            unlockedTag.putInt(entry.getKey(), entry.getValue());
        }
        tag.put("unlocked", unlockedTag);

        // Guardar perks desactivados
        CompoundTag disabledTag = new CompoundTag();
        int index = 0;
        for (String perkId : disabledPerks) {
            disabledTag.putString("perk_" + index, perkId);
            index++;
        }
        tag.put("disabled", disabledTag);

        return tag;
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
                PlayerStats stats = minecraft.player.getData(ModAttachments.PLAYER_STATS);

                // Cargar los perks desde el NBT
                loadPerksFromTag(stats, perksData);

                StatsSystem.LOGGER.info("Perks sincronizados desde el servidor ({} perks)",
                        stats.getTotalPerksUnlocked());
            }
        });
    }

    /**
     * Carga los perks desde un CompoundTag al PlayerStats
     */
    private void loadPerksFromTag(PlayerStats stats, CompoundTag tag) {
        StatsSystem.LOGGER.info("=== SYNC PERKS: Cargando perks desde tag ===");

        // Cargar perks desbloqueados
        if (tag.contains("unlocked")) {
            CompoundTag unlockedTag = tag.getCompound("unlocked");
            StatsSystem.LOGGER.info("Total perks a sincronizar: {}", unlockedTag.getAllKeys().size());

            for (String perkId : unlockedTag.getAllKeys()) {
                int level = unlockedTag.getInt(perkId);
                int currentLevel = stats.getPerkLevel(perkId);

                StatsSystem.LOGGER.info("Perk {}: servidor={}, cliente={}", perkId, level, currentLevel);

                // Si el perk no está desbloqueado, desbloquearlo
                if (!stats.hasPerk(perkId)) {
                    StatsSystem.LOGGER.info("  -> Desbloqueando perk {} (nivel 1)", perkId);
                    stats.unlockPerk(perkId);
                }

                // Actualizar el nivel si es diferente
                if (stats.getPerkLevel(perkId) != level) {
                    StatsSystem.LOGGER.info("  -> Actualizando nivel de {} a {}", perkId, level);
                    stats.upgradePerk(perkId, level);
                    StatsSystem.LOGGER.info("  -> Nivel verificado después de upgrade: {}", stats.getPerkLevel(perkId));
                }
            }
        } else {
            // Compatibilidad con versiones anteriores
            for (String perkId : tag.getAllKeys()) {
                int level = tag.getInt(perkId);
                if (!stats.hasPerk(perkId)) {
                    stats.unlockPerk(perkId);
                }
                if (stats.getPerkLevel(perkId) != level) {
                    stats.upgradePerk(perkId, level);
                }
            }
        }

        // Cargar perks desactivados
        if (tag.contains("disabled")) {
            CompoundTag disabledTag = tag.getCompound("disabled");
            StatsSystem.LOGGER.info("Perks desactivados a cargar: {}", disabledTag.getAllKeys().size());
            for (String key : disabledTag.getAllKeys()) {
                String perkId = disabledTag.getString(key);
                StatsSystem.LOGGER.info("  -> Desactivando perk: {}", perkId);
                stats.disablePerk(perkId);
            }
        }

        StatsSystem.LOGGER.info("=== FIN SYNC PERKS ===");
    }
}
