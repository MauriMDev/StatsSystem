package com.maurimdev.statssystem.gameplay.event;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.core.domain.stats.StatType;
import com.maurimdev.statssystem.core.service.DerivedAttributesCalculator;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Random;

/**
 * Handler que aplica las bonificaciones de stats a los jugadores
 * Este handler maneja:
 * - Atributos base (HP, Daño, Velocidad de minado)
 * - Eventos de gameplay (Críticos, Caída, Farming, Consumo de comida)
 */
@EventBusSubscriber(modid = StatsSystem.MOD_ID)
public class StatBonusHandler {

    // IDs únicos para los attribute modifiers
    private static final ResourceLocation VITALITY_HP_MODIFIER = ResourceLocation.fromNamespaceAndPath(StatsSystem.MOD_ID, "vitality_hp_bonus");
    private static final ResourceLocation STRENGTH_DAMAGE_MODIFIER = ResourceLocation.fromNamespaceAndPath(StatsSystem.MOD_ID, "strength_damage_bonus");
    private static final ResourceLocation STRENGTH_KNOCKBACK_RES_MODIFIER = ResourceLocation.fromNamespaceAndPath(StatsSystem.MOD_ID, "strength_knockback_resistance");
    private static final ResourceLocation MINING_SPEED_MODIFIER = ResourceLocation.fromNamespaceAndPath(StatsSystem.MOD_ID, "mining_speed_bonus");
    private static final ResourceLocation IRON_SKIN_ARMOR_MODIFIER = ResourceLocation.fromNamespaceAndPath(StatsSystem.MOD_ID, "iron_skin_armor_bonus");

    private static final Random RANDOM = new Random();

    // ============================================
    // APLICAR ATRIBUTOS AL INICIAR/CAMBIAR STATS
    // ============================================

    /**
     * Aplica/actualiza atributos cuando el jugador se loguea
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof Player player) {
            updateAllAttributes(player);
        }
    }

    /**
     * Aplica/actualiza atributos cuando el jugador respawnea
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof Player player) {
            // Programar para el siguiente tick para asegurar que todo esté inicializado
            player.getServer().execute(() -> updateAllAttributes(player));
        }
    }

    /**
     * Aplica/actualiza atributos cuando el jugador cambia de dimensión
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof Player player) {
            updateAllAttributes(player);
        }
    }

    /**
     * Actualiza atributos periódicamente (cada 20 ticks = 1 segundo)
     * Esto asegura que los bonos se mantengan sincronizados si los stats cambian
     * También aplica regeneración de vida
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Solo en servidor
        if (player.level().isClientSide) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            // Cada segundo: actualizar atributos
            if (player.tickCount % 20 == 0) {
                updateAllAttributes(player);

                // NOTA: Regeneración de vida ya NO viene de Vitality
                // Ahora se obtiene SOLO mediante perks (Regeneration, Nature Warrior, etc.)
                // y efectos de poción. Los perks manejan su propia regeneración.
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error in player tick handler", e);
        }
    }

    /**
     * NOTA: La regeneración de vida ya NO se aplica desde Vitality.
     *
     * La regeneración ahora viene SOLO de:
     * - Perk "Regeneration" (Vitality Tier 1)
     * - Perk "Nature Warrior" (Combo)
     * - Efectos de poción de Regeneración
     * - Otros perks que otorguen regeneración
     *
     * Los perks manejan su propia lógica de regeneración en sus respectivos handlers.
     * Este método se eliminó para evitar regeneración duplicada.
     */

    // ============================================
    // ACTUALIZACIÓN DE ATRIBUTOS
    // ============================================

