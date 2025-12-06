package com.maurimdev.statssystem.core.domain.stats;

import com.maurimdev.statssystem.core.service.StatsProgressionService;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Modelo de datos que almacena todas las estadísticas de un jugador
 * Se guarda en NBT y persiste entre sesiones
 *
 * NOTA: La lógica de progresión está en StatsProgressionService
 */
public class PlayerStats {

    // ========== NIVELES DE ESTADÍSTICAS (0-64) ==========
    private int vitality;
    private int strength;
    private int dexterity;
    private int mining;
    private int agility;
    private int farming;

    // ========== XP ACUMULADA POR HABILIDAD ==========
    private double vitalityXP;
    private double strengthXP;
    private double dexterityXP;
    private double miningXP;
    private double agilityXP;
    private double farmingXP;

    // ========== PERKS DESBLOQUEADOS ==========
    // Mapa de perk ID -> nivel del perk (1 a maxLevel)
    private Map<String, Integer> unlockedPerks;

    // ========== PERKS DESACTIVADOS ==========
    // Set de IDs de perks que el usuario ha desactivado manualmente
    // Por defecto, todos los perks están ACTIVOS, solo guardamos los desactivados
    private Set<String> disabledPerks;

    // ========== CONSTRUCTOR ==========

    public PlayerStats() {
        // Valores iniciales
        this.vitality = 0;
        this.strength = 0;
        this.dexterity = 0;
        this.mining = 0;
        this.agility = 0;
        this.farming = 0;

        // XP en 0
        this.vitalityXP = 0.0;
        this.strengthXP = 0.0;
        this.dexterityXP = 0.0;
        this.miningXP = 0.0;
        this.agilityXP = 0.0;
        this.farmingXP = 0.0;

        // Perks vacíos
        this.unlockedPerks = new HashMap<>();
        this.disabledPerks = new java.util.HashSet<>();
    }

    // ========== GETTERS DE NIVELES ==========

    public int getLevel(StatType stat) {
        return switch (stat) {
            case VITALITY -> vitality;
            case STRENGTH -> strength;
            case DEXTERITY -> dexterity;
            case MINING -> mining;
            case AGILITY -> agility;
            case FARMING -> farming;
        };
    }

    public int getVitality() { return vitality; }
    public int getStrength() { return strength; }
    public int getDexterity() { return dexterity; }
    public int getMining() { return mining; }
    public int getAgility() { return agility; }
    public int getFarming() { return farming; }

    // ========== SETTERS DE NIVELES ==========

    public void setLevel(StatType stat, int level) {
        level = StatsProgressionService.clampLevel(level);

        switch (stat) {
            case VITALITY -> vitality = level;
            case STRENGTH -> strength = level;
            case DEXTERITY -> dexterity = level;
            case MINING -> mining = level;
            case AGILITY -> agility = level;
            case FARMING -> farming = level;
        }
    }

    public void setVitality(int vitality) { this.vitality = StatsProgressionService.clampLevel(vitality); }
    public void setStrength(int strength) { this.strength = StatsProgressionService.clampLevel(strength); }
    public void setDexterity(int dexterity) { this.dexterity = StatsProgressionService.clampLevel(dexterity); }
    public void setMining(int mining) { this.mining = StatsProgressionService.clampLevel(mining); }
    public void setAgility(int agility) { this.agility = StatsProgressionService.clampLevel(agility); }
    public void setFarming(int farming) { this.farming = StatsProgressionService.clampLevel(farming); }

    // ========== MÉTODOS DE UTILIDAD DE LECTURA ==========

    /**
     * Calcula el nivel total sumando todas las estadísticas
     */
    public int getTotalLevel() {
        return vitality + strength + dexterity + mining + agility + farming;
    }

    // ========== MÉTODOS DELEGADOS A StatsProgressionService ==========
    // Estos métodos se mantienen por compatibilidad, pero delegan al servicio

    /**
     * @deprecated Usar StatsProgressionService.levelUp(stats, stat)
     */
    @Deprecated
    public boolean levelUp(StatType stat) {
        return StatsProgressionService.levelUp(this, stat);
    }

    /**
     * @deprecated Usar StatsProgressionService.calculateBonus(stat, level)
     */
    @Deprecated
    public double getBonus(StatType stat) {
        return StatsProgressionService.calculateBonus(stat, getLevel(stat));
    }

    /**
     * @deprecated Usar StatsProgressionService.hasReachedSoftCap(stat, level)
     */
    @Deprecated
    public boolean hasReachedSoftCap(StatType stat) {
        return StatsProgressionService.hasReachedSoftCap(stat, getLevel(stat));
    }

