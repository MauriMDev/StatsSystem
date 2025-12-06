# Stats System

A comprehensive RPG-style stats and progression system mod for Minecraft.

**Current Version:** 1.0.0 (In Development)
**Minecraft Version:** 1.21.1
**Mod Loader:** NeoForge 21.1.77
**Author:** MauriMDev

---

## Overview

Stats System adds a permanent character progression system to Minecraft through 6 specialized attributes that level up automatically as you perform related actions, similar to The Elder Scrolls V: Skyrim. These attributes improve various aspects of your character and unlock perks (passive and active abilities) that modify gameplay.

---

## Table of Contents

- [Features](#features)
- [The 6 Attributes](#the-6-attributes)
- [Derived Attributes](#derived-attributes)
- [Progression System](#progression-system)
- [Perk System](#perk-system)
- [Soul Book](#soul-book)
- [Equipment Requirements](#equipment-requirements)
- [Commands](#commands)
- [Installation](#installation)
- [Configuration](#configuration)
- [Technical Architecture](#technical-architecture)
- [Development Status](#development-status)
- [License](#license)

---

## Features

- **6 Independent Attributes** with levels from 0 to 64
- **Automatic Progression** by performing actions (Skyrim-style)
- **Soft Cap System** to balance late-game progression
- **Perk System** with 56 unlockable abilities organized in 4 tiers
- **Soul Book** as the central item of the system
- **Equipment Requirements** based on attribute levels
- **Data Persistence** between game sessions using NeoForge Data Attachments
- **Curios Integration** to equip the Soul Book in a dedicated slot
- **Custom GUI** with tabs (Stats and Perks)
- **Admin Commands** for server management
- **Client-Server Synchronization** optimized to prevent lag
- **Weapon Scaling System** based on player attributes

---

## The 6 Attributes

Each player has 6 attributes that level up automatically while playing:

### VITALITY
- **Symbol:** Heart
- **Soft Cap:** 40
- **Hard Cap:** 64
- **Primary Effect:** +2.0 HP per level (soft capped)
- **How to Level Up:** Taking damage from any source
- **Secondary Effects:**
  - Base for health regeneration perks
  - Requirement for heavy armor

### STRENGTH
- **Symbol:** Muscle
- **Soft Cap:** 30
- **Hard Cap:** 64
- **Primary Effect:** +0.5 Attack Damage per level (unarmed only)
- **How to Level Up:** Killing mobs
- **Secondary Effects:**
  - Melee weapon scaling
  - Knockback resistance (+1% per level)
  - Requirement for melee weapons

### DEXTERITY
- **Symbol:** Target
- **Soft Cap:** 25
- **Hard Cap:** 64
- **Primary Effect:** +2% Attack Speed per level
- **How to Level Up:** Attacking with bow or crossbow
- **Secondary Effects:**
  - Critical hit chance (+0.5% per level)
  - Critical damage (+1% per level)
  - Projectile range (+0.5% per level)
  - Ranged weapon scaling

### MINING
- **Symbol:** Pickaxe
- **Soft Cap:** 45
- **Hard Cap:** 64
- **Primary Effect:** +5% Mining Speed per level
- **How to Level Up:** Breaking blocks
- **Secondary Effects:**
  - Mining fortune (via perks)
  - Tool durability reduction (via perks)
  - Requirement for mining tools

### AGILITY
- **Symbol:** Running Shoe
- **Soft Cap:** 25
- **Hard Cap:** 64
- **Primary Effect:** +1% Movement Speed per level
- **How to Level Up:** Sprinting, swimming, falling
- **Secondary Effects:**
  - Fall damage resistance (+0.5% per level)
  - Hunger consumption reduction (+0.4% per level)
  - Maximum food points: 20 + (level/64)^1.5 × 40
  - Movement abilities (via perks)

### FARMING
- **Symbol:** Wheat
- **Soft Cap:** 50
- **Hard Cap:** 64
- **Primary Effect:** +3% Crop Growth Speed per level
- **How to Level Up:** Using bonemeal, harvesting crops, fishing
- **Secondary Effects:**
  - Extra crop drops (+0.4% per level)
  - Harvest efficiency (via perks)
  - Crop growth bonuses (via perks)

---

## Derived Attributes

Derived attributes are calculated automatically based on your primary attribute levels. These are displayed in the custom HUD:

### From VITALITY
- **Max Health:** Base 20 HP + (level/64)^1.8 × 80 HP (max 100 HP at level 64)
- **Health Regeneration:** Only through perks
- **Armor:** From equipped armor + Iron Skin perk

### From STRENGTH
- **Attack Damage:** Base 1 + (level/64)^1.8 × 8 (unarmed only, max 9 at level 64)
- **Knockback Resistance:** level × 1.0% (max 64% at level 64)

### From DEXTERITY
- **Critical Chance:** level × 0.5% (max 32% at level 64)
- **Critical Damage:** level × 1.0% (max +64% at level 64)
- **Projectile Range:** level × 0.5% (max +32% at level 64)

### From MINING
- **Mining Speed:** floor(level / 3) × 1.0% (max +21% at level 64)

### From AGILITY
- **Fall Resistance:** level × 0.5% (max 32% at level 64)
- **Hunger Reduction:** level × 0.4% (max 25.6% at level 64)
- **Max Energy:** 20 base + (level/64)^1.5 × 40 (max 60 food points at level 64)

### From FARMING
- **Crop Drop Bonus:** level × 0.4% (max 25.6% at level 64)

---

## Progression System

### XP Formula

The system uses the same XP formula as vanilla Minecraft:

- **Levels 1-16:** XP = level² + 6 × level
- **Levels 17-31:** XP = 2.5 × level² - 40.5 × level + 360
- **Levels 32-64:** XP = 4.5 × level² - 162.5 × level + 2220

### Soft Cap System

After reaching the "soft cap", bonuses are reduced to half:

```
If level <= soft cap:
    bonus = level × bonus_per_level

If level > soft cap:
    bonus = (soft_cap × bonus_per_level) + ((level - soft_cap) × bonus_per_level × 0.5)
```

**Example with Vitality (soft cap 40):**
- Levels 0-40: +2.0 HP per level = 80 HP
- Levels 41-64: +1.0 HP per level = 24 HP
- **Total at level 64:** 104 HP bonus

### Practice Mode (Currently Implemented)

Gain XP for specific attributes by performing related actions:

| Action | Attribute | XP Gained |
|--------|-----------|-----------|
| Running (sprint) | AGILITY | 0.1 XP/tick (~2 XP/s) |
| Swimming | AGILITY | 0.05 XP/tick (~1 XP/s) |
| Falling | AGILITY | Height × 0.1 |
| Breaking blocks | MINING | Hardness × 0.5 |
| Attacking with bow | DEXTERITY | Damage × 0.1 |
| Killing mobs | STRENGTH | Mob max HP × 0.2 |
| Taking damage | VITALITY | Damage × 0.3 |
| Using bonemeal | FARMING | 2 XP |
| Harvesting crops | FARMING | 1 XP |
| Fishing | FARMING | 5 XP |

### Synchronization

- XP synchronizes with the server every 2 seconds (40 ticks)
- When leveling up, synchronization is immediate
- Level up sound plays when an attribute increases
- Optimized to prevent server lag

---

## Perk System

The mod includes 56 perks distributed across 8 categories:

### Perk Categories

1. **Vitality Perks** (8 perks) - Regeneration, resistance, survival
2. **Strength Perks** (8 perks) - Melee damage, critical hits, area damage
3. **Dexterity Perks** (8 perks) - Attack speed, precision, ranged combat
4. **Mining Perks** (8 perks) - Mining speed, fortune, excavation
5. **Agility Perks** (8 perks) - Mobility, jumping, speed
6. **Farming Perks** (8 perks) - Crops, harvesting, growth
7. **Combo Perks** (8 perks) - Require multiple high attributes

### Perk Tiers

Perks are divided into 4 power tiers:

- **Tier 1:** Basic perks (low level requirement, 5-10 XP levels cost)
- **Tier 2:** Intermediate perks (medium level requirement, 18-25 XP levels cost)
- **Tier 3:** Advanced perks (high level requirement, 45-50 XP levels cost)
- **Combo:** Require multiple high attributes (40-60 XP levels cost)

### Unlocking Perks

- Each perk requires a minimum level in its associated attribute
- Perks are unlocked by spending Minecraft XP levels
- Some perks have multiple upgrade levels (max 1-5 depending on the perk)
- Combo Perks require multiple high attributes

### Notable Perks

**Tier 1 Examples:**
- **Iron Skin** (Vitality): +3 armor per level, upgradeable to level 5
- **Power Strike** (Strength): +10% melee damage per level, upgradeable to level 5
- **Swift Strikes** (Dexterity): +10% attack speed per level, upgradeable to level 5
- **Efficient Mining** (Mining): +15% mining speed per level, upgradeable to level 5

**Tier 3 Examples:**
- **Phoenix Heart** (Vitality): Automatically revive with 50% HP (24-hour cooldown)
- **Execute** (Strength): +100% damage to enemies below 20% HP
- **Sniper** (Dexterity): Fully charged arrows guarantee critical hits
- **Excavator** (Mining): Mine in a 3×3 area (toggleable)
- **Double Jump** (Agility): Jump again while in the air
- **Instant Growth** (Farming): Bonemeal instantly grows crops

**Combo Perks:**
- **Berserker Mode** (Strength 56 + Vitality 56): +50% damage and +30% attack speed when below 30% HP
- **Ore Breaker** (Strength 40 + Dexterity 40 + Mining 56): Mine ores with bare hands
- **Assassin's Mark** (Dexterity 48 + Strength 32): +100% damage when attacking from behind
- **Nature Warrior** (Farming 40 + Agility 32): +0.5 HP/s and +20% speed near plants

### Implementation Status

- Total Perks: 56
- Implemented: 42 (75%)
- Pending: 14 (25%)

All 56 perks are registered in the mod, the GUI is fully functional, and the unlock/upgrade system works. However, some perk effects are still under development.

---

## Soul Book

The **Soul Book** is the central item of the mod:

### Characteristics

- **Rarity:** EPIC (purple)
- **Stack Size:** 1 (not stackable)
- **Fire Resistant:** Yes (doesn't burn in lava/fire)
- **Function:** Opens the stats and perks GUI

### How to Use

1. Obtain the Soul Book (crafting or creative mode)
2. Right-click with the Soul Book, or press the **R key**
3. A GUI opens with 2 tabs:
   - **Stats Tab:** View and monitor your attributes
   - **Perks Tab:** Unlock and upgrade perks

### Curios Integration

If you have the **Curios** mod installed:
- You can equip the Soul Book in a special "soul_book" slot
- Doesn't take up space in your main inventory
- Easier access to the GUI

---

## Equipment Requirements

The mod adds attribute requirements to use equipment:

### Armor Requirements

| Armor Type | Required VITALITY |
|------------|-------------------|
| Leather | 5 |
| Chainmail | 10 |
| Iron | 15 |
| Gold | 12 |
| Diamond | 25 |
| Netherite | 35 |

### Weapon and Tool Requirements

Swords, axes, pickaxes, shovels, and bows require combinations of:
- **STRENGTH:** For swords and axes
- **MINING:** For pickaxes and shovels
- **DEXTERITY:** For bows and crossbows

### Mechanics

- If you try to equip an item without the required attributes, it's cancelled
- You receive a chat message indicating the requirements
- Requirements are balanced to align with natural progression

---

## Commands

The mod includes the `/stats` command with several subcommands:

```
/stats info                    # View your own stats
/stats info <player>          # View another player's stats (OP required)
/stats set <stat> <level>     # Modify stat level (OP level 2 required)
/stats reset                  # Reset all stats and perks
/stats perks list             # List unlocked perks
```

---

## Installation

### Requirements

- Minecraft 1.21.1
- NeoForge 21.1.77 or later
- Curios API 9.5.1+1.21.1 (optional, for Soul Book slot)

### Steps

1. Download the mod JAR file
2. Place it in your `mods` folder
3. Launch Minecraft with NeoForge
4. (Optional) Install Curios for enhanced Soul Book functionality

---

## Configuration

### Balance Configuration

The mod includes a configuration file for XP gain rates:

```java
// Agility
AGILITY_XP_PER_SPRINT_TICK = 0.1
AGILITY_XP_PER_SWIM_TICK = 0.05

// Mining
MINING_XP_MULTIPLIER = 0.5

// Dexterity
DEXTERITY_XP_MULTIPLIER = 0.1

// Strength
STRENGTH_XP_MULTIPLIER = 0.2

// Vitality
VITALITY_XP_MULTIPLIER = 0.3

// Farming
FARMING_XP_BONEMEAL = 2.0
FARMING_XP_HARVEST = 1.0
FARMING_XP_FISHING = 5.0

// Sync
SYNC_INTERVAL = 40 ticks (2 seconds)
```

### Building from Source

```bash
# Build the mod
./gradlew build

# Run client test environment
./gradlew runClient

# Clean build
./gradlew clean
```

---

## Technical Architecture

### Project Structure

```
src/main/java/com/maurimdev/statssystem/
├── StatsSystem.java                    (Main mod class)
├── client/                             (Client-side code)
│   ├── ui/screen/                      (GUI screens)
│   │   ├── TabbedStatsScreen.java      (Main screen with tabs)
│   │   ├── StatsScreen.java            (Stats tab)
│   │   └── PerksScreen.java            (Perks tab)
│   └── ui/components/                  (UI components)
├── core/                               (Core logic)
│   ├── domain/                         (Data models)
│   │   ├── stats/PlayerStats.java      (Player data)
│   │   ├── stats/StatType.java         (Stats enum)
│   │   └── perk/Perk.java              (Perk model)
│   ├── progression/
│   │   └── PracticeModeProgression.java (Progression system)
│   └── service/
│       ├── StatsProgressionService.java (Stats calculations)
│       └── PerkService.java             (Perk service)
├── gameplay/                           (Game integration)
│   ├── event/                          (Event handlers)
│   │   ├── ModEvents.java              (General events)
│   │   ├── StatBonusHandler.java       (Apply bonuses)
│   │   └── [Various perk handlers]     (Perk implementations)
│   ├── command/StatsCommand.java       (Commands)
│   └── item/SoulBookItem.java          (Soul Book item)
├── infrastructure/
│   ├── persistence/ModAttachments.java (Data Attachments)
│   ├── config/PracticeBalanceConfig.java
│   └── integration/CuriosIntegration.java
├── network/                            (Networking)
│   ├── ModMessages.java                (Packet registration)
│   ├── SyncStatsPacket.java            (Sync stats)
│   ├── SyncPerksPacket.java            (Sync perks)
│   ├── UnlockPerkPacket.java           (Unlock perk)
│   ├── TogglePerkPacket.java           (Toggle perk)
│   └── ResetStatsPacket.java           (Reset all)
└── init/                               (Initialization)
    ├── ModItems.java                   (Item registration)
    ├── ModPerks.java                   (Perk registration)
    ├── ModCreativeTabs.java            (Creative tabs)
    └── EquipmentRequirementRegistry.java
```

### Key Components

#### 1. Data Attachments (NeoForge 1.21.1)

Modern persistence system that replaces Capabilities:
- Automatic save/load to NBT
- Thread-safe
- Directly attached to player entity

#### 2. Networking System

Packets for client-server synchronization:
- `SyncStatsPacket` - Synchronizes levels and XP
- `SyncPerksPacket` - Synchronizes unlocked perks
- `UnlockPerkPacket` - Client requests to unlock perk
- `ResetStatsPacket` - Client requests complete reset

#### 3. Event System

NeoForge events used:
- `PlayerTickEvent.Post` - Detect sprinting/swimming
- `BlockEvent.BreakEvent` - Detect mining
- `LivingDamageEvent.Post` - Detect combat/damage
- `LivingDeathEvent` - Detect mob kills
- Many more for specific perk effects

---

## Development Status

**Current Version:** 1.0.0 (In Development)

### Completed

- [x] 6 attribute system with exponential scaling
- [x] Automatic progression (Practice Mode)
- [x] 56 perks registered (42 fully implemented)
- [x] Tabbed GUI (Stats and Perks)
- [x] Soul Book item
- [x] Curios integration
- [x] Data persistence (Data Attachments)
- [x] Equipment requirements
- [x] Admin commands
- [x] Weapon scaling system
- [x] Custom HUD overlays

### In Development

- [ ] Complete functionality for remaining 14 perks
- [ ] Balance adjustments and fine-tuning
- [ ] Extensive multiplayer testing
- [ ] Additional XP gain events

### Future (Potentially)

- [ ] Optional difficulty modes
- [ ] Additional perks and mechanics
- [ ] Compatibility with other popular mods
- [ ] Configuration GUI

---

## License

All Rights Reserved

This project is the property of **MauriMDev**.

---

## Contact

For bug reports or suggestions, please use the GitHub Issues page.

---

**Happy progressing!**
