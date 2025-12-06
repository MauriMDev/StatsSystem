package com.maurimdev.statssystem.client.ui.overlay.renderer;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.client.ui.overlay.HUDConstants;
import com.maurimdev.statssystem.client.ui.overlay.calculator.BarRenderingUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Renderizador de la barra de armadura con diseño horizontal en la parte superior.
 *
 * Extiende el sistema vanilla para soportar valores de armadura superiores a 20 puntos,
 * mostrando todos los iconos en una sola fila horizontal en la parte superior de la pantalla.
 *
 * La barra se posiciona inmediatamente después de la barra de vida.
 */
@EventBusSubscriber(modid = StatsSystem.MOD_ID, value = Dist.CLIENT)
public class VanillaArmorBarRenderer {

    // Sprites de armadura (vanilla)
    private static final ResourceLocation ARMOR_EMPTY_SPRITE =
            ResourceLocation.withDefaultNamespace("hud/armor_empty");
    private static final ResourceLocation ARMOR_HALF_SPRITE =
            ResourceLocation.withDefaultNamespace("hud/armor_half");
    private static final ResourceLocation ARMOR_FULL_SPRITE =
            ResourceLocation.withDefaultNamespace("hud/armor_full");

    /**
     * Intercepta el evento de renderizado de la barra de armadura vanilla
     * y la reemplaza con nuestra versión horizontal superior.
     */
    @SubscribeEvent
    public static void onArmorBarRender(RenderGuiLayerEvent.Pre event) {
        if (!event.getName().equals(VanillaGuiLayers.ARMOR_LEVEL)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        // No mostrar en modo creativo o espectador
        if (player == null || player.getAbilities().instabuild) {
            return;
        }

        // Cancelar renderizado vanilla
        event.setCanceled(true);

        // Obtener el valor REAL de armadura (con decimales)
        double armorValueDouble = player.getAttributeValue(Attributes.ARMOR);
        int armorValue = Mth.ceil(armorValueDouble);

        // Renderizar nuestra versión solo si hay armadura
        if (armorValue > 0) {
            renderHorizontalArmorBar(event.getGuiGraphics(), player, armorValue, armorValueDouble);
        }
    }

    /**
     * Renderiza la barra de armadura horizontalmente en la parte superior.
     *
     * La barra se posiciona en la tercera fila, debajo de vida y comida.
     * Solo se muestra cuando el jugador tiene armadura equipada.
     *
     * @param graphics        Contexto de renderizado
     * @param player          Jugador actual
     * @param armorValue      Puntos de armadura redondeados (para cálculos)
     * @param armorValueExact Puntos de armadura exactos (con decimales)
     */
    private static void renderHorizontalArmorBar(GuiGraphics graphics, Player player, int armorValue, double armorValueExact) {
        // Calcular cantidad de pecheras a mostrar (basado en el valor exacto)
        // Convertir armadura a "medios puntos" para manejar decimales
        int armorLast = Mth.ceil(armorValueExact * 2.0); // Convertir a "medias pecheras"
        int totalArmorIcons = Mth.ceil(armorValueExact / 2.0); // Total de pecheras a mostrar

        // Posición: Tercera fila (debajo de vida y comida)
        // Vida: fila 1
        // Comida: fila 2
        // Armadura: fila 3
        int x = HUDConstants.LEFT_MARGIN;
        int y = HUDConstants.TOP_Y_OFFSET + 2 * (HUDConstants.ICON_SIZE + HUDConstants.BAR_VERTICAL_SPACING);

        // Renderizar la barra horizontal usando armorLast (en medias pecheras)
        BarRenderingUtils.renderHorizontalBar(
                graphics,
                x,
                y,
                totalArmorIcons,
                armorLast,  // Usar el valor en "medias pecheras" para mostrar correctamente
                ARMOR_EMPTY_SPRITE,
                ARMOR_HALF_SPRITE,
                ARMOR_FULL_SPRITE
        );
    }
}
