#!/bin/bash

# Script to start SonarQube with Docker for FileSelector project
# Usage: ./start-sonarqube.sh

set -e

echo "=========================================="
echo "Starting SonarQube Server"
echo "=========================================="
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Error: Docker is not running!"
    echo ""
    echo "Please start Docker and try again:"
    echo "  - Windows/Mac: Start Docker Desktop"
    echo "  - Linux: sudo systemctl start docker"
    exit 1
fi

# Check if sonarqube container already exists
if docker ps -a --format '{{.Names}}' | grep -q '^sonarqube$'; then
    echo "SonarQube container already exists."

    # Check if it's running
    if docker ps --format '{{.Names}}' | grep -q '^sonarqube$'; then
        echo "✅ SonarQube is already running!"
    else
        echo "Starting existing SonarQube container..."
        docker start sonarqube
        echo "✅ SonarQube started!"
    fi
else
    echo "Creating new SonarQube container..."
    docker run -d --name sonarqube \
        -p 9000:9000 \
        -v sonarqube_data:/opt/sonarqube/data \
        -v sonarqube_extensions:/opt/sonarqube/extensions \
        -v sonarqube_logs:/opt/sonarqube/logs \
        sonarqube:latest

    echo "✅ SonarQube container created!"
    echo ""
    echo "⏳ Waiting for SonarQube to start (this may take 1-2 minutes)..."
fi

echo ""
echo "Checking SonarQube status..."
COUNTER=0
MAX_TRIES=60

while [ $COUNTER -lt $MAX_TRIES ]; do
    if docker logs sonarqube 2>&1 | grep -q "SonarQube is operational"; then
        echo "✅ SonarQube is ready!"
        break
    fi

    if [ $COUNTER -eq 0 ]; then
        echo -n "Waiting"
    fi
    echo -n "."
    sleep 2
    COUNTER=$((COUNTER + 1))
done

echo ""
echo ""

if [ $COUNTER -eq $MAX_TRIES ]; then
    echo "⚠️  SonarQube is taking longer than expected to start."
    echo "Check logs with: docker logs sonarqube"
else
    echo "=========================================="
    echo "SonarQube is Ready!"
    echo "=========================================="
    echo ""
    echo "📊 Access SonarQube at: http://localhost:9000"
    echo ""
    echo "🔐 Default login credentials:"
    echo "   Username: admin"
    echo "   Password: admin"
    echo ""
    echo "📝 Next steps:"
    echo "   1. Open http://localhost:9000 in your browser"
    echo "   2. Login and change the default password"
    echo "   3. Create a project: 'File Selector' with key 'fileselector'"
    echo "   4. Generate a token"
    echo "   5. Run: ./gradlew qualityCheck"
    echo "   6. Run: ./gradlew sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.token=YOUR_TOKEN"
    echo ""
    echo "To stop SonarQube: docker stop sonarqube"
    echo "To view logs: docker logs sonarqube"
    echo "=========================================="
fi
