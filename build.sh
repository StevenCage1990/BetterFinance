#!/bin/bash

# Build script for Family Finance Management System
# This script builds the frontend and packages it with the backend

set -e

echo "========================================="
echo "Building Family Finance Management System"
echo "========================================="

# Navigate to project root
cd "$(dirname "$0")"

# Build frontend
echo ""
echo "Step 1: Building frontend..."
cd finance-frontend
npm install
npm run build
cd ..

echo ""
echo "Step 2: Building backend..."
cd finance-backend
./mvnw clean package -DskipTests -B
cd ..

echo ""
echo "========================================="
echo "Build completed successfully!"
echo "========================================="
echo ""
echo "To run the application:"
echo "  cd finance-backend && java -jar target/finance-backend-1.0.0-SNAPSHOT.jar"
echo ""
echo "Or with Docker:"
echo "  docker-compose up --build"
echo ""
