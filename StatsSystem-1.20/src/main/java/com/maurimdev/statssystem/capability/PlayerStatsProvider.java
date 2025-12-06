package com.maurimdev.statssystem.capability;

import com.maurimdev.statssystem.stats.PlayerStats;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provider que "adjunta" la capability de estadísticas a los jugadores
 *
 * Este es el "pegamento" entre el jugador y sus datos personalizados
 * Cuando Forge pregunta "¿qué capabilities tiene este jugador?",
 * esta clase responde y proporciona acceso a PlayerStatsStorage
 *
 * Implementa dos interfaces:
 * - ICapabilityProvider: Dice "yo proveo capabilities"
 * - INBTSerializable: Dice "puedo guardarme y cargarme en formato NBT"
 */
public class PlayerStatsProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    // ============================================
    // REGISTRO DE LA CAPABILITY
    // ============================================

    /**
     * Capability estática que representa "las estadísticas del jugador"
     *
     * Es como un "token" o "identificador" único que Forge usa para
     * saber de qué tipo de datos estamos hablando
     *
     * CapabilityToken: Sistema moderno de Forge para crear capabilities
     * Es genérico: Capability<IPlayerStatsCapability> = capability del tipo IPlayerStatsCapability
     */
    public static Capability<IPlayerStatsCapability> PLAYER_STATS =
            CapabilityManager.get(new CapabilityToken<>() {});

    // ============================================
    // ALMACENAMIENTO REAL DE DATOS
    // ============================================

    /**
     * La instancia real que guarda los datos
     * Cada jugador tendrá su propia instancia de PlayerStatsStorage
     * Esta variable vive mientras el jugador esté en el mundo
     */
    private PlayerStatsStorage capability = null;

    /**
     * LazyOptional: Wrapper especial de Forge para manejar capabilities de forma segura
     *
     * ¿Por qué "Lazy"? (Perezoso)
     * - No crea la capability hasta que alguien la pida
     * - Puede estar "vacío" si la capability no existe
     *
     * ¿Por qué "Optional"? (Opcional)
     * - Puede estar presente o ausente
     * - Evita NullPointerException
     *
     * Piensa en ello como una "caja" que puede contener la capability o estar vacía
     */
    private LazyOptional<IPlayerStatsCapability> optional = LazyOptional.empty();

    // ============================================
    // CONSTRUCTOR
    // ============================================

    /**
     * Constructor que se llama cuando se crea el provider
     * Inicializa la capability y el LazyOptional
     */
    public PlayerStatsProvider() {
        // Crear la instancia real de almacenamiento
        this.capability = new PlayerStatsStorage();

        // Crear el LazyOptional que "envuelve" la capability
        // LazyOptional.of(() -> capability) = "crea un optional que contiene 'capability'"
        // El () -> es una lambda que devuelve la capability cuando se necesite
        this.optional = LazyOptional.of(() -> capability);
    }

    // ============================================
    // PROVEER LA CAPABILITY (ICapabilityProvider)
    // ============================================

    /**
     * Método que Forge llama para preguntar: "¿Tienes esta capability?"
     *
     * Este es el método MÁS IMPORTANTE del provider
     *
     * Flujo:
     * 1. Forge pregunta: "¿Tienes la capability X?"
     * 2. Este método verifica si X es la nuestra (PLAYER_STATS)
     * 3. Si SÍ: devuelve el LazyOptional con nuestros datos
     * 4. Si NO: devuelve LazyOptional.empty() (vacío)
     *
     * @param cap La capability que Forge está buscando
     * @param side La dirección (null para entities, usado en bloques/items)
     * @param <T> Tipo genérico de la capability
     * @return LazyOptional con la capability si coincide, vacío si no
     */
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        // Pregunta: ¿La capability que buscan (cap) es la nuestra (PLAYER_STATS)?
        if (cap == PLAYER_STATS) {
            // SÍ: Devolver nuestro optional
            // .cast() convierte LazyOptional<IPlayerStatsCapability> a LazyOptional<T>
            return optional.cast();
        }

        // NO: No tenemos esa capability, devolver vacío
        // Otros mods podrían buscar otras capabilities en el mismo jugador
        return LazyOptional.empty();
    }

    // ============================================
    // SERIALIZACIÓN NBT (Guardado)
    // ============================================

    /**
     * Guarda los datos en formato NBT para persistencia
     *
     * Forge llama este método cuando:
     * - El jugador sale del mundo
     * - El juego hace auto-guardado
     * - El jugador cambia de dimensión
     * - El jugador muere
     *
     * El NBT se guarda en: saves/[mundo]/playerdata/[UUID].dat
     *
     * @return CompoundTag con todos los datos serializados
     */
    @Override
    public CompoundTag serializeNBT() {
        // Obtener las stats desde el storage
        PlayerStats stats = capability.getStats();

        // Usar el método saveNBTData() que ya implementaste en PlayerStats
        // Este método convierte todos los valores a formato NBT
        CompoundTag nbt = stats.saveNBTData();

        // Ejemplo de lo que contiene el NBT:
        // {
        //   "vitality": 25,
        //   "endurance": 15,
        //   "mode": "XP",
        //   "difficulty": "NORMAL",
        //   ...
        // }

        return nbt;
    }

    // ============================================
    // DESERIALIZACIÓN NBT (Carga)
    // ============================================

    /**
     * Carga los datos desde formato NBT
     *
     * Forge llama este método cuando:
     * - El jugador entra al mundo
     * - Se carga el mundo desde disco
     * - El jugador respawnea después de morir
     *
     * @param nbt El CompoundTag con los datos guardados
     */
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        // Obtener las stats desde el storage
        PlayerStats stats = capability.getStats();

        // Usar el método loadNBTData() que ya implementaste en PlayerStats
        // Este método lee todos los valores del NBT y los aplica a las stats
        stats.loadNBTData(nbt);

        // Ahora las stats del jugador están restauradas tal como estaban
    }

    // ============================================
    // LIMPIEZA (Invalidate)
    // ============================================

    /**
     * Invalida el LazyOptional cuando ya no se necesita
     *
     * ¿Cuándo se llama?
     * - Cuando el jugador sale del servidor
     * - Cuando la entidad jugador es destruida
     * - Cuando se cambia de dimensión (antes de crear nueva entidad)
     *
     * ¿Por qué es importante?
     * - Libera memoria
     * - Previene memory leaks
     * - Le dice a otros sistemas "esta capability ya no está disponible"
     */
    public void invalidate() {
        // Invalida el LazyOptional
        // Después de esto, cualquier intento de usar 'optional' devolverá vacío
        this.optional.invalidate();
    }
}