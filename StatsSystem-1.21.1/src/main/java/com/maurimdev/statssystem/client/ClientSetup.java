package com.maurimdev.statssystem.client;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.client.keybinding.ModKeyBindings;
import com.maurimdev.statssystem.client.ui.overlay.DerivedAttributesHUD;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Configuración del lado del cliente
 */
@EventBusSubscriber(modid = StatsSystem.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    /**
     * Registra las teclas personalizadas
     */
    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(ModKeyBindings.OPEN_STATS_MENU);
        StatsSystem.LOGGER.info("KeyBindings registrados correctamente en el cliente.");
    }

    /**
     * Registra las capas de GUI (HUD overlays)
     */
    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        // Registrar el HUD de atributos derivados
        // Se posiciona después de la barra de experiencia para no interferir con elementos vanilla
        event.registerAbove(
            VanillaGuiLayers.EXPERIENCE_BAR,
            ResourceLocation.fromNamespaceAndPath(StatsSystem.MOD_ID, "derived_attributes"),
            new DerivedAttributesHUD()
        );

        StatsSystem.LOGGER.info("HUD de atributos derivados registrado correctamente.");
    }
}
