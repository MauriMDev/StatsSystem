package com.maurimdev.statssystem.core.service;

import com.maurimdev.statssystem.core.domain.stats.DerivedAttribute;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.core.domain.stats.StatType;

/**
 * Servicio que calcula los valores de todos los atributos derivados
 * basándose en las estadísticas principales del jugador.
 *
 * Cada método de cálculo está documentado con su fórmula exacta.
 */
public class DerivedAttributesCalculator {

    // ============================================
    // VITALITY - VIDA MÁXIMA
    // ============================================

    /**
     * Calcula el bono de Vida Máxima desde VITALITY
     *
     * Fórmula: (nivel/64)^1.8 × 80 HP
     *
     * Early (1-20):  +2-8 HP
     * Mid (21-45):   +15-40 HP
     * Late (46-64):  +50-80 HP
     *
     * @param stats Estadísticas del jugador
     * @return HP extra otorgados por Vitality
     */
    public static double calculateMaxHealth(PlayerStats stats) {
        int vitalityLevel = stats.getLevel(StatType.VITALITY);
        double levelRatio = vitalityLevel / 64.0;
        double exponentialRatio = Math.pow(levelRatio, 1.8);
        return exponentialRatio * 80.0; // Máximo 80 HP extras a nivel 64
    }

    // ============================================
    // VITALITY - REGENERACIÓN DE VIDA
    // ============================================

    /**
     * Calcula la Regeneración de Vida SOLO desde perks y efectos
     *
     * IMPORTANTE: La regeneración de vida ya NO escala con el nivel de Vitality.
     * Solo se obtiene regeneración mediante:
     * - Perk: Regeneration (Vitality Tier 1) - +0.5/1.0/2.0 HP cada X segundos
     * - Perk: Nature Warrior (Combo) - +0.5 HP/s cerca de plantas
     * - Efectos de poción de Regeneración
     * - Otros perks que otorguen regeneración
     *
     * Este método ya NO se usa para calcular regeneración pasiva.
     * Se mantiene solo para compatibilidad y muestra en la UI.
     *
     * @param stats Estadísticas del jugador
     * @return 0.0 (la regeneración ahora viene solo de perks/efectos)
     */
    public static double calculateHealthRegen(PlayerStats stats) {
        // Ya no calculamos regeneración desde stats
        // La regeneración viene SOLO de perks y efectos de poción
        return 0.0;
    }

    // ============================================
    // STRENGTH - DAÑO DE ATAQUE
    // ============================================

    /**
     * Calcula el bono de Daño de Ataque desde STRENGTH
     *
     * Fórmula: (nivel/64)^1.8 × 8 DMG
     *
     * IMPORTANTE: Este bonus SOLO se aplica cuando el jugador NO tiene un arma equipada.
     * Cuando tiene un arma, el daño viene del weapon scaling.
     *
     * BALANCE:
     * - Puño nivel 64:            1 base + 8 bonus = 9 daño
     * - Espada Madera nivel 64:   4 base + 7 scaling = 11 daño
     * - Espada Piedra nivel 64:   5 base + 10 scaling = 15 daño
     * - Espada Hierro nivel 64:   6 base + 15 scaling = 21 daño
     * - Espada Oro nivel 64:      4 base + 14 scaling = 18 daño
     * - Espada Diamante nivel 64: 7 base + 30 scaling = 37 daño
     * - Espada Netherite nivel 64: 8 base + 35 scaling = 43 daño
     *
     * El puño es viable para early game, pero TODAS las armas son superiores.
     *
     * Early (1-20):  +0.15-0.8 daño (total: 1.15-1.8)
     * Mid (21-45):   +1.5-4.0 daño (total: 2.5-5.0)
     * Late (46-64):  +5-8 daño (total: 6-9, inferior a espada de madera)
     *
     * @param stats Estadísticas del jugador
     * @return Daño extra otorgado por Strength (solo para puño)
     */
    public static double calculateAttackDamage(PlayerStats stats) {
        int strengthLevel = stats.getLevel(StatType.STRENGTH);
        double levelRatio = strengthLevel / 64.0;
        double exponentialRatio = Math.pow(levelRatio, 1.8);
        return exponentialRatio * 8.0; // Máximo +8 daño a nivel 64 (balanceado)
    }

    // ============================================
    // STRENGTH - RESISTENCIA AL KNOCKBACK
    // ============================================

