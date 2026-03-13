#!/bin/bash

# Verificación pre-commit para CI/CD
# Ejecutar antes de hacer commit de los workflows

echo "🔍 Verificando implementación CI/CD..."
echo ""

errors=0

# Verificar workflows
echo "📁 Verificando workflows..."
workflow_files=(
  ".github/workflows/ci.yml"
  ".github/workflows/auto-merge-to-develop.yml"
  ".github/workflows/auto-merge-to-main.yml"
  ".github/workflows/pr-size.yml"
  ".github/workflows/pr-labels.yml"
  ".github/workflows/conventional-commits.yml"
  ".github/workflows/build-and-push.yml"
  ".github/workflows/deploy.yml"
  ".github/workflows/release.yml"
  ".github/workflows/cleanup-branches.yml"
)

for file in "${workflow_files[@]}"; do
  if [ -f "$file" ]; then
    echo "  ✓ $file"
  else
    echo "  ✗ $file NO ENCONTRADO"
    errors=$((errors + 1))
  fi
done

# Verificar archivos de configuración
echo ""
echo "📄 Verificando configuración..."
config_files=(
  ".github/labeler.yml"
  ".github/PULL_REQUEST_TEMPLATE.md"
  ".releaserc.json"
  ".github/SETUP_CICD.md"
)

for file in "${config_files[@]}"; do
  if [ -f "$file" ]; then
    echo "  ✓ $file"
  else
    echo "  ✗ $file NO ENCONTRADO"
    errors=$((errors + 1))
  fi
done

# Verificar actualizaciones de documentación
echo ""
echo "📝 Verificando documentación actualizada..."
docs=(
  "AGENTS.md"
  ".github/instructions/git.instructions.md"
)

for file in "${docs[@]}"; do
  if grep -q "CI/CD" "$file" 2>/dev/null; then
    echo "  ✓ $file contiene referencias a CI/CD"
  else
    echo "  ⚠ $file podría necesitar actualización"
  fi
done

# Verificar sintaxis YAML básica (si yq está instalado)
echo ""
echo "🔧 Verificando sintaxis YAML..."
if command -v yq &> /dev/null || command -v yamllint &> /dev/null; then
  for file in .github/workflows/*.yml; do
    if command -v yq &> /dev/null; then
      if yq eval '.' "$file" > /dev/null 2>&1; then
        echo "  ✓ $file - sintaxis válida"
      else
        echo "  ✗ $file - ERROR DE SINTAXIS"
        errors=$((errors + 1))
      fi
    fi
  done
else
  echo "  ⚠ yq o yamllint no instalado, saltando validación de sintaxis"
  echo "    Instalar con: brew install yq  (o)  pip install yamllint"
fi

# Verificar sintaxis JSON
echo ""
echo "🔧 Verificando sintaxis JSON..."
if [ -f ".releaserc.json" ]; then
  if python -m json.tool .releaserc.json > /dev/null 2>&1; then
    echo "  ✓ .releaserc.json - sintaxis válida"
  else
    echo "  ✗ .releaserc.json - ERROR DE SINTAXIS"
    errors=$((errors + 1))
  fi
fi

# Resumen final
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if [ $errors -eq 0 ]; then
  echo "✅ Verificación completa - TODO OK"
  echo ""
  echo "📋 Próximos pasos:"
  echo "  1. Revisar .github/SETUP_CICD.md para configurar secrets"
  echo "  2. git add ."
  echo "  3. git commit -m 'feat: implementar CI/CD con GitHub Actions'"
  echo "  4. git push origin <branch>"
  echo "  5. Crear PR a develop"
  exit 0
else
  echo "❌ Se encontraron $errors errores"
  echo "   Corrige los errores antes de hacer commit"
  exit 1
fi
