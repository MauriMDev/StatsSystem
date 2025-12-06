package com.maurimdev.statssystem.client.ui.overlay.renderer;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.client.ui.overlay.HUDConstants;
import com.maurimdev.statssystem.client.ui.overlay.calculator.FoodBarCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Renderizador de la barra de comida con diseño horizontal en la parte superior.
 *
 * Extiende el sistema vanilla para soportar hasta 60 puntos de comida (30 muslitos),
 * mostrando todos los iconos en una sola fila horizontal en la parte superior de la pantalla.
 *
 * Características:
 * - Soporte para medios muslitos (valores impares de comida)
 * - Escalado dinámico según nivel de Agilidad
 * - Efectos visuales de hambre (efecto Hunger)
 * - Diseño horizontal sin límite de iconos
 */
@EventBusSubscriber(modid = StatsSystem.MOD_ID, value = Dist.CLIENT)
public class VanillaFoodBarRenderer {

    // Sprites de comida normales
    private static final ResourceLocation FOOD_EMPTY_SPRITE =
            ResourceLocation.withDefaultNamespace("hud/food_empty");
    private static final ResourceLocation FOOD_HALF_SPRITE =
            ResourceLocation.withDefaultNamespace("hud/food_half");
    private static final ResourceLocation FOOD_FULL_SPRITE =
            ResourceLocation.withDefaultNamespace("hud/food_full");

    // Sprites de comida con efecto de hambre
    private static final ResourceLocation FOOD_EMPTY_HUNGER_SPRITE =
            ResourceLocation.withDefaultNamespace("hud/food_empty_hunger");
    private static final ResourceLocation FOOD_HALF_HUNGER_SPRITE =
            ResourceLocation.withDefaultNamespace("hud/food_half_hunger");
    private static final ResourceLocation FOOD_FULL_HUNGER_SPRITE =
            ResourceLocation.withDefaultNamespace("hud/food_full_hunger");

    /**
     * Intercepta el evento de renderizado de la barra de comida vanilla
     * y la reemplaza con nuestra versión horizontal superior.
     */
    @SubscribeEvent
    public static void onFoodBarRender(RenderGuiLayerEvent.Pre event) {
        if (!event.getName().equals(VanillaGuiLayers.FOOD_LEVEL)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        // No mostrar en modo creativo, espectador, o montado en vehículo
        if (player == null || player.getAbilities().instabuild || player.isPassenger()) {
            return;
        }

        // Cancelar renderizado vanilla
        event.setCanceled(true);

        // Renderizar nuestra versión solo si hay comida
        FoodData foodData = player.getFoodData();
        int foodLevel = foodData.getFoodLevel();

        if (foodLevel > 0 || foodData.getSaturationLevel() > 0) {
            renderHorizontalFoodBar(event.getGuiGraphics(), player, foodData);
        }
    }

    /**
     * Renderiza la barra de comida horizontalmente en la parte superior.
     *
     * La barra se posiciona en la tercera fila, debajo de vida y armadura.
     * Incluye animación de tambaleo cuando la saturación es 0 (vanilla).
     *
     * @param graphics Contexto de renderizado
     * @param player   Jugador actual
     * @param foodData Datos de comida del jugador
     */
    private static void renderHorizontalFoodBar(GuiGraphics graphics, Player player, FoodData foodData) {
        Minecraft mc = Minecraft.getInstance();
        Gui gui = mc.gui;
        int tickCount = gui.getGuiTicks();

        // Obtener nivel actual y calcular cantidad de muslitos a mostrar
        int foodLevel = foodData.getFoodLevel();
        int totalIcons = FoodBarCalculator.calculateDisplayIcons(player, foodLevel);
        float saturation = foodData.getSaturationLevel();

        // Posición base: Segunda fila (debajo de vida)
        int baseX = HUDConstants.LEFT_MARGIN;
        int baseY = HUDConstants.TOP_Y_OFFSET + (HUDConstants.ICON_SIZE + HUDConstants.BAR_VERTICAL_SPACING);

        // Seleccionar sprites según efecto de hambre
        boolean hasHunger = player.hasEffect(MobEffects.HUNGER);
        ResourceLocation emptySprite = hasHunger ? FOOD_EMPTY_HUNGER_SPRITE : FOOD_EMPTY_SPRITE;
        ResourceLocation halfSprite = hasHunger ? FOOD_HALF_HUNGER_SPRITE : FOOD_HALF_SPRITE;
        ResourceLocation fullSprite = hasHunger ? FOOD_FULL_HUNGER_SPRITE : FOOD_FULL_SPRITE;

        // Random para tambaleo (mismo seed que vanilla)
        RandomSource random = RandomSource.create((long)(tickCount * 312871));

        RenderSystem.enableBlend();

        // Renderizar cada muslito con posible tambaleo
        for (int i = 0; i < totalIcons; i++) {
            int iconX = baseX + (i * HUDConstants.ICON_SPACING);
            int iconY = baseY;

            // === ANIMACIÓN VANILLA: Tambaleo cuando saturación = 0 ===
            // Los muslitos tiemblan cuando no tienes saturación
            if (saturation <= 0.0F && tickCount % (foodLevel * 3 + 1) == 0) {
                iconY += (random.nextInt(3) - 1); // Mueve -1, 0, o +1 píxel
            }

            // Renderizar contenedor vacío
            graphics.blitSprite(emptySprite, iconX, iconY, HUDConstants.ICON_SIZE, HUDConstants.ICON_SIZE);

            // Determinar sprite de comida según el valor actual
            int valueForThisIcon = i * 2;
            ResourceLocation foodSprite = null;

            if (foodLevel > valueForThisIcon + 1) {
                // Muslito lleno
                foodSprite = fullSprite;
            } else if (foodLevel == valueForThisIcon + 1) {
                // Medio muslito
                foodSprite = halfSprite;
            }

            // Renderizar comida encima si corresponde
            if (foodSprite != null) {
                graphics.blitSprite(foodSprite, iconX, iconY, HUDConstants.ICON_SIZE, HUDConstants.ICON_SIZE);
            }
        }

        RenderSystem.disableBlend();
    }
}
