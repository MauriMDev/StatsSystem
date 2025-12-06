package com.maurimdev.statssystem.init;

import com.maurimdev.statssystem.core.domain.perk.*;
import com.maurimdev.statssystem.core.domain.stats.StatType;
import com.maurimdev.statssystem.core.service.PerkService;

import java.util.Map;

/**
 * Registro centralizado de todos los perks del mod.
 * Aquí se definen y registran todos los perks disponibles en el juego.
 */
public class ModPerks {

    // Referencias a perks individuales (útil para verificaciones posteriores)

    // VITALITY - Tier 1
    public static Perk IRON_SKIN;
    public static Perk REGENERATION;  // Consolidado: antes era I/II/III

    // VITALITY - Tier 2
    public static Perk SECOND_WIND;

    // VITALITY - Tier 3
    public static Perk PHOENIX_HEART;

    // STRENGTH - Tier 1
    public static Perk POWER_STRIKE;
    public static Perk KNOCKBACK_MASTERY;
    public static Perk HEAVY_HITTER;

    // STRENGTH - Tier 2
    public static Perk CRITICAL_STRIKE;
    public static Perk ARMOR_BREAKER;
    public static Perk CLEAVE;

    // STRENGTH - Tier 3
    public static Perk EXECUTE;
    public static Perk TITANS_FURY;

    // DEXTERITY - Tier 1
    public static Perk SWIFT_STRIKES;
    public static Perk QUICK_DRAW;
    public static Perk LIGHT_STEPS;

    // DEXTERITY - Tier 2
    public static Perk EAGLE_EYE;
    public static Perk PARRY;
    public static Perk PIERCING_SHOT;

    // DEXTERITY - Tier 3
    public static Perk BLADE_DANCE;
    public static Perk SNIPER;

    // MINING - Tier 1
    public static Perk EFFICIENT_MINING;
    public static Perk VEIN_FINDER;
    public static Perk PRESERVATION;

    // MINING - Tier 2
    public static Perk FORTUNE_I;
    public static Perk TUNNEL_VISION;
    public static Perk UNBREAKABLE_TOOLS;

    // MINING - Tier 3
    public static Perk FORTUNE_II;
    public static Perk EXCAVATOR;

    // AGILITY - Tier 1
    public static Perk SPRINT_MASTER;
    public static Perk FEATHER_FALL;
    public static Perk LEAP;

    // AGILITY - Tier 2
    public static Perk WALL_JUMP;
    public static Perk AIR_DASH;
    public static Perk SAFE_LANDING;

    // AGILITY - Tier 3
    public static Perk WIND_RUNNER;
    public static Perk DOUBLE_JUMP;

    // FARMING - Tier 1
    public static Perk GREEN_THUMB;
    public static Perk HARVEST_MASTER;
    public static Perk FERTILITY;

    // FARMING - Tier 2
    public static Perk MASS_HARVEST;
    public static Perk LUCKY_HARVEST;
    public static Perk COMPOST;

    // FARMING - Tier 3
    public static Perk NATURES_BLESSING;
    public static Perk INSTANT_GROWTH;

    // Combo Perks
    public static Perk ORE_BREAKER;
    public static Perk BERSERKER_MODE;
    public static Perk DEADLY_PRECISION;
    public static Perk LIGHTNING_MINER;
    public static Perk IRON_FORTRESS;
    public static Perk NATURE_WARRIOR;
    public static Perk ASSASSINS_MARK;
    public static Perk MASTER_CRAFTSMAN;

