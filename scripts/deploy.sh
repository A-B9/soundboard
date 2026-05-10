#!/bin/bash

echo "Stopping and removing any existing containers"
docker compose down

echo "Building docker image"
docker build -t soundboard:latest .

echo "Running docker container in detached mode"
docker compose up -d

echo "Deployment complete! The Soundboard application should now be running in a Docker container. You can access it at http://localhost:8080."
