# 📁 Configuraciones de Nginx

Esta carpeta contiene las configuraciones de Nginx para diferentes entornos.

---

## 📄 Archivos disponibles

| Archivo | Entorno | Uso |
|---------|---------|-----|
| `nginx.conf` | **Producción VPS** | Configurado para alanmarcha.com con SSL |
| `nginx.local.conf` | **Desarrollo local** | App en host (mvn), MySQL + Nginx en Docker |

---

## 🎯 ¿Cuál usar?

### Desarrollo local con hot-reload
```bash
# Usar nginx.local.conf
docker compose -f docker-compose.local.yml up -d
mvn spring-boot:run

# Accede a http://localhost
```

**Configuración**: Spring Boot en `host.docker.internal:8080`

---

### Producción en VPS (alanmarcha.com)
```bash
# En el VPS, usar nginx.conf
docker compose up -d --build

# Accede a https://alanmarcha.com
```

**Configuración**: 
- Spring Boot en `app:8080` (red Docker)
- SSL con certificados Let's Encrypt
- Redirección automática HTTP → HTTPS
- Dominio: `alanmarcha.com` y `www.alanmarcha.com`

---

## 🔧 Personalización

### Cambiar upstream (backend)

Edita la sección `upstream spring_backend`:

```nginx
upstream spring_backend {
    # Para Docker:
    server app:8080 max_fails=3 fail_timeout=30s;
    
    # Para local:
    # server host.docker.internal:8080 max_fails=3 fail_timeout=30s;
    
    # Para múltiples instancias (load balancing):
    # server app1:8080 max_fails=3 fail_timeout=30s;
    # server app2:8080 max_fails=3 fail_timeout=30s;
}
```

### Ajustar rate limiting

```nginx
# Cambiar límites al principio del archivo
limit_req_zone $binary_remote_addr zone=api_limit:10m rate=30r/s;  # 30 req/segundo
limit_req_zone $binary_remote_addr zone=auth_limit:10m rate=5r/m;  # 5 login/minuto
```

### Aumentar tamaño máximo de uploads

```nginx
http {
    client_max_body_size 100M;  # Era 50M
}
```

---

## 📊 Ver logs

### En desarrollo local
```bash
docker logs marcha-nginx-local -f
```

### En producción
```bash
docker logs marcha-nginx -f

# Solo errores
docker exec marcha-nginx tail -f /var/log/nginx/error.log

# Solo accesos
docker exec marcha-nginx tail -f /var/log/nginx/access.log
```

---

## ✅ Validar configuración

Antes de hacer reload, verifica sintaxis:

```bash
# Validar sintaxis (local)
docker exec marcha-nginx-local nginx -t

# Validar sintaxis (producción)
docker exec marcha-nginx nginx -t

# Recargar sin downtime
docker exec marcha-nginx nginx -s reload
```

---

## 🔗 Recursos

- [Documentación completa](../NGINX_GUIDE.md)
- [Deploy en VPS](../DEPLOY_VPS.md) - Guía paso a paso para alanmarcha.com
- [Variables de entorno](../ENV_GUIDE.md)
