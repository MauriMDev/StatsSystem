package com.maurimdev.statssystem.client.ui.components;

import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.core.domain.stats.StatType;
import com.maurimdev.statssystem.core.service.StatsProgressionService;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Componente para renderizar una tarjeta individual de estadística.
 * Incluye el nombre, nivel, progreso y barra de XP.
 */
public class StatCard {

    // ============================================
    // CONSTANTES DE COLOR
    // ============================================

    private static final int COLOR_TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int COLOR_TEXT_SECONDARY = 0xFF999999;
    private static final int COLOR_STAT_HOVER = 0x30FFD700;
    private static final int COLOR_BORDER_HIGHLIGHT = 0xFFFFD700;

    // ============================================
    // RENDERIZADO
    // ============================================

    /**
     * Renderiza una tarjeta de estadística completa
     *
     * @param graphics GuiGraphics para renderizar
     * @param font Fuente para textos
     * @param stats Estadísticas del jugador
     * @param statType Tipo de estadística a renderizar
     * @param x Posición X
     * @param y Posición Y
     * @param width Ancho de la tarjeta
     * @param height Alto de la tarjeta
     * @param isHovered Si está en hover
     * @param isSelected Si está seleccionada
     * @param animation Valor de animación (0-1)
     * @param isTiny Si es pantalla tiny
     * @param isSmall Si es pantalla small
     */
    public static void render(GuiGraphics graphics, Font font, PlayerStats stats, StatType statType,
                              int x, int y, int width, int height,
                              boolean isHovered, boolean isSelected, float animation,
                              boolean isTiny, boolean isSmall) {

        // Fondo base sutil para dar profundidad (más visible en small screens)
        if (!isTiny) {
            int baseBg = isSmall ? 0x15202020 : 0x10101010;
            graphics.fill(x, y, x + width, y + height, baseBg);
        }

        // Fondo hover/selected
        if (isHovered || isSelected) {
            int bgColor = isSelected ? COLOR_STAT_HOVER : (COLOR_STAT_HOVER & 0x20FFFFFF);
            graphics.fill(x, y, x + width, y + height, bgColor);
        }

        // Borde si está seleccionado
        if (isSelected) {
            drawBorder(graphics, x, y, width, height, COLOR_BORDER_HIGHLIGHT);
        } else if (isHovered && isSmall) {
            // Borde sutil en hover para small screens (más feedback visual)
            drawBorder(graphics, x, y, width, height, 0xFF886622);
        }

        int level = stats.getLevel(statType);
        int maxLevel = 64;
        int softCap = statType.getSoftCap();

        // Espaciado adaptativo (aumentado para tiny para mejor legibilidad)
        int padding = isTiny ? 5 : (isSmall ? 4 : 5);

        String symbol = getStatSymbol(statType);
        String name = getStatName(statType);
        String color = getStatColor(statType);

        // Calcular progreso (usado en todos los casos)
        float progressPercentage = 0;
        int currentProgress = 0;
        int requiredProgress = 0;

        if (level < maxLevel) {
            currentProgress = (int) stats.getXP(statType);
            requiredProgress = StatsProgressionService.calculatePracticeXPNeeded(level);
            if (requiredProgress > 0) {
                progressPercentage = Math.min(100, (currentProgress / (float) requiredProgress) * 100);
            }
        }

        if (isTiny) {
            // PANTALLAS TINY: Layout limpio de 2 líneas
            // Línea 1: Símbolo + Nombre (izquierda) | Nivel (derecha)
            // Línea 2: Barra de progreso con texto dentro

            int lineHeight = 11; // Altura del texto

            // PRIMERA LÍNEA: Símbolo + Nombre a la izquierda, Nivel a la derecha
            String nameWithSymbol = symbol + " " + color + name;
            graphics.drawString(font, nameWithSymbol, x + padding, y + padding, COLOR_TEXT_PRIMARY);

            // Nivel ultra compacto: solo el número
            String levelText = String.valueOf(level);
            String displayLevel = level >= softCap ? "§6" + levelText : "§7" + levelText;
            int levelWidth = font.width(levelText);
            int levelX = x + width - levelWidth - padding;
            graphics.drawString(font, displayLevel, levelX, y + padding, COLOR_TEXT_SECONDARY);

            // BARRA DE PROGRESO: Más pequeña y pegada al nombre
            int barHeight = 5; // 2/3 de 8px = 5px (más delgada)
            int barMargin = 2; // Margen mínimo entre nombre y barra
            int barY = y + padding + lineHeight + barMargin;
            int barWidth = width - (padding * 2);
            int barX = x + padding;

            // Renderizar barra SIN texto (más simple)
            XPProgressBar.renderWithText(graphics, font, barX, barY, barWidth, barHeight,
                progressPercentage, level >= softCap, animation, null);

        } else {
            // PANTALLAS NORMALES Y PEQUEÑAS: Layout completo con toda la información
            int lineHeight = isSmall ? 13 : 15;

            // PRIMERA LÍNEA: Símbolo + Nombre a la izquierda, Nivel a la derecha
            String nameWithSymbol = symbol + " " + color + name;

            // Formato de nivel completo para ambos (small y normal)
            String levelText = level + "/" + maxLevel;
            String displayLevel = level >= softCap ? "§6" + levelText : "§7" + levelText;
            int levelWidth = font.width(levelText);
            int levelX = x + width - levelWidth - padding;

            graphics.drawString(font, nameWithSymbol, x + padding, y + padding, COLOR_TEXT_PRIMARY);
            graphics.drawString(font, displayLevel, levelX, y + padding, COLOR_TEXT_SECONDARY);

            // Barra de progreso justo debajo del nombre (con pequeño margen)
            int barHeight = isSmall ? 14 : 15;
            int barMargin = isSmall ? 4 : 5; // Pequeño margen entre nombre y barra
            int barY = y + padding + lineHeight + barMargin;
            int barWidth = width - (padding * 2);
            int barX = x + padding;

            // Texto para mostrar dentro de la barra
            String barText = null;
            if (level < maxLevel) {
                barText = currentProgress + "/" + requiredProgress;
            } else {
                barText = "MAX!";
            }

            XPProgressBar.renderWithText(graphics, font, barX, barY, barWidth, barHeight,
                progressPercentage, level >= softCap, animation, barText);

            // Línea decorativa inferior en small screens para dar más definición
            if (isSmall) {
                int lineY = y + height - 1;
                graphics.fill(x + 2, lineY, x + width - 2, lineY + 1, 0x40664422);
            }
        }
    }

