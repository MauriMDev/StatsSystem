package com.maurimdev.statssystem.client.ui.overlay;

import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.core.domain.stats.StatType;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import com.maurimdev.statssystem.init.ModPerks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.registries.Registries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * HUD permanente que muestra las estadísticas del jugador en la esquina inferior derecha
 * DESACTIVADO: Este HUD ha sido reemplazado por DerivedAttributesHUD
 */
// @EventBusSubscriber(value = Dist.CLIENT)
public class StatsHUD {

    private static final int PADDING = 6;
    private static final int LINE_HEIGHT = 10;
    private static final int PANEL_WIDTH = 180;

    // Colores
    private static final int COLOR_BACKGROUND = 0xC0000000; // Negro semi-transparente
    private static final int COLOR_BORDER = 0xFF404040;     // Gris oscuro
    private static final int COLOR_HEADER = 0xFFFFD700;     // Dorado
    private static final int COLOR_TEXT = 0xFFFFFFFF;       // Blanco
    private static final int COLOR_STAT_NAME = 0xFFAAAAAA;  // Gris claro
    private static final int COLOR_VALUE = 0xFF00FF00;      // Verde

    // @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.options.hideGui) {
            return;
        }

        renderStatsPanel(event.getGuiGraphics(), mc.player);
    }

    private static void renderStatsPanel(GuiGraphics graphics, Player player) {
        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

        // Posición: esquina inferior derecha
        int screenHeight = graphics.guiHeight();
        int screenWidth = graphics.guiWidth();
        int x = screenWidth - PANEL_WIDTH - 5; // Alineado a la derecha
        int y = screenHeight - 120; // Ajustar según altura del panel

        // Calcular estadísticas derivadas
        float totalDamage = calculateTotalDamage(player, stats);
        int baseArmor = (int) player.getArmorValue();
        int ironSkinArmor = calculateIronSkinArmorBonus(stats);
        float protectionReduction = calculateProtectionReduction(player);
        float critChance = calculateCritChance(stats);

        // Dibujar fondo del panel
        int panelHeight = 110;
        graphics.fill(x, y, x + PANEL_WIDTH, y + panelHeight, COLOR_BACKGROUND);

        // Borde
        drawBorder(graphics, x, y, PANEL_WIDTH, panelHeight);

        // Renderizar contenido
        int currentY = y + PADDING;

        // Header
        graphics.drawString(
            Minecraft.getInstance().font,
            "§6§lESTADÍSTICAS",
            x + PADDING,
            currentY,
            COLOR_HEADER,
            false
        );
        currentY += LINE_HEIGHT + 3;

        // Línea divisoria
        graphics.fill(x + PADDING, currentY, x + PANEL_WIDTH - PADDING, currentY + 1, COLOR_BORDER);
        currentY += 4;

        // Estadísticas de combate
        currentY = drawStat(graphics, x, currentY, "⚔ Daño", String.format("%.1f", totalDamage));

        // Mostrar armadura: base + Iron Skin
        if (ironSkinArmor > 0) {
            currentY = drawStat(graphics, x, currentY, "🛡 Armadura",
                String.format("%d + %d", baseArmor, ironSkinArmor));
        } else {
            currentY = drawStat(graphics, x, currentY, "🛡 Armadura", String.valueOf(baseArmor));
        }

        // Mostrar reducción de Protection si existe
        if (protectionReduction > 0) {
            currentY = drawStat(graphics, x, currentY, "🛡 Protection", String.format("%.0f%% reducción", protectionReduction));
        }

        if (critChance > 0) {
            currentY = drawStat(graphics, x, currentY, "✨ Crítico", String.format("%.0f%% (150%% dmg)", critChance));
        }

        currentY += 3;

        // Stats base
        int col1X = x + PADDING;
        int col2X = x + PANEL_WIDTH / 2 + 5;

        currentY = drawStatCompact(graphics, col1X, currentY, "💪 STR", stats.getStrength());
        currentY = drawStatCompact(graphics, col1X, currentY, "❤ VIT", stats.getVitality());
        currentY = drawStatCompact(graphics, col1X, currentY, "⛏ MIN", stats.getMining());

        int tempY = currentY - (3 * LINE_HEIGHT);
        tempY = drawStatCompact(graphics, col2X, tempY, "🎯 DEX", stats.getDexterity());
        tempY = drawStatCompact(graphics, col2X, tempY, "👟 AGI", stats.getAgility());
        tempY = drawStatCompact(graphics, col2X, tempY, "🌾 FAR", stats.getFarming());
    }

    private static int drawStat(GuiGraphics graphics, int x, int y, String label, String value) {
        graphics.drawString(
            Minecraft.getInstance().font,
            label + ": §a" + value,
            x + PADDING,
            y,
            COLOR_TEXT,
            false
        );
        return y + LINE_HEIGHT;
    }

    private static int drawStatCompact(GuiGraphics graphics, int x, int y, String label, int value) {
        graphics.drawString(
            Minecraft.getInstance().font,
            label + ": §a" + value,
            x,
            y,
            COLOR_TEXT,
            false
        );
        return y + LINE_HEIGHT;
    }

    private static void drawBorder(GuiGraphics graphics, int x, int y, int width, int height) {
        // Top
        graphics.fill(x, y, x + width, y + 1, COLOR_BORDER);
        // Bottom
        graphics.fill(x, y + height - 1, x + width, y + height, COLOR_BORDER);
        // Left
        graphics.fill(x, y, x + 1, y + height, COLOR_BORDER);
        // Right
        graphics.fill(x + width - 1, y, x + width, y + height, COLOR_BORDER);
    }

    /**
     * Calcula el daño total: item en mano + bonus de Strength + perks + encantamientos
     */
    private static float calculateTotalDamage(Player player, PlayerStats stats) {
        // Obtener el daño de ataque total del jugador (incluye arma + base)
        double baseDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);

        ItemStack heldItem = player.getMainHandItem();
        double totalDamage = baseDamage;

        if (!heldItem.isEmpty()) {
            // Agregar daño de Sharpness/Filo: +0.5 + (0.5 * nivel)
            int sharpnessLevel = heldItem.getEnchantmentLevel(
                player.level().registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT)
                    .getHolderOrThrow(Enchantments.SHARPNESS)
            );
            if (sharpnessLevel > 0) {
                totalDamage += 0.5 + (0.5 * sharpnessLevel);
            }
        }

        // Bonus de Strength: +0.5 por nivel
        double strengthBonus = stats.getStrength() * 0.5;
        totalDamage += strengthBonus;

        // Bonus de Power Strike perk: +10% por nivel (multiplicativo)
        if (stats.isPerkActive(ModPerks.POWER_STRIKE.getId())) {
            int perkLevel = stats.getPerkLevel(ModPerks.POWER_STRIKE.getId());
            double powerStrikeMultiplier = 1.0 + (perkLevel * 0.10);
            totalDamage *= powerStrikeMultiplier;
        }

        return (float) totalDamage;
    }

    /**
     * Calcula los puntos de armadura equivalentes de Iron Skin
     * Iron Skin da 5% reducción por nivel
     * Aproximadamente 1 punto de armadura = 4% reducción
     * Por lo tanto: puntos = (reducción% / 4)
     */
    private static int calculateIronSkinArmorBonus(PlayerStats stats) {
        if (!stats.isPerkActive(ModPerks.IRON_SKIN.getId())) {
            return 0;
        }

        int perkLevel = stats.getPerkLevel(ModPerks.IRON_SKIN.getId());
        float reductionPercent = perkLevel * 5.0f; // 5% por nivel

        // Convertir reducción a puntos de armadura equivalentes
        // 1 punto de armadura ≈ 4% reducción
        int armorPoints = Math.round(reductionPercent / 4.0f);

        return armorPoints;
    }

    /**
     * Calcula la reducción de daño desde Protection encantamiento
     * Protection da 4% de reducción por nivel en cada pieza
     */
    private static float calculateProtectionReduction(Player player) {
        int totalProtectionLevel = 0;

        // Sumar niveles de Protection de todas las piezas de armadura
        for (ItemStack armor : player.getArmorSlots()) {
            if (!armor.isEmpty()) {
                int protectionLevel = armor.getEnchantmentLevel(
                    player.level().registryAccess()
                        .registryOrThrow(Registries.ENCHANTMENT)
                        .getHolderOrThrow(Enchantments.PROTECTION)
                );
                totalProtectionLevel += protectionLevel;
            }
        }

        // Cada nivel de Protection da 4% de reducción
        return totalProtectionLevel * 4.0f;
    }

    /**
     * Calcula la probabilidad de crítico desde Critical Strike
     */
    private static float calculateCritChance(PlayerStats stats) {
        if (!stats.isPerkActive(ModPerks.CRITICAL_STRIKE.getId())) {
            return 0.0f;
        }

        int perkLevel = stats.getPerkLevel(ModPerks.CRITICAL_STRIKE.getId());
        return perkLevel * 10.0f; // 10% por nivel
    }
}
