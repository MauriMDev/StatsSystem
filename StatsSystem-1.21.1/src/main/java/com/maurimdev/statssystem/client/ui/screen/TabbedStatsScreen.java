package com.maurimdev.statssystem.client.ui.screen;

import com.maurimdev.statssystem.client.config.HUDConfig;
import com.maurimdev.statssystem.client.keybinding.ModKeyBindings;
import com.maurimdev.statssystem.client.ui.rendering.ScreenRenderer;
import com.maurimdev.statssystem.client.ui.rendering.SystemLayout;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Pantalla principal con sistema de tabs (Stats / Perks)
 */
public class TabbedStatsScreen extends Screen {

    // ============================================
    // ENUMS Y CONSTANTES
    // ============================================

    public enum Tab {
        STATS("Estadísticas", "Estadísticas"),
        PERKS("Habilidades", "Habilidades");

        private final String displayName;
        private final String shortName;

        Tab(String displayName, String shortName) {
            this.displayName = displayName;
            this.shortName = shortName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getShortName() {
            return shortName;
        }
    }

    private static final int TAB_SPACING = 8;  // Reducido para tabs más pequeños
    private static final int TAB_SPACING_TINY = 6;  // Aún más pequeño para pantallas tiny
    // Los tabs ahora se posicionan dinámicamente en el rectángulo superior

    // Colores mejorados con más opacidad y contraste
    private static final int COLOR_TAB_ACTIVE = 0xFF2A1F17;      // Sólido, no transparente
    private static final int COLOR_TAB_INACTIVE = 0xEE1A1410;
    private static final int COLOR_TAB_HOVER = 0xFF332211;
    private static final int COLOR_TAB_BORDER = 0xFFFFAA00;
    private static final int COLOR_TAB_BORDER_INACTIVE = 0xFF664422;

    // ============================================
    // DATOS
    // ============================================

    private Tab currentTab;
    private final PlayerStats stats;

    // Subpantallas
    private StatsScreen statsScreen;
    private PerksScreen perksScreen;

    // Layout y renderer para obtener tamaños adaptativos y renderizar header
    private SystemLayout layout;
    private ScreenRenderer renderer;

    // Hover state
    private Tab hoveredTab = null;

    // Botón de toggle del HUD (custom)
    private HudToggleButtonArea hudToggleButtonArea;
    private boolean hudButtonHovered = false;

    private record HudToggleButtonArea(int x, int y, int width, int height) {
        boolean contains(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width &&
                    mouseY >= y && mouseY <= y + height;
        }
    }

    // ============================================
    // CONSTRUCTOR
    // ============================================

    public TabbedStatsScreen() {
        this(Tab.STATS);
    }

    public TabbedStatsScreen(Tab initialTab) {
        super(Component.literal("Stats System"));
        this.currentTab = initialTab;
        this.stats = loadPlayerStats();
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

        // Crear layout y renderer para obtener tamaños adaptativos
        this.layout = new SystemLayout(this.width, this.height);
        this.renderer = new ScreenRenderer(this.font, layout);

        // Crear subpantallas si no existen
        if (this.statsScreen == null) {
            this.statsScreen = new StatsScreen();
        }
        if (this.perksScreen == null) {
            this.perksScreen = new PerksScreen();
        }

        // Inicializar subpantallas con el contexto correcto
        this.statsScreen.init(this.minecraft, this.width, this.height);
        this.perksScreen.reinit(this.minecraft, this.width, this.height);

        // Crear área del botón de toggle del HUD
        setupHudToggleButton();
    }

    private void setupHudToggleButton() {
        // Botón en la esquina superior derecha del header
        // Ajustar tamaño según tipo de pantalla
        int buttonWidth, buttonHeight;
        if (layout.isTinyScreen()) {
            // Pantallas tiny: botón más pequeño, una sola línea
            buttonWidth = layout.scaled(48);
            buttonHeight = layout.scaled(16);
        } else {
            // Pantallas normales/pequeñas: botón normal con 2 líneas
            buttonWidth = layout.scaled(68);
            buttonHeight = layout.scaled(28);
        }

        int buttonX = layout.getLeftPos() + layout.getGuiWidth() - buttonWidth - layout.scaled(10);
        int buttonY = layout.getTopPos() + layout.scaled(8);

        hudToggleButtonArea = new HudToggleButtonArea(buttonX, buttonY, buttonWidth, buttonHeight);
    }

