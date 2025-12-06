package com.maurimdev.statssystem.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;

/**
 * El Soul Book - Libro místico que permite acceder al sistema de estadísticas
 */
public class SoulBookItem extends Item {

    // ============================================
    // PARTE 1: CONSTRUCTOR
    // ============================================

    public SoulBookItem(Properties properties) {
        super(properties);
    }

    // ============================================
    // PARTE 2: CLICK DERECHO
    // ============================================

    /**
     * Se ejecuta cuando el jugador hace click derecho con el item
     *
     * @param level El mundo
     * @param player El jugador que usó el item
     * @param hand La mano con la que se usó (MAIN_HAND o OFF_HAND)
     * @return Resultado de la interacción
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Solo ejecuta del lado del servidor
        if (!level.isClientSide) {
            player.displayClientMessage(Component.literal("¡Has usado el ítem!"), true);
            // Aquí puedes hacer lo que quieras: dar efectos, spawnear entidades, abrir GUI, etc.
        }

        return InteractionResultHolder.success(stack);
    }
}