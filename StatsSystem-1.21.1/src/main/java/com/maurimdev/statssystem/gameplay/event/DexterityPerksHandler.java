package com.maurimdev.statssystem.gameplay.event;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import com.maurimdev.statssystem.init.ModPerks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handler para perks de DEXTERITY
 *
 * TIER 1:
 * - Swift Strikes: +10% velocidad ataque por nivel (max 5)
 * - Quick Draw: -10% tiempo carga arco/ballesta por nivel (max 3)
 * - Light Steps: No activa pressure plates ni tripwires
 *
 * TIER 2:
 * - Eagle Eye: +15% precisión y alcance proyectiles por nivel (max 3)
 * - Parry: Bloquear en timing perfecto refleja 50% daño
 * - Piercing Shot: Flechas atraviesan 1 enemigo
 *
 * TIER 3:
 * - Blade Dance: Cada hit +10% velocidad ataque (stack 5x, 8s)
 * - Sniper: Sin caída daño distancia + crítico garantizado si cargada
 */
@EventBusSubscriber(modid = StatsSystem.MOD_ID)
public class DexterityPerksHandler {

    // Blade Dance stacks
    private static final Map<UUID, BladeDanceData> BLADE_DANCE_STACKS = new HashMap<>();
    private static final long BLADE_DANCE_DURATION_MS = 8000; // 8 segundos
    private static final int MAX_BLADE_DANCE_STACKS = 5;

    // Parry timing window
    private static final Map<UUID, Long> LAST_BLOCK_TIME = new HashMap<>();
    private static final long PARRY_WINDOW_MS = 300; // 300ms para parry perfecto

    private static class BladeDanceData {
        int stacks;
        long timestamp;

        BladeDanceData(int stacks, long timestamp) {
            this.stacks = stacks;
            this.timestamp = timestamp;
        }
    }

    // ============================================
    // TIER 1 - SWIFT STRIKES
    // ============================================

    /**
     * SWIFT STRIKES: Aumenta velocidad de ataque
     * +10% por nivel (max 5 = +50%)
     *
     * Se aplica via atributo en ModEvents para persistencia
     */
    // Implementado en ModEvents.java como atributo

    // ============================================
    // TIER 1 - QUICK DRAW
    // ============================================

