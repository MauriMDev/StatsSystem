package com.maurimdev.statssystem.gameplay.event;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.gameplay.item.SoulBookItem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Detecta cuando el libro especial del atril se obtiene
 * y lo convierte inmediatamente en Soul Book
 */
@EventBusSubscriber(modid = StatsSystem.MOD_ID)
public class LecternBookPickupHandler {

    /**
     * Se dispara cuando una entidad (incluyendo items) aparece en el mundo
     * Interceptamos aquí para convertir el written book en Soul Book
     * cuando se dropea al suelo
     */
    @SubscribeEvent
    public static void onItemSpawn(EntityJoinLevelEvent event) {
        // Solo en el servidor
        if (event.getLevel().isClientSide()) {
            return;
        }

        // Verificar si es un ItemEntity (item en el suelo)
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();

            // Verificar si es nuestro libro especial del lectern
            if (SoulBookItem.isLecternSoulBook(stack)) {
                // Convertir a Soul Book
                ItemStack soulBook = SoulBookItem.convertToSoulBook(stack);

                // Reemplazar el item en la entidad
                itemEntity.setItem(soulBook);

                StatsSystem.LOGGER.info("Converted dropped lectern book to Soul Book at {}",
                    itemEntity.position());
            }
        }
    }

    /**
     * Revisa cada tick si el jugador tiene el libro especial en su inventario
     * y lo convierte a Soul Book inmediatamente
     * Esto captura cuando el libro va directo al inventario desde el lectern
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Solo en el servidor
        if (player.level().isClientSide()) {
            return;
        }

        // Revisar todo el inventario del jugador
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            // Si encontramos el libro especial del lectern
            if (SoulBookItem.isLecternSoulBook(stack)) {
                // Convertir a Soul Book
                ItemStack soulBook = SoulBookItem.convertToSoulBook(stack);

                // Reemplazar en el inventario
                player.getInventory().setItem(i, soulBook);

                // Mensaje al jugador
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§6You have obtained the Ancient Soul Book!")
                        .withStyle(style -> style.withBold(true)),
                    true
                );

                StatsSystem.LOGGER.info("Player {} obtained Soul Book from lectern",
                    player.getName().getString());

                // No seguir revisando, ya encontramos y convertimos el libro
                break;
            }
        }
    }
}
