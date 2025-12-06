package com.maurimdev.statssystem.client.ui.rendering;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Renderizador general para las pantallas del sistema.
 * Solo maneja renderizado básico de marcos, fondos y paneles.
 * NO contiene lógica específica de stats o perks.
 */
public class ScreenRenderer {

    // ============================================
    // CONSTANTES DE COLOR
    // ============================================

    // Tema oscuro y elegante
    public static final int COLOR_BACKGROUND = 0xE0000000;
    public static final int COLOR_PANEL_DARK = 0xE0080808;
    public static final int COLOR_PANEL_LIGHT = 0xE0181818;
    public static final int COLOR_BORDER = 0xFF6B5416;
    public static final int COLOR_BORDER_HIGHLIGHT = 0xFFFFD700;
    public static final int COLOR_TEXT_PRIMARY = 0xFFFFFFFF;
    public static final int COLOR_TEXT_SECONDARY = 0xFF999999;
    public static final int COLOR_TEXT_GOLD = 0xFFFFD700;

    private final Font font;
    private final SystemLayout layout;

    // ============================================
    // CONSTRUCTOR
    // ============================================

    public ScreenRenderer(Font font, SystemLayout layout) {
        this.font = font;
        this.layout = layout;
    }

    // ============================================
    // RENDERIZADO PRINCIPAL
    // ============================================

    /**
     * Renderiza el fondo completo de la pantalla
     */
    public void renderBackground(GuiGraphics graphics, int screenWidth, int screenHeight) {
        graphics.fill(0, 0, screenWidth, screenHeight, COLOR_BACKGROUND);
    }

    /**
     * Renderiza el marco principal de la GUI
     */
    public void renderMainFrame(GuiGraphics graphics) {
        int leftPos = layout.getLeftPos();
        int topPos = layout.getTopPos();
        int guiWidth = layout.getGuiWidth();
        int guiHeight = layout.getGuiHeight();

        graphics.fill(leftPos, topPos, leftPos + guiWidth, topPos + guiHeight, COLOR_PANEL_DARK);
        drawBorder(graphics, leftPos, topPos, guiWidth, guiHeight, COLOR_BORDER);
    }

    // ============================================
    // HEADER (SECCIÓN SUPERIOR)
    // ============================================

    /**
     * Renderiza la sección superior (header) - solo el fondo y borde inferior
     * NO incluye el título del mod (eso se maneja por separado)
     */
    public void renderTopSection(GuiGraphics graphics) {
        int leftPos = layout.getLeftPos();
        int topPos = layout.getTopPos();
        int guiWidth = layout.getGuiWidth();
        int topSectionBottom = layout.getTopSectionBottom();
        graphics.fill(leftPos, topPos, leftPos + guiWidth, topSectionBottom, COLOR_PANEL_LIGHT);

        // Línea única de separación con el cuerpo
        graphics.fill(leftPos, topSectionBottom, leftPos + guiWidth, topSectionBottom + 1, COLOR_BORDER);
    }

    /**
     * Renderiza el título del mod centrado para pantallas individuales
     * Sin tabs, puede usar todo el espacio del header
     */
    public void renderModTitleCentered(GuiGraphics graphics) {
        int leftPos = layout.getLeftPos();
        int topPos = layout.getTopPos();
        int guiWidth = layout.getGuiWidth();
        int centerX = leftPos + (guiWidth / 2);
        int titleY = topPos + layout.scaled(18);

        String title = "§6§lSTATS SYSTEM";
        int titleWidth = font.width("STATS SYSTEM");
        int textX = centerX - (titleWidth / 2);

        // Sombra sutil
        graphics.drawString(font, "§8§lSTATS SYSTEM", textX + 2, titleY + 2, 0x60000000, false);

        // Título principal
        graphics.drawString(font, title, textX, titleY, COLOR_TEXT_GOLD, false);

        // Línea decorativa debajo del título
        int lineWidth = titleWidth + 60;
        int lineY = titleY + 14;
        graphics.fill(centerX - lineWidth / 2, lineY, centerX + lineWidth / 2, lineY + 2, COLOR_BORDER);

        // Detalles brillantes en los extremos
        graphics.fill(centerX - lineWidth / 2 - 2, lineY - 1, centerX - lineWidth / 2 + 2, lineY + 3, COLOR_BORDER_HIGHLIGHT);
        graphics.fill(centerX + lineWidth / 2 - 2, lineY - 1, centerX + lineWidth / 2 + 2, lineY + 3, COLOR_BORDER_HIGHLIGHT);
    }

