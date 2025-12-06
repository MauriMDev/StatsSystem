package com.maurimdev.statssystem.capability;

import com.maurimdev.statssystem.stats.PlayerStats;

/**
 * Implementación de la capability que almacena las estadísticas del jugador
 * Esta clase es la que realmente GUARDA los datos en memoria
 */
public class PlayerStatsStorage implements IPlayerStatsCapability {

    // ============================================
    // ALMACENAMIENTO DE DATOS
    // ============================================

    /**
     * La instancia real de PlayerStats que contiene todos los datos
     * Se crea una nueva instancia vacía cuando se crea el Storage
     * Esta variable persiste mientras el jugador esté en el mundo
     */
    private PlayerStats stats = new PlayerStats();

    // ============================================
    // IMPLEMENTACIÓN DE MÉTODOS DE LA INTERFAZ
    // ============================================

    /**
     * Obtiene las estadísticas almacenadas
     * Este metodo es llamado cada vez que algo necesita leer las stats
     * Por ejemplo: GUI, cálculos de daño, comandos, etc.
     *
     * @return La instancia de PlayerStats con todos los datos
     */
    @Override
    public PlayerStats getStats() {
        return stats;
        // Devuelve la referencia directa a 'stats'
        // Cualquier modificación a este objeto afectará el original
    }

    /**
     * Establece/reemplaza las estadísticas almacenadas
     * Útil para resetear stats o cargar desde un backup
     *
     * @param stats La nueva instancia de PlayerStats a almacenar
     */
    @Override
    public void setStats(PlayerStats stats) {
        this.stats = stats;
        // 'this.stats' = la variable de clase (línea 17)
        // 'stats' = el parámetro recibido
        // Reemplaza completamente la instancia anterior
    }

    /**
     * Copia los datos desde otra capability
     * Este metodo es CRÍTICO cuando el jugador muere y respawnea
     *
     * Flujo al morir:
     * 1. Jugador muere → Forge crea nueva entidad jugador
     * 2. Nueva entidad = nuevas capabilities = datos vacíos
     * 3. Forge llama este metodo para copiar del jugador viejo al nuevo
     * 4. Las stats se preservan después de la muerte
     *
     * @param source La capability del jugador anterior (antes de morir)
     */
    @Override
    public void copyFrom(IPlayerStatsCapability source) {
        // Obtiene las stats de la fuente (jugador viejo)
        PlayerStats sourceStats = source.getStats();

        // Usa el metodo copyFrom() de PlayerStats para copiar todos los valores
        // Este metodo ya lo implementaste en PlayerStats.java (línea ~205)
        stats.copyFrom(sourceStats);

        // Ahora 'this.stats' tiene los mismos valores que 'sourceStats'
        // El jugador mantiene su progresión después de morir
    }
}