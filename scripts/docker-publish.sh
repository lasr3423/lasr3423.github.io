#!/usr/bin/env bash

set -euo pipefail

if [[ $# -lt 1 || $# -gt 2 ]]; then
  echo "Usage: ./scripts/docker-publish.sh <dockerhub-namespace> [image-tag]"
  exit 1
fi

namespace="$1"
tag="${2:-latest}"
builder="${BUILDX_BUILDER:-readme-multiarch}"

if ! command -v docker >/dev/null 2>&1; then
  echo "docker command not found"
  exit 1
fi

if ! docker buildx inspect "$builder" >/dev/null 2>&1; then
  docker buildx create --name "$builder" --use
else
  docker buildx use "$builder"
fi

docker buildx inspect --bootstrap >/dev/null

docker buildx bake \
  --file docker-bake.hcl \
  --set "*.output=type=registry" \
  --set "*.platform=linux/amd64,linux/arm64" \
  --set backend.tags="${namespace}/readme-backend:${tag}" \
  --set frontend.tags="${namespace}/readme-frontend:${tag}"
