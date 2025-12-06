package com.maurimdev.statssystem.infrastructure.config;

/**
 * ⚖️ CONFIGURACIÓN DE BALANCEO - MODO PRÁCTICA
 *
 * Este archivo controla cuánta XP se gana por realizar acciones en modo práctica.
 * Ajusta estos valores para hacer el juego más fácil o más difícil.
 *
 * 📌 SOLO AFECTA AL MODO PRÁCTICA (cuando subes de nivel automáticamente)
 * 📌 NO afecta al modo XP (cuando gastas niveles de Minecraft para subir stats)
 */
public class PracticeBalanceConfig {

    // ============================================
    // 🏃 AGILITY - Ganancia de XP al correr
    // ============================================

    /**
     * XP ganada por tick de sprint (20 ticks = 1 segundo)
     *
     * Valor actual: 0.5 XP por tick
     * = 10 XP por segundo de sprint
     *
     * Para nivel 1 (necesitas 7 XP):
     * - 0.5 XP/tick = ~0.7 segundos de sprint
     *
     * Para nivel 10 (necesitas 106 XP):
     * - 0.5 XP/tick = ~10.6 segundos de sprint
     *
     * 🔧 AJUSTAR SI:
     * - Muy fácil: Reduce a 0.1 - 0.3
     * - Muy difícil: Aumenta a 0.7 - 1.0
     */
    public static final double AGILITY_XP_PER_SPRINT_TICK = 0.1; // ⬅️ CAMBIA ESTE VALOR

    /**
     * Intervalo de ticks para verificar sprint (no cambiar a menos que sepas lo que haces)
     */
    public static final int AGILITY_CHECK_INTERVAL = 20; // 20 ticks = 1 segundo

    // ============================================
    // ⛏️ MINING - Ganancia de XP al minar bloques
    // ============================================

    /**
     * Multiplicador de XP basado en dureza del bloque
     *
     * Fórmula: XP = dureza del bloque * multiplicador
     * Clampeo: Entre XP_MIN y XP_MAX
     *
     * Ejemplos de dureza de bloques:
     * - Tierra/Arena: 0.5
     * - Madera: 2.0
     * - Piedra: 1.5
     * - Hierro: 3.0
     * - Obsidiana: 50.0
     * - Piedra base: -1 (indestructible, no da XP)
     *
     * Valor actual: 2.0
     * - Tierra = 1.0 XP (clampeo mínimo)
     * - Piedra = 3.0 XP
     * - Hierro = 6.0 XP
     * - Obsidiana = 10.0 XP (clampeo máximo)
     *
     * 🔧 AJUSTAR SI:
     * - Muy fácil: Reduce a 0.5 - 1.0
     * - Muy difícil: Aumenta a 3.0 - 5.0
     */
    public static final double MINING_XP_MULTIPLIER = 0.5; // ⬅️ CAMBIA ESTE VALOR

    /**
     * XP mínima por bloque minado
     */
    public static final double MINING_XP_MIN = 0.3; // ⬅️ CAMBIA ESTE VALOR

    /**
     * XP máxima por bloque minado
     */
    public static final double MINING_XP_MAX = 3.0; // ⬅️ CAMBIA ESTE VALOR

    // ============================================
    // 💪 STRENGTH - Ganancia de XP al matar mobs
    // ============================================

