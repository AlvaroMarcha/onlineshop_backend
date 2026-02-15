#!/bin/bash

echo "🚀 Iniciando entorno de desarrollo..."

# Parar contenedores si están corriendo
echo "🛑 Deteniendo contenedores anteriores..."
docker compose down

# Construir la app
echo "🔨 Construyendo proyecto con Maven..."
mvn clean package -DskipTests

# Levantar contenedores
echo "🐳 Levantando Docker..."
docker compose up --build -d

echo "⏳ Esperando unos segundos a que todo arranque..."
sleep 5

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