    // ============================================
    // UTILIDADES - STAT SYMBOLS Y NAMES
    // ============================================

    public static String getStatSymbol(StatType statType) {
        return switch (statType) {
            case VITALITY -> "§c❤";
            case STRENGTH -> "§6💪";
            case DEXTERITY -> "§b🎯";
            case MINING -> "§7⛏";
            case AGILITY -> "§a👟";
            case FARMING -> "§2🌾";
        };
    }

    public static String getStatName(StatType statType) {
        return switch (statType) {
            case VITALITY -> "Vitalidad";
            case STRENGTH -> "Fuerza";
            case DEXTERITY -> "Destreza";
            case MINING -> "Minería";
            case AGILITY -> "Agilidad";
            case FARMING -> "Agricultura";
        };
    }

    /**
     * Obtiene el código de color descriptivo para cada stat (igual que en PerkCategory)
     */
    public static String getStatColor(StatType statType) {
        return switch (statType) {
            case VITALITY -> "§c";    // Rojo
            case STRENGTH -> "§6";    // Dorado/Naranja
            case DEXTERITY -> "§b";   // Aqua/Cian
            case MINING -> "§7";      // Gris
            case AGILITY -> "§a";     // Verde
            case FARMING -> "§2";     // Verde oscuro
        };
    }

    // ============================================
    // UTILIDADES - RENDERIZADO
    // ============================================

    /**
     * Dibuja solo bordes superior e inferior (sin laterales)
     * Los bordes están exactamente en los límites del card
     */
    private static void drawBorder(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        // Borde superior: justo en el borde superior del card
        graphics.fill(x, y, x + width, y + 1, color);
        // Borde inferior: justo en el borde inferior del card
        graphics.fill(x, y + height - 1, x + width, y + height, color);
    }
}