    /**
     * Calcula la Resistencia al Knockback desde STRENGTH
     *
     * Fórmula: nivel × 1.0%
     *
     * Nivel 1:  +1%
     * Nivel 32: +32%
     * Nivel 64: +64%
     *
     * @param stats Estadísticas del jugador
     * @return Porcentaje de resistencia al knockback (0.0 a 0.64)
     */
    public static double calculateKnockbackResistance(PlayerStats stats) {
        int strengthLevel = stats.getLevel(StatType.STRENGTH);
        return strengthLevel * 0.01; // +1% por nivel
    }

    // ============================================
    // DEXTERITY - PROBABILIDAD DE CRÍTICO
    // ============================================

    /**
     * Calcula la Probabilidad de Crítico desde DEXTERITY
     *
     * Fórmula: nivel × 0.5%
     *
     * Nivel 1:  +0.5%
     * Nivel 32: +16%
     * Nivel 64: +32%
     *
     * @param stats Estadísticas del jugador
     * @return Porcentaje de probabilidad de crítico (0.0 a 0.32)
     */
    public static double calculateCritChance(PlayerStats stats) {
        int dexterityLevel = stats.getLevel(StatType.DEXTERITY);
        return dexterityLevel * 0.005; // +0.5% por nivel
    }

    // ============================================
    // DEXTERITY - DAÑO CRÍTICO
    // ============================================

    /**
     * Calcula el Daño Crítico adicional desde DEXTERITY
     *
     * Fórmula: nivel × 1.0%
     *
     * Nivel 1:  +1%
     * Nivel 32: +32%
     * Nivel 64: +64%
     *
     * @param stats Estadísticas del jugador
     * @return Porcentaje de daño crítico adicional (0.0 a 0.64)
     */
    public static double calculateCritDamage(PlayerStats stats) {
        int dexterityLevel = stats.getLevel(StatType.DEXTERITY);
        return dexterityLevel * 0.01; // +1% por nivel
    }

    // ============================================
    // DEXTERITY - ALCANCE DE PROYECTILES
    // ============================================

    /**
     * Calcula el Alcance de Proyectiles desde DEXTERITY
     *
     * Fórmula: nivel × 0.5%
     *
     * Nivel 1:  +0.5%
     * Nivel 32: +16%
     * Nivel 64: +32%
     *
     * @param stats Estadísticas del jugador
     * @return Porcentaje de aumento de alcance (0.0 a 0.32)
     */
    public static double calculateProjectileRange(PlayerStats stats) {
        int dexterityLevel = stats.getLevel(StatType.DEXTERITY);
        return dexterityLevel * 0.005; // +0.5% por nivel
    }

    // ============================================
    // MINING - VELOCIDAD DE MINADO
    // ============================================

    /**
     * Calcula la Velocidad de Minado desde MINING
     *
     * Fórmula: floor(nivel / 3) × 1.0%
     *
     * Nivel 3:  +1%
     * Nivel 30: +10%
     * Nivel 64: +21%
     *
     * @param stats Estadísticas del jugador
     * @return Porcentaje de velocidad de minado (0.0 a 0.21)
     */
    public static double calculateMiningSpeed(PlayerStats stats) {
        int miningLevel = stats.getLevel(StatType.MINING);
        return Math.floor(miningLevel / 3.0) * 0.01; // +1% cada 3 niveles
    }

    // ============================================
    // AGILITY - RESISTENCIA A CAÍDA
    // ============================================

    /**
     * Calcula la Resistencia a Caída desde AGILITY
     *
     * Fórmula: nivel × 0.5%
     *
     * Nivel 1:  +0.5%
     * Nivel 32: +16%
     * Nivel 64: +32%
     *
     * @param stats Estadísticas del jugador
     * @return Porcentaje de reducción de daño por caída (0.0 a 0.32)
     */
    public static double calculateFallResistance(PlayerStats stats) {
        int agilityLevel = stats.getLevel(StatType.AGILITY);
        return agilityLevel * 0.005; // +0.5% por nivel
    }

    // ============================================
    // AGILITY - REDUCCIÓN DE HAMBRE
    // ============================================

    /**
     * Calcula la Reducción de Consumo de Hambre desde AGILITY
     *
     * Fórmula: nivel × 0.4%
     *
     * Nivel 1:  +0.4%
     * Nivel 32: +12.8%
     * Nivel 64: +25.6%
     *
     * @param stats Estadísticas del jugador
     * @return Porcentaje de reducción de hambre (0.0 a 0.256)
     */
    public static double calculateHungerReduction(PlayerStats stats) {
        int agilityLevel = stats.getLevel(StatType.AGILITY);
        return agilityLevel * 0.004; // +0.4% por nivel
    }

    // ============================================
    // AGILITY - CAPACIDAD MÁXIMA DE ENERGÍA
    // ============================================