    /**
     * Renderiza el título del mod centrado para TabbedStatsScreen
     * Adaptativo según el tamaño de pantalla:
     * - Pantallas normales: Título grande centrado arriba
     * - Pantallas pequeñas: Título normal centrado arriba
     * - Pantallas tiny: NO se renderiza (se maneja desde TabbedStatsScreen)
     */
    public void renderModTitleCenteredForTabs(GuiGraphics graphics) {
        int leftPos = layout.getLeftPos();
        int topPos = layout.getTopPos();
        int guiWidth = layout.getGuiWidth();
        int centerX = leftPos + (guiWidth / 2);

        // Ajustar tamaño según pantalla
        String title;
        int titleY;
        float scale = 1.0f;

        if (layout.isSmallScreen()) {
            // Pantallas pequeñas: título normal, más arriba
            title = "§6§lSTATS SYSTEM";
            titleY = topPos + layout.scaled(8);
        } else {
            // Pantallas normales: título MÁS GRANDE
            title = "§6§lSTATS SYSTEM";
            titleY = topPos + layout.scaled(12);
            scale = 1.2f; // 20% más grande
        }

        int titleWidth = font.width("STATS SYSTEM");
        int textX = centerX - (int)(titleWidth * scale / 2);

        // Renderizar con escala si es necesario
        if (scale != 1.0f) {
            graphics.pose().pushPose();
            graphics.pose().translate(textX, titleY, 0);
            graphics.pose().scale(scale, scale, scale);

            // Sombra
            graphics.drawString(font, "§8§lSTATS SYSTEM", 2, 2, 0x60000000, false);
            // Título
            graphics.drawString(font, title, 0, 0, COLOR_TEXT_GOLD, false);

            graphics.pose().popPose();
        } else {
            // Sin escala
            // Sombra
            graphics.drawString(font, "§8§lSTATS SYSTEM", textX + 1, titleY + 1, 0x60000000, false);
            // Título
            graphics.drawString(font, title, textX, titleY, COLOR_TEXT_GOLD, false);
        }
    }

    // ============================================
    // PANEL IZQUIERDO
    // ============================================

    /**
     * Renderiza el fondo del panel izquierdo
     */
    public void renderLeftPanelBackground(GuiGraphics graphics) {
        int leftPos = layout.getLeftPos();
        int topPos = layout.getTopPos();
        int leftPanelWidth = layout.getLeftPanelWidth();
        int guiHeight = layout.getGuiHeight();
        int topSectionBottom = layout.getTopSectionBottom();

        int panelTop = topSectionBottom + 2;

        graphics.fill(leftPos, panelTop, leftPos + leftPanelWidth, topPos + guiHeight, COLOR_PANEL_LIGHT);
        graphics.fill(leftPos + leftPanelWidth, panelTop, leftPos + leftPanelWidth + 2, topPos + guiHeight, COLOR_BORDER);
    }

    /**
     * Renderiza el header del panel izquierdo con título y nivel total
     * (Usado específicamente en StatsScreen)
     */
    public void renderLeftPanelHeader(GuiGraphics graphics, String title, String rightText) {
        int leftPos = layout.getLeftPos();
        int topSectionBottom = layout.getTopSectionBottom();
        int leftPanelWidth = layout.getLeftPanelWidth();

        int panelTop = topSectionBottom + 2;
        int y = panelTop + layout.scaled(8);

        graphics.drawString(font, title,
                leftPos + layout.scaled(layout.getPadding()), y, COLOR_TEXT_GOLD);

        int rightWidth = font.width(rightText);
        graphics.drawString(font, rightText,
                leftPos + leftPanelWidth - rightWidth - layout.scaled(layout.getPadding()),
                y, COLOR_TEXT_SECONDARY);

        // Línea decorativa debajo del título (similar a PerksScreen)
        int lineY = y + 12;
        graphics.fill(leftPos + layout.scaled(10), lineY,
                leftPos + leftPanelWidth - layout.scaled(10), lineY + 1,
                COLOR_BORDER);
    }

    // ============================================
    // PANEL DERECHO
    // ============================================

    /**
     * Renderiza el fondo del panel derecho
     */
    public void renderRightPanel(GuiGraphics graphics) {
        int rightPanelLeft = layout.getRightPanelLeft();
        int topSectionBottom = layout.getTopSectionBottom();
        int leftPos = layout.getLeftPos();
        int topPos = layout.getTopPos();
        int guiWidth = layout.getGuiWidth();
        int guiHeight = layout.getGuiHeight();

        int panelTop = topSectionBottom + 2;

        graphics.fill(rightPanelLeft + 2, panelTop, leftPos + guiWidth, topPos + guiHeight, COLOR_PANEL_DARK);
    }

    // ============================================
    // UTILIDADES DE RENDERIZADO
    // ============================================

    /**
     * Dibuja un borde alrededor de un rectángulo
     */
    public void drawBorder(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x - 1, y - 1, x + width + 1, y, color);
        graphics.fill(x - 1, y + height, x + width + 1, y + height + 1, color);
        graphics.fill(x - 1, y, x, y + height, color);
        graphics.fill(x + width, y, x + width + 1, y + height, color);
    }
}