    /**
     * Inicializa y registra todos los perks
     */
    public static void register() {
        PerkService service = PerkService.getInstance();

        // ============================================
        // VITALITY PERKS - Tier 1
        // ============================================

        IRON_SKIN = Perk.builder()
                .id("vitality_iron_skin")
                .name("Iron Skin")
                .description("Tu piel se endurece como el hierro, protegiéndote como una armadura completa.")
                .description("§7Nivel 1: §e+3 🛡 Armadura §8(una pieza)")
                .description("§7Nivel 2: §e+6 🛡 Armadura §8(dos piezas)")
                .description("§7Nivel 3: §e+9 🛡 Armadura §8(tres piezas)")
                .description("§7Nivel 4: §e+12 🛡 Armadura §8(casi completa)")
                .description("§7Nivel 5: §e+15 🛡 Armadura §8(set completo de hierro!)")
                .associatedStat(StatType.VITALITY)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_1)
                        .xpLevelCost(8)
                        .build())
                .effectType(PerkEffect.DAMAGE_REDUCTION)
                .maxLevel(5)
                .build();

        REGENERATION = Perk.builder()
                .id("vitality_regeneration")
                .name("Regeneration")
                .description("Tu cuerpo se regenera naturalmente con el tiempo.")
                .description("§7Nivel 1: §e+0.5 HP cada 5 segundos")
                .description("§7Nivel 2: §e+1.0 HP cada 3 segundos")
                .description("§7Nivel 3: §e+2.0 HP cada 2 segundos")
                .associatedStat(StatType.VITALITY)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_1)
                        .xpLevelCost(10)
                        .build())
                .effectType(PerkEffect.PASSIVE_BONUS)
                .maxLevel(3)
                .build();

        // ============================================
        // STRENGTH PERKS - Tier 1
        // ============================================

        POWER_STRIKE = Perk.builder()
                .id("strength_power_strike")
                .name("Power Strike")
                .description("Aumenta tu daño cuerpo a cuerpo.")
                .description("§7Daño melee: §e+10% por nivel")
                .associatedStat(StatType.STRENGTH)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_1)
                        .xpLevelCost(5)
                        .build())
                .effectType(PerkEffect.PASSIVE_BONUS)
                .maxLevel(5)
                .build();

        KNOCKBACK_MASTERY = Perk.builder()
                .id("strength_knockback_mastery")
                .name("Knockback Mastery")
                .description("Aumenta el knockback a enemigos.")
                .description("§7Knockback: §e+15% por nivel")
                .associatedStat(StatType.STRENGTH)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_1)
                        .xpLevelCost(7)
                        .build())
                .effectType(PerkEffect.PASSIVE_BONUS)
                .maxLevel(3)
                .build();

        HEAVY_HITTER = Perk.builder()
                .id("strength_heavy_hitter")
                .name("Heavy Hitter")
                .description("Probabilidad de aturdir al enemigo.")
                .description("§7Chance de stun: §e5% por 1 segundo")
                .associatedStat(StatType.STRENGTH)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_1)
                        .xpLevelCost(8)
                        .build())
                .effectType(PerkEffect.ON_HIT)
                .maxLevel(1)
                .build();

        // ============================================
        // DEXTERITY PERKS - Tier 1
        // ============================================

        SWIFT_STRIKES = Perk.builder()
                .id("dexterity_swift_strikes")
                .name("Swift Strikes")
                .description("Aumenta tu velocidad de ataque.")
                .description("§7Velocidad de ataque: §e+10% por nivel")
                .associatedStat(StatType.DEXTERITY)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_1)
                        .xpLevelCost(5)
                        .build())
                .effectType(PerkEffect.ATTACK_SPEED)
                .maxLevel(5)
                .build();

        QUICK_DRAW = Perk.builder()
                .id("dexterity_quick_draw")
                .name("Quick Draw")
                .description("Reduces el tiempo de carga de arco/ballesta.")
                .description("§7Tiempo de carga: §e-10% por nivel")
                .associatedStat(StatType.DEXTERITY)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_1)
                        .xpLevelCost(6)
                        .build())
                .effectType(PerkEffect.PASSIVE_BONUS)
                .maxLevel(3)
                .build();

        LIGHT_STEPS = Perk.builder()
                .id("dexterity_light_steps")
                .name("Light Steps")
                .description("No activas pressure plates ni tripwires.")
                .description("§7Paso silencioso y seguro")
                .associatedStat(StatType.DEXTERITY)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_1)
                        .xpLevelCost(8)
                        .build())
                .effectType(PerkEffect.PASSIVE_BONUS)
                .maxLevel(1)
                .build();

        // ============================================
        // MINING PERKS - Tier 1
        // ============================================

        EFFICIENT_MINING = Perk.builder()
                .id("mining_efficient_mining")
                .name("Efficient Mining")
                .description("Aumenta tu velocidad de minado.")
                .description("§7Velocidad de minado: §e+15% por nivel")
                .associatedStat(StatType.MINING)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_1)
                        .xpLevelCost(5)
                        .build())
                .effectType(PerkEffect.MINING_SPEED)
                .maxLevel(5)
                .build();

        VEIN_FINDER = Perk.builder()
                .id("mining_vein_finder")
                .name("Vein Finder")
                .description("Resalta los ores cercanos.")
                .description("§7Radio de detección: §e8 bloques")
                .description("§7Activable con toggle")
                .associatedStat(StatType.MINING)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_1)
                        .xpLevelCost(8)
                        .build())
                .effectType(PerkEffect.TOGGLE_ABILITY)
                .maxLevel(1)
                .build();

        PRESERVATION = Perk.builder()
                .id("mining_preservation")
                .name("Preservation")
                .description("Reduce el desgaste de herramientas de minado.")
                .description("§7Desgaste reducido: §e-15% por nivel")
                .associatedStat(StatType.MINING)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_1)
                        .xpLevelCost(7)
                        .build())
                .effectType(PerkEffect.PASSIVE_BONUS)
                .maxLevel(3)
                .build();

        // ============================================
        // AGILITY PERKS - Tier 1
        // ============================================

        SPRINT_MASTER = Perk.builder()
                .id("agility_sprint_master")
                .name("Sprint Master")
                .description("Reduce el consumo de hambre al correr.")
                .description("§7Consumo reducido: §e-20% por nivel")
                .associatedStat(StatType.AGILITY)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_1)
                        .xpLevelCost(5)
                        .build())
                .effectType(PerkEffect.PASSIVE_BONUS)
                .maxLevel(5)
                .build();

        FEATHER_FALL = Perk.builder()
                .id("agility_feather_fall")
                .name("Feather Fall")
                .description("Reduce el daño de caída.")
                .description("§7Daño reducido: §e-30% por nivel")
                .associatedStat(StatType.AGILITY)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_1)
                        .xpLevelCost(7)
                        .build())
                .effectType(PerkEffect.DAMAGE_REDUCTION)
                .maxLevel(3)
                .build();

        LEAP = Perk.builder()
                .id("agility_leap")
                .name("Leap")
                .description("Aumenta la altura de salto.")
                .description("§7Altura de salto: §e+25% por nivel")
                .associatedStat(StatType.AGILITY)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_1)
                        .xpLevelCost(8)
                        .build())
                .effectType(PerkEffect.MOVEMENT_SPEED)
                .maxLevel(3)
                .build();

        // ============================================
        // FARMING PERKS - Tier 1
        // ============================================

        GREEN_THUMB = Perk.builder()
                .id("farming_green_thumb")
                .name("Green Thumb")
                .description("Aumenta la velocidad de crecimiento de plantas.")
                .description("§7Velocidad de crecimiento: §e+20% por nivel")
                .associatedStat(StatType.FARMING)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_1)
                        .xpLevelCost(5)
                        .build())
                .effectType(PerkEffect.PASSIVE_BONUS)
                .maxLevel(5)
                .build();

        HARVEST_MASTER = Perk.builder()
                .id("farming_harvest_master")
                .name("Harvest Master")
                .description("Cosechar replanta automáticamente.")
                .description("§7Usa las semillas del drop para replantar")
                .associatedStat(StatType.FARMING)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_1)
                        .xpLevelCost(7)
                        .build())
                .effectType(PerkEffect.ON_HARVEST)
                .maxLevel(1)
                .build();

        FERTILITY = Perk.builder()
                .id("farming_fertility")
                .name("Fertility")
                .description("Bonemeal afecta un área más grande.")
                .description("§7Radio: §e3x3 + 1 bloque por nivel")
                .associatedStat(StatType.FARMING)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_1)
                        .xpLevelCost(8)
                        .build())
                .effectType(PerkEffect.PASSIVE_BONUS)
                .maxLevel(3)
                .build();

        // ============================================
        // VITALITY PERKS - Tier 2
        // ============================================

        SECOND_WIND = Perk.builder()
                .id("vitality_second_wind")
                .name("Second Wind")
                .description("Tu cuerpo metaboliza la comida más rápido.")
                .description("§7Reduce el cooldown de comida en §e20% por nivel")
                .description("§7Aumenta la saturación recibida en §e10% por nivel")
                .associatedStat(StatType.VITALITY)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_2)
                        .xpLevelCost(20)
                        .build())
                .effectType(PerkEffect.PASSIVE_BONUS)
                .maxLevel(3)
                .build();

        // ============================================
        // VITALITY PERKS - Tier 3
        // ============================================

        PHOENIX_HEART = Perk.builder()
                .id("vitality_phoenix_heart")
                .name("Phoenix Heart")
                .description("Como el fénix, renaces de las cenizas.")
                .description("§7Revives automáticamente con §e50% HP")
                .description("§7Otorga §eResistencia II §7por 10 segundos")
                .description("§7Cooldown: §e1 día real")
                .associatedStat(StatType.VITALITY)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_3)
                        .xpLevelCost(50)
                        .build())
                .effectType(PerkEffect.SPECIAL_ABILITY)
                .maxLevel(1)
                .build();

        // ============================================
        // STRENGTH PERKS - Tier 2
        // ============================================

        CRITICAL_STRIKE = Perk.builder()
                .id("strength_critical_strike")
                .name("Critical Strike")
                .description("Chance de crítico melee aumentada.")
                .description("§7Chance: §e+10% por nivel §7(crítico = +50% daño)")
                .associatedStat(StatType.STRENGTH)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_2)
                        .xpLevelCost(20)
                        .build())
                .effectType(PerkEffect.CRITICAL_CHANCE)
                .maxLevel(3)
                .build();

        ARMOR_BREAKER = Perk.builder()
                .id("strength_armor_breaker")
                .name("Armor Breaker")
                .description("Ignora armadura enemiga.")
                .description("§7Ignora: §e20% de armadura por nivel")
                .associatedStat(StatType.STRENGTH)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_2)
                        .xpLevelCost(22)
                        .build())
                .effectType(PerkEffect.PASSIVE_BONUS)
                .maxLevel(3)
                .build();

        CLEAVE = Perk.builder()
                .id("strength_cleave")
                .name("Cleave")
                .description("Los golpes melee afectan un área.")
                .description("§7Radio: §e2 bloques")
                .associatedStat(StatType.STRENGTH)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_2)
                        .xpLevelCost(18)
                        .build())
                .effectType(PerkEffect.ON_HIT)
                .maxLevel(1)
                .build();

        // ============================================
        // STRENGTH PERKS - Tier 3
        // ============================================

        EXECUTE = Perk.builder()
                .id("strength_execute")
                .name("Execute")
                .description("Daño masivo a enemigos con poca vida.")
                .description("§7+100% daño a enemigos con menos de §e20% HP")
                .associatedStat(StatType.STRENGTH)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_3)
                        .xpLevelCost(45)
                        .build())
                .effectType(PerkEffect.CONDITIONAL)
                .maxLevel(1)
                .build();

        TITANS_FURY = Perk.builder()
                .id("strength_titans_fury")
                .name("Titan's Fury")
                .description("Cada kill aumenta tu poder.")
                .description("§7+5% daño por kill §7(stack max 5x, dura 10s)")
                .associatedStat(StatType.STRENGTH)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_3)
                        .xpLevelCost(50)
                        .build())
                .effectType(PerkEffect.ON_KILL)
                .maxLevel(1)
                .build();

        // ============================================
        // DEXTERITY PERKS - Tier 2
        // ============================================

        EAGLE_EYE = Perk.builder()
                .id("dexterity_eagle_eye")
                .name("Eagle Eye")
                .description("Mejora precisión y alcance de proyectiles.")
                .description("§7+15% precisión y alcance por nivel")
                .associatedStat(StatType.DEXTERITY)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_2)
                        .xpLevelCost(18)
                        .build())
                .effectType(PerkEffect.PASSIVE_BONUS)
                .maxLevel(3)
                .build();

        PARRY = Perk.builder()
                .id("dexterity_parry")
                .name("Parry")
                .description("Bloquear en timing perfecto refleja el daño.")
                .description("§7Refleja §e50% del daño")
                .associatedStat(StatType.DEXTERITY)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_2)
                        .xpLevelCost(22)
                        .build())
                .effectType(PerkEffect.ON_DAMAGE_TAKEN)
                .maxLevel(1)
                .build();

        PIERCING_SHOT = Perk.builder()
                .id("dexterity_piercing_shot")
                .name("Piercing Shot")
                .description("Las flechas atraviesan enemigos.")
                .description("§7Atraviesa §e1 enemigo")
                .associatedStat(StatType.DEXTERITY)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_2)
                        .xpLevelCost(20)
                        .build())
                .effectType(PerkEffect.PASSIVE_BONUS)
                .maxLevel(1)
                .build();

        // ============================================
        // DEXTERITY PERKS - Tier 3
        // ============================================

        BLADE_DANCE = Perk.builder()
                .id("dexterity_blade_dance")
                .name("Blade Dance")
                .description("Cada hit aumenta tu velocidad de ataque.")
                .description("§7+10% vel. ataque §7(stack 5x, dura 8s)")
                .associatedStat(StatType.DEXTERITY)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_3)
                        .xpLevelCost(45)
                        .build())
                .effectType(PerkEffect.ON_HIT)
                .maxLevel(1)
                .build();

        SNIPER = Perk.builder()
                .id("dexterity_sniper")
                .name("Sniper")
                .description("Flechas perfectas sin caída de daño.")
                .description("§7Sin caída de daño por distancia")
                .description("§7Crítico garantizado si está §etotalmente cargada")
                .associatedStat(StatType.DEXTERITY)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_3)
                        .xpLevelCost(48)
                        .build())
                .effectType(PerkEffect.SPECIAL_ABILITY)
                .maxLevel(1)
                .build();

        // ============================================
        // MINING PERKS - Tier 2
        // ============================================

        FORTUNE_I = Perk.builder()
                .id("mining_fortune_1")
                .name("Fortune I")
                .description("Fortuna I en herramientas de minado.")
                .description("§7Efecto Fortuna I sin encantamiento")
                .associatedStat(StatType.MINING)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_2)
                        .xpLevelCost(22)
                        .build())
                .effectType(PerkEffect.ON_BLOCK_BREAK)
                .maxLevel(1)
                .build();

        TUNNEL_VISION = Perk.builder()
                .id("mining_tunnel_vision")
                .name("Tunnel Vision")
                .description("Minar en línea recta da bonus de velocidad.")
                .description("§7+10% velocidad por nivel §7(3+ bloques seguidos)")
                .associatedStat(StatType.MINING)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_2)
                        .xpLevelCost(18)
                        .build())
                .effectType(PerkEffect.CONDITIONAL)
                .maxLevel(3)
                .build();

        UNBREAKABLE_TOOLS = Perk.builder()
                .id("mining_unbreakable_tools")
                .name("Unbreakable Tools")
                .description("Las herramientas de minado nunca se rompen.")
                .description("§7Se detienen al §e1% durabilidad")
                .associatedStat(StatType.MINING)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_2)
                        .xpLevelCost(25)
                        .build())
                .effectType(PerkEffect.PASSIVE_BONUS)
                .maxLevel(1)
                .build();

        // ============================================
        // MINING PERKS - Tier 3
        // ============================================

        FORTUNE_II = Perk.builder()
                .id("mining_fortune_2")
                .name("Fortune II")
                .description("Fortuna II en herramientas de minado.")
                .description("§7Efecto Fortuna II sin encantamiento")
                .associatedStat(StatType.MINING)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_3)
                        .xpLevelCost(45)
                        .build())
                .effectType(PerkEffect.ON_BLOCK_BREAK)
                .maxLevel(1)
                .build();

        EXCAVATOR = Perk.builder()
                .id("mining_excavator")
                .name("Excavator")
                .description("Minado 3×3 con pico.")
                .description("§7Activable con toggle")
                .associatedStat(StatType.MINING)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_3)
                        .xpLevelCost(50)
                        .build())
                .effectType(PerkEffect.TOGGLE_ABILITY)
                .maxLevel(1)
                .build();

        // ============================================
        // AGILITY PERKS - Tier 2
        // ============================================

        WALL_JUMP = Perk.builder()
                .id("agility_wall_jump")
                .name("Wall Jump")
                .description("Saltar contra muros permite un segundo salto.")
                .description("§7Habilidad especial de parkour")
                .associatedStat(StatType.AGILITY)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_2)
                        .xpLevelCost(20)
                        .build())
                .effectType(PerkEffect.SPECIAL_ABILITY)
                .maxLevel(1)
                .build();

        AIR_DASH = Perk.builder()
                .id("agility_air_dash")
                .name("Air Dash")
                .description("Dash aéreo en la dirección que miras.")
                .description("§7Cooldown: §e3 segundos")
                .associatedStat(StatType.AGILITY)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_2)
                        .xpLevelCost(25)
                        .build())
                .effectType(PerkEffect.COOLDOWN_ABILITY)
                .maxLevel(1)
                .build();

        SAFE_LANDING = Perk.builder()
                .id("agility_safe_landing")
                .name("Safe Landing")
                .description("Sin daño de caída hasta cierta altura.")
                .description("§7Sin daño hasta §e20 bloques")
                .associatedStat(StatType.AGILITY)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_2)
                        .xpLevelCost(18)
                        .build())
                .effectType(PerkEffect.PASSIVE_BONUS)
                .maxLevel(1)
                .build();

        // ============================================
        // AGILITY PERKS - Tier 3
        // ============================================

        WIND_RUNNER = Perk.builder()
                .id("agility_wind_runner")
                .name("Wind Runner")
                .description("Sprint no consume hambre.")
                .description("§7Requiere Sprint Master nivel 5")
                .associatedStat(StatType.AGILITY)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_3)
                        .xpLevelCost(45)
                        .build())
                .effectType(PerkEffect.PASSIVE_BONUS)
                .maxLevel(1)
                .build();

        DOUBLE_JUMP = Perk.builder()
                .id("agility_double_jump")
                .name("Double Jump")
                .description("Doble salto en el aire.")
                .description("§7Salta una segunda vez mientras estás en el aire")
                .associatedStat(StatType.AGILITY)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_3)
                        .xpLevelCost(50)
                        .build())
                .effectType(PerkEffect.SPECIAL_ABILITY)
                .maxLevel(1)
                .build();

        // ============================================
        // FARMING PERKS - Tier 2
        // ============================================

        MASS_HARVEST = Perk.builder()
                .id("farming_mass_harvest")
                .name("Mass Harvest")
                .description("Cosecha cultivos en área 3×3.")
                .description("§7Click derecho en cultivo maduro")
                .associatedStat(StatType.FARMING)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_2)
                        .xpLevelCost(20)
                        .build())
                .effectType(PerkEffect.ON_HARVEST)
                .maxLevel(1)
                .build();

        LUCKY_HARVEST = Perk.builder()
                .id("farming_lucky_harvest")
                .name("Lucky Harvest")
                .description("Aumenta drops de cultivos.")
                .description("§7+25% drops por nivel")
                .associatedStat(StatType.FARMING)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_2)
                        .xpLevelCost(22)
                        .build())
                .effectType(PerkEffect.PASSIVE_BONUS)
                .maxLevel(3)
                .build();

        COMPOST = Perk.builder()
                .id("farming_compost")
                .name("Compost")
                .description("Residuos orgánicos se convierten en bonemeal.")
                .description("§7Plantas y comida → bonemeal")
                .associatedStat(StatType.FARMING)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_2)
                        .xpLevelCost(18)
                        .build())
                .effectType(PerkEffect.SPECIAL_ABILITY)
                .maxLevel(1)
                .build();

        // ============================================
        // FARMING PERKS - Tier 3
        // ============================================

        NATURES_BLESSING = Perk.builder()
                .id("farming_natures_blessing")
                .name("Nature's Blessing")
                .description("Las plantas nunca mueren.")
                .description("§7No necesitan agua ni luz para crecer")
                .associatedStat(StatType.FARMING)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_3)
                        .xpLevelCost(45)
                        .build())
                .effectType(PerkEffect.PASSIVE_BONUS)
                .maxLevel(1)
                .build();

        INSTANT_GROWTH = Perk.builder()
                .id("farming_instant_growth")
                .name("Instant Growth")
                .description("Bonemeal causa crecimiento instantáneo.")
                .description("§71 uso = cultivo maduro")
                .associatedStat(StatType.FARMING)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.TIER_3)
                        .xpLevelCost(50)
                        .build())
                .effectType(PerkEffect.PASSIVE_BONUS)
                .maxLevel(1)
                .build();

        // ============================================
        // COMBO PERKS
        // ============================================

        MASTER_CRAFTSMAN = Perk.builder()
                .id("combo_master_craftsman")
                .name("Master Craftsman")
                .description("Craftear no consume durabilidad de herramientas.")
                .description("§7+25% velocidad de crafteo")
                .associatedStat(null)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.COMBO)
                        .xpLevelCost(40)
                        .requiredStats(Map.of(
                                StatType.MINING, 32,
                                StatType.FARMING, 32,
                                StatType.DEXTERITY, 32
                        ))
                        .build())
                .effectType(PerkEffect.SPECIAL_ABILITY)
                .maxLevel(1)
                .combo(true)
                .build();

        NATURE_WARRIOR = Perk.builder()
                .id("combo_nature_warrior")
                .name("Nature Warrior")
                .description("Estar cerca de plantas da regeneración y velocidad.")
                .description("§75 bloques: +0.5 HP/s y +20% speed")
                .associatedStat(null)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.COMBO)
                        .xpLevelCost(45)
                        .requiredStats(Map.of(
                                StatType.FARMING, 40,
                                StatType.AGILITY, 32
                        ))
                        .build())
                .effectType(PerkEffect.CONDITIONAL)
                .maxLevel(1)
                .combo(true)
                .build();

        LIGHTNING_MINER = Perk.builder()
                .id("combo_lightning_miner")
                .name("Lightning Miner")
                .description("Cada bloque minado da speed boost que stackea.")
                .description("§7Max 5x, +10% por stack")
                .associatedStat(null)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.COMBO)
                        .xpLevelCost(50)
                        .requiredStats(Map.of(
                                StatType.MINING, 48,
                                StatType.AGILITY, 32
                        ))
                        .build())
                .effectType(PerkEffect.ON_BLOCK_BREAK)
                .maxLevel(1)
                .combo(true)
                .build();

        DEADLY_PRECISION = Perk.builder()
                .id("combo_deadly_precision")
                .name("Deadly Precision")
                .description("Flechas completamente cargadas = crítico garantizado.")
                .description("§7Atraviesa 2 enemigos")
                .associatedStat(null)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.COMBO)
                        .xpLevelCost(55)
                        .requiredStats(Map.of(StatType.DEXTERITY, 48))
                        .build())
                .effectType(PerkEffect.SPECIAL_ABILITY)
                .maxLevel(1)
                .combo(true)
                .build();

        ASSASSINS_MARK = Perk.builder()
                .id("combo_assassins_mark")
                .name("Assassin's Mark")
                .description("Atacar por la espalda = daño masivo.")
                .description("§7+100% daño (180° arco trasero)")
                .associatedStat(null)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.COMBO)
                        .xpLevelCost(55)
                        .requiredStats(Map.of(
                                StatType.DEXTERITY, 48,
                                StatType.STRENGTH, 32
                        ))
                        .build())
                .effectType(PerkEffect.CONDITIONAL)
                .maxLevel(1)
                .combo(true)
                .build();

        ORE_BREAKER = Perk.builder()
                .id("combo_ore_breaker")
                .name("Ore Breaker")
                .description("Puedes minar ores con las manos.")
                .associatedStat(null) // Combo perk
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.COMBO)
                        .xpLevelCost(60)
                        .requiredStats(Map.of(
                                StatType.STRENGTH, 40,
                                StatType.DEXTERITY, 40,
                                StatType.MINING, 56
                        ))
                        .build())
                .effectType(PerkEffect.SPECIAL_ABILITY)
                .maxLevel(1)
                .combo(true)
                .build();

        BERSERKER_MODE = Perk.builder()
                .id("combo_berserker_mode")
                .name("Berserker Mode")
                .description("Al bajar de 30% HP: +50% daño y +30% velocidad ataque.")
                .description("§7Duración: 10s | Cooldown: 5 minutos")
                .associatedStat(null) // Combo perk
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.COMBO)
                        .xpLevelCost(60)
                        .requiredStats(Map.of(
                                StatType.STRENGTH, 56,
                                StatType.VITALITY, 56
                        ))
                        .build())
                .effectType(PerkEffect.CONDITIONAL)
                .maxLevel(1)
                .combo(true)
                .build();

        IRON_FORTRESS = Perk.builder()
                .id("combo_iron_fortress")
                .name("Iron Fortress")
                .description("Cuanto menor HP, mayor resistencia.")
                .description("§7Hasta +40% reducción daño a 20% HP")
                .associatedStat(null)
                .requirement(PerkRequirement.builder()
                        .tier(PerkTier.COMBO)
                        .xpLevelCost(60)
                        .requiredStats(Map.of(
                                StatType.VITALITY, 56,
                                StatType.STRENGTH, 40
                        ))
                        .build())
                .effectType(PerkEffect.CONDITIONAL)
                .maxLevel(1)
                .combo(true)
                .build();

        // ============================================
        // REGISTRAR TODOS LOS PERKS
        // ============================================

        // Vitality - Tier 1
        service.registerPerk(IRON_SKIN);
        service.registerPerk(REGENERATION);

        // Vitality - Tier 2
        service.registerPerk(SECOND_WIND);

        // Vitality - Tier 3
        service.registerPerk(PHOENIX_HEART);

        // Strength - Tier 1
        service.registerPerk(POWER_STRIKE);
        service.registerPerk(KNOCKBACK_MASTERY);
        service.registerPerk(HEAVY_HITTER);

        // Strength - Tier 2
        service.registerPerk(CRITICAL_STRIKE);
        service.registerPerk(ARMOR_BREAKER);
        service.registerPerk(CLEAVE);

        // Strength - Tier 3
        service.registerPerk(EXECUTE);
        service.registerPerk(TITANS_FURY);

        // Dexterity - Tier 1
        service.registerPerk(SWIFT_STRIKES);
        service.registerPerk(QUICK_DRAW);
        service.registerPerk(LIGHT_STEPS);

        // Dexterity - Tier 2
        service.registerPerk(EAGLE_EYE);
        service.registerPerk(PARRY);
        service.registerPerk(PIERCING_SHOT);

        // Dexterity - Tier 3
        service.registerPerk(BLADE_DANCE);
        service.registerPerk(SNIPER);

        // Mining - Tier 1
        service.registerPerk(EFFICIENT_MINING);
        service.registerPerk(VEIN_FINDER);
        service.registerPerk(PRESERVATION);

        // Mining - Tier 2
        service.registerPerk(FORTUNE_I);
        service.registerPerk(TUNNEL_VISION);
        service.registerPerk(UNBREAKABLE_TOOLS);

        // Mining - Tier 3
        service.registerPerk(FORTUNE_II);
        service.registerPerk(EXCAVATOR);

        // Agility - Tier 1
        service.registerPerk(SPRINT_MASTER);
        service.registerPerk(FEATHER_FALL);
        service.registerPerk(LEAP);

        // Agility - Tier 2
        service.registerPerk(WALL_JUMP);
        service.registerPerk(AIR_DASH);
        service.registerPerk(SAFE_LANDING);

        // Agility - Tier 3
        service.registerPerk(WIND_RUNNER);
        service.registerPerk(DOUBLE_JUMP);

        // Farming - Tier 1
        service.registerPerk(GREEN_THUMB);
        service.registerPerk(HARVEST_MASTER);
        service.registerPerk(FERTILITY);

        // Farming - Tier 2
        service.registerPerk(MASS_HARVEST);
        service.registerPerk(LUCKY_HARVEST);
        service.registerPerk(COMPOST);

        // Farming - Tier 3
        service.registerPerk(NATURES_BLESSING);
        service.registerPerk(INSTANT_GROWTH);

        // Combo Perks
        service.registerPerk(ORE_BREAKER);
        service.registerPerk(BERSERKER_MODE);
        service.registerPerk(DEADLY_PRECISION);
        service.registerPerk(LIGHTNING_MINER);
        service.registerPerk(IRON_FORTRESS);
        service.registerPerk(NATURE_WARRIOR);
        service.registerPerk(ASSASSINS_MARK);
        service.registerPerk(MASTER_CRAFTSMAN);

        System.out.println("✓ ModPerks: Registrados " + service.getTotalPerkCount() + " perks (56 totales)");
    }
}