    /**
     * Calcula la Energía Máxima TOTAL (food points totales) desde AGILITY
     *
     * Fórmula: 20 base + (nivel/64)^1.5 × 40 bonus
     *
     * Early (1-20):  21-26 food points
     * Mid (21-45):   30-46 food points
     * Late (46-64):  50-60 food points
     *
     * A nivel 0:  20 FOOD (10 muslitos) - vanilla
     * A nivel 32: 34 FOOD (17 muslitos)
     * A nivel 64: 60 FOOD (30 muslitos)
     *
     * Ratio comparado con vida: 50% del bonus de vida (40 food bonus vs 80 HP bonus)
     *
     * Nota: En Minecraft, 1 muslito = 2 puntos de hambre
     *
     * @param stats Estadísticas del jugador
     * @return Puntos TOTALES de energía máxima (no solo el bonus)
     */
    public static int calculateMaxEnergy(PlayerStats stats) {
        int agilityLevel = stats.getLevel(StatType.AGILITY);
        // Curva exponencial suave: (nivel/64)^1.5 × 40 Food bonus
        double levelRatio = agilityLevel / 64.0;
        double exponentialRatio = Math.pow(levelRatio, 1.5);
        int foodBonus = (int) (exponentialRatio * 40.0);

        // Retornar el TOTAL (base + bonus), no solo el bonus
        return 20 + foodBonus; // 20 base + hasta 40 bonus = 60 máximo a nivel 64
    }

    // ============================================
    // FARMING - DROPS EXTRA DE CULTIVOS
    // ============================================

    /**
     * Calcula la probabilidad de Drops Extra de Cultivos desde FARMING
     *
     * Fórmula: nivel × 0.4%
     *
     * Nivel 1:  +0.4%
     * Nivel 32: +12.8%
     * Nivel 64: +25.6%
     *
     * @param stats Estadísticas del jugador
     * @return Porcentaje de probabilidad de drop extra (0.0 a 0.256)
     */
    public static double calculateCropDrops(PlayerStats stats) {
        int farmingLevel = stats.getLevel(StatType.FARMING);
        return farmingLevel * 0.004; // +0.4% por nivel
    }

    // ============================================
    // MÉTODO GENERAL PARA OBTENER CUALQUIER ATRIBUTO
    // ============================================

    /**
     * Calcula el valor de un atributo derivado específico
     *
     * @param attribute Atributo derivado a calcular
     * @param stats Estadísticas del jugador
     * @return Valor calculado del atributo
     */
    public static double calculate(DerivedAttribute attribute, PlayerStats stats) {
        return switch (attribute) {
            case MAX_HEALTH -> calculateMaxHealth(stats);
            case HEALTH_REGEN -> calculateHealthRegen(stats);
            case ARMOR -> 0.0; // El armor se calcula directamente del jugador, no de stats
            case ATTACK_DAMAGE -> calculateAttackDamage(stats);
            case KNOCKBACK_RESISTANCE -> calculateKnockbackResistance(stats);
            case CRIT_CHANCE -> calculateCritChance(stats);
            case CRIT_DAMAGE -> calculateCritDamage(stats);
            case PROJECTILE_RANGE -> calculateProjectileRange(stats);
            case MINING_SPEED -> calculateMiningSpeed(stats);
            case FALL_RESISTANCE -> calculateFallResistance(stats);
            case HUNGER_REDUCTION -> calculateHungerReduction(stats);
            case MAX_ENERGY -> calculateMaxEnergy(stats);
            case CROP_DROPS -> calculateCropDrops(stats);
        };
    }

    /**
     * Formatea un valor de atributo derivado para mostrarlo en el HUD
     *
     * @param attribute Atributo a formatear
     * @param value Valor del atributo
     * @return String formateado para mostrar
     */
    public static String formatValue(DerivedAttribute attribute, double value) {
        return switch (attribute) {
            case MAX_HEALTH -> String.format("%.1f", value / 2.0); // Convertir HP a corazones (1 corazón = 2 HP)
            case ARMOR -> String.format("%.1f", value); // Armor directo
            case ATTACK_DAMAGE -> String.format("%.1f", value);
            case HEALTH_REGEN -> String.format("%.2f", value);
            case MAX_ENERGY -> String.format("%d", (int) value); // Mostrar food points totales directamente
            case CRIT_CHANCE, CRIT_DAMAGE, PROJECTILE_RANGE, MINING_SPEED,
                 FALL_RESISTANCE, HUNGER_REDUCTION, CROP_DROPS ->
                String.format("%.1f", value * 100); // Sin %% porque la unidad ya lo tiene
            case KNOCKBACK_RESISTANCE -> String.format("%.0f", value * 100); // Sin %% porque la unidad ya lo tiene
        };
    }
}
