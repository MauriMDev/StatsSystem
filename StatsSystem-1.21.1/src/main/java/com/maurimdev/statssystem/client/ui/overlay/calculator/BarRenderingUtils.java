package com.maurimdev.statssystem.client.ui.overlay.calculator;

import com.maurimdev.statssystem.client.ui.overlay.HUDConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Utilidades compartidas para renderizar barras HUD con múltiples filas.
 *
 * Proporciona métodos comunes para el renderizado de iconos en barras como
 * comida, armadura y vida, evitando duplicación de código.
 */
public class BarRenderingUtils {

    /**
     * Renderiza una barra completa de iconos con soporte para múltiples filas.
     *
     * @param graphics      Contexto de renderizado
     * @param baseX         Posición X base (punto de inicio del renderizado)
     * @param baseY         Posición Y base (línea base de la primera fila)
     * @param totalIcons    Cantidad total de iconos a mostrar
     * @param currentValue  Valor actual (determina cuántos iconos están llenos)
     * @param rowSpacing    Espaciado en píxeles entre filas
     * @param emptySprite   Sprite para iconos vacíos
     * @param halfSprite    Sprite para iconos medio llenos
     * @param fullSprite    Sprite para iconos llenos
     * @param direction     Dirección de renderizado: 1 = izquierda a derecha, -1 = derecha a izquierda
     */
    public static void renderMultiRowBar(
            GuiGraphics graphics,
            int baseX,
            int baseY,
            int totalIcons,
            int currentValue,
            int rowSpacing,
            ResourceLocation emptySprite,
            ResourceLocation halfSprite,
            ResourceLocation fullSprite,
            int direction
    ) {
        int rows = HUDConstants.calculateRows(totalIcons);

        RenderSystem.enableBlend();

        // Renderizar cada fila en orden inverso (las superiores se dibujan primero)
        for (int row = rows - 1; row >= 0; row--) {
            int rowY = baseY - (row * rowSpacing);
            int iconsInRow = Math.min(HUDConstants.ICONS_PER_ROW, totalIcons - (row * HUDConstants.ICONS_PER_ROW));

            renderBarRow(
                    graphics,
                    baseX,
                    rowY,
                    row,
                    iconsInRow,
                    currentValue,
                    emptySprite,
                    halfSprite,
                    fullSprite,
                    direction
            );
        }

        RenderSystem.disableBlend();
    }

    /**
     * Renderiza una fila individual de iconos.
     *
     * @param graphics      Contexto de renderizado
     * @param baseX         Posición X base de la fila
     * @param rowY          Posición Y de esta fila
     * @param rowIndex      Índice de la fila (0 = primera fila)
     * @param iconsInRow    Cantidad de iconos en esta fila
     * @param currentValue  Valor actual (determina cuántos iconos están llenos)
     * @param emptySprite   Sprite para iconos vacíos
     * @param halfSprite    Sprite para iconos medio llenos
     * @param fullSprite    Sprite para iconos llenos
     * @param direction     Dirección: 1 = izquierda a derecha, -1 = derecha a izquierda
     */
    private static void renderBarRow(
            GuiGraphics graphics,
            int baseX,
            int rowY,
            int rowIndex,
            int iconsInRow,
            int currentValue,
            ResourceLocation emptySprite,
            ResourceLocation halfSprite,
            ResourceLocation fullSprite,
            int direction
    ) {
        // PASO 1: Dibujar todos los iconos vacíos (background)
        for (int i = 0; i < iconsInRow; i++) {
            int iconX = calculateIconX(baseX, i, direction);
            graphics.blitSprite(emptySprite, iconX, rowY, HUDConstants.ICON_SIZE, HUDConstants.ICON_SIZE);
        }

        // PASO 2: Dibujar iconos llenos/medios encima (foreground)
        for (int i = 0; i < iconsInRow; i++) {
            int iconX = calculateIconX(baseX, i, direction);
            int iconIndex = (rowIndex * HUDConstants.ICONS_PER_ROW) + i;

            // Determinar el sprite según el valor actual
            ResourceLocation sprite = determineSprite(iconIndex, currentValue, halfSprite, fullSprite);
            if (sprite != null) {
                graphics.blitSprite(sprite, iconX, rowY, HUDConstants.ICON_SIZE, HUDConstants.ICON_SIZE);
            }
        }
    }

