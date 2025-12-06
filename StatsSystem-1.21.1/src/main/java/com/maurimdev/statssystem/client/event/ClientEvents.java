package com.maurimdev.statssystem.client.event;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.client.keybinding.ModKeyBindings;
import com.maurimdev.statssystem.client.ui.screen.TabbedStatsScreen;
import com.maurimdev.statssystem.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Eventos del lado del CLIENTE
 */
@EventBusSubscriber(modid = StatsSystem.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null) return;

        // ✅ Solo manejar R cuando NO hay pantalla abierta
        // Las pantallas manejan R internamente con keyPressed()
        if (ModKeyBindings.OPEN_STATS_MENU.consumeClick() && minecraft.screen == null) {
            // Verificar si tiene el Soul Book (en inventario o Curios)
            if (!hasSoulBook(player)) {
                player.displayClientMessage(
                        Component.translatable("message.statssystem.soul_book.required")
                                .withStyle(style -> style.withColor(0xFF5555).withBold(true)),
                        true
                );
                return;
            }
            // Abrir la pantalla apropiada
            openStatsScreen();
        }
    }

    /**
     * Abre la pantalla de stats con tabs (Stats / Perks)
     * PÚBLICO para que SoulBookItem pueda usarlo
     */
    public static void openStatsScreen() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player != null) {
            minecraft.setScreen(new TabbedStatsScreen());
        }
    }

    /**
     * Verifica si el jugador tiene el Soul Book
     * ✅ MODIFICADO: Ahora busca también en slots de Curios
     */
    private static boolean hasSoulBook(LocalPlayer player) {
        // 1. Buscar en el inventario normal
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(ModItems.SOUL_BOOK.get())) {
                return true;
            }
        }

        // 2. Buscar en los slots de Curios
        var curiosInventory = CuriosApi.getCuriosInventory(player);
        if (curiosInventory.isPresent()) {
            var handler = curiosInventory.get();
            // Buscar el Soul Book en cualquier slot de Curios
            var found = handler.findFirstCurio(stack -> stack.is(ModItems.SOUL_BOOK.get()));
            if (found.isPresent()) {
                return true;
            }
        }

        return false;
    }
}