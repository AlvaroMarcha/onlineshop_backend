# 📋 Guía de Variables de Entorno

## 🚀 Configuración Rápida

### Desarrollo Local

1. Copia el template:
   ```bash
   cp .env.example .env
   ```

2. Completa los valores según tu configuración local:
   - **Base de datos**: Docker (localhost:3306) o MySQL local
   - **Rutas**: Windows (`C:/uploads/...`) o Linux (`/home/user/uploads/...`)
   - **CORS**: `http://localhost:*` para desarrollo
   - **Stripe**: Usa claves de test (`sk_test_...`)

3. Asegúrate de cambiar las contraseñas de los usuarios administradores

---

## 🌐 Configuración para Producción (VPS con alanmarcha.com)

### Variables críticas a cambiar:

| Variable | Desarrollo | Producción (VPS) |
|----------|-----------|-----------------|
| `DB_URL` | `jdbc:mysql://localhost:3306/...` | `jdbc:mysql://mysql:3306/onlineshop?useSSL=false` |
| `DB_SHOW_SQL` | `true` | `false` |
| `APP_BASE_URL` | `http://localhost:8080` | `https://alanmarcha.com` |
| `APP_FRONTEND_URL` | `http://localhost:5500` | `https://alanmarcha.com` |
| `IMAGES_STORAGE_PATH` | `C:/uploads/images` | `/uploads/images` |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:*,...` | `https://alanmarcha.com,https://www.alanmarcha.com` |
| `ADMIN_PASSWORD` | `Admin123!` | **Cambia por contraseña segura** |
| `SUPER_ADMIN_PASSWORD` | `SuperAdmin123!` | **Cambia por contraseña segura** |
| `STRIPE_SECRET_KEY` | `sk_test_...` | `sk_live_...` (claves reales) |
| `STRIPE_WEBHOOK_SECRET` | `whsec_test...` | `whsec_live...` (claves reales) |
| `JWT_SECRET` | Cualquiera (dev) | **Generar con** `openssl rand -base64 64` |

### Configuración en VPS:

1. Conecta por SSH a tu servidor
2. Edita el archivo `.env` en el directorio del proyecto:
   ```bash
   cd ~/onlineshop_backend
   nano .env
   ```
3. Actualiza todas las variables de producción
4. Guarda (`Ctrl+O`, `Enter`, `Ctrl+X`)
5. Reinicia servicios:
   ```bash
   docker compose down
   docker compose up -d --build
   ```

### Diferencias VPS vs Desarrollo:

- **Base de datos**: En VPS, `mysql` es el hostname del container (red Docker interna)
- **Rutas**: Linux usa `/uploads/` en lugar de `C:/uploads/`
- **SSL**: URL base debe ser HTTPS (`https://alanmarcha.com`)
- **CORS**: Restringe a tu dominio real, no wildcards en producción

---

## 🔐 Seguridad

### ⚠️ Valores sensibles que DEBES cambiar en producción:

- `jwt.secret` — Genera uno nuevo con: `openssl rand -hex 32`
- `ADMIN_PASSWORD` y `SUPER_ADMIN_PASSWORD`
- `MAIL_PASSWORD` — Usa contraseñas de aplicación de Gmail
- `GOOGLE_CLIENT_SECRET` y `GOOGLE_REFRESH_TOKEN`
- `STRIPE_SECRET_KEY` — Cambia a claves live en producción

### ✅ El archivo `.env` está en `.gitignore`

NO lo subas a Git. Si accidentalmente lo subes:
1. Revierte el commit
2. Cambia TODAS las contraseñas y secrets
3. Regenera claves JWT, API keys, etc.

---

## 📦 Variables por Sección

### Base de Datos
- `MYSQL_*` — Solo para contenedor Docker local
- `DB_URL`, `DB_USER`, `DB_PASS` — Conexión a MySQL
- `DB_DRIVER`, `DB_DIALECT` — Configuración JPA
- `DB_SHOW_SQL` — Logs SQL (true en dev, false en prod)
- `DB_HBM2DDL` — Gestión de esquema (update/validate)

### Servidor
- `APP_BASE_URL` — URL base del backend
- `APP_FRONTEND_URL` — URL del frontend
- `IMAGES_STORAGE_PATH` — Ruta de imágenes
- `INVOICES_STORAGE_PATH` — Ruta de facturas
- `APP_IMAGES_PUBLIC_PATH` — Ruta pública `/images`

### CORS
- `CORS_ALLOWED_ORIGINS` — Lista separada por comas
  - Soporta wildcards: `https://miapp-*.vercel.app`

### JWT
- `jwt.secret` — Clave secreta (mínimo 32 chars)
- `jwt.expiration` — Tiempo en ms (3600000 = 1 hora)

### Usuarios Seed
- `ADMIN_*` — Usuario admin (ROLE_ADMIN)
- `SUPER_ADMIN_*` — Usuario superadmin (ROLE_SUPER_ADMIN)

### Mail (Gmail SMTP)
- `MAIL_HOST`, `MAIL_PORT` — Servidor SMTP
- `MAIL_USERNAME`, `MAIL_PASSWORD` — Credenciales Gmail

### Google OAuth2
- Para autenticación XOAUTH2 con Gmail
- Requiere proyecto en Google Cloud Console
- `GOOGLE_REFRESH_TOKEN` puede expirar cada 7 días en modo test

### Company
- Datos de la empresa para facturas PDF
- Logo: ruta relativa o URL

### Stripe
- `STRIPE_SECRET_KEY` — sk_test/sk_live
- `STRIPE_WEBHOOK_SECRET` — whsec_...
- `STRIPE_CURRENCY` — eur/usd/etc

---

## 🛠️ Comandos Útiles

### Generar JWT secret seguro:
```bash
openssl rand -hex 32
```

### Ver variables cargadas en Spring Boot:
```bash
./mvnw spring-boot:run | grep "DB_URL\|APP_BASE_URL\|CORS"
```

### Validar .env (Linux/Mac):
```bash
set -a
source .env
set +a
echo $DB_URL
```

---

## 📝 Checklist Pre-Producción

- [ ] `DB_URL` apunta a Railway/PlanetScale
- [ ] `DB_SHOW_SQL=false`
- [ ] `APP_BASE_URL` y `APP_FRONTEND_URL` usan HTTPS
- [ ] `CORS_ALLOWED_ORIGINS` incluye dominio de producción
- [ ] Contraseñas de admin cambiadas
- [ ] `jwt.secret` regenerado
- [ ] Stripe usa claves live (`sk_live_...`)
- [ ] Mail configurado con credenciales de producción
- [ ] Rutas de almacenamiento apuntan a `/opt/render/...`
- [ ] `.env` NO está en el repositorio

---

## 🆘 Troubleshooting

### Error: "Cannot load driver class: ${DB_DRIVER}"
→ La variable no se cargó. Render no usa `.env`, configura en el dashboard.

### Error: CORS bloqueado
→ Verifica `CORS_ALLOWED_ORIGINS` incluye la URL exacta del frontend.

### Error: JWT inválido
→ Asegúrate que `jwt.secret` es el mismo en todas las instancias.

### Error: Mail no envía
→ Verifica `MAIL_PASSWORD` sea una "Contraseña de aplicación" de Gmail, no la contraseña normal.

### Error: Stripe webhook falla
→ Sincroniza `STRIPE_WEBHOOK_SECRET` con el valor del dashboard de Stripe.