    /**
     * Multiplicador de XP basado en vida máxima del mob
     *
     * Fórmula: XP = vida máxima del mob * multiplicador
     * Clampeo: Entre XP_MIN y XP_MAX
     *
     * Ejemplos de vida de mobs:
     * - Pollo/Gallina: 4 HP (2 corazones)
     * - Zombie: 20 HP (10 corazones)
     * - Creeper: 20 HP (10 corazones)
     * - Esqueleto Wither: 20 HP (10 corazones)
     * - Enderman: 40 HP (20 corazones)
     * - Wither: 300 HP (150 corazones)
     * - Ender Dragon: 200 HP (100 corazones)
     *
     * Valor actual: 0.5
     * - Pollo = 5.0 XP (clampeo mínimo)
     * - Zombie = 10.0 XP
     * - Enderman = 20.0 XP
     * - Wither = 30.0 XP (clampeo máximo)
     *
     * 🔧 AJUSTAR SI:
     * - Muy fácil: Reduce a 0.2 - 0.3
     * - Muy difícil: Aumenta a 1.0 - 2.0
     */
    public static final double STRENGTH_XP_MULTIPLIER = 0.2; // ⬅️ CAMBIA ESTE VALOR

    /**
     * XP mínima por kill
     */
    public static final double STRENGTH_XP_MIN = 2.0; // ⬅️ CAMBIA ESTE VALOR

    /**
     * XP máxima por kill
     */
    public static final double STRENGTH_XP_MAX = 10.0; // ⬅️ CAMBIA ESTE VALOR

    // ============================================
    // ❤️ VITALITY - Ganancia de XP al recibir daño
    // ============================================

    /**
     * Multiplicador de XP basado en daño recibido
     *
     * Fórmula: XP = daño recibido * multiplicador
     * Clampeo: Entre XP_MIN y XP_MAX
     *
     * Ejemplos de daño recibido:
     * - Caída pequeña: ~2 daño
     * - Ataque de zombie: ~3 daño
     * - Explosión de creeper: ~8-20 daño
     * - Caída mortal: 20+ daño
     *
     * Valor actual: 0.8
     * - Caída pequeña = 1.6 XP
     * - Zombie = 2.4 XP
     * - Creeper = 3.0 XP (clampeo máximo)
     *
     * 🔧 AJUSTAR SI:
     * - Muy fácil: Reduce a 0.3 - 0.5
     * - Muy difícil: Aumenta a 1.0 - 1.5
     */
    public static final double VITALITY_XP_MULTIPLIER = 0.3; // ⬅️ CAMBIA ESTE VALOR

    /**
     * XP mínima por daño recibido
     */
    public static final double VITALITY_XP_MIN = 0.3; // ⬅️ CAMBIA ESTE VALOR

    /**
     * XP máxima por daño recibido
     */
    public static final double VITALITY_XP_MAX = 1.0; // ⬅️ CAMBIA ESTE VALOR

    // ============================================
    // 🎯 DEXTERITY - Ganancia de XP al atacar
    // ============================================

    /**
     * XP ganada por cada golpe exitoso
     *
     * Se gana XP cada vez que el jugador ataca una entidad,
     * independientemente del daño causado.
     *
     * Valor actual: 0.05 XP por golpe
     *
     * Para nivel 1 (necesitas 7 XP):
     * - 0.05 XP/golpe = ~140 golpes
     *
     * Para nivel 10 (necesitas 106 XP):
     * - 0.05 XP/golpe = ~2120 golpes
     *
     * 🔧 AJUSTAR SI:
     * - Muy fácil: Aumenta a 0.1 - 0.2
     * - Muy difícil: Reduce a 0.02 - 0.03
     */
    public static final double DEXTERITY_XP_PER_HIT = 0.05; // ⬅️ CAMBIA ESTE VALOR

    /**
     * XP ganada por esquivar un ataque (para futuro uso)
     * Esta funcionalidad se implementará en versiones posteriores
     */
    public static final double DEXTERITY_DODGE_XP = 0.3; // Para futuro uso

    // ============================================
    // 🌾 FARMING - Ganancia de XP al cultivar
    // ============================================

    /**
     * XP ganada al plantar cultivos
     */
    public static final double FARMING_XP_PER_PLANT = 0.3;

    /**
     * XP ganada al cosechar cultivos maduros
     */
    public static final double FARMING_XP_PER_HARVEST = 0.3;

    /**
     * XP ganada al usar bonemeal en plantas
     */
    public static final double FARMING_XP_PER_BONEMEAL = 0.2;

