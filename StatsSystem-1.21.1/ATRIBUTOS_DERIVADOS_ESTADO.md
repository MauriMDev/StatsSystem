# Estado de Atributos Derivados

## ✅ Atributos Implementados y Funcionando

### VITALITY

#### 1. Vida Máxima (MAX_HEALTH) ✅
- **Fórmula:** `(nivel/64)^1.8 × 80 HP`
- **Rango:**
  - Early (1-20): +2-8 HP
  - Mid (21-45): +15-40 HP
  - Late (46-64): +50-80 HP (25 corazones totales)
- **Estado:** ✅ **FUNCIONANDO CORRECTAMENTE**
- **Implementación:** Modificador de atributo aplicado al jugador
- **Visible en HUD:** ✅ Sí

#### 2. Regeneración de Vida (HEALTH_REGEN) ✅
- **Fórmula:** Solo desde perks y efectos (no escala con Vitality)
- **Fuentes:**
  - Perk "Regeneration" (Vitality Tier 1): 0.10/0.33/1.00 HP/s
  - Perk "Nature Warrior" (Combo): +0.5 HP/s cerca de plantas
  - Efectos de poción
- **Estado:** ✅ **FUNCIONANDO CORRECTAMENTE**
- **Implementación:** Sistema de perks activos
- **Visible en HUD:** ✅ Sí

#### 3. Armadura Total (ARMOR) ⚠️
- **Fórmula:** Armadura de equipment + Iron Skin perk
- **Perk Iron Skin (Vitality Tier 1):**
  - 5 niveles máximo
  - **PROBLEMA:** No se visualiza en el HUD
  - **PROBLEMA:** El bonus de armadura del perk no se aplica correctamente
- **Estado:** ⚠️ **PARCIALMENTE FUNCIONAL**
- **Implementación:**
  - ✅ Armadura de equipment funciona
  - ❌ Iron Skin no aplica bonus de armadura
  - ❌ No se muestra en HUD
- **Visible en HUD:** ⚠️ Solo muestra armadura de equipment

### STRENGTH

#### 4. Daño de Ataque (ATTACK_DAMAGE) ✅
- **Fórmula:** `(nivel/64)^1.8 × 8 DMG` (solo para puño)
- **Weapon Scaling:** Armas escalan con STR + DEX
- **Rango:**
  - Puño nivel 64: 1 base + 8 bonus = 9 daño
  - Espada Netherite nivel 64: 8 base + 18 scaling = 26 daño
- **Estado:** ✅ **FUNCIONANDO CORRECTAMENTE**
- **Implementación:**
  - ✅ Weapon scaling en `LivingIncomingDamageEvent`
  - ✅ Respeta reducción de armadura
  - ✅ Multiplier balanceado (1.0) para PvP
- **Visible en HUD:** ✅ Sí (muestra daño con arma equipada)

#### 5. Resistencia al Knockback (KNOCKBACK_RESISTANCE) ✅
- **Fórmula:** `nivel × 1.0%`
- **Rango:**
  - Nivel 32: +32%
  - Nivel 64: +64%
- **Estado:** ✅ **FUNCIONANDO CORRECTAMENTE**
- **Implementación:** Modificador de atributo
- **Visible en HUD:** ✅ Sí

### DEXTERITY

#### 6. Probabilidad de Crítico (CRIT_CHANCE) ✅
- **Fórmula:** `nivel × 0.5%` + bonus de arma
- **Bonus de armas:**
  - Espada Netherite: +15%
  - Espada Oro: +18%
  - Ballesta: +12%
- **Rango:**
  - Nivel 32: 16% + weapon bonus
  - Nivel 64: 32% + weapon bonus
  - Ejemplo: Espada Netherite lvl 64 = 47%
- **Estado:** ✅ **FUNCIONANDO CORRECTAMENTE**
- **Implementación:** Sistema de críticos en eventos de daño
- **Visible en HUD:** ✅ Sí