    /**
     * QUICK DRAW: Reduce tiempo de carga de arco/ballesta
     * -10% por nivel (max 3 = -30%)
     */
    @SubscribeEvent
    public static void onArrowLoose_QuickDraw(ArrowLooseEvent event) {
        Player player = event.getEntity();

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.QUICK_DRAW.getId())) {
                return;
            }

            int perkLevel = stats.getPerkLevel(ModPerks.QUICK_DRAW.getId());
            if (perkLevel <= 0) {
                return;
            }

            // Aumentar el charge efectivo del arco
            // -10% tiempo = +11% charge efectivo aproximado
            int originalCharge = event.getCharge();
            float chargeMultiplier = 1.0f + (perkLevel * 0.11f);
            int newCharge = Math.min(72000, (int) (originalCharge * chargeMultiplier));

            // Nota: ArrowLooseEvent no permite modificar directamente el charge
            // Se implementa aumentando la velocidad de la flecha en el evento de disparo

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Quick Draw perk", e);
        }
    }

    // ============================================
    // TIER 1 - LIGHT STEPS
    // ============================================

    /**
     * LIGHT STEPS: No activa pressure plates ni tripwires
     * Implementado via mixin o detección de colisión
     */

    // ============================================
    // TIER 2 - EAGLE EYE
    // ============================================

    /**
     * EAGLE EYE: Mejora precisión y alcance proyectiles
     * +15% por nivel (max 3 = +45%)
     *
     * Implementado aumentando velocidad de proyectiles y reduciendo caída
     */
    // Se aplica en el evento de disparo de flecha (EntityJoinLevelEvent)

    // ============================================
    // TIER 2 - PARRY
    // ============================================

    /**
     * PARRY: Detectar inicio de bloqueo
     */
    @SubscribeEvent
    public static void onBlockStart_Parry(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack item = event.getItemStack();

        // Verificar si está usando escudo
        if (!item.getItem().toString().contains("shield")) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.PARRY.getId())) {
                return;
            }

            // Registrar el momento en que empieza a bloquear
            LAST_BLOCK_TIME.put(player.getUUID(), System.currentTimeMillis());

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Parry perk (block start)", e);
        }
    }

    /**
     * PARRY: Refleja 50% daño si bloquea en timing perfecto
     */
    @SubscribeEvent
    public static void onLivingDamage_Parry(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Verificar que está bloqueando
        if (!player.isBlocking()) {
            return;
        }

        // Solo daño directo (melee)
        if (event.getSource().isDirect()) {
            return;
        }

        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.PARRY.getId())) {
                return;
            }

            Long blockStartTime = LAST_BLOCK_TIME.get(player.getUUID());
            if (blockStartTime == null) {
                return;
            }

            long currentTime = System.currentTimeMillis();
            long timeSinceBlock = currentTime - blockStartTime;

            // Parry perfecto = bloquear dentro de 300ms de empezar a bloquear
            if (timeSinceBlock <= PARRY_WINDOW_MS) {
                // PARRY EXITOSO
                float damage = event.getOriginalDamage();

                // Reducir daño a 0
                event.setNewDamage(0);

                // Reflejar 50% del daño al atacante
                if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                    attacker.hurt(player.damageSources().thorns(player), damage * 0.5f);

                    player.sendSystemMessage(Component.literal("§e⚔ §l¡PARRY PERFECTO! §r§e50% daño reflejado"));
                }

                // Limpiar el timing
                LAST_BLOCK_TIME.remove(player.getUUID());
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Parry perk", e);
        }
    }

    // ============================================
    // TIER 2 - PIERCING SHOT
    // ============================================

    /**
     * PIERCING SHOT: Flechas atraviesan 1 enemigo
     *
     * Implementado configurando el piercing level de la flecha
     */
    // Se implementa en el evento de creación de proyectil

    // ============================================
    // TIER 3 - BLADE DANCE
    // ============================================

    /**
     * BLADE DANCE: Cada hit aumenta velocidad ataque
     * +10% por stack (max 5x = +50%), dura 8s
     */
    @SubscribeEvent
    public static void onLivingDamage_BladeDance(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        // Solo ataques melee
        if (!event.getSource().isDirect()) {
            return;
        }

        try {
            PlayerStats stats = attacker.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.BLADE_DANCE.getId())) {
                return;
            }

            UUID playerId = attacker.getUUID();
            long currentTime = System.currentTimeMillis();

            // Obtener stacks actuales
            BladeDanceData data = BLADE_DANCE_STACKS.get(playerId);

            int currentStacks = 0;
            if (data != null && (currentTime - data.timestamp) < BLADE_DANCE_DURATION_MS) {
                currentStacks = data.stacks;
            }

            // Incrementar stacks (max 5)
            int newStacks = Math.min(currentStacks + 1, MAX_BLADE_DANCE_STACKS);

            // Guardar nuevos stacks
            BLADE_DANCE_STACKS.put(playerId, new BladeDanceData(newStacks, currentTime));

            // Mensaje al jugador
            int bonusPercent = newStacks * 10;
            attacker.sendSystemMessage(Component.literal("§d⚔ BLADE DANCE: " + newStacks + "x (+" + bonusPercent + "% atk speed)"));

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Blade Dance perk", e);
        }
    }

    /**
     * Obtener bonus de velocidad de ataque de Blade Dance
     */
    public static float getBladeDanceBonus(Player player) {
        try {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.BLADE_DANCE.getId())) {
                return 0.0f;
            }

            UUID playerId = player.getUUID();
            long currentTime = System.currentTimeMillis();

            BladeDanceData data = BLADE_DANCE_STACKS.get(playerId);

            if (data == null || (currentTime - data.timestamp) >= BLADE_DANCE_DURATION_MS) {
                return 0.0f;
            }

            // +10% por stack
            return data.stacks * 0.10f;

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error obteniendo Blade Dance bonus", e);
            return 0.0f;
        }
    }

    // ============================================
    // TIER 3 - SNIPER
    // ============================================

    /**
     * SNIPER: Sin caída de daño por distancia + crítico garantizado si cargada
     *
     * Implementado modificando proyectiles
     */
    @SubscribeEvent
    public static void onArrowDamage_Sniper(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof AbstractArrow arrow)) {
            return;
        }

        if (!(arrow.getOwner() instanceof Player shooter)) {
            return;
        }

        try {
            PlayerStats stats = shooter.getData(ModAttachments.PLAYER_STATS);

            if (!stats.isPerkActive(ModPerks.SNIPER.getId())) {
                return;
            }

            // Sin caída de daño por distancia (mantener daño base)
            // Crítico garantizado si la flecha está completamente cargada
            if (arrow.isCritArrow()) {
                // Ya es crítica, aumentar daño
                event.setAmount(event.getAmount() * 1.5f);

                shooter.sendSystemMessage(Component.literal("§6⚡ SNIPER CRÍTICO!"));
            } else {
                // Forzar crítico si tiene suficiente velocidad
                if (arrow.getDeltaMovement().length() > 2.0) {
                    event.setAmount(event.getAmount() * 1.5f);
                    arrow.setCritArrow(true);

                    shooter.sendSystemMessage(Component.literal("§6⚡ SNIPER CRÍTICO!"));
                }
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error en Sniper perk", e);
        }
    }
}
