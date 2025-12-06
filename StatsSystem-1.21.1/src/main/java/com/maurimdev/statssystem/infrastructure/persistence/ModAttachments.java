package com.maurimdev.statssystem.infrastructure.persistence;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Sistema de Data Attachments para NeoForge 1.21.1
 * Reemplaza el sistema viejo de Capabilities
 */
public class ModAttachments {

    // DeferredRegister para registrar los attachment types
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, StatsSystem.MOD_ID);

    /**
     * Serializador personalizado para PlayerStats
     */
    private static final IAttachmentSerializer<CompoundTag, PlayerStats> PLAYER_STATS_SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public @NotNull PlayerStats read(@NotNull IAttachmentHolder holder, @NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
                    PlayerStats stats = new PlayerStats();
                    stats.loadNBTData(tag);
                    return stats;
                }

                @Override
                public @NotNull CompoundTag write(@NotNull PlayerStats stats, @NotNull HolderLookup.Provider provider) {
                    return stats.saveNBTData();
                }
            };

    /**
     * Attachment para las estadísticas del jugador
     * Se adjunta automáticamente a cada EntityPlayer
     * El serializador maneja automáticamente el guardado/cargado en NBT
     */
    public static final Supplier<AttachmentType<PlayerStats>> PLAYER_STATS =
            ATTACHMENT_TYPES.register("player_stats", () ->
                    AttachmentType.builder(PlayerStats::new)
                            .serialize(PLAYER_STATS_SERIALIZER)
                            .build()
            );
}