# Stats System - Minecraft Mod

**Versión:** 1.0.0 (En Desarrollo)
**Minecraft:** 1.21.1
**NeoForge:** 21.1.77
**Autor:** MaurimDev

Sistema de estadísticas y progresión RPG para Minecraft con progresión automática al estilo Skyrim.

---

## Tabla de Contenidos

1. [Concepto del Mod](#concepto-del-mod)
2. [Características Principales](#características-principales)
3. [Las 6 Estadísticas](#las-6-estadísticas)
4. [Sistema de Soft Cap](#sistema-de-soft-cap)
5. [Progresión Automática](#progresión-automática)
6. [Sistema de Perks](#sistema-de-perks)
7. [Soul Book](#soul-book)
8. [Requisitos de Equipamiento](#requisitos-de-equipamiento)
9. [Configuración del Proyecto](#configuración-del-proyecto)

---

## Concepto del Mod

**Stats System** añade un sistema de progresión permanente al jugador mediante **6 estadísticas especializadas** que suben de nivel automáticamente al realizar acciones relacionadas, similar a Skyrim.

Las estadísticas mejoran distintos aspectos del personaje y desbloquean **perks** (habilidades pasivas y activas) que modifican el gameplay.

---

## Características Principales

- ✅ **6 Estadísticas** independientes con niveles de 0 a 64
- ✅ **Progresión automática** al realizar acciones (estilo Skyrim)
- ✅ **Sistema de Soft Caps** para balancear progresión avanzada
- ✅ **Sistema de Perks** con 56 habilidades desbloqueables
- ✅ **Soul Book** como item central del sistema
- ✅ **Requisitos de equipamiento** basados en stats
- ✅ **Persistencia** de datos entre sesiones
- ✅ **Integración con Curios** para equipar el Soul Book
- ✅ **GUI personalizada** con tabs (Stats y Perks)
- ✅ **Sistema de comandos** para administradores

---

## Las 6 Estadísticas

Cada jugador tiene 6 estadísticas que suben automáticamente al jugar:

| Stat | Símbolo | Soft Cap | Bonus/Nivel | Cómo Subirla |
|------|---------|----------|-------------|--------------|
| **VITALITY** (Vitalidad) | ❤ | 40 | +2.0 HP | Recibir daño |
| **STRENGTH** (Fuerza) | 💪 | 30 | +0.5 Daño | Matar mobs |
| **DEXTERITY** (Destreza) | 🎯 | 25 | +2% Vel. Ataque | Atacar con arco/ballesta |
| **MINING** (Minería) | ⛏ | 45 | +5% Vel. Minado | Minar bloques |
| **AGILITY** (Agilidad) | 👟 | 25 | +1% Velocidad | Correr (sprint), nadar, caer |
| **FARMING** (Agricultura) | 🌾 | 50 | +3% Crec. Cultivos | Usar bonemeal, cosechar, pescar |

**Hard Cap:** Todas las estadísticas tienen un máximo de **64 niveles**.

---

## Sistema de Soft Cap

Después de alcanzar el "soft cap", los bonuses se reducen a la **mitad**:

```
Si nivel ≤ soft cap:
    bonus = nivel × bonus_por_nivel

Si nivel > soft cap:
    bonus = (soft_cap × bonus_por_nivel) + ((nivel - soft_cap) × bonus_por_nivel × 0.5)
```

**Ejemplo con Vitality (soft cap 40):**
- Niveles 0-40: +2.0 HP por nivel = **80 HP**
- Niveles 41-64: +1.0 HP por nivel = **24 HP**
- **Total máximo nivel 64:** 104 HP extra

Esto previene que las stats sean demasiado poderosas al nivel máximo.

---

## Progresión Automática

Las estadísticas suben automáticamente al realizar acciones relacionadas:

### Cómo Ganar XP

| Acción | Stat | XP Ganada | Detalles |
|--------|------|-----------|----------|
| **Correr** (sprint) | AGILITY | 0.1 XP/tick | ~2 XP por segundo corriendo |
| **Nadar** | AGILITY | 0.05 XP/tick | ~1 XP por segundo nadando |
| **Caer** | AGILITY | Altura × 0.1 | Caídas largas dan más XP |
| **Minar bloques** | MINING | Dureza × 0.5 | Bloques duros dan más XP |
| **Atacar con arco** | DEXTERITY | Daño × 0.1 | Por cada flecha que impacta |
| **Matar mobs** | STRENGTH | HP máx mob × 0.2 | Mobs fuertes dan más XP |
| **Recibir daño** | VITALITY | Daño × 0.3 | Cada vez que te golpean |
| **Usar bonemeal** | FARMING | 2 XP | Por cada uso |
| **Cosechar cultivos** | FARMING | 1 XP | Por bloque cosechado |
| **Pescar** | FARMING | 5 XP | Por cada pesca exitosa |

### XP Necesaria para Subir de Nivel

Usa la fórmula de XP de Minecraft vanilla:

```
Nivel 1:   7 XP
Nivel 5:   25 XP
Nivel 10:  106 XP
Nivel 20:  326 XP
Nivel 30:  849 XP
Nivel 50:  2,869 XP
Nivel 64:  ~4,500 XP
```

### Sincronización

- XP se sincroniza con el servidor cada **2 segundos**
- Cuando subes de nivel, sincronización **inmediata**
- Sonido de level up al subir estadística
- Optimizado para no causar lag

---

## Sistema de Perks

El mod incluye **56 perks** distribuidos en 8 categorías:

### Categorías de Perks

1. **Vitality Perks** (8 perks) - Regeneración, resistencia, supervivencia
   - Ejemplos: Iron Skin, Regeneration, Last Stand, Phoenix Heart

2. **Strength Perks** (8 perks) - Daño melee, críticos, área
   - Ejemplos: Power Strike, Critical Strike, Execute, Titan's Fury

3. **Dexterity Perks** (8 perks) - Velocidad de ataque, precisión
   - Ejemplos: Swift Strikes, Eagle Eye, Blade Dance, Sniper

4. **Mining Perks** (8 perks) - Velocidad, fortuna, excavación
   - Ejemplos: Efficient Mining, Fortune I/II, Excavator, Vein Finder

5. **Agility Perks** (8 perks) - Movilidad, saltos, velocidad
   - Ejemplos: Sprint Master, Double Jump, Wall Jump, Wind Runner

6. **Farming Perks** (8 perks) - Cultivos, cosecha, crecimiento
   - Ejemplos: Green Thumb, Mass Harvest, Nature's Blessing, Instant Growth

7. **Combo Perks** (8 perks) - Requieren múltiples stats altas
   - Ejemplos: Berserker Mode, Assassin's Mark, Ore Breaker

### Sistema de Tiers

Los perks se dividen en 3 tiers de poder:

- **Tier 1:** Perks básicos (bajo nivel requerido)
- **Tier 2:** Perks intermedios (nivel medio requerido)
- **Tier 3:** Perks avanzados (nivel alto requerido)

### Desbloqueo de Perks

- Cada perk requiere un **nivel mínimo** en su estadística asociada
- Se desbloquean gastando **niveles de XP de Minecraft**
- Algunos perks tienen **múltiples niveles** (max 1-5 dependiendo del perk)
- Los **Combo Perks** requieren múltiples estadísticas altas

### Estado de Implementación

⚠️ **NOTA:** El sistema de perks está parcialmente implementado:
- ✅ Todos los 56 perks están **registrados** en el mod
- ✅ Sistema de desbloqueo y upgrade **funcional**
- ⚠️ **Funcionalidad de muchos perks aún en desarrollo**
- ✅ GUI de perks **completa** con filtros por categoría y tier

---

## Soul Book

El **Soul Book** es el item central del mod:

### Características

- **Raridad:** EPIC (color púrpura)
- **Stack:** 1 (no apilable)
- **Fire Resistant:** Sí (no se quema en lava)
- **Función:** Abrir la GUI de estadísticas y perks

### Cómo Usar

1. **Obtener** el Soul Book (crafteo o modo creativo)
2. **Click derecho** con el Soul Book, o presionar **tecla R**
3. Se abre la GUI con 2 tabs:
   - **Stats Tab:** Ver y monitorear tus estadísticas
   - **Perks Tab:** Desbloquear y mejorar perks

### Integración con Curios

Si tienes el mod **Curios** instalado:
- Puedes equipar el Soul Book en un **slot especial "soul_book"**
- No ocupa espacio en tu inventario principal
- Acceso más fácil a la GUI

---

## Requisitos de Equipamiento

El mod añade **requisitos de stats** para usar equipamiento:

### Armaduras

| Tipo | VITALITY | Ejemplo |
|------|----------|---------|
| Leather | 5 | Armadura de cuero básica |
| Chainmail | 10 | Armadura de cadena |
| Iron | 15 | Armadura de hierro |
| Gold | 12 | Armadura de oro (requisito especial) |
| Diamond | 25 | Armadura de diamante |
| Netherite | 35 | Armadura de netherite |

### Armas y Herramientas

Las espadas, hachas, picos, palas y arcos requieren combinaciones de:
- **STRENGTH:** Para espadas y hachas
- **MINING:** Para picos y palas
- **DEXTERITY:** Para arcos y ballestas

### Mecánica

- Si intentas equipar un item sin los stats necesarios, **se cancela**
- Recibes un **mensaje en chat** indicando los requisitos
- Los requisitos están balanceados para alinearse con la progresión natural

---

## Configuración del Proyecto

### Dependencias

```gradle
minecraft: 1.21.1
neoforge: 21.1.77
curios: 9.5.1+1.21.1 (opcional)
```

### Build

```bash
# Compilar el mod
./gradlew build

# Ejecutar cliente de prueba
./gradlew runClient

# Limpiar build
./gradlew clean
```

### Archivos de Configuración

#### PracticeBalanceConfig.java
```java
AGILITY_XP_PER_SPRINT_TICK = 0.1
AGILITY_XP_PER_SWIM_TICK = 0.05
MINING_XP_MULTIPLIER = 0.5
DEXTERITY_XP_MULTIPLIER = 0.1
STRENGTH_XP_MULTIPLIER = 0.2
VITALITY_XP_MULTIPLIER = 0.3
FARMING_XP_BONEMEAL = 2.0
FARMING_XP_HARVEST = 1.0
FARMING_XP_FISHING = 5.0
SYNC_INTERVAL = 40 ticks (2 segundos)
```

---

## Comandos

El mod incluye el comando `/stats` con varios subcomandos:

```
/stats info                    # Ver tus propias stats
/stats info <jugador>          # Ver stats de otro jugador (OP)
/stats set <stat> <nivel>      # Modificar nivel de stat (OP nivel 2)
/stats reset                   # Resetear todas las stats y perks
/stats perks list              # Listar perks desbloqueados
```

---

## Arquitectura Técnica

### Estructura del Proyecto

```
src/main/java/com/maurimdev/statssystem/
├── StatsSystem.java                    (Clase principal)
├── client/                             (Código cliente)
│   ├── ui/screen/                      (GUIs)
│   │   ├── TabbedStatsScreen.java      (Pantalla principal con tabs)
│   │   ├── StatsScreen.java            (Tab de estadísticas)
│   │   └── PerksScreen.java            (Tab de perks)
│   └── ui/components/                  (Componentes UI)
├── core/                               (Lógica central)
│   ├── domain/                         (Modelos de datos)
│   │   ├── stats/PlayerStats.java      (Datos del jugador)
│   │   ├── stats/StatType.java         (Enum de stats)
│   │   └── perk/Perk.java              (Modelo de perk)
│   ├── progression/
│   │   └── PracticeModeProgression.java (Sistema de progresión)
│   └── service/
│       ├── StatsProgressionService.java (Cálculos de stats)
│       └── PerkService.java             (Servicio de perks)
├── gameplay/                           (Integración con juego)
│   ├── event/                          (Event handlers)
│   │   ├── ModEvents.java              (Eventos generales)
│   │   ├── StatBonusHandler.java       (Aplicar bonuses)
│   │   ├── VitalityPerksHandler.java   (Perks de Vitality)
│   │   ├── StrengthPerksHandler.java   (Perks de Strength)
│   │   ├── DexterityPerksHandler.java  (Perks de Dexterity)
│   │   ├── MiningPerksHandler.java     (Perks de Mining)
│   │   ├── AgilityPerksHandler.java    (Perks de Agility)
│   │   ├── FarmingPerksHandler.java    (Perks de Farming)
│   │   └── ComboPerksHandler.java      (Combo perks)
│   ├── command/StatsCommand.java       (Comandos)
│   └── item/SoulBookItem.java          (Soul Book item)
├── infrastructure/
│   ├── persistence/ModAttachments.java (Data Attachments)
│   ├── config/PracticeBalanceConfig.java
│   └── integration/CuriosIntegration.java
├── network/                            (Networking)
│   ├── ModMessages.java                (Registro de packets)
│   ├── SyncStatsPacket.java            (Sincronizar stats)
│   ├── SyncPerksPacket.java            (Sincronizar perks)
│   ├── UnlockPerkPacket.java           (Desbloquear perk)
│   ├── TogglePerkPacket.java           (Activar/desactivar perk)
│   └── ResetStatsPacket.java           (Reset completo)
└── init/                               (Inicialización)
    ├── ModItems.java                   (Registro de items)
    ├── ModPerks.java                   (Registro de perks)
    ├── ModCreativeTabs.java            (Creative tabs)
    └── EquipmentRequirementRegistry.java
```

### Componentes Clave

#### 1. Data Attachments (NeoForge 1.21.1)

Sistema moderno de persistencia que reemplaza Capabilities:
- Guardado/cargado automático en NBT
- Thread-safe
- Se adjunta directamente a la entidad del jugador

#### 2. Sistema de Networking

Packets para sincronización cliente-servidor:
- `SyncStatsPacket` - Sincroniza niveles y XP
- `SyncPerksPacket` - Sincroniza perks desbloqueados
- `UnlockPerkPacket` - Cliente solicita desbloquear perk
- `ResetStatsPacket` - Cliente solicita reset completo

#### 3. Event System

Eventos de NeoForge utilizados:
- `PlayerTickEvent.Post` - Detectar sprint/nadar
- `BlockEvent.BreakEvent` - Detectar minería
- `LivingDamageEvent.Post` - Detectar combate/daño
- `LivingDeathEvent` - Detectar kills de mobs
- Y muchos más para perks específicos

---

## Licencia

Este proyecto es propiedad de **MaurimDev**.

---

## Contacto

Para reportar bugs o sugerencias, contacta al autor a través de los canales oficiales del proyecto.

---

## Estado de Desarrollo

**Versión actual:** 1.0.0 (En desarrollo)

### Completado ✅
- Sistema de 6 estadísticas
- Progresión automática (Practice Mode)
- Registro de 56 perks
- GUI con tabs (Stats y Perks)
- Soul Book item
- Integración con Curios
- Sistema de persistencia (Data Attachments)
- Requisitos de equipamiento
- Sistema de comandos

### En Desarrollo ⚠️
- Funcionalidad completa de todos los perks
- Balance y ajustes finos
- Testing extensivo

### Futuro (Posiblemente) 🔮
- Sistema de dificultades opcional
- Más perks y mecánicas
- Compatibilidad con otros mods

---

**¡Disfruta del mod y buena suerte en tu progresión!**
