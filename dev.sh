#!/bin/bash

echo "🚀 Iniciando entorno de desarrollo..."

# Funciones
start_db() {
    echo "🐳 Levantando Docker (base de datos)..."
    docker compose up -d db
}

stop_db() {
    echo "🛑 Deteniendo contenedores de base de datos..."
    docker compose stop db
}

start_app_hotreload() {
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
    echo "Uso: ./dev.sh [db-start|db-stop|app|all]"
    echo "  db-start : Levanta solo la base de datos"
    echo "  db-stop  : Detiene solo la base de datos"
    echo "  app      : Construye y levanta Spring Boot"
    echo "  app-hotreload : Ejecuta Spring Boot en modo hot-reload"
    echo "  all      : Levanta DB y Spring Boot (por defecto)"
}

# Si no se pasa parámetro, usar 'all'
param=${1:-all}

# Ejecutar según parámetro
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

echo "✅ Proyecto levantado correctamente."
echo ""
echo "👉 Backend: http://localhost:8080"
echo "👉 BBDD: localhost:3306"
echo ""
echo "📜 Para ver logs:"
echo "   docker compose logs -f app"
echo ""
echo "🛑 Para parar todo:"
echo "   docker compose down"
