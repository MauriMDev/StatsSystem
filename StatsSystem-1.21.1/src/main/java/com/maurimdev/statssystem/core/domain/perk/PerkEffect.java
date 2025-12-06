package com.maurimdev.statssystem.core.domain.perk;

/**
 * Tipos de efectos que puede tener un perk.
 * Define cómo se implementa y aplica el efecto del perk en el juego.
 */
public enum PerkEffect {
    /**
     * Bonus pasivo permanente (ej: +10% daño, +2 HP)
     * Se aplica continuamente mientras el perk esté activo
     */
    PASSIVE_BONUS("Bonus Pasivo"),

    /**
     * Reducción de daño recibido
     * Se aplica cuando el jugador recibe daño
     */
    DAMAGE_REDUCTION("Reducción de Daño"),

    /**
     * Bonus a stats específicas
     * Aumenta el valor efectivo de una o más stats
     */
    STAT_BONUS("Bonus a Stats"),

    /**
     * Efecto que se activa al golpear un enemigo
     * Se ejecuta en el evento de ataque exitoso
     */
    ON_HIT("Al Golpear"),

    /**
     * Efecto que se activa al matar un enemigo
     * Se ejecuta cuando un mob muere por el jugador
     */
    ON_KILL("Al Matar"),

    /**
     * Efecto que se activa al recibir daño
     * Se ejecuta cuando el jugador es dañado
     */
    ON_DAMAGE_TAKEN("Al Recibir Daño"),

    /**
     * Efecto que se activa al romper bloques
     * Se ejecuta cuando el jugador mina un bloque
     */
    ON_BLOCK_BREAK("Al Romper Bloque"),

    /**
     * Efecto que se activa al plantar
     * Se ejecuta cuando el jugador planta cultivos
     */
    ON_PLANT("Al Plantar"),

    /**
     * Efecto que se activa al cosechar
     * Se ejecuta cuando el jugador cosecha cultivos
     */
    ON_HARVEST("Al Cosechar"),

    /**
     * Efecto condicional (se activa bajo ciertas condiciones)
     * Ej: bonus cuando HP < 30%
     */
    CONDITIONAL("Condicional"),

    /**
     * Habilidad con cooldown
     * Puede activarse manualmente pero tiene tiempo de reutilización
     */
    COOLDOWN_ABILITY("Habilidad con Cooldown"),

    /**
     * Habilidad que se puede activar/desactivar
     * El jugador controla cuándo está activa
     */
    TOGGLE_ABILITY("Habilidad Activable"),

    /**
     * Aumenta velocidad de ataque
     * Modifica el atributo de velocidad de ataque
     */
    ATTACK_SPEED("Velocidad de Ataque"),

    /**
     * Aumenta velocidad de movimiento
     * Modifica el atributo de velocidad de movimiento
     */
    MOVEMENT_SPEED("Velocidad de Movimiento"),

    /**
     * Aumenta velocidad de minado
     * Modifica la velocidad de ruptura de bloques
     */
    MINING_SPEED("Velocidad de Minado"),

    /**
     * Aumenta probabilidad de crítico
     * Modifica la chance de hacer ataques críticos
     */
    CRITICAL_CHANCE("Chance de Crítico"),

    /**
     * Habilidad especial única
     * Efectos complejos o combinaciones de efectos
     */
    SPECIAL_ABILITY("Habilidad Especial");

    private final String displayName;

    PerkEffect(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Obtiene el nombre para mostrar del tipo de efecto
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Verifica si este efecto requiere manejo manual de eventos
     */
    public boolean requiresEventHandling() {
        return switch (this) {
            case ON_HIT, ON_KILL, ON_DAMAGE_TAKEN, ON_BLOCK_BREAK,
                 ON_PLANT, ON_HARVEST -> true;
            default -> false;
        };
    }

    /**
     * Verifica si este efecto es pasivo (siempre activo)
     */
    public boolean isPassive() {
        return switch (this) {
            case PASSIVE_BONUS, DAMAGE_REDUCTION, STAT_BONUS,
                 ATTACK_SPEED, MOVEMENT_SPEED, MINING_SPEED,
                 CRITICAL_CHANCE -> true;
            default -> false;
        };
    }

    /**
     * Verifica si este efecto requiere activación manual
     */
    public boolean requiresActivation() {
        return switch (this) {
            case COOLDOWN_ABILITY, TOGGLE_ABILITY -> true;
            default -> false;
        };
    }
}