#### 7. Daño Crítico (CRIT_DAMAGE) ✅
- **Fórmula:** `nivel × 1.0%`
- **Rango:**
  - Nivel 32: +32%
  - Nivel 64: +64%
- **Estado:** ✅ **FUNCIONANDO CORRECTAMENTE**
- **Implementación:** Multiplicador en evento de crítico
- **Visible en HUD:** ✅ Sí

#### 8. Alcance de Proyectiles (PROJECTILE_RANGE) ⚠️
- **Fórmula:** `nivel × 0.5%`
- **Rango:**
  - Nivel 32: +16%
  - Nivel 64: +32%
- **Estado:** ⚠️ **IMPLEMENTADO PERO NO TESTEADO**
- **Implementación:** Fórmula existe pero necesita validación
- **Visible en HUD:** ✅ Sí

### MINING

#### 9. Velocidad de Minado (MINING_SPEED) ✅
- **Fórmula:** `floor(nivel / 3) × 1.0%`
- **Rango:**
  - Nivel 30: +10%
  - Nivel 64: +21%
- **Estado:** ✅ **FUNCIONANDO CORRECTAMENTE**
- **Implementación:** Modificador de atributo `BLOCK_BREAK_SPEED`
- **Visible en HUD:** ✅ Sí

### AGILITY

#### 10. Resistencia a Caída (FALL_RESISTANCE) ⚠️
- **Fórmula:** `nivel × 0.5%`
- **Rango:**
  - Nivel 32: +16%
  - Nivel 64: +32%
- **Estado:** ⚠️ **IMPLEMENTADO PERO NO TESTEADO**
- **Implementación:** Fórmula existe pero necesita validación
- **Visible en HUD:** ✅ Sí

#### 11. Reducción de Hambre (HUNGER_REDUCTION) ⚠️
- **Fórmula:** `nivel × 0.4%`
- **Rango:**
  - Nivel 32: +12.8%
  - Nivel 64: +25.6%
- **Estado:** ⚠️ **IMPLEMENTADO PERO NO TESTEADO**
- **Implementación:** Fórmula existe pero necesita validación
- **Visible en HUD:** ✅ Sí

#### 12. Capacidad Máxima de Energía (MAX_ENERGY) ⚠️
- **Fórmula:** `floor(nivel / 8) × 2 puntos`
- **Rango:**
  - Nivel 32: +8 puntos (4 muslitos)
  - Nivel 64: +16 puntos (8 muslitos)
- **Estado:** ⚠️ **IMPLEMENTADO PERO NO TESTEADO**
- **Implementación:** Fórmula existe pero necesita validación
- **Visible en HUD:** ✅ Sí

### FARMING

#### 13. Drops Extra de Cultivos (CROP_DROPS) ⚠️
- **Fórmula:** `nivel × 0.4%`
- **Rango:**
  - Nivel 32: +12.8%
  - Nivel 64: +25.6%
- **Estado:** ⚠️ **IMPLEMENTADO PERO NO TESTEADO**
- **Implementación:** Fórmula existe pero necesita validación
- **Visible en HUD:** ✅ Sí

---

## 📊 Resumen del Estado

### ✅ Totalmente Funcionales (6/13)
1. Vida Máxima
2. Regeneración de Vida
3. Daño de Ataque (con Weapon Scaling)
4. Resistencia al Knockback
5. Probabilidad de Crítico
6. Daño Crítico
7. Velocidad de Minado

### ⚠️ Necesitan Testing/Corrección (6/13)
1. **Armadura Total** - Iron Skin no funciona ni se muestra en HUD ⚠️ **PRIORIDAD ALTA**
2. Alcance de Proyectiles
3. Resistencia a Caída
4. Reducción de Hambre
5. Capacidad Máxima de Energía
6. Drops Extra de Cultivos

---

## 🛠️ Tareas Pendientes

### Prioridad Alta
1. ❌ **Arreglar Iron Skin perk**
   - Hacer que aplique el bonus de armadura
   - Mostrar en el HUD la armadura total (equipment + perk)
   - Asegurar que el bonus es significativo (5 niveles)