    /**
     * Calcula la posición X de un icono en una fila.
     *
     * @param baseX     Posición X base
     * @param index     Índice del icono en la fila
     * @param direction Dirección: 1 = izquierda a derecha, -1 = derecha a izquierda
     * @return Posición X del icono
     */
    private static int calculateIconX(int baseX, int index, int direction) {
        if (direction > 0) {
            // Izquierda a derecha: avanzar positivamente
            return baseX + (index * HUDConstants.ICON_SPACING);
        } else {
            // Derecha a izquierda: avanzar negativamente
            return baseX - (index * HUDConstants.ICON_SPACING) - HUDConstants.ICON_SIZE;
        }
    }

    /**
     * Determina qué sprite usar según el valor actual y el índice del icono.
     *
     * @param iconIndex   Índice del icono
     * @param currentValue Valor actual (en puntos, no iconos)
     * @param halfSprite  Sprite para medio icono
     * @param fullSprite  Sprite para icono lleno
     * @return Sprite a usar, o null si debe quedarse vacío
     */
    private static ResourceLocation determineSprite(
            int iconIndex,
            int currentValue,
            ResourceLocation halfSprite,
            ResourceLocation fullSprite
    ) {
        // Cada icono representa 2 puntos (comida, armadura, vida)
        int valueForThisIcon = iconIndex * 2;

        if (currentValue > valueForThisIcon + 1) {
            // Icono lleno (ambos puntos ocupados)
            return fullSprite;
        } else if (currentValue == valueForThisIcon + 1) {
            // Medio icono (solo primer punto ocupado)
            return halfSprite;
        } else {
            // Vacío
            return null;
        }
    }

    /**
     * Calcula la posición Y de la fila más alta de una barra.
     *
     * @param baseY      Posición Y base (primera fila)
     * @param rows       Número total de filas
     * @param rowSpacing Espaciado entre filas
     * @return Posición Y de la fila superior
     */
    public static int calculateTopRowY(int baseY, int rows, int rowSpacing) {
        return baseY - ((rows - 1) * rowSpacing);
    }

    /**
     * Renderiza una barra horizontal completamente lineal (sin límite de iconos por fila).
     *
     * Este método es ideal para barras en la parte superior de la pantalla donde
     * todos los iconos se muestran en una sola fila horizontal.
     *
     * @param graphics      Contexto de renderizado
     * @param x             Posición X inicial
     * @param y             Posición Y
     * @param totalIcons    Cantidad total de iconos a mostrar
     * @param currentValue  Valor actual (determina cuántos iconos están llenos)
     * @param emptySprite   Sprite para iconos vacíos
     * @param halfSprite    Sprite para iconos medio llenos
     * @param fullSprite    Sprite para iconos llenos
     * @return Ancho total de la barra renderizada (en píxeles)
     */
    public static int renderHorizontalBar(
            GuiGraphics graphics,
            int x,
            int y,
            int totalIcons,
            int currentValue,
            ResourceLocation emptySprite,
            ResourceLocation halfSprite,
            ResourceLocation fullSprite
    ) {
        if (totalIcons <= 0) return 0;

        RenderSystem.enableBlend();

        // PASO 1: Dibujar todos los iconos vacíos (background)
        for (int i = 0; i < totalIcons; i++) {
            int iconX = x + (i * HUDConstants.ICON_SPACING);
            graphics.blitSprite(emptySprite, iconX, y, HUDConstants.ICON_SIZE, HUDConstants.ICON_SIZE);
        }

        // PASO 2: Dibujar iconos llenos/medios encima (foreground)
        for (int i = 0; i < totalIcons; i++) {
            int iconX = x + (i * HUDConstants.ICON_SPACING);

            // Determinar el sprite según el valor actual
            ResourceLocation sprite = determineSprite(i, currentValue, halfSprite, fullSprite);
            if (sprite != null) {
                graphics.blitSprite(sprite, iconX, y, HUDConstants.ICON_SIZE, HUDConstants.ICON_SIZE);
            }
        }

        RenderSystem.disableBlend();

        // Retornar el ancho total de la barra
        return HUDConstants.calculateBarWidth(totalIcons);
    }
}