    // ============================================
    // RENDERIZADO
    // ============================================

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Background muy sutil y transparente para no tapar elementos
        // Solo un ligero degradado para dar contexto
        graphics.fillGradient(0, 0, this.width, this.height, 0x60000000, 0x80000000);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 1. Renderizar background primero
        renderBackground(graphics, mouseX, mouseY, partialTick);

        // 2. Renderizar contenido de la tab activa SIN su background
        switch (currentTab) {
            case STATS -> {
                // Renderizar solo contenido sin background
                statsScreen.renderContentOnly(graphics, mouseX, mouseY, partialTick);
            }
            case PERKS -> {
                // Renderizar solo contenido sin background
                perksScreen.renderContentOnly(graphics, mouseX, mouseY, partialTick);
            }
        }

        // 3. Renderizar título del mod (solo si NO es tiny, centrado y grande)
        if (!layout.isTinyScreen()) {
            renderer.renderModTitleCenteredForTabs(graphics);
        }

        // 4. Renderizar tabs DESPUÉS del contenido (para que queden arriba de todo)
        renderTabs(graphics, mouseX, mouseY);

        // 5. Renderizar botón HUD custom
        renderHudToggleButton(graphics, mouseX, mouseY);

        // 6. Renderizar widgets de la screen base (si los hay)
        super.render(graphics, mouseX, mouseY, partialTick);

        // 7. Renderizar tooltip del botón de atributos si está en hover
        if (hudButtonHovered) {
            if (HUDConfig.isHudEnabled()) {
                graphics.renderTooltip(this.font,
                    Component.literal("§aAtributos Derivados Activados\n§7Panel visible en el juego\n§7Click para desactivar"),
                    mouseX, mouseY);
            } else {
                graphics.renderTooltip(this.font,
                    Component.literal("§cAtributos Derivados Desactivados\n§7Panel oculto\n§7Click para activar"),
                    mouseX, mouseY);
            }
        }
    }

    private void renderHudToggleButton(GuiGraphics graphics, int mouseX, int mouseY) {
        if (hudToggleButtonArea == null) return;

        // Actualizar hover state
        hudButtonHovered = hudToggleButtonArea.contains(mouseX, mouseY);

        boolean isEnabled = HUDConfig.isHudEnabled();
        int x = hudToggleButtonArea.x;
        int y = hudToggleButtonArea.y;
        int width = hudToggleButtonArea.width;
        int height = hudToggleButtonArea.height;

        // Background
        int bgColor;
        if (isEnabled) {
            bgColor = hudButtonHovered ? 0x8000AA00 : 0x60008800;  // Verde
        } else {
            bgColor = hudButtonHovered ? 0x80AA0000 : 0x60880000;  // Rojo
        }
        graphics.fill(x, y, x + width, y + height, bgColor);

        // Borde
        int borderColor = hudButtonHovered ? ScreenRenderer.COLOR_BORDER_HIGHLIGHT : ScreenRenderer.COLOR_BORDER;
        int borderThickness = hudButtonHovered ? 2 : 1;

        // Top
        graphics.fill(x, y, x + width, y + borderThickness, borderColor);
        // Bottom
        graphics.fill(x, y + height - borderThickness, x + width, y + height, borderColor);
        // Left
        graphics.fill(x, y, x + borderThickness, y + height, borderColor);
        // Right
        graphics.fill(x + width - borderThickness, y, x + width, y + height, borderColor);

        // Renderizado adaptativo según tamaño de pantalla
        if (layout.isTinyScreen()) {
            // Pantallas tiny: una sola línea compacta con símbolo + estado
            String text = isEnabled ? "§a✓ Stats" : "§c✗ Stats";
            String rawText = isEnabled ? "✓ Stats" : "✗ Stats";
            int textWidth = font.width(rawText);
            int textX = x + (width / 2) - (textWidth / 2);
            int textY = y + (height / 2) - 4;

            graphics.drawString(font, text, textX, textY, 0xFFFFFFFF, false);
        } else {
            // Pantallas normales/pequeñas: 2 líneas con label descriptivo
            // Línea 1: Label "Atributos"
            String labelText = "§7Atributos";
            String rawLabel = "Atributos";
            int labelWidth = font.width(rawLabel);
            int labelX = x + (width / 2) - (labelWidth / 2);
            int labelY = y + 3;

            graphics.drawString(font, labelText, labelX, labelY, 0xFFFFFFFF, false);

            // Línea 2: Estado "Activo" / "Inactivo"
            String statusText = isEnabled ? "§aActivo" : "§cInactivo";
            String rawStatus = isEnabled ? "Activo" : "Inactivo";
            int statusWidth = font.width(rawStatus);
            int statusX = x + (width / 2) - (statusWidth / 2);
            int statusY = y + 15;

            graphics.drawString(font, statusText, statusX, statusY, 0xFFFFFFFF, false);
        }
    }

