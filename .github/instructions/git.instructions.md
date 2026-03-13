---
description: Describe when these instructions should be loaded
# applyTo: 'Describe when these instructions should be loaded' # when provided, instructions will automatically be added to the request context when the pattern matches an attached file
---
Provide project context and coding guidelines that AI should follow when generating code, answering questions, or reviewing changes.

Branch Strategy (Automated Flow)
 - **develop**: rama principal de trabajo donde se integran todas las features y fixes
 - **main**: rama de producción, siempre estable, con releases automáticas
 - **feature/**, **bugfix/**, **refactor/**, **hotfix/**: ramas para desarrollo
 - Flujo automatizado en cascada:
   1. Crea branch desde develop: `feature/descripcion-corta`, `bugfix/descripcion-corta`, etc.
   2. PR a develop → auto-merge cuando tests pasan + 1 aprobación
   3. develop → main se mergea automáticamente cuando hay cambios aprobados
   4. main dispara release automática + deploy a producción
 - Nomenclatura obligatoria: `feature/`, `bugfix/`, `refactor/`, `hotfix/` + descripción corta en kebab-case
 - Las branches de feature/bugfix se borran automáticamente después del merge
 - Mantén tu branch actualizada: `git pull origin develop` regularmente

Commit Messages (Conventional Commits)
  - **OBLIGATORIO**: Seguir formato [Conventional Commits](https://www.conventionalcommits.org/)
  - Formato: `tipo(scope opcional): descripción`
  - Tipos válidos:
    * `feat`: Nueva funcionalidad
    * `fix`: Corrección de bug
    * `refactor`: Refactorización sin cambios funcionales
    * `perf`: Mejora de rendimiento
    * `test`: Añadir o modificar tests
    * `docs`: Cambios en documentación
    * `style`: Cambios de formato (no afectan la lógica)
    * `chore`: Build, CI/CD, o tareas de mantenimiento
  - Ejemplos válidos:
    * `feat(catalog): añadir filtro de productos por categoría`
    * `fix: resolver error en cálculo de totales`
    * `refactor(auth): simplificar validación de tokens`
    * `test(order): añadir tests para PaymentService`
  - Siempre en español, tiempo presente
  - Incluir referencias a issues cuando aplique: `Fixes #123`, `Related to #456`
  - Para cambios BREAKING: añadir `!` después del tipo o `BREAKING CHANGE:` en el body
    * Ejemplo: `feat!: cambiar estructura de response del endpoint products`

Pull Requests (Automated)
  - **Título del PR**: DEBE seguir Conventional Commits (validado automáticamente)
  - Crear PR a develop para features/bugfix/refactor/hotfix
  - El PR template se autocompleta con checklist
  - Requisitos para auto-merge:
    * ✅ Todos los tests pasan (CI check)
    * ✅ Al menos 1 aprobación manual del propietario del repositorio
    * ✅ Título sigue Conventional Commits
    * ✅ PR no es draft
    * ✅ PR tiene menos de 1000 líneas (límite estricto)
    * ✅ No hay conflictos
  - Auto-labels aplicados según archivos modificados
  - Descripción clara en español de los cambios
  - Incluir screenshots/GIFs si afecta UI
  - **IMPORTANTE PARA AGENTES IA**: 
    * ✋ **NUNCA mergear PRs sin aprobación manual explícita del usuario**
    * Solo crear las PRs y reportar su estado
    * Esperar a que el usuario revise y apruebe manualmente cada PR
    * Solo mergear si el usuario lo solicita explícitamente
  - El sistema mergea automáticamente cuando se cumplen los requisitos (después de aprobación)
  - **NUNCA** crear PR directo a main (solo develop → main automático)

Best Practices
  - ensure code is well-tested and follows the project's coding standards
  - avoid committing large files or sensitive information (e.g., passwords, API keys)
  - use .gitignore to exclude files that should not be tracked
  - regularly clean up branches that have been merged to keep the repository organized (automático)
  - communicate with the team about ongoing work and potential conflicts to minimize merge issues
  - always review your changes before committing and ensure that your commit messages accurately reflect the changes made. This helps maintain a clear project history and facilitates collaboration among team members.
  - follow the project's coding style and conventions (e.g., naming conventions, indentation)
  - write clear and maintainable code, with comments where necessary to explain complex logic
  - **ANTES de abrir PR**: ejecutar `mvn test` localmente para asegurar que pasan todos los tests
  - **PR Size**: mantener PRs pequeños (<500 líneas) para facilitar revisión
  - **Conventional Commits**: verificar título del PR antes de abrir (ej: `feat: descripción`)

CI/CD Pipeline (Automated)
  - **CI en cada PR**: tests automáticos ejecutados en GitHub Actions
  - **Auto-merge**: PRs aprobadas se mergean automáticamente
  - **Cascading flow**: feature → develop → main automáticamente
  - **Releases**: semantic-release genera versiones automáticamente en main
    * `feat:` → minor version (1.2.0 → 1.3.0)
    * `fix:` → patch version (1.2.0 → 1.2.1)
    * `feat!:` o `BREAKING CHANGE:` → major version (1.2.0 → 2.0.0)
  - **Deploy**: automático a VPS cuando main recibe cambios
  - **Docker**: imagen publicada en GitHub Container Registry (ghcr.io)
  - **CHANGELOG.md**: generado automáticamente por semantic-release
  - **Branch cleanup**: branches mergeadas se borran automáticamente

IMPORTANT: Always review your changes before committing and ensure that your commit messages accurately reflect the changes made. This helps maintain a clear project history and facilitates collaboration among team members.

Always do Pull Request to the develop branch for feature work. The develop → main merge is automatic when tests pass and PR is approved.
Language: The code always in Java, Spring Boot, and related technologies in English. Commits and PRs in Spanish.
