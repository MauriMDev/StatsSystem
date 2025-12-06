package com.maurimdev.statssystem.client.ui.overlay;

/**
 * Constantes para el renderizado de las barras HUD (vida, armadura, comida).
 *
 * Centraliza todos los valores numéricos usados en el renderizado de barras
 * para facilitar su mantenimiento y configuración.
 */
public class HUDConstants {

    // ===== CONFIGURACIÓN GENERAL DE ICONOS =====

    /** Tamaño de un icono en píxeles (9x9) */
    public static final int ICON_SIZE = 9;

    /** Cantidad de iconos por fila antes de saltar a la siguiente */
    public static final int ICONS_PER_ROW = 10;

    /** Espaciado horizontal entre iconos en píxeles */
    public static final int ICON_SPACING = 8;

    // ===== POSICIONAMIENTO BASE (HUD SUPERIOR APILADO VERTICALMENTE) =====

    /** Posición Y desde la parte superior de la pantalla */
    public static final int TOP_Y_OFFSET = 10;

    /** Margen desde el borde izquierdo de la pantalla */
    public static final int LEFT_MARGIN = 10;

    /** Espaciado vertical entre barras diferentes (vida, armadura, comida) */
    public static final int BAR_VERTICAL_SPACING = 2;

    // ===== ESPACIADO ENTRE FILAS (DEPRECATED - Ya no se usan filas múltiples) =====

    /** @deprecated Ya no se usan filas múltiples, todo es horizontal */
    @Deprecated
    public static final int MAX_ROW_SPACING = 10;

    /** @deprecated Ya no se usan filas múltiples, todo es horizontal */
    @Deprecated
    public static final int MIN_FOOD_ROW_SPACING = 2;

    /** @deprecated Ya no se usan filas múltiples, todo es horizontal */
    @Deprecated
    public static final int MIN_ARMOR_ROW_SPACING = 2;

    /** @deprecated Ya no se usan filas múltiples, todo es horizontal */
    @Deprecated
    public static final int MIN_HEALTH_ROW_SPACING = 3;

    // ===== VALORES DE JUEGO =====

    /** Comida base vanilla (en puntos de comida, no muslitos) */
    public static final int BASE_FOOD_POINTS = 20;

    /** Puntos de comida que representa cada icono (muslito) */
    public static final int FOOD_POINTS_PER_ICON = 2;

    /** Puntos de armadura que representa cada icono (pechera) */
    public static final int ARMOR_POINTS_PER_ICON = 2;

    /** Vida base vanilla (en puntos de vida, no corazones) */
    public static final int BASE_HEALTH_POINTS = 20;

    /** Puntos de vida que representa cada icono (corazón) */
    public static final int HEALTH_POINTS_PER_ICON = 2;

    // ===== FÓRMULAS DE CÁLCULO =====

    /** Nivel de stat máximo para cálculos de bonificación */
    public static final double MAX_STAT_LEVEL = 64.0;

    /** Exponente para cálculo de comida por Agilidad: (nivel/64)^1.5 */
    public static final double FOOD_STAT_EXPONENT = 1.5;

    /** Multiplicador de comida bonus: resultado * 40 */
    public static final double FOOD_BONUS_MULTIPLIER = 40.0;

    // ===== MÉTODOS AUXILIARES =====

    /**
     * Calcula el ancho total en píxeles de una barra horizontal.
     *
     * @param totalIcons Cantidad total de iconos
     * @return Ancho total en píxeles
     */
    public static int calculateBarWidth(int totalIcons) {
        if (totalIcons == 0) return 0;
        return (totalIcons * ICON_SPACING) + ICON_SIZE;
    }

    /**
     * @deprecated Ya no se usan filas múltiples, todo es horizontal
     */
    @Deprecated
    public static int calculateRowSpacing(int rows, int minSpacing) {
        return Math.max(MAX_ROW_SPACING - (rows - 2), minSpacing);
    }

    /**
     * @deprecated Ya no se usan filas múltiples, todo es horizontal
     */
    @Deprecated
    public static int calculateRows(int totalIcons) {
        return (totalIcons + ICONS_PER_ROW - 1) / ICONS_PER_ROW;  // Ceiling division
    }

    /**
     * Convierte puntos de comida a cantidad de iconos (muslitos).
     *
     * @param foodPoints Puntos de comida
     * @return Cantidad de muslitos a mostrar
     */
    public static int foodPointsToIcons(int foodPoints) {
        return (foodPoints + 1) / 2;  // Ceiling division
    }

    /**
     * Convierte puntos de armadura a cantidad de iconos (pecheras).
     *
     * @param armorPoints Puntos de armadura
     * @return Cantidad de pecheras a mostrar
     */
    public static int armorPointsToIcons(int armorPoints) {
        return (armorPoints + 1) / 2;  // Ceiling division
    }
}
