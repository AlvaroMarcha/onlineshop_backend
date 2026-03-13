# Configuración de Branch Protection Rules

## 📋 Nombres Exactos de Status Checks

Para configurar las Branch Protection Rules en GitHub, necesitas añadir estos nombres **exactamente como aparecen aquí**:

### ✅ Status Checks Requeridos para `develop`

Añade estos 3 checks (son los críticos):

```
Build and Test
Check PR Title
Check PR Size
```

**Alternativamente**, si GitHub los muestra con el formato completo, usa:

```
CI - Build and Test / Build and Test
Conventional Commits Check / Check PR Title
PR Size Check / Check PR Size
```

### ✅ Status Checks Requeridos para `main`

Los mismos 3 checks que para `develop`:

```
Build and Test
Check PR Title
Check PR Size
```

---

## 🛠️ Pasos para Configurar Branch Protection (Rulesets)

### Opción 1: Branch Protection Rules (Recomendado)

1. Ve a **Settings** → **Branches**
2. Click en **Add branch protection rule**

#### Para la rama `develop`:

**Branch name pattern:** `develop`

**Configuración:**

- ✅ **Require a pull request before merging**
  - Required approvals: `1`
  - ❌ Dismiss stale pull request approvals when new commits are pushed

- ✅ **Require status checks to pass before merging**
  - ✅ Require branches to be up to date before merging: **NO** (importante para auto-merge)
  - **Status checks that are required:**
    - Busca y añade: `Build and Test`
    - Busca y añade: `Check PR Title`
    - Busca y añade: `Check PR Size`

- ✅ **Require conversation resolution before merging** (opcional)

- ✅ **Do not allow bypassing the above settings**

- ❌ **Allow force pushes**: NO

- ❌ **Allow deletions**: NO

**Click en "Create"**

#### Para la rama `main`:

Repite los mismos pasos pero con:
- **Branch name pattern:** `main`
- Mismos 3 status checks
- Misma configuración

---

### Opción 2: Rulesets (Nuevo sistema de GitHub)

Si estás usando Rulesets en lugar de Branch Protection Rules:

1. Ve a **Settings** → **Rules** → **Rulesets**
2. Click en **New ruleset** → **New branch ruleset**

#### Configuración del Ruleset para `develop`:

**Ruleset Name:** `Protect develop branch`

**Enforcement status:** Active

**Target branches:**
- **Include by pattern:** `develop`

**Rules:**

1. **Restrict deletions:** ✅
2. **Require a pull request before merging:** ✅
   - Required approvals: `1`
3. **Require status checks to pass:** ✅
   - **Status checks:**
     - Click en **Add checks**
     - En el campo de búsqueda, escribe: `Build and Test` y selecciónalo
     - Click en **Add checks** de nuevo y añade: `Check PR Title`
     - Click en **Add checks** de nuevo y añade: `Check PR Size`
   - ❌ **Require branches to be up to date before merging**: NO marcar

**Click en "Create"**

Repite para `main` con el mismo proceso.

---

## 🔍 Cómo Encontrar los Nombres de los Checks

Si los nombres anteriores no funcionan, sigue estos pasos:

1. Ve a tu Pull Request: https://github.com/AlvaroMarcha/onlineshop_backend/pull/207
2. Scroll hasta la sección de checks
3. Los nombres que ves ahí son los que debes copiar exactamente
4. Ejemplo: si ves "Build and Test ✓", el nombre es `Build and Test`

**Truco:** Cuando empieces a escribir en el campo de búsqueda de status checks, GitHub te sugerirá automáticamente los checks disponibles.

---

## ⚠️ Problemas Comunes

### "Required status checks cannot be empty"

**Causa:** Intentas guardar sin añadir ningún check.

**Solución:** 
1. Asegúrate de hacer click en **"Add checks"** o buscar los checks en el campo de búsqueda
2. Los checks solo aparecen si se han ejecutado **al menos una vez** en el repositorio
3. Como ya tienes una PR con checks ejecutados, deberían aparecer en la búsqueda

### Los checks no aparecen en la búsqueda

**Causa:** GitHub solo sugiere checks que se han ejecutado previamente.

**Solución:**
1. Ve a tu PR y verifica que los checks se hayan ejecutado
2. Actualiza la página de configuración de Branch Protection
3. Intenta buscar escribiendo solo parte del nombre (ej: "Build")

### Auto-merge no se activa

**Causa:** "Require branches to be up to date" está marcado.

**Solución:** 
- **DESMARCA** "Require branches to be up to date before merging"
- Esta opción fuerza que la PR esté actualizada con la rama base antes del merge
- Con auto-merge, esto causa que el bot no pueda mergear automáticamente

---

## 🎯 Configuración Mínima para que Auto-Merge Funcione

**Lo que DEBES configurar:**
1. ✅ Require pull request (1 approval)
2. ✅ Status checks: `Build and Test`, `Check PR Title`, `Check PR Size`
3. ❌ Require branches to be up to date: **NO** (crítico)

**Lo que NO debes hacer:**
- ❌ NO marcar "Require branches to be up to date before merging"
- ❌ NO añadir checks que no son esenciales (como "Add labels to PR")

---

## 🧪 Verificar Configuración

Una vez configurado, prueba:

```bash
# Ver si las reglas están activas
gh api repos/AlvaroMarcha/onlineshop_backend/branches/develop/protection

# Ver tu PR actual
gh pr view 207 --json mergeable,mergeStateStatus
```

Si todo está bien configurado, al aprobar la PR debería aparecer el botón de "Enable auto-merge" o activarse automáticamente.

---

## 📞 Comando de Ayuda

Si sigues teniendo problemas, ejecuta:

```bash
# Ver todos los checks disponibles en el repo
gh api repos/AlvaroMarcha/onlineshop_backend/commits/$(git rev-parse HEAD)/check-runs --jq '.check_runs[] | .name'
```

Esto te mostrará los nombres exactos de todos los checks ejecutados.

---

**Última actualización:** 13/03/2026 01:00 CET