    /**
     * @deprecated Usar StatsProgressionService.addPracticeXP(stats, stat, amount)
     */
    @Deprecated
    public boolean addXP(StatType stat, double amount) {
        return StatsProgressionService.addPracticeXP(this, stat, amount);
    }

    // ========== GETTERS/SETTERS DE XP ==========

    public double getXP(StatType stat) {
        return switch (stat) {
            case VITALITY -> vitalityXP;
            case STRENGTH -> strengthXP;
            case DEXTERITY -> dexterityXP;
            case MINING -> miningXP;
            case AGILITY -> agilityXP;
            case FARMING -> farmingXP;
        };
    }

    public void setXP(StatType stat, double xp) {
        xp = StatsProgressionService.clampXP(xp);
        switch (stat) {
            case VITALITY -> vitalityXP = xp;
            case STRENGTH -> strengthXP = xp;
            case DEXTERITY -> dexterityXP = xp;
            case MINING -> miningXP = xp;
            case AGILITY -> agilityXP = xp;
            case FARMING -> farmingXP = xp;
        }
    }

    // ========== MÉTODOS DE GESTIÓN DE PERKS ==========

    /**
     * Verifica si el jugador tiene un perk desbloqueado
     * @param perkId ID del perk a verificar
     * @return true si el perk está desbloqueado
     */
    public boolean hasPerk(String perkId) {
        return unlockedPerks.containsKey(perkId);
    }

    /**
     * Obtiene el nivel actual de un perk desbloqueado
     * @param perkId ID del perk
     * @return Nivel del perk (1 a maxLevel), o 0 si no está desbloqueado
     */
    public int getPerkLevel(String perkId) {
        return unlockedPerks.getOrDefault(perkId, 0);
    }

    /**
     * Desbloquea un perk en nivel 1
     * @param perkId ID del perk a desbloquear
     * @return true si se desbloqueó correctamente, false si ya estaba desbloqueado
     */
    public boolean unlockPerk(String perkId) {
        if (hasPerk(perkId)) {
            return false; // Ya está desbloqueado
        }
        unlockedPerks.put(perkId, 1);
        return true;
    }

    /**
     * Mejora un perk existente a un nuevo nivel
     * @param perkId ID del perk
     * @param newLevel Nuevo nivel del perk
     * @return true si se mejoró correctamente, false si el perk no está desbloqueado
     */
    public boolean upgradePerk(String perkId, int newLevel) {
        if (!hasPerk(perkId)) {
            return false; // El perk debe estar desbloqueado primero
        }
        unlockedPerks.put(perkId, newLevel);
        return true;
    }

    /**
     * Obtiene todos los IDs de perks desbloqueados
     * @return Set inmutable de IDs de perks
     */
    public Set<String> getUnlockedPerks() {
        return Set.copyOf(unlockedPerks.keySet());
    }

    /**
     * Obtiene el mapa completo de perks con sus niveles (solo lectura)
     * @return Mapa inmutable de perkId -> nivel
     */
    public Map<String, Integer> getPerksMap() {
        return Map.copyOf(unlockedPerks);
    }

    /**
     * Elimina un perk desbloqueado (usado para reset o respec)
     * @param perkId ID del perk a eliminar
     * @return true si el perk fue eliminado, false si no existía
     */
    public boolean removePerk(String perkId) {
        return unlockedPerks.remove(perkId) != null;
    }

    /**
     * Obtiene la cantidad total de perks desbloqueados
     * @return Número de perks activos
     */
    public int getTotalPerksUnlocked() {
        return unlockedPerks.size();
    }

    // ========== GESTIÓN DE ACTIVACIÓN/DESACTIVACIÓN DE PERKS ==========

    /**
     * Verifica si un perk está activo (desbloqueado Y no desactivado)
     * @param perkId ID del perk
     * @return true si el perk está activo
     */
    public boolean isPerkActive(String perkId) {
        return hasPerk(perkId) && !disabledPerks.contains(perkId);
    }

    /**
     * Verifica si un perk está desactivado
     * @param perkId ID del perk
     * @return true si el perk está desactivado
     */
    public boolean isPerkDisabled(String perkId) {
        return disabledPerks.contains(perkId);
    }

    /**
     * Activa un perk (lo remueve de la lista de desactivados)
     * @param perkId ID del perk
     */
    public void enablePerk(String perkId) {
        disabledPerks.remove(perkId);
    }

    /**
     * Desactiva un perk (lo añade a la lista de desactivados)
     * @param perkId ID del perk
     */
    public void disablePerk(String perkId) {
        if (hasPerk(perkId)) {
            disabledPerks.add(perkId);
        }
    }

