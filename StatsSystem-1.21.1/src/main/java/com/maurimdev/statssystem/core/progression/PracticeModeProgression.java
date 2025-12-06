package com.maurimdev.statssystem.core.progression;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.core.domain.stats.StatType;
import com.maurimdev.statssystem.init.ModItems;
import com.maurimdev.statssystem.infrastructure.config.PracticeBalanceConfig;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import com.maurimdev.statssystem.network.ModMessages;
import com.maurimdev.statssystem.network.SyncStatsPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🏃 PROGRESIÓN AUTOMÁTICA - MODO PRÁCTICA (MEJORADO)
 *
 * Sistema de progresión automática que recompensa el juego habilidoso:
 * - Críticos dan más XP
 * - Matar de un golpe da bonus
 * - Múltiples formas de subir cada stat
 * - Bonificaciones por técnica
 */
@EventBusSubscriber(modid = StatsSystem.MOD_ID)
public class PracticeModeProgression {

    private static final int SPRINT_TICK_INTERVAL = PracticeBalanceConfig.AGILITY_CHECK_INTERVAL;
    private static final int SYNC_INTERVAL = PracticeBalanceConfig.SYNC_INTERVAL;

    // Thread-safe HashMaps
    private static final Map<UUID, Boolean> pendingSyncs = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> lastSyncTick = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> lastFallDistance = new ConcurrentHashMap<>();
    private static final Map<UUID, Float> lastMobHealth = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> wasCritical = new ConcurrentHashMap<>();

