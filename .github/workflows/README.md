# GitHub Actions Workflows

Este directorio contiene los workflows de CI/CD automatizados del proyecto.

## 📋 Workflows Disponibles

### 🧪 CI/CD Core

| Workflow | Trigger | Descripción |
|----------|---------|-------------|
| **ci.yml** | PR a develop/main, push a develop/main | Ejecuta tests con Maven y Java 21 |
| **build-and-push.yml** | Push a main/develop | Build imagen Docker y push a ghcr.io |
| **deploy.yml** | Tras build exitoso en main | Deploy automático a VPS vía SSH |
| **release.yml** | Push a main | Genera releases y CHANGELOG con semantic-release |

### 🔀 Automatización de PRs

| Workflow | Trigger | Descripción |
|----------|---------|-------------|
| **auto-merge-to-develop.yml** | PR review aprobado | Auto-merge de feature/bugfix/refactor → develop |
| **auto-merge-to-main.yml** | PR review aprobado | Auto-merge de develop → main |
| **cleanup-branches.yml** | PR cerrado (merged) | Borra automáticamente branches mergeadas |

### ✅ Validación de PRs

| Workflow | Trigger | Descripción |
|----------|---------|-------------|
| **conventional-commits.yml** | PR opened/edited | Valida título de PR con Conventional Commits |
| **pr-size.yml** | PR opened/sync | Valida tamaño de PR (<500 líneas warning, >1000 bloqueado) |
| **pr-labels.yml** | PR opened/sync | Añade labels automáticos según archivos modificados |

## 🚀 Flujo Completo

```
feature/nueva-funcionalidad
         ↓ (PR + tests + approval)
      develop
         ↓ (auto-merge cuando aprobado)
       main
         ↓ (triggers)
    ┌────┴────┐
    ↓         ↓
  Build    Release
    ↓         ↓
  Deploy   Changelog
```

## ⚙️ Configuración Requerida

Para que los workflows funcionen correctamente:

1. **Branch Protection Rules** (develop + main)
   - Require PR before merge
   - Require 1 approval
   - Require status checks: `build-and-test`, `Check PR Title`

2. **Workflow Permissions** (Settings → Actions → General)
   - ✅ Read and write permissions
   - ✅ Allow GitHub Actions to create and approve PRs

3. **Secrets** (para deploy)
   - `SSH_HOST`, `SSH_USER`, `SSH_PRIVATE_KEY`
   - `SSH_PORT` (opcional, default: 22)
   - `SSH_APP_PATH` (ruta de la app en VPS)

Ver documentación completa: [../.github/SETUP_CICD.md](../SETUP_CICD.md)

## 📊 Monitoreo

- **Ver ejecuciones:** Pestaña "Actions" en GitHub
- **Ver releases:** Pestaña "Releases" en GitHub
- **Ver imágenes:** Pestaña "Packages" en GitHub
- **CLI:** `gh run list`, `gh run view [id]`

## 🛠️ Desarrollo Local

Para verificar workflows antes de push:

```bash
# PowerShell
.\verify-cicd.ps1

# Bash
./verify-cicd.sh
```

## 📚 Referencias

- [Conventional Commits](https://www.conventionalcommits.org/)
- [Semantic Release](https://semantic-release.gitbook.io/)
- [GitHub Actions Docs](https://docs.github.com/en/actions)
