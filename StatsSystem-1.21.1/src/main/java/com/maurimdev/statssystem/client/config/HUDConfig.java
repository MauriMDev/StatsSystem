package com.maurimdev.statssystem.client.config;

/**
 * Configuración del HUD de atributos derivados.
 * Guarda las preferencias del usuario sobre la visualización del HUD.
 */
public class HUDConfig {

    private static boolean hudEnabled = true;

    /**
     * Verifica si el HUD está habilitado
     */
    public static boolean isHudEnabled() {
        return hudEnabled;
    }

    /**
     * Habilita o deshabilita el HUD
     */
    public static void setHudEnabled(boolean enabled) {
        hudEnabled = enabled;
    }

    /**
     * Alterna el estado del HUD (toggle)
     */
    public static void toggleHud() {
        hudEnabled = !hudEnabled;
    }
}
