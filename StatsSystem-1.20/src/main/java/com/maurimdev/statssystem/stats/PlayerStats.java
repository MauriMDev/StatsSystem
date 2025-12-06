package com.maurimdev.statssystem.stats;

import net.minecraft.nbt.CompoundTag;

/**
 * Clase que almacena todas las estadísticas de un jugador
 * Se guarda en NBT y persiste entre sesiones
 */
public class PlayerStats {

    // ========== NIVELES DE ESTADÍSTICAS (0-64) ==========
    private int vitality;
    private int endurance;
    private int strength;
    private int combat;
    private int mining;
    private int agility;

    // ========== CONFIGURACIÓN DEL JUGADOR ==========
    private ProgressionMode mode;
    private Difficulty difficulty;
    private boolean hasSelectedMode;

    // ========== XP ACUMULADA POR HABILIDAD (Solo modo PRACTICE) ==========
    private double vitalityXP;
    private double enduranceXP;
    private double strengthXP;
    private double combatXP;
    private double miningXP;
    private double agilityXP;

    // ========== CONSTRUCTOR ==========

    public PlayerStats() {
        // Valores iniciales
        this.vitality = 0;
        this.endurance = 0;
        this.strength = 0;
        this.combat = 0;
        this.mining = 0;
        this.agility = 0;

        // Sin configuración inicial
        this.mode = null;
        this.difficulty = null;
        this.hasSelectedMode = false;

        // XP en 0
        this.vitalityXP = 0.0;
        this.enduranceXP = 0.0;
        this.strengthXP = 0.0;
        this.combatXP = 0.0;
        this.miningXP = 0.0;
        this.agilityXP = 0.0;
    }

    // ========== GETTERS DE NIVELES ==========

    public int getLevel(StatType stat) {
        return switch (stat) {
            case VITALITY -> vitality;
            case ENDURANCE -> endurance;
            case STRENGTH -> strength;
            case COMBAT -> combat;
            case MINING -> mining;
            case AGILITY -> agility;
        };
    }

    public int getVitality() { return vitality; }
    public int getEndurance() { return endurance; }
    public int getStrength() { return strength; }
    public int getCombat() { return combat; }
    public int getMining() { return mining; }
    public int getAgility() { return agility; }

    // ========== SETTERS DE NIVELES ==========

    public void setLevel(StatType stat, int level) {
        // Validar límites (0-64)
        level = Math.max(0, Math.min(64, level));

        switch (stat) {
            case VITALITY -> vitality = level;
            case ENDURANCE -> endurance = level;
            case STRENGTH -> strength = level;
            case COMBAT -> combat = level;
            case MINING -> mining = level;
            case AGILITY -> agility = level;
        }
    }

    public void setVitality(int vitality) { this.vitality = Math.max(0, Math.min(64, vitality)); }
    public void setEndurance(int endurance) { this.endurance = Math.max(0, Math.min(64, endurance)); }
    public void setStrength(int strength) { this.strength = Math.max(0, Math.min(64, strength)); }
    public void setCombat(int combat) { this.combat = Math.max(0, Math.min(64, combat)); }
    public void setMining(int mining) { this.mining = Math.max(0, Math.min(64, mining)); }
    public void setAgility(int agility) { this.agility = Math.max(0, Math.min(64, agility)); }

    // ========== MÉTODOS DE PROGRESIÓN ==========

    /**
     * Sube una estadística en 1 nivel
     * @param stat La estadística a subir
     * @return true si se pudo subir, false si ya está en el máximo
     */
    public boolean levelUp(StatType stat) {
        int currentLevel = getLevel(stat);
        if (currentLevel >= 64) {
            return false; // Ya está en el máximo
        }
        setLevel(stat, currentLevel + 1);
        return true;
    }

