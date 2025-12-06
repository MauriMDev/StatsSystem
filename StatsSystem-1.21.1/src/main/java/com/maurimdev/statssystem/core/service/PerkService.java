package com.maurimdev.statssystem.core.service;

import com.maurimdev.statssystem.core.domain.perk.*;
import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.core.domain.stats.StatType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio singleton que gestiona todos los perks disponibles en el juego.
 * Mantiene un registro centralizado de perks y proporciona métodos de consulta.
 *
 * Thread-safe para uso en servidores multiplayer.
 */
public class PerkService {

    private static PerkService instance;

    // Registro principal de perks por ID
    private final Map<String, Perk> perksById;

    // Árboles de perks organizados por stat
    private final Map<StatType, PerkTree> perkTreesByStat;

    // Perks de combinación (no asociados a una stat específica)
    private final List<Perk> comboPerks;

    // ============================================
    // SINGLETON
    // ============================================

    private PerkService() {
        this.perksById = new ConcurrentHashMap<>();
        this.perkTreesByStat = new EnumMap<>(StatType.class);
        this.comboPerks = new ArrayList<>();

        // Inicializar árboles para cada stat
        for (StatType stat : StatType.values()) {
            perkTreesByStat.put(stat, new PerkTree(stat));
        }
    }

    /**
     * Obtiene la instancia única del servicio
     */
    public static PerkService getInstance() {
        if (instance == null) {
            instance = new PerkService();
        }
        return instance;
    }

    /**
     * Reinicia el servicio (útil para testing)
     */
    public static void reset() {
        instance = new PerkService();
    }

    // ============================================
    // REGISTRO DE PERKS
    // ============================================

    /**
     * Registra un perk en el servicio
     * @param perk El perk a registrar
     * @throws IllegalArgumentException si el ID ya está registrado
     */
    public void registerPerk(Perk perk) {
        String id = perk.getId();

        if (perksById.containsKey(id)) {
            throw new IllegalArgumentException("Perk con ID '" + id + "' ya está registrado");
        }

        // Registrar en el mapa principal
        perksById.put(id, perk);

        // Agregar al árbol correspondiente
        if (perk.isCombo()) {
            comboPerks.add(perk);
        } else {
            StatType stat = perk.getAssociatedStat();
            if (stat != null) {
                PerkTree tree = perkTreesByStat.get(stat);
                tree.addPerk(perk);
            }
        }
    }

    /**
     * Registra múltiples perks a la vez
     * @param perks Colección de perks a registrar
     */
    public void registerPerks(Collection<Perk> perks) {
        perks.forEach(this::registerPerk);
    }

    // ============================================
    // CONSULTAS BÁSICAS
    // ============================================

    /**
     * Obtiene un perk por su ID
     * @param perkId ID del perk
     * @return El perk si existe, null en caso contrario
     */
    public Perk getPerkById(String perkId) {
        return perksById.get(perkId);
    }

    /**
     * Verifica si existe un perk con el ID dado
     * @param perkId ID del perk a verificar
     * @return true si el perk está registrado
     */
    public boolean hasPerk(String perkId) {
        return perksById.containsKey(perkId);
    }

    /**
     * Obtiene todos los perks registrados
     * @return Colección inmutable de todos los perks
     */
    public Collection<Perk> getAllPerks() {
        return Collections.unmodifiableCollection(perksById.values());
    }

    /**
     * Obtiene el número total de perks registrados
     * @return Cantidad de perks en el registro
     */
    public int getTotalPerkCount() {
        return perksById.size();
    }

    // ============================================
    // CONSULTAS POR STAT
    // ============================================

    /**
     * Obtiene el árbol de perks de una stat específica
     * @param stat La stat a consultar
     * @return El PerkTree asociado a esa stat
     */
    public PerkTree getPerkTree(StatType stat) {
        return perkTreesByStat.get(stat);
    }

    /**
     * Obtiene todos los perks de una stat específica
     * @param stat La stat a consultar
     * @return Lista de perks asociados a esa stat
     */
    public List<Perk> getPerksForStat(StatType stat) {
        return perkTreesByStat.get(stat).getAllPerks();
    }

    /**
     * Obtiene los perks de un tier específico para una stat
     * @param stat La stat a consultar
     * @param tier El tier a consultar
     * @return Lista de perks del tier especificado
     */
    public List<Perk> getPerksForStatAndTier(StatType stat, PerkTier tier) {
        return perkTreesByStat.get(stat).getPerksForTier(tier);
    }

