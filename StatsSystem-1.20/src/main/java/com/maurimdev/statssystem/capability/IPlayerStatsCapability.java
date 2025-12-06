package com.maurimdev.statssystem.capability;
import com.maurimdev.statssystem.stats.PlayerStats;

/**
 * Interfaz que define las operaciones disponibles para las estadísticas del jugador
 */
public interface IPlayerStatsCapability {

    /**
     * Obtiene las estadísticas del jugador
     * @return La instancia de PlayerStats
     */
    PlayerStats getStats();

    /**
     * Establece las estadísticas del jugador
     * @param stats La nueva instancia de PlayerStats
     */
    void setStats(PlayerStats stats);

    /**
     * Copia las estadísticas desde otra capability
     * Útil cuando el jugador muere y respawnea
     * @param source La capability fuente desde donde copiar
     */
    void copyFrom(IPlayerStatsCapability source);
}