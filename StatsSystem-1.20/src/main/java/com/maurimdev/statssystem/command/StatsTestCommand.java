package com.maurimdev.statssystem.command;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.capability.PlayerStatsProvider;
import com.maurimdev.statssystem.stats.Difficulty;
import com.maurimdev.statssystem.stats.PlayerStats;
import com.maurimdev.statssystem.stats.ProgressionMode;
import com.maurimdev.statssystem.stats.StatType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Comando de prueba para verificar que el sistema de capabilities funciona
 *
 * Comandos disponibles:
 * - /statstest info     → Ver tus estadísticas actuales
 * - /statstest setup    → Configurar modo y dificultad
 * - /statstest set <stat> <valor> → Cambiar una estadística
 * - /statstest reset    → Resetear todas las stats a 0
 */
public class StatsTestCommand {

    /**
     * Registra el comando en el sistema de Minecraft
     * Este método debe ser llamado desde StatsSystem.java
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("statstest") // Comando base: /statstest
                        .requires(source -> source.hasPermission(2)) // Solo OPs nivel 2+

                        // Subcomando: /statstest info
                        .then(Commands.literal("info")
                                .executes(StatsTestCommand::showInfo)
                        )

                        // Subcomando: /statstest setup
                        .then(Commands.literal("setup")
                                .executes(StatsTestCommand::setupStats)
                        )

                        // Subcomando: /statstest set <stat> <valor>
                        .then(Commands.literal("set")
                                .then(Commands.literal("vitality")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 64))
                                                .executes(ctx -> setStat(ctx, StatType.VITALITY))
                                        )
                                )
                                .then(Commands.literal("endurance")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 64))
                                                .executes(ctx -> setStat(ctx, StatType.ENDURANCE))
                                        )
                                )
                                .then(Commands.literal("strength")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 64))
                                                .executes(ctx -> setStat(ctx, StatType.STRENGTH))
                                        )
                                )
                                .then(Commands.literal("combat")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 64))
                                                .executes(ctx -> setStat(ctx, StatType.COMBAT))
                                        )
                                )
                                .then(Commands.literal("mining")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 64))
                                                .executes(ctx -> setStat(ctx, StatType.MINING))
                                        )
                                )
                                .then(Commands.literal("agility")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 64))
                                                .executes(ctx -> setStat(ctx, StatType.AGILITY))
                                        )
                                )
                        )

                        // Subcomando: /statstest reset
                        .then(Commands.literal("reset")
                                .executes(StatsTestCommand::resetStats)
                        )
        );
    }

    // ============================================
    // COMANDO: /statstest info
    // ============================================

    /**
     * Muestra todas las estadísticas del jugador
     */
    private static int showInfo(CommandContext<CommandSourceStack> context) {
        // Obtener el jugador que ejecutó el comando
        ServerPlayer player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Este comando solo puede ser usado por jugadores"));
            return 0;
        }

