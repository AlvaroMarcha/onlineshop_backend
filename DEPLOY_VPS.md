# 🚀 Deploy en VPS (Linux) con Docker

Guía completa para desplegar tu aplicación Spring Boot con Nginx en un VPS Linux.

**Dominio configurado**: `alanmarcha.com`

---

## 📋 Prerrequisitos

- ✅ VPS con Ubuntu 20.04+ / Debian 11+ (mínimo 2GB RAM)
- ✅ Acceso SSH al servidor
- ✅ Dominio apuntando al IP del VPS (DNS tipo A)
- ✅ Puertos 80 y 443 abiertos en firewall

---

## 🎯 Arquitectura final

```
Internet (HTTPS)
    ↓
alanmarcha.com (DNS → IP VPS)
    ↓
Nginx (puerto 443) - SSL, rate limiting, compresión
    ↓
Spring Boot (puerto 8080) - API REST
    ↓
MySQL (puerto 3306 - solo interno)
```

---

## 🔧 Paso 1: Configurar DNS

En tu proveedor de dominio (Namecheap, GoDaddy, Cloudflare...):

```
Tipo: A
Nombre: @
Valor: 123.45.67.89 (IP de tu VPS)
TTL: 3600

Tipo: A
Nombre: www
Valor: 123.45.67.89
TTL: 3600
```

**Verificar** (espera 5-60 minutos):
```bash
nslookup alanmarcha.com
# Debe mostrar tu IP
```

---

## 🖥️ Paso 2: Preparar el servidor

### 2.1 Conectar por SSH

```bash
# Desde tu máquina local
ssh root@123.45.67.89
# O con usuario no-root:
ssh tu-usuario@123.45.67.89
```

### 2.2 Actualizar sistema

```bash
sudo apt update && sudo apt upgrade -y
```

### 2.3 Instalar Docker y Docker Compose

```bash
# Instalar Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Añadir usuario actual al grupo docker
sudo usermod -aG docker $USER

# Instalar Docker Compose
sudo apt install docker-compose-plugin -y

# Verificar instalación
docker --version
docker compose version
```

**Cerrar sesión y volver a entrar** para aplicar cambios de grupo.

### 2.4 Instalar Git

```bash
sudo apt install git -y
git --version
```

---

## 📦 Paso 3: Clonar repositorio

```bash
# Ir al home
cd ~

# Clonar repositorio
git clone https://github.com/AlvaroMarcha/onlineshop_backend.git

# Entrar al directorio
cd onlineshop_backend

# Cambiar a rama deseada (develop o main)
git checkout develop
```

---

## 🔐 Paso 4: Configurar variables de entorno

### 4.1 Crear archivo .env

```bash
# Copiar template
cp .env.example .env

# Editar con nano o vim
nano .env
```

### 4.2 Variables críticas para PRODUCCIÓN

```bash
# ========================================
# PRODUCCIÓN - alanmarcha.com
# ========================================

# MySQL (container interno)
MYSQL_ROOT_PASSWORD=tu-password-mysql-muy-seguro-123
MYSQL_DATABASE=onlineshop

# Database URL (conectar desde app al container mysql)
DB_URL=jdbc:mysql://mysql:3306/onlineshop?useSSL=false
DB_USERNAME=root
DB_PASSWORD=tu-password-mysql-muy-seguro-123

# Servidor
SERVER_PORT=8080
APP_BASE_URL=https://alanmarcha.com

# CORS (frontend)
CORS_ALLOWED_ORIGINS=https://alanmarcha.com,https://www.alanmarcha.com

# JWT (generar con: openssl rand -base64 64)
JWT_SECRET=tu-secreto-jwt-super-largo-minimo-256-caracteres
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=2592000000

# Admin users (CAMBIAR CONTRASEÑAS!)
ADMIN_USERNAME=admin
ADMIN_EMAIL=admin@alanmarcha.com
ADMIN_PASSWORD=Admin2026!Seguro!
ADMIN_PHONE=+34600000000

SUPER_ADMIN_USERNAME=superadmin
SUPER_ADMIN_EMAIL=superadmin@alanmarcha.com
SUPER_ADMIN_PASSWORD=SuperAdmin2026!MuySeguro!
SUPER_ADMIN_PHONE=+34600000001

# Email (Gmail u otro SMTP)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=tu-app-password-de-gmail

# Google OAuth (si aplica)
GOOGLE_CLIENT_ID=tu-client-id
GOOGLE_CLIENT_SECRET=tu-client-secret

# Stripe (modo LIVE)
STRIPE_SECRET_KEY=sk_live_tu-clave-real
STRIPE_PUBLISHABLE_KEY=pk_live_tu-clave-real

# Empresa
COMPANY_NAME=Alan Marcha Shop
COMPANY_EMAIL=info@alanmarcha.com
COMPANY_PHONE=+34600000000
COMPANY_ADDRESS=Tu dirección completa
```

