variable "DOCKER_IMAGE_NAMESPACE" {
  default = "your-dockerhub-id"
}

variable "IMAGE_TAG" {
  default = "latest"
}

group "default" {
  targets = ["backend", "frontend"]
}

target "backend" {
  context = "./readme"
  dockerfile = "Dockerfile"
  platforms = ["linux/amd64", "linux/arm64"]
  tags = ["${DOCKER_IMAGE_NAMESPACE}/readme-backend:${IMAGE_TAG}"]
}

target "frontend" {
  context = "./readme_vue"
  dockerfile = "Dockerfile"
  platforms = ["linux/amd64", "linux/arm64"]
  args = {
    VITE_API_BASE_URL = "/api"
  }
  tags = ["${DOCKER_IMAGE_NAMESPACE}/readme-frontend:${IMAGE_TAG}"]
}
