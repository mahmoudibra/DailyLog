#!/bin/bash
set -e

cd "$(dirname "$0")"

echo "Stopping DailyTracker server..."
docker-compose down

echo "Server stopped."
