package com.maurimdev.statssystem.client.ui.overlay;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.client.config.HUDConfig;
import com.maurimdev.statssystem.core.domain.stats.DerivedAttribute;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.core.domain.stats.StatType;
import com.maurimdev.statssystem.core.service.DerivedAttributesCalculator;
import com.maurimdev.statssystem.gameplay.scaling.WeaponScaling;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import com.maurimdev.statssystem.init.ModPerks;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantments;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

/**
 * HUD mejorado que muestra los atributos derivados en la esquina superior izquierda.
 *
 * Diseño moderno con colores atractivos y organización clara por categorías.
 * Solo se muestra cuando se presiona TAB y si está habilitado en la configuración.
 */
public class DerivedAttributesHUD implements LayeredDraw.Layer {

    // Dimensiones y espaciado base (se escalan según el tamaño de pantalla)
    private static final int BASE_MARGIN_X = 5;
    private static final int BASE_MARGIN_Y = 5;
    private static final int BASE_PANEL_PADDING = 8;
    private static final int BASE_LINE_HEIGHT = 11;
    private static final int BASE_SECTION_SPACING = 3;
    private static final int BASE_PANEL_WIDTH = 220;

    // Escalas para diferentes tamaños de pantalla (usando ancho REAL de ventana, no GUI scaled)
    private static final int SMALL_SCREEN_WIDTH = 1600;  // Pantalla pequeña (antes 1280)
    private static final int MEDIUM_SCREEN_WIDTH = 2560; // Pantalla mediana (antes 1920)
    private static final float SMALL_SCALE = 0.7f;       // 70% del tamaño
    private static final float MEDIUM_SCALE = 1.0f;      // 100% del tamaño
    private static final float LARGE_SCALE = 1.5f;       // 150% del tamaño (antes 1.8f)

