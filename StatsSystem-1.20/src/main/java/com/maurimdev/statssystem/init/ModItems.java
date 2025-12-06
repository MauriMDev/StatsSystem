package com.maurimdev.statssystem.init;

import com.maurimdev.statssystem.StatsSystem;
import com.maurimdev.statssystem.item.SoulBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

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
     * ForgeRegistries.ITEMS = registro de items de Minecraft
     * StatsSystem.MOD_ID = "statssystem"
     */
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, StatsSystem.MOD_ID);

    // ============================================
    // PASO 2: Registrar items individuales
    // ============================================

    /**
     * El Soul Book
     */
    public static final RegistryObject<Item> SOUL_BOOK =
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
     * Registra todos los items en el event bus de Forge
     *
     * @param eventBus El event bus del mod
     */
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        StatsSystem.LOGGER.info("Items registrados");
    }
}