    // ============================================
    // CONSULTAS DE PERKS COMBO
    // ============================================

    /**
     * Obtiene todos los perks de combinación
     * @return Lista inmutable de perks combo
     */
    public List<Perk> getComboPerks() {
        return Collections.unmodifiableList(comboPerks);
    }

    /**
     * Obtiene perks combo de un tier específico
     * @param tier El tier a consultar
     * @return Lista de perks combo del tier especificado
     */
    public List<Perk> getComboPerksByTier(PerkTier tier) {
        return comboPerks.stream()
                .filter(perk -> perk.getTier() == tier)
                .toList();
    }

    // ============================================
    // CONSULTAS AVANZADAS
    // ============================================

    /**
     * Obtiene perks que el jugador puede desbloquear ahora
     * @param playerStats Stats del jugador
     * @param playerXpLevel Nivel de XP del jugador
     * @param stat Stat específica a consultar (null para incluir combos)
     * @return Lista de perks disponibles para desbloquear
     */
    public List<Perk> getAvailablePerks(PlayerStats playerStats, int playerXpLevel, StatType stat) {
        if (stat == null) {
            // Incluir solo combo perks
            return comboPerks.stream()
                    .filter(perk -> perk.canUnlock(playerStats, playerXpLevel))
                    .toList();
        }

        return perkTreesByStat.get(stat).getAvailablePerks(playerStats, playerXpLevel);
    }

    /**
     * Obtiene perks desbloqueados por el jugador
     * @param playerStats Stats del jugador con perks
     * @return Lista de perks que el jugador tiene activos
     */
    public List<Perk> getUnlockedPerks(PlayerStats playerStats) {
        List<Perk> unlocked = new ArrayList<>();

        for (String perkId : playerStats.getUnlockedPerks()) {
            Perk perk = getPerkById(perkId);
            if (perk != null) {
                unlocked.add(perk);
            }
        }

        return unlocked;
    }

    /**
     * Obtiene perks por tipo de efecto
     * @param effectType El tipo de efecto a buscar
     * @return Lista de perks con ese tipo de efecto
     */
    public List<Perk> getPerksByEffectType(PerkEffect effectType) {
        return perksById.values().stream()
                .filter(perk -> perk.getEffectType() == effectType)
                .toList();
    }

    // ============================================
    // VALIDACIÓN
    // ============================================

    /**
     * Valida si un jugador puede desbloquear un perk
     * @param playerStats Stats del jugador
     * @param playerXpLevel Nivel de XP del jugador
     * @param perkId ID del perk a validar
     * @return true si puede desbloquearlo
     */
    public boolean canUnlock(PlayerStats playerStats, int playerXpLevel, String perkId) {
        Perk perk = getPerkById(perkId);
        if (perk == null) {
            return false;
        }

        // No puede desbloquear si ya lo tiene
        if (playerStats.hasPerk(perkId)) {
            return false;
        }

        return perk.canUnlock(playerStats, playerXpLevel);
    }

    /**
     * Valida si un jugador puede mejorar un perk
     * @param playerStats Stats del jugador
     * @param perkId ID del perk
     * @return true si puede mejorarlo
     */
    public boolean canUpgrade(PlayerStats playerStats, String perkId) {
        Perk perk = getPerkById(perkId);
        if (perk == null) {
            return false;
        }

        int currentLevel = playerStats.getPerkLevel(perkId);
        if (currentLevel == 0) {
            return false; // No está desbloqueado
        }

        return currentLevel < perk.getMaxLevel();
    }

    // ============================================
    // INFORMACIÓN Y DEBUG
    // ============================================

    /**
     * Obtiene estadísticas del registro de perks
     * @return String con información formateada
     */
    public String getRegistryStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("§6§lPerk Registry Stats:\n");
        sb.append("§7Total Perks: §e").append(getTotalPerkCount()).append("\n");
        sb.append("§7Combo Perks: §e").append(comboPerks.size()).append("\n");

        for (StatType stat : StatType.values()) {
            int count = getPerksForStat(stat).size();
            sb.append("§7").append(stat.getDisplayName()).append(": §e").append(count).append("\n");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "PerkService{" +
                "totalPerks=" + getTotalPerkCount() +
                ", comboPerks=" + comboPerks.size() +
                '}';
    }
}
