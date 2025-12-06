package com.maurimdev.statssystem.client.ui.components;

import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.core.domain.stats.StatType;
import com.maurimdev.statssystem.core.service.DerivedAttributesCalculator;
import com.maurimdev.statssystem.core.service.StatsProgressionService;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Componente para renderizar el panel de detalles de una estadística.
 * Muestra información extendida sobre una stat seleccionada.
 */
public class StatDetailsPanel {

    // ============================================
    // CONSTANTES DE COLOR
    // ============================================

    private static final int COLOR_TEXT_GOLD = 0xFFFFD700;
    private static final int COLOR_TEXT_SECONDARY = 0xFF999999;
    private static final int COLOR_BORDER_HIGHLIGHT = 0xFFFFD700;

    // ============================================
    // CÁLCULO DE ALTURA
    // ============================================

    /**
     * Calcula la altura total del contenido de detalles para una stat
     */
    public static int calculateContentHeight(PlayerStats stats, StatType selectedStat) {
        int level = stats.getLevel(selectedStat);
        int softCap = selectedStat.getSoftCap();

        int height = 0;
        height += 18; // Título
        height += 50; // Card de información básica
        height += 8;  // Espacio
        height += 14; // Título "Efecto:"

        List<String> description = getStatDescription(selectedStat);
        height += description.size() * 11; // Líneas de descripción

        height += 8;  // Espacio
        height += 14; // Título "Cómo subir:"

        List<String> xpSources = getStatXPSources(selectedStat);
        height += xpSources.size() * 11; // Líneas de fuentes XP

        // Si está en softcap, añadir altura del warning
        if (level >= softCap && level < 64) {
            height += 10; // Espacio
            height += 30; // Card de warning
        }

        return height;
    }

    // ============================================
    // RENDERIZADO
    // ============================================

    /**
     * Renderiza el panel de detalles de una estadística
     *
     * @param graphics GuiGraphics para renderizar
     * @param font Fuente para textos
     * @param stats Estadísticas del jugador
     * @param selectedStat Stat seleccionada
     * @param x Posición X inicial
     * @param y Posición Y inicial
     * @param cardWidth Ancho disponible para las tarjetas
     */
    public static void render(GuiGraphics graphics, Font font, PlayerStats stats, StatType selectedStat,
                              int x, int y, int cardWidth) {

        int level = stats.getLevel(selectedStat);
        int softCap = selectedStat.getSoftCap();

        // Título con icono
        String symbol = StatCard.getStatSymbol(selectedStat);
        graphics.drawString(font, symbol + " §6§l" + StatCard.getStatName(selectedStat),
                x, y, COLOR_TEXT_GOLD);

        int currentY = y + 18;

        // Card de información básica
        // Nota: cardWidth ya es el ancho disponible total, NO sumarle x
        graphics.fill(x, currentY, x + cardWidth, currentY + 50, 0x40000000);

        // Línea decorativa izquierda (3px de ancho)
        graphics.fill(x, currentY, x + 3, currentY + 50, COLOR_BORDER_HIGHLIGHT);

        graphics.drawString(font, "§7Nivel: §e" + level + " §7/ §664",
                x + 4, currentY + 8, COLOR_TEXT_SECONDARY);

        String bonus = getStatBonus(selectedStat, level, softCap);
        graphics.drawString(font, "§7Bonus actual: §a" + bonus,
                x + 4, currentY + 22, COLOR_TEXT_SECONDARY);

        // Mostrar progreso hacia siguiente nivel
        if (level < 64) {
            int currentProg = (int) stats.getXP(selectedStat);
            int requiredProg = StatsProgressionService.calculatePracticeXPNeeded(level);
            float progress = 0;
            if (requiredProg > 0) {
                progress = Math.min(100, (currentProg / (float) requiredProg) * 100);
            }
            graphics.drawString(font, "§7Progreso: §e" + currentProg + " §7/ §e" + requiredProg + " §7(§e" + String.format("%.0f", progress) + "%§7)",
                    x + 4, currentY + 36, COLOR_TEXT_SECONDARY);
        } else {
            graphics.drawString(font, "§a§l¡NIVEL MÁXIMO!",
                    x + 4, currentY + 36, 0xFF7CB342);
        }

        currentY += 58;

        // Sección de efectos
        graphics.drawString(font, "§e§lEfecto:",
                x, currentY, COLOR_TEXT_GOLD);
        currentY += 14;

        List<String> description = getStatDescription(selectedStat);
        for (String line : description) {
            graphics.drawString(font, "§7• " + line,
                    x + 4, currentY, COLOR_TEXT_SECONDARY);
            currentY += 11;
        }

        currentY += 8;

        // Sección: Cómo subir esta stat
        graphics.drawString(font, "§e§lCómo subir:",
                x, currentY, COLOR_TEXT_GOLD);
        currentY += 14;

        List<String> xpSources = getStatXPSources(selectedStat);
        for (String source : xpSources) {
            graphics.drawString(font, "§7• " + source,
                    x + 4, currentY, COLOR_TEXT_SECONDARY);
            currentY += 11;
        }

        // Si está en softcap, avisar
        if (level >= softCap && level < 64) {
            currentY += 10;
            graphics.fill(x, currentY, x + cardWidth, currentY + 30, 0x40FFA500);
            graphics.fill(x, currentY, x + 3, currentY + 30, 0xFFFFAA00);
            graphics.drawString(font, "§6⚠ Soft Cap Alcanzado",
                    x + 4, currentY + 6, 0xFFFFAA00);
            graphics.drawString(font, "§7Progreso más lento después de " + softCap,
                    x + 4, currentY + 18, COLOR_TEXT_SECONDARY);
        }
    }

