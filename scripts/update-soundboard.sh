#!/bin/bash

echo "Rebuilding docker image"
docker compose build soundboard
docker compose up -d --no-deps soundboard

echo "Soundboard application updated! The Soundboard application should now be running with the latest changes."
