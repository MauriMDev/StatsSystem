package com.maurimdev.statssystem.client.ui.screen;

import com.maurimdev.statssystem.client.ui.components.ScrollablePanel;
import com.maurimdev.statssystem.client.ui.components.StatCard;
import com.maurimdev.statssystem.client.ui.rendering.ScreenRenderer;
import com.maurimdev.statssystem.client.ui.rendering.SystemLayout;
import com.maurimdev.statssystem.core.domain.perk.*;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.core.domain.stats.StatType;
import com.maurimdev.statssystem.core.service.PerkService;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import com.maurimdev.statssystem.network.ModMessages;
import com.maurimdev.statssystem.network.UnlockPerkPacket;
import com.maurimdev.statssystem.network.TogglePerkPacket;
import com.maurimdev.statssystem.network.UpgradePerkPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Pantalla de perks con visualización de árboles por stat
 * Usa componentes compartidos con StatsScreen para consistencia visual
 */
public class PerksScreen extends Screen {

    // ============================================
    // CONSTANTES
    // ============================================

    // Usar constantes del renderer para consistencia total
    private static final int COLOR_TEXT_PRIMARY = ScreenRenderer.COLOR_TEXT_PRIMARY;
    private static final int COLOR_TEXT_SECONDARY = ScreenRenderer.COLOR_TEXT_SECONDARY;
    private static final int COLOR_TEXT_GOLD = ScreenRenderer.COLOR_TEXT_GOLD;
    private static final int COLOR_BORDER_HIGHLIGHT = ScreenRenderer.COLOR_BORDER_HIGHLIGHT;

    // Diseño mejorado de perks cards - ahora adaptativo
    private static final int PERK_ITEM_HEIGHT = 90;  // Altura estándar para pantallas normales y small
    private static final int PERK_ITEM_HEIGHT_TINY = 160;  // Altura mayor solo para pantallas tiny
    private static final int PERK_ITEM_PADDING = 12;   // Aumentado de 8 a 12 para más spacing entre items
    private static final int PERK_ITEM_PADDING_COMPACT = 10;   // Aumentado de 6 a 10 para mejor spacing
    private static final int PERK_CARD_WIDTH = 450;   // Ancho de las tarjetas al límite del scroll

    // Constantes para la barra de scroll
    private static final int SCROLLBAR_WIDTH = 10;
    private static final int SCROLLBAR_PADDING = 6;

    // Constantes para renderizado de UI
    private static final int TEXT_VERTICAL_OFFSET = 4;
    private static final int CATEGORY_SECTION_TOP_OFFSET = 45;
    private static final int TIER_BUTTON_TOP_OFFSET = 15;
    private static final int SCROLL_CLIP_TOP_OFFSET = 40;
    private static final int SCROLL_CLIP_BOTTOM_OFFSET = 45;
    private static final int PERK_LIST_LEFT_OFFSET = 10;

    // ============================================
    // COMPONENTES
    // ============================================

    private SystemLayout layout;
    private ScreenRenderer renderer;
    private ScrollablePanel scrollablePanel;

    // ============================================
    // TIPOS INTERNOS
    // ============================================

    /**
     * Categoría de perks para filtrado
     */
    private enum PerkCategory {
        VITALITY(StatType.VITALITY, "§c❤ Vitalidad"),
        STRENGTH(StatType.STRENGTH, "§6💪 Fuerza"),
        DEXTERITY(StatType.DEXTERITY, "§b🎯 Destreza"),
        MINING(StatType.MINING, "§7⛏ Minería"),
        AGILITY(StatType.AGILITY, "§a👟 Agilidad"),
        FARMING(StatType.FARMING, "§2🌾 Agricultura"),
        COMBO(null, "§d✨ Combinadas");

        private final StatType stat;
        private final String displayName;

        PerkCategory(StatType stat, String displayName) {
            this.stat = stat;
            this.displayName = displayName;
        }

        public StatType getStat() {
            return stat;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean isCombo() {
            return stat == null;
        }
    }

    /**
     * Filtro de tier para perks
     */
    private enum TierFilter {
        ALL(null, "Todas"),
        TIER_1(PerkTier.TIER_1, "Tier I"),
        TIER_2(PerkTier.TIER_2, "Tier II"),
        TIER_3(PerkTier.TIER_3, "Tier III"),
        COMBO(PerkTier.COMBO, "Combo");

        private final PerkTier tier;
        private final String displayName;

        TierFilter(PerkTier tier, String displayName) {
            this.tier = tier;
            this.displayName = displayName;
        }

        public PerkTier getTier() {
            return tier;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean matches(Perk perk) {
            return tier == null || perk.getTier() == tier;
        }
    }

    // ============================================
    // DATOS
    // ============================================

    private PlayerStats stats;
    private PerkService perkService;
    private PerkCategory selectedCategory;
    private TierFilter selectedTier;
    private List<Perk> displayedPerks;
    private PerkCategory hoveredCategory = null;
    private TierFilter hoveredTier = null;

    // Áreas clickeables de las categorías
    private final List<CategoryButtonArea> categoryButtonAreas = new ArrayList<>();
    private final List<TierButtonArea> tierButtonAreas = new ArrayList<>();

    private record CategoryButtonArea(PerkCategory category, int x, int y, int width, int height) {
        boolean contains(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width &&
                    mouseY >= y && mouseY <= y + height;
        }
    }

    private record TierButtonArea(TierFilter tier, int x, int y, int width, int height) {
        boolean contains(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width &&
                    mouseY >= y && mouseY <= y + height;
        }
    }

    // ============================================
    // CONSTRUCTOR
    // ============================================

    /**
     * Obtiene la altura de item de perk basada en el tamaño de pantalla
     */
    private int getPerkItemHeight() {
        if (layout == null) return PERK_ITEM_HEIGHT;
        if (layout.isTinyScreen()) return PERK_ITEM_HEIGHT_TINY;
        return PERK_ITEM_HEIGHT;  // Mismo para normal y small
    }