    private void renderTabs(GuiGraphics graphics, int mouseX, int mouseY) {
        updateHoveredTab(mouseX, mouseY);

        int leftPos = layout.getLeftPos();
        int topPos = layout.getTopPos();
        int guiWidth = layout.getGuiWidth();
        int topSectionHeight = layout.scaled(layout.getTopSectionHeight());

        // Obtener tamaños de tabs adaptativos
        int tabWidth = layout.getTabWidth();
        int tabHeight = layout.getTabHeight();
        int tabSpacing = layout.isTinyScreen() ? TAB_SPACING_TINY : TAB_SPACING;

        int totalWidth = (tabWidth * Tab.values().length) + (tabSpacing * (Tab.values().length - 1));

        // SIEMPRE centrar tabs horizontalmente
        int centerX = leftPos + (guiWidth / 2);
        int startX = centerX - (totalWidth / 2);

        // Posición vertical de tabs según si hay título o no
        int tabY;
        if (layout.isTinyScreen()) {
            // Pantallas tiny: SIN título, tabs centrados verticalmente
            tabY = topPos + (topSectionHeight - tabHeight) / 2;
        } else {
            // Pantallas normales/pequeñas: CON título arriba, tabs abajo
            tabY = topPos + topSectionHeight - tabHeight - layout.scaled(8);
        }

        int index = 0;
        for (Tab tab : Tab.values()) {
            int tabX = startX + (index * (tabWidth + tabSpacing));

            boolean isActive = tab == currentTab;
            boolean isHovered = tab == hoveredTab && !isActive;

            renderTab(graphics, tab, tabX, tabY, tabWidth, tabHeight, isActive, isHovered);

            index++;
        }
    }

    private void renderTab(GuiGraphics graphics, Tab tab, int x, int y, int tabWidth, int tabHeight, boolean isActive, boolean isHovered) {
        // Sombra más prominente
        graphics.fill(x + 3, y + 3, x + tabWidth + 3, y + tabHeight + 3, 0xAA000000);

        int bgColor = isActive ? COLOR_TAB_ACTIVE : (isHovered ? COLOR_TAB_HOVER : COLOR_TAB_INACTIVE);
        graphics.fill(x, y, x + tabWidth, y + tabHeight, bgColor);

        // Border completo más visible
        int borderColor = isActive ? COLOR_TAB_BORDER : COLOR_TAB_BORDER_INACTIVE;
        int borderThickness = isActive ? 2 : 1;

        // Frame completo
        // Top
        graphics.fill(x, y, x + tabWidth, y + borderThickness, borderColor);
        // Bottom
        graphics.fill(x, y + tabHeight - borderThickness, x + tabWidth, y + tabHeight, borderColor);
        // Left
        graphics.fill(x, y, x + borderThickness, y + tabHeight, borderColor);
        // Right
        graphics.fill(x + tabWidth - borderThickness, y, x + tabWidth, y + tabHeight, borderColor);

        // Texto siempre con el nombre completo en español
        if (this.font != null) {
            int textColor = isActive ? 0xFFFFAA00 : (isHovered ? 0xFFFFDD88 : 0xFFCCCCCC);
            String text = tab.getDisplayName();
            int textWidth = this.font.width(text);
            int textX = x + (tabWidth / 2) - (textWidth / 2);
            int textY = y + (tabHeight / 2) - 4;

            // Sombra del texto más fuerte para mejor legibilidad
            graphics.drawString(this.font, text, textX + 2, textY + 2, 0xDD000000, false);
            graphics.drawString(this.font, text, textX, textY, textColor, false);
        }
    }

