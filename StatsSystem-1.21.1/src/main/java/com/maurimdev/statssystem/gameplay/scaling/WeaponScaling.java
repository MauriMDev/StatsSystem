package com.maurimdev.statssystem.gameplay.scaling;

import com.maurimdev.statssystem.core.domain.stats.StatType;
import net.minecraft.world.item.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Sistema de escalado de armas tipo Dark Souls
 * Las armas escalan con diferentes stats, multiplicando su daño base
 * El escalado varía según el tier del material (netherite mejor que madera)
 */
public class WeaponScaling {

    /**
     * Material tier del item
     */
    public enum MaterialTier {
        WOOD(0),
        STONE(1),
        IRON(2),
        GOLD(2), // Gold tiene mismo nivel que iron
        DIAMOND(3),
        NETHERITE(4);

        private final int level;

        MaterialTier(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    /**
     * Grados de escalado (tipo Dark Souls)
     */
    public enum ScalingGrade {
        S(1.50, "§6S§7"), // Excepcional - 150%
        A(1.25, "§eA§7"), // Excelente - 125%
        B(1.00, "§aB§7"), // Bueno - 100%
        C(0.75, "§2C§7"), // Medio - 75%
        D(0.50, "§7D§7"), // Bajo - 50%
        E(0.25, "§8E§7"), // Muy bajo - 25%
        NONE(0.00, "§8-§7"); // Sin escalado

        private final double multiplier;
        private final String displayName;

        ScalingGrade(double multiplier, String displayName) {
            this.multiplier = multiplier;
            this.displayName = displayName;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Detecta el tier del material de un item
     */
    private static MaterialTier getMaterialTier(ItemStack itemStack) {
        String itemName = itemStack.getItem().toString().toLowerCase();

        if (itemName.contains("netherite")) return MaterialTier.NETHERITE;
        if (itemName.contains("diamond")) return MaterialTier.DIAMOND;
        if (itemName.contains("iron")) return MaterialTier.IRON;
        if (itemName.contains("gold")) return MaterialTier.GOLD;
        if (itemName.contains("stone")) return MaterialTier.STONE;
        if (itemName.contains("wood")) return MaterialTier.WOOD;

        // Por defecto, asumimos tier medio
        return MaterialTier.IRON;
    }

    /**
     * Ajusta el grado de escalado según el tier del material
     * Netherite escala mejor, madera escala peor
     */
    private static ScalingGrade adjustGradeByTier(ScalingGrade baseGrade, MaterialTier tier) {
        // Calcular el ajuste basado en el tier
        int adjustment = tier.getLevel() - 2; // Iron/Gold = 0, Diamond = +1, Netherite = +2, Stone = -1, Wood = -2

        // Aplicar el ajuste
        ScalingGrade[] grades = {ScalingGrade.E, ScalingGrade.D, ScalingGrade.C, ScalingGrade.B, ScalingGrade.A, ScalingGrade.S};
        int baseIndex = java.util.Arrays.asList(grades).indexOf(baseGrade);

        if (baseIndex == -1) return baseGrade;

        int newIndex = Math.max(0, Math.min(grades.length - 1, baseIndex + adjustment));
        return grades[newIndex];
    }

    /**
     * Configuración de escalado para un tipo de item
     */
    public static class ScalingConfig {
        private final Map<StatType, ScalingGrade> scalings;

        public ScalingConfig() {
            this.scalings = new HashMap<>();
        }

        public ScalingConfig withStat(StatType stat, ScalingGrade grade) {
            scalings.put(stat, grade);
            return this;
        }

        public Map<StatType, ScalingGrade> getScalings() {
            return scalings;
        }

        public boolean hasScaling() {
            return !scalings.isEmpty();
        }
    }

    // ============================================
    // CONFIGURACIONES DE ESCALADO POR TIPO DE ITEM
    // ============================================

    /**
     * Obtiene la configuración de escalado para un item
     * El escalado se ajusta según el tier del material (netherite > diamond > iron > stone > wood)
     */
    public static ScalingConfig getScalingForItem(ItemStack itemStack) {
        Item item = itemStack.getItem();
        MaterialTier tier = getMaterialTier(itemStack);

        // ESPADAS - Balanceadas entre fuerza y destreza
        if (item instanceof SwordItem) {
            // Base: STR C, DEX D (tier medio)
            ScalingGrade strGrade = adjustGradeByTier(ScalingGrade.C, tier);
            ScalingGrade dexGrade = adjustGradeByTier(ScalingGrade.D, tier);

            return new ScalingConfig()
                .withStat(StatType.STRENGTH, strGrade)    // Daño físico
                .withStat(StatType.DEXTERITY, dexGrade);  // Precisión
        }

        // HACHAS - Fuerza pura
        if (item instanceof AxeItem) {
            // Base: STR B (tier medio)
            ScalingGrade strGrade = adjustGradeByTier(ScalingGrade.B, tier);

            return new ScalingConfig()
                .withStat(StatType.STRENGTH, strGrade);   // Fuerza bruta
        }

        // ARCOS Y BALLESTAS - Destreza pura (no tienen tiers de material)
        if (item instanceof BowItem || item instanceof CrossbowItem) {
            return new ScalingConfig()
                .withStat(StatType.DEXTERITY, ScalingGrade.S);  // Precisión excepcional
        }

        // TRIDENTE - Versatil (no tiene tiers de material)
        if (item instanceof TridentItem) {
            return new ScalingConfig()
                .withStat(StatType.STRENGTH, ScalingGrade.C)
                .withStat(StatType.DEXTERITY, ScalingGrade.B);
        }

        // PICOS - Herramienta de minería con algo de fuerza
        if (item instanceof PickaxeItem) {
            // Base: MINING C, STR E (tier medio)
            ScalingGrade miningGrade = adjustGradeByTier(ScalingGrade.C, tier);
            ScalingGrade strGrade = adjustGradeByTier(ScalingGrade.E, tier);

            return new ScalingConfig()
                .withStat(StatType.MINING, miningGrade)    // Bonus de minería
                .withStat(StatType.STRENGTH, strGrade);    // Algo de fuerza
        }

        // PALAS
        if (item instanceof ShovelItem) {
            // Base: STR D (tier medio)
            ScalingGrade strGrade = adjustGradeByTier(ScalingGrade.D, tier);

            return new ScalingConfig()
                .withStat(StatType.STRENGTH, strGrade);
        }

        // AZADAS - Arma rápida de destreza (menos daño que espada/hacha pero muy veloz)
        if (item instanceof HoeItem) {
            // Base: DEX B (tier medio) - Arma rápida basada en precisión
            ScalingGrade dexGrade = adjustGradeByTier(ScalingGrade.B, tier);

            return new ScalingConfig()
                .withStat(StatType.DEXTERITY, dexGrade);
        }

        // Sin escalado
        return new ScalingConfig();
    }

    /**
     * Calcula el bono de daño basado en el escalado del arma y los stats del jugador
     *
     * @param baseAttackDamage Daño base del arma (ej: espada de diamante = 7)
     * @param scalingConfig Configuración de escalado del arma
     * @param statLevels Mapa de niveles de stats del jugador
     * @return Daño bonus calculado
     */
    public static double calculateScalingBonus(double baseAttackDamage, ScalingConfig scalingConfig, Map<StatType, Integer> statLevels) {
        if (!scalingConfig.hasScaling()) {
            return 0.0;
        }

        double totalBonus = 0.0;

        for (Map.Entry<StatType, ScalingGrade> entry : scalingConfig.getScalings().entrySet()) {
            StatType stat = entry.getKey();
            ScalingGrade grade = entry.getValue();

            int statLevel = statLevels.getOrDefault(stat, 0);

            if (statLevel > 0) {
                // FÓRMULA DE ESCALADO EXPONENCIAL (balanceado para PvP)
                //
                // CURVA DE PROGRESIÓN:
                // - Early (1-20):  Poco daño extra (~15% del potencial)
                // - Mid (21-45):   Daño notable (~60% del potencial)
                // - Late (46-64):  Poderoso (~100% del potencial, ~x2-3 daño base)
                //
                // Ejemplo: Espada de madera (4 dmg base), STR/DEX = 64, grade C+D (0.75+0.5=1.25x total)
                // Ratio exponencial = (64/64)^1.8 = 1.0
                // Bonus STR = 1.0 * 0.75 * 4 * 1.0 = 3.0 daño
                // Bonus DEX = 1.0 * 0.50 * 4 * 1.0 = 2.0 daño
                // Daño total = 4 + 3.0 + 2.0 = 9.0 (~2.25x)
                //
                // Ejemplo: Espada de netherite (8 dmg base), STR/DEX = 64, grade A+B (1.25+1.0=2.25x total)
                // Bonus STR = 1.0 * 1.25 * 8 * 1.0 = 10.0 daño
                // Bonus DEX = 1.0 * 1.00 * 8 * 1.0 = 8.0 daño
                // Daño total = 8 + 10.0 + 8.0 = 26.0 (~3.25x)
                //
                // Nota: Con este balance el scaling es significativo pero no overwhelming en PvP

                double statRatio = statLevel / 64.0; // 0.0 a 1.0
                double exponentialRatio = Math.pow(statRatio, 1.8); // Curva exponencial

                // Multiplicador de late game (x1.0 el daño base como máximo - BALANCEADO PARA PVP)
                double lateGameMultiplier = 1.0;

                double bonus = exponentialRatio * grade.getMultiplier() * baseAttackDamage * lateGameMultiplier;
                totalBonus += bonus;
            }
        }

        return totalBonus;
    }

    /**
     * Genera la descripción de escalado para tooltips
     */
    public static String getScalingDescription(ScalingConfig config) {
        if (!config.hasScaling()) {
            return "";
        }

        StringBuilder builder = new StringBuilder("§7Escalado: ");
        boolean first = true;

        for (Map.Entry<StatType, ScalingGrade> entry : config.getScalings().entrySet()) {
            if (!first) {
                builder.append(" §8/§7 ");
            }
            first = false;

            StatType stat = entry.getKey();
            ScalingGrade grade = entry.getValue();

            String statSymbol = getStatSymbol(stat);
            builder.append(statSymbol)
                   .append(" ")
                   .append(grade.getDisplayName());
        }

        return builder.toString();
    }

    /**
     * Obtiene el símbolo unicode de un stat
     */
    private static String getStatSymbol(StatType stat) {
        return switch (stat) {
            case VITALITY -> "❤";
            case STRENGTH -> "⚔";
            case DEXTERITY -> "🎯";
            case MINING -> "⛏";
            case AGILITY -> "👟";
            case FARMING -> "🌾";
        };
    }

    // ============================================
    // CRÍTICO DE ARMAS
    // ============================================

    /**
     * Obtiene el bonus de probabilidad de crítico de un arma
     *
     * Este porcentaje se SUMA directamente al crítico de Dexterity.
     * Solo las armas de combate tienen crítico base (espadas, hachas, azadas, arcos, ballestas, tridente).
     * Las herramientas (picos, palas) NO tienen crítico.
     *
     * BALANCE:
     * - Oro tiene el mayor % para compensar baja durabilidad
     * - Espadas > Hachas > Azadas (para que azada no esté rota con velocidad)
     * - Ballesta > Arco (más difícil de usar, siempre carga completa)
     * - Tiers bajos (madera/piedra) dan poco crítico
     * - Tiers altos (hierro+) dan crítico significativo
     *
     * Ejemplos a nivel DEX 64 (32% base):
     * - Espada Netherite: 32% + 15% = 47%
     * - Espada Oro: 32% + 18% = 50%
     * - Ballesta: 32% + 12% = 44%
     *
     * @param itemStack Item a verificar
     * @return Probabilidad de crítico (0.0 a 0.18 = 0% a 18%)
     */
    public static float getCriticalChanceBonus(ItemStack itemStack) {
        Item item = itemStack.getItem();
        MaterialTier tier = getMaterialTier(itemStack);

        // ESPADAS - Mayor crítico, arma principal balanceada
        if (item instanceof SwordItem) {
            return switch (tier) {
                case WOOD -> 0.03f;      // 3%
                case STONE -> 0.05f;     // 5%
                case IRON -> 0.08f;      // 8%
                case GOLD -> 0.18f;      // 18% - ORO VIABLE!
                case DIAMOND -> 0.12f;   // 12%
                case NETHERITE -> 0.15f; // 15%
            };
        }

        // HACHAS - Crítico medio, arma de fuerza
        if (item instanceof AxeItem) {
            return switch (tier) {
                case WOOD -> 0.02f;      // 2%
                case STONE -> 0.03f;     // 3%
                case IRON -> 0.05f;      // 5%
                case GOLD -> 0.14f;      // 14% - Oro viable
                case DIAMOND -> 0.08f;   // 8%
                case NETHERITE -> 0.10f; // 10%
            };
        }

        // AZADAS - Crítico bajo, compensa con velocidad
        if (item instanceof HoeItem) {
            return switch (tier) {
                case WOOD -> 0.01f;      // 1%
                case STONE -> 0.02f;     // 2%
                case IRON -> 0.04f;      // 4%
                case GOLD -> 0.10f;      // 10% - Build DEX/velocidad
                case DIAMOND -> 0.06f;   // 6%
                case NETHERITE -> 0.08f; // 8%
            };
        }

        // ARCO - Crítico bueno, arma de precisión
        if (item instanceof BowItem) {
            return 0.10f; // 10% - Sin tiers
        }

        // BALLESTA - Mayor crítico que arco (más difícil de usar, siempre carga completa)
        if (item instanceof CrossbowItem) {
            return 0.12f; // 12% - Recompensa por dificultad
        }

        // TRIDENTE - Crítico medio, arma versátil
        if (item instanceof TridentItem) {
            return 0.08f; // 8% - Sin tiers
        }

        // Sin crítico (herramientas, otros items)
        return 0.0f;
    }
}