    // Colores modernos (más transparentes)
    private static final int COLOR_BACKGROUND = 0xA0000000;      // Negro semi-transparente
    private static final int COLOR_BORDER_OUTER = 0xCC4A4A4A;    // Gris medio semi-transparente
    private static final int COLOR_BORDER_INNER = 0xCC2A2A2A;    // Gris oscuro semi-transparente
    private static final int COLOR_HEADER = 0xFFFFD700;          // Dorado
    private static final int COLOR_TEXT = 0xFFFFFFFF;            // Blanco
    private static final int COLOR_VALUE = 0xFF00FF88;           // Verde agua
    private static final int COLOR_SECTION = 0xFFAAAAAA;         // Gris claro

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null || mc.options.hideGui) {
            return;
        }

        // Solo mostrar si está habilitado en la configuración
        if (!HUDConfig.isHudEnabled()) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
            Font font = mc.font;

            // Calcular escala según tamaño de VENTANA (no GUI scaled)
            // Usamos el ancho real de la ventana para determinar el tamaño
            int windowWidth = mc.getWindow().getWidth();
            float scale = calculateScale(windowWidth);

            // Posición base: esquina superior izquierda
            int startX = BASE_MARGIN_X;
            int startY = BASE_MARGIN_Y;

            // Calcular altura del panel
            int panelHeight = calculatePanelHeight(BASE_LINE_HEIGHT, BASE_SECTION_SPACING, BASE_PANEL_PADDING);

            // Aplicar escala a toda la renderización
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(startX, startY, 0);
            guiGraphics.pose().scale(scale, scale, 1.0f);

            // Renderizar panel (sin offset, ya que translate lo maneja)
            renderModernPanel(guiGraphics, 0, 0, BASE_PANEL_WIDTH, panelHeight);

            // Renderizar contenido
            int currentY = BASE_PANEL_PADDING;

            // Título principal
            currentY = renderTitle(guiGraphics, font, 0, currentY, BASE_PANEL_PADDING,
                                  BASE_LINE_HEIGHT, BASE_PANEL_WIDTH);

            // VITALITY (pasamos player para obtener vida total real)
            currentY = renderSection(guiGraphics, font, stats, player, currentY, 0,
                BASE_LINE_HEIGHT, BASE_PANEL_PADDING,
                StatType.VITALITY, DerivedAttribute.MAX_HEALTH, DerivedAttribute.HEALTH_REGEN, DerivedAttribute.ARMOR);

            // STRENGTH
            currentY = renderSection(guiGraphics, font, stats, player, currentY, 0,
                BASE_LINE_HEIGHT, BASE_PANEL_PADDING,
                StatType.STRENGTH, DerivedAttribute.ATTACK_DAMAGE, DerivedAttribute.KNOCKBACK_RESISTANCE);

            // DEXTERITY
            currentY = renderSection(guiGraphics, font, stats, player, currentY, 0,
                BASE_LINE_HEIGHT, BASE_PANEL_PADDING,
                StatType.DEXTERITY, DerivedAttribute.CRIT_CHANCE, DerivedAttribute.CRIT_DAMAGE,
                DerivedAttribute.PROJECTILE_RANGE);

            // MINING
            currentY = renderSection(guiGraphics, font, stats, player, currentY, 0,
                BASE_LINE_HEIGHT, BASE_PANEL_PADDING,
                StatType.MINING, DerivedAttribute.MINING_SPEED);

            // AGILITY
            currentY = renderSection(guiGraphics, font, stats, player, currentY, 0,
                BASE_LINE_HEIGHT, BASE_PANEL_PADDING,
                StatType.AGILITY, DerivedAttribute.FALL_RESISTANCE, DerivedAttribute.HUNGER_REDUCTION,
                DerivedAttribute.MAX_ENERGY);

            // FARMING
            renderSection(guiGraphics, font, stats, player, currentY, 0,
                BASE_LINE_HEIGHT, BASE_PANEL_PADDING,
                StatType.FARMING, DerivedAttribute.CROP_DROPS);

            guiGraphics.pose().popPose();

        } catch (Exception e) {
            // Silently fail
        }
    }

    /**
     * Calcula la escala según el tamaño de pantalla
     * Usa el ancho REAL de la ventana (en pixels) no el GUI scaled
     * Pantallas grandes = más grande, pantallas pequeñas = más pequeño
     */
    private float calculateScale(int windowWidth) {
        if (windowWidth < SMALL_SCREEN_WIDTH) {
            return SMALL_SCALE;  // 0.7 para pantallas pequeñas (<1600px)
        } else if (windowWidth < MEDIUM_SCREEN_WIDTH) {
            return MEDIUM_SCALE; // 1.0 para pantallas medianas (1600-2560px)
        } else {
            return LARGE_SCALE;  // 1.5 para pantallas grandes (>2560px)
        }
    }

    /**
     * Renderiza el título principal del panel
     */
    private int renderTitle(GuiGraphics graphics, Font font, int x, int y, int padding, int lineHeight, int panelWidth) {
        String title = "§l§6ATRIBUTOS DERIVADOS";
        graphics.drawString(font, title, x + padding, y, COLOR_HEADER, false);

        // Línea divisoria decorativa
        int lineY = y + lineHeight;
        graphics.fill(
            x + padding,
            lineY,
            x + panelWidth - padding,
            lineY + 1,
            COLOR_BORDER_OUTER
        );

        return lineY + 4;
    }

    /**
     * Renderiza una sección de estadística con sus atributos
     */
    private int renderSection(GuiGraphics graphics, Font font, PlayerStats stats, Player player, int startY, int startX,
                               int lineHeight, int padding, StatType statType, DerivedAttribute... attributes) {
        int currentY = startY;

        // Título de la sección (nombre de la stat)
        String sectionTitle = "§7" + statType.getSymbol() + " " + statType.getDisplayName();
        graphics.drawString(font, sectionTitle, startX + padding + 2, currentY, COLOR_SECTION, false);
        currentY += lineHeight;

        // Renderizar cada atributo
        for (DerivedAttribute attribute : attributes) {
            currentY = renderAttribute(graphics, font, stats, player, currentY, startX, lineHeight, padding, attribute);
        }

        // Espacio entre secciones
        return currentY + (int)(BASE_SECTION_SPACING);
    }

    /**
     * Renderiza un atributo derivado individual con formato mejorado
     */
    private int renderAttribute(GuiGraphics graphics, Font font, PlayerStats stats, Player player, int y, int x,
                                 int lineHeight, int padding, DerivedAttribute attribute) {
        double value;
        String formattedValue;

        // Para atributos que deben mostrar el valor TOTAL (base + bonus), obtenerlo del jugador
        if (attribute == DerivedAttribute.MAX_HEALTH) {
            // Vida TOTAL: 20 HP base (10 corazones) + bonus de Vitality
            value = player.getMaxHealth();
            // Mostrar HP total (no corazones) porque es más claro
            // 50 corazones = 100 HP
            formattedValue = String.format("%.0f", value);
        } else if (attribute == DerivedAttribute.HEALTH_REGEN) {
            // Regeneración TOTAL: SOLO de perks y efectos (NO de Vitality)
            value = calculateActualHealthRegen(stats);
            formattedValue = String.format("%.2f", value);
        } else if (attribute == DerivedAttribute.ATTACK_DAMAGE) {
            // Daño TOTAL: daño del jugador + daño del arma en mano
            value = calculateTotalAttackDamage(player);
            formattedValue = String.format("%.1f", value);
        } else if (attribute == DerivedAttribute.KNOCKBACK_RESISTANCE) {
            // Resistencia TOTAL: bonus de Strength + armadura de netherite
            value = calculateTotalKnockbackResistance(player, stats);
            formattedValue = String.format("%.0f", Math.min(100, value * 100)); // Sin %, la unidad ya lo tiene
        } else if (attribute == DerivedAttribute.ARMOR) {
            // Armadura TOTAL: armor base + Iron Skin perk + bonus de Protection
            value = calculateEffectiveArmor(player);
            formattedValue = String.format("%.1f", value);
        } else if (attribute == DerivedAttribute.CRIT_CHANCE) {
            // Probabilidad de crítico TOTAL: DEX base + bonus del arma en mano
            double dexCritChance = DerivedAttributesCalculator.calculate(attribute, stats);
            double weaponCritChance = 0.0;

            ItemStack heldItem = player.getMainHandItem();
            if (!heldItem.isEmpty()) {
                weaponCritChance = WeaponScaling.getCriticalChanceBonus(heldItem);
            }

            value = dexCritChance + weaponCritChance;
            formattedValue = String.format("%.1f", value * 100); // Sin %, la unidad ya lo tiene
        } else {
            // Para los demás atributos (porcentajes, bonos, etc), usar el calculador normal
            value = DerivedAttributesCalculator.calculate(attribute, stats);
            formattedValue = DerivedAttributesCalculator.formatValue(attribute, value);
        }

        // Formato: "  • Nombre: §aValor unidad"
        String text = "  §8• §f" + attribute.getDisplayName() + ": §a" + formattedValue + " " + attribute.getUnit();

        graphics.drawString(font, text, x + padding + 4, y, COLOR_TEXT, false);

        return y + lineHeight;
    }

    /**
     * Calcula el daño total REAL de ataque del jugador.
     *
     * Sistema de cálculo:
     * - SIN ARMA: 1 (base) + Strength bonus + Scaling de puño
     * - CON ARMA: Daño del arma + Scaling del arma + Encantamientos
     *   (NO se suma el bonus de Strength, solo el scaling)
     */
    private double calculateTotalAttackDamage(Player player) {
        try {
            ItemStack heldItem = player.getMainHandItem();
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            // Calcular el daño base del arma usando ItemAttributeModifiers
            // Este método lee los modificadores directamente del item
            double weaponBaseDamage = getWeaponBaseDamage(heldItem);

            // Verificar si tiene un arma con weapon scaling
            WeaponScaling.ScalingConfig scalingConfig = WeaponScaling.getScalingForItem(heldItem);

            if (scalingConfig.hasScaling() && weaponBaseDamage > 0) {
                // ===== CON ARMA QUE TIENE SCALING =====
                // Sumar +1 del daño base del puño que Minecraft agrega internamente
                weaponBaseDamage += 1.0;

                // Weapon scaling del arma
                Map<StatType, Integer> statLevels = new HashMap<>();
                for (StatType statType : StatType.values()) {
                    statLevels.put(statType, stats.getLevel(statType));
                }

                double scalingBonus = WeaponScaling.calculateScalingBonus(
                    weaponBaseDamage,
                    scalingConfig,
                    statLevels
                );

                // Encantamientos
                double enchantmentBonus = 0.0;
                int sharpnessLevel = heldItem.getEnchantmentLevel(
                    player.level().registryAccess()
                        .registryOrThrow(Registries.ENCHANTMENT)
                        .getHolderOrThrow(Enchantments.SHARPNESS)
                );

                if (sharpnessLevel > 0) {
                    enchantmentBonus = 0.5 + (0.5 * sharpnessLevel);
                }

                return weaponBaseDamage + scalingBonus + enchantmentBonus;

            } else if (weaponBaseDamage == 0) {
                // ===== SIN ARMA (PUÑO) =====
                double fistBaseDamage = 1.0;
                // Bonus de Strength (SOLO para puño)
                double strengthBonus = DerivedAttributesCalculator.calculateAttackDamage(stats);
                return fistBaseDamage + strengthBonus;

            } else {
                // ===== CON ARMA SIN SCALING =====
                // Sumar +1 del daño base del puño
                return weaponBaseDamage + 1.0;
            }

        } catch (Exception e) {
            // En caso de error, retornar daño base de puño
            return 1.0;
        }
    }

    /**
     * Obtiene el daño base del arma leyendo los atributos del item.
     * Solo lee el modificador del arma, sin el +1 del puño base.
     *
     * @param itemStack El item a verificar
     * @return El daño base del arma (0 si no tiene)
     */
    private double getWeaponBaseDamage(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return 0.0;
        }

        ItemAttributeModifiers itemModifiers = itemStack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (itemModifiers == null) {
            return 0.0;
        }

        double weaponDamage = 0.0;
        for (var entry : itemModifiers.modifiers()) {
            if (entry.attribute().equals(Attributes.ATTACK_DAMAGE) &&
                (entry.slot().equals(EquipmentSlotGroup.MAINHAND) ||
                 entry.slot().equals(EquipmentSlotGroup.HAND))) {

                AttributeModifier modifier = entry.modifier();
                if (modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                    weaponDamage += modifier.amount();
                }
            }
        }

        return weaponDamage;
    }

    /**
     * Calcula la regeneración de salud REAL del jugador desde perks y efectos.
     *
     * La regeneración ya NO escala con Vitality, solo viene de:
     * - Perk "Regeneration" (Vitality Tier 1):
     *   · Nivel 1: +0.5 HP cada 5 segundos = 0.10 HP/s
     *   · Nivel 2: +1.0 HP cada 3 segundos = 0.33 HP/s
     *   · Nivel 3: +2.0 HP cada 2 segundos = 1.00 HP/s
     * - Perk "Nature Warrior" (Combo): +0.5 HP/s cerca de plantas
     * - Efectos de poción de Regeneración
     * - Otros perks que otorguen regeneración
     *
     * @param stats Estadísticas del jugador
     * @return HP por segundo de regeneración
     */
    private double calculateActualHealthRegen(PlayerStats stats) {
        try {
            double totalRegen = 0.0;

            // Verificar si tiene el perk Regeneration activo
            if (stats.isPerkActive(ModPerks.REGENERATION.getId())) {
                int perkLevel = stats.getPerkLevel(ModPerks.REGENERATION.getId());

                // Calcular HP/s según nivel del perk
                switch (perkLevel) {
                    case 1:
                        totalRegen += 0.10; // 0.5 HP cada 5s
                        break;
                    case 2:
                        totalRegen += 0.33; // 1.0 HP cada 3s
                        break;
                    case 3:
                        totalRegen += 1.00; // 2.0 HP cada 2s
                        break;
                }
            }

            // TODO: Agregar otros perks que otorguen regeneración (Nature Warrior, etc)
            // TODO: Verificar efectos de poción de Regeneración

            return totalRegen;

        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Calcula la resistencia al knockback TOTAL del jugador.
     *
     * Incluye:
     * - Bonus de Strength (nivel 64 = +64%)
     * - Armadura de netherite (+10% por pieza, hasta +40%)
     */
    private double calculateTotalKnockbackResistance(Player player, PlayerStats stats) {
        try {
            // Bonus de Strength
            double strengthBonus = DerivedAttributesCalculator.calculateKnockbackResistance(stats);

            // Bonus de armadura de netherite (cada pieza da 0.1 = 10%)
            double armorBonus = 0.0;
            for (ItemStack armorPiece : player.getArmorSlots()) {
                if (!armorPiece.isEmpty()) {
                    String itemName = armorPiece.getItem().toString().toLowerCase();
                    if (itemName.contains("netherite")) {
                        armorBonus += 0.1; // +10% por pieza de netherite
                    }
                }
            }

            return Math.min(1.0, strengthBonus + armorBonus); // Máximo 100%

        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Calcula el valor de armadura EFECTIVO incluyendo encantamientos y perks.
     *
     * En Minecraft:
     * - Armor value base: puntos de defensa de las piezas equipadas
     * - Iron Skin perk: +3 armor por nivel (max +15 a nivel 5 = set completo de hierro)
     * - Protection: cada nivel agrega un EPF (Enchantment Protection Factor)
     *
     * Mostramos un valor "equivalente" sumando:
     * - Armor base (0-20 puntos de equipment)
     * - Iron Skin bonus (0-15 puntos del perk)
     * - Bonus estimado de Protection (cada nivel IV = +1.6 puntos aprox)
     */
    private double calculateEffectiveArmor(Player player) {
        try {
            // Armor base (incluye equipment + Iron Skin perk automáticamente)
            double baseArmor = player.getArmorValue();

            // Bonus de Protection (cada nivel agrega ~0.4 puntos equivalentes)
            double protectionBonus = 0.0;

            var registryAccess = player.level().registryAccess();
            var enchantmentRegistry = registryAccess.registryOrThrow(Registries.ENCHANTMENT);

            for (ItemStack armorPiece : player.getArmorSlots()) {
                if (!armorPiece.isEmpty()) {
                    int protectionLevel = armorPiece.getEnchantmentLevel(
                        enchantmentRegistry.getHolderOrThrow(Enchantments.PROTECTION)
                    );

                    if (protectionLevel > 0) {
                        // Cada nivel de Protection agrega ~0.4 puntos de armor equivalente
                        protectionBonus += protectionLevel * 0.4;
                    }
                }
            }

            return baseArmor + protectionBonus;

        } catch (Exception e) {
            return player.getArmorValue();
        }
    }

    /**
     * Renderiza un panel moderno con borde doble y sombra
     */
    private void renderModernPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        // Sombra sutil
        graphics.fill(x + 2, y + 2, x + width + 2, y + height + 2, 0x40000000);

        // Fondo principal
        graphics.fill(x, y, x + width, y + height, COLOR_BACKGROUND);

        // Borde exterior
        renderBorder(graphics, x, y, width, height, COLOR_BORDER_OUTER);

        // Borde interior (inset)
        renderBorder(graphics, x + 1, y + 1, width - 2, height - 2, COLOR_BORDER_INNER);

        // Línea superior decorativa (acento dorado)
        graphics.fill(x + 3, y + 3, x + width - 3, y + 4, COLOR_HEADER);
    }

    /**
     * Renderiza un borde rectangular
     */
    private void renderBorder(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        // Top
        graphics.fill(x, y, x + width, y + 1, color);
        // Bottom
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        // Left
        graphics.fill(x, y, x + 1, y + height, color);
        // Right
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }

    /**
     * Calcula la altura total del panel basada en el contenido
     */
    private int calculatePanelHeight(int lineHeight, int sectionSpacing, int panelPadding) {
        // Título
        int height = lineHeight + 5; // Título + divisor

        // 6 secciones (stats)
        int sections = 6;

        // Altura por sección:
        // - Título de sección: lineHeight
        // - VITALITY: 3 atributos (MAX_HEALTH, HEALTH_REGEN, ARMOR)
        // - STRENGTH: 2 atributos
        // - DEXTERITY: 3 atributos
        // - MINING: 1 atributo
        // - AGILITY: 3 atributos
        // - FARMING: 1 atributo
        int totalAttributes = 3 + 2 + 3 + 1 + 3 + 1; // = 13 atributos

        height += (sections * lineHeight) + (totalAttributes * lineHeight);
        height += (sections * sectionSpacing);
        height += (panelPadding * 2);

        return height;
    }
}