        // Obtener la capability
        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(statsCapability -> {
            PlayerStats stats = statsCapability.getStats();

            // Enviar mensajes al jugador
            player.sendSystemMessage(Component.literal("§6§l=== TUS ESTADÍSTICAS ==="));
            player.sendSystemMessage(Component.literal(""));

            // Mostrar configuración
            if (stats.hasSelectedMode()) {
                player.sendSystemMessage(Component.literal("§eModo: §f" + stats.getMode().getDisplayName()));
                player.sendSystemMessage(Component.literal("§eDificultad: §f" + stats.getDifficulty().getDisplayName() +
                        " §c§l[BLOQUEADA]"));
            } else {
                player.sendSystemMessage(Component.literal("§c¡Aún no has configurado tu modo!"));
                player.sendSystemMessage(Component.literal("§7Usa /statstest setup para configurar"));

                // Mostrar dificultad detectada del mundo
                net.minecraft.world.Difficulty worldDiff = player.level().getDifficulty();
                player.sendSystemMessage(Component.literal("§7Dificultad del mundo: §f" + worldDiff.name()));
            }

            player.sendSystemMessage(Component.literal(""));

            // Mostrar cada estadística
            for (StatType stat : StatType.values()) {
                int level = stats.getLevel(stat);
                double bonus = stats.getBonus(stat);
                boolean softCap = stats.hasReachedSoftCap(stat);

                String softCapText = softCap ? " §c(Soft Cap alcanzado)" : "";

                player.sendSystemMessage(Component.literal(
                        stat.getSymbol() + " §b" + stat.getDisplayName() + ": §f" + level + "/64 " +
                                "§7(Bonus: " + String.format("%.2f", bonus) + ")" + softCapText
                ));
            }

            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("§eNivel Total: §f" + stats.getTotalLevel()));
            player.sendSystemMessage(Component.literal("§6§l======================="));
        });

        return 1;
    }

    // ============================================
    // COMANDO: /statstest setup
    // ============================================

    /**
     * Configura el modo y dificultad (solo para testing)
     */
    private static int setupStats(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Este comando solo puede ser usado por jugadores"));
            return 0;
        }

        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(statsCapability -> {
            PlayerStats stats = statsCapability.getStats();

            // Verificar si ya está configurado
            if (stats.hasSelectedMode()) {
                player.sendSystemMessage(Component.literal("§c✗ Ya has configurado tu modo y dificultad"));
                player.sendSystemMessage(Component.literal("§7Usa /statstest reset para reiniciar"));
                return;
            }

            // NUEVO: Obtener la dificultad ACTUAL del mundo
            net.minecraft.world.Difficulty currentWorldDiff = player.level().getDifficulty();

            // Configurar modo
            stats.setMode(ProgressionMode.XP);

            // Configurar dificultad del MOD basándose en el mundo
            stats.setDifficultyFromWorld(currentWorldDiff);
            stats.setHasSelectedMode(true);

            // IMPORTANTE: Bloquear la dificultad del mundo
            // (Para testing, usamos la dificultad actual)
            // Más adelante, la GUI permitirá elegir y cambiar el mundo

            player.sendSystemMessage(Component.literal("§a✓ Configuración aplicada:"));
            player.sendSystemMessage(Component.literal("  §7Modo: §f" + stats.getMode().getDisplayName()));
            player.sendSystemMessage(Component.literal("  §7Dificultad: §f" + stats.getDifficulty().getDisplayName()));
            player.sendSystemMessage(Component.literal("  §c§lDificultad BLOQUEADA"));
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("§7Ya no puedes cambiar la dificultad del mundo"));
            player.sendSystemMessage(Component.literal("§7ni desde el menú ni con comandos"));
            player.sendSystemMessage(Component.literal("§7hasta que resetees con §f/statstest reset"));

            StatsSystem.LOGGER.info("Stats configuradas y dificultad bloqueada para " +
                    player.getName().getString());
        });

        return 1;
    }

    // ============================================
    // COMANDO: /statstest set <stat> <valor>
    // ============================================

    /**
     * Establece el valor de una estadística específica
     */
    private static int setStat(CommandContext<CommandSourceStack> context, StatType stat) {
        ServerPlayer player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Este comando solo puede ser usado por jugadores"));
            return 0;
        }

        // Obtener el valor del argumento
        int value = IntegerArgumentType.getInteger(context, "value");

        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(statsCapability -> {
            PlayerStats stats = statsCapability.getStats();

            // Establecer el nuevo valor
            int oldValue = stats.getLevel(stat);
            stats.setLevel(stat, value);
            double bonus = stats.getBonus(stat);

            // Mensaje de confirmación
            player.sendSystemMessage(Component.literal(
                    "§a✓ " + stat.getSymbol() + " " + stat.getDisplayName() +
                            " cambiada: §f" + oldValue + " §7→ §f" + value
            ));
            player.sendSystemMessage(Component.literal(
                    "  §7Bonus actual: §f" + String.format("%.2f", bonus)
            ));

            StatsSystem.LOGGER.info(player.getName().getString() + " cambió " +
                    stat.name() + " a " + value);
        });

        return 1;
    }

    // ============================================
    // COMANDO: /statstest reset
    // ============================================

    /**
     * Resetea todas las estadísticas a 0
     */
    private static int resetStats(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Este comando solo puede ser usado por jugadores"));
            return 0;
        }

        player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(statsCapability -> {
            PlayerStats stats = statsCapability.getStats();

            // Resetear stats
            stats.reset();

            // Desbloquear la configuración
            stats.setMode(null);
            stats.setDifficulty(null);
            stats.setHasSelectedMode(false);

            player.sendSystemMessage(Component.literal("§c§l✓ REINICIO COMPLETO"));
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("§7Se ha reseteado:"));
            player.sendSystemMessage(Component.literal("  §c- Todas las estadísticas → 0"));
            player.sendSystemMessage(Component.literal("  §c- Modo de juego → Sin configurar"));
            player.sendSystemMessage(Component.literal("  §c- Dificultad → Sin configurar"));
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal("§a✓ La dificultad del mundo está DESBLOQUEADA"));
            player.sendSystemMessage(Component.literal("§7Ahora puedes:"));
            player.sendSystemMessage(Component.literal("  §71. Cambiar la dificultad del mundo en el menú"));
            player.sendSystemMessage(Component.literal("  §72. Ejecutar §f/statstest setup §7para reconfigurar"));

            StatsSystem.LOGGER.info("RESET COMPLETO para " + player.getName().getString() +
                    " - Dificultad desbloqueada");
        });

        return 1;
    }
}