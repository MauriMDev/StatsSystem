package com.maurimdev.statssystem.gameplay.command;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.core.domain.stats.StatType;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Comandos administrativos para gestionar el sistema de estadísticas
 *
 * Comandos disponibles:
 * - /stats info               → Ver tus estadísticas actuales
 * - /stats set <stat> <valor> → Establecer un nivel específico
 * - /stats add <stat> <valor> → Sumar niveles
 * - /stats addxp <stat> <xp>  → Sumar XP
 * - /stats setxp <stat> <xp>  → Establecer XP
 * - /stats reset              → Resetear todas las stats a 0
 * - /stats max                → Subir todas las stats a 64
 * - /stats perk <perkId>      → Dar/quitar un perk (testing)
 * - /stats perks              → Listar perks desbloqueados
 */
public class StatsCommand {

    /**
     * Registra el comando en el sistema de Minecraft
     * Este método debe ser llamado desde StatsSystem.java
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("stats") // Comando base: /stats
                        .requires(source -> source.hasPermission(2)) // Solo OPs nivel 2+

                        // Subcomando: /stats info
                        .then(Commands.literal("info")
                                .executes(StatsCommand::showInfo)
                        )

                        // Subcomando: /stats set <stat> <valor>
                        .then(Commands.literal("set")
                                .then(Commands.literal("vitality")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 64))
                                                .executes(ctx -> setStat(ctx, StatType.VITALITY))
                                        )
                                )
                                .then(Commands.literal("strength")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 64))
                                                .executes(ctx -> setStat(ctx, StatType.STRENGTH))
                                        )
                                )
                                .then(Commands.literal("dexterity")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 64))
                                                .executes(ctx -> setStat(ctx, StatType.DEXTERITY))
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
                                .then(Commands.literal("farming")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 64))
                                                .executes(ctx -> setStat(ctx, StatType.FARMING))
                                        )
                                )
                        )

                        // Subcomando: /stats add <stat> <valor>
                        .then(Commands.literal("add")
                                .then(Commands.literal("vitality")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 64))
                                                .executes(ctx -> addStat(ctx, StatType.VITALITY))
                                        )
                                )
                                .then(Commands.literal("strength")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 64))
                                                .executes(ctx -> addStat(ctx, StatType.STRENGTH))
                                        )
                                )
                                .then(Commands.literal("dexterity")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 64))
                                                .executes(ctx -> addStat(ctx, StatType.DEXTERITY))
                                        )
                                )
                                .then(Commands.literal("mining")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 64))
                                                .executes(ctx -> addStat(ctx, StatType.MINING))
                                        )
                                )
                                .then(Commands.literal("agility")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 64))
                                                .executes(ctx -> addStat(ctx, StatType.AGILITY))
                                        )
                                )
                                .then(Commands.literal("farming")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 64))
                                                .executes(ctx -> addStat(ctx, StatType.FARMING))
                                        )
                                )
                        )

                        // Subcomando: /stats addxp <stat> <xp>
                        .then(Commands.literal("addxp")
                                .then(Commands.literal("vitality")
                                        .then(Commands.argument("xp", IntegerArgumentType.integer(1))
                                                .executes(ctx -> addXP(ctx, StatType.VITALITY))
                                        )
                                )
                                .then(Commands.literal("strength")
                                        .then(Commands.argument("xp", IntegerArgumentType.integer(1))
                                                .executes(ctx -> addXP(ctx, StatType.STRENGTH))
                                        )
                                )
                                .then(Commands.literal("dexterity")
                                        .then(Commands.argument("xp", IntegerArgumentType.integer(1))
                                                .executes(ctx -> addXP(ctx, StatType.DEXTERITY))
                                        )
                                )
                                .then(Commands.literal("mining")
                                        .then(Commands.argument("xp", IntegerArgumentType.integer(1))
                                                .executes(ctx -> addXP(ctx, StatType.MINING))
                                        )
                                )
                                .then(Commands.literal("agility")
                                        .then(Commands.argument("xp", IntegerArgumentType.integer(1))
                                                .executes(ctx -> addXP(ctx, StatType.AGILITY))
                                        )
                                )
                                .then(Commands.literal("farming")
                                        .then(Commands.argument("xp", IntegerArgumentType.integer(1))
                                                .executes(ctx -> addXP(ctx, StatType.FARMING))
                                        )
                                )
                        )

                        // Subcomando: /stats max (todas las stats a 64)
                        .then(Commands.literal("max")
                                .executes(StatsCommand::maxStats)
                        )

                        // Subcomando: /stats reset
                        .then(Commands.literal("reset")
                                .executes(StatsCommand::resetStats)
                        )

                        // Subcomando: /stats perks (listar perks)
                        .then(Commands.literal("perks")
                                .executes(StatsCommand::listPerks)
                        )

                        // Subcomando: /stats perk <perkId> (dar/quitar perk)
                        .then(Commands.literal("perk")
                                .then(Commands.argument("perkId", com.mojang.brigadier.arguments.StringArgumentType.string())
                                        .executes(StatsCommand::togglePerk)
                                )
                        )
        );
    }

    // ============================================
    // COMANDO: /stats info
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

        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

        // Enviar mensajes al jugador
        player.sendSystemMessage(Component.literal("§6§l=== TUS ESTADÍSTICAS ==="));
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

        return 1;
    }

    // ============================================
    // COMANDO: /stats set <stat> <valor>
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

        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

        // Establecer el nuevo valor
        int oldValue = stats.getLevel(stat);
        stats.setLevel(stat, value);
        double bonus = stats.getBonus(stat);

        // Forzar sincronización con el cliente
        player.setData(ModAttachments.PLAYER_STATS, stats);

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

        return 1;
    }

    // ============================================
    // COMANDO: /stats reset
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

        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

        // Resetear stats
        stats.reset();

        // Forzar sincronización con el cliente
        player.setData(ModAttachments.PLAYER_STATS, stats);

        player.sendSystemMessage(Component.literal("§c§l✓ REINICIO COMPLETO"));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§7Se ha reseteado:"));
        player.sendSystemMessage(Component.literal("  §c- Todas las estadísticas → 0"));
        player.sendSystemMessage(Component.literal("  §c- Todo el progreso XP → 0"));

        StatsSystem.LOGGER.info("RESET COMPLETO para " + player.getName().getString());

        return 1;
    }

    // ============================================
    // COMANDO: /stats add <stat> <valor>
    // ============================================

    /**
     * Suma niveles a una estadística
     */
    private static int addStat(CommandContext<CommandSourceStack> context, StatType stat) {
        ServerPlayer player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Este comando solo puede ser usado por jugadores"));
            return 0;
        }

