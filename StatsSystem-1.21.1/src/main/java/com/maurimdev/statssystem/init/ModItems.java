package com.maurimdev.statssystem.init;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.gameplay.item.SoulBookItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registro central de todos los items del mod
 */
public class ModItems {

    // ============================================
    // PASO 1: Crear el DeferredRegister
    // ============================================

    /**
     * DeferredRegister para items
     *
     * BuiltInRegistries.ITEM = registro de items de Minecraft (NeoForge)
     * StatsSystem.MOD_ID = "statssystem"
     */
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, StatsSystem.MOD_ID);

    // ============================================
    // PASO 2: Registrar items individuales
    // ============================================

    /**
     * El Soul Book
     * ✅ DeferredHolder<Item, Item> en lugar de RegistryObject<Item>
     */
    public static final DeferredHolder<Item, Item> SOUL_BOOK =
            ITEMS.register("soul_book", () -> new SoulBookItem(
                    new Item.Properties()
                            .stacksTo(1)
                            .fireResistant()
                            .rarity(Rarity.EPIC)
            ));

    // ============================================
    // PASO 3: Método de registro
    // ============================================

    /**
     * Registra todos los items en el event bus de NeoForge
     *
     * @param eventBus El event bus del mod
     */
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        StatsSystem.LOGGER.info("Items registrados");
    }
}