    /**
     * Obtiene el padding de item de perk basado en el tamaño de pantalla
     */
    private int getPerkItemPadding() {
        if (layout == null) return PERK_ITEM_PADDING;
        if (layout.isSmallScreen()) return PERK_ITEM_PADDING_COMPACT;
        return PERK_ITEM_PADDING;
    }

    public PerksScreen() {
        super(Component.literal("Habilidades"));
        this.perkService = PerkService.getInstance();
        this.stats = loadPlayerStats();
        this.selectedCategory = PerkCategory.VITALITY;
        this.selectedTier = TierFilter.ALL;
        this.displayedPerks = new ArrayList<>();
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

        // Crear componentes de layout y renderizado (igual que StatsScreen)
        this.layout = new SystemLayout(this.width, this.height);
        this.renderer = new ScreenRenderer(this.font, layout);

        // Crear ScrollablePanel
        this.scrollablePanel = new ScrollablePanel(layout);

        // Actualizar stats y perks
        this.stats = loadPlayerStats();

        updateDisplayedPerks();

        initWidgets();
    }

    /**
     * Método público para reinicializar desde TabbedStatsScreen
     */
    public void reinit(Minecraft minecraft, int width, int height) {
        this.minecraft = minecraft;
        this.width = width;
        this.height = height;
        this.font = minecraft.font;
        init();
    }

    private void initWidgets() {
        this.clearWidgets();
        categoryButtonAreas.clear();
        tierButtonAreas.clear();

        // Registrar áreas de botones custom
        registerCategoryButtons();
        registerTierButtons();

        // Configurar el ScrollablePanel
        int rightPanelLeft = layout.getRightPanelLeft();
        int topSectionBottom = layout.getTopSectionBottom();
        int rightPanelWidth = layout.getRightPanelWidth();

        int clipY = topSectionBottom + layout.scaled(SCROLL_CLIP_TOP_OFFSET);
        int clipHeight = layout.getGuiHeight() - layout.scaled(layout.getTopSectionHeight()) - layout.scaled(SCROLL_CLIP_BOTTOM_OFFSET);

        scrollablePanel.setBounds(rightPanelLeft, clipY, rightPanelWidth, clipHeight);

        // Usar el nuevo API: configurar un supplier para calcular automáticamente la altura del contenido
        scrollablePanel.setContentHeightSupplier(() ->
            displayedPerks.size() * layout.scaled(getPerkItemHeight() + getPerkItemPadding())
        );

        // NO agregar botones de minecraft, usaremos renderizado custom
    }

    /**
     * Registra las áreas clickeables de los cards de categorías
     * Formato similar a StatCards: una columna con altura adaptativa
     */
    private void registerCategoryButtons() {
        int topSectionBottom = layout.getTopSectionBottom();
        int startY = topSectionBottom + layout.scaled(CATEGORY_SECTION_TOP_OFFSET);

        // Usar las mismas medidas que StatCards, pero ancho completo (sin restar scrollbar)
        // porque el panel izquierdo de Perks no tiene scrollbar
        int cardWidth = layout.getLeftPanelWidth() - layout.scaled(layout.getPadding() * 2);
        int cardHeight = layout.getStatCardHeight();
        int startX = layout.getStatCardX();
        int statHeight = layout.scaled(layout.getStatHeight());

        int index = 0;
        for (PerkCategory category : PerkCategory.values()) {
            int x = startX;
            int y = startY + (index * statHeight);

            categoryButtonAreas.add(new CategoryButtonArea(category, x, y, cardWidth, cardHeight));
            index++;
        }
    }

    /**
     * Registra las áreas clickeables de los botones de tier
     */
    private void registerTierButtons() {
        int rightPanelLeft = layout.getRightPanelLeft();
        int topSectionBottom = layout.getTopSectionBottom();
        int rightPanelWidth = layout.getRightPanelWidth();
        boolean isTiny = layout.isTinyScreen();

        // Botones horizontales con tamaños adaptativos
        int buttonWidth = layout.scaled(isTiny ? 85 : 70);  // Más ancho en tiny
        int buttonHeight = layout.scaled(isTiny ? 26 : 22); // Más alto en tiny
        int spacing = layout.scaled(isTiny ? 4 : 6);        // Menos espacio en tiny para que quepan mejor

        // Centrar horizontalmente
        int totalWidth = (buttonWidth * TierFilter.values().length) + (spacing * (TierFilter.values().length - 1));
        int startX = rightPanelLeft + (rightPanelWidth / 2) - (totalWidth / 2);
        int startY = topSectionBottom + layout.scaled(TIER_BUTTON_TOP_OFFSET);

        int index = 0;
        for (TierFilter tier : TierFilter.values()) {
            int x = startX + (index * (buttonWidth + spacing));
            tierButtonAreas.add(new TierButtonArea(tier, x, startY, buttonWidth, buttonHeight));
            index++;
        }
    }

    /**
     * Tipo de botón de perk
     */
    private enum PerkButtonType {
        UNLOCK,   // Botón para desbloquear perk
        TOGGLE,   // Botón para activar/desactivar perk
        UPGRADE   // Botón para mejorar nivel de perk
    }

    /**
     * Registra las áreas clickeables de los botones integrados en cards
     */
    private final List<PerkButtonArea> perkButtonAreas = new ArrayList<>();
    private PerkButtonArea hoveredPerkButton = null;

    private record PerkButtonArea(Perk perk, int x, int y, int width, int height, PerkButtonType type, boolean enabled) {
        boolean contains(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width &&
                    mouseY >= y && mouseY <= y + height;
        }

        boolean isUnlockButton() {
            return type == PerkButtonType.UNLOCK;
        }

        boolean isToggleButton() {
            return type == PerkButtonType.TOGGLE;
        }

        boolean isUpgradeButton() {
            return type == PerkButtonType.UPGRADE;
        }
    }

    private void updateDisplayedPerks() {
        updateDisplayedPerks(true); // Por defecto, resetear scroll
    }

