package com.maurimdev.statssystem.gameplay.event;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.core.domain.stats.StatType;
import com.maurimdev.statssystem.gameplay.scaling.WeaponScaling;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler que aplica el escalado de armas basado en las stats del jugador
 * Sistema inspirado en Dark Souls
 *
 * Usa LivingIncomingDamageEvent para modificar el daño ANTES de que se aplique
 * la reducción de armadura, permitiendo que el sistema de protección funcione correctamente.
 */
@EventBusSubscriber(modid = StatsSystem.MOD_ID)
public class WeaponScalingHandler {

    /**
     * Aplica el escalado de armas al daño ANTES de la reducción de armadura
     */
    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        // Solo procesar si el atacante es un jugador
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        try {
            // Obtener el arma que está usando
            ItemStack weapon = player.getMainHandItem();
            if (weapon.isEmpty()) {
                return;
            }

            // Obtener la configuración de escalado del arma
            WeaponScaling.ScalingConfig scalingConfig = WeaponScaling.getScalingForItem(weapon);
            if (!scalingConfig.hasScaling()) {
                return;
            }

            // Obtener los stats del jugador
            PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);

            // Crear mapa de niveles de stats
            Map<StatType, Integer> statLevels = new HashMap<>();
            for (StatType statType : StatType.values()) {
                statLevels.put(statType, stats.getLevel(statType));
            }

            // Obtener el daño actual (puede haber sido modificado por otros handlers)
            float currentDamage = event.getAmount();

            StatsSystem.LOGGER.info("═══ WEAPON SCALING DEBUG ═══");
            StatsSystem.LOGGER.info("Original damage: {}", currentDamage);
            StatsSystem.LOGGER.info("Current damage (before scaling): {}", currentDamage);

            // Calcular el bonus de escalado basado en el daño base del arma
            double scalingBonus = WeaponScaling.calculateScalingBonus(
                currentDamage,
                scalingConfig,
                statLevels
            );

            StatsSystem.LOGGER.info("Scaling bonus calculated: {}", scalingBonus);

            // Aplicar el bonus al daño
            if (scalingBonus > 0) {
                float newDamage = currentDamage + (float) scalingBonus;
                event.setAmount(newDamage);

                StatsSystem.LOGGER.info("New damage (after scaling): {}", newDamage);
            }

        } catch (Exception e) {
            StatsSystem.LOGGER.error("Error applying weapon scaling", e);
        }
    }
}
