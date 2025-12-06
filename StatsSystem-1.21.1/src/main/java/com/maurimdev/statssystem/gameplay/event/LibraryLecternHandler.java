package com.maurimdev.statssystem.gameplay.event;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.gameplay.item.SoulBookItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Coloca el Soul Book en atriles de bibliotecas cuando se cargan chunks
 * Este es un sistema simple que detecta atriles en chunks recién cargados
 */
@EventBusSubscriber(modid = StatsSystem.MOD_ID)
public class LibraryLecternHandler {

    // Evitar procesar el mismo chunk múltiples veces
    private static final Set<Long> processedChunks = new HashSet<>();
    private static final int MAX_CACHE_SIZE = 1000;

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof Level level)) {
            return;
        }

        // Solo en el servidor
        if (level.isClientSide()) {
            return;
        }

        BlockPos chunkPos = event.getChunk().getPos().getWorldPosition();
        long chunkKey = chunkPos.asLong();

        // Ya procesamos este chunk
        if (processedChunks.contains(chunkKey)) {
            return;
        }

        // Limpiar cache si crece mucho
        if (processedChunks.size() > MAX_CACHE_SIZE) {
            processedChunks.clear();
        }

        processedChunks.add(chunkKey);

        // Buscar atriles en el chunk
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int chunkX = event.getChunk().getPos().x * 16;
        int chunkZ = event.getChunk().getPos().z * 16;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                    mutablePos.set(chunkX + x, y, chunkZ + z);
                    BlockState state = level.getBlockState(mutablePos);

                    if (state.getBlock() instanceof LecternBlock) {
                        // Encontramos un atril, verificar si está vacío
                        if (level.getBlockEntity(mutablePos) instanceof LecternBlockEntity lectern) {
                            if (!lectern.hasBook()) {
                                // 100% probabilidad - siempre colocar el Soul Book en atriles vacíos
                                ItemStack soulBook = SoulBookItem.createSoulBookForLectern();
                                LecternBlock.tryPlaceBook(null, level, mutablePos.immutable(), state, soulBook);
                                StatsSystem.LOGGER.info("Placed Soul Book on lectern at {}", mutablePos);
                            }
                        }
                    }
                }
            }
        }
    }
}