**Guardar**: `Ctrl+O`, `Enter`, `Ctrl+X`

### 4.3 Generar JWT Secret seguro

```bash
# Generar secreto aleatorio
openssl rand -base64 64

# Copiar resultado y pegarlo en JWT_SECRET del .env
```

---

## 🔒 Paso 5: Configurar SSL con Let's Encrypt

### 5.1 Instalar Certbot

```bash
sudo apt install certbot -y
```

### 5.2 Obtener certificado SSL

**IMPORTANTE**: Detén nginx si ya está corriendo:
```bash
docker compose down
```

Obtener certificado:
```bash
sudo certbot certonly --standalone \
  -d alanmarcha.com \
  -d www.alanmarcha.com \
  --agree-tos \
  --no-eff-email \
  --email admin@alanmarcha.com
```

Certificados quedan en:
```
/etc/letsencrypt/live/alanmarcha.com/fullchain.pem
/etc/letsencrypt/live/alanmarcha.com/privkey.pem
```

### 5.3 Verificar certificados

```bash
sudo ls -la /etc/letsencrypt/live/alanmarcha.com/
```

### 5.4 Dar acceso a Docker

```bash
# Crear grupo para certificados
sudo groupadd sslcerts

# Añadir usuario actual y root
sudo usermod -aG sslcerts $USER
sudo usermod -aG sslcerts root

# Cambiar permisos
sudo chgrp -R sslcerts /etc/letsencrypt/live
sudo chgrp -R sslcerts /etc/letsencrypt/archive
sudo chmod 750 /etc/letsencrypt/live
sudo chmod 750 /etc/letsencrypt/archive
```

---

## 🐳 Paso 6: Actualizar docker-compose.yml

Editar `docker-compose.yml`:

```bash
nano docker-compose.yml
```

Añadir volumen de certificados SSL al servicio nginx:

```yaml
  nginx:
    image: nginx:alpine
    container_name: marcha-nginx
    restart: always
    ports:
      - "80:80"
      - "443:443"  # ← AÑADIR puerto HTTPS
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./uploads:/uploads:ro
      - nginx_logs:/var/log/nginx
      - /etc/letsencrypt:/etc/letsencrypt:ro  # ← AÑADIR certificados SSL
    depends_on:
      - app
    networks:
      - marcha-network
```

**Guardar**: `Ctrl+O`, `Enter`, `Ctrl+X`

---

## 🚀 Paso 7: Desplegar aplicación

### 7.1 Crear directorio de uploads

```bash
mkdir -p uploads
sudo chown -R $USER:$USER uploads
sudo chmod 755 uploads
```

### 7.2 Build y levantar servicios

```bash
# Build y levantar todo
docker compose up -d --build

# Ver logs en tiempo real
docker compose logs -f

# Ver solo logs de app
docker compose logs -f app

# Ver solo logs de nginx
docker compose logs -f nginx
```

**Primera vez tardará 5-10 minutos** (descarga imágenes, build Maven, etc.)

### 7.3 Verificar que todo está corriendo

```bash
docker compose ps
```

Debes ver:
```
NAME              STATUS
marcha-mysql      Up
marcha-backend    Up
marcha-nginx      Up
```

---

## ✅ Paso 8: Verificar funcionamiento

### 8.1 Health check

```bash
curl https://alanmarcha.com/health/status
# Respuesta: {"status":"UP"}
```

### 8.2 Test API

```bash
# Productos
curl https://alanmarcha.com/api/products

# Login admin
curl -X POST https://alanmarcha.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin2026!Seguro!"
  }'
```

### 8.3 Verificar en navegador

- 🌐 https://alanmarcha.com
- 🌐 https://www.alanmarcha.com
- 🔒 Debe mostrar candado verde (SSL válido)

---

## 🔄 Paso 9: Renovación automática de SSL

### 9.1 Configurar cron para renovación

```bash
# Editar crontab
sudo crontab -e
```

Añadir al final:
```bash
# Renovar SSL cada día a las 3am, reiniciar nginx si se renueva
0 3 * * * certbot renew --quiet --deploy-hook "cd /home/tu-usuario/onlineshop_backend && docker compose restart nginx"
```

**Cambiar** `/home/tu-usuario/` por tu ruta real.

### 9.2 Verificar renovación (test)

```bash
sudo certbot renew --dry-run
```

---

## 🔧 Paso 10: Comandos útiles de gestión

### Gestión de contenedores

```bash
# Ver estado
docker compose ps

# Ver logs
docker compose logs -f

# Reiniciar servicio específico
docker compose restart app
docker compose restart nginx
docker compose restart mysql

# Parar todo
docker compose down

# Parar y eliminar volúmenes (¡CUIDADO! borra DB)
docker compose down -v

# Rebuild después de cambios en código
git pull origin develop
docker compose up -d --build
```

