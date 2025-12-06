package com.maurimdev.statssystem.client.keybinding;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/**
 * Registra todas las teclas personalizadas del mod
 */
public class ModKeyBindings {

    // Nombre de la categoría donde aparecerán nuestras teclas
    public static final String CATEGORY = "key.categories.statssystem";

    // La tecla 'R' para abrir el menú de estadísticas
    public static final KeyMapping OPEN_STATS_MENU = new KeyMapping(
            "key.statssystem.open_stats",           // Nombre de traducción
            KeyConflictContext.IN_GAME,              // Solo funciona en el juego
            InputConstants.Type.KEYSYM,              // Tipo: teclado
            GLFW.GLFW_KEY_R,                         // Tecla: R
            CATEGORY                                 // Categoría
    );

    /**
     * Este método se llama desde el cliente para registrar las teclas
     */
    public static void register() {
        // No hacemos nada aquí, el registro se hace en ClientSetup
        // Pero necesitamos inicializar las constantes estáticas
    }
}