    /**
     * XP ganada al criar animales
     */
    public static final double FARMING_XP_PER_BREED = 0.5;

    /**
     * XP ganada al pescar
     */
    public static final double FARMING_XP_PER_FISH = 0.3;

    // ============================================
    // ✨ BONIFICACIONES ESPECIALES
    // ============================================

    // VITALITY
    /**
     * XP ganada al comer/curarse
     */
    public static final double VITALITY_XP_PER_HEAL = 0.2;

    /**
     * XP ganada al bloquear daño con escudo (por punto de daño bloqueado)
     */
    public static final double VITALITY_XP_PER_BLOCK = 0.5;

    // STRENGTH
    /**
     * Bonus multiplicador al matar de un solo golpe (2x XP)
     */
    public static final double STRENGTH_ONESHOT_MULTIPLIER = 2.0;

    /**
     * Bonus multiplicador al hacer crítico melee (1.5x XP)
     */
    public static final double STRENGTH_CRITICAL_MULTIPLIER = 1.5;

    // DEXTERITY
    /**
     * Bonus multiplicador al hacer crítico (2x XP)
     */
    public static final double DEXTERITY_CRITICAL_MULTIPLIER = 2.0;

    /**
     * XP ganada al disparar flecha
     */
    public static final double DEXTERITY_XP_PER_ARROW = 0.1;

    /**
     * XP bonus al matar con proyectil
     */
    public static final double DEXTERITY_PROJECTILE_KILL_BONUS = 0.3;

    // MINING
    /**
     * Bonus multiplicador al minar ores (2x XP)
     */
    public static final double MINING_ORE_MULTIPLIER = 2.0;

    /**
     * Bonus multiplicador al usar herramienta correcta (1.3x XP)
     */
    public static final double MINING_CORRECT_TOOL_MULTIPLIER = 1.3;

    // AGILITY
    /**
     * XP ganada al saltar mientras corre
     */
    public static final double AGILITY_XP_PER_JUMP = 0.05;

    /**
     * XP ganada al nadar (por tick)
     */
    public static final double AGILITY_XP_PER_SWIM_TICK = 0.05;

    /**
     * XP ganada por bloque de caída (sin morir)
     * Fórmula: (altura - 3) * multiplicador
     */
    public static final double AGILITY_XP_PER_FALL_BLOCK = 0.1;

    // ============================================
    // 🔄 SINCRONIZACIÓN
    // ============================================

    /**
     * Cada cuántos ticks sincronizar XP con el cliente (no level ups)
     * 20 ticks = 1 segundo
     * 40 ticks = 2 segundos
     *
     * Menor = más sincronización = más lag potencial
     * Mayor = menos sincronización = UI menos responsive
     */
    public static final int SYNC_INTERVAL = 40; // 2 segundos

    // ============================================
    // 📊 MÉTODOS AUXILIARES
    // ============================================

    /**
     * Calcula XP de minería con los valores configurados
     */
    public static double calculateMiningXP(float blockHardness) {
        if (blockHardness <= 0) {
            return 0;
        }
        double xp = blockHardness * MINING_XP_MULTIPLIER;
        return Math.max(MINING_XP_MIN, Math.min(MINING_XP_MAX, xp));
    }

    /**
     * Calcula XP de fuerza con los valores configurados
     */
    public static double calculateStrengthXP(float mobMaxHealth) {
        double xp = mobMaxHealth * STRENGTH_XP_MULTIPLIER;
        return Math.max(STRENGTH_XP_MIN, Math.min(STRENGTH_XP_MAX, xp));
    }

    /**
     * Calcula XP de vitalidad con los valores configurados
     */
    public static double calculateVitalityXP(float damageReceived) {
        double xp = damageReceived * VITALITY_XP_MULTIPLIER;
        return Math.max(VITALITY_XP_MIN, Math.min(VITALITY_XP_MAX, xp));
    }
}
