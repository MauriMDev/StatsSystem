package com.maurimdev.statssystem.core.service;

import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.core.domain.stats.StatType;

/**
 * Servicio que maneja toda la lógica de progresión de estadísticas
 * Separado del modelo de datos para mejor organización
 *
 * ⚙️ RESPONSABILIDADES:
 * - Calcular XP necesaria para subir de nivel (modo práctica)
 * - Calcular costo en niveles de Minecraft (modo XP)
 * - Procesar level ups automáticos
 * - Validar niveles y XP
 *
 * ⚖️ BALANCEO:
 * - Las constantes están en balance/PracticeBalanceConfig.java y balance/XPModeBalanceConfig.java
 * - Este servicio solo contiene la lógica de cálculo
 */
public class StatsProgressionService {

    /**
     * Calcula el bonus actual de una estadística con curva exponencial
     *
     * CURVA DE PROGRESIÓN:
     * - Early game (1-20):  Aumentos pequeños (~15-25% del potencial)
     * - Mid game (21-45):   Aumentos notables (~50-70% del potencial)
     * - Late game (46-64):  ¡ÉPICO! (~100% del potencial)
     *
     * Usa fórmula exponencial: (nivel/64)^1.8 para progresión no lineal
     */
    public static double calculateBonus(StatType stat, int level) {
        double bonusPerLevel = stat.getBonusPerLevel();

        // Curva exponencial: los primeros niveles dan poco, los últimos dan MUCHO
        // Exponente 1.8 = buen balance entre early (difícil) y late (épico)
        double levelRatio = level / 64.0; // 0.0 a 1.0
        double exponentialRatio = Math.pow(levelRatio, 1.8);

        // Multiplicador máximo a nivel 64 (diferente por stat para balance)
        double lateGameMultiplier = switch (stat) {
            case VITALITY -> 0.5;  // Conservador: ~64 HP extras a nivel 64 (32 corazones)
            case STRENGTH -> 3.0;  // Daño bruto incrementado
            case DEXTERITY -> 3.0; // Daño rápido incrementado
            case MINING -> 2.5;    // Velocidad de minado
            case AGILITY -> 2.5;   // Velocidad de movimiento
            case FARMING -> 2.0;   // Farming
        };

        double maxBonus = 64 * bonusPerLevel * lateGameMultiplier;

        return maxBonus * exponentialRatio;
    }

    /**
     * Verifica si una estadística ha alcanzado el soft cap
     */
    public static boolean hasReachedSoftCap(StatType stat, int level) {
        return level >= stat.getSoftCap();
    }

    /**
     * Calcula la XP necesaria para subir de nivel en modo práctica
     * Usa las mismas fórmulas que el sistema de experiencia de Minecraft
     */
    public static int calculatePracticeXPNeeded(int level) {
        if (level <= 16) {
            return level * level + 6 * level;
        } else if (level <= 31) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
    }

    /**
     * Añade XP a una estadística y procesa level ups automáticos
     * Retorna true si hubo al menos un level up
     */
    public static boolean addPracticeXP(PlayerStats stats, StatType stat, double amount) {
        double currentXP = stats.getXP(stat);
        int currentLevel = stats.getLevel(stat);

        // No se puede subir más allá del nivel máximo
        if (currentLevel >= 64) {
            return false;
        }

        currentXP += amount;
        int xpNeeded = calculatePracticeXPNeeded(currentLevel);

        boolean leveledUp = false;

        // Procesar múltiples level ups si hay suficiente XP
        while (currentXP >= xpNeeded && currentLevel < 64) {
            currentXP -= xpNeeded;
            currentLevel++;
            leveledUp = true;

            // Calcular nueva XP necesaria si no llegamos al máximo
            if (currentLevel < 64) {
                xpNeeded = calculatePracticeXPNeeded(currentLevel);
            }
        }

        // Actualizar stats
        stats.setXP(stat, currentXP);
        if (leveledUp) {
            stats.setLevel(stat, currentLevel);
        }

        return leveledUp;
    }

    /**
     * Intenta subir de nivel una estadística
     * Retorna true si fue exitoso
     */
    public static boolean levelUp(PlayerStats stats, StatType stat) {
        int currentLevel = stats.getLevel(stat);
        if (currentLevel >= 64) {
            return false;
        }
        stats.setLevel(stat, currentLevel + 1);
        return true;
    }

    /**
     * Valida que un nivel esté dentro del rango permitido (0-64)
     */
    public static int clampLevel(int level) {
        return Math.max(0, Math.min(64, level));
    }

    /**
     * Valida que la XP no sea negativa
     */
    public static double clampXP(double xp) {
        return Math.max(0, xp);
    }
}
