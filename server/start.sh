#!/bin/bash
set -e

cd "$(dirname "$0")"

echo "Starting DailyTracker server..."
docker-compose up -d

echo ""
echo "Server is running at http://localhost:8080"
echo "PostgreSQL is running at localhost:5432"
echo ""
echo "To view logs: docker-compose -f $(pwd)/docker-compose.yml logs -f"
echo "To stop:      ./stop.sh"