    /**
     * Alterna el estado de un perk (activa/desactiva)
     * @param perkId ID del perk
     * @return true si ahora está activo, false si está desactivado
     */
    public boolean togglePerk(String perkId) {
        if (disabledPerks.contains(perkId)) {
            disabledPerks.remove(perkId);
            return true; // Ahora está activo
        } else {
            if (hasPerk(perkId)) {
                disabledPerks.add(perkId);
            }
            return false; // Ahora está desactivado
        }
    }

    /**
     * Obtiene el set de perks desactivados (solo lectura)
     * @return Set inmutable de IDs de perks desactivados
     */
    public Set<String> getDisabledPerks() {
        return Set.copyOf(disabledPerks);
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Resetea todas las estadísticas, XP y perks a 0
     */
    public void reset() {
        vitality = 0;
        strength = 0;
        dexterity = 0;
        mining = 0;
        agility = 0;
        farming = 0;

        vitalityXP = 0.0;
        strengthXP = 0.0;
        dexterityXP = 0.0;
        miningXP = 0.0;
        agilityXP = 0.0;
        farmingXP = 0.0;

        unlockedPerks.clear();
        disabledPerks.clear();
    }

    /**
     * Copia todos los datos de otro PlayerStats (stats, XP y perks)
     */
    public void copyFrom(PlayerStats other) {
        this.vitality = other.vitality;
        this.strength = other.strength;
        this.dexterity = other.dexterity;
        this.mining = other.mining;
        this.agility = other.agility;
        this.farming = other.farming;

        this.vitalityXP = other.vitalityXP;
        this.strengthXP = other.strengthXP;
        this.dexterityXP = other.dexterityXP;
        this.miningXP = other.miningXP;
        this.agilityXP = other.agilityXP;
        this.farmingXP = other.farmingXP;

        this.unlockedPerks.clear();
        this.unlockedPerks.putAll(other.unlockedPerks);

        this.disabledPerks.clear();
        this.disabledPerks.addAll(other.disabledPerks);
    }

    // ========== GUARDADO Y LECTURA NBT ==========

    /**
     * Guarda los datos en un CompoundTag (NBT)
     * Usado por el sistema de Data Attachments
     */
    public CompoundTag saveNBTData() {
        CompoundTag tag = new CompoundTag();

        // Niveles
        tag.putInt("vitality", vitality);
        tag.putInt("strength", strength);
        tag.putInt("dexterity", dexterity);
        tag.putInt("mining", mining);
        tag.putInt("agility", agility);
        tag.putInt("farming", farming);

        // XP de habilidades
        tag.putDouble("vitalityXP", vitalityXP);
        tag.putDouble("strengthXP", strengthXP);
        tag.putDouble("dexterityXP", dexterityXP);
        tag.putDouble("miningXP", miningXP);
        tag.putDouble("agilityXP", agilityXP);
        tag.putDouble("farmingXP", farmingXP);

        // Perks desbloqueados
        CompoundTag perksTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : unlockedPerks.entrySet()) {
            perksTag.putInt(entry.getKey(), entry.getValue());
        }
        tag.put("perks", perksTag);

        // Perks desactivados
        CompoundTag disabledTag = new CompoundTag();
        int index = 0;
        for (String perkId : disabledPerks) {
            disabledTag.putString("perk_" + index, perkId);
            index++;
        }
        tag.put("disabledPerks", disabledTag);

        return tag;
    }

    /**
     * Carga los datos desde un CompoundTag (NBT)
     * Usado por el sistema de Data Attachments
     */
    public void loadNBTData(CompoundTag tag) {
        try {
            // Niveles
            vitality = tag.getInt("vitality");
            strength = tag.getInt("strength");
            dexterity = tag.getInt("dexterity");
            mining = tag.getInt("mining");
            agility = tag.getInt("agility");
            farming = tag.getInt("farming");

            // XP de habilidades
            vitalityXP = tag.getDouble("vitalityXP");
            strengthXP = tag.getDouble("strengthXP");
            dexterityXP = tag.getDouble("dexterityXP");
            miningXP = tag.getDouble("miningXP");
            agilityXP = tag.getDouble("agilityXP");
            farmingXP = tag.getDouble("farmingXP");

            // Perks desbloqueados
            unlockedPerks.clear();
            if (tag.contains("perks")) {
                CompoundTag perksTag = tag.getCompound("perks");
                for (String perkId : perksTag.getAllKeys()) {
                    int level = perksTag.getInt(perkId);
                    unlockedPerks.put(perkId, level);
                }
            }

            // Perks desactivados
            disabledPerks.clear();
            if (tag.contains("disabledPerks")) {
                CompoundTag disabledTag = tag.getCompound("disabledPerks");
                for (String key : disabledTag.getAllKeys()) {
                    String perkId = disabledTag.getString(key);
                    disabledPerks.add(perkId);
                }
            }

        } catch (Exception e) {
            System.err.println("Error cargando stats del jugador: " + e.getMessage());
        }
    }

}