package com.maurimdev.statssystem.client.ui.rendering;

/**
 * Layout centralizado del sistema de interfaz.
 * Gestiona todas las medidas, escalado y posicionamiento para las pantallas de Stats y Perks.
 * Organizado en secciones: Header y Body (panel izquierdo + panel derecho).
 */
public class SystemLayout {

    // ============================================
    // CONSTANTES BASE
    // ============================================

    // Dimensiones base y escalado
    public static final int BASE_GUI_WIDTH = 650;
    public static final int BASE_GUI_HEIGHT = 450;
    public static final float MAX_SCALE = 1.0f;
    public static final float MIN_SCALE = 0.45f;
    public static final float SCREEN_MARGIN_PERCENT = 0.95f;

    // Umbrales para detección de pantallas pequeñas
    private static final int SMALL_SCREEN_HEIGHT_THRESHOLD = 600;
    private static final int TINY_SCREEN_HEIGHT_THRESHOLD = 400;

    // ============================================
    // CONSTANTES: HEADER
    // ============================================

    // Header: Alturas adaptativas
    public static final int TOP_SECTION_HEIGHT_NORMAL = 70;
    public static final int TOP_SECTION_HEIGHT_COMPACT = 60;
    public static final int TOP_SECTION_HEIGHT_TINY = 45;

    // Header: Tabs adaptativos
    public static final int TAB_WIDTH_NORMAL = 150;
    public static final int TAB_HEIGHT_NORMAL = 35;
    public static final int TAB_WIDTH_SMALL = 120;
    public static final int TAB_HEIGHT_SMALL = 28;
    public static final int TAB_WIDTH_TINY = 80;   // Ajustado para nombres completos en español
    public static final int TAB_HEIGHT_TINY = 16;  // Reducido en altura para pantallas tiny

    // ============================================
    // CONSTANTES: BODY
    // ============================================

    // Body: Panel izquierdo (30% del ancho total)
    public static final int LEFT_PANEL_WIDTH_PERCENT = 30;

    // Body: Padding adaptativo
    public static final int PADDING_NORMAL = 12;
    public static final int PADDING_COMPACT = 8;
    public static final int PADDING_TINY = 4;

    // Body: Altura de items de stat (ajustada para layout de 2 líneas)
    public static final int STAT_ITEM_HEIGHT = 40;         // Reducido de 50: nombre + barra + padding
    public static final int STAT_ITEM_HEIGHT_COMPACT = 42; // Reducido de 52
    public static final int STAT_ITEM_HEIGHT_TINY = 54;    // Ajustado para barra más delgada: 5+11+2+5+5=28px (36 con margen)

    // Body: Espacio reservado para scrollbar
    public static final int SCROLLBAR_RESERVED_SPACE = 18;

    // ============================================
    // VARIABLES CALCULADAS
    // ============================================

    private final int screenWidth;
    private final int screenHeight;

    // Dimensiones y escala de la GUI
    private int guiWidth;
    private int guiHeight;
    private float guiScale;

    // Posición de la GUI en pantalla
    private int leftPos;
    private int topPos;

    // Estado de responsividad
    private boolean isSmallScreen;
    private boolean isTinyScreen;

    // Header
    private int currentTopSectionHeight;
    private int topSectionBottom;

    // Body
    private int currentPadding;
    private int currentStatHeight;
    private int leftPanelWidth;
    private int rightPanelLeft;

    // ============================================
    // CONSTRUCTOR
    // ============================================

    public SystemLayout(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        recalculate();
    }

    // ============================================
    // RECÁLCULO
    // ============================================

    /**
     * Recalcula todo el layout basándose en el tamaño de pantalla actual
     */
    public void recalculate() {
        calculateResponsiveScale();
        calculateResponsiveState();
        calculatePositions();
        calculateHeader();
        calculateBody();
    }

