package com.maurimdev.statssystem.client.ui.overlay.renderer;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.client.ui.overlay.HUDConstants;
import com.maurimdev.statssystem.client.ui.overlay.calculator.HealthBarCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Renderizador de la barra de vida usando la lógica vanilla EXACTA.
 */
@EventBusSubscriber(modid = StatsSystem.MOD_ID, value = Dist.CLIENT)
public class VanillaHealthBarRenderer {

    private static int lastHealth = 0;
    private static int displayHealth = 0;
    private static long lastHealthTime = 0L;
    private static long healthBlinkTime = 0L;
    private static float lastActualHealth = 0.0F; // Para detectar cambios en medios corazones
    private static long damageBlinkTime = 0L; // Para tambaleo solo cuando recibes daño

    @SubscribeEvent
    public static void onHealthBarRender(RenderGuiLayerEvent.Pre event) {
        if (!event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null || player.getAbilities().instabuild) {
            return;
        }

        event.setCanceled(true);
        renderHealth(mc, event.getGuiGraphics());
    }

    private static void renderHealth(Minecraft mc, GuiGraphics guiGraphics) {
        Player player = mc.player;
        if (player == null) return;

        Gui gui = mc.gui;
        int tickCount = gui.getGuiTicks();

        // Usar la salud real del jugador (con decimales) para detectar cambios
        float actualHealth = player.getHealth();
        int health = Mth.ceil(actualHealth);
        long currentTime = System.currentTimeMillis();

        // === LÓGICA VANILLA EXACTA PARA healthBlinkTime ===
        // Parpadeo cuando recibes daño o te curas
        // Usar actualHealth para detectar cambios en medios corazones
        if (actualHealth < lastActualHealth && player.invulnerableTime > 0) {
            lastHealthTime = currentTime;
            healthBlinkTime = tickCount + 20; // Parpadea 20 ticks al recibir daño
            damageBlinkTime = tickCount + 20; // Tambalea 20 ticks al recibir daño
        } else if (actualHealth > lastActualHealth) {
            // Parpadea cuando te curas (incluyendo regeneración natural)
            lastHealthTime = currentTime;
            healthBlinkTime = tickCount + 10; // Parpadea 10 ticks al curarse
            // NO actualizar damageBlinkTime - no tambalear cuando te curas
        }

        if (currentTime - lastHealthTime > 1000L) {
            lastHealth = health;
            displayHealth = health;
            lastHealthTime = currentTime;
        }

        lastHealth = health;
        lastActualHealth = actualHealth; // Guardar para la próxima comparación

        // Actualizar displayHealth para seguir la salud actual
        displayHealth = health;

        // Para mostrar medios corazones, necesitamos trabajar con la salud real
        // healthLast representa el valor visual (puede tener medios corazones)
        int healthLast = Mth.ceil(actualHealth * 2.0F); // Convertir a "medio corazones"

        // === PARPADEO BLANCO VANILLA (solo durante daño/curación) ===
        boolean isBlinking = healthBlinkTime > (long)tickCount && (healthBlinkTime - (long)tickCount) / 3L % 2L == 1L;

        int absorb = Mth.ceil(player.getAbsorptionAmount());
        int maxHealth = Mth.ceil(player.getMaxHealth());

        // Calcular cuántos corazones totales mostrar (incluyendo vacíos por Vitalidad)
        int totalHeartsToDisplay = HealthBarCalculator.calculateDisplayHearts(player);

        // Debug log cada 60 ticks (3 segundos)
        if (tickCount % 60 == 0) {
            StatsSystem.LOGGER.error("RENDERER - Total Hearts: {}, Current Health: {}, Max Health: {}, Absorb: {}",
                totalHeartsToDisplay, health, maxHealth, absorb);
        }

        int x = HUDConstants.LEFT_MARGIN;
        int y = HUDConstants.TOP_Y_OFFSET;

        RandomSource random = RandomSource.create((long)(tickCount * 312871));

        Gui.HeartType heartType = getHeartType(player);
        boolean hardcore = false;

        int absorption = Mth.ceil((float)absorb / 2.0F);
        int healthTotal = Mth.ceil((float)maxHealth / 2.0F);

        // === ÍNDICE DEL CORAZÓN QUE SALTA (vanilla) ===
        // Solo cuando tienes efecto de Regeneración
        int regeneratingHeartIndex = -1;
        if (player.hasEffect(MobEffects.REGENERATION)) {
            regeneratingHeartIndex = tickCount % Mth.ceil(maxHealth + 5.0F);
        }

        RenderSystem.enableBlend();

        // Iterar sobre TODOS los corazones posibles (incluyendo vacíos por Vitalidad)
        // Mostrar todos en una sola fila horizontal continua
        for (int heart = totalHeartsToDisplay - 1; heart >= 0; --heart) {
            int heartX = x + heart * 8;  // Cada corazón 8px a la derecha del anterior
            int heartY = y;

            // Tambaleo vanilla: cuando tienes poca vida O cuando recibes daño (NO cuando te curas)
            boolean shouldShake = (health + absorb <= 4) || (damageBlinkTime > tickCount);
            if (shouldShake) {
                heartY += random.nextInt(2);
            }

            // Corazón salta hacia arriba cuando está regenerando (SOLO con efecto de Regeneración)
            if (heart < healthTotal && heart == regeneratingHeartIndex) {
                heartY -= 2;
            }

            // Renderizar contenedor
            renderHeart(guiGraphics, Gui.HeartType.CONTAINER, heartX, heartY, hardcore, isBlinking, false);

            // Renderizar absorción
            if (isBlinking && heart < absorption) {
                renderHeart(guiGraphics, heartType == Gui.HeartType.WITHERED ? heartType : Gui.HeartType.ABSORBING, heartX, heartY, hardcore, true, heart >= healthTotal);
            } else if (heart < absorption) {
                renderHeart(guiGraphics, heartType == Gui.HeartType.WITHERED ? heartType : Gui.HeartType.ABSORBING, heartX, heartY, hardcore, false, heart >= healthTotal);
            }

            int i2 = heart * 2;
            boolean halfHeart = i2 + 1 == healthLast;
            boolean fullHeart = i2 + 1 < healthLast;

            // === RENDERIZADO VANILLA EXACTO ===
            // Parpadea SOLO cuando isBlinking está activo (daño/curación)
            if (isBlinking && i2 < healthLast) {
                renderHeart(guiGraphics, heartType, heartX, heartY, hardcore, true, halfHeart);
            }

            // Renderizado normal (sin parpadeo)
            // Mostrar corazón si el índice está dentro de la salud actual
            if (i2 < healthLast) {
                renderHeart(guiGraphics, heartType, heartX, heartY, hardcore, false, halfHeart);
            }
        }

        RenderSystem.disableBlend();
    }

    private static void renderHeart(GuiGraphics graphics, Gui.HeartType type, int x, int y, boolean hardcore, boolean blinking, boolean half) {
        ResourceLocation sprite = type.getSprite(hardcore, half, blinking);
        graphics.blitSprite(sprite, x, y, 9, 9);
    }

    private static Gui.HeartType getHeartType(Player player) {
        if (player.hasEffect(MobEffects.WITHER)) {
            return Gui.HeartType.WITHERED;
        } else if (player.isFullyFrozen()) {
            return Gui.HeartType.FROZEN;
        } else {
            return Gui.HeartType.NORMAL;
        }
    }
}