    /**
     * Actualiza todos los atributos base del jugador según sus stats
     */
    private static void updateAllAttributes(Player player) {
        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            updateVitalityBonus(player, stats);
            updateStrengthBonus(player, stats);
            updateStrengthKnockbackResistance(player, stats);
            updateMiningBonus(player, stats);
            updateIronSkinBonus(player, stats);
            updateAgilityFoodBonus(player, stats);

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error updating player attributes", e);
        }
    }

    /**
     * VITALIDAD: CURVA EXPONENCIAL ÉPICA
     *
     * Early (1-20):  +2-8 HP (~1-4 corazones)
     * Mid (21-45):   +15-40 HP (~7-20 corazones)
     * Late (46-64):  +50-80 HP (~25-40 corazones)
     *
     * A nivel 64: 20 HP base + 80 HP bonus = 100 HP TOTAL (50 corazones) ¡TANQUE!
     */
    private static void updateVitalityBonus(Player player, PlayerStats stats) {
        int vitalityLevel = stats.getLevel(StatType.VITALITY);

        // Curva exponencial: (nivel/64)^1.8 × 80 HP máximo
        double levelRatio = vitalityLevel / 64.0;
        double exponentialRatio = Math.pow(levelRatio, 1.8);
        double hpBonus = exponentialRatio * 80.0; // Máximo 80 HP extras a nivel 64

        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            // Remover modificador antiguo si existe
            maxHealth.removeModifier(VITALITY_HP_MODIFIER);

            // Aplicar nuevo modificador
            if (hpBonus > 0) {
                AttributeModifier modifier = new AttributeModifier(
                    VITALITY_HP_MODIFIER,
                    hpBonus,
                    AttributeModifier.Operation.ADD_VALUE
                );
                maxHealth.addPermanentModifier(modifier);
            }

            // Asegurar que la salud actual no exceda el nuevo máximo
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }
    }

    /**
     * FUERZA: DAÑO BONUS SOLO PARA PUÑO (sin arma)
     *
     * IMPORTANTE: Este bonus ya NO se aplica como modificador permanente.
     * Se aplica manualmente en el evento de daño, SOLO cuando el jugador ataca sin arma.
     *
     * Daño base puño = 1
     *
     * BALANCEADO:
     * Early (1-20):  +0.15-0.8 daño (total: 1.15-1.8)
     * Mid (21-45):   +1.5-4.0 daño (total: 2.5-5.0, similar a espada de piedra)
     * Late (46-64):  +5-8 daño (total: 6-9, similar a espada de hierro sin scaling)
     *
     * Las armas siempre hacen más daño gracias al weapon scaling, incentivando su uso.
     *
     * A nivel 64: 1 base + 15 bonus + scaling ≈ 20-25 dmg de puño
     *
     * Cuando tiene un arma equipada, el bonus NO se aplica, solo el weapon scaling.
     */
    private static void updateStrengthBonus(Player player, PlayerStats stats) {
        // LIMPIAR cualquier modificador viejo que pudiera existir de versiones anteriores
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            // Remover el modificador antiguo si existe
            attackDamage.removeModifier(STRENGTH_DAMAGE_MODIFIER);
        }

        // Ya NO aplicamos el modificador permanente aquí
        // El bonus se aplicará manualmente en el evento de daño
        // solo cuando el jugador NO tiene un arma equipada

        // NOTA: Este método ahora limpia modificadores viejos y no aplica nuevos
    }

    /**
     * MINERÍA: +1% velocidad de minado cada 3 niveles
     * Nivel 64 = +21.3% velocidad (más balanceado)
     */
    private static void updateMiningBonus(Player player, PlayerStats stats) {
        int miningLevel = stats.getLevel(StatType.MINING);
        double speedBonus = Math.floor(miningLevel / 3.0) * 0.01; // +1% cada 3 niveles como multiplicador

        AttributeInstance blockBreakSpeed = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
        if (blockBreakSpeed != null) {
            // Remover modificador antiguo si existe
            blockBreakSpeed.removeModifier(MINING_SPEED_MODIFIER);

            // Aplicar nuevo modificador
            if (speedBonus > 0) {
                AttributeModifier modifier = new AttributeModifier(
                    MINING_SPEED_MODIFIER,
                    speedBonus,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );
                blockBreakSpeed.addPermanentModifier(modifier);
            }
        }
    }

    /**
     * IRON SKIN PERK: Armadura adicional
     *
     * Fórmula: +3 puntos de armadura por nivel del perk
     *
     * Nivel 1: +3 armor (una pieza de hierro)
     * Nivel 2: +6 armor (dos piezas de hierro)
     * Nivel 3: +9 armor (tres piezas de hierro)
     * Nivel 4: +12 armor (casi set completo)
     * Nivel 5: +15 armor (set completo de hierro - 60% reducción base)
     *
     * Este perk otorga una cantidad MASIVA de armadura, haciendo que
     * el esfuerzo para desbloquearlo valga MUCHO la pena. A nivel 5,
     * es como llevar una armadura completa de hierro invisible.
     *
     * TEMÁTICA: Tu piel literalmente se convierte en hierro.
     */
    private static void updateIronSkinBonus(Player player, PlayerStats stats) {
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            // Remover modificador antiguo si existe
            armor.removeModifier(IRON_SKIN_ARMOR_MODIFIER);

            // Verificar si el jugador tiene el perk activo
            if (stats.isPerkActive(com.maurimdev.statssystem.init.ModPerks.IRON_SKIN.getId())) {
                int perkLevel = stats.getPerkLevel(com.maurimdev.statssystem.init.ModPerks.IRON_SKIN.getId());

                if (perkLevel > 0) {
                    // +3 puntos de armadura por nivel (armadura de hierro completa a nivel 5)
                    double armorBonus = perkLevel * 3.0;

                    AttributeModifier modifier = new AttributeModifier(
                        IRON_SKIN_ARMOR_MODIFIER,
                        armorBonus,
                        AttributeModifier.Operation.ADD_VALUE
                    );
                    armor.addPermanentModifier(modifier);
                }
            }
        }
    }

    /**
     * FUERZA: RESISTENCIA AL KNOCKBACK
     *
     * Fórmula: nivel × 1.0%
     *
     * Nivel 1:  +1%
     * Nivel 32: +32%
     * Nivel 64: +64%
     */
    private static void updateStrengthKnockbackResistance(Player player, PlayerStats stats) {
        double knockbackResistance = DerivedAttributesCalculator.calculateKnockbackResistance(stats);

        AttributeInstance kbResistance = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbResistance != null) {
            // Remover modificador antiguo si existe
            kbResistance.removeModifier(STRENGTH_KNOCKBACK_RES_MODIFIER);

            // Aplicar nuevo modificador
            if (knockbackResistance > 0) {
                AttributeModifier modifier = new AttributeModifier(
                    STRENGTH_KNOCKBACK_RES_MODIFIER,
                    knockbackResistance,
                    AttributeModifier.Operation.ADD_VALUE
                );
                kbResistance.addPermanentModifier(modifier);
            }
        }
    }

    /**
     * AGILIDAD: CURVA DE COMIDA MÁXIMA (MENOR RATIO QUE VIDA)
     *
     * Early (1-20):  +1-6 food (~0.5-3 muslitos)
     * Mid (21-45):   +10-26 food (~5-13 muslitos)
     * Late (46-64):  +30-40 food (~15-20 muslitos)
     *
     * A nivel 64: 20 food base + 40 food bonus = 60 FOOD TOTAL (30 muslitos)
     * A nivel 32: 20 food base + 14 food bonus = 34 FOOD (17 muslitos) - 50% del camino
     *
     * Ratio comparado con vida:
     * - Vida nivel 64: 20 base + 80 bonus = 100 HP (50 corazones)
     * - Comida nivel 64: 20 base + 40 bonus = 60 Food (30 muslitos)
     * - Ratio: 40 food vs 80 HP = 50% del bonus de vida
     *
     * NOTA: La limitación de comida máxima ahora se maneja en el mixin FoodDataMixin
     * que permite hasta 60 puntos independientemente del nivel de agilidad.
     * Este método ya no aplica límites.
     */
    private static void updateAgilityFoodBonus(Player player, PlayerStats stats) {
        // Ya NO aplicamos límites aquí
        // El mixin FoodDataMixin permite comer hasta 60 puntos siempre
        // La agilidad solo afecta la cantidad de ÍCONOS mostrados en el HUD,
        // no el máximo que el jugador puede comer
    }

    // Variable para trackear si el ataque fue crítico y su multiplicador
    private static final java.util.Map<java.util.UUID, Boolean> CRITICAL_HIT_TRACKER = new java.util.HashMap<>();
    private static final java.util.Map<java.util.UUID, Float> CRITICAL_MULTIPLIER_TRACKER = new java.util.HashMap<>();

    // ============================================
    // MONITOR DE DAÑO DETALLADO
    // ============================================

    /**
     * Monitor que muestra información detallada del daño REAL aplicado
     * Se ejecuta DESPUÉS de que el daño se aplicó
     */
    @SubscribeEvent
    public static void monitorDamagePost(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        float damageApplied = event.getNewDamage();
        String targetName = event.getEntity().getName().getString();
        float targetHealthAfter = event.getEntity().getHealth();

        // Verificar si fue crítico
        boolean wasCritical = CRITICAL_HIT_TRACKER.getOrDefault(player.getUUID(), false);
        CRITICAL_HIT_TRACKER.remove(player.getUUID()); // Limpiar

        String critIndicator = wasCritical ? "§c⚡ CRÍTICO" : "§7Normal";

        player.sendSystemMessage(Component.literal(
            String.format("§e━━━ DAÑO APLICADO ━━━\n" +
                          "§7Target: §f%s\n" +
                          "§7Tipo: %s\n" +
                          "§7Daño: §e%.1f\n" +
                          "§7HP restante: §c%.1f",
                targetName,
                critIndicator,
                damageApplied,
                targetHealthAfter)));
    }


    // ============================================
    // DESTREZA: APLICACIÓN MANUAL DEL MULTIPLICADOR CRÍTICO
    // ============================================

    /**
     * DESTREZA: Aplica el bonus de daño crítico del mod
     *
     * Este es el sistema de críticos INDEPENDIENTE del crítico vanilla de Minecraft.
     * El crítico vanilla (saltar y golpear) sigue funcionando normalmente (×1.5 daño).
     * El crítico del mod SUMA daño adicional basado en Dexterity.
     *
     * Fórmula: daño_final = daño_actual + (daño_actual × bonus_dex)
     *
     * Ejemplos con puño (9 dmg) y DEX 64 (+64% bonus):
     * - Normal: 9.0
     * - Crítico mod: 9.0 + (9.0 × 0.64) = 14.76
     * - Crítico vanilla: 9.0 × 1.5 = 13.5
     * - Ambos: 13.5 + (13.5 × 0.64) = 22.14
     *
     * PRIORIDAD LOW: Se ejecuta DESPUÉS del bonus de Strength/weapon scaling
     * y DESPUÉS del crítico vanilla, para sumar el bonus sobre el daño ya calculado.
     */
    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW)
    public static void applyCriticalDamageMultiplier(LivingDamageEvent.Pre event) {
        // Verificar que el atacante sea un jugador
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        // Verificar si hay un bonus de crítico guardado para este jugador
        Float critMultiplier = CRITICAL_MULTIPLIER_TRACKER.remove(player.getUUID());
        if (critMultiplier != null && critMultiplier > 1.0f) {
            float currentDamage = event.getNewDamage();
            float bonusMultiplier = critMultiplier - 1.0f; // Convertir 1.64 a 0.64
            float bonusDamage = currentDamage * bonusMultiplier;
            float newDamage = currentDamage + bonusDamage;
            event.setNewDamage(newDamage);

            StatsSystem.LOGGER.info("⚡ MOD CRIT: {} + ({} × {}) = {}",
                currentDamage, currentDamage, bonusMultiplier, newDamage);
        }
    }

    // ============================================
    // STRENGTH: BONUS DE DAÑO PARA PUÑO
    // ============================================

    /**
     * FUERZA: Aplica el bonus de daño cuando el jugador ataca SIN ARMA
     * Este es el nuevo método para aplicar el bonus de Strength de forma condicional
     *
     * PRIORIDAD HIGHEST: Se ejecuta ANTES del multiplicador crítico,
     * para que el crítico multiplique el daño total (base + bonus).
     */
    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST)
    public static void onPlayerAttack(LivingDamageEvent.Pre event) {
        // Verificar que el atacante sea un jugador
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        // Verificar que NO tenga un arma equipada
        if (!player.getMainHandItem().isEmpty()) {
            // Tiene un arma, no aplicar el bonus directo de Strength
            // Solo se aplicará el weapon scaling
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
            double strengthBonus = DerivedAttributesCalculator.calculateAttackDamage(stats);

            if (strengthBonus > 0) {
                // Sumar el bonus al daño
                float currentDamage = event.getNewDamage();
                event.setNewDamage(currentDamage + (float) strengthBonus);
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error applying strength bonus to fist damage", e);
        }
    }

    // ============================================
    // DESTREZA: CRÍTICOS Y ARCOS
    // ============================================

    /**
     * DESTREZA: Sistema de críticos del mod (independiente del crítico vanilla)
     *
     * CRÍTICO VANILLA (saltar y golpear): 1.5× daño - se mantiene intacto
     * CRÍTICO DEX MOD: chance base de DEX + bonus del arma
     *
     * Probabilidad total = (DEX × 0.5%) + bonus_arma
     *
     * A nivel 64:
     * - 32% chance base de DEX
     * - +% según arma equipada (espada netherite +15%, oro +18%, etc.)
     * - +64% daño extra cuando sale crítico del mod
     *
     * Ejemplos (puño = 9 dmg, espada netherite = 43 dmg):
     * - Puño normal: 9.0
     * - Puño vanilla crit (salto): 9.0 × 1.5 = 13.5
     * - Puño mod crit (32% chance): 9.0 + (9.0 × 0.64) = 14.76
     * - Espada normal: 43.0
     * - Espada mod crit (32% + 15% = 47% chance): 43.0 + (43.0 × 0.64) = 70.52
     */
    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
            int dexterityLevel = stats.getLevel(StatType.DEXTERITY);

            // Probabilidad base de DEX
            float dexCritChance = dexterityLevel * 0.005f; // +0.5% por nivel (máx 32%)

            // Bonus de crítico del arma equipada
            float weaponCritChance = 0.0f;
            net.minecraft.world.item.ItemStack weapon = player.getMainHandItem();
            if (!weapon.isEmpty()) {
                weaponCritChance = com.maurimdev.statssystem.gameplay.scaling.WeaponScaling.getCriticalChanceBonus(weapon);
            }

            // Probabilidad total = DEX + Arma
            float totalCritChance = dexCritChance + weaponCritChance;

            // Si no hay chance de crítico, salir
            if (totalCritChance <= 0) {
                return;
            }

            float roll = RANDOM.nextFloat();

            StatsSystem.LOGGER.info("=== MOD CRITICAL CHECK ===");
            StatsSystem.LOGGER.info("DEX: {}% | Weapon: {}% | Total: {}% | Roll: {}",
                dexCritChance * 100, weaponCritChance * 100, totalCritChance * 100, roll);

            // Verificar si sale el crítico del mod
            if (roll < totalCritChance) {
                // ¡Crítico del mod activado!
                float bonusDamage = dexterityLevel * 0.01f; // +1% por nivel (máx +64%)

                // Guardar el bonus de daño para aplicarlo manualmente
                // NO tocamos el multiplicador de Minecraft, lo dejamos intacto
                CRITICAL_HIT_TRACKER.put(player.getUUID(), true);
                CRITICAL_MULTIPLIER_TRACKER.put(player.getUUID(), 1.0f + bonusDamage);

                StatsSystem.LOGGER.info("⚡ MOD CRITICAL HIT! Damage bonus: +{}%", bonusDamage * 100);
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error in critical hit handler", e);
        }
    }

    /**
     * DESTREZA: ALCANCE DE PROYECTILES
     *
     * Aumenta la velocidad de los proyectiles (y por tanto su alcance)
     * +0.5% por nivel (máx +32%)
     *
     * Funciona aumentando la velocidad del proyectil, lo que le permite viajar más lejos
     * antes de caer por gravedad
     */
    @SubscribeEvent
    public static void onProjectileLaunch(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        // Solo procesar proyectiles lanzados por jugadores
        if (!(event.getEntity() instanceof AbstractArrow arrow)) {
            return;
        }

        if (!(arrow.getOwner() instanceof Player player)) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
            double rangeBonus = DerivedAttributesCalculator.calculateProjectileRange(stats);

            if (rangeBonus > 0) {
                // Aumentar la velocidad del proyectil para extender su alcance
                // Multiplicamos la velocidad actual por (1 + bonus)
                double multiplier = 1.0 + rangeBonus;
                arrow.setDeltaMovement(arrow.getDeltaMovement().scale(multiplier));
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error in projectile range handler", e);
        }
    }

    // ============================================
    // AGILIDAD: CONSUMO DE COMIDA Y CAÍDA
    // ============================================

    /**
     * NOTA: El mixin FoodDataMixin no logra interceptar el método eat() en 1.21.1
     * Por eso usamos este event handler como backup para ajustar el foodLevel
     * DESPUÉS de que el jugador come.
     */

    private static final java.util.Map<java.util.UUID, Integer> FOOD_BEFORE_EAT = new java.util.HashMap<>();

    /**
     * Captura el foodLevel ANTES de que el jugador empiece a comer
     */
    @SubscribeEvent
    public static void onFoodEatStart(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Solo en servidor
        if (player.level().isClientSide) {
            return;
        }

        // Solo para comida
        if (event.getItem().getFoodProperties(player) == null) {
            return;
        }

        // Guardar foodLevel actual
        int currentFood = player.getFoodData().getFoodLevel();
        FOOD_BEFORE_EAT.put(player.getUUID(), currentFood);

        StatsSystem.LOGGER.info("===================================");
        StatsSystem.LOGGER.info("EVENT onFoodEatStart: Jugador empezó a comer. FoodLevel actual: {}", currentFood);
        StatsSystem.LOGGER.info("===================================");
    }

    /**
     * Ajusta el foodLevel DESPUÉS de que el jugador termina de comer
     * para permitir más de 20 puntos
     */
    @SubscribeEvent
    public static void onFoodEatFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Solo en servidor
        if (player.level().isClientSide) {
            return;
        }

        // Solo para comida
        if (event.getItem().getFoodProperties(player) == null) {
            return;
        }

        Integer foodBefore = FOOD_BEFORE_EAT.remove(player.getUUID());
        if (foodBefore == null) {
            foodBefore = 20; // Default si no lo capturamos
        }

        FoodData foodData = player.getFoodData();
        int foodAfter = foodData.getFoodLevel();
        int nutrition = event.getItem().getFoodProperties(player).nutrition();

        StatsSystem.LOGGER.info("===================================");
        StatsSystem.LOGGER.info("EVENT onFoodEatFinish: before={}, after={}, nutrition={}", foodBefore, foodAfter, nutrition);
        StatsSystem.LOGGER.info("===================================");

        // Calcular cuánto debería ser (sin límite de 20)
        int targetFood = Math.min(foodBefore + nutrition, 60);

        // Si vanilla lo clampeó a 20, ajustar manualmente
        if (targetFood > foodAfter) {
            foodData.setFoodLevel(targetFood);
            StatsSystem.LOGGER.info("EVENT: AJUSTADO foodLevel de {} a {}", foodAfter, targetFood);
        }
    }

    /**
     * AGILIDAD: Reduce consumo de hambre al sprintar
     * -0.4% consumo por nivel (máx -25.6%)
     *
     * NOTA: El límite de comida máxima se maneja en FoodDataMixin, no aquí
     */
    @SubscribeEvent
    public static void onPlayerTick_Agility(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Solo cada tick en servidor
        if (player.level().isClientSide) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
            int agilityLevel = stats.getLevel(StatType.AGILITY);

            if (agilityLevel > 0 && player.isSprinting()) {
                FoodData foodData = player.getFoodData();

                // Reducir consumo de hambre al sprintar
                float reduction = agilityLevel * 0.004f; // -0.4% por nivel

                // Cada 10 ticks (0.5 segundos), chance de recuperar 1 punto de exhaustion
                if (player.tickCount % 10 == 0 && RANDOM.nextFloat() < reduction) {
                    // Reducir exhaustion ligeramente
                    foodData.setExhaustion(Math.max(0, foodData.getExhaustionLevel() - 0.1f));
                }
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error in agility tick handler", e);
        }
    }

    /**
     * AGILIDAD: Reduce daño de caída
     * -0.5% daño por nivel (máx -32%)
     */
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
            int agilityLevel = stats.getLevel(StatType.AGILITY);

            if (agilityLevel > 0) {
                float reduction = agilityLevel * 0.005f; // -0.5% por nivel
                float newDamage = event.getDamageMultiplier() * (1.0f - reduction);
                event.setDamageMultiplier(Math.max(0, newDamage));
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error in fall damage handler", e);
        }
    }

    // ============================================
    // AGRICULTURA: CULTIVOS
    // ============================================

    // Nota: El bonus de crecimiento de cultivos (+0.6% por nivel) requiere
    // implementación más avanzada con tick events o loot tables.
    // Por ahora, el bonus principal de FARMING viene del drop extra al cosechar.

    /**
     * AGRICULTURA: Chance de drops extra al romper cultivos
     * +0.4% chance por nivel (máx +25.6%)
     */
    @SubscribeEvent
    public static void onBreakBlock(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        BlockState state = event.getState();
        Block block = state.getBlock();

        // Solo procesar cultivos maduros
        if (!(block instanceof CropBlock cropBlock)) {
            return;
        }

        if (!cropBlock.isMaxAge(state)) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
            int farmingLevel = stats.getLevel(StatType.FARMING);

            if (farmingLevel > 0) {
                float dropBonus = farmingLevel * 0.004f; // +0.4% por nivel

                // Chance de drop extra
                if (RANDOM.nextFloat() < dropBonus) {
                    // El drop extra se manejará via loot table en el futuro
                    // Por ahora, simplemente registramos que debería haber bonus
                    // TODO: Implementar drops extra via loot modifiers
                }
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error in farming break handler", e);
        }
    }
}
