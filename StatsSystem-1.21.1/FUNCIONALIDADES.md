# 📊 Stats System - Documentación Completa de Funcionalidades

**Versión:** 1.21.1
**Última actualización:** 2025-12-02

---

## 📑 Tabla de Contenidos

1. [Sistema de Atributos Principales](#1-sistema-de-atributos-principales)
2. [Atributos Derivados](#2-atributos-derivados)
3. [Sistema de Perks](#3-sistema-de-perks)
4. [Sistema de Progresión y XP](#4-sistema-de-progresión-y-xp)
5. [Mecánicas de Combate](#5-mecánicas-de-combate)
6. [Estado de Implementación](#6-estado-de-implementación)

---

## 1. Sistema de Atributos Principales

El mod cuenta con **6 atributos principales** que definen las capacidades del jugador:

### 1.1 📊 Tabla Resumen de Atributos

| Atributo | Símbolo | Soft Cap | Hard Cap | Bonus por Nivel |
|----------|---------|----------|----------|----------------|
| **Vitalidad** (Vitality) | ❤ | 40 | 64 | 2.0 |
| **Fuerza** (Strength) | 💪 | 30 | 64 | 0.5 |
| **Destreza** (Dexterity) | 🎯 | 25 | 64 | 0.02 |
| **Minería** (Mining) | ⛏ | 45 | 64 | 0.05 |
| **Agilidad** (Agility) | 👟 | 25 | 64 | 0.01 |
| **Agricultura** (Farming) | 🌾 | 50 | 64 | 0.03 |

### 1.2 📈 Curva de Progresión

Todos los atributos utilizan una **curva exponencial** para su progresión:

```
Bonus = (nivel/64)^1.8 × MaxBonus
```

Esta curva asegura que:
- **Early Game (1-20):** Aumentos pequeños (~15-25% del potencial)
- **Mid Game (21-45):** Aumentos notables (~50-70% del potencial)
- **Late Game (46-64):** ¡ÉPICO! (~100% del potencial)

**Soft Cap:** El nivel donde la progresión se vuelve más difícil (requiere más XP)
**Hard Cap:** Nivel máximo absoluto (64 para todos los atributos)

### 1.3 🎯 Descripción Detallada por Atributo

#### ❤ **VITALIDAD (Vitality)**
- **Propósito:** Aumenta tu resistencia y capacidad de supervivencia
- **Soft Cap:** 40
- **Afecta a:**
  - Vida Máxima
  - Regeneración de Vida (solo via perks)
  - Armadura Total (via perks)

#### 💪 **FUERZA (Strength)**
- **Propósito:** Aumenta tu poder de ataque cuerpo a cuerpo
- **Soft Cap:** 30
- **Afecta a:**
  - Daño de Ataque (solo sin arma)
  - Resistencia al Knockback
  - Escalado de armas cuerpo a cuerpo

#### 🎯 **DESTREZA (Dexterity)**
- **Propósito:** Mejora tu precisión y velocidad de ataque
- **Soft Cap:** 25
- **Afecta a:**
  - Probabilidad de Crítico
  - Daño Crítico
  - Alcance de Proyectiles
  - Escalado de armas de rango

#### ⛏ **MINERÍA (Mining)**
- **Propósito:** Optimiza tu eficiencia al minar
- **Soft Cap:** 45
- **Afecta a:**
  - Velocidad de Minado
  - Drops de minerales (via perks)
  - Durabilidad de herramientas (via perks)

#### 👟 **AGILIDAD (Agility)**
- **Propósito:** Aumenta tu movilidad y resistencia
- **Soft Cap:** 25
- **Afecta a:**
  - Resistencia a Caída
  - Reducción de Hambre
  - Energía Máxima (hambre total)
  - Velocidad de movimiento (via perks)

#### 🌾 **AGRICULTURA (Farming)**
- **Propósito:** Mejora tu capacidad de cultivar y cosechar
- **Soft Cap:** 50
- **Afecta a:**
  - Drops Extra de Cultivos
  - Velocidad de crecimiento (via perks)
  - Eficiencia de cosecha (via perks)

---

## 2. Atributos Derivados

Los atributos derivados son **calculados automáticamente** basándose en los niveles de los atributos principales. Se muestran en el HUD del jugador.

### 2.1 ❤ Derivados de VITALIDAD

#### 💚 **Vida Máxima (Max Health)**
**Fórmula:** `(nivel/64)^1.8 × 80 HP`

| Nivel | HP Bonus | Total (base 20 HP) | Corazones |
|-------|----------|-------------------|-----------|
| 0 | 0 HP | 20 HP | 10 ❤ |
| 1 | 0.04 HP | 20.04 HP | 10 ❤ |
| 10 | 2.5 HP | 22.5 HP | 11.25 ❤ |
| 20 | 7.8 HP | 27.8 HP | 13.9 ❤ |
| 32 | 20.5 HP | 40.5 HP | 20.25 ❤ |
| 40 (soft cap) | 29.8 HP | 49.8 HP | 24.9 ❤ |
| 50 | 47.2 HP | 67.2 HP | 33.6 ❤ |
| 64 | 80 HP | 100 HP | 50 ❤ |

**Nota:** 1 corazón = 2 HP

#### 🔋 **Regeneración de Vida (Health Regen)**
**Estado:** ⚠️ **YA NO ESCALA CON NIVEL**

La regeneración de vida **solo se obtiene mediante perks**:
- Perk: **Regeneration** (Vitality Tier 1) - +0.5/1.0/2.0 HP periódicamente
- Perk: **Nature Warrior** (Combo) - +0.5 HP/s cerca de plantas
- Efectos de poción de Regeneración

#### 🛡 **Armadura Total (Armor)**
**Estado:** Calculado directamente del equipo del jugador + perks

- **Iron Skin** (Perk Tier 1): +3 por nivel (max +15 armadura)

### 2.2 💪 Derivados de FUERZA

#### ⚔️ **Daño de Ataque (Attack Damage)**
**Fórmula:** `(nivel/64)^1.8 × 8 DMG`

**⚠️ IMPORTANTE:** Este bonus **SOLO se aplica sin arma** (puño). Con arma, el daño viene del weapon scaling.

| Nivel | Daño Bonus | Daño Puño Total | vs Espada Madera (4 base) |
|-------|------------|-----------------|---------------------------|
| 0 | 0 | 1 DMG | Espada: 4 DMG ✅ |
| 10 | 0.3 DMG | 1.3 DMG | Espada: 4 + scaling ✅ |
| 20 | 0.8 DMG | 1.8 DMG | Espada: 4 + scaling ✅ |
| 32 | 2.0 DMG | 3.0 DMG | Espada: 4 + scaling ✅ |
| 50 | 4.7 DMG | 5.7 DMG | Espada: 4 + scaling ✅ |
| 64 | 8 DMG | 9 DMG | Espada Diamante: 7 + 30 = 37 DMG ✅ |

**Balance:** El puño es viable en early game, pero **cualquier arma es superior** incluso a nivel 64.

#### 💪 **Resistencia al Knockback (Knockback Resistance)**
**Fórmula:** `nivel × 1.0%`

| Nivel | Resistencia |
|-------|-------------|
| 1 | +1% |
| 10 | +10% |
| 20 | +20% |
| 32 | +32% |
| 50 | +50% |
| 64 | +64% |

### 2.3 🎯 Derivados de DESTREZA

#### 🎲 **Probabilidad de Crítico (Crit Chance)**
**Fórmula:** `nivel × 0.5%`

| Nivel | Probabilidad |
|-------|--------------|
| 1 | +0.5% |
| 10 | +5% |
| 20 | +10% |
| 32 | +16% |
| 50 | +25% |
| 64 | +32% |

#### 💥 **Daño Crítico (Crit Damage)**
**Fórmula:** `nivel × 1.0%`

| Nivel | Daño Crítico Adicional |
|-------|------------------------|
| 1 | +1% |
| 10 | +10% |
| 20 | +20% |
| 32 | +32% |
| 50 | +50% |
| 64 | +64% |

**Ejemplo:** Con 64% daño crítico, un hit de 10 DMG hace 16.4 DMG en crítico.

#### 🏹 **Alcance de Proyectiles (Projectile Range)**
**Fórmula:** `nivel × 0.5%`

| Nivel | Alcance Adicional |
|-------|-------------------|
| 1 | +0.5% |
| 10 | +5% |
| 20 | +10% |
| 32 | +16% |
| 50 | +25% |
| 64 | +32% |

### 2.4 ⛏ Derivados de MINERÍA

#### ⛏️ **Velocidad de Minado (Mining Speed)**
**Fórmula:** `floor(nivel / 3) × 1.0%`

| Nivel | Velocidad |
|-------|-----------|
| 3 | +1% |
| 15 | +5% |
| 30 | +10% |
| 45 (soft cap) | +15% |
| 64 | +21% |

**Nota:** Se obtiene +1% cada 3 niveles.

### 2.5 👟 Derivados de AGILIDAD

#### 🪂 **Resistencia a Caída (Fall Resistance)**
**Fórmula:** `nivel × 0.5%`

| Nivel | Reducción de Daño |
|-------|-------------------|
| 1 | +0.5% |
| 10 | +5% |
| 20 | +10% |
| 32 | +16% |
| 50 | +25% |
| 64 | +32% |

#### 🍖 **Reducción de Hambre (Hunger Reduction)**
**Fórmula:** `nivel × 0.4%`

| Nivel | Reducción |
|-------|-----------|
| 1 | +0.4% |
| 10 | +4% |
| 20 | +8% |
| 32 | +12.8% |
| 50 | +20% |
| 64 | +25.6% |

#### ⚡ **Energía Máxima (Max Energy)**
**Fórmula:** `20 base + (nivel/64)^1.5 × 40 bonus`

| Nivel | Food Points | Muslitos 🍖 |
|-------|-------------|-------------|
| 0 | 20 | 10 |
| 10 | 23 | 11.5 |
| 20 | 28 | 14 |
| 32 | 34 | 17 |
| 40 | 38 | 19 |
| 50 | 44 | 22 |
| 64 | 60 | 30 |

**Nota:** 1 muslito = 2 puntos de hambre

### 2.6 🌾 Derivados de AGRICULTURA

#### 🌾 **Drops Extra de Cultivos (Crop Drops)**
**Fórmula:** `nivel × 0.4%`

| Nivel | Probabilidad de Drop Extra |
|-------|----------------------------|
| 1 | +0.4% |
| 10 | +4% |
| 20 | +8% |
| 32 | +12.8% |
| 50 (soft cap) | +20% |
| 64 | +25.6% |

---

## 3. Sistema de Perks

El mod cuenta con **56 perks totales** distribuidos en:
- **18 perks Tier 1** (3 por atributo × 6 atributos)
- **18 perks Tier 2** (3 por atributo × 6 atributos)
- **12 perks Tier 3** (2 por atributo × 6 atributos)
- **8 perks Combo** (requieren múltiples atributos)

### 3.1 Tiers y Requisitos

| Tier | Nivel Requerido | Costo Base XP | Color |
|------|-----------------|---------------|-------|
| **Tier I** | Nivel varies (5-8) | 5-10 niveles | 🔵 Azul |
| **Tier II** | Nivel varies (18-25) | 18-25 niveles | 🟠 Naranja |
| **Tier III** | Nivel varies (45-50) | 45-50 niveles | 🔴 Rojo |
| **Combo** | Múltiples stats | 40-60 niveles | 🟣 Morado |

### 3.2 ❤ Perks de VITALIDAD

#### **Tier 1**

##### 🛡 **Iron Skin** (Upgradeable: 5 niveles)
- **Costo:** 8 niveles XP
- **Efecto:** Tu piel se endurece como el hierro
- **Por nivel:**
  - Nivel 1: +3 🛡 Armadura (1 pieza)
  - Nivel 2: +6 🛡 Armadura (2 piezas)
  - Nivel 3: +9 🛡 Armadura (3 piezas)
  - Nivel 4: +12 🛡 Armadura (casi completa)
  - Nivel 5: +15 🛡 Armadura (set completo de hierro!)
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `VitalityPerksHandler.java`, `StatBonusHandler.java`

##### 🔋 **Regeneration** (Upgradeable: 3 niveles)
- **Costo:** 10 niveles XP
- **Efecto:** Regeneración de salud natural
- **Por nivel:**
  - Nivel 1: +0.5 HP cada 5 segundos
  - Nivel 2: +1.0 HP cada 3 segundos
  - Nivel 3: +2.0 HP cada 2 segundos
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `VitalityPerksHandler.java`

#### **Tier 2**

##### 🍖 **Second Wind** (Upgradeable: 3 niveles)
- **Costo:** 20 niveles XP
- **Efecto:** Metaboliza la comida más rápido
- **Por nivel:**
  - Cooldown de comida: -20% por nivel
  - Saturación recibida: +10% por nivel
- **Estado:** ⏳ **NO IMPLEMENTADO**

#### **Tier 3**

##### 🔥 **Phoenix Heart** (Nivel único)
- **Costo:** 50 niveles XP
- **Efecto:** Renaces de las cenizas
  - Revives automáticamente con 50% HP
  - Otorga Resistencia II por 10 segundos
  - Cooldown: 1 día real (24 horas)
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `VitalityPerksHandler.java`

### 3.3 💪 Perks de FUERZA

#### **Tier 1**

##### ⚔️ **Power Strike** (Upgradeable: 5 niveles)
- **Costo:** 5 niveles XP
- **Efecto:** +10% daño melee por nivel
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `StrengthPerksHandler.java`

##### 💥 **Knockback Mastery** (Upgradeable: 3 niveles)
- **Costo:** 7 niveles XP
- **Efecto:** +15% knockback por nivel
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `StrengthPerksHandler.java`

##### 😵 **Heavy Hitter** (Nivel único)
- **Costo:** 8 niveles XP
- **Efecto:** 5% chance de aturdir al enemigo por 1 segundo
- **Estado:** ⏳ **NO IMPLEMENTADO**

#### **Tier 2**

##### 🎯 **Critical Strike** (Upgradeable: 3 niveles)
- **Costo:** 20 niveles XP
- **Efecto:** +10% chance de crítico melee por nivel (crítico = +50% daño)
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `StrengthPerksHandler.java`

##### 🔨 **Armor Breaker** (Upgradeable: 3 niveles)
- **Costo:** 22 niveles XP
- **Efecto:** Ignora 20% de armadura enemiga por nivel
- **Estado:** ⏳ **NO IMPLEMENTADO**

##### ⚔️ **Cleave** (Nivel único)
- **Costo:** 18 niveles XP
- **Efecto:** Los golpes melee afectan un área de 2 bloques
- **Estado:** ⏳ **NO IMPLEMENTADO**

#### **Tier 3**

##### 💀 **Execute** (Nivel único)
- **Costo:** 45 niveles XP
- **Efecto:** +100% daño a enemigos con menos de 20% HP
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `StrengthPerksHandler.java`

##### 🔥 **Titan's Fury** (Nivel único)
- **Costo:** 50 niveles XP
- **Efecto:** Cada kill aumenta tu poder
  - +5% daño por kill
  - Stack max 5x
  - Dura 10 segundos
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `StrengthPerksHandler.java`

### 3.4 🎯 Perks de DESTREZA

#### **Tier 1**

##### ⚡ **Swift Strikes** (Upgradeable: 5 niveles)
- **Costo:** 5 niveles XP
- **Efecto:** +10% velocidad de ataque por nivel
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `DexterityPerksHandler.java`

##### 🏹 **Quick Draw** (Upgradeable: 3 niveles)
- **Costo:** 6 niveles XP
- **Efecto:** -10% tiempo de carga de arco/ballesta por nivel
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `DexterityPerksHandler.java`

##### 👣 **Light Steps** (Nivel único)
- **Costo:** 8 niveles XP
- **Efecto:** No activas pressure plates ni tripwires
- **Estado:** ⏳ **NO IMPLEMENTADO**

#### **Tier 2**

##### 🦅 **Eagle Eye** (Upgradeable: 3 niveles)
- **Costo:** 18 niveles XP
- **Efecto:** +15% precisión y alcance de proyectiles por nivel
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `DexterityPerksHandler.java`

##### 🛡️ **Parry** (Nivel único)
- **Costo:** 22 niveles XP
- **Efecto:** Bloquear en timing perfecto refleja 50% del daño
- **Estado:** ⏳ **NO IMPLEMENTADO**

##### 🏹 **Piercing Shot** (Nivel único)
- **Costo:** 20 niveles XP
- **Efecto:** Las flechas atraviesan 1 enemigo
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `DexterityPerksHandler.java`

#### **Tier 3**

##### ⚔️ **Blade Dance** (Nivel único)
- **Costo:** 45 niveles XP
- **Efecto:** Cada hit aumenta velocidad de ataque
  - +10% velocidad
  - Stack 5x
  - Dura 8 segundos
- **Estado:** ⏳ **NO IMPLEMENTADO**

##### 🎯 **Sniper** (Nivel único)
- **Costo:** 48 niveles XP
- **Efecto:** Flechas perfectas
  - Sin caída de daño por distancia
  - Crítico garantizado si está totalmente cargada
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `DexterityPerksHandler.java`

### 3.5 ⛏ Perks de MINERÍA

#### **Tier 1**

##### ⚡ **Efficient Mining** (Upgradeable: 5 niveles)
- **Costo:** 5 niveles XP
- **Efecto:** +15% velocidad de minado por nivel
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `MiningPerksHandler.java`

##### 💎 **Vein Finder** (Nivel único, Toggle)
- **Costo:** 8 niveles XP
- **Efecto:** Resalta los ores cercanos en un radio de 8 bloques
- **Estado:** ⏳ **NO IMPLEMENTADO**

##### 🛠️ **Preservation** (Upgradeable: 3 niveles)
- **Costo:** 7 niveles XP
- **Efecto:** -15% desgaste de herramientas de minado por nivel
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `MiningPerksHandler.java`

#### **Tier 2**

##### ✨ **Fortune I** (Nivel único)
- **Costo:** 22 niveles XP
- **Efecto:** Fortuna I en herramientas de minado sin encantamiento
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `MiningPerksHandler.java`

##### 🎯 **Tunnel Vision** (Upgradeable: 3 niveles)
- **Costo:** 18 niveles XP
- **Efecto:** +10% velocidad al minar en línea recta (3+ bloques seguidos)
- **Estado:** ⏳ **NO IMPLEMENTADO**

##### 🔧 **Unbreakable Tools** (Nivel único)
- **Costo:** 25 niveles XP
- **Efecto:** Las herramientas de minado nunca se rompen (se detienen al 1% durabilidad)
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `MiningPerksHandler.java`

#### **Tier 3**

##### ✨✨ **Fortune II** (Nivel único)
- **Costo:** 45 niveles XP
- **Efecto:** Fortuna II en herramientas de minado sin encantamiento
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `MiningPerksHandler.java`

##### 🏗️ **Excavator** (Nivel único, Toggle)
- **Costo:** 50 niveles XP
- **Efecto:** Minado 3×3 con pico
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `MiningPerksHandler.java`

### 3.6 👟 Perks de AGILIDAD

#### **Tier 1**

##### 🏃 **Sprint Master** (Upgradeable: 5 niveles)
- **Costo:** 5 niveles XP
- **Efecto:** -20% consumo de hambre al correr por nivel
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `AgilityPerksHandler.java`

##### 🪶 **Feather Fall** (Upgradeable: 3 niveles)
- **Costo:** 7 niveles XP
- **Efecto:** -30% daño de caída por nivel
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `AgilityPerksHandler.java`

##### 🦘 **Leap** (Upgradeable: 3 niveles)
- **Costo:** 8 niveles XP
- **Efecto:** +25% altura de salto por nivel
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `AgilityPerksHandler.java`

#### **Tier 2**

##### 🧗 **Wall Jump** (Nivel único)
- **Costo:** 20 niveles XP
- **Efecto:** Saltar contra muros permite un segundo salto (habilidad de parkour)
- **Estado:** ⏳ **NO IMPLEMENTADO**

##### 💨 **Air Dash** (Nivel único)
- **Costo:** 25 niveles XP
- **Efecto:** Dash aéreo en la dirección que miras (cooldown: 3 segundos)
- **Estado:** ⏳ **NO IMPLEMENTADO**

##### 🛬 **Safe Landing** (Nivel único)
- **Costo:** 18 niveles XP
- **Efecto:** Sin daño de caída hasta 20 bloques
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `AgilityPerksHandler.java`

#### **Tier 3**

##### 💨 **Wind Runner** (Nivel único)
- **Costo:** 45 niveles XP
- **Efecto:** Sprint no consume hambre (requiere Sprint Master nivel 5)
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `AgilityPerksHandler.java`

##### 🦘🦘 **Double Jump** (Nivel único)
- **Costo:** 50 niveles XP
- **Efecto:** Doble salto en el aire
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `AgilityPerksHandler.java`

### 3.7 🌾 Perks de AGRICULTURA

#### **Tier 1**

##### 🌱 **Green Thumb** (Upgradeable: 5 niveles)
- **Costo:** 5 niveles XP
- **Efecto:** +20% velocidad de crecimiento de plantas por nivel
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `FarmingPerksHandler.java`

##### 🌾 **Harvest Master** (Nivel único)
- **Costo:** 7 niveles XP
- **Efecto:** Cosechar replanta automáticamente (usa las semillas del drop)
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `FarmingPerksHandler.java`

##### 🦴 **Fertility** (Upgradeable: 3 niveles)
- **Costo:** 8 niveles XP
- **Efecto:** Bonemeal afecta un área 3×3 + 1 bloque por nivel
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `FarmingPerksHandler.java`

#### **Tier 2**

##### 🌾🌾 **Mass Harvest** (Nivel único)
- **Costo:** 20 niveles XP
- **Efecto:** Cosecha cultivos en área 3×3 (click derecho en cultivo maduro)
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `FarmingPerksHandler.java`

##### 🍀 **Lucky Harvest** (Upgradeable: 3 niveles)
- **Costo:** 22 niveles XP
- **Efecto:** +25% drops de cultivos por nivel
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `FarmingPerksHandler.java`

##### ♻️ **Compost** (Nivel único)
- **Costo:** 18 niveles XP
- **Efecto:** Residuos orgánicos se convierten en bonemeal (plantas y comida → bonemeal)
- **Estado:** ⏳ **NO IMPLEMENTADO**

#### **Tier 3**

##### 🌿 **Nature's Blessing** (Nivel único)
- **Costo:** 45 niveles XP
- **Efecto:** Las plantas nunca mueren (no necesitan agua ni luz para crecer)
- **Estado:** ⏳ **NO IMPLEMENTADO**

##### ⚡🌱 **Instant Growth** (Nivel único)
- **Costo:** 50 niveles XP
- **Efecto:** Bonemeal causa crecimiento instantáneo (1 uso = cultivo maduro)
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `FarmingPerksHandler.java`

### 3.8 🟣 Perks COMBO

Los perks combo requieren **múltiples atributos** a niveles específicos.

##### 🔨 **Ore Breaker**
- **Costo:** 60 niveles XP
- **Requisitos:** Strength 40 + Dexterity 40 + Mining 56
- **Efecto:** Puedes minar ores con las manos
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `ComboPerksHandler.java`

##### 😡 **Berserker Mode**
- **Costo:** 60 niveles XP
- **Requisitos:** Strength 56 + Vitality 56
- **Efecto:** Al bajar de 30% HP: +50% daño y +30% velocidad de ataque
  - Duración: 10 segundos
  - Cooldown: 5 minutos
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `ComboPerksHandler.java`

##### 🎯💀 **Deadly Precision**
- **Costo:** 55 niveles XP
- **Requisitos:** Dexterity 48
- **Efecto:** Flechas completamente cargadas = crítico garantizado + atraviesa 2 enemigos
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `ComboPerksHandler.java`

##### ⚡⛏ **Lightning Miner**
- **Costo:** 50 niveles XP
- **Requisitos:** Mining 48 + Agility 32
- **Efecto:** Cada bloque minado da speed boost
  - Max 5x stacks
  - +10% velocidad por stack
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `ComboPerksHandler.java`

##### 🏰 **Iron Fortress**
- **Costo:** 60 niveles XP
- **Requisitos:** Vitality 56 + Strength 40
- **Efecto:** Cuanto menor HP, mayor resistencia (hasta +40% reducción de daño a 20% HP)
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `ComboPerksHandler.java`

##### 🌿⚔️ **Nature Warrior**
- **Costo:** 45 niveles XP
- **Requisitos:** Farming 40 + Agility 32
- **Efecto:** Estar cerca de plantas (5 bloques) da +0.5 HP/s y +20% velocidad
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `ComboPerksHandler.java`

##### 🗡️ **Assassin's Mark**
- **Costo:** 55 niveles XP
- **Requisitos:** Dexterity 48 + Strength 32
- **Efecto:** Atacar por la espalda = +100% daño (180° arco trasero)
- **Estado:** ✅ **IMPLEMENTADO**
- **Archivo:** `ComboPerksHandler.java`

##### 🔧 **Master Craftsman**
- **Costo:** 40 niveles XP
- **Requisitos:** Mining 32 + Farming 32 + Dexterity 32
- **Efecto:** Craftear no consume durabilidad de herramientas + 25% velocidad de crafteo
- **Estado:** ⏳ **NO IMPLEMENTADO**

---

## 4. Sistema de Progresión y XP

### 4.1 Fórmula de XP por Nivel

El sistema utiliza la **misma fórmula que Minecraft** para calcular XP necesaria:

```java
// Nivel 1-16:
XP = nivel² + 6 × nivel

// Nivel 17-31:
XP = 2.5 × nivel² - 40.5 × nivel + 360

// Nivel 32-64:
XP = 4.5 × nivel² - 162.5 × nivel + 2220
```

### 4.2 Tabla de XP Requerida por Nivel

| Nivel | XP Necesaria | XP Acumulada | Dificultad |
|-------|--------------|--------------|------------|
| 1 | 7 | 7 | ⭐ Muy fácil |
| 5 | 55 | 55 | ⭐ Fácil |
| 10 | 160 | 550 | ⭐⭐ Normal |
| 16 | 352 | 1,507 | ⭐⭐ Normal |
| 20 | 550 | 2,895 | ⭐⭐⭐ Medio |
| 30 | 1,395 | 9,315 | ⭐⭐⭐ Medio |
| 32 | 1,507 | 10,829 | ⭐⭐⭐⭐ Difícil |
| 40 (Soft Cap Vitality) | 4,825 | 26,425 | ⭐⭐⭐⭐⭐ Muy difícil |
| 50 | 10,325 | 58,675 | ⭐⭐⭐⭐⭐ Épico |
| 64 | 22,732 | 150,872 | ⭐⭐⭐⭐⭐⭐ Legendario |

### 4.3 Modos de Progresión

Actualmente el sistema implementa:

#### 📊 **Modo Práctica (Practice Mode)**
- **Concepto:** Ganar XP específica para cada stat mediante acciones relacionadas
- **Estado:** ✅ **SISTEMA BASE IMPLEMENTADO**
- **Eventos que otorgan XP:**
  - 🔨 **Mining:** Minar bloques otorga XP de Minería
  - 🌾 **Farming:** Cosechar cultivos otorga XP de Agricultura
  - ⚔️ **Combate:** Dañar entidades otorga XP de Fuerza/Destreza
  - 🏃 **Movimiento:** Acciones de movimiento otorgan XP de Agilidad
  - ❤️ **Supervivencia:** Sobrevivir otorga XP de Vitalidad

**Nota:** Los valores exactos de XP por acción se encuentran en los handlers de eventos individuales.

#### 🎯 **Modo XP de Minecraft**
- **Concepto:** Usar niveles de experiencia de Minecraft para subir stats
- **Estado:** ⚠️ **PARCIALMENTE IMPLEMENTADO** (sistema base existe, necesita más eventos)
- **Costo:** Varía según el stat y el nivel actual

---

## 5. Mecánicas de Combate

### 5.1 Sistema de Weapon Scaling

**Archivo:** `WeaponScalingHandler.java`

El sistema de weapon scaling permite que las armas escalen con los atributos del jugador.

#### 📊 Escalado por Tipo de Arma

**Armas Cuerpo a Cuerpo:**
- Escalan con **STRENGTH**
- Bonus por tier de material:
  - Madera/Oro: Bajo escalado
  - Piedra/Hierro: Escalado medio
  - Diamante/Netherite: Alto escalado

**Ejemplo (Espada de Diamante):**
```
Daño Base: 7
Escalado Strength: (nivel_strength / 64) × 30
A nivel 32 Strength: 7 + 15 = 22 DMG
A nivel 64 Strength: 7 + 30 = 37 DMG
```

**Armas de Proyectiles:**
- Escalan con **DEXTERITY**
- Bonus de alcance y precisión

### 5.2 Sistema de Críticos

**Archivos:** `DexterityPerksHandler.java`, `StrengthPerksHandler.java`

- **Probabilidad base:** Calculada desde Dexterity (ver Atributos Derivados)
- **Daño crítico:** Base + bonus de Dexterity
- **Crítico melee:** Power Strike + Critical Strike perks
- **Crítico proyectiles:** Sniper + Deadly Precision perks

### 5.3 Resistencia y Defensa

**Archivo:** `StatBonusHandler.java`

- **Armadura base:** Del equipo equipado
- **Iron Skin:** +3 armadura por nivel (max +15)
- **Resistencia al knockback:** Desde Strength (ver Atributos Derivados)

---

## 6. Estado de Implementación

### 6.1 Resumen General

| Sistema | Estado | Completitud |
|---------|--------|-------------|
| **Atributos Principales** | ✅ Completo | 100% |
| **Atributos Derivados** | ✅ Completo | 100% |
| **Sistema de Perks (Total: 56)** | 🟡 Parcial | ~75% |
| **Sistema de Progresión** | ✅ Completo | 100% |
| **Weapon Scaling** | ✅ Completo | 100% |
| **UI/HUD** | ✅ Completo | 100% |

### 6.2 Perks por Estado de Implementación

#### ✅ **IMPLEMENTADOS (42 perks)**

**Vitality (3/4):**
- Iron Skin
- Regeneration
- Phoenix Heart

**Strength (5/8):**
- Power Strike
- Knockback Mastery
- Critical Strike
- Execute
- Titan's Fury

**Dexterity (5/8):**
- Swift Strikes
- Quick Draw
- Eagle Eye
- Piercing Shot
- Sniper

**Mining (6/8):**
- Efficient Mining
- Preservation
- Fortune I
- Unbreakable Tools
- Fortune II
- Excavator

**Agility (6/8):**
- Sprint Master
- Feather Fall
- Leap
- Safe Landing
- Wind Runner
- Double Jump

**Farming (6/8):**
- Green Thumb
- Harvest Master
- Fertility
- Mass Harvest
- Lucky Harvest
- Instant Growth

**Combo (7/8):**
- Ore Breaker
- Berserker Mode
- Deadly Precision
- Lightning Miner
- Iron Fortress
- Nature Warrior
- Assassin's Mark

#### ⏳ **PENDIENTES (14 perks)**

**Vitality:**
- Second Wind

**Strength:**
- Heavy Hitter
- Armor Breaker
- Cleave

**Dexterity:**
- Light Steps
- Parry
- Blade Dance

**Mining:**
- Vein Finder
- Tunnel Vision

**Agility:**
- Wall Jump
- Air Dash

**Farming:**
- Compost
- Nature's Blessing

**Combo:**
- Master Craftsman

### 6.3 Archivos de Implementación

| Categoría | Archivo | Líneas | Estado |
|-----------|---------|--------|--------|
| Core - Stats | `PlayerStats.java` | ~500 | ✅ |
| Core - Perks | `Perk.java`, `PerkService.java` | ~800 | ✅ |
| Core - Cálculos | `DerivedAttributesCalculator.java` | ~354 | ✅ |
| Core - Progresión | `StatsProgressionService.java` | ~141 | ✅ |
| Gameplay - Vitality | `VitalityPerksHandler.java` | ~300 | 🟡 |
| Gameplay - Strength | `StrengthPerksHandler.java` | ~400 | 🟡 |
| Gameplay - Dexterity | `DexterityPerksHandler.java` | ~500 | 🟡 |
| Gameplay - Mining | `MiningPerksHandler.java` | ~600 | 🟡 |
| Gameplay - Agility | `AgilityPerksHandler.java` | ~550 | 🟡 |
| Gameplay - Farming | `FarmingPerksHandler.java` | ~650 | 🟡 |
| Gameplay - Combo | `ComboPerksHandler.java` | ~700 | 🟡 |
| Gameplay - Weapon | `WeaponScalingHandler.java` | ~400 | ✅ |
| Gameplay - Stats Bonus | `StatBonusHandler.java` | ~350 | ✅ |
| UI - Screens | `TabbedStatsScreen.java`, `PerksScreen.java` | ~2,400 | ✅ |
| UI - HUD | `CustomHealthArmorHUD.java` | ~500 | ✅ |

---

## 📝 Notas Finales

### Balanceo General

El sistema está diseñado con una **curva exponencial** que favorece:
- **Early Game:** Progresión rápida pero bonuses pequeños
- **Mid Game:** Bonuses notables, progresión moderada
- **Late Game:** Bonuses épicos, progresión muy lenta

El **Soft Cap** en cada stat marca el punto donde la progresión se vuelve significativamente más difícil, incentivando a los jugadores a diversificar sus stats en lugar de maxear solo una.

### Sistema de Perks

Los perks están organizados en **4 tiers** con dificultad y poder crecientes:
- **Tier 1:** Mejoras básicas y pasivas
- **Tier 2:** Habilidades activas y mejoras significativas
- **Tier 3:** Habilidades poderosas y game-changers
- **Combo:** Requieren inversión en múltiples stats, ofrecen efectos únicos

### Próximos Pasos

Para completar el sistema, se necesita implementar:
1. Los 14 perks pendientes
2. Eventos de XP más detallados para cada stat
3. Balanceo final de valores numéricos
4. Sistema de práctica más robusto

---

**Documentación generada:** 2025-12-02
**Versión del mod:** 1.21.1
**Total de perks:** 56 (42 implementados, 14 pendientes)
