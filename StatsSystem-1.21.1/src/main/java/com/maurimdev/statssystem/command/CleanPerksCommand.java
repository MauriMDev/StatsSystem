package com.maurimdev.statssystem.command;

import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import com.maurimdev.statssystem.network.ModMessages;
import com.maurimdev.statssystem.network.SyncPerksPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Comando para limpiar perks obsoletos y resetear estados corruptos
 */
public class CleanPerksCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("cleanperks")
                .requires(source -> source.hasPermission(2)) // Requiere permisos de OP
                .executes(CleanPerksCommand::execute)
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("Este comando solo puede ser usado por jugadores"));
            return 0;
        }

        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

        // Lista de perks obsoletos a eliminar
        List<String> obsoletePerks = new ArrayList<>();
        obsoletePerks.add("vitality_regeneration_1");
        obsoletePerks.add("vitality_regeneration_2");
        obsoletePerks.add("vitality_regeneration_3");

        int removed = 0;
        for (String perkId : obsoletePerks) {
            if (stats.hasPerk(perkId)) {
                stats.removePerk(perkId);
                removed++;
            }
        }

        // Limpiar perks desactivados (activar todos)
        List<String> allPerks = new ArrayList<>(stats.getUnlockedPerks());
        int activated = 0;
        for (String perkId : allPerks) {
            if (stats.isPerkDisabled(perkId)) {
                stats.enablePerk(perkId);
                activated++;
            }
        }

        // Sincronizar con el cliente
        ModMessages.sendToPlayer(new SyncPerksPacket(stats), player);

        // Mensaje de éxito
        player.sendSystemMessage(Component.literal("§a✓ Limpieza completada:"));
        player.sendSystemMessage(Component.literal("  §7- Perks obsoletos eliminados: §e" + removed));
        player.sendSystemMessage(Component.literal("  §7- Perks reactivados: §e" + activated));
        player.sendSystemMessage(Component.literal("  §7- Total perks activos: §e" + stats.getTotalPerksUnlocked()));

        return 1;
    }
}