        int amount = IntegerArgumentType.getInteger(context, "value");
        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

        int oldValue = stats.getLevel(stat);
        int newValue = Math.min(64, oldValue + amount);
        stats.setLevel(stat, newValue);

        // Forzar sincronización con el cliente
        player.setData(ModAttachments.PLAYER_STATS, stats);

        player.sendSystemMessage(Component.literal(
                "§a✓ " + stat.getSymbol() + " " + stat.getDisplayName() +
                        ": §f" + oldValue + " §7→ §f" + newValue + " §a(+"+amount+")"
        ));

        StatsSystem.LOGGER.info(player.getName().getString() + " añadió +" + amount + " a " + stat.name());

        return 1;
    }

    // ============================================
    // COMANDO: /stats addxp <stat> <xp>
    // ============================================

    /**
     * Suma XP a una estadística
     */
    private static int addXP(CommandContext<CommandSourceStack> context, StatType stat) {
        ServerPlayer player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Este comando solo puede ser usado por jugadores"));
            return 0;
        }

        int xpAmount = IntegerArgumentType.getInteger(context, "xp");
        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

        double oldXP = stats.getXP(stat);
        stats.addXP(stat, xpAmount);
        double newXP = stats.getXP(stat);
        int level = stats.getLevel(stat);

        player.sendSystemMessage(Component.literal(
                "§a✓ +" + xpAmount + " XP a " + stat.getSymbol() + " " + stat.getDisplayName()
        ));
        player.sendSystemMessage(Component.literal(
                "  §7XP: §f" + String.format("%.0f", oldXP) + " §7→ §f" + String.format("%.0f", newXP) + " §8(Nivel " + level + ")"
        ));

        StatsSystem.LOGGER.info(player.getName().getString() + " añadió " + xpAmount + " XP a " + stat.name());

        return 1;
    }

    // ============================================
    // COMANDO: /stats max
    // ============================================

    /**
     * Sube todas las estadísticas al máximo (64)
     */
    private static int maxStats(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Este comando solo puede ser usado por jugadores"));
            return 0;
        }

        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

        // Establecer todas las stats a 64
        for (StatType stat : StatType.values()) {
            stats.setLevel(stat, 64);
        }

        // IMPORTANTE: Forzar actualización de los attachments y sincronización con el cliente
        // Esto asegura que los datos se guarden y se envíen al cliente correctamente
        player.setData(ModAttachments.PLAYER_STATS, stats);

        player.sendSystemMessage(Component.literal("§6§l✓ ESTADÍSTICAS AL MÁXIMO"));
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§7Todas las estadísticas están ahora en §f64§7/64"));
        player.sendSystemMessage(Component.literal("§7¡Eres imparable!"));

        StatsSystem.LOGGER.info(player.getName().getString() + " subió todas las stats a 64");

        return 1;
    }

    // ============================================
    // COMANDO: /stats perks
    // ============================================

    /**
     * Lista todos los perks desbloqueados
     */
    private static int listPerks(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Este comando solo puede ser usado por jugadores"));
            return 0;
        }

        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

        player.sendSystemMessage(Component.literal("§6§l=== PERKS DESBLOQUEADOS ==="));
        player.sendSystemMessage(Component.literal(""));

        if (stats.getTotalPerksUnlocked() == 0) {
            player.sendSystemMessage(Component.literal("§cNo tienes perks desbloqueados"));
        } else {
            for (String perkId : stats.getUnlockedPerks()) {
                int level = stats.getPerkLevel(perkId);
                player.sendSystemMessage(Component.literal("§e• §f" + perkId + " §7(Nivel " + level + ")"));
            }
        }

        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§7Total: §e" + stats.getTotalPerksUnlocked() + " §7perks"));

        return 1;
    }

    // ============================================
    // COMANDO: /stats perk <perkId>
    // ============================================

    /**
     * Da o quita un perk (toggle)
     */
    private static int togglePerk(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFailure(Component.literal("Este comando solo puede ser usado por jugadores"));
            return 0;
        }

        String perkId = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "perkId");
        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

        if (stats.hasPerk(perkId)) {
            // Quitar perk
            stats.removePerk(perkId);
            player.sendSystemMessage(Component.literal("§c✗ Perk removido: §f" + perkId));
            StatsSystem.LOGGER.info("{} removió perk: {}", player.getName().getString(), perkId);
        } else {
            // Dar perk nivel 1
            stats.unlockPerk(perkId);
            player.sendSystemMessage(Component.literal("§a✓ Perk desbloqueado: §f" + perkId));
            StatsSystem.LOGGER.info("{} desbloqueó perk: {}", player.getName().getString(), perkId);
        }

        // Forzar guardado
        player.getData(ModAttachments.PLAYER_STATS).saveNBTData();

        return 1;
    }
}
