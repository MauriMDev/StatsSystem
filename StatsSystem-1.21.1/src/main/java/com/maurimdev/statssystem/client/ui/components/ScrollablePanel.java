package com.maurimdev.statssystem.client.ui.components;

import com.maurimdev.statssystem.client.ui.rendering.SystemLayout;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Supplier;

/**
 * Componente reutilizable para manejar scroll en paneles
 * Maneja tanto scroll con rueda del ratón como con barra visual
 *
 * Características mejoradas:
 * - Cálculo automático de altura de contenido mediante Supplier
 * - Aparición automática del scrollbar solo cuando es necesario
 * - El scrollbar resta espacio al área de contenido automáticamente
 * - API simplificada para facilitar su uso
 */
public class ScrollablePanel {

    // ============================================
    // CONSTANTES
    // ============================================

    private static final int SCROLLBAR_WIDTH = 8;
    private static final int SCROLLBAR_PADDING = 4;
    private static final int DEFAULT_SCROLL_SPEED = 40;

    // Colores
    private static final int COLOR_SCROLLBAR_TRACK = 0x30000000;
    private static final int COLOR_SCROLLBAR_THUMB = 0xAAFFD700;
    private static final int COLOR_SCROLLBAR_THUMB_HOVER = 0xCCFFD700;
    private static final int COLOR_SCROLLBAR_BORDER = 0xFFFFD700;

    // ============================================
    // DATOS
    // ============================================

    private final SystemLayout layout;

    // Área del panel (bounds completos SIN scrollbar)
    private int fullPanelX;
    private int fullPanelY;
    private int fullPanelWidth;
    private int fullPanelHeight;

    // Scroll state
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;

    // Altura de contenido (puede ser calculada automáticamente)
    private Supplier<Integer> contentHeightSupplier = null;
    private int manualContentHeight = 0;

    // Velocidad de scroll (en píxeles por tick de rueda)
    private int scrollSpeed;

    // Arrastre de scrollbar
    private boolean isDraggingScrollbar = false;
    private int dragStartY = 0;
    private int dragStartScrollOffset = 0;

    // ============================================
    // CONSTRUCTOR
    // ============================================

    public ScrollablePanel(SystemLayout layout) {
        this.layout = layout;
        this.scrollSpeed = DEFAULT_SCROLL_SPEED;
    }

    // ============================================
    // CONFIGURACIÓN
    // ============================================

    /**
     * Configura el área del panel scrolleable (área completa, el scrollbar restará espacio automáticamente)
     */
    public void setBounds(int x, int y, int width, int height) {
        this.fullPanelX = x;
        this.fullPanelY = y;
        this.fullPanelWidth = width;
        this.fullPanelHeight = height;
        recalculateMaxScroll();
    }

    /**
     * Configura la altura del contenido manualmente
     * @deprecated Usar setContentHeightSupplier para cálculo automático
     */
    @Deprecated
    public void setContentHeight(int contentHeight) {
        this.manualContentHeight = contentHeight;
        this.contentHeightSupplier = null;
        recalculateMaxScroll();
    }

    /**
     * Configura un proveedor de altura de contenido que se calculará automáticamente
     * Esto permite que el contenido determine dinámicamente cuándo necesita scroll
     */
    public void setContentHeightSupplier(Supplier<Integer> supplier) {
        this.contentHeightSupplier = supplier;
        recalculateMaxScroll();
    }

    /**
     * Configura la velocidad de scroll (píxeles por tick)
     */
    public void setScrollSpeed(int speed) {
        this.scrollSpeed = speed;
    }

    /**
     * Resetea el scroll a la posición inicial
     */
    public void resetScroll() {
        scrollOffset = 0;
    }

    // ============================================
    // CÁLCULOS
    // ============================================

    /**
     * Obtiene la altura actual del contenido total (puede ser mayor que el área visible)
     */
    private int getTotalContentHeight() {
        if (contentHeightSupplier != null) {
            return contentHeightSupplier.get();
        }
        return manualContentHeight;
    }

    /**
     * Recalcula el scroll máximo basado en el contenido actual
     */
    private void recalculateMaxScroll() {
        int contentHeight = getTotalContentHeight();
        maxScrollOffset = Math.max(0, contentHeight - fullPanelHeight);
        // Asegurar que scrollOffset esté dentro de los límites
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
    }

    // ============================================
    // GETTERS
    // ============================================

    public int getScrollOffset() {
        return scrollOffset;
    }

    public int getMaxScrollOffset() {
        return maxScrollOffset;
    }

    /**
     * Indica si hay contenido scrolleable (el contenido es más alto que el área visible)
     */
    public boolean hasScrollableContent() {
        recalculateMaxScroll(); // Recalcular cada vez para detectar cambios dinámicos
        return maxScrollOffset > 0;
    }

    public boolean isDragging() {
        return isDraggingScrollbar;
    }

    /**
     * Obtiene el ancho del área de contenido (restando el scrollbar si es necesario)
     * Este es el ancho que debes usar para renderizar tu contenido
     */
    public int getContentWidth() {
        if (hasScrollableContent()) {
            // Si hay scroll, restar el espacio total de la scrollbar
            // SCROLLBAR_WIDTH (8) + SCROLLBAR_PADDING * 2 (4*2=8) = 16 píxeles totales
            int scrollbarTotalSpace = layout.scaled(SCROLLBAR_WIDTH + SCROLLBAR_PADDING * 2);
            return fullPanelWidth - scrollbarTotalSpace;
        } else {
            // Si NO hay scroll, usar el ancho completo
            return fullPanelWidth;
        }
    }

    /**
     * Obtiene la posición X del contenido
     */
    public int getContentX() {
        return fullPanelX;
    }

    /**
     * Obtiene la posición Y del contenido
     */
    public int getContentY() {
        return fullPanelY;
    }

    /**
     * Obtiene la altura del área visible
     */
    public int getContentHeight() {
        return fullPanelHeight;
    }

    // ============================================
    // INFO DE SCROLLBAR
    // ============================================

    private record ScrollbarInfo(int x, int y, int width, int height, int thumbY, int thumbHeight) {
        boolean isMouseOverThumb(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width &&
                    mouseY >= thumbY && mouseY <= thumbY + thumbHeight;
        }

        boolean isMouseOverTrack(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width &&
                    mouseY >= y && mouseY <= y + height;
        }
    }

    private ScrollbarInfo getScrollbarInfo() {
        if (!hasScrollableContent()) {
            return null;
        }

        // Posición X del scrollbar: justo después del área de contenido
        // El contenido ocupa desde fullPanelX hasta (fullPanelX + getContentWidth())
        // El scrollbar empieza justo después, con un pequeño padding
        int contentWidth = getContentWidth();
        int scrollbarX = fullPanelX + contentWidth + layout.scaled(SCROLLBAR_PADDING);

        // Calcular tamaño y posición del thumb de la scrollbar
        int totalContentHeight = getTotalContentHeight();
        float visibleRatio = (float) fullPanelHeight / totalContentHeight;
        int thumbHeight = Math.max(20, (int) (fullPanelHeight * visibleRatio));

        float scrollProgress = maxScrollOffset > 0 ? (float) scrollOffset / maxScrollOffset : 0f;
        int thumbY = fullPanelY + (int) ((fullPanelHeight - thumbHeight) * scrollProgress);

        return new ScrollbarInfo(
                scrollbarX,
                fullPanelY,
                layout.scaled(SCROLLBAR_WIDTH),
                fullPanelHeight,
                thumbY,
                thumbHeight
        );
    }

    // ============================================
    // RENDERIZADO
    // ============================================

    /**
     * Renderiza la barra de scroll si hay contenido scrolleable
     */
    public void renderScrollbar(GuiGraphics graphics) {
        ScrollbarInfo info = getScrollbarInfo();
        if (info == null) {
            return;
        }

        // Renderizar track (fondo de la scrollbar) - más sutil
        graphics.fill(
                info.x,
                info.y,
                info.x + info.width,
                info.y + info.height,
                COLOR_SCROLLBAR_TRACK
        );

        // Renderizar thumb (la parte que se mueve)
        int thumbColor = isDraggingScrollbar ? COLOR_SCROLLBAR_THUMB_HOVER : COLOR_SCROLLBAR_THUMB;
        graphics.fill(
                info.x,
                info.thumbY,
                info.x + info.width,
                info.thumbY + info.thumbHeight,
                thumbColor
        );

        // Borde del thumb para mejor visibilidad
        graphics.fill(
                info.x,
                info.thumbY,
                info.x + info.width,
                info.thumbY + 1,
                COLOR_SCROLLBAR_BORDER
        );
        graphics.fill(
                info.x,
                info.thumbY + info.thumbHeight - 1,
                info.x + info.width,
                info.thumbY + info.thumbHeight,
                COLOR_SCROLLBAR_BORDER
        );
    }

    /**
     * Habilita el scissor (clipping) para el área del panel
     * El scissor solo cubre el área de contenido, NO el scrollbar
     */
    public void enableScissor(GuiGraphics graphics) {
        // El scissor debe cubrir solo el área de contenido
        // Si hay scrollbar, el scissor termina donde empieza el scrollbar
        int scissorWidth = getContentWidth();
        graphics.enableScissor(fullPanelX, fullPanelY, fullPanelX + scissorWidth, fullPanelY + fullPanelHeight);
    }

    /**
     * Deshabilita el scissor
     */
    public void disableScissor(GuiGraphics graphics) {
        graphics.disableScissor();
    }

    // ============================================
    // MANEJO DE EVENTOS
    // ============================================

    /**
     * Maneja el evento de scroll con la rueda del ratón
     * @return true si el evento fue manejado
     */
    public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!hasScrollableContent()) {
            return false;
        }

        // Verificar si el mouse está sobre el panel
        if (mouseX >= fullPanelX && mouseX <= fullPanelX + fullPanelWidth &&
                mouseY >= fullPanelY && mouseY <= fullPanelY + fullPanelHeight) {

            int oldScrollOffset = scrollOffset;
            scrollOffset -= (int)(scrollY * layout.scaled(scrollSpeed));
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));

            return scrollOffset != oldScrollOffset;
        }

        return false;
    }

    /**
     * Maneja el evento de clic del ratón
     * @return true si el evento fue manejado
     */
    public boolean handleMouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) { // Solo click izquierdo
            return false;
        }

        ScrollbarInfo info = getScrollbarInfo();
        if (info == null) {
            return false;
        }

        if (info.isMouseOverThumb((int) mouseX, (int) mouseY)) {
            // Clic en el thumb - iniciar arrastre
            isDraggingScrollbar = true;
            dragStartY = (int) mouseY;
            dragStartScrollOffset = scrollOffset;
            return true;
        } else if (info.isMouseOverTrack((int) mouseX, (int) mouseY)) {
            // Clic en el track - saltar a esa posición
            int relativeY = (int) mouseY - info.y;
            float scrollPercent = (float) relativeY / info.height;
            scrollOffset = (int) (maxScrollOffset * scrollPercent);
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
            return true;
        }

        return false;
    }

    /**
     * Maneja el evento de arrastre del ratón
     * @return true si el evento fue manejado
     */
    public boolean handleMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!isDraggingScrollbar || button != 0) {
            return false;
        }

        ScrollbarInfo info = getScrollbarInfo();
        if (info == null) {
            return false;
        }

        // Calcular el desplazamiento basado en cuánto se movió el mouse
        int deltaY = (int) mouseY - dragStartY;

        // Convertir el delta del mouse a delta de scroll
        int availableThumbSpace = info.height - info.thumbHeight;
        if (availableThumbSpace > 0) {
            float scrollPercent = (float) deltaY / availableThumbSpace;
            int newScrollOffset = dragStartScrollOffset + (int) (maxScrollOffset * scrollPercent);
            scrollOffset = Math.max(0, Math.min(newScrollOffset, maxScrollOffset));
        }

        return true;
    }

    /**
     * Maneja el evento de soltar el ratón
     * @return true si el evento fue manejado
     */
    public boolean handleMouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && isDraggingScrollbar) {
            isDraggingScrollbar = false;
            return true;
        }
        return false;
    }
}
