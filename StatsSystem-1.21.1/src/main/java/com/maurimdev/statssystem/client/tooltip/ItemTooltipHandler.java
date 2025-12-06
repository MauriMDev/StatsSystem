package com.maurimdev.statssystem.client.tooltip;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.core.domain.stats.StatType;
import com.maurimdev.statssystem.gameplay.scaling.WeaponScaling;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler que agrega información de escalado a los tooltips de items
 * Se ejecuta PRIMERO (prioridad HIGH) para mostrar escalado antes que requisitos
 */
@EventBusSubscriber(modid = StatsSystem.MOD_ID, value = Dist.CLIENT)
public class ItemTooltipHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onItemTooltip(ItemTooltipEvent event) {
        try {
            ItemStack itemStack = event.getItemStack();
            Player player = event.getEntity();

            if (itemStack.isEmpty() || player == null) {
                return;
            }

            List<Component> tooltip = event.getToolTip();
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            // Obtener la configuración de escalado
            WeaponScaling.ScalingConfig scalingConfig = WeaponScaling.getScalingForItem(itemStack);
            boolean hasScaling = scalingConfig.hasScaling();

            // Crear mapa de niveles de stats
            Map<StatType, Integer> statLevels = new HashMap<>();
            for (StatType statType : StatType.values()) {
                statLevels.put(statType, stats.getLevel(statType));
            }

            // Calcular el bonus de daño
            double baseAttackDamage = getBaseAttackDamage(itemStack);
            double scalingBonus = 0.0;
            if (hasScaling && baseAttackDamage > 0) {
                scalingBonus = WeaponScaling.calculateScalingBonus(
                    baseAttackDamage,
                    scalingConfig,
                    statLevels
                );

                StatsSystem.LOGGER.info("═══ TOOLTIP SCALING DEBUG ═══");
                StatsSystem.LOGGER.info("Base attack damage: {}", baseAttackDamage);
                StatsSystem.LOGGER.info("Scaling bonus: {}", scalingBonus);
                StatsSystem.LOGGER.info("Total damage: {}", baseAttackDamage + scalingBonus);
            }

            // Calcular bonus de encantamientos
            double enchantmentBonus = getEnchantmentDamageBonus(itemStack, player);

            // ===== MOSTRAR SECCIÓN DE ESCALADO (solo si tiene scaling) =====
            if (hasScaling) {
                tooltip.add(Component.literal(""));
                tooltip.add(Component.literal("§8§m               §r §b⚔ Escalado §8§m               "));

                // Mostrar escalados por stat
                for (Map.Entry<StatType, WeaponScaling.ScalingGrade> entry : scalingConfig.getScalings().entrySet()) {
                    StatType stat = entry.getKey();
                    WeaponScaling.ScalingGrade grade = entry.getValue();
                    int playerLevel = statLevels.getOrDefault(stat, 0);

                    String symbol = getStatSymbol(stat);
                    String statName = getStatName(stat);

                    // Formato: "  ⚔ Fuerza: A (Nivel 32)"
                    tooltip.add(Component.literal(
                        String.format("  %s §7%s: %s §8(Nv.%d)", symbol, statName, grade.getDisplayName(), playerLevel)
                    ));
                }

                // Mostrar daño total y bonus (si tiene daño base)
                if (baseAttackDamage > 0) {
                    double totalDamage = baseAttackDamage + scalingBonus + enchantmentBonus;

                    tooltip.add(Component.literal(""));
                    tooltip.add(Component.literal(String.format("  §7Daño base: §f%.1f", baseAttackDamage)));

                    if (scalingBonus > 0.1) {
                        tooltip.add(Component.literal(String.format("  §7Bonus escalado: §a+%.1f", scalingBonus)));
                    }

                    if (enchantmentBonus > 0.01) {
                        tooltip.add(Component.literal(String.format("  §7Bonus encantamientos: §d+%.1f", enchantmentBonus)));
                    }

                    if (scalingBonus > 0.1 || enchantmentBonus > 0.01) {
                        tooltip.add(Component.literal(String.format("  §7Daño total: §e§l%.1f", totalDamage)));
                    } else {
                        tooltip.add(Component.literal(String.format("  §7Daño total: §e§l%.1f §8(sin bonus)", totalDamage)));
                    }
                }
            }

            // ===== MOSTRAR PROBABILIDAD DE CRÍTICO (para armas de combate) =====
            float weaponCritChance = WeaponScaling.getCriticalChanceBonus(itemStack);
            if (weaponCritChance > 0.0f) {
                // Obtener crítico de DEX del jugador
                int dexLevel = stats.getLevel(StatType.DEXTERITY);
                float dexCritChance = dexLevel * 0.005f;
                float totalCritChance = dexCritChance + weaponCritChance;

                tooltip.add(Component.literal(""));
                tooltip.add(Component.literal("§8§m               §r §6⚡ Crítico §8§m               "));
                tooltip.add(Component.literal(String.format("  §7Prob. arma: §e%.1f%%", weaponCritChance * 100)));
                tooltip.add(Component.literal(String.format("  §7Prob. DEX (Nv.%d): §e%.1f%%", dexLevel, dexCritChance * 100)));
                tooltip.add(Component.literal(String.format("  §7Prob. total: §6§l%.1f%%", totalCritChance * 100)));

                if (dexLevel > 0) {
                    int dexCritDamage = dexLevel; // +1% por nivel
                    tooltip.add(Component.literal(String.format("  §7Daño crítico: §c+%d%%", dexCritDamage)));
                }
            }

            // ===== MOSTRAR INFORMACIÓN DE ENCANTAMIENTOS PARA OTROS ITEMS =====
            addEnchantmentInfo(itemStack, player, tooltip);

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error adding item tooltip", e);
        }
    }

    private static String getStatSymbol(StatType statType) {
        return switch (statType) {
            case VITALITY -> "❤";
            case STRENGTH -> "⚔";
            case DEXTERITY -> "🎯";
            case MINING -> "⛏";
            case AGILITY -> "👟";
            case FARMING -> "🌾";
        };
    }

    private static String getStatName(StatType statType) {
        return switch (statType) {
            case VITALITY -> "Vitalidad";
            case STRENGTH -> "Fuerza";
            case DEXTERITY -> "Destreza";
            case MINING -> "Minería";
            case AGILITY -> "Agilidad";
            case FARMING -> "Agricultura";
        };
    }

    /**
     * Obtiene el daño base REAL de un arma leyendo sus atributos
     * Funciona con cualquier item (vanilla o de otros mods)
     */
    private static double getBaseAttackDamage(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return 1.0; // Puño vacío hace 1 de daño
        }

        try {
            // Leer los modificadores de atributos del item
            ItemAttributeModifiers itemModifiers = itemStack.get(DataComponents.ATTRIBUTE_MODIFIERS);

            if (itemModifiers == null) {
                return 0.0;
            }

            double weaponBaseDamage = 0.0;

            // Iterar sobre todos los modificadores de atributos
            for (var entry : itemModifiers.modifiers()) {
                // Buscar el atributo ATTACK_DAMAGE
                if (entry.attribute().equals(Attributes.ATTACK_DAMAGE)) {
                    // Verificar que sea para la mano principal
                    if (entry.slot().equals(EquipmentSlotGroup.MAINHAND) ||
                        entry.slot().equals(EquipmentSlotGroup.HAND)) {

                        AttributeModifier modifier = entry.modifier();
                        // Solo sumar si es una operación ADD_VALUE (no multiplicadores)
                        if (modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                            weaponBaseDamage += modifier.amount();
                            StatsSystem.LOGGER.info("Found ATTACK_DAMAGE modifier: {} (operation: {})",
                                modifier.amount(), modifier.operation());
                        }
                    }
                }
            }

            // Minecraft suma +1 de daño base del puño a todas las armas
            // El atributo del item solo tiene el bonus, así que sumamos +1
            double finalDamage = weaponBaseDamage + 1.0;

            StatsSystem.LOGGER.info("getBaseAttackDamage() modifier: {}, final (with base punch): {}",
                weaponBaseDamage, finalDamage);
            return finalDamage;
        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error reading weapon base damage from item attributes", e);
            return 0.0;
        }
    }

    /**
     * Calcula el bonus de daño de encantamientos
     * Incluye: Sharpness (Filo), Power (Poder), Smite (Castigo), etc.
     */
    private static double getEnchantmentDamageBonus(ItemStack itemStack, Player player) {
        if (itemStack.isEmpty() || player == null) {
            return 0.0;
        }

        try {
            double totalBonus = 0.0;
            var registryAccess = player.level().registryAccess();
            var enchantmentRegistry = registryAccess.registryOrThrow(Registries.ENCHANTMENT);

            // SHARPNESS (Filo) - Para armas melee
            // Fórmula: 0.5 + (0.5 × nivel) = +3 daño en Filo V
            int sharpnessLevel = itemStack.getEnchantmentLevel(
                enchantmentRegistry.getHolderOrThrow(Enchantments.SHARPNESS)
            );
            if (sharpnessLevel > 0) {
                totalBonus += 0.5 + (0.5 * sharpnessLevel);
            }

            // SMITE (Castigo) - Contra no-muertos
            // Fórmula: 2.5 × nivel = +12.5 daño en Smite V
            int smiteLevel = itemStack.getEnchantmentLevel(
                enchantmentRegistry.getHolderOrThrow(Enchantments.SMITE)
            );
            if (smiteLevel > 0) {
                totalBonus += 2.5 * smiteLevel;
            }

            // BANE_OF_ARTHROPODS (Perdición de artrópodos) - Contra artrópodos
            // Fórmula: 2.5 × nivel = +12.5 daño en Bane V
            int baneLevel = itemStack.getEnchantmentLevel(
                enchantmentRegistry.getHolderOrThrow(Enchantments.BANE_OF_ARTHROPODS)
            );
            if (baneLevel > 0) {
                totalBonus += 2.5 * baneLevel;
            }

            // POWER (Poder) - Para arcos
            // Fórmula: 0.5 + ((nivel - 1) / 4) × 2.5 = +3 daño en Power V
            // Simplificado: 0.5 + 0.625 × (nivel - 1)
            if (itemStack.getItem() instanceof BowItem) {
                int powerLevel = itemStack.getEnchantmentLevel(
                    enchantmentRegistry.getHolderOrThrow(Enchantments.POWER)
                );
                if (powerLevel > 0) {
                    totalBonus += 0.5 + (0.625 * (powerLevel - 1));
                }
            }

            // IMPALING (Empalamiento) - Para tridents
            // Fórmula: 2.5 × nivel = +12.5 daño en Impaling V (contra criaturas acuáticas)
            int impalingLevel = itemStack.getEnchantmentLevel(
                enchantmentRegistry.getHolderOrThrow(Enchantments.IMPALING)
            );
            if (impalingLevel > 0) {
                totalBonus += 2.5 * impalingLevel;
            }

            // PIERCING (Perforación) - Para ballestas
            // No afecta directamente al daño, pero lo incluimos por completitud
            if (itemStack.getItem() instanceof CrossbowItem) {
                // Las ballestas no tienen encantamiento de daño directo como Power
                // pero Piercing permite atravesar múltiples enemigos
            }

            return totalBonus;

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error calculating enchantment damage bonus", e);
            return 0.0;
        }
    }

    /**
     * Agrega información adicional de encantamientos para armaduras, herramientas, etc.
     */
    private static void addEnchantmentInfo(ItemStack itemStack, Player player, List<Component> tooltip) {
        if (itemStack.isEmpty() || player == null) {
            return;
        }

        try {
            var registryAccess = player.level().registryAccess();
            var enchantmentRegistry = registryAccess.registryOrThrow(Registries.ENCHANTMENT);
            List<Component> enchantInfo = new ArrayList<>();

            // ===== ARMADURAS =====
            if (itemStack.getItem() instanceof ArmorItem) {
                // PROTECTION (Protección)
                int protectionLevel = itemStack.getEnchantmentLevel(
                    enchantmentRegistry.getHolderOrThrow(Enchantments.PROTECTION)
                );
                if (protectionLevel > 0) {
                    enchantInfo.add(Component.literal(
                        String.format("  §9🛡 Protección %d: §b-%d%% daño", protectionLevel, protectionLevel * 4)
                    ));
                }

                // FIRE PROTECTION
                int fireProtLevel = itemStack.getEnchantmentLevel(
                    enchantmentRegistry.getHolderOrThrow(Enchantments.FIRE_PROTECTION)
                );
                if (fireProtLevel > 0) {
                    enchantInfo.add(Component.literal(
                        String.format("  §c🔥 Prot. Fuego %d: §c-%d%% daño fuego", fireProtLevel, fireProtLevel * 8)
                    ));
                }

                // FEATHER FALLING (Caída de pluma)
                int featherFallLevel = itemStack.getEnchantmentLevel(
                    enchantmentRegistry.getHolderOrThrow(Enchantments.FEATHER_FALLING)
                );
                if (featherFallLevel > 0) {
                    enchantInfo.add(Component.literal(
                        String.format("  §f🪶 Caída Suave %d: §7-%d%% daño caída", featherFallLevel, featherFallLevel * 12)
                    ));
                }

                // THORNS (Espinas)
                int thornsLevel = itemStack.getEnchantmentLevel(
                    enchantmentRegistry.getHolderOrThrow(Enchantments.THORNS)
                );
                if (thornsLevel > 0) {
                    int thornsDamage = thornsLevel * 2; // Aproximado
                    enchantInfo.add(Component.literal(
                        String.format("  §5⚡ Espinas %d: §d~%d daño reflejado", thornsLevel, thornsDamage)
                    ));
                }
            }

            // ===== HERRAMIENTAS =====
            if (itemStack.getItem() instanceof DiggerItem) {
                // EFFICIENCY (Eficiencia)
                int efficiencyLevel = itemStack.getEnchantmentLevel(
                    enchantmentRegistry.getHolderOrThrow(Enchantments.EFFICIENCY)
                );
                if (efficiencyLevel > 0) {
                    enchantInfo.add(Component.literal(
                        String.format("  §e⚡ Eficiencia %d: §a+%.0f%% velocidad", efficiencyLevel, Math.pow(efficiencyLevel, 2) * 10)
                    ));
                }

                // FORTUNE (Fortuna)
                int fortuneLevel = itemStack.getEnchantmentLevel(
                    enchantmentRegistry.getHolderOrThrow(Enchantments.FORTUNE)
                );
                if (fortuneLevel > 0) {
                    enchantInfo.add(Component.literal(
                        String.format("  §6💎 Fortuna %d: §eMás drops", fortuneLevel)
                    ));
                }

                // SILK TOUCH (Toque de seda)
                int silkTouchLevel = itemStack.getEnchantmentLevel(
                    enchantmentRegistry.getHolderOrThrow(Enchantments.SILK_TOUCH)
                );
                if (silkTouchLevel > 0) {
                    enchantInfo.add(Component.literal("  §b🧊 Toque de Seda: §7Bloques intactos"));
                }
            }

            // ===== ARCOS Y BALLESTAS =====
            if (itemStack.getItem() instanceof BowItem || itemStack.getItem() instanceof CrossbowItem) {
                // QUICK CHARGE (Carga rápida) - solo ballestas
                if (itemStack.getItem() instanceof CrossbowItem) {
                    int quickChargeLevel = itemStack.getEnchantmentLevel(
                        enchantmentRegistry.getHolderOrThrow(Enchantments.QUICK_CHARGE)
                    );
                    if (quickChargeLevel > 0) {
                        double reduction = quickChargeLevel * 0.25 * 100;
                        enchantInfo.add(Component.literal(
                            String.format("  §a⚡ Carga Rápida %d: §e-%.0f%% tiempo", quickChargeLevel, reduction)
                        ));
                    }

                    // PIERCING (Perforación)
                    int piercingLevel = itemStack.getEnchantmentLevel(
                        enchantmentRegistry.getHolderOrThrow(Enchantments.PIERCING)
                    );
                    if (piercingLevel > 0) {
                        enchantInfo.add(Component.literal(
                            String.format("  §d➤ Perforación %d: §7Atraviesa %d enemigos", piercingLevel, piercingLevel)
                        ));
                    }
                }

                // INFINITY (Infinidad) - solo arcos
                if (itemStack.getItem() instanceof BowItem) {
                    int infinityLevel = itemStack.getEnchantmentLevel(
                        enchantmentRegistry.getHolderOrThrow(Enchantments.INFINITY)
                    );
                    if (infinityLevel > 0) {
                        enchantInfo.add(Component.literal("  §6∞ Infinidad: §7Flechas ilimitadas"));
                    }
                }

                // FLAME (Llama)
                int flameLevel = itemStack.getEnchantmentLevel(
                    enchantmentRegistry.getHolderOrThrow(Enchantments.FLAME)
                );
                if (flameLevel > 0) {
                    enchantInfo.add(Component.literal("  §c🔥 Llama: §7Flechas ardientes"));
                }
            }

            // ===== AGREGAR INFO AL TOOLTIP SI HAY ALGO =====
            if (!enchantInfo.isEmpty()) {
                tooltip.add(Component.literal(""));
                tooltip.add(Component.literal("§8§m               §r §5✨ Encantamientos §8§m               "));
                tooltip.addAll(enchantInfo);
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error adding enchantment info", e);
        }
    }
}
