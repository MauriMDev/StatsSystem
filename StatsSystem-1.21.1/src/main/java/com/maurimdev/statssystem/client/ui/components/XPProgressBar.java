package com.maurimdev.statssystem.client.ui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Componente reutilizable para renderizar barras de progreso estilo experiencia de Minecraft.
 * Incluye efectos de gradiente, animaciones de flash y soporte para soft cap.
 */
public class XPProgressBar {

    // ============================================
    // CONSTANTES DE COLOR
    // ============================================

    private static final int COLOR_PROGRESS_BG = 0xFF000000;           // Negro completo
    private static final int COLOR_PROGRESS_FILL = 0xFF7CB342;         // Verde tipo Minecraft XP
    private static final int COLOR_PROGRESS_FILL_DARK = 0xFF558B2F;    // Verde oscuro para gradiente
    private static final int COLOR_PROGRESS_SOFTCAP = 0xFFFFB74D;      // Naranja dorado
    private static final int COLOR_PROGRESS_SOFTCAP_DARK = 0xFFE65100; // Naranja oscuro para gradiente
    private static final int COLOR_BORDER = 0xFF222222;                // Borde sutil
    private static final int COLOR_TEXT_LIGHT = 0xFFFFFFFF;            // Blanco para texto
    private static final int COLOR_TEXT_SHADOW = 0xFF000000;           // Negro para sombra

    // ============================================
    // RENDERIZADO
    // ============================================

    /**
     * Renderiza una barra de progreso estilo experiencia de Minecraft
     *
     * @param graphics GuiGraphics para renderizar
     * @param x Posición X
     * @param y Posición Y
     * @param width Ancho de la barra
     * @param height Alto de la barra
     * @param progressPercentage Porcentaje de progreso (0-100)
     * @param isSoftCapped Si ha alcanzado el soft cap (cambia color a naranja)
     * @param animation Valor de animación para el flash (0-1)
     */
    public static void render(GuiGraphics graphics, int x, int y, int width, int height,
                              float progressPercentage, boolean isSoftCapped, float animation) {
        // Fondo oscuro
        graphics.fill(x, y, x + width, y + height, COLOR_PROGRESS_BG);

        // Calcular ancho del relleno
        int fillWidth = (int) ((progressPercentage / 100.0f) * width);

        if (fillWidth > 0) {
            // Color basado en soft cap
            int fillColor = isSoftCapped ? COLOR_PROGRESS_SOFTCAP : COLOR_PROGRESS_FILL;
            int fillColorDark = isSoftCapped ? COLOR_PROGRESS_SOFTCAP_DARK : COLOR_PROGRESS_FILL_DARK;

            // Efecto de gradiente vertical (tipo Minecraft)
            // Parte superior más oscura
            graphics.fill(x, y, x + fillWidth, y + height / 2, fillColorDark);
            // Parte inferior más clara
            graphics.fill(x, y + height / 2, x + fillWidth, y + height, fillColor);

            // Animación de flash al subir nivel
            if (animation > 0) {
                int flashAlpha = (int) (animation * 180); // 70% max opacity
                int flashColor = (flashAlpha << 24) | 0x00FFFFFF;
                graphics.fill(x, y, x + fillWidth, y + height, flashColor);
            }

            // Pequeño brillo en el borde derecho del progreso
            if (fillWidth < width && fillWidth > 2) {
                graphics.fill(x + fillWidth - 1, y, x + fillWidth, y + height, 0x80FFFFFF);
            }
        }

        // Borde sutil
        drawBorder(graphics, x, y, width, height, COLOR_BORDER);
    }

    /**
     * Renderiza una barra de progreso con texto centrado dentro
     *
     * @param graphics GuiGraphics para renderizar
     * @param font Font para el texto
     * @param x Posición X
     * @param y Posición Y
     * @param width Ancho de la barra
     * @param height Alto de la barra
     * @param progressPercentage Porcentaje de progreso (0-100)
     * @param isSoftCapped Si ha alcanzado el soft cap (cambia color a naranja)
     * @param animation Valor de animación para el flash (0-1)
     * @param text Texto a mostrar dentro de la barra (null para no mostrar texto)
     */
    public static void renderWithText(GuiGraphics graphics, Font font, int x, int y, int width, int height,
                                      float progressPercentage, boolean isSoftCapped, float animation, String text) {
        // Renderizar la barra normal primero
        render(graphics, x, y, width, height, progressPercentage, isSoftCapped, animation);

        // Si hay texto, renderizarlo centrado
        if (text != null && !text.isEmpty()) {
            int textWidth = font.width(text);
            int textX = x + (width / 2) - (textWidth / 2);
            int textY = y + (height / 2) - 4; // Centrado verticalmente (altura de fuente ~8px)

            // Sombra para mejor legibilidad
            graphics.drawString(font, text, textX + 1, textY + 1, COLOR_TEXT_SHADOW, false);
            // Texto principal en blanco
            graphics.drawString(font, text, textX, textY, COLOR_TEXT_LIGHT, false);
        }
    }

    /**
     * Versión simplificada sin animación
     */
    public static void render(GuiGraphics graphics, int x, int y, int width, int height,
                              float progressPercentage, boolean isSoftCapped) {
        render(graphics, x, y, width, height, progressPercentage, isSoftCapped, 0f);
    }

    /**
     * Versión más simplificada sin soft cap ni animación
     */
    public static void render(GuiGraphics graphics, int x, int y, int width, int height,
                              float progressPercentage) {
        render(graphics, x, y, width, height, progressPercentage, false, 0f);
    }

    // ============================================
    // UTILIDADES
    // ============================================

    /**
     * Dibuja un borde alrededor de un rectángulo
     */
    private static void drawBorder(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x - 1, y - 1, x + width + 1, y, color);                  // Top
        graphics.fill(x - 1, y + height, x + width + 1, y + height + 1, color); // Bottom
        graphics.fill(x - 1, y, x, y + height, color);                         // Left
        graphics.fill(x + width, y, x + width + 1, y + height, color);         // Right
    }
}
