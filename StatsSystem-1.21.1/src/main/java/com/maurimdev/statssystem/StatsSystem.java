package com.maurimdev.statssystem;

import com.maurimdev.statssystem.command.CleanPerksCommand;
import com.maurimdev.statssystem.gameplay.command.StatsCommand;
import com.maurimdev.statssystem.infrastructure.integration.CuriosIntegration;
import com.maurimdev.statssystem.infrastructure.persistence.ModAttachments;
import com.maurimdev.statssystem.init.EquipmentRequirementRegistry;
import com.maurimdev.statssystem.init.ModCreativeTabs;
import com.maurimdev.statssystem.init.ModItems;
import com.maurimdev.statssystem.init.ModPerks;
import com.maurimdev.statssystem.network.ModMessages;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

@Mod(StatsSystem.MOD_ID)
public class StatsSystem {

    public static final String MOD_ID = "statssystem";
    public static final Logger LOGGER = LogUtils.getLogger();

    // ✅ Constructor con IEventBus directamente (NeoForge 1.21.1)
    public StatsSystem(IEventBus modEventBus) {
        LOGGER.info("Stats System inicializando...");

        // Registrar items
        ModItems.register(modEventBus);

        // Registrar tab creativa personalizada
        ModCreativeTabs.register(modEventBus);

        // ✅ Registrar Data Attachments (reemplaza capabilities)
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);

        // ✅ Registrar packets de red con el nuevo sistema
        modEventBus.addListener(ModMessages::registerPayloads);

        // ✅ Registrar compatibilidad con Curios
        CuriosIntegration.register(modEventBus);

        // ✅ Inicializar registro de requisitos de equipo
        EquipmentRequirementRegistry.init();
        LOGGER.info("Equipment requirements registered: {} items", EquipmentRequirementRegistry.getRegistrySize());

        // ✅ Inicializar registro de perks
        ModPerks.register();
        LOGGER.info("Perks registered successfully");

        // ✅ Registrar eventos en el bus de NeoForge
        NeoForge.EVENT_BUS.register(this);

        LOGGER.info("Stats System inicializado correctamente");
    }

    /**
     * Registrar comandos
     */
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        StatsCommand.register(event.getDispatcher());
        CleanPerksCommand.register(event.getDispatcher());
        LOGGER.info("Comandos registrados: /stats, /cleanperks");
    }
}