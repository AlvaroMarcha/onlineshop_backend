# Configuración de CI/CD para GitHub Actions

Este documento describe cómo configurar los secrets y branch protection rules necesarios para que los workflows de CI/CD funcionen correctamente.

## 📋 Requisitos Previos

1. Repositorio en GitHub con permisos de administrador
2. Acceso SSH al VPS de producción
3. Cuenta en GitHub Container Registry (automática con GitHub)

---

## 🔐 Configuración de Secrets

Ve a **Settings → Secrets and variables → Actions** en tu repositorio de GitHub y añade los siguientes secrets:

### Secrets para Deploy SSH (Obligatorios)

| Secret | Descripción | Ejemplo |
|--------|-------------|---------|
| `SSH_HOST` | IP o dominio del VPS | `123.45.67.89` o `vps.example.com` |
| `SSH_USER` | Usuario SSH para conectar al VPS | `root` o `deployuser` |
| `SSH_PRIVATE_KEY` | Clave privada SSH (contenido completo) | `-----BEGIN OPENSSH PRIVATE KEY-----\n...` |
| `SSH_PORT` | Puerto SSH (opcional, default 22) | `22` |
| `SSH_APP_PATH` | Ruta absoluta de la aplicación en el VPS | `/opt/onlineshop` |

### Cómo obtener la clave SSH privada

```bash
# En tu máquina local, genera un par de claves (si no lo tienes)
ssh-keygen -t ed25519 -C "github-actions-deploy"

# Copia la clave pública al VPS
ssh-copy-id -i ~/.ssh/id_ed25519.pub user@vps-host

# Copia el contenido de la clave PRIVADA para el secret
cat ~/.ssh/id_ed25519
# Copia TODO el output (incluyendo -----BEGIN y -----END)
```

### Variables de Configuración Adicionales

Los secrets de aplicación (JWT_SECRET, STRIPE_*, GOOGLE_*, etc.) deben estar configurados en el VPS mediante archivo `.env` o variables de entorno en `docker-compose.yml`.

---

## 🛡️ Configuración de Branch Protection

### Para la rama `develop`

Ve a **Settings → Branches → Add branch protection rule**

**Branch name pattern:** `develop`

Configuración recomendada:
- ✅ **Require a pull request before merging**
  - Required approvals: **1**
  - Dismiss stale pull request approvals when new commits are pushed: ✅
  - Require review from Code Owners: ❌ (opcional)
- ✅ **Require status checks to pass before merging**
  - ❌ Require branches to be up to date before merging (permite auto-merge más rápido)
  - Required checks:
    - `build-and-test` (del workflow ci.yml)
    - `Check PR Title` (del workflow conventional-commits.yml)
- ✅ **Require conversation resolution before merging** (opcional)
- ✅ **Do not allow bypassing the above settings**
- ❌ Allow force pushes
- ❌ Allow deletions

### Para la rama `main`

**Branch name pattern:** `main`

Configuración recomendada:
- ✅ **Require a pull request before merging**
  - Required approvals: **1**
  - Restrict who can dismiss pull request reviews: ✅ (opcional, solo admins)
- ✅ **Require status checks to pass before merging**
  - ❌ Require branches to be up to date
  - Required checks:
    - `build-and-test`
    - `Check PR Title`
- ✅ **Require linear history** (opcional, fuerza rebase/squash)
- ✅ **Do not allow bypassing the above settings**
- ❌ Allow force pushes
- ❌ Allow deletions

---

## ⚙️ Permisos de Workflows

Ve a **Settings → Actions → General**

Configuración requerida:

**Workflow permissions:**
- ✅ **Read and write permissions**
- ✅ **Allow GitHub Actions to create and approve pull requests**

**Fork pull request workflows:**
- Según preferencia (no crítico para repo privado)

---

## 📦 GitHub Container Registry (GHCR)

No requiere configuración adicional. Los workflows usan `GITHUB_TOKEN` automáticamente.

Para verificar imágenes publicadas:
1. Ve a la página principal del repositorio
2. Click en **Packages** (lateral derecho)
3. Deberías ver `onlineshop_backend` después del primer push a `main` o `develop`

---

## 🧪 Verificación de la Configuración

### Test 1: Verificar workflows sintácticamente

```bash
# En tu máquina local
cd onlineshop_backend
ls .github/workflows/*.yml
# Deberías ver 10 archivos
```

### Test 2: Verificar secrets configurados

```bash
# Usando GitHub CLI (gh)
gh secret list
# Deberías ver: SSH_HOST, SSH_USER, SSH_PRIVATE_KEY, SSH_PORT, SSH_APP_PATH
```

### Test 3: Crear un PR de prueba

