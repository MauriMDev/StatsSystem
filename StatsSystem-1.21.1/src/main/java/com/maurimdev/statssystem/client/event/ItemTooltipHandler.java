package com.maurimdev.statssystem.client.event;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.core.domain.equipment.EquipmentRequirement;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.core.domain.stats.StatType;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import com.maurimdev.statssystem.init.EquipmentRequirementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.Map;

/**
 * Handler para añadir tooltips de requisitos de stats a items de equipamiento
 * Se ejecuta DESPUÉS (prioridad LOW) del handler de escalado
 */
@EventBusSubscriber(modid = StatsSystem.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ItemTooltipHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();
        Player player = event.getEntity();

        // Verificar si el item tiene requisitos
        if (!EquipmentRequirementRegistry.hasRequirement(stack.getItem())) {
            return;
        }

        // Obtener los requisitos
        EquipmentRequirement requirement = EquipmentRequirementRegistry.getRequirement(stack);

        if (requirement.isEmpty()) {
            return;
        }

        // Obtener stats del jugador si está disponible
        PlayerStats playerStats = null;
        if (player != null) {
            playerStats = player.getData(ModAttachments.PLAYER_STATS);
        }

        // Verificar si cumple todos los requisitos
        boolean meetsAllRequirements = playerStats != null && requirement.meetsRequirements(playerStats);

        // ===== SECCIÓN DE REQUISITOS =====
        tooltip.add(Component.empty());
        String headerColor = meetsAllRequirements ? "§a" : "§c";
        tooltip.add(Component.literal("§8§m               §r " + headerColor + "⚠ Requisitos §8§m               "));

        // Mostrar cada requisito
        Map<StatType, Integer> requiredStats = requirement.getRequiredStats();
        for (Map.Entry<StatType, Integer> entry : requiredStats.entrySet()) {
            StatType stat = entry.getKey();
            int required = entry.getValue();

            // Símbolo y nombre del stat
            String symbol = getStatSymbol(stat);
            String name = getStatName(stat);

            // Verificar si el jugador cumple el requisito
            boolean meetsRequirement = true;
            int playerLevel = 0;

            if (playerStats != null) {
                playerLevel = playerStats.getLevel(stat);
                meetsRequirement = playerLevel >= required;
            }

            // Color y marca según si cumple o no
            String statusColor = meetsRequirement ? "§a" : "§c";
            String checkMark = meetsRequirement ? "✓" : "✗";

            // Formato: "  ✓ ⚔ Fuerza: 25 (32)"
            tooltip.add(Component.literal(
                String.format("  %s%s §7%s: §f%d %s(%d)", statusColor, checkMark, symbol + " " + name, required, statusColor, playerLevel)
            ));
        }

        // Si no cumple requisitos, añadir aviso
        if (!meetsAllRequirements && playerStats != null) {
            tooltip.add(Component.empty());
            tooltip.add(Component.literal("  §c§o⚠ No puedes usar este item"));
        }
    }

    private static String getStatSymbol(StatType statType) {
        return switch (statType) {
            case VITALITY -> "❤";
            case STRENGTH -> "💪";
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
}