    /**
     * Calcula el bonus actual de una estadística
     * @param stat La estadística
     * @return El valor del bonus
     */
    public double getBonus(StatType stat) {
        int level = getLevel(stat);
        int softCap = stat.getSoftCap();
        double bonusPerLevel = stat.getBonusPerLevel();

        if (level <= softCap) {
            // Antes del soft cap: bonus completo
            return level * bonusPerLevel;
        } else {
            // Bonus hasta el soft cap
            double normalBonus = softCap * bonusPerLevel;

            // Bonus reducido después del soft cap (50%)
            int levelsAboveCap = level - softCap;
            double reducedBonus = levelsAboveCap * bonusPerLevel * 0.5;

            return normalBonus + reducedBonus;
        }
    }

    /**
     * Verifica si una stat ha alcanzado el soft cap
     */
    public boolean hasReachedSoftCap(StatType stat) {
        return getLevel(stat) >= stat.getSoftCap();
    }

    /**
     * Calcula el nivel total (suma de todas las stats)
     */
    public int getTotalLevel() {
        return vitality + endurance + strength + combat + mining + agility;
    }

    // ========== GETTERS/SETTERS DE XP (Modo Práctica) ==========

    public double getXP(StatType stat) {
        return switch (stat) {
            case VITALITY -> vitalityXP;
            case ENDURANCE -> enduranceXP;
            case STRENGTH -> strengthXP;
            case COMBAT -> combatXP;
            case MINING -> miningXP;
            case AGILITY -> agilityXP;
        };
    }

    public void setXP(StatType stat, double xp) {
        xp = Math.max(0, xp);
        switch (stat) {
            case VITALITY -> vitalityXP = xp;
            case ENDURANCE -> enduranceXP = xp;
            case STRENGTH -> strengthXP = xp;
            case COMBAT -> combatXP = xp;
            case MINING -> miningXP = xp;
            case AGILITY -> agilityXP = xp;
        }
    }

    /**
     * Añade XP a una habilidad (solo modo práctica)
     * @param stat La estadística
     * @param amount Cantidad de XP a añadir
     * @return true si subió de nivel, false si no
     */
    public boolean addXP(StatType stat, double amount) {
        if (mode != ProgressionMode.PRACTICE) {
            return false; // Solo funciona en modo práctica
        }

        double currentXP = getXP(stat);
        int currentLevel = getLevel(stat);

        if (currentLevel >= 64) {
            return false; // Ya está en el máximo
        }

        // Añadir XP
        currentXP += amount;

        // Calcular XP necesaria para subir
        int xpNeeded = calculatePracticeXPNeeded(currentLevel);

        // Verificar si sube de nivel
        boolean leveledUp = false;
        while (currentXP >= xpNeeded && currentLevel < 64) {
            currentXP -= xpNeeded;
            currentLevel++;
            leveledUp = true;

            if (currentLevel < 64) {
                xpNeeded = calculatePracticeXPNeeded(currentLevel);
            }
        }

        // Guardar cambios
        setXP(stat, currentXP);
        if (leveledUp) {
            setLevel(stat, currentLevel);
        }

        return leveledUp;
    }

    /**
     * Calcula XP necesaria para subir de nivel (fórmula de Minecraft vanilla)
     */
    private int calculatePracticeXPNeeded(int level) {
        if (level <= 16) {
            return level * level + 6 * level;
        } else if (level <= 31) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
    }

    // ========== GETTERS/SETTERS DE CONFIGURACIÓN ==========

