package com.maurimdev.statssystem.client.ui.screen;

import com.maurimdev.statssystem.client.keybinding.ModKeyBindings;
import com.maurimdev.statssystem.client.ui.components.ScrollablePanel;
import com.maurimdev.statssystem.client.ui.components.StatCard;
import com.maurimdev.statssystem.client.ui.components.StatDetailsPanel;
import com.maurimdev.statssystem.client.ui.rendering.ScreenRenderer;
import com.maurimdev.statssystem.client.ui.rendering.SystemLayout;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.core.domain.stats.StatType;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla principal de estadísticas estilo Souls
 * Refactorizada para delegar renderizado y layout a componentes especializados
 */
public class StatsScreen extends Screen {

    // ============================================
    // COMPONENTES
    // ============================================

    private SystemLayout layout;
    private ScreenRenderer renderer;
    private ScrollablePanel leftScrollPanel;   // Scroll para el panel izquierdo (stats)
    private ScrollablePanel rightScrollPanel;  // Scroll para el panel derecho (detalles)

    // ============================================
    // DATOS
    // ============================================

    private final PlayerStats stats;
    private StatType hoveredStat;
    private StatType selectedStat;
    private float[] statAnimations;

    // ============================================
    // ÁREAS INTERACTIVAS
    // ============================================

    private final List<StatCardArea> statCardAreas = new ArrayList<>();

    private record StatCardArea(StatType stat, int x, int y, int width, int height) {
        boolean contains(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width &&
                    mouseY >= y && mouseY <= y + height;
        }
    }

    // ============================================
    // CONSTRUCTOR
    // ============================================

    public StatsScreen() {
        super(Component.literal("Estadísticas"));
        this.stats = loadPlayerStats();
        this.statAnimations = new float[StatType.values().length];
        this.hoveredStat = null;
        this.selectedStat = StatType.values()[0];
    }

    private PlayerStats loadPlayerStats() {
        if (Minecraft.getInstance().player == null) {
            return new PlayerStats();
        }
        return Minecraft.getInstance().player.getData(ModAttachments.PLAYER_STATS);
    }

    // ============================================
    // INICIALIZACIÓN
    // ============================================

    @Override
    protected void init() {
        super.init();

        // Crear componentes de layout y renderizado
        this.layout = new SystemLayout(this.width, this.height);
        this.renderer = new ScreenRenderer(this.font, layout);
        this.leftScrollPanel = new ScrollablePanel(layout);
        this.rightScrollPanel = new ScrollablePanel(layout);

        this.clearWidgets();
        statCardAreas.clear();

        initWidgets();
    }

    private void initWidgets() {
        addStatButtons();
        setupLeftScrollPanel();
        setupRightScrollPanel();
    }

    private void addStatButtons() {
        // Calcular posiciones con espacio suficiente para el header "ATRIBUTOS" + línea decorativa
        // Título está en: topSectionBottom + 2 + scaled(8)
        // Línea está en: título_y + 12
        // Margen después de la línea para separación visual
        int headerSpace = layout.scaled(45); // Espacio: título + línea + margen
        int startY = layout.getTopSectionBottom() + headerSpace;
        int statHeight = layout.scaled(layout.getStatHeight());
        int itemWidth = layout.getStatCardWidth();

        // Registrar áreas de stats
        statCardAreas.clear();
        int index = 0;
        for (StatType statType : StatType.values()) {
            int yPos = startY + (index * statHeight);
            int itemX = layout.getStatCardX();

            // Registrar área clickeable para hover
            statCardAreas.add(new StatCardArea(statType, itemX, yPos, itemWidth, layout.getStatCardHeight()));

            index++;
        }
    }