    // ============================================
    // INPUT
    // ============================================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Click izquierdo
            // Verificar click en botón HUD PRIMERO (tiene prioridad)
            if (hudToggleButtonArea != null && hudToggleButtonArea.contains((int) mouseX, (int) mouseY)) {
                HUDConfig.toggleHud();
                return true;
            }

            // Verificar clicks en tabs
            Tab clickedTab = getTabAt((int) mouseX, (int) mouseY);
            if (clickedTab != null && clickedTab != currentTab) {
                switchTab(clickedTab);
                return true;
            }
        }

        // Delegar a la pantalla activa
        switch (currentTab) {
            case STATS -> {
                return statsScreen.mouseClicked(mouseX, mouseY, button);
            }
            case PERKS -> {
                return perksScreen.mouseClicked(mouseX, mouseY, button);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Delegar a la pantalla activa
        switch (currentTab) {
            case STATS -> {
                return statsScreen.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
            }
            case PERKS -> {
                return perksScreen.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // Delegar a la pantalla activa
        switch (currentTab) {
            case STATS -> {
                return statsScreen.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }
            case PERKS -> {
                return perksScreen.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Delegar a la pantalla activa
        switch (currentTab) {
            case STATS -> {
                return statsScreen.mouseReleased(mouseX, mouseY, button);
            }
            case PERKS -> {
                return perksScreen.mouseReleased(mouseX, mouseY, button);
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC o tecla de stats cierra la pantalla
        if (keyCode == 256 || ModKeyBindings.OPEN_STATS_MENU.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }

        // Delegar a la pantalla activa
        switch (currentTab) {
            case STATS -> {
                return statsScreen.keyPressed(keyCode, scanCode, modifiers);
            }
            case PERKS -> {
                return perksScreen.keyPressed(keyCode, scanCode, modifiers);
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ============================================
    // UTILIDADES
    // ============================================

    private void switchTab(Tab newTab) {
        if (newTab != currentTab) {
            currentTab = newTab;

            // Re-inicializar la pantalla activa para refrescar datos
            switch (currentTab) {
                case STATS -> statsScreen.init(this.minecraft, this.width, this.height);
                case PERKS -> perksScreen.reinit(this.minecraft, this.width, this.height);
            }
        }
    }

    private void updateHoveredTab(int mouseX, int mouseY) {
        hoveredTab = getTabAt(mouseX, mouseY);
    }

    private Tab getTabAt(int mouseX, int mouseY) {
        // Usar la MISMA lógica que renderTabs
        int leftPos = layout.getLeftPos();
        int topPos = layout.getTopPos();
        int guiWidth = layout.getGuiWidth();
        int topSectionHeight = layout.scaled(layout.getTopSectionHeight());

        // Obtener tamaños de tabs adaptativos
        int tabWidth = layout.getTabWidth();
        int tabHeight = layout.getTabHeight();
        int tabSpacing = layout.isTinyScreen() ? TAB_SPACING_TINY : TAB_SPACING;

        int totalWidth = (tabWidth * Tab.values().length) + (tabSpacing * (Tab.values().length - 1));

        // SIEMPRE centrar tabs horizontalmente
        int centerX = leftPos + (guiWidth / 2);
        int startX = centerX - (totalWidth / 2);

        // Posición vertical de tabs según si hay título o no
        int tabY;
        if (layout.isTinyScreen()) {
            // Pantallas tiny: SIN título, tabs centrados verticalmente
            tabY = topPos + (topSectionHeight - tabHeight) / 2;
        } else {
            // Pantallas normales/pequeñas: CON título arriba, tabs abajo
            tabY = topPos + topSectionHeight - tabHeight - layout.scaled(8);
        }

        int index = 0;
        for (Tab tab : Tab.values()) {
            int tabX = startX + (index * (tabWidth + tabSpacing));

            if (mouseX >= tabX && mouseX <= tabX + tabWidth &&
                    mouseY >= tabY && mouseY <= tabY + tabHeight) {
                return tab;
            }

            index++;
        }

        return null;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}