```bash
# Crea una rama de prueba
git checkout develop
git pull origin develop
git checkout -b feature/test-ci-cd

# Haz un cambio mínimo
echo "# CI/CD Test" >> README.md
git add README.md
git commit -m "feat: test CI/CD pipeline"
git push origin feature/test-ci-cd

# Crea PR a develop desde GitHub UI
# Verifica que se ejecutan los workflows en la pestaña Actions
```

---

## 🚀 Flujo Completo Esperado

### Escenario: Nuevo Feature

1. **Developer crea branch**
   ```bash
   git checkout -b feature/add-new-endpoint
   # ... código ...
   git commit -m "feat(api): añadir endpoint para búsqueda avanzada"
   git push origin feature/add-new-endpoint
   ```

2. **Developer crea PR a develop**
   - GitHub Actions ejecuta workflow `ci.yml` automáticamente
   - Se aplican labels automáticos según archivos modificados
   - Se verifica tamaño del PR
   - Se valida título con Conventional Commits

3. **Reviewer aprueba el PR**
   - Workflow `auto-merge-to-develop.yml` detecta la aprobación
   - Verifica que todos los checks pasaron
   - Activa auto-merge
   - PR se mergea automáticamente a `develop`
   - Branch `feature/add-new-endpoint` se borra automáticamente

4. **Auto-merge a main**
   - Se crea PR automático de `develop` → `main` (manual o automático según configuración)
   - Reviewer aprueba
   - Workflow `auto-merge-to-main.yml` mergea a `main`
   - **NO** se borra rama `develop`

5. **Release y Deploy automáticos**
   - Push a `main` dispara `release.yml`
   - `semantic-release` analiza commits desde último release
   - Genera nueva versión (ej: `v1.3.0`)
   - Crea GitHub Release
   - Actualiza `CHANGELOG.md`
   - Push a `main` dispara `build-and-push.yml`
   - Build de imagen Docker
   - Push a `ghcr.io/usuario/onlineshop_backend:latest`
   - `deploy.yml` se dispara al completarse el build
   - SSH al VPS
   - Pull de imagen desde ghcr.io
   - `docker compose up -d`
   - ✅ Deploy completo

---

## 🐛 Troubleshooting

### Problema: Auto-merge no se activa

**Causa posible:**
- Branch protection rules no configuradas correctamente
- Falta aprobación del PR
- Checks requeridos no pasaron

**Solución:**
1. Verifica branch protection en Settings → Branches
2. Asegúrate que el PR tiene al menos 1 aprobación
3. Verifica que todos los checks en verde

### Problema: Deploy falla con "Permission denied"

**Causa:** Clave SSH incorrecta o usuario sin permisos

**Solución:**
1. Verifica que `SSH_PRIVATE_KEY` contiene la clave COMPLETA
2. Verifica que la clave pública está en `~/.ssh/authorized_keys` del VPS
3. Prueba conexión manual: `ssh -i ~/.ssh/id_ed25519 user@host`

### Problema: "Error: Unable to resolve action"

**Causa:** Sintaxis incorrecta en workflow o action no existe

**Solución:**
1. Verifica sintaxis YAML
2. Comprueba que las versiones de actions existen (ej: `@v4` vs `@v3`)

### Problema: Semantic release no genera versión

**Causa:** No hay commits que generen release (solo chore, docs, etc)

**Solución:**
- Al menos un commit debe ser `feat:` o `fix:` desde el último release
- Verifica formato de commits con Conventional Commits

---

## 📊 Monitoring

### Ver estado de workflows

- GitHub UI: **Actions** tab
- GitHub CLI: `gh run list`

### Ver logs de un workflow

```bash
gh run view [run-id] --log
```

### Ver releases generados

```bash
gh release list
```

### Ver imágenes Docker publicadas

```bash
# Usando GitHub CLI
gh api /user/packages?package_type=container

# O visita:
# https://github.com/USUARIO?tab=packages
```

---

## 📚 Referencias

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Semantic Release](https://semantic-release.gitbook.io/)
- [GitHub Container Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)

---

## ✅ Checklist Final

Antes de considerar la configuración completa, verifica:

- [ ] Todos los secrets SSH configurados en GitHub
- [ ] Branch protection rules configurados para `develop` y `main`
- [ ] Workflow permissions habilitados (read + write)
- [ ] Archivo `.env` configurado en el VPS con secrets de aplicación
- [ ] `docker-compose.yml` en el VPS apunta a imagen de ghcr.io
- [ ] Primer PR de prueba ejecutado exitosamente
- [ ] Auto-merge funcionando en PR a develop
- [ ] Release generado en `main` correctamente
- [ ] Deploy a VPS completado sin errores
- [ ] Aplicación accesible después del deploy

**¡Todo listo!** 🎉