    private void setupLeftScrollPanel() {
        // Configurar el ScrollablePanel para el panel izquierdo (lista de stats)
        int leftPos = layout.getLeftPos();
        int topSectionBottom = layout.getTopSectionBottom();

        // Área de scroll: debajo del header "ATRIBUTOS" + línea decorativa hasta el final del panel
        // IMPORTANTE: El scissor debe empezar 3px ANTES de donde empiezan los cards
        // para que el borde superior (que se extiende 1px arriba) no se corte
        int headerSpace = layout.scaled(45); // Mismo valor que en addStatButtons
        int scrollX = leftPos + layout.scaled(layout.getPadding());
        int scrollY = topSectionBottom + headerSpace - 3; // -3px para dar espacio al borde superior
        int scrollWidth = layout.getLeftPanelFullWidth(); // Ancho SIN padding
        int scrollHeight = layout.getGuiHeight() - layout.scaled(layout.getTopSectionHeight()) - headerSpace - layout.scaled(4) + 3; // +3px para compensar

        leftScrollPanel.setBounds(scrollX, scrollY, scrollWidth, scrollHeight);

        // Usar el nuevo API: configurar un supplier para calcular automáticamente la altura del contenido
        leftScrollPanel.setContentHeightSupplier(() -> {
            int statHeight = layout.scaled(layout.getStatHeight());
            return StatType.values().length * statHeight;
        });
    }

    private void setupRightScrollPanel() {
        // Configurar el ScrollablePanel para el panel derecho (detalles de stat)
        int rightPanelLeft = layout.getRightPanelLeft();
        int topSectionBottom = layout.getTopSectionBottom();
        int rightPanelWidth = layout.getRightPanelWidth();

        int scrollX = rightPanelLeft + layout.scaled(18);
        int scrollY = topSectionBottom + layout.scaled(18);
        int scrollWidth = rightPanelWidth - layout.scaled(36); // Restar padding de ambos lados
        int scrollHeight = layout.getGuiHeight() - layout.scaled(layout.getTopSectionHeight()) - layout.scaled(36);

        rightScrollPanel.setBounds(scrollX, scrollY, scrollWidth, scrollHeight);

        // La altura del contenido se calcula dinámicamente según la stat seleccionada
        rightScrollPanel.setContentHeightSupplier(() -> {
            if (selectedStat == null) {
                return 0;
            }
            // Usar el método de StatDetailsPanel para calcular la altura exacta
            return StatDetailsPanel.calculateContentHeight(stats, selectedStat);
        });
    }

    // ============================================
    // RENDERIZADO
    // ============================================

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderer.renderBackground(graphics, this.width, this.height);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updateAnimations();
        updateHoveredStat(mouseX, mouseY);