    private void updateDisplayedPerks(boolean resetScroll) {
        if (selectedCategory == null) {
            displayedPerks = List.of();
            return;
        }

        List<Perk> allPerks;

        // Si es categoría combo, obtener TODAS las perks combinadas
        if (selectedCategory.isCombo()) {
            allPerks = perkService.getComboPerks();

            // Filtrar por tier si es necesario (dentro de combos)
            if (selectedTier == TierFilter.ALL || selectedTier == TierFilter.COMBO) {
                displayedPerks = allPerks;
            } else {
                // Si selecciona Tier I/II/III en categoría Combo, no mostrar nada
                // porque todos los combos son tier COMBO
                displayedPerks = List.of();
            }
        } else {
            // Categoría de stat específica (Vitalidad, Fuerza, etc.)
            StatType stat = selectedCategory.getStat();
            if (stat == null) {
                displayedPerks = List.of();
                return;
            }

            PerkTree tree = perkService.getPerkTree(stat);
            if (tree == null) {
                displayedPerks = List.of();
                return;
            }

            // Si el filtro es COMBO: mostrar solo combos que requieran esta stat
            if (selectedTier == TierFilter.COMBO) {
                List<Perk> comboPerks = perkService.getComboPerks();
                displayedPerks = comboPerks.stream()
                    .filter(perk -> perk.getRequirement().getRequiredStats().containsKey(stat))
                    .toList();
            } else {
                // Si el filtro NO es COMBO: mostrar solo perks normales de esta stat
                allPerks = tree.getAllPerks();

                if (selectedTier == TierFilter.ALL) {
                    displayedPerks = allPerks;
                } else {
                    // Filtrar por tier específico (Tier I, II, III)
                    displayedPerks = allPerks.stream()
                        .filter(selectedTier::matches)
                        .toList();
                }
            }
        }

        // Solo resetear scroll si se indica (cuando cambian categorías/filtros, NO cuando se desbloquea)
        if (resetScroll && scrollablePanel != null) {
            scrollablePanel.resetScroll();
        }
    }

    private void unlockPerk(Perk perk) {
        // Enviar packet al servidor
        ModMessages.sendToServer(new UnlockPerkPacket(perk.getId()));
        // Refrescar pantalla después de un delay pequeño
        refreshScreenAfterAction(true);
    }

    private void togglePerk(Perk perk) {
        // Optimistic update: actualizar UI inmediatamente
        boolean wasActive = stats.isPerkActive(perk.getId());
        if (wasActive) {
            stats.disablePerk(perk.getId());
        } else {
            stats.enablePerk(perk.getId());
        }

        // Enviar packet al servidor para toggle
        ModMessages.sendToServer(new TogglePerkPacket(perk.getId()));

        // Refrescar pantalla inmediatamente (sin delay)
        refreshScreenAfterAction(false);
    }

    private void upgradePerk(Perk perk) {
        // Enviar packet al servidor para upgrade
        ModMessages.sendToServer(new UpgradePerkPacket(perk.getId()));
        // Refrescar pantalla después de un delay pequeño
        refreshScreenAfterAction(true);
    }

