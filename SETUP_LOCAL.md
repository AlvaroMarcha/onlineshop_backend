# 🏠 Guía de Configuración Local - Paso a Paso

Esta guía te explica **detalladamente** cómo levantar el proyecto en tu máquina local para desarrollo.

---

## 📋 Índice

1. [Prerrequisitos](#prerrequisitos)
2. [Configuración Inicial](#configuración-inicial)
3. [Opción A: Desarrollo con Hot-Reload (Recomendado)](#opción-a-desarrollo-con-hot-reload-recomendado)
4. [Opción B: Stack Completo en Docker](#opción-b-stack-completo-en-docker)
5. [Opción C: Desarrollo con Nginx Local](#opción-c-desarrollo-con-nginx-local)
6. [Verificar que Todo Funciona](#verificar-que-todo-funciona)
7. [Comandos Útiles](#comandos-útiles)
8. [Troubleshooting](#troubleshooting)

---

## 📦 Prerrequisitos

### 1. Docker Desktop

**Descargar e instalar Docker Desktop:**

- **Windows**: https://docs.docker.com/desktop/install/windows-install/
- **Mac**: https://docs.docker.com/desktop/install/mac-install/
- **Linux**: https://docs.docker.com/desktop/install/linux-install/

**Verificar instalación:**
```bash
docker --version
docker compose version
```

Debes ver algo como:
```
Docker version 24.0.0
Docker Compose version v2.20.0
```

**Asegúrate de que Docker Desktop está corriendo** (icono en la bandeja del sistema).

---

### 2. Java 21

**Verificar si ya lo tienes:**
```bash
java -version
```

Si no está instalado o es menor a versión 21:

- **Windows/Mac**: Descargar desde https://adoptium.net/ (Eclipse Temurin 21)
- **Linux**:
  ```bash
  sudo apt update
  sudo apt install openjdk-21-jdk -y
  ```

**Configurar JAVA_HOME** (si es necesario):
```bash
# Windows (PowerShell como administrador)
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-21.0.0.0-hotspot", "Machine")

# Linux/Mac (añadir a ~/.bashrc o ~/.zshrc)
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

---

### 3. Maven (Incluido en el proyecto)

Este proyecto incluye **Maven Wrapper** (`mvnw`), así que **NO necesitas instalar Maven**.

**Verificar que funciona:**
```bash
# Windows
.\mvnw.cmd --version

# Linux/Mac
./mvnw --version
```

---

### 4. Git

**Verificar instalación:**
```bash
git --version
```

Si no está instalado:
- **Windows**: https://git-scm.com/download/win
- **Mac**: `brew install git`
- **Linux**: `sudo apt install git -y`

---

## 🔧 Configuración Inicial

### Paso 1: Clonar el repositorio (si aún no lo tienes)

```bash
# Clonar
git clone https://github.com/AlvaroMarcha/onlineshop_backend.git

# Entrar al directorio
cd onlineshop_backend

# Cambiar a rama develop
git checkout develop
```

---

### Paso 2: Crear archivo .env

El proyecto necesita un archivo `.env` con las variables de entorno.

```bash
# Copiar el template
cp .env.example .env

# Windows PowerShell (si cp no funciona)
Copy-Item .env.example .env
```

**Editar el archivo .env** (con VS Code, Notepad++, o cualquier editor):

```bash
# Windows
code .env

# O con notepad
notepad .env
```

**Variables mínimas necesarias para desarrollo local:**

```env
# ========================================
# DESARROLLO LOCAL - Windows
# ========================================

# MySQL Container
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=onlineshop

# Database Connection
DB_URL=jdbc:mysql://localhost:3306/onlineshop?useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=root
DB_DRIVER=com.mysql.cj.jdbc.Driver
DB_DIALECT=org.hibernate.dialect.MySQLDialect
DB_SHOW_SQL=true
DB_HBM2DDL=update

# Server
SERVER_PORT=8080
APP_NAME=Online Shop
APP_BASE_URL=http://localhost:8080
APP_FRONTEND_URL=http://localhost:5500

# Storage Paths (CAMBIAR según tu sistema operativo)
# Windows:
IMAGES_STORAGE_PATH=C:/Users/TU_USUARIO/Documents/BACKEND/0_JAVA/onlineshop_backend/uploads/images
INVOICES_STORAGE_PATH=C:/Users/TU_USUARIO/Documents/BACKEND/0_JAVA/onlineshop_backend/uploads/invoices

# Linux/Mac:
# IMAGES_STORAGE_PATH=/home/tu-usuario/onlineshop_backend/uploads/images
# INVOICES_STORAGE_PATH=/home/tu-usuario/onlineshop_backend/uploads/invoices

APP_IMAGES_PUBLIC_PATH=/images

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:*,http://127.0.0.1:*

# JWT
JWT_SECRET=development-secret-key-change-in-production
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=2592000000

# Admin Users (Seed Data)
ADMIN_USERNAME=admin
ADMIN_EMAIL=admin@test.com
ADMIN_PASSWORD=Admin123!
ADMIN_PHONE=+34600000000

SUPER_ADMIN_USERNAME=superadmin
SUPER_ADMIN_EMAIL=superadmin@test.com
SUPER_ADMIN_PASSWORD=SuperAdmin123!
SUPER_ADMIN_PHONE=+34600000001

# Email (OPCIONAL para desarrollo - puedes dejarlo vacío)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=

# Google OAuth (OPCIONAL)
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=

# Stripe (usar claves TEST)
STRIPE_SECRET_KEY=sk_test_tu-clave-test
STRIPE_PUBLISHABLE_KEY=pk_test_tu-clave-test
```

**⚠️ IMPORTANTE**: Cambia las rutas de `IMAGES_STORAGE_PATH` e `INVOICES_STORAGE_PATH` según tu usuario y sistema operativo.

---

### Paso 3: Crear carpetas de uploads

```bash
# Crear directorios
mkdir -p uploads/images
mkdir -p uploads/invoices

# Windows PowerShell (si mkdir -p no funciona)
New-Item -ItemType Directory -Path "uploads/images" -Force
New-Item -ItemType Directory -Path "uploads/invoices" -Force
```

---

## 🚀 Opción A: Desarrollo con Hot-Reload (Recomendado)

Esta es la **mejor opción para desarrollo** porque permite cambios en código sin reiniciar.

### ¿Qué se levanta?
- ✅ MySQL en Docker (container)
- ✅ Spring Boot en tu máquina (proceso local con Maven)
- ❌ Nginx NO (accedes directo a Spring Boot)

### Paso a paso:

#### 1. Levantar solo la base de datos

```bash
# Usando el script de desarrollo
./dev.sh db-start

# Windows PowerShell (si bash no funciona)
docker compose up -d mysql
```

**Espera 10-15 segundos** para que MySQL esté listo.

#### 2. Verificar que MySQL está corriendo

```bash
docker compose ps
```

Debes ver:
```
NAME            STATUS
marcha-mysql    Up
```

#### 3. Ejecutar Spring Boot con hot-reload

```bash
# Opción 1: Con el script (Linux/Mac/Git Bash)
./dev.sh app-hotreload

# Opción 2: Comando directo (Windows PowerShell)
.\mvnw.cmd spring-boot:run

# Opción 3: Maven directo (si tienes Maven instalado)
mvn spring-boot:run
```

**Primera vez tardará 2-5 minutos** descargando dependencias.

#### 4. Ver logs

La consola mostrará los logs de Spring Boot en tiempo real:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.5.5)

...
Started BackendTemplateWebApplication in 8.234 seconds
```

#### 5. Acceder a la aplicación

- **API**: http://localhost:8080
- **Health Check**: http://localhost:8080/health/status
- **Swagger UI** (si está configurado): http://localhost:8080/swagger-ui.html

### Ventajas de esta opción:
✅ Hot-reload automático al guardar archivos  
✅ Logs en consola directamente  
✅ Fácil debugging  
✅ Menor uso de recursos  

### Para detener:
```bash
# Ctrl+C en la consola de Spring Boot

# Luego detener MySQL
./dev.sh db-stop

# O detener todo
docker compose down
```

---

## 🐳 Opción B: Stack Completo en Docker

Todo corre en Docker (MySQL + Spring Boot + Nginx).

### ¿Qué se levanta?
- ✅ MySQL en Docker
- ✅ Spring Boot en Docker
- ✅ Nginx en Docker (reverse proxy)

### Paso a paso:

#### 1. Build y levantar todo

```bash
# Usando el script
./dev.sh all

# O comando directo
docker compose up -d --build
```

**Primera vez tardará 5-10 minutos** (build de Maven + imagen Docker).

#### 2. Ver logs

```bash
# Todos los servicios
docker compose logs -f

# Solo app
docker compose logs -f app

# Solo nginx
docker compose logs -f nginx

# Solo mysql
docker compose logs -f mysql
```

#### 3. Verificar que todo está corriendo

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

#### 4. Acceder a la aplicación

- **A través de Nginx**: http://localhost (puerto 80)
- **Directo a Spring Boot**: http://localhost:8080 (si exponiste el puerto)
- **Health Check**: http://localhost/health/status

### Para detener:
```bash
# Detener todo
./dev.sh stop

# O comando directo
docker compose down
```

---

## 🌐 Opción C: Desarrollo con Nginx Local

MySQL y Nginx en Docker, Spring Boot en tu máquina.

### ¿Qué se levanta?
- ✅ MySQL en Docker
- ✅ Nginx en Docker (proxy a Spring Boot en tu máquina)
- ✅ Spring Boot en tu máquina (hot-reload)

### Paso a paso:

#### 1. Levantar MySQL + Nginx

```bash
# Usando el script
./dev.sh nginx-local

# O comando directo
docker compose -f docker-compose.local.yml up -d
```

#### 2. Ejecutar Spring Boot

```bash
# En otra terminal
./dev.sh app-hotreload

# O
.\mvnw.cmd spring-boot:run
```

#### 3. Acceder a la aplicación

- **A través de Nginx**: http://localhost (puerto 80)
- **Directo a Spring Boot**: http://localhost:8080
- **Health Check**: http://localhost/health/status

### Ventajas:
✅ Nginx como en producción  
✅ Spring Boot con hot-reload  
✅ Test de rate limiting y compresión  

### Para detener:
```bash
# Detener MySQL + Nginx
./dev.sh nginx-stop

# O
docker compose -f docker-compose.local.yml down

# Ctrl+C en Spring Boot
```

---

## ✅ Verificar que Todo Funciona

### 1. Health Check

```bash
curl http://localhost:8080/health/status
```

Respuesta esperada:
```json
{"status":"UP"}
```

### 2. Listar productos (debe devolver array vacío o productos seed)

```bash
curl http://localhost:8080/api/products
```

Respuesta esperada:
```json
[
  {
    "id": 1,
    "name": "Laptop HP 15 Ryzen 5",
    "price": 599.99,
    ...
  }
]
```

### 3. Login con usuario admin

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin123!"
  }'
```

Respuesta esperada:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "...",
  "expiresIn": 3600000
}
```

### 4. Verificar base de datos

```bash
# Entrar a MySQL
docker exec -it marcha-mysql mysql -uroot -proot

# Ver bases de datos
SHOW DATABASES;

# Usar la base de datos
USE onlineshop;

# Ver tablas creadas
SHOW TABLES;

# Ver usuarios seed
SELECT * FROM users;

# Salir
EXIT;
```

Debes ver tablas como: `users`, `roles`, `products`, `categories`, etc.

---

## 🛠️ Comandos Útiles

### Script de desarrollo (dev.sh)

```bash
# Ver ayuda
./dev.sh help

# Levantar solo MySQL
./dev.sh db-start

# Detener MySQL
./dev.sh db-stop

# Spring Boot con hot-reload
./dev.sh app-hotreload

# Spring Boot en Docker
./dev.sh app

# Stack completo
./dev.sh all

# MySQL + Nginx + Spring Boot en host
./dev.sh nginx-local

# Detener Nginx local
./dev.sh nginx-stop

# Ver logs de nginx
./dev.sh nginx-logs

# Stack completo con nginx
./dev.sh production

# Detener todo
./dev.sh stop
```

### Docker Compose

```bash
# Levantar servicios
docker compose up -d

# Levantar con rebuild
docker compose up -d --build

# Ver logs
docker compose logs -f

# Ver estado
docker compose ps

# Detener servicios
docker compose down

# Detener y eliminar volúmenes (¡borra DB!)
docker compose down -v

# Reiniciar servicio específico
docker compose restart app
docker compose restart mysql
docker compose restart nginx

# Ver logs de servicio específico
docker compose logs -f app
```

### Maven

```bash
# Compilar
.\mvnw.cmd compile

# Compilar sin logs
.\mvnw.cmd compile -q

# Ejecutar tests
.\mvnw.cmd test

# Package (crear JAR)
.\mvnw.cmd clean package

# Package sin tests
.\mvnw.cmd clean package -DskipTests

# Limpiar build
.\mvnw.cmd clean

# Ver dependencias
.\mvnw.cmd dependency:tree
```

### Git

```bash
# Ver estado
git status

# Ver ramas
git branch

# Cambiar a develop
git checkout develop

# Actualizar código
git pull origin develop

# Ver logs
git log --oneline -10
```

---

## 🐛 Troubleshooting

### ❌ Error: Port 3306 is already in use

**Causa**: Ya tienes MySQL corriendo en tu máquina.

**Soluciones**:

1. **Cambiar puerto en docker-compose.yml**:
   ```yaml
   mysql:
     ports:
       - "3307:3306"  # Usar puerto 3307 en host
   ```
   
   Y en .env:
   ```env
   DB_URL=jdbc:mysql://localhost:3307/onlineshop?...
   ```

2. **Detener MySQL local**:
   ```bash
   # Windows
   net stop MySQL80
   
   # Linux
   sudo systemctl stop mysql
   
   # Mac
   brew services stop mysql
   ```

---

### ❌ Error: Port 8080 is already in use

**Causa**: Otro proceso usando puerto 8080.

**Solución 1 - Cambiar puerto**:

En `.env`:
```env
SERVER_PORT=8081
```

Accede a: http://localhost:8081

**Solución 2 - Matar proceso**:
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <numero-pid> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

---

### ❌ Error: Cannot connect to Docker daemon

**Causa**: Docker Desktop no está corriendo.

**Solución**:
1. Abre Docker Desktop
2. Espera a que arranque completamente
3. Verifica: `docker ps`

---

### ❌ Error: Access denied for user 'root'@'localhost'

**Causa**: Contraseña incorrecta en .env.

**Solución**:
1. Verifica que `DB_PASSWORD` en .env coincide con `MYSQL_ROOT_PASSWORD`
2. Por defecto ambos deben ser `root`
3. Si cambiaste alguno, reinicia MySQL:
   ```bash
   docker compose down -v
   docker compose up -d mysql
   ```

---

### ❌ Error: Table 'onlineshop.users' doesn't exist

**Causa**: Base de datos no se creó.

**Solución**:
1. Verifica `DB_HBM2DDL=update` en .env
2. Reinicia Spring Boot
3. Revisa logs por errores SQL

---

### ❌ Error: Maven dependencies not downloading

**Causa**: Problemas de red o caché corrupta.

**Solución**:
```bash
# Limpiar caché de Maven
.\mvnw.cmd clean

# Forzar descarga de dependencias
.\mvnw.cmd dependency:purge-local-repository

# Volver a compilar
.\mvnw.cmd clean install
```

---

### ❌ Error: LazyInitializationException

**Causa**: Intentando acceder a datos lazy fuera de transacción.

**Solución**: Ya está corregido en `DataInitializer.java` con `@Transactional`.

Si lo ves en tu código:
1. Añade `@Transactional` al método
2. O usa `fetch = FetchType.EAGER` en la relación

---

### ❌ Hot-reload no funciona

**Causa**: Spring DevTools no configurado.

**Solución**:

Verifica en `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

En VS Code, instala extensión: **Spring Boot Extension Pack**

---

### ❌ Error: 502 Bad Gateway (con nginx)

**Causa**: Spring Boot no está corriendo.

**Solución**:
1. Verifica que Spring Boot está activo: `curl http://localhost:8080/health/status`
2. Si no responde, inicia Spring Boot: `.\mvnw.cmd spring-boot:run`
3. Revisa logs: `docker logs marcha-nginx-local`

---

### ❌ Uploads no funcionan

**Causa**: Permisos o ruta incorrecta.

**Solución**:
1. Verifica que la carpeta existe:
   ```bash
   ls uploads/images
   ```

2. Verifica rutas en .env (deben ser absolutas)

3. Dale permisos:
   ```bash
   # Linux/Mac
   chmod -R 755 uploads
   
   # Windows: Click derecho → Propiedades → Seguridad → Editar
   ```

---

### 🔍 Ver logs detallados

Para debugging:

```bash
# Spring Boot con más logs
export LOGGING_LEVEL_ROOT=DEBUG
.\mvnw.cmd spring-boot:run

# O en application.properties
logging.level.root=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

---

## 📚 Recursos adicionales

- [ENV_GUIDE.md](./ENV_GUIDE.md) - Variables de entorno
- [NGINX_GUIDE.md](./NGINX_GUIDE.md) - Configuración de nginx
- [DEPLOY_VPS.md](./DEPLOY_VPS.md) - Deploy en producción
- [README.md](./README.md) - Documentación general

---

## 🎯 Checklist de inicio rápido

- [ ] Docker Desktop instalado y corriendo
- [ ] Java 21 instalado (`java -version`)
- [ ] Proyecto clonado
- [ ] Archivo `.env` creado y editado
- [ ] Rutas de storage actualizadas en `.env`
- [ ] Carpetas `uploads/images` y `uploads/invoices` creadas
- [ ] MySQL levantado (`./dev.sh db-start`)
- [ ] Spring Boot corriendo (`.\mvnw.cmd spring-boot:run`)
- [ ] Health check OK (`curl http://localhost:8080/health/status`)
- [ ] Login admin funciona
- [ ] Productos seed creados

---

**¡Listo para desarrollar! 🚀**

Si tienes problemas, revisa la sección de [Troubleshooting](#troubleshooting) o consulta los logs.
