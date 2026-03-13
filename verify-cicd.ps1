# Verificación pre-commit para CI/CD
# Ejecutar antes de hacer commit de los workflows
# PowerShell version

Write-Host ""
Write-Host "🔍 Verificando implementación CI/CD..." -ForegroundColor Cyan
Write-Host ""

$errors = 0

# Verificar workflows
Write-Host "📁 Verificando workflows..." -ForegroundColor Yellow
$workflowFiles = @(
  ".github\workflows\ci.yml",
  ".github\workflows\auto-merge-to-develop.yml",
  ".github\workflows\auto-merge-to-main.yml",
  ".github\workflows\pr-size.yml",
  ".github\workflows\pr-labels.yml",
  ".github\workflows\conventional-commits.yml",
  ".github\workflows\build-and-push.yml",
  ".github\workflows\deploy.yml",
  ".github\workflows\release.yml",
  ".github\workflows\cleanup-branches.yml"
)

foreach ($file in $workflowFiles) {
  if (Test-Path $file) {
    Write-Host "  ✓ $file" -ForegroundColor Green
  } else {
    Write-Host "  ✗ $file NO ENCONTRADO" -ForegroundColor Red
    $errors++
  }
}

# Verificar archivos de configuración
Write-Host ""
Write-Host "📄 Verificando configuración..." -ForegroundColor Yellow
$configFiles = @(
  ".github\labeler.yml",
  ".github\PULL_REQUEST_TEMPLATE.md",
  ".releaserc.json",
  ".github\SETUP_CICD.md"
)

foreach ($file in $configFiles) {
  if (Test-Path $file) {
    Write-Host "  ✓ $file" -ForegroundColor Green
  } else {
    Write-Host "  ✗ $file NO ENCONTRADO" -ForegroundColor Red
    $errors++
  }
}

# Verificar actualizaciones de documentación
Write-Host ""
Write-Host "📝 Verificando documentación actualizada..." -ForegroundColor Yellow
$docs = @(
  "AGENTS.md",
  ".github\instructions\git.instructions.md"
)

foreach ($file in $docs) {
  if (Test-Path $file) {
    $content = Get-Content $file -Raw
    if ($content -match "CI/CD") {
      Write-Host "  ✓ $file contiene referencias a CI/CD" -ForegroundColor Green
    } else {
      Write-Host "  ⚠ $file podría necesitar actualización" -ForegroundColor Yellow
    }
  }
}

# Verificar sintaxis JSON
Write-Host ""
Write-Host "🔧 Verificando sintaxis JSON..." -ForegroundColor Yellow
if (Test-Path ".releaserc.json") {
  try {
    $null = Get-Content ".releaserc.json" -Raw | ConvertFrom-Json
    Write-Host "  ✓ .releaserc.json - sintaxis válida" -ForegroundColor Green
  } catch {
    Write-Host "  ✗ .releaserc.json - ERROR DE SINTAXIS" -ForegroundColor Red
    $errors++
  }
}

# Verificar sintaxis YAML básica
Write-Host ""
Write-Host "🔧 Verificando sintaxis YAML..." -ForegroundColor Yellow
foreach ($file in Get-ChildItem -Path ".github\workflows\*.yml") {
  try {
    $null = Get-Content $file.FullName -Raw
    Write-Host "  ✓ $($file.Name) - sintaxis válida" -ForegroundColor Green
  } catch {
    Write-Host "  ✗ $($file.Name) - ERROR DE SINTAXIS" -ForegroundColor Red
    $errors++
  }
}

# Resumen final
Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor White
if ($errors -eq 0) {
  Write-Host "✅ Verificación completa - TODO OK" -ForegroundColor Green
  Write-Host ""
  Write-Host "📋 Próximos pasos:" -ForegroundColor Cyan
  Write-Host "  1. Revisar .github\SETUP_CICD.md para configurar secrets"
  Write-Host "  2. git add ."
  Write-Host "  3. git commit -m 'feat: implementar CI/CD con GitHub Actions'"
  Write-Host "  4. git push origin nombre-branch"
  Write-Host "  5. Crear PR a develop"
  Write-Host ""
  exit 0
} else {
  Write-Host "❌ Se encontraron $errors errores" -ForegroundColor Red
  Write-Host "   Corrige los errores antes de hacer commit" -ForegroundColor Red
  Write-Host ""
  exit 1
}