    /**
     * Calcula la escala responsiva basada en el tamaño de pantalla
     */
    private void calculateResponsiveScale() {
        int availableWidth = (int) (screenWidth * SCREEN_MARGIN_PERCENT);
        int availableHeight = (int) (screenHeight * SCREEN_MARGIN_PERCENT);

        float scaleX = (float) availableWidth / BASE_GUI_WIDTH;
        float scaleY = (float) availableHeight / BASE_GUI_HEIGHT;

        guiScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, Math.min(scaleX, scaleY)));
        guiWidth = (int) (BASE_GUI_WIDTH * guiScale);
        guiHeight = (int) (BASE_GUI_HEIGHT * guiScale);
    }

    /**
     * Detecta el tipo de pantalla (normal, small, tiny)
     */
    private void calculateResponsiveState() {
        this.isTinyScreen = screenHeight < TINY_SCREEN_HEIGHT_THRESHOLD;
        this.isSmallScreen = screenHeight < SMALL_SCREEN_HEIGHT_THRESHOLD && !isTinyScreen;
    }

    /**
     * Calcula las posiciones base de la GUI
     */
    private void calculatePositions() {
        this.leftPos = (screenWidth - guiWidth) / 2;
        this.topPos = (screenHeight - guiHeight) / 2;
    }

    // ============================================
    // HEADER: CÁLCULOS
    // ============================================

    /**
     * Calcula las medidas del header
     */
    private void calculateHeader() {
        // Usar valores adaptativos según tamaño
        if (isTinyScreen) {
            this.currentTopSectionHeight = TOP_SECTION_HEIGHT_TINY;
        } else if (isSmallScreen) {
            this.currentTopSectionHeight = TOP_SECTION_HEIGHT_COMPACT;
        } else {
            this.currentTopSectionHeight = TOP_SECTION_HEIGHT_NORMAL;
        }

        this.topSectionBottom = topPos + scaled(currentTopSectionHeight);
    }

    // ============================================
    // HEADER: GETTERS
    // ============================================

    public int getTopSectionHeight() {
        return currentTopSectionHeight;
    }

    public int getTopSectionBottom() {
        return topSectionBottom;
    }

    /**
     * Obtiene el ancho de tab adaptativo según el tamaño de pantalla
     */
    public int getTabWidth() {
        if (isTinyScreen) return TAB_WIDTH_TINY;
        if (isSmallScreen) return TAB_WIDTH_SMALL;
        return TAB_WIDTH_NORMAL;
    }

    /**
     * Obtiene la altura de tab adaptativa según el tamaño de pantalla
     */
    public int getTabHeight() {
        if (isTinyScreen) return TAB_HEIGHT_TINY;
        if (isSmallScreen) return TAB_HEIGHT_SMALL;
        return TAB_HEIGHT_NORMAL;
    }

    // ============================================
    // BODY: CÁLCULOS
    // ============================================

    /**
     * Calcula las medidas del body (paneles)
     */
    private void calculateBody() {
        // Padding adaptativo
        if (isTinyScreen) {
            this.currentPadding = PADDING_TINY;
            this.currentStatHeight = STAT_ITEM_HEIGHT_TINY;
        } else if (isSmallScreen) {
            this.currentPadding = PADDING_COMPACT;
            this.currentStatHeight = STAT_ITEM_HEIGHT_COMPACT;
        } else {
            this.currentPadding = PADDING_NORMAL;
            this.currentStatHeight = STAT_ITEM_HEIGHT;
        }

        // Paneles
        this.leftPanelWidth = (guiWidth * LEFT_PANEL_WIDTH_PERCENT) / 100;
        this.rightPanelLeft = leftPos + leftPanelWidth;
    }

    // ============================================
    // BODY: PANEL IZQUIERDO - GETTERS
    // ============================================

    public int getLeftPanelWidth() {
        return leftPanelWidth;
    }

    /**
     * Calcula la altura disponible para contenido scrolleable en el panel izquierdo
     */
    public int getLeftPanelContentHeight() {
        return guiHeight - scaled(currentTopSectionHeight) - scaled(currentPadding * 2);
    }

    /**
     * Calcula el ancho completo del panel izquierdo (sin restar el espacio de scrollbar)
     * Para usar en el cálculo del área de scroll
     */
    public int getLeftPanelFullWidth() {
        return leftPanelWidth - scaled(currentPadding * 2);
    }

    /**
     * Calcula el ancho de una tarjeta de stat (con espacio para scrollbar)
     */
    public int getStatCardWidth() {
        return leftPanelWidth - scaled(currentPadding * 2) - scaled(SCROLLBAR_RESERVED_SPACE);
    }

    /**
     * Calcula el alto de una tarjeta de stat
     */
    public int getStatCardHeight() {
        return scaled(currentStatHeight - (isTinyScreen ? 2 : 4));
    }

    /**
     * Calcula la posición X de una tarjeta de stat
     */
    public int getStatCardX() {
        return leftPos + scaled(currentPadding);
    }

    /**
     * Calcula la posición Y de un stat específico en la lista
     */
    public int getStatItemY(int index) {
        int startY = topSectionBottom + scaled(currentPadding + (isTinyScreen ? 15 : 25));
        int statHeight = scaled(currentStatHeight);
        return startY + (index * statHeight);
    }

    // ============================================
    // BODY: PANEL DERECHO - GETTERS
    // ============================================

    public int getRightPanelLeft() {
        return rightPanelLeft;
    }

    public int getRightPanelWidth() {
        return guiWidth - leftPanelWidth - 2;
    }

    /**
     * Calcula la altura disponible para contenido scrolleable en el panel derecho
     */
    public int getRightPanelContentHeight() {
        return guiHeight - scaled(currentTopSectionHeight) - scaled(currentPadding * 2);
    }

    // ============================================
    // GETTERS GENERALES
    // ============================================

    public int getGuiWidth() {
        return guiWidth;
    }

    public int getGuiHeight() {
        return guiHeight;
    }

    public float getGuiScale() {
        return guiScale;
    }

    public int getLeftPos() {
        return leftPos;
    }

    public int getTopPos() {
        return topPos;
    }

    public int getPadding() {
        return currentPadding;
    }

    public int getStatHeight() {
        return currentStatHeight;
    }

    public boolean isSmallScreen() {
        return isSmallScreen || isTinyScreen;
    }

    public boolean isTinyScreen() {
        return isTinyScreen;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    // ============================================
    // UTILIDADES
    // ============================================

    /**
     * Escala un valor base según la escala de la GUI
     */
    public int scaled(int baseValue) {
        return (int) (baseValue * guiScale);
    }
}
