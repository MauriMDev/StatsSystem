package com.maurimdev.statssystem.client.ui.overlay.calculator;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.client.ui.overlay.HUDConstants;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.core.domain.stats.StatType;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import net.minecraft.world.entity.player.Player;

/**
 * Calculadora de valores relacionados con la barra de comida.
 *
 * Centraliza toda la lógica de cálculo de comida máxima basada en
 * las estadísticas del jugador (Agilidad).
 */
public class FoodBarCalculator {

    /**
     * Calcula la comida máxima que puede tener el jugador según su nivel de Agilidad.
     *
     * Fórmula: BASE_FOOD + (agilidad/64)^1.5 × 40
     *
     * Ejemplos:
     * - Agilidad 0:  20 puntos de comida (10 muslitos)
     * - Agilidad 32: 34 puntos de comida (17 muslitos)
     * - Agilidad 64: 60 puntos de comida (30 muslitos)
     *
     * @param player Jugador para el cual calcular
     * @return Puntos de comida máximos (no muslitos)
     */
    public static int calculateMaxFood(Player player) {
        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
            int agilityLevel = stats.getLevel(StatType.AGILITY);

            // Calcular bonus de comida según agilidad
            double levelRatio = agilityLevel / HUDConstants.MAX_STAT_LEVEL;
            double exponentialRatio = Math.pow(levelRatio, HUDConstants.FOOD_STAT_EXPONENT);
            int foodBonus = (int) (exponentialRatio * HUDConstants.FOOD_BONUS_MULTIPLIER);

            return HUDConstants.BASE_FOOD_POINTS + foodBonus;
        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error calculating max food by agility", e);
            return HUDConstants.BASE_FOOD_POINTS;  // Fallback a valor vanilla
        }
    }

    /**
     * Calcula cuántos muslitos deben mostrarse en la barra de comida.
     *
     * Se muestra el mayor entre:
     * - La comida actual del jugador
     * - El máximo de comida según la Agilidad
     *
     * Esto asegura que siempre se vean todos los muslitos llenos.
     *
     * @param player      Jugador
     * @param currentFood Nivel de comida actual (en puntos)
     * @return Cantidad de muslitos a mostrar en la UI
     */
    public static int calculateDisplayIcons(Player player, int currentFood) {
        int maxFood = calculateMaxFood(player);
        int foodToDisplay = Math.max(currentFood, maxFood);
        return HUDConstants.foodPointsToIcons(foodToDisplay);
    }
}