    public ProgressionMode getMode() { return mode; }
    public void setMode(ProgressionMode mode) { this.mode = mode; }

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }

    public boolean hasSelectedMode() { return hasSelectedMode; }
    public void setHasSelectedMode(boolean hasSelectedMode) { this.hasSelectedMode = hasSelectedMode; }

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Resetea todas las estadísticas a 0
     */
    public void reset() {
        vitality = 0;
        endurance = 0;
        strength = 0;
        combat = 0;
        mining = 0;
        agility = 0;

        vitalityXP = 0.0;
        enduranceXP = 0.0;
        strengthXP = 0.0;
        combatXP = 0.0;
        miningXP = 0.0;
        agilityXP = 0.0;
    }

    /**
     * Copia los datos de otra instancia
     */
    public void copyFrom(PlayerStats other) {
        this.vitality = other.vitality;
        this.endurance = other.endurance;
        this.strength = other.strength;
        this.combat = other.combat;
        this.mining = other.mining;
        this.agility = other.agility;

        this.mode = other.mode;
        this.difficulty = other.difficulty;
        this.hasSelectedMode = other.hasSelectedMode;

        this.vitalityXP = other.vitalityXP;
        this.enduranceXP = other.enduranceXP;
        this.strengthXP = other.strengthXP;
        this.combatXP = other.combatXP;
        this.miningXP = other.miningXP;
        this.agilityXP = other.agilityXP;
    }

    // ========== GUARDADO Y LECTURA NBT ==========

    /**
     * Guarda los datos en un CompoundTag (NBT)
     */
    public CompoundTag saveNBTData() {
        CompoundTag tag = new CompoundTag();

        // Niveles
        tag.putInt("vitality", vitality);
        tag.putInt("endurance", endurance);
        tag.putInt("strength", strength);
        tag.putInt("combat", combat);
        tag.putInt("mining", mining);
        tag.putInt("agility", agility);

        // Configuración
        if (mode != null) {
            tag.putString("mode", mode.name());
        }
        if (difficulty != null) {
            tag.putString("difficulty", difficulty.name());
        }
        tag.putBoolean("hasSelectedMode", hasSelectedMode);

        // XP de habilidades
        tag.putDouble("vitalityXP", vitalityXP);
        tag.putDouble("enduranceXP", enduranceXP);
        tag.putDouble("strengthXP", strengthXP);
        tag.putDouble("combatXP", combatXP);
        tag.putDouble("miningXP", miningXP);
        tag.putDouble("agilityXP", agilityXP);

        return tag;
    }

    /**
     * Carga los datos desde un CompoundTag (NBT)
     */
    public void loadNBTData(CompoundTag tag) {
        // Niveles
        vitality = tag.getInt("vitality");
        endurance = tag.getInt("endurance");
        strength = tag.getInt("strength");
        combat = tag.getInt("combat");
        mining = tag.getInt("mining");
        agility = tag.getInt("agility");

        // Configuración
        if (tag.contains("mode")) {
            mode = ProgressionMode.valueOf(tag.getString("mode"));
        }
        if (tag.contains("difficulty")) {
            difficulty = Difficulty.valueOf(tag.getString("difficulty"));
        }
        hasSelectedMode = tag.getBoolean("hasSelectedMode");

        // XP de habilidades
        vitalityXP = tag.getDouble("vitalityXP");
        enduranceXP = tag.getDouble("enduranceXP");
        strengthXP = tag.getDouble("strengthXP");
        combatXP = tag.getDouble("combatXP");
        miningXP = tag.getDouble("miningXP");
        agilityXP = tag.getDouble("agilityXP");
    }

    /**
     * Establece la dificultad basándose en la dificultad del mundo de Minecraft
     * Solo funciona si aún no se ha establecido
     *
     * @param worldDifficulty La dificultad del mundo (PEACEFUL, EASY, NORMAL, HARD)
     */
    public void setDifficultyFromWorld(net.minecraft.world.Difficulty worldDifficulty) {
        // Solo establecer si no se ha configurado antes
        if (this.difficulty != null) {
            return; // Ya está bloqueada
        }

        // Mapear la dificultad del mundo a la dificultad del mod
        this.difficulty = switch (worldDifficulty) {
            case PEACEFUL, EASY -> Difficulty.EASY;      // Peaceful y Easy → Fácil
            case NORMAL -> Difficulty.NORMAL;            // Normal → Normal
            case HARD -> Difficulty.HARD;                // Hard → Difícil
        };

        // NOTA: EXTREME es una dificultad especial del mod, no del juego
        // Solo se puede establecer manualmente si el jugador lo elige
    }

    /**
     * Verifica si la dificultad está bloqueada
     */
    public boolean isDifficultyLocked() {
        return difficulty != null && hasSelectedMode;
    }
}