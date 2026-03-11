# 🌐 Guía de Nginx Reverse Proxy

Esta guía explica cómo usar **Nginx** como reverse proxy para tu aplicación Spring Boot, tanto en **desarrollo local** como en **producción**.

---

## 📋 Índice

1. [¿Por qué usar Nginx?](#por-qué-usar-nginx)
2. [Estructura de archivos](#estructura-de-archivos)
3. [Desarrollo Local](#desarrollo-local)
4. [Producción (Docker completo)](#producción-docker-completo)
5. [Características incluidas](#características-incluidas)
6. [Configuración SSL/HTTPS](#configuración-ssl-https)
7. [Troubleshooting](#troubleshooting)

---

## 🤔 ¿Por qué usar Nginx?

### Ventajas de usar Nginx como reverse proxy:

✅ **Rendimiento**: Nginx maneja contenido estático mucho más rápido que Spring Boot  
✅ **Rate Limiting**: Protección contra abuso de API (5 login/min, 30 req/s general)  
✅ **SSL/TLS Termination**: Gestión centralizada de certificados HTTPS  
✅ **Compresión Gzip**: Reduce el tamaño de respuestas (JSON, HTML, CSS)  
✅ **Load Balancing**: Fácil escalar a múltiples instancias de Spring Boot  
✅ **Seguridad**: Headers de seguridad, ocultación del servidor backend  
✅ **Caching**: Archivos estáticos, imágenes de productos  
✅ **Logs centralizados**: Acceso y errores en un solo lugar  

---

## 📁 Estructura de archivos

```
onlineshop_backend/
├── nginx/
│   ├── nginx.conf         # Configuración para PRODUCCIÓN (app en Docker)
│   └── nginx.local.conf   # Configuración para DESARROLLO (app en host)
├── docker-compose.yml          # Producción: MySQL + App + Nginx
├── docker-compose.local.yml    # Desarrollo: MySQL + Nginx (app en host)
├── uploads/                    # Imágenes de productos
└── .env                        # Variables de entorno
```

---

## 💻 Desarrollo Local

### Escenario: Spring Boot en host, Nginx + MySQL en Docker

Este setup permite **hot-reload** de código con `mvn spring-boot:run` mientras usas Nginx.

### 1️⃣ Levantar MySQL y Nginx

```bash
# Levantar contenedores de desarrollo
docker compose -f docker-compose.local.yml up -d

# Ver logs
docker compose -f docker-compose.local.yml logs -f nginx
```

### 2️⃣ Iniciar Spring Boot en el host

```bash
# Opción A: Usar el script dev.sh
./dev.sh app-hotreload

# Opción B: Maven directo
mvn spring-boot:run
```

### 3️⃣ Acceder a la aplicación

- **A través de Nginx**: http://localhost (puerto 80)
- **Backend directo**: http://localhost:8080 (sin nginx)
- **MySQL**: localhost:3306

### 4️⃣ Detener todo

```bash
# Parar contenedores
docker compose -f docker-compose.local.yml down

# Ctrl+C en la terminal de Spring Boot
```

### Actualizar script dev.sh (OPCIONAL)

Puedes añadir comandos al script `dev.sh` para automatizar:

```bash
# Añadir al final de dev.sh
start_nginx() {
    echo "🌐 Levantando Nginx + MySQL para desarrollo..."
    docker compose -f docker-compose.local.yml up -d
}

stop_nginx() {
    echo "🛑 Deteniendo Nginx + MySQL..."
    docker compose -f docker-compose.local.yml down
}
```

Uso: `./dev.sh start_nginx`

---

## 🚀 Producción (Docker completo)

### Escenario: MySQL + Spring Boot + Nginx todos en Docker en VPS

Ideal para **VPS (Ubuntu/Debian), servidores dedicados** o cualquier servidor con Docker.

**Dominio configurado**: `alanmarcha.com` con SSL (HTTPS)

### 1️⃣ Levantar todo el stack

```bash
# Build y levantar todos los servicios
docker compose up -d --build

# Ver logs
docker compose logs -f

# Ver logs solo de nginx
docker compose logs -f nginx
```

### 2️⃣ Verificar que funciona

```bash
# Health check
curl http://localhost/health/status

# API test
curl http://localhost/api/products
```

### 3️⃣ Acceso

- **Puerto HTTP**: 80
- **Puerto HTTPS**: 443 (cuando configures SSL)
- MySQL: 3306 (solo interno, no expuesto a Internet)

### 4️⃣ Comandos útiles

```bash
# Reiniciar solo nginx
docker compose restart nginx

# Ver configuración de nginx
docker exec marcha-nginx nginx -t

# Recargar configuración sin downtime
docker exec marcha-nginx nginx -s reload

# Logs en tiempo real
docker compose logs -f nginx

# Parar todo
docker compose down

# Parar y eliminar volúmenes (¡cuidado! borra DB)
docker compose down -v
```

---

## ⚙️ Características incluidas

### 🔒 Rate Limiting

| Endpoint | Límite | Burst |
|----------|--------|-------|
| `/api/auth/login`, `/api/auth/register` | 5 req/min | 3 |
| `/api/*` (general) | 30 req/s | 50 |
| `/health/status` | Sin límite | - |

### 📦 Compresión Gzip

Automáticamente comprime:
- JSON (`application/json`)
- JavaScript (`application/javascript`)
- CSS (`text/css`)
- HTML (`text/html`)
- SVG (`image/svg+xml`)

**Ahorro típico**: 60-80% en tamaño de respuestas.

### 🛡️ Headers de Seguridad

```
X-Frame-Options: SAMEORIGIN
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Referrer-Policy: strict-origin-when-cross-origin
```

### 📂 Archivos estáticos

- `/uploads/` → Servidos directamente por Nginx con caché de 7 días
- `/static/` → Proxy a Spring Boot con caché de 30 días

### ⏱️ Timeouts

- **Connect**: 60s
- **Send**: 60s
- **Read**: 60s

---

## 🔐 Configuración SSL/HTTPS

### Para producción en VPS con dominio alanmarcha.com

#### 1️⃣ Obtener certificados (en tu VPS)

```bash
# Conectar al VPS por SSH
ssh root@tu-ip-vps

# Instalar certbot
sudo apt update
sudo apt install certbot -y

# Detener nginx temporalmente
docker compose down

# Obtener certificado
sudo certbot certonly --standalone \
  -d alanmarcha.com \
  -d www.alanmarcha.com \
  --agree-tos \
  --email admin@alanmarcha.com

# Los certificados quedan en:
# /etc/letsencrypt/live/alanmarcha.com/fullchain.pem
# /etc/letsencrypt/live/alanmarcha.com/privkey.pem
```

#### 2️⃣ Actualizar docker-compose.yml

```yaml
nginx:
  # ...
  ports:
    - "80:80"
    - "443:443"  # ← AÑADIR puerto HTTPS
  volumes:
    - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
    - ./uploads:/uploads:ro
    - /etc/letsencrypt:/etc/letsencrypt:ro  # ← AÑADIR certificados
```

#### 3️⃣ nginx.conf ya está configurado

El archivo `nginx/nginx.conf` ya incluye:
- ✅ Redirección HTTP → HTTPS
- ✅ Configuración SSL con TLS 1.2 y 1.3
- ✅ Headers HSTS
- ✅ Dominio `alanmarcha.com` configurado

#### 4️⃣ Reiniciar nginx

```bash
docker compose up -d
docker compose logs nginx
```

### Renovación automática

```bash
# Añadir a crontab (cada 2 meses)
0 3 1 */2 * certbot renew --quiet && docker compose restart nginx
```

---

## 🐛 Troubleshooting

### ❌ Error: 502 Bad Gateway

**Causa**: Nginx no puede conectar con Spring Boot.

**Solución**:

```bash
# Verificar que Spring Boot está corriendo
curl http://localhost:8080/health/status

# Ver logs de nginx
docker logs marcha-nginx

# Ver logs de app
docker logs marcha-backend

# En desarrollo local: verificar que usas nginx.local.conf
docker exec marcha-nginx-local cat /etc/nginx/nginx.conf | grep upstream
# Debe mostrar: server host.docker.internal:8080
```

### ❌ Error: Connection refused

**Desarrollo local**: Spring Boot no está corriendo en el host.

```bash
# Iniciar Spring Boot
mvn spring-boot:run

# O con el script
./dev.sh app-hotreload
```

### ❌ Error: 413 Request Entity Too Large

**Causa**: Archivo subido demasiado grande.

**Solución**: Aumentar `client_max_body_size` en nginx.conf:

```nginx
http {
    client_max_body_size 100M;  # Era 50M
}
```

Luego: `docker compose restart nginx`

### ❌ Error: Too Many Requests (429)

**Causa**: Rate limiting activado.

**Solución temporal** (desarrollo): Comentar las líneas `limit_req` en nginx.conf.

**Solución permanente**: Ajustar los límites según tus necesidades.

### ❌ CORS errors en navegador

**Causa**: Nginx y Spring Boot manejando CORS diferente.

**Solución**: Descomentar los headers CORS en nginx.conf (líneas ~56-60) o dejar que solo Spring lo maneje.

### 🔍 Ver configuración activa de Nginx

```bash
# Mostrar configuración parseada
docker exec marcha-nginx nginx -T

# Validar sintaxis
docker exec marcha-nginx nginx -t
```

### 📊 Monitorear logs en tiempo real

```bash
# Todos los logs
docker compose logs -f

# Solo nginx
docker compose logs -f nginx

# Solo errores de nginx
docker exec marcha-nginx tail -f /var/log/nginx/error.log

# Solo accesos
docker exec marcha-nginx tail -f /var/log/nginx/access.log
```

---

## 🎯 Próximos pasos

1. ✅ **Usar configuración local**: `docker compose -f docker-compose.local.yml up -d`
2. ✅ **Probar en producción local**: `docker compose up -d --build`
3. ✅ **Deploy a VPS**: Seguir [DEPLOY_VPS.md](./DEPLOY_VPS.md)
4. ✅ **Configurar SSL**: Certificados Let's Encrypt gratuitos
5. ⏳ **Monitoreo**: Añadir Prometheus/Grafana para métricas
6. ⏳ **CDN**: Cloudflare delante de Nginx para caché global

---

## 📚 Recursos adicionales

- [Documentación oficial de Nginx](https://nginx.org/en/docs/)
- [Nginx Reverse Proxy Guide](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/)
- [Let's Encrypt](https://letsencrypt.org/)
- [Spring Boot + Nginx](https://www.baeldung.com/spring-boot-nginx)
- [Deploy en VPS](./DEPLOY_VPS.md) - Guía completa para alanmarcha.com

---

## 💡 Tips finales

1. **Logs son tu amigo**: Siempre revisa logs ante problemas
2. **Testa la configuración**: `nginx -t` antes de reload
3. **Rate limiting en producción**: Ajusta según tu tráfico real
4. **Backups**: No olvides hacer backup de `/etc/letsencrypt` si usas SSL
5. **Monitoreo**: Configura alertas para HTTP 5xx en producción

---

**¿Dudas?** Revisa la sección de [Troubleshooting](#troubleshooting) o consulta los logs 👆
