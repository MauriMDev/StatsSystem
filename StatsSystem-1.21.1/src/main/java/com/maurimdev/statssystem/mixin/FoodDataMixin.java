package com.maurimdev.statssystem.mixin;

import com.maurimdev.statssystem.StatsSystem;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin para FoodData que extiende el sistema de comida de Minecraft.
 *
 * Modificaciones:
 * - Permite comer hasta 60 puntos de comida (en lugar del límite vanilla de 20)
 * - Consume hambre correctamente punto por punto (en lugar de 2 en 2)
 * - Soporta regeneración y mecánicas de hambre para valores superiores a 20
 *
 * NOTA: El incremento de comida al comer se maneja en StatBonusHandler.onFoodEatFinish()
 * porque el método eat() no puede ser interceptado directamente en Minecraft 1.21.1.
 */
@Mixin(FoodData.class)
public class FoodDataMixin {

    @Shadow private int foodLevel;
    @Shadow private float saturationLevel;
    @Shadow private float exhaustionLevel;

    /**
     * Permite comer hasta 60 puntos (en lugar del límite vanilla de 20).
     * Este método se ejecuta cuando el jugador intenta comer.
     */
    @Inject(method = "needsFood", at = @At("HEAD"), cancellable = true)
    public void needsFood(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(this.foodLevel < 60);
    }

    /**
     * Reemplaza el tick de comida vanilla para manejar correctamente
     * consumo de hambre y regeneración con valores superiores a 20.
     *
     * Mecánicas implementadas:
     * - Consumo de exhaustion (cada 4.0 = -1 saturation o -1 food)
     * - Regeneración natural con saturation alta (10 ticks)
     * - Regeneración normal sin saturation (80 ticks)
     * - Daño por hambre cuando foodLevel <= 0
     * - Restauración automática en Peaceful
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void customTick(Player player, CallbackInfo ci) {
        Difficulty difficulty = player.level().getDifficulty();

        // === MODO PEACEFUL: RESTAURACIÓN AUTOMÁTICA ===
        if (difficulty == Difficulty.PEACEFUL && this.foodLevel < 60) {
            if (player.tickCount % 10 == 0) {
                this.foodLevel = Math.min(this.foodLevel + 1, 60);
            }
        }

        // === CONSUMO DE EXHAUSTION ===
        // Cada 4.0 de exhaustion reduce 1 saturation (o 1 food si saturation es 0)
        if (this.exhaustionLevel > 4.0F) {
            this.exhaustionLevel -= 4.0F;

            if (this.saturationLevel > 0.0F) {
                // Consumir saturation primero
                this.saturationLevel = Math.max(this.saturationLevel - 1.0F, 0.0F);
            } else if (difficulty != Difficulty.PEACEFUL) {
                // Si no hay saturation, consumir 1 punto de food
                this.foodLevel = Math.max(this.foodLevel - 1, 0);
            }
        }

        // === REGENERACIÓN NATURAL ===
        boolean naturalRegenEnabled = player.level().getGameRules()
            .getBoolean(net.minecraft.world.level.GameRules.RULE_NATURAL_REGENERATION);

        if (naturalRegenEnabled && this.saturationLevel > 0.0F && player.isHurt() && this.foodLevel >= 20) {
            // Regeneración rápida con saturation alta (cada 10 ticks)
            // Cura medio corazón (0.5F) como en vanilla
            if (player.tickCount % 10 == 0) {
                player.heal(0.5F);
                this.addExhaustion(6.0F);
            }
        } else if (naturalRegenEnabled && this.foodLevel >= 18 && player.isHurt()) {
            // Regeneración normal sin saturation (cada 80 ticks)
            // Cura medio corazón (0.5F) como en vanilla
            if (player.tickCount % 80 == 0) {
                player.heal(0.5F);
                this.addExhaustion(6.0F);
            }
        } else if (this.foodLevel <= 0) {
            // Daño por hambre
            if (player.tickCount % 80 == 0) {
                if (player.getHealth() > 10.0F || difficulty == Difficulty.HARD ||
                    (player.getHealth() > 1.0F && difficulty == Difficulty.NORMAL)) {
                    player.hurt(player.damageSources().starve(), 1.0F);
                }
            }
        }

        // Cancelar el método vanilla
        ci.cancel();
    }

    @Shadow
    public void addExhaustion(float exhaustion) {
        throw new AssertionError("Mixin shadow failed");
    }
}
