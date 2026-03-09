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
    echo "Uso: ./dev.sh [db-start|db-stop|app|app-hotreload|stop|all]"
    echo "  db-start      : Levanta solo la base de datos"
    echo "  db-stop       : Detiene solo la base de datos"
    echo "  app           : Construye y levanta Spring Boot en Docker"
    echo "  app-hotreload : Ejecuta Spring Boot en modo hot-reload"
    echo "  stop          : Detiene TODO (contenedores y red)"
    echo "  all           : Levanta DB y Spring Boot (por defecto)"
}

# Si no se pasa parámetro, usar 'all'
param=${1:-all}

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
    stop)
        stop_all
        ;;
    all)
        start_db
        echo "⏳ Esperando 10 segundos a que la base de datos esté lista..."
        sleep 10
        start_app
        ;;
    *)
        help_menu
        ;;
esac

echo "✅ Script ejecutado."