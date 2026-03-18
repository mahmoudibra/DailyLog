#!/bin/bash
set -e

cd "$(dirname "$0")"

if [ "$1" = "--fresh" ]; then
    echo "Starting DailyTracker server (fresh build, no cache)..."
    docker-compose build --no-cache
    docker-compose up -d
else
    echo "Starting DailyTracker server..."
    docker-compose up -d --build
fi

echo ""
echo "Server is running at http://localhost:8080"
echo "PostgreSQL is running at localhost:5432"
echo ""
echo "To view logs: docker-compose -f $(pwd)/docker-compose.yml logs -f"
echo "To stop:      ./stop.sh"
