#!/bin/bash

echo "Rebuilding docker image"
docker compose up -d --build --no-deps soundboard

echo "Soundboard application updated! The Soundboard application should now be running with the latest changes."
