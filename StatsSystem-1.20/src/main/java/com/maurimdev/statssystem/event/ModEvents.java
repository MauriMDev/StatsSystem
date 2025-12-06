package com.maurimdev.statssystem.event;

import com.maurimdev.statssystem.stats.PlayerStats;
import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.capability.IPlayerStatsCapability;
import com.maurimdev.statssystem.capability.PlayerStatsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Clase que maneja todos los eventos principales del mod
 * relacionados con el ciclo de vida de las capabilities
 *
 * @Mod.EventBusSubscriber hace que Forge registre automáticamente
 * todos los métodos con @SubscribeEvent
 */
@Mod.EventBusSubscriber(modid = StatsSystem.MOD_ID)
public class ModEvents {

    // ============================================
    // EVENTO 1: REGISTRAR LA CAPABILITY
    // ============================================

    /**
     * Se ejecuta una sola vez al inicio del juego
     * Registra la capability en el sistema de Forge
     * <p>
     * Sin este evento, Forge no sabría que nuestra capability existe
     *
     * @param event Evento de registro de capabilities
     */
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        // Registrar la capability en Forge
        // Le decimos: "Existe una capability del tipo IPlayerStatsCapability"
        event.register(IPlayerStatsCapability.class);

        // Log de confirmación
        StatsSystem.LOGGER.info("Capability PlayerStats registrada correctamente");
    }

    // ============================================
    // EVENTO 2: ADJUNTAR CAPABILITY AL JUGADOR
    // ============================================

    /**
     * Se ejecuta cuando se crea cualquier entidad en el mundo
     * Adjuntamos nuestro Provider solo a los jugadores
     * <p>
     * Flujo:
     * 1. Jugador entra al mundo
     * 2. Forge crea la entidad Player
     * 3. Forge dispara este evento: "¿Alguien quiere adjuntar algo a esta entidad?"
     * 4. Verificamos si es un jugador
     * 5. Si lo es, adjuntamos nuestro PlayerStatsProvider
     * 6. Ahora el jugador tiene acceso a sus stats
     *
     * @param event Evento de adjuntar capabilities
     */
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        // Verificar si la entidad es un jugador
        if (event.getObject() instanceof Player) {

            // Verificar que no tenga ya la capability
            // (previene duplicados, aunque no debería pasar)
            if (!event.getObject().getCapability(PlayerStatsProvider.PLAYER_STATS).isPresent()) {

                // Crear un ResourceLocation único para identificar nuestra capability
                // Formato: "modid:nombre"
                // Ejemplo: "statssystem:player_stats"
                ResourceLocation location = new ResourceLocation(
                        StatsSystem.MOD_ID,
                        "player_stats"
                );

                // Adjuntar el provider al jugador
                // addCapability(id, provider)
                // - id: Identificador único
                // - provider: Instancia de PlayerStatsProvider que creamos
                event.addCapability(location, new PlayerStatsProvider());

                // Log de confirmación (solo en desarrollo)
                // StatsSystem.LOGGER.debug("Capability adjuntada al jugador: " + event.getObject().getName().getString());
            }
        }
    }

    // ============================================
    // EVENTO 3: COPIAR DATOS AL MORIR/RESPAWNEAR
    // ============================================

    /**
     * Se ejecuta cuando el jugador muere y respawnea
     * <p>
     * Problema:
     * - Jugador muere
     * - Minecraft crea una NUEVA entidad jugador
     * - La nueva entidad tiene capabilities VACÍAS
     * - Sin este evento, perderías todas tus stats al morir
     * <p>
     * Solución:
     * - Este evento nos da acceso al jugador VIEJO (antes de morir)
     * - Y al jugador NUEVO (después de respawnear)
     * - Copiamos los datos del viejo al nuevo
     *
     * @param event Evento de clonación del jugador
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // Verificar que sea después de la muerte (no por cambio de dimensión)
        if (event.isWasDeath()) {

            // Obtener el jugador VIEJO (el que murió)
            Player oldPlayer = event.getOriginal();

            // Obtener el jugador NUEVO (el que respawneó)
            Player newPlayer = event.getEntity();

            // Obtener la capability del jugador viejo
            oldPlayer.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(oldStats -> {

                // Obtener la capability del jugador nuevo
                newPlayer.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(newStats -> {

                    // Copiar todos los datos del viejo al nuevo
                    // Usa el método copyFrom() que implementaste en PlayerStatsStorage
                    newStats.copyFrom(oldStats);

                    // Log de confirmación
                    StatsSystem.LOGGER.info("Stats copiadas después de la muerte para: "
                            + newPlayer.getName().getString());
                });
            });
        }
    }

    // ============================================
    // EVENTO 4: JUGADOR ENTRA AL MUNDO
    // ============================================

    /**
     * Se ejecuta cuando un jugador entra al mundo (login)
     * Útil para sincronizar datos con el cliente en multiplayer
     * <p>
     * En singleplayer no es tan crítico, pero en servidores:
     * - El servidor tiene los datos reales
     * - El cliente necesita una copia para mostrar en la GUI
     * - Este es el momento perfecto para sincronizar
     * <p>
     * NOTA: Por ahora solo hacemos un log, más adelante añadiremos
     * el sistema de networking para sincronizar
     *
     * @param event Evento de jugador entrando
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {

            player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(statsCapability -> {
                PlayerStats stats = statsCapability.getStats();

                // Si es la primera vez (no ha seleccionado modo)
                if (!stats.hasSelectedMode()) {
                    // Establecer dificultad basándose en el mundo
                    net.minecraft.world.Difficulty worldDifficulty = player.level().getDifficulty();
                    stats.setDifficultyFromWorld(worldDifficulty);

                    StatsSystem.LOGGER.info(player.getName().getString() +
                            " - Dificultad automática establecida: " +
                            stats.getDifficulty().getDisplayName() +
                            " (basada en dificultad del mundo: " + worldDifficulty.name() + ")");
                }

                // Log de bienvenida
                int totalLevel = stats.getTotalLevel();
                StatsSystem.LOGGER.info(player.getName().getString() +
                        " entró al mundo. Nivel total: " + totalLevel);

                // TODO: Aquí irá el código de sincronización (Fase siguiente)
            });
        }
    }

    // ============================================
    // EVENTO 5: JUGADOR SALE DEL MUNDO
    // ============================================

    /**
     * Se ejecuta cuando un jugador sale del mundo (logout)
     * <p>
     * Buen momento para:
     * - Guardar datos manualmente (aunque Forge ya lo hace)
     * - Limpiar caches
     * - Hacer logs de auditoría
     *
     * @param event Evento de jugador saliendo
     */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {

            // Log de despedida
            player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
                int totalLevel = stats.getStats().getTotalLevel();
                StatsSystem.LOGGER.info(player.getName().getString()
                        + " salió del mundo. Nivel total guardado: " + totalLevel);
            });
        }
    }

    // ============================================
    // EVENTO 6: CAMBIO DE DIMENSIÓN
    // ============================================

    /**
     * Se ejecuta cuando el jugador cambia de dimensión
     * (Overworld ↔ Nether ↔ End)
     * <p>
     * Similar al evento de muerte, pero sin morir:
     * - Minecraft puede crear una nueva entidad en la nueva dimensión
     * - Necesitamos copiar los datos si es necesario
     *
     * @param event Evento de cambio de dimensión
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {

            // Verificar que los datos persistan
            player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {

                // Log de confirmación
                StatsSystem.LOGGER.info(player.getName().getString()
                        + " cambió de dimensión. Stats intactas.");

                // TODO: Re-sincronizar con el cliente
                // ModMessages.sendToPlayer(new SyncStatsPacket(stats.getStats()), player);
            });
        }
    }

    // ============================================
// EVENTO: VERIFICAR Y FORZAR DIFICULTAD BLOQUEADA
// ============================================

    /**
     * Se ejecuta cada tick del servidor para verificar
     * que la dificultad no haya sido cambiada manualmente
     * <p>
     * Si detecta un cambio, lo revierte inmediatamente
     */
    @SubscribeEvent
    public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
        // Solo verificar al final del tick
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) {
            return;
        }

        // Verificar cada 20 ticks (1 segundo) en lugar de cada tick
        if (event.getServer().getTickCount() % 20 != 0) {
            return;
        }

        // Verificar cada jugador
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(statsCapability -> {
                PlayerStats stats = statsCapability.getStats();

                // Si la dificultad está bloqueada
                if (stats.isDifficultyLocked()) {
                    // Obtener la dificultad configurada en el mod
                    net.minecraft.world.Difficulty modDifficulty = convertToMinecraftDifficulty(stats.getDifficulty());

                    // Obtener la dificultad actual del mundo
                    net.minecraft.world.Difficulty worldDifficulty = player.getServer().getWorldData()
                            .getDifficulty();

                    // Si son diferentes, forzar la del mod
                    if (worldDifficulty != modDifficulty) {
                        player.getServer().setDifficulty(modDifficulty, true);

                        // Notificar al jugador
                        player.sendSystemMessage(Component.literal(
                                "§c§l✗ Dificultad revertida automáticamente"
                        ));
                        player.sendSystemMessage(Component.literal(
                                "§7La dificultad está bloqueada en: §f" + stats.getDifficulty().getDisplayName()
                        ));
                        player.sendSystemMessage(Component.literal(
                                "§7Usa §f/statstest reset §7para desbloquear"
                        ));

                        StatsSystem.LOGGER.warn("Dificultad revertida para mantener el bloqueo - Jugador: " +
                                player.getName().getString());
                    }
                }
            });
        }
    }

    /**
     * Convierte la dificultad del mod a la dificultad de Minecraft
     */
    private static net.minecraft.world.Difficulty convertToMinecraftDifficulty(
            com.maurimdev.statssystem.stats.Difficulty modDifficulty) {
        return switch (modDifficulty) {
            case EASY -> net.minecraft.world.Difficulty.EASY;
            case NORMAL -> net.minecraft.world.Difficulty.NORMAL;
            case HARD, EXTREME -> net.minecraft.world.Difficulty.HARD;
        };
    }
}