    // ============================================
    // RENDERIZADO
    // ============================================

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // No renderizar background - TabbedStatsScreen lo maneja
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderContentOnly(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    /**
     * Renderiza solo el contenido sin el background (usado por TabbedStatsScreen)
     */
    public void renderContentOnly(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Validar que tenemos lo necesario para renderizar
        if (this.font == null || this.minecraft == null || this.renderer == null) {
            return;
        }

        // Actualizar hover states
        updateHoverStates(mouseX, mouseY);

        // 1. Renderizar el marco principal usando el renderer compartido
        renderer.renderMainFrame(graphics);

        // 2. Renderizar sección superior (sin título, los tabs ya están ahí)
        renderer.renderTopSection(graphics);

        // 3. Renderizar panel izquierdo con fondo
        renderer.renderLeftPanelBackground(graphics);
        renderLeftPanelContent(graphics);
        renderCategoryButtons(graphics);

        // 4. Renderizar panel derecho con las habilidades
        renderer.renderRightPanel(graphics);
        renderTierButtons(graphics);
        renderPerks(graphics, mouseX, mouseY);

        // 5. Renderizar widgets (botones) - aunque ya no usamos botones de Minecraft
        for (var widget : this.renderables) {
            widget.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    private void updateHoverStates(int mouseX, int mouseY) {
        // Actualizar hover de categorías
        hoveredCategory = null;
        for (CategoryButtonArea area : categoryButtonAreas) {
            if (area.contains(mouseX, mouseY)) {
                hoveredCategory = area.category;
                break;
            }
        }

        // Actualizar hover de tiers
        hoveredTier = null;
        for (TierButtonArea area : tierButtonAreas) {
            if (area.contains(mouseX, mouseY)) {
                hoveredTier = area.tier;
                break;
            }
        }

        // Actualizar hover de botones de perks
        hoveredPerkButton = null;
        for (PerkButtonArea area : perkButtonAreas) {
            if (area.contains(mouseX, mouseY)) {
                hoveredPerkButton = area;
                break;
            }
        }
    }

    // ============================================
    // MÉTODOS DE RENDERIZADO
    // ============================================

    /**
     * Renderiza el contenido del panel izquierdo (info de perks)
     */
    private void renderLeftPanelContent(GuiGraphics graphics) {
        int leftPos = layout.getLeftPos();
        int leftPanelWidth = layout.getLeftPanelWidth();
        int topSectionBottom = layout.getTopSectionBottom();

        // Calcular total de perks
        int totalUnlockedPerks = 0;
        int totalPerks = 0;
        for (PerkCategory category : PerkCategory.values()) {
            if (category == PerkCategory.COMBO) {
                List<Perk> comboPerks = perkService.getComboPerks();
                totalPerks += comboPerks.size();
                totalUnlockedPerks += comboPerks.stream().filter(p -> stats.hasPerk(p.getId())).count();
            } else if (category.getStat() != null) {
                PerkTree tree = perkService.getPerkTree(category.getStat());
                if (tree != null) {
                    List<Perk> perks = tree.getAllPerks();
                    totalPerks += perks.size();
                    totalUnlockedPerks += perks.stream().filter(p -> stats.hasPerk(p.getId())).count();
                }
            }
        }

        // Header con título y total de perks (similar a StatsScreen)
        String title = "§e§lHABILIDADES";
        String perksText;
        if (layout.isTinyScreen()) {
            // Pantallas tiny: solo el número
            perksText = "§e" + totalUnlockedPerks + "§7/§e" + totalPerks;
        } else {
            // Pantallas normales/pequeñas: texto más descriptivo
            perksText = "§7Total: §e" + totalUnlockedPerks + "§7/§e" + totalPerks;
        }
        renderer.renderLeftPanelHeader(graphics, title, perksText);
    }

    /**
     * Renderiza los cards de categorías (formato similar a StatCard)
     */
    private void renderCategoryButtons(GuiGraphics graphics) {
        for (CategoryButtonArea area : categoryButtonAreas) {
            boolean isSelected = area.category == selectedCategory;
            boolean isHovered = area.category == hoveredCategory;

            renderCategoryCard(graphics, area.category, area.x, area.y, area.width, area.height, isSelected, isHovered);
        }
    }

    /**
     * Renderiza los botones de tier
     */
    private void renderTierButtons(GuiGraphics graphics) {
        for (TierButtonArea area : tierButtonAreas) {
            boolean isSelected = area.tier == selectedTier;
            boolean isHovered = area.tier == hoveredTier;

            renderTierButton(graphics, area.tier, area.x, area.y, area.width, area.height, isSelected, isHovered);
        }
    }

    private void renderTierButton(GuiGraphics graphics, TierFilter tier, int x, int y, int width, int height, boolean isSelected, boolean isHovered) {
        // Colores específicos para cada tier
        int bgColor;
        int borderColor;
        int textColor;
        String colorCode;

        switch (tier) {
            case TIER_1 -> {
                // Tier 1: Azul claro
                bgColor = isSelected ? 0x803FB8FF : (isHovered ? 0x603FB8FF : 0x403FB8FF);
                borderColor = isSelected ? 0xFF3FB8FF : (isHovered ? 0xFF3FB8FF : 0x603FB8FF);
                textColor = isSelected ? 0xFF3FB8FF : (isHovered ? 0xFFFFFFFF : 0xFF88DDFF);
                colorCode = isSelected ? "§b" : "§7";
            }
            case TIER_2 -> {
                // Tier 2: Naranja/Amarillo
                bgColor = isSelected ? 0x80FFB74D : (isHovered ? 0x60FFB74D : 0x40FFB74D);
                borderColor = isSelected ? 0xFFFFB74D : (isHovered ? 0xFFFFB74D : 0x60FFB74D);
                textColor = isSelected ? 0xFFFFB74D : (isHovered ? 0xFFFFFFFF : 0xFFFFDD88);
                colorCode = isSelected ? "§6" : "§7";
            }
            case TIER_3 -> {
                // Tier 3: Rojo/Magenta
                bgColor = isSelected ? 0x80FF5252 : (isHovered ? 0x60FF5252 : 0x40FF5252);
                borderColor = isSelected ? 0xFFFF5252 : (isHovered ? 0xFFFF5252 : 0x60FF5252);
                textColor = isSelected ? 0xFFFF5252 : (isHovered ? 0xFFFFFFFF : 0xFFFF8888);
                colorCode = isSelected ? "§c" : "§7";
            }
            case COMBO -> {
                // Tier Combo: Morado
                bgColor = isSelected ? 0x80AA00FF : (isHovered ? 0x60AA00FF : 0x40AA00FF);
                borderColor = isSelected ? 0xFFAA00FF : (isHovered ? 0xFFAA00FF : 0x60AA00FF);
                textColor = isSelected ? 0xFFFF00FF : (isHovered ? 0xFFFFFFFF : 0xFFDD88FF);
                colorCode = isSelected ? "§d" : "§7";
            }
            default -> {
                // ALL: Color neutral/dorado
                bgColor = isSelected ? 0x80FFD700 : (isHovered ? 0x60FFD700 : 0x40000000);
                borderColor = isSelected ? ScreenRenderer.COLOR_BORDER_HIGHLIGHT : (isHovered ? ScreenRenderer.COLOR_BORDER : 0x60666666);
                textColor = isSelected ? 0xFFFFD700 : (isHovered ? 0xFFFFFFFF : 0xFFCCCCCC);
                colorCode = isSelected ? "§6" : "§7";
            }
        }

        graphics.fill(x, y, x + width, y + height, bgColor);

        // Borde
        int borderThickness = isSelected ? 2 : 1;
        graphics.fill(x, y, x + width, y + borderThickness, borderColor);
        graphics.fill(x, y + height - borderThickness, x + width, y + height, borderColor);
        graphics.fill(x, y, x + borderThickness, y + height, borderColor);
        graphics.fill(x + width - borderThickness, y, x + width, y + height, borderColor);

        // Texto
        String text = colorCode + tier.getDisplayName();

        int textWidth = font.width(tier.getDisplayName());
        int textX = x + (width / 2) - (textWidth / 2);
        int textY = y + (height / 2) - 4;

        graphics.drawString(font, text, textX + 1, textY + 1, 0x80000000, false);
        graphics.drawString(font, text, textX, textY, textColor, false);
    }

    /**
     * Renderiza un card de categoría con formato similar a StatCard
     * Muestra el nombre de la categoría y contadores de perks por tier
     */
    private void renderCategoryCard(GuiGraphics graphics, PerkCategory category, int x, int y, int width, int height, boolean isSelected, boolean isHovered) {
        boolean isTiny = layout.isTinyScreen();
        boolean isSmall = layout.isSmallScreen();

        // Fondo base sutil
        if (!isTiny) {
            int baseBg = isSmall ? 0x15202020 : 0x10101010;
            graphics.fill(x, y, x + width, y + height, baseBg);
        }

        // Fondo hover/selected
        if (isHovered || isSelected) {
            int bgColor = isSelected ? 0x30FFD700 : (0x30FFD700 & 0x20FFFFFF);
            graphics.fill(x, y, x + width, y + height, bgColor);
        }

        // Borde si está seleccionado
        if (isSelected) {
            drawCardBorder(graphics, x, y, width, height, COLOR_BORDER_HIGHLIGHT);
        } else if (isHovered && isSmall) {
            drawCardBorder(graphics, x, y, width, height, 0xFF886622);
        }

        // Calcular perks por tier para esta categoría
        int[] tierCounts = new int[4]; // [T1, T2, T3, Combo]
        int[] tierTotals = new int[4];

        if (category == PerkCategory.COMBO) {
            // Para categoría COMBO: contar solo combo perks
            List<Perk> comboPerks = perkService.getComboPerks();
            tierTotals[3] = comboPerks.size();
            tierCounts[3] = (int) comboPerks.stream().filter(p -> stats.hasPerk(p.getId())).count();
        } else if (category.getStat() != null) {
            // Para otras categorías: contar por tier normal
            PerkTree tree = perkService.getPerkTree(category.getStat());
            if (tree != null) {
                List<Perk> allPerks = tree.getAllPerks();
                for (Perk perk : allPerks) {
                    boolean unlocked = stats.hasPerk(perk.getId());
                    switch (perk.getTier()) {
                        case TIER_1:
                            tierTotals[0]++;
                            if (unlocked) tierCounts[0]++;
                            break;
                        case TIER_2:
                            tierTotals[1]++;
                            if (unlocked) tierCounts[1]++;
                            break;
                        case TIER_3:
                            tierTotals[2]++;
                            if (unlocked) tierCounts[2]++;
                            break;
                        case COMBO:
                            tierTotals[3]++;
                            if (unlocked) tierCounts[3]++;
                            break;
                    }
                }
            }
        }

        int padding = isTiny ? 5 : (isSmall ? 4 : 5);
        int lineHeight = isTiny ? 11 : (isSmall ? 13 : 15);

        // PRIMERA LÍNEA: Nombre de la categoría a la izquierda, total desbloqueado/total a la derecha
        String name = category.getDisplayName();
        graphics.drawString(font, name, x + padding, y + padding, COLOR_TEXT_PRIMARY);

        // Calcular total de perks desbloqueadas para esta categoría
        int totalUnlocked = 0;
        int totalPerks = 0;
        for (int i = 0; i < 4; i++) {
            totalUnlocked += tierCounts[i];
            totalPerks += tierTotals[i];
        }

        // Mostrar total a la derecha (formato: desb/total)
        String totalText = totalUnlocked + "/" + totalPerks;
        String displayTotal = "§7" + totalText;
        int totalWidth = font.width(totalText);
        int totalX = x + width - totalWidth - padding;
        graphics.drawString(font, displayTotal, totalX, y + padding, COLOR_TEXT_SECONDARY);

        // SEGUNDA LÍNEA: Solo en pantallas normales/pequeñas (no tiny)
        if (!isTiny) {
            // Contadores de perks por tier distribuidos con mejor espacio
            // Primero a la izquierda, último a la derecha, los del medio distribuidos
            int counterY = y + padding + lineHeight + (isSmall ? 4 : 5);

            // Colores por tier: T1 (azul), T2 (naranja), T3 (rojo), Combo (morado)
            String[] tierColors = {"§b", "§6", "§c", "§d"};

            for (int i = 0; i < 4; i++) {
                String color = tierColors[i];
                String countText = tierCounts[i] + "/" + tierTotals[i];
                String count = color + countText;
                int textWidth = font.width(countText);
                int posX;

                if (i == 0) {
                    // Primero: alineado a la izquierda
                    posX = x + padding;
                } else if (i == 3) {
                    // Último: alineado a la derecha
                    posX = x + width - textWidth - padding;
                } else {
                    // Los del medio: distribuir en el espacio restante
                    int contentWidth = width - (padding * 2);
                    int sectionWidth = contentWidth / 3; // Dividir en 3 partes el espacio
                    posX = x + padding + (i * sectionWidth) - (textWidth / 2);
                }

                graphics.drawString(font, count, posX, counterY, COLOR_TEXT_SECONDARY);
            }
        }
    }

    /**
     * Dibuja solo bordes superior e inferior (igual que StatCard)
     * Los bordes están exactamente en los límites del card
     */
    private void drawCardBorder(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        // Borde superior: justo en el borde superior del card
        graphics.fill(x, y, x + width, y + 1, color);
        // Borde inferior: justo en el borde inferior del card
        graphics.fill(x, y + height - 1, x + width, y + height, color);
    }

    private void renderPerks(GuiGraphics graphics, int mouseX, int mouseY) {
        int rightPanelLeft = layout.getRightPanelLeft();
        int topSectionBottom = layout.getTopSectionBottom();
        int startY = topSectionBottom + layout.scaled(CATEGORY_SECTION_TOP_OFFSET);
        int startX = rightPanelLeft + layout.scaled(PERK_LIST_LEFT_OFFSET);

        // Limpiar áreas de botones de perks
        perkButtonAreas.clear();

        // Si no hay perks para mostrar
        if (displayedPerks.isEmpty()) {
            String noPerksMsg = "§7No hay habilidades disponibles";
            int msgX = startX + layout.scaled(40);
            int msgY = startY + layout.scaled(50);
            graphics.drawString(this.font, noPerksMsg, msgX, msgY, COLOR_TEXT_SECONDARY, false);
            return;
        }

        // Aplicar scissor (clipping) para el área de scroll
        scrollablePanel.enableScissor(graphics);

        int index = 0;
        for (Perk perk : displayedPerks) {
            int y = startY + (index * layout.scaled(getPerkItemHeight() + getPerkItemPadding())) - scrollablePanel.getScrollOffset();

            // Solo renderizar si está visible (con los bounds del scrollablePanel)
            int clipY = topSectionBottom + layout.scaled(SCROLL_CLIP_TOP_OFFSET);
            int clipHeight = layout.getGuiHeight() - layout.scaled(layout.getTopSectionHeight()) - layout.scaled(SCROLL_CLIP_BOTTOM_OFFSET);

            if (y + layout.scaled(getPerkItemHeight()) >= clipY && y <= clipY + clipHeight) {
                renderPerk(graphics, perk, startX, y, mouseX, mouseY);
            }

            index++;
        }

        scrollablePanel.disableScissor(graphics);

        // Renderizar la barra de scroll usando el ScrollablePanel
        scrollablePanel.renderScrollbar(graphics);
    }

    private void renderPerk(GuiGraphics graphics, Perk perk, int x, int y, int mouseX, int mouseY) {
        if (this.font == null) {
            return;
        }

        boolean isUnlocked = stats.hasPerk(perk.getId());
        boolean isCombo = perk.isCombo();
        int perkLevel = stats.getPerkLevel(perk.getId());
        boolean isTiny = layout.isTinyScreen();
        boolean isSmall = layout.isSmallScreen();
        PerkTier tier = perk.getTier();

        // Calcular ancho disponible considerando el espacio de la scrollbar
        int rightPanelWidth = layout.getRightPanelWidth();
        int itemWidth = rightPanelWidth - layout.scaled(PERK_LIST_LEFT_OFFSET * 2) - layout.scaled(SCROLLBAR_WIDTH + SCROLLBAR_PADDING);
        int itemHeight = layout.scaled(getPerkItemHeight());

        // Colores por tier y estado
        int bgColor;
        int borderColor;

        if (isUnlocked) {
            // Si está desbloqueado: verde
            bgColor = 0x50558B2F;
            borderColor = 0xFF7CB342;
        } else {
            // Color por tier
            switch (tier) {
                case TIER_1:
                    // Tier 1: Azul claro
                    bgColor = 0x303FB8FF;
                    borderColor = 0xFF3FB8FF;
                    break;
                case TIER_2:
                    // Tier 2: Naranja/Amarillo
                    bgColor = 0x30FFB74D;
                    borderColor = 0xFFFFB74D;
                    break;
                case TIER_3:
                    // Tier 3: Rojo/Magenta
                    bgColor = 0x30FF5252;
                    borderColor = 0xFFFF5252;
                    break;
                case COMBO:
                    // Tier Combo: Morado brillante
                    bgColor = 0x30AA00FF;
                    borderColor = 0xFFAA00FF;
                    break;
                default:
                    bgColor = 0x25FFFFFF;
                    borderColor = ScreenRenderer.COLOR_BORDER;
            }
        }

        // Background con color por tier
        graphics.fill(x, y, x + itemWidth, y + itemHeight, bgColor);

        // Borde izquierdo más grueso
        graphics.fill(x, y, x + 3, y + itemHeight, borderColor);
        // Resto del borde más sutil
        graphics.fill(x, y, x + itemWidth, y + 1, borderColor & 0x60FFFFFF);
        graphics.fill(x, y + itemHeight - 1, x + itemWidth, y + itemHeight, borderColor & 0x60FFFFFF);
        graphics.fill(x + itemWidth - 1, y, x + itemWidth, y + itemHeight, borderColor & 0x60FFFFFF);

        // Padding interno igual para todas las pantallas (no reducir en tiny)
        int contentPadding = isSmall ? 8 : 10;
        int contentX = x + layout.scaled(contentPadding);
        int contentY = y + layout.scaled(isSmall ? 6 : 8);

        // Nombre del perk con icono
        String icon = isUnlocked ? "§a✓" : (isCombo ? "§d★" : "§7○");
        String name = icon + " §f§l" + perk.getName();

        if (isUnlocked && perk.isUpgradeable()) {
            name += " §7[§e" + perkLevel + "§7/§e" + perk.getMaxLevel() + "§7]";
        }

        graphics.drawString(this.font, name, contentX, contentY, COLOR_TEXT_PRIMARY, false);

        // LineHeight igual para todas las pantallas (no reducir en tiny)
        int lineHeight = isSmall ? 13 : 14;

        // Tier y tipo más compacto
        String tierInfo;
        if (tier == PerkTier.COMBO) {
            // Para tier combo: mostrar en morado y negrita
            tierInfo = "§d§l" + tier.getDisplayName() + " §8• " + perk.getEffectType().getDisplayName();
        } else {
            // Para otros tiers: formato normal
            tierInfo = "§8" + tier.getDisplayName() + " §8• " + perk.getEffectType().getDisplayName();
        }
        graphics.drawString(this.font, tierInfo, contentX, contentY + lineHeight, COLOR_TEXT_SECONDARY, false);

        // Descripción - SIEMPRE mostrar con la misma longitud
        List<String> description = perk.getDescription();
        if (!description.isEmpty()) {
            String firstLine = description.get(0);
            // Máxima longitud igual para tiny: small=55, normal=60
            int maxLength = isSmall ? 55 : 60;
            if (firstLine.length() > maxLength) {
                firstLine = firstLine.substring(0, maxLength - 3) + "...";
            }
            graphics.drawString(this.font, "§7" + firstLine, contentX, contentY + (lineHeight * 2) + 2, COLOR_TEXT_SECONDARY, false);
        }

        // Mostrar requisitos de stats
        Map<StatType, Integer> requiredStats = perk.getRequirement().getRequiredStats();
        boolean hasStatRequirements = false;
        StringBuilder reqText = new StringBuilder("§7Requiere: ");

        if (!requiredStats.isEmpty()) {
            // Perks combo: tienen múltiples stats en requiredStats
            hasStatRequirements = true;
            int index = 0;
            for (Map.Entry<StatType, Integer> entry : requiredStats.entrySet()) {
                if (index > 0) reqText.append("§7, ");

                StatType stat = entry.getKey();
                int requiredLevel = entry.getValue();
                int playerLevel = stats.getLevel(stat);

                // Color según si cumple el requisito
                String color = playerLevel >= requiredLevel ? "§a" : "§c";

                // Símbolo del stat
                String symbol = StatCard.getStatSymbol(stat);

                reqText.append(symbol).append(" ").append(color).append(requiredLevel);
                index++;
            }
        } else if (perk.getAssociatedStat() != null) {
            // Perks normales: usar el nivel de desbloqueo del tier
            hasStatRequirements = true;
            StatType stat = perk.getAssociatedStat();
            int requiredLevel = tier.getUnlockLevel();
            int playerLevel = stats.getLevel(stat);

            // Color según si cumple el requisito
            String color = playerLevel >= requiredLevel ? "§a" : "§c";

            // Símbolo del stat
            String symbol = StatCard.getStatSymbol(stat);

            reqText.append(symbol).append(" ").append(color).append(requiredLevel);
        }

        if (hasStatRequirements) {
            graphics.drawString(this.font, reqText.toString(), contentX, contentY + (lineHeight * 3) + 2, COLOR_TEXT_SECONDARY, false);
        }

        // Línea divisoria sutil - con MÁS espacio arriba para la descripción y requisitos
        // Si tiene requisitos de stats, necesita más espacio
        int dividerY = contentY + (hasStatRequirements ?
            (isSmall ? 58 : 62) :
            (isSmall ? 46 : 50));
        graphics.fill(contentX, dividerY, x + itemWidth - layout.scaled(contentPadding), dividerY + 1, 0x40FFFFFF);

        // Costo en XP y botón integrado
        int cost = perk.getRequirement().getXpLevelCost();
        int playerXpLevel = minecraft.player != null ? minecraft.player.experienceLevel : 0;

        // Tamaños de botones - altura más pequeña y consistente
        int buttonWidth = layout.scaled(isTiny ? 85 : 70);
        int buttonHeight = isTiny ? layout.scaled(26) : 18; // Altura reducida sin escalar en pantallas normales

        if (isUnlocked) {
            // PERK YA DESBLOQUEADO
            boolean isPerkActive = stats.isPerkActive(perk.getId());
            boolean isUpgradeable = perk.isUpgradeable() && perkLevel < perk.getMaxLevel();

            // Botón TOGGLE - Parte superior derecha
            int toggleButtonX = x + itemWidth - buttonWidth - layout.scaled(contentPadding);
            int toggleButtonY = contentY;
            perkButtonAreas.add(new PerkButtonArea(perk, toggleButtonX, toggleButtonY, buttonWidth, buttonHeight, PerkButtonType.TOGGLE, true));
            renderToggleButton(graphics, toggleButtonX, toggleButtonY, buttonWidth, buttonHeight, isPerkActive, mouseX, mouseY);

            if (isUpgradeable) {
                int upgradeCost = cost;
                boolean canUpgrade = playerXpLevel >= upgradeCost;

                // Mostrar costo de upgrade en la parte inferior izquierda
                String upgradeCostText = "§7Costo: §e" + upgradeCost + " §7niveles";
                graphics.drawString(this.font, upgradeCostText, contentX, dividerY + 6, COLOR_TEXT_GOLD, false);

                // Botón UPGRADE - alineado al lado derecho del contenedor
                int upgradeButtonX = x + itemWidth - buttonWidth - layout.scaled(contentPadding);
                int upgradeButtonY = dividerY + 3;
                perkButtonAreas.add(new PerkButtonArea(perk, upgradeButtonX, upgradeButtonY, buttonWidth, buttonHeight, PerkButtonType.UPGRADE, canUpgrade));
                renderUpgradeButton(graphics, upgradeButtonX, upgradeButtonY, buttonWidth, buttonHeight, canUpgrade, mouseX, mouseY, perkLevel, perk.getMaxLevel());
            } else {
                // Sin upgrade disponible, solo mostrar estado
                String statusText = isPerkActive ? "§a✓ Activo" : "§c✗ Inactivo";
                graphics.drawString(this.font, statusText, contentX, dividerY + 6, isPerkActive ? 0xFF7CB342 : 0xFFFF5252, false);
            }
        } else {
            // PERK NO DESBLOQUEADO
            boolean canUnlock = perkService.canUnlock(stats, playerXpLevel, perk.getId());

            // Mostrar costo de desbloqueo en la parte inferior izquierda
            String costText = "§7Costo: §e" + cost + " §7niveles";
            graphics.drawString(this.font, costText, contentX, dividerY + 6, COLOR_TEXT_GOLD, false);

            // Botón UNLOCK - alineado al lado derecho del contenedor
            int unlockButtonX = x + itemWidth - buttonWidth - layout.scaled(contentPadding);
            int unlockButtonY = dividerY + 3;

            perkButtonAreas.add(new PerkButtonArea(perk, unlockButtonX, unlockButtonY, buttonWidth, buttonHeight, PerkButtonType.UNLOCK, canUnlock));
            renderUnlockButton(graphics, unlockButtonX, unlockButtonY, buttonWidth, buttonHeight, canUnlock, mouseX, mouseY);
        }
    }

    // ============================================
    // MÉTODOS HELPER PARA RENDERIZADO
    // ============================================

    /**
     * Verifica si el mouse está sobre un botón
     */
    private boolean isButtonHovered(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    /**
     * Dibuja el borde de un botón
     */
    private void drawButtonBorder(GuiGraphics graphics, int x, int y, int width, int height, int borderColor) {
        graphics.fill(x, y, x + width, y + 1, borderColor);
        graphics.fill(x, y + height - 1, x + width, y + height, borderColor);
        graphics.fill(x, y, x + 1, y + height, borderColor);
        graphics.fill(x + width - 1, y, x + width, y + height, borderColor);
    }

    /**
     * Calcula la posición X para centrar texto en un botón
     */
    private int getCenteredTextX(int buttonX, int buttonWidth, String text) {
        int textWidth = font.width(text);
        return buttonX + (buttonWidth / 2) - (textWidth / 2);
    }

    /**
     * Calcula la posición Y para centrar texto en un botón
     */
    private int getCenteredTextY(int buttonY, int buttonHeight) {
        return buttonY + (buttonHeight / 2) - TEXT_VERTICAL_OFFSET;
    }

    /**
     * Refresca la pantalla después de una acción (unlock, toggle, upgrade)
     */
    private void refreshScreenAfterAction(boolean withDelay) {
        if (withDelay) {
            minecraft.execute(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                reloadAndRefresh();
            });
        } else {
            reloadAndRefresh();
        }
    }

    /**
     * Recarga stats y refresca widgets
     */
    private void reloadAndRefresh() {
        this.stats = loadPlayerStats();
        updateDisplayedPerks(false);
        initWidgets();
    }

    // ============================================
    // RENDERIZADO DE BOTONES
    // ============================================

    private void renderUnlockButton(GuiGraphics graphics, int x, int y, int width, int height, boolean canUnlock, int mouseX, int mouseY) {
        boolean isHovered = isButtonHovered(x, y, width, height, mouseX, mouseY);

        // Background
        int bgColor = canUnlock ? (isHovered ? 0xA07CB342 : 0x807CB342) : 0x60666666;
        graphics.fill(x, y, x + width, y + height, bgColor);

        // Borde
        int borderColor = canUnlock ? (isHovered ? 0xFF7CB342 : 0xCC7CB342) : 0x80666666;
        drawButtonBorder(graphics, x, y, width, height, borderColor);

        // Texto
        String displayText = canUnlock ? "Desbloquear" : "Bloqueado";
        String text = canUnlock ? "§a" + displayText : "§7" + displayText;
        int textX = getCenteredTextX(x, width, displayText);
        int textY = getCenteredTextY(y, height);

        graphics.drawString(font, text, textX, textY, canUnlock ? 0xFFFFFFFF : 0xFF999999, false);
    }

    private void renderToggleButton(GuiGraphics graphics, int x, int y, int width, int height, boolean isActive, int mouseX, int mouseY) {
        boolean isHovered = isButtonHovered(x, y, width, height, mouseX, mouseY);

        // Background - Verde si está activo, Rojo si está inactivo
        int bgColor = isActive
            ? (isHovered ? 0xA07CB342 : 0x807CB342)  // Verde
            : (isHovered ? 0xA0FF5252 : 0x80FF5252);  // Rojo
        int borderColor = isActive
            ? (isHovered ? 0xFF7CB342 : 0xCC7CB342)
            : (isHovered ? 0xFFFF5252 : 0xCCFF5252);

        graphics.fill(x, y, x + width, y + height, bgColor);
        drawButtonBorder(graphics, x, y, width, height, borderColor);

        // Texto
        String displayText = isActive ? "Activo" : "Inactivo";
        String text = isActive ? "§a" + displayText : "§c" + displayText;
        int textX = getCenteredTextX(x, width, displayText);
        int textY = getCenteredTextY(y, height);

        graphics.drawString(font, text, textX, textY, 0xFFFFFFFF, false);
    }

    private void renderUpgradeButton(GuiGraphics graphics, int x, int y, int width, int height, boolean canUpgrade, int mouseX, int mouseY, int currentLevel, int maxLevel) {
        boolean isHovered = isButtonHovered(x, y, width, height, mouseX, mouseY);

        // Background - Dorado si puede mejorar, Gris si no
        int bgColor = canUpgrade
            ? (isHovered ? 0xA0FFD700 : 0x80FFD700)  // Dorado
            : 0x60666666;  // Gris
        int borderColor = canUpgrade
            ? (isHovered ? 0xFFFFD700 : 0xCCFFD700)
            : 0x80666666;

        graphics.fill(x, y, x + width, y + height, bgColor);
        drawButtonBorder(graphics, x, y, width, height, borderColor);

        // Texto
        String displayText = canUpgrade ? "Mejorar" : "Sin XP";
        String text = canUpgrade ? "§e" + displayText : "§c" + displayText;
        int textX = getCenteredTextX(x, width, displayText);
        int textY = getCenteredTextY(y, height);

        graphics.drawString(font, text, textX, textY, canUpgrade ? 0xFFFFFFFF : 0xFFFF5555, false);
    }

    // ============================================
    // INPUT
    // ============================================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Click izquierdo
            // Verificar clicks en la scrollbar primero usando ScrollablePanel
            if (scrollablePanel.handleMouseClicked(mouseX, mouseY, button)) {
                return true;
            }

            // Verificar clicks en botones de categorías
            for (CategoryButtonArea area : categoryButtonAreas) {
                if (area.contains((int) mouseX, (int) mouseY)) {
                    selectedCategory = area.category;
                    updateDisplayedPerks();
                    initWidgets();
                    return true;
                }
            }

            // Verificar clicks en botones de tier
            for (TierButtonArea area : tierButtonAreas) {
                if (area.contains((int) mouseX, (int) mouseY)) {
                    selectedTier = area.tier;
                    updateDisplayedPerks();
                    initWidgets();
                    return true;
                }
            }

            // Verificar clicks en botones de perks (unlock, toggle, upgrade)
            for (PerkButtonArea area : perkButtonAreas) {
                if (area.contains((int) mouseX, (int) mouseY)) {
                    if (area.isUnlockButton() && area.enabled) {
                        // Botón de desbloqueo
                        unlockPerk(area.perk);
                        return true;
                    } else if (area.isToggleButton()) {
                        // Botón de toggle (activar/desactivar)
                        togglePerk(area.perk);
                        return true;
                    } else if (area.isUpgradeButton() && area.enabled) {
                        // Botón de upgrade (mejorar nivel)
                        upgradePerk(area.perk);
                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Delegar al ScrollablePanel
        if (scrollablePanel.handleMouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // Delegar al ScrollablePanel
        if (scrollablePanel.handleMouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Delegar al ScrollablePanel
        if (scrollablePanel.handleMouseReleased(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}