    // ============================================
    // UTILIDADES - CÁLCULO DE BONUS
    // ============================================

    private static String getStatBonus(StatType statType, int level, int softCap) {
        // Crear PlayerStats temporal para el cálculo
        PlayerStats tempStats = new PlayerStats();
        tempStats.setLevel(statType, level);

        return switch (statType) {
            case VITALITY -> {
                double hpBonus = DerivedAttributesCalculator.calculateMaxHealth(tempStats);
                yield "+" + String.format("%.1f", hpBonus) + " HP (" + String.format("%.1f", hpBonus / 2.0) + " corazones)";
            }
            case STRENGTH -> {
                double damageBonus = DerivedAttributesCalculator.calculateAttackDamage(tempStats);
                yield "+" + String.format("%.1f", damageBonus) + " Daño base";
            }
            case DEXTERITY -> {
                double critChance = DerivedAttributesCalculator.calculateCritChance(tempStats) * 100;
                double critDamage = DerivedAttributesCalculator.calculateCritDamage(tempStats) * 100;
                yield "+" + String.format("%.1f", critChance) + "% Crit, +" + String.format("%.0f", critDamage) + "% Dmg Crit";
            }
            case MINING -> {
                double speedBonus = DerivedAttributesCalculator.calculateMiningSpeed(tempStats) * 100;
                yield "+" + String.format("%.0f", speedBonus) + "% Velocidad de minado";
            }
            case AGILITY -> {
                double hungerReduction = DerivedAttributesCalculator.calculateHungerReduction(tempStats) * 100;
                double fallReduction = DerivedAttributesCalculator.calculateFallResistance(tempStats) * 100;
                yield "-" + String.format("%.1f", hungerReduction) + "% Hambre, -" + String.format("%.1f", fallReduction) + "% Caída";
            }
            case FARMING -> {
                double dropBonus = DerivedAttributesCalculator.calculateCropDrops(tempStats) * 100;
                yield "+" + String.format("%.1f", dropBonus) + "% Drops";
            }
        };
    }

    // ============================================
    // UTILIDADES - DESCRIPCIONES
    // ============================================

