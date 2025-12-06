package com.maurimdev.statssystem.client.ui.overlay.calculator;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.client.ui.overlay.HUDConstants;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.core.domain.stats.StatType;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * Calculadora de valores relacionados con la barra de vida.
 *
 * Centraliza toda la lógica de cálculo de vida máxima basada en
 * las estadísticas del jugador (Vitalidad).
 */
public class HealthBarCalculator {

    /**
     * Calcula la vida máxima que puede tener el jugador según su nivel de Vitalidad.
     *
     * Fórmula: BASE_HEALTH + (vitalidad/64)^1.8 × 80
     *
     * Ejemplos:
     * - Vitalidad 0:  20 HP (10 corazones)
     * - Vitalidad 32: 60 HP (30 corazones)
     * - Vitalidad 64: 100 HP (50 corazones)
     *
     * @param player Jugador para el cual calcular
     * @return Puntos de vida máximos
     */
    public static float calculateMaxHealth(Player player) {
        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
            int vitalityLevel = stats.getLevel(StatType.VITALITY);

            // Calcular bonus de vida según vitalidad (mismo cálculo que StatBonusHandler)
            double levelRatio = vitalityLevel / 64.0;
            double exponentialRatio = Math.pow(levelRatio, 1.8);
            double hpBonus = exponentialRatio * 80.0;

            float maxHealth = HUDConstants.BASE_HEALTH_POINTS + (float) hpBonus;

            // Debug log
            if (vitalityLevel > 0) {
                StatsSystem.LOGGER.debug("Vitality Level: {}, Max Health Calculated: {}, HP Bonus: {}",
                    vitalityLevel, maxHealth, hpBonus);
            }

            return maxHealth;
        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error calculating max health by vitality", e);
            return HUDConstants.BASE_HEALTH_POINTS;  // Fallback a valor vanilla
        }
    }

    /**
     * Calcula cuántos corazones deben mostrarse en la barra de vida.
     *
     * Siempre muestra el máximo posible según la Vitalidad del jugador,
     * para que se vean todos los corazones vacíos disponibles.
     *
     * @param player Jugador
     * @return Cantidad de corazones a mostrar en la UI
     */
    public static int calculateDisplayHearts(Player player) {
        // Calcular el máximo de vida según Vitalidad
        float maxHealthByVitality = calculateMaxHealth(player);
        int absorption = Mth.ceil(player.getAbsorptionAmount());

        // Siempre mostrar corazones hasta el máximo por Vitalidad + absorción
        float healthToDisplay = maxHealthByVitality + absorption;
        int hearts = Mth.ceil(healthToDisplay / 2.0f);

        // Log cada 60 frames
        if (System.currentTimeMillis() % 3000 < 50) {
            StatsSystem.LOGGER.error("calculateDisplayHearts() - maxHealthByVitality: {}, absorption: {}, healthToDisplay: {}, hearts: {}",
                maxHealthByVitality, absorption, healthToDisplay, hearts);
        }

        return hearts;
    }
}
