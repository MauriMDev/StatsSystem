package com.maurimdev.statssystem.infrastructure.integration;

import com.maurimdev.statssystem.StatsSystem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

/**
 * Integración OPCIONAL con Curios API
 * Solo se activa si el usuario tiene Curios instalado
 *
 * NOTA: En Curios 1.21.1+, los slots se registran mediante archivos JSON
 * en data/curios/slots/ en lugar de código Java
 */
public class CuriosIntegration {

    /**
     * Verifica si Curios está cargado
     */
    public static void register(IEventBus modEventBus) {
        if (isCuriosLoaded()) {
            StatsSystem.LOGGER.info("✓ Curios detectado - Slot 'soul_book' disponible");
            StatsSystem.LOGGER.info("  El slot se registra automáticamente desde data/curios/slots/soul_book.json");
        } else {
            StatsSystem.LOGGER.info("ℹ Curios no detectado - Mod funcionará sin slots adicionales");
        }
    }

    /**
     * Verifica si Curios está instalado
     */
    public static boolean isCuriosLoaded() {
        return ModList.get().isLoaded("curios");
    }
}