    // ============================================
    // SISTEMA DE SINCRONIZACIÓN PERIÓDICA
    // ============================================

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!hasSoulBook(player)) {
            return;
        }

        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
        UUID playerUUID = player.getUUID();
        int currentTick = player.tickCount;

        // AGILITY - Sprint
        if (currentTick % SPRINT_TICK_INTERVAL == 0) {
            if (player.isSprinting() && !player.isInWater() && !player.isInLava()) {
                double xpGain = PracticeBalanceConfig.AGILITY_XP_PER_SPRINT_TICK;
                boolean leveledUp = stats.addXP(StatType.AGILITY, xpGain);

                if (leveledUp) {
                    syncImmediately(player, stats, StatType.AGILITY);
                } else {
                    markPendingSync(playerUUID);
                }
            }
        }

        // AGILITY - Nadar
        if (player.isSwimming() && currentTick % 20 == 0) {
            double xpGain = PracticeBalanceConfig.AGILITY_XP_PER_SWIM_TICK;
            boolean leveledUp = stats.addXP(StatType.AGILITY, xpGain);

            if (leveledUp) {
                syncImmediately(player, stats, StatType.AGILITY);
            } else {
                markPendingSync(playerUUID);
            }
        }

        // AGILITY - Detectar caída
        if (player.onGround() && player.fallDistance == 0) {
            Double lastFall = lastFallDistance.get(playerUUID);
            if (lastFall != null && lastFall > 3.0) {
                // XP por sobrevivir caída
                double blocksFallen = lastFall - 3.0;
                double xpGain = blocksFallen * PracticeBalanceConfig.AGILITY_XP_PER_FALL_BLOCK;
                boolean leveledUp = stats.addXP(StatType.AGILITY, xpGain);

                if (leveledUp) {
                    syncImmediately(player, stats, StatType.AGILITY);
                } else {
                    markPendingSync(playerUUID);
                }
            }
            lastFallDistance.remove(playerUUID);
        } else if (!player.onGround() && player.fallDistance > 0) {
            lastFallDistance.put(playerUUID, (double) player.fallDistance);
        }

        // Sincronizar cada 2 segundos
        if (currentTick % SYNC_INTERVAL == 0) {
            if (pendingSyncs.getOrDefault(playerUUID, false)) {
                ModMessages.sendToPlayer(new SyncStatsPacket(stats), player);
                pendingSyncs.put(playerUUID, false);
                lastSyncTick.put(playerUUID, currentTick);
            }
        }
    }

    // ============================================
    // MINING - Minar bloques (MEJORADO)
    // ============================================

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        if (!hasSoulBook(player)) {
            return;
        }

        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
        BlockState state = event.getState();
        float hardness = state.getDestroySpeed(player.level(), event.getPos());

        if (hardness > 0) {
            double xpGain = PracticeBalanceConfig.calculateMiningXP(hardness);

            // BONUS: Minar ores da 2x XP
            if (isOre(state.getBlock())) {
                xpGain *= PracticeBalanceConfig.MINING_ORE_MULTIPLIER;
            }

            // BONUS: Usar herramienta correcta da 1.3x XP
            if (isCorrectTool(player.getMainHandItem(), state)) {
                xpGain *= PracticeBalanceConfig.MINING_CORRECT_TOOL_MULTIPLIER;
            }

            boolean leveledUp = stats.addXP(StatType.MINING, xpGain);

            if (leveledUp) {
                syncImmediately(player, stats, StatType.MINING);
            } else {
                markPendingSync(player.getUUID());
            }
        }
    }

    // ============================================
    // DEXTERITY - Atacar mobs (MEJORADO)
    // ============================================

    @SubscribeEvent
    public static void onPlayerAttack(LivingDamageEvent.Pre event) {
        // Detectar si el ataque es crítico antes del daño
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }

        if (target instanceof Player) {
            return;
        }

        // Detectar crítico: jugador cayendo + no está en agua + velocidad Y negativa
        boolean isCritical = player.fallDistance > 0.0F && !player.onGround()
                && !player.isInWater() && player.getDeltaMovement().y < 0;

        wasCritical.put(player.getUUID(), isCritical);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        DamageSource source = event.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }

        if (target instanceof Player) {
            return;
        }

        if (!hasSoulBook(player)) {
            return;
        }

        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

        // DEXTERITY: XP base por golpe
        double dexterityXP = PracticeBalanceConfig.DEXTERITY_XP_PER_HIT;

        // BONUS: Crítico da 2x XP
        Boolean isCritical = wasCritical.get(player.getUUID());
        if (isCritical != null && isCritical) {
            dexterityXP *= PracticeBalanceConfig.DEXTERITY_CRITICAL_MULTIPLIER;
        }

        boolean dexterityLevelUp = stats.addXP(StatType.DEXTERITY, dexterityXP);

        if (dexterityLevelUp) {
            syncImmediately(player, stats, StatType.DEXTERITY);
        } else {
            markPendingSync(player.getUUID());
        }

        wasCritical.remove(player.getUUID());
    }

    // ============================================
    // STRENGTH - Matar mobs (MEJORADO)
    // ============================================

    @SubscribeEvent
    public static void onMobDamaged(LivingDamageEvent.Pre event) {
        // Guardamos la vida del mob antes del daño para detectar one-shots
        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }

        if (target instanceof Player) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        lastMobHealth.put(target.getUUID(), target.getHealth());
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }

        if (target instanceof Player) {
            return;
        }

        if (!hasSoulBook(player)) {
            return;
        }

        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
        float maxHealth = target.getMaxHealth();

        // Calcular XP base
        double xpGain = PracticeBalanceConfig.calculateStrengthXP(maxHealth);

        // BONUS: Matar de un solo golpe da 2x XP
        Float lastHealth = lastMobHealth.get(target.getUUID());
        if (lastHealth != null && lastHealth >= target.getMaxHealth() * 0.95f) {
            xpGain *= PracticeBalanceConfig.STRENGTH_ONESHOT_MULTIPLIER;
        }

        // BONUS: Crítico da 1.5x XP
        Boolean isCritical = wasCritical.get(player.getUUID());
        if (isCritical != null && isCritical) {
            xpGain *= PracticeBalanceConfig.STRENGTH_CRITICAL_MULTIPLIER;
        }

        boolean leveledUp = stats.addXP(StatType.STRENGTH, xpGain);

        // DEXTERITY BONUS: Matar con proyectil
        if (event.getSource().getDirectEntity() instanceof Projectile) {
            double dexterityBonus = PracticeBalanceConfig.DEXTERITY_PROJECTILE_KILL_BONUS;
            boolean dexLevelUp = stats.addXP(StatType.DEXTERITY, dexterityBonus);
            if (dexLevelUp) {
                syncImmediately(player, stats, StatType.DEXTERITY);
            }
        }

        if (leveledUp) {
            syncImmediately(player, stats, StatType.STRENGTH);
        } else {
            markPendingSync(player.getUUID());
        }

        lastMobHealth.remove(target.getUUID());
    }

    // ============================================
    // VITALITY - Recibir daño y curarse (MEJORADO)
    // ============================================

    @SubscribeEvent
    public static void onPlayerDamaged(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!hasSoulBook(player)) {
            return;
        }

        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
        float damage = event.getNewDamage();

        double vitalityXP = PracticeBalanceConfig.calculateVitalityXP(damage);
        boolean vitalityLevelUp = stats.addXP(StatType.VITALITY, vitalityXP);

        if (vitalityLevelUp) {
            syncImmediately(player, stats, StatType.VITALITY);
        } else {
            markPendingSync(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!hasSoulBook(player)) {
            return;
        }

        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
        double xpGain = PracticeBalanceConfig.VITALITY_XP_PER_HEAL;
        boolean leveledUp = stats.addXP(StatType.VITALITY, xpGain);

        if (leveledUp) {
            syncImmediately(player, stats, StatType.VITALITY);
        } else {
            markPendingSync(player.getUUID());
        }
    }

    // ============================================
    // DEXTERITY - Disparar flechas
    // ============================================

    @SubscribeEvent
    public static void onArrowShoot(ArrowLooseEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!hasSoulBook(player)) {
            return;
        }

        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
        double xpGain = PracticeBalanceConfig.DEXTERITY_XP_PER_ARROW;
        boolean leveledUp = stats.addXP(StatType.DEXTERITY, xpGain);

        if (leveledUp) {
            syncImmediately(player, stats, StatType.DEXTERITY);
        } else {
            markPendingSync(player.getUUID());
        }
    }

    // ============================================
    // AGILITY - Saltar mientras corre
    // ============================================

    @SubscribeEvent
    public static void onJump(net.neoforged.neoforge.event.entity.living.LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!hasSoulBook(player)) {
            return;
        }

        // Solo dar XP si está sprintando
        if (player.isSprinting()) {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
            double xpGain = PracticeBalanceConfig.AGILITY_XP_PER_JUMP;
            boolean leveledUp = stats.addXP(StatType.AGILITY, xpGain);

            if (leveledUp) {
                syncImmediately(player, stats, StatType.AGILITY);
            } else {
                markPendingSync(player.getUUID());
            }
        }
    }

    // ============================================
    // FARMING - Plantar cultivos
    // ============================================

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!hasSoulBook(player)) {
            return;
        }

        BlockState placedBlock = event.getPlacedBlock();
        Block block = placedBlock.getBlock();

        if (block instanceof CropBlock || block instanceof StemBlock || block instanceof SaplingBlock) {
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
            double xpGain = PracticeBalanceConfig.FARMING_XP_PER_PLANT;
            boolean leveledUp = stats.addXP(StatType.FARMING, xpGain);

            if (leveledUp) {
                syncImmediately(player, stats, StatType.FARMING);
            } else {
                markPendingSync(player.getUUID());
            }
        }
    }

    // ============================================
    // FARMING - Cosechar cultivos maduros
    // ============================================

    @SubscribeEvent
    public static void onCropHarvest(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        if (!hasSoulBook(player)) {
            return;
        }

        BlockState brokenBlock = event.getState();
        Block block = brokenBlock.getBlock();

        if (block instanceof CropBlock cropBlock) {
            if (cropBlock.isMaxAge(brokenBlock)) {
                PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
                double xpGain = PracticeBalanceConfig.FARMING_XP_PER_HARVEST;
                boolean leveledUp = stats.addXP(StatType.FARMING, xpGain);

                if (leveledUp) {
                    syncImmediately(player, stats, StatType.FARMING);
                } else {
                    markPendingSync(player.getUUID());
                }
            }
        }
    }

    // ============================================
    // FARMING - Usar bonemeal
    // ============================================

    @SubscribeEvent
    public static void onBonemealUse(BonemealEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        if (!event.isValidBonemealTarget()) {
            return;
        }

        if (!hasSoulBook(player)) {
            return;
        }

        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
        double xpGain = PracticeBalanceConfig.FARMING_XP_PER_BONEMEAL;
        boolean leveledUp = stats.addXP(StatType.FARMING, xpGain);

        if (leveledUp) {
            syncImmediately(player, stats, StatType.FARMING);
        } else {
            markPendingSync(player.getUUID());
        }
    }

    // ============================================
    // FARMING - Criar animales (NUEVO)
    // ============================================

    @SubscribeEvent
    public static void onAnimalBreed(net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent event) {
        if (!(event.getCausedByPlayer() instanceof ServerPlayer player)) {
            return;
        }

        if (!hasSoulBook(player)) {
            return;
        }

        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
        double xpGain = PracticeBalanceConfig.FARMING_XP_PER_BREED;
        boolean leveledUp = stats.addXP(StatType.FARMING, xpGain);

        if (leveledUp) {
            syncImmediately(player, stats, StatType.FARMING);
        } else {
            markPendingSync(player.getUUID());
        }
    }

    // ============================================
    // FARMING - Pescar (NUEVO)
    // ============================================

    @SubscribeEvent
    public static void onFish(ItemFishedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!hasSoulBook(player)) {
            return;
        }

        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
        double xpGain = PracticeBalanceConfig.FARMING_XP_PER_FISH;
        boolean leveledUp = stats.addXP(StatType.FARMING, xpGain);

        if (leveledUp) {
            syncImmediately(player, stats, StatType.FARMING);
        } else {
            markPendingSync(player.getUUID());
        }
    }

    // ============================================
    // MÉTODOS AUXILIARES
    // ============================================

    private static void syncImmediately(ServerPlayer player, PlayerStats stats, StatType stat) {
        ModMessages.sendToPlayer(new SyncStatsPacket(stats), player);

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS,
                1.0F, 1.0F);

        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                        getStatColor(stat) + getStatSymbol(stat) + " " +
                                stat.getDisplayName() + " subió a nivel " + stats.getLevel(stat) + "!"
                ),
                true
        );

        pendingSyncs.put(player.getUUID(), false);
        lastSyncTick.put(player.getUUID(), player.tickCount);
    }

    private static void markPendingSync(UUID playerUUID) {
        pendingSyncs.put(playerUUID, true);
    }

    private static String getStatColor(StatType stat) {
        return switch (stat) {
            case VITALITY -> "§4";
            case STRENGTH -> "§e";
            case DEXTERITY -> "§b";
            case MINING -> "§6";
            case AGILITY -> "§a";
            case FARMING -> "§2";
        };
    }

    private static String getStatSymbol(StatType stat) {
        return switch (stat) {
            case VITALITY -> "❤";
            case STRENGTH -> "💪";
            case DEXTERITY -> "🎯";
            case MINING -> "⛏";
            case AGILITY -> "👟";
            case FARMING -> "🌾";
        };
    }

    private static boolean isOre(Block block) {
        String blockName = BuiltInRegistries.BLOCK.getKey(block).toString();
        return blockName.contains("_ore") || blockName.contains("ancient_debris");
    }

    private static boolean isCorrectTool(ItemStack tool, BlockState state) {
        if (tool.isEmpty()) {
            return false;
        }
        return tool.isCorrectToolForDrops(state);
    }

    private static boolean hasSoulBook(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.SOUL_BOOK.get())) {
                return true;
            }
        }

        return CuriosApi.getCuriosInventory(player)
                .map(curiosInventory -> curiosInventory.findFirstCurio(ModItems.SOUL_BOOK.get()).isPresent())
                .orElse(false);
    }

    @SubscribeEvent
    public static void onPlayerLogout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() == null) {
            return;
        }

        try {
            UUID playerUUID = event.getEntity().getUUID();
            pendingSyncs.remove(playerUUID);
            lastSyncTick.remove(playerUUID);
            lastFallDistance.remove(playerUUID);
            wasCritical.remove(playerUUID);
            lastMobHealth.clear(); // Limpiar todos los mobs
        } catch (Exception e) {
            System.err.println("Error limpiando datos de PracticeModeProgression: " + e.getMessage());
        }
    }
}
