param(
  [Parameter(Mandatory = $true)]
  [string]$Namespace,

  [Parameter(Mandatory = $false)]
  [string]$Tag = "latest"
)

$ErrorActionPreference = "Stop"
$builder = if ($env:BUILDX_BUILDER) { $env:BUILDX_BUILDER } else { "readme-multiarch" }

docker buildx inspect $builder *> $null
if ($LASTEXITCODE -ne 0) {
  docker buildx create --name $builder --use | Out-Null
} else {
  docker buildx use $builder | Out-Null
}

docker buildx inspect --bootstrap | Out-Null

docker buildx bake `
  --file docker-bake.hcl `
  --set "*.output=type=registry" `
  --set "*.platform=linux/amd64,linux/arm64" `
  --set "backend.tags=$Namespace/readme-backend:$Tag" `
  --set "frontend.tags=$Namespace/readme-frontend:$Tag"
