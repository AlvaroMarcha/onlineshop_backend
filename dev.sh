#!/bin/bash

echo "🚀 Iniciando entorno de desarrollo..."

# Funciones
start_db() {
    echo "🐳 Levantando Docker (base de datos)..."
    docker compose up -d mysql
}

stop_db() {
    echo "🛑 Deteniendo contenedor de base de datos..."
    docker compose stop mysql
}

stop_all() {
    echo "🛑 Parando TODO el entorno..."
    docker compose down
}

start_nginx_local() {
    echo "🌐 Levantando Nginx + MySQL para desarrollo local..."
    echo "   (Spring Boot debe ejecutarse con mvn spring-boot:run)"
    docker compose -f docker-compose.local.yml up -d
    echo "✅ Accede a http://localhost (nginx proxy a localhost:8080)"
}

stop_nginx_local() {
    echo "🛑 Deteniendo entorno local con Nginx..."
    docker compose -f docker-compose.local.yml down
}

start_production_stack() {
    echo "🚀 Levantando stack completo de producción (MySQL + App + Nginx)..."
    docker compose up -d --build
    echo "✅ Accede a http://localhost (todo en Docker)"
}

logs_nginx() {
    echo "📋 Logs de Nginx en tiempo real..."
    docker compose logs -f nginx
}

start_app_hotreload() {
    echo "🛑 Parando contenedor app si existe..."
    docker compose stop app

    echo "🔨 Ejecutando Spring Boot con Maven..."
    mvn spring-boot:run
}

start_app() {
    echo "🔨 Construyendo proyecto con Maven..."
    mvn clean package -DskipTests

    echo "🚀 Levantando Spring Boot..."
    docker compose up -d app
}

help_menu() {
    echo "========================================="
    echo "🛠️  Script de Desarrollo - Online Shop"
    echo "========================================="
    echo ""
    echo "Uso: ./dev.sh [comando]"
    echo ""
    echo "📦 Base de datos:"
    echo "  db-start         : Levanta solo MySQL"
    echo "  db-stop          : Detiene solo MySQL"
    echo ""
    echo "🚀 Aplicación:"
    echo "  app              : Build + levanta Spring Boot en Docker"
    echo "  app-hotreload    : Ejecuta Spring Boot con hot-reload (mvn)"
    echo ""
    echo "🌐 Nginx (Reverse Proxy):"
    echo "  nginx-local      : MySQL + Nginx (app en host con mvn)"
    echo "  nginx-stop       : Detiene entorno nginx local"
    echo "  nginx-logs       : Ver logs de nginx en tiempo real"
    echo "  production       : Stack completo (MySQL + App + Nginx en Docker)"
    echo ""
    echo "🛑 Control:"
    echo "  stop             : Detiene TODO"
    echo "  all              : Levanta DB + App (sin nginx)"
    echo ""
    echo "========================================="
}

# Si no se pasa parámetro, mostrar ayuda
param=${1:-help}

case "$param" in
    db-start)
        start_db
        ;;
    db-stop)
        stop_db
        ;;
    app)
        start_app
        ;;
    app-hotreload)
        start_app_hotreload
        ;;
    nginx-local)
        start_nginx_local
        ;;
    nginx-stop)
        stop_nginx_local
        ;;
    nginx-logs)
        logs_nginx
        ;;
    production)
        start_production_stack
        ;;
    stop)
        stop_all
        ;;
    all)
        start_db
        echo "⏳ Esperando 10 segundos a que la base de datos esté lista..."
        sleep 10
        start_app
        ;;
    help|*)
        help_menu
        ;;
esac

echo "✅ Script ejecutado."