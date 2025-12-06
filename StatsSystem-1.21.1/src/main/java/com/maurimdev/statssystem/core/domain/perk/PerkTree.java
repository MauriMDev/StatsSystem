package com.maurimdev.statssystem.core.domain.perk;

import com.maurimdev.statssystem.core.domain.stats.PlayerStats;
import com.maurimdev.statssystem.core.domain.stats.StatType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Representa el árbol de perks de una estadística específica.
 * Organiza los perks por tier y proporciona métodos para consultar y filtrar perks.
 */
public class PerkTree {
    private final StatType associatedStat;
    private final Map<PerkTier, List<Perk>> perksByTier;

    /**
     * Constructor que inicializa el árbol de perks vacío para una stat
     * @param associatedStat La stat asociada a este árbol de perks
     */
    public PerkTree(StatType associatedStat) {
        this.associatedStat = associatedStat;
        this.perksByTier = new EnumMap<>(PerkTier.class);

        // Inicializar listas vacías para cada tier
        for (PerkTier tier : PerkTier.values()) {
            perksByTier.put(tier, new ArrayList<>());
        }
    }

    // ============================================
    // MÉTODOS DE GESTIÓN
    // ============================================

    /**
     * Añade un perk al árbol en el tier correspondiente
     * @param perk El perk a añadir
     */
    public void addPerk(Perk perk) {
        PerkTier tier = perk.getTier();
        List<Perk> tierPerks = perksByTier.get(tier);

        if (!tierPerks.contains(perk)) {
            tierPerks.add(perk);
        }
    }

    /**
     * Añade múltiples perks al árbol
     * @param perks Colección de perks a añadir
     */
    public void addPerks(Collection<Perk> perks) {
        perks.forEach(this::addPerk);
    }

    // ============================================
    // MÉTODOS DE CONSULTA
    // ============================================

    /**
     * Obtiene la stat asociada a este árbol
     */
    public StatType getAssociatedStat() {
        return associatedStat;
    }

    /**
     * Obtiene todos los perks de un tier específico
     * @param tier El tier a consultar
     * @return Lista inmutable de perks del tier
     */
    public List<Perk> getPerksForTier(PerkTier tier) {
        return List.copyOf(perksByTier.get(tier));
    }

    /**
     * Obtiene todos los perks del árbol
     * @return Lista de todos los perks en todos los tiers
     */
    public List<Perk> getAllPerks() {
        return perksByTier.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los perks desbloqueables según el nivel de stat del jugador
     * @param statLevel Nivel actual de la stat del jugador
     * @return Lista de perks cuyos tiers están desbloqueados
     */
    public List<Perk> getUnlockedPerks(int statLevel) {
        return perksByTier.entrySet().stream()
                .filter(entry -> entry.getKey().isUnlockedAt(statLevel))
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los perks bloqueados según el nivel de stat del jugador
     * @param statLevel Nivel actual de la stat del jugador
     * @return Lista de perks cuyos tiers NO están desbloqueados
     */
    public List<Perk> getLockedPerks(int statLevel) {
        return perksByTier.entrySet().stream()
                .filter(entry -> !entry.getKey().isUnlockedAt(statLevel))
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toList());
    }

    /**
     * Busca un perk por su ID
     * @param perkId ID del perk a buscar
     * @return El perk si existe, null en caso contrario
     */
    public Perk getPerkById(String perkId) {
        return getAllPerks().stream()
                .filter(perk -> perk.getId().equals(perkId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Verifica si el árbol contiene un perk específico
     * @param perkId ID del perk a verificar
     * @return true si el perk existe en el árbol
     */
    public boolean hasPerk(String perkId) {
        return getPerkById(perkId) != null;
    }

    /**
     * Obtiene el número total de perks en el árbol
     * @return Cantidad de perks
     */
    public int getTotalPerkCount() {
        return getAllPerks().size();
    }

    /**
     * Obtiene el número de perks en un tier específico
     * @param tier El tier a consultar
     * @return Cantidad de perks en ese tier
     */
    public int getPerkCountForTier(PerkTier tier) {
        return perksByTier.get(tier).size();
    }

    /**
     * Obtiene todos los tiers que tienen al menos un perk
     * @return Lista de tiers con perks
     */
    public List<PerkTier> getPopulatedTiers() {
        return perksByTier.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
    }

    // ============================================
    // MÉTODOS DE FILTRADO AVANZADO
    // ============================================

    /**
     * Obtiene perks que el jugador puede desbloquear ahora
     * @param playerStats Stats del jugador
     * @param playerXpLevel Nivel de XP del jugador
     * @return Lista de perks que cumplen todos los requisitos
     */
    public List<Perk> getAvailablePerks(PlayerStats playerStats, int playerXpLevel) {
        int statLevel = playerStats.getLevel(associatedStat);

        return getUnlockedPerks(statLevel).stream()
                .filter(perk -> perk.canUnlock(playerStats, playerXpLevel))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene perks por tipo de efecto
     * @param effectType El tipo de efecto a buscar
     * @return Lista de perks con ese tipo de efecto
     */
    public List<Perk> getPerksByEffectType(PerkEffect effectType) {
        return getAllPerks().stream()
                .filter(perk -> perk.getEffectType() == effectType)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene solo los perks pasivos
     * @return Lista de perks con efectos pasivos
     */
    public List<Perk> getPassivePerks() {
        return getAllPerks().stream()
                .filter(perk -> perk.getEffectType().isPassive())
                .collect(Collectors.toList());
    }

    /**
     * Obtiene solo los perks activos (que requieren activación)
     * @return Lista de perks con efectos activos
     */
    public List<Perk> getActivePerks() {
        return getAllPerks().stream()
                .filter(perk -> perk.getEffectType().requiresActivation())
                .collect(Collectors.toList());
    }

    // ============================================
    // MÉTODOS DE INFORMACIÓN
    // ============================================

    @Override
    public String toString() {
        return "PerkTree{" +
                "stat=" + associatedStat.getDisplayName() +
                ", totalPerks=" + getTotalPerkCount() +
                ", tier1=" + getPerkCountForTier(PerkTier.TIER_1) +
                ", tier2=" + getPerkCountForTier(PerkTier.TIER_2) +
                ", tier3=" + getPerkCountForTier(PerkTier.TIER_3) +
                '}';
    }

    /**
     * Obtiene un resumen del árbol de perks
     * @return String con información formateada
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("§6§l").append(associatedStat.getDisplayName()).append(" Perk Tree\n");
        sb.append("§7Total Perks: §e").append(getTotalPerkCount()).append("\n");

        for (PerkTier tier : PerkTier.values()) {
            int count = getPerkCountForTier(tier);
            if (count > 0) {
                sb.append("§7").append(tier.getDisplayName()).append(": §e").append(count).append(" perks\n");
            }
        }

        return sb.toString();
    }
}