        renderMainScreen(graphics, mouseX, mouseY);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    /**
     * Renderiza solo el contenido sin el background (usado por TabbedStatsScreen)
     */
    public void renderContentOnly(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updateAnimations();
        updateHoveredStat(mouseX, mouseY);

        renderMainScreen(graphics, mouseX, mouseY);

        // Renderizar widgets sin llamar a super que podría renderizar background
        for (var widget : this.renderables) {
            widget.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    private void renderMainScreen(GuiGraphics graphics, int mouseX, int mouseY) {
        renderer.renderMainFrame(graphics);
        renderer.renderTopSection(graphics);

        // Panel izquierdo
        renderer.renderLeftPanelBackground(graphics);

        // Header adaptativo según tamaño de pantalla
        String title = "§e§lATRIBUTOS";
        String levelText;
        if (layout.isTinyScreen()) {
            // Pantallas tiny: solo el número
            levelText = "§e" + stats.getTotalLevel();
        } else {
            // Pantallas normales/pequeñas: texto completo
            levelText = "§7Nivel: §e" + stats.getTotalLevel();
        }
        renderer.renderLeftPanelHeader(graphics, title, levelText);

        renderAllStats(graphics);

        // Panel derecho
        renderer.renderRightPanel(graphics);
        if (selectedStat != null) {
            renderStatDetails(graphics);
        }
    }

    private void renderAllStats(GuiGraphics graphics) {
        // Aplicar scissor para clipping
        leftScrollPanel.enableScissor(graphics);

        int headerSpace = layout.scaled(45); // Mismo valor que en addStatButtons
        int startY = layout.getTopSectionBottom() + headerSpace;
        int statHeight = layout.scaled(layout.getStatHeight());

        // Usar el nuevo API del ScrollablePanel
        int width = leftScrollPanel.getContentWidth(); // Ancho que resta scrollbar si es necesario
        int height = layout.getStatCardHeight();
        int x = leftScrollPanel.getContentX(); // Posición X del contenido

        int index = 0;
        for (StatType statType : StatType.values()) {
            // Calcular posición con scroll offset
            int yPos = startY + (index * statHeight) - leftScrollPanel.getScrollOffset();

            // Solo renderizar si está visible en el área de scroll
            int scrollY = layout.getTopSectionBottom() + headerSpace;
            int scrollHeight = layout.getGuiHeight() - layout.scaled(layout.getTopSectionHeight()) - headerSpace - layout.scaled(4);

            if (yPos + height >= scrollY && yPos <= scrollY + scrollHeight) {
                boolean isHovered = statType == hoveredStat;
                boolean isSelected = statType == selectedStat;
                float animation = statAnimations[index];

                // Usar componente StatCard
                StatCard.render(graphics, this.font, stats, statType, x, yPos, width, height,
                        isHovered, isSelected, animation, layout.isTinyScreen(), layout.isSmallScreen());
            }

            index++;
        }

        leftScrollPanel.disableScissor(graphics);

        // Renderizar scrollbar solo si es necesario (aparece automáticamente)
        if (leftScrollPanel.hasScrollableContent()) {
            leftScrollPanel.renderScrollbar(graphics);
        }
    }

    private void renderStatDetails(GuiGraphics graphics) {
        // Activar scissor para el panel derecho
        rightScrollPanel.enableScissor(graphics);

        // Usar las coordenadas del scrollable panel
        int detailX = rightScrollPanel.getContentX();
        int detailY = rightScrollPanel.getContentY();
        int cardWidth = rightScrollPanel.getContentWidth();
        int scrollOffset = rightScrollPanel.getScrollOffset();

        // Renderizar el panel de detalles con offset de scroll
        StatDetailsPanel.render(graphics, this.font, stats, selectedStat, detailX, detailY - scrollOffset, cardWidth);

        rightScrollPanel.disableScissor(graphics);

        // Renderizar scrollbar si es necesario
        if (rightScrollPanel.hasScrollableContent()) {
            rightScrollPanel.renderScrollbar(graphics);
        }
    }

    // ============================================
    // ACTUALIZACIÓN DE ESTADO
    // ============================================

    private void updateAnimations() {
        for (int i = 0; i < statAnimations.length; i++) {
            if (statAnimations[i] > 0) {
                statAnimations[i] -= 0.15f; // ANIMATION_SPEED
                if (statAnimations[i] < 0) statAnimations[i] = 0;
            }
        }
    }

    private void updateHoveredStat(int mouseX, int mouseY) {
        hoveredStat = null;

        int headerSpace = layout.scaled(45); // Mismo valor que en addStatButtons
        int startY = layout.getTopSectionBottom() + headerSpace;
        int statHeight = layout.scaled(layout.getStatHeight());

        // Usar el nuevo API del ScrollablePanel
        int width = leftScrollPanel.getContentWidth();
        int x = leftScrollPanel.getContentX();

        int index = 0;
        for (StatType statType : StatType.values()) {
            // Calcular posición con scroll offset
            int yPos = startY + (index * statHeight) - leftScrollPanel.getScrollOffset();
            int height = layout.getStatCardHeight();

            // Verificar si el mouse está sobre este stat
            if (mouseX >= x && mouseX <= x + width &&
                mouseY >= yPos && mouseY <= yPos + height) {
                hoveredStat = statType;
                break;
            }

            index++;
        }
    }

    // ============================================
    // INPUT
    // ============================================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Primero verificar si es click en alguna scrollbar
        if (button == 0) {
            if (leftScrollPanel.handleMouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            if (rightScrollPanel.handleMouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        // Luego verificar clicks en stats
        if (hoveredStat != null) {
            selectedStat = hoveredStat;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Delegar a ambos ScrollablePanels (solo uno manejará el evento según posición del mouse)
        if (leftScrollPanel.handleMouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        if (rightScrollPanel.handleMouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // Delegar a ambos ScrollablePanels
        if (leftScrollPanel.handleMouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        if (rightScrollPanel.handleMouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Delegar a ambos ScrollablePanels
        if (leftScrollPanel.handleMouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        if (rightScrollPanel.handleMouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 || ModKeyBindings.OPEN_STATS_MENU.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}