### Gestión de logs

```bash
# Logs de nginx
docker compose logs -f nginx

# Logs de Spring Boot
docker compose logs -f app

# Últimas 100 líneas
docker compose logs --tail=100 app

# Limpiar logs antiguos
docker system prune -a --volumes
```

### Gestión de base de datos

```bash
# Entrar a MySQL desde container
docker exec -it marcha-mysql mysql -uroot -p
# Introducir MYSQL_ROOT_PASSWORD

# Backup de base de datos
docker exec marcha-mysql mysqldump -uroot -p onlineshop > backup_$(date +%Y%m%d).sql

# Restaurar backup
docker exec -i marcha-mysql mysql -uroot -p onlineshop < backup_20260311.sql
```

### Monitoreo de recursos

```bash
# Ver uso de CPU/RAM por container
docker stats

# Ver espacio en disco
df -h

# Ver logs del sistema
journalctl -u docker -f
```

---

## 🔥 Firewall (UFW)

### Configurar firewall básico

```bash
# Instalar UFW si no está
sudo apt install ufw -y

# Configurar reglas
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow ssh
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# Activar firewall
sudo ufw enable

# Ver estado
sudo ufw status
```

---

## 🚨 Troubleshooting

### ❌ Error: Cannot connect to alanmarcha.com

**Solución**:
1. Verificar DNS: `nslookup alanmarcha.com`
2. Verificar nginx: `docker logs marcha-nginx`
3. Verificar firewall: `sudo ufw status`

### ❌ Error: 502 Bad Gateway

**Causa**: Spring Boot no arrancó.

**Solución**:
```bash
# Ver logs de app
docker compose logs app

# Verificar que mysql está activo
docker compose ps mysql

# Reiniciar app
docker compose restart app
```

### ❌ Error: SSL certificate problem

**Solución**:
```bash
# Verificar certificados
sudo certbot certificates

# Renovar manualmente
sudo certbot renew --force-renewal

# Reiniciar nginx
docker compose restart nginx
```

### ❌ Error: Permission denied en uploads

**Solución**:
```bash
sudo chown -R $USER:$USER uploads
sudo chmod -R 755 uploads
docker compose restart app
```

---

## 📊 Monitoreo en producción (opcional)

### Instalar Portainer (UI para Docker)

```bash
docker volume create portainer_data

docker run -d \
  -p 9000:9000 \
  --name=portainer \
  --restart=always \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v portainer_data:/data \
  portainer/portainer-ce:latest
```

Accede a: `http://tu-ip:9000`

---

## 🔐 Seguridad adicional

### 1. Cambiar puerto SSH (opcional)

```bash
sudo nano /etc/ssh/sshd_config
# Cambiar: Port 22 → Port 2222
sudo systemctl restart sshd
```

### 2. Fail2ban (protección contra brute force)

```bash
sudo apt install fail2ban -y
sudo systemctl enable fail2ban
sudo systemctl start fail2ban
```

### 3. Actualizar sistema regularmente

```bash
# Ejecutar cada semana
sudo apt update && sudo apt upgrade -y
```

---

## 🎯 Checklist de deploy completo

- [ ] DNS apuntando a IP del VPS
- [ ] Docker y Docker Compose instalados
- [ ] Repositorio clonado
- [ ] Variables .env configuradas (PRODUCCIÓN)
- [ ] JWT_SECRET generado
- [ ] Contraseñas de admin cambiadas
- [ ] Certificado SSL obtenido
- [ ] docker-compose.yml con volumen SSL
- [ ] Contenedores corriendo (docker compose ps)
- [ ] Health check funcionando (curl /health/status)
- [ ] API respondiendo (curl /api/products)
- [ ] HTTPS funcionando (navegador con candado)
- [ ] Renovación SSL automática configurada
- [ ] Firewall configurado
- [ ] Backups de DB programados

---

## 📚 Próximos pasos

1. ✅ **Configurar frontend**: Apuntar a `https://alanmarcha.com`
2. ⏳ **Backups automáticos**: Programar cron de MySQL dumps
3. ⏳ **Monitoreo**: Configurar alertas (Uptime Robot, etc.)
4. ⏳ **CDN**: Cloudflare delante de nginx
5. ⏳ **CI/CD**: GitHub Actions para deploy automático

---

## 📞 Soporte

- Logs: `docker compose logs -f`
- Nginx test: `docker exec marcha-nginx nginx -t`
- SSL test: [SSL Labs](https://www.ssllabs.com/ssltest/)
- Performance test: [GTmetrix](https://gtmetrix.com/)

---

**¡Deploy completado! 🚀** Tu aplicación está corriendo en `https://alanmarcha.com`
