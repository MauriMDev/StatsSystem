package com.maurimdev.statssystem;

import com.maurimdev.statssystem.command.StatsTestCommand;
import com.maurimdev.statssystem.init.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(StatsSystem.MOD_ID)
public class StatsSystem {

    public static final String MOD_ID = "statssystem";
    public static final Logger LOGGER = LogUtils.getLogger();

    public StatsSystem(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        LOGGER.info("Stats System inicializando...");

        // ============================================
        // REGISTRAR ITEMS ← AÑADIR ESTA LÍNEA
        // ============================================
        ModItems.register(modEventBus);

        // Registrar esta clase para eventos de Forge
        forgeEventBus.register(this);
    }

    // ============================================
    // REGISTRAR COMANDOS
    // ============================================

    /**
     * Evento que se dispara cuando Minecraft registra comandos
     * Aquí registramos nuestro comando de prueba
     */
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        StatsTestCommand.register(event.getDispatcher());
        LOGGER.info("Comando /statstest registrado");
    }
}