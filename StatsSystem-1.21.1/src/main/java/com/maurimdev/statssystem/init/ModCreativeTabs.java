package com.maurimdev.statssystem.init;

import com.maurimdev.statssystem.StatsSystem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, StatsSystem.MOD_ID);

    // ✅ Tab personalizada del mod
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> STATS_SYSTEM_TAB =
            CREATIVE_MODE_TABS.register("stats_system_tab",
                    () -> CreativeModeTab.builder()
                            .title(Component.literal("Stats System"))
                            .icon(() -> new ItemStack(ModItems.SOUL_BOOK.get()))
                            .displayItems((parameters, output) -> {
                                // ✅ Agregar items a la tab
                                output.accept(ModItems.SOUL_BOOK.get());
                                // Aquí puedes agregar más items en el futuro
                            })
                            .build()
            );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}