### Prioridad Media
2. ⚠️ **Validar Alcance de Proyectiles**
   - Testear que funcione con arcos
   - Testear que funcione con ballestas
   - Verificar que el aumento sea visible

3. ⚠️ **Validar Resistencia a Caída**
   - Testear reducción de daño por caída
   - Verificar que el porcentaje sea correcto

4. ⚠️ **Validar Reducción de Hambre**
   - Testear que reduce el consumo de hambre
   - Verificar que afecta sprinting, saltos, etc.

5. ⚠️ **Validar Capacidad Máxima de Energía**
   - Testear que añade muslitos extra
   - Verificar que la barra de hambre se expande

### Prioridad Baja
6. ⚠️ **Validar Drops Extra de Cultivos**
   - Testear con wheat, carrots, potatoes
   - Verificar que el % de drop extra funciona

---

## 🎯 Sistema de Weapon Scaling

### Estado Actual: ✅ Funcionando y Balanceado

**Cambio Reciente:** `lateGameMultiplier` reducido de `5.0` a `1.0` para balance de PvP

**Resultados:**
- Espada Netherite nivel 64: ~26 daño base
- Crítico nivel 64: ~43 daño
- Vs armadura full netherite prot 4: ~8.6 daño crítico

**Balance PvP:**
- Tiempo para matar: ~6 críticos o ~12-15 golpes normales
- Combat duration: ~20-30 segundos
- ✅ No es oneshot
- ✅ Hay contraplay
- ✅ Estratégico y balanceado

### Grades de Scaling
- **S Grade:** 1.50x multiplier
- **A Grade:** 1.25x multiplier
- **B Grade:** 1.00x multiplier
- **C Grade:** 0.75x multiplier
- **D Grade:** 0.50x multiplier
- **E Grade:** 0.25x multiplier

### Ejemplos
- **Espada:** STR (A) + DEX (B)
- **Hacha:** STR (B)
- **Arco:** DEX (S)
- **Tridente:** STR (C) + DEX (B)

---

## 🐛 Problemas Corregidos Recientemente

### ✅ Daño ignoraba armadura
- **Problema:** Weapon scaling se aplicaba en `LivingDamageEvent.Pre` (después de reducción de armadura)
- **Solución:** Cambió a `LivingIncomingDamageEvent` (antes de reducción de armadura)
- **Estado:** ✅ CORREGIDO

### ✅ Daño del HUD/Tooltip no coincidía con daño real
- **Problema:** HUD leía 3.0 de base en lugar de 4.0 (faltaba sumar +1 del puño base)
- **Solución:** Suma +1.0 al daño base del item
- **Estado:** ✅ CORREGIDO

### ✅ Weapon scaling demasiado alto
- **Problema:** Multiplicador de 5.0 causaba ~100 de daño con espada netherite
- **Solución:** Reducido a 1.0 para balance PvP
- **Estado:** ✅ CORREGIDO

---

## 📝 Notas del Desarrollador

### Filosofía de Balance
- **Early game:** Stats dan bonuses pequeños pero notables
- **Mid game:** Stats empiezan a brillar, build diversity emerge
- **Late game:** Stats son poderosos pero no overwhelming

### Weapon Scaling vs Stats Puros
- **Puño:** Viable early game pero inferior a cualquier arma
- **Armas:** Siempre superiores, scaling recompensa inversión
- **Balance:** x2-3 daño base a nivel 64 (no x10-12 como antes)

### PvP Balance
- Jugador con Vitality 64 tiene 50 HP (25 corazones)
- Jugador con armadura + Iron Skin resistirá ~91-92% del daño
- Combates duran suficiente para ser estratégicos
- Perks como Dodge, Parry, Iron Fortress añaden profundidad

---

**Última actualización:** 2025-11-21
**Versión:** 1.0.0
**Estado general:** 🟡 Funcional pero necesita trabajo en atributos secundarios