    private static List<String> getStatDescription(StatType statType) {
        List<String> lines = new ArrayList<>();

        switch (statType) {
            case VITALITY -> {
                lines.add("§aVida Extra:");
                lines.add("Progresión exponencial (nivel/64)^1.8 × 80");
                lines.add("Nivel 20: +8 HP (4 corazones)");
                lines.add("Nivel 32: +15 HP (7.5 corazones)");
                lines.add("Nivel 64: +80 HP (40 corazones)");
                lines.add("");
                lines.add("§aEscalado de Armas:");
                lines.add("Mejora armas pesadas y defensivas");
            }
            case STRENGTH -> {
                lines.add("§aDaño sin Arma (Puño):");
                lines.add("Progresión exponencial (nivel/64)^1.8 × 15");
                lines.add("Nivel 20: +1.5 daño");
                lines.add("Nivel 32: +3 daño");
                lines.add("Nivel 64: +15 daño base");
                lines.add("");
                lines.add("§c¡IMPORTANTE!");
                lines.add("El bonus directo NO se aplica con armas");
                lines.add("Con armas solo se aplica el weapon scaling");
                lines.add("");
                lines.add("§aEscalado de Armas:");
                lines.add("Espadas (B), Hachas (A), Picos (D)");
                lines.add("Incrementa el daño base del arma");
            }
            case DEXTERITY -> {
                lines.add("§aCríticos:");
                lines.add("+0.5% chance crítico por nivel");
                lines.add("+1% daño crítico por nivel");
                lines.add("Nivel 64: +32% chance, +64% dmg crit");
                lines.add("");
                lines.add("§aEscalado de Armas:");
                lines.add("Arcos (S), Espadas (C), Tridente (B)");
                lines.add("Excelente para armas de precisión");
            }
            case MINING -> {
                lines.add("§aVelocidad de Minado:");
                lines.add("+1% velocidad cada 3 niveles");
                lines.add("Nivel 64: +21% velocidad");
                lines.add("");
                lines.add("§aEscalado de Armas:");
                lines.add("Picos (B) - Bonus en combate con pico");
            }
            case AGILITY -> {
                lines.add("§aEficiencia de Stamina:");
                lines.add("-0.4% consumo hambre (sprint) por nivel");
                lines.add("Nivel 64: -25.6% consumo");
                lines.add("");
                lines.add("§aReducción de Caída:");
                lines.add("-0.5% daño de caída por nivel");
                lines.add("Nivel 64: -32% daño");
            }
            case FARMING -> {
                lines.add("§aDrops Extra:");
                lines.add("+0.4% chance drops por nivel");
                lines.add("Nivel 64: +25.6% chance");
                lines.add("");
                lines.add("§aEscalado de Armas:");
                lines.add("Azadas (B) - Bonus en combate con azada");
            }
        }

        return lines;
    }

    /**
     * Retorna las fuentes de XP para una estadística específica
     */
    private static List<String> getStatXPSources(StatType statType) {
        List<String> sources = new ArrayList<>();

        switch (statType) {
            case VITALITY -> {
                sources.add("Recibir daño (+0.3 XP × daño)");
                sources.add("Curarse/Comer (+0.2 XP)");
            }
            case STRENGTH -> {
                sources.add("Matar mobs (+0.2 XP × HP mob)");
                sources.add("§6Crítico melee (×1.5 XP)");
                sources.add("§6One-shot kill (×2 XP)");
            }
            case DEXTERITY -> {
                sources.add("Atacar mobs (+0.05 XP/golpe)");
                sources.add("§6Crítico (×2 XP)");
                sources.add("Disparar flechas (+0.1 XP)");
                sources.add("Matar con proyectil (+0.3 XP bonus)");
            }
            case MINING -> {
                sources.add("Minar bloques (+0.5 XP × dureza)");
                sources.add("§6Minar ores (×2 XP)");
                sources.add("§6Herramienta correcta (×1.3 XP)");
            }
            case AGILITY -> {
                sources.add("Correr/Sprint (+0.1 XP/tick)");
                sources.add("Saltar mientras corre (+0.05 XP)");
                sources.add("Nadar (+0.05 XP/tick)");
                sources.add("Caer altura (+0.1 XP × bloques)");
            }
            case FARMING -> {
                sources.add("Plantar cultivos (+0.3 XP)");
                sources.add("Cosechar cultivos (+0.3 XP)");
                sources.add("Usar bonemeal (+0.2 XP)");
                sources.add("Criar animales (+0.5 XP)");
                sources.add("Pescar (+0.3 XP)");
            }
        }

        return sources;
    }
}
