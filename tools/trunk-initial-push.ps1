<#
.SYNOPSIS
  Inicializa Git en trunk (main), crea commits incrementales por microservicio y opcionalmente hace push.

.USAGE
    cd "C:\Users\Hugo\Documents\KL_SYTEM_ECOMERCE"
    .\tools\trunk-initial-push.ps1
    .\tools\trunk-initial-push.ps1 -DoPush   # además: git push -u origin main

  Requisitos: Git en PATH y credenciales GitHub para el remoto HTTPS (o cambia $RemoteUrl a SSH).

  Trunk-based: una sola rama principal (main), commits pequeños con traza clara.
#>
param(
    [string]$RemoteUrl = "https://github.com/AdminMod18/KL_SYTEM_ECOMERCE.git",
    [switch]$DoPush
)

$ErrorActionPreference = "Stop"
$Root = Split-Path $PSScriptRoot -Parent
Set-Location $Root

$null = Get-Command git -ErrorAction Stop

if (-not (Test-Path ".git")) {
    git init
    git branch -M main
}

# Identidad solo en este repo si no está definida (evita fallos en equipos nuevos).
if (-not (git config --get user.email 2>$null)) {
    $email = if ($env:GIT_USER_EMAIL) { $env:GIT_USER_EMAIL } else { "AdminMod18@users.noreply.github.com" }
    $name = if ($env:GIT_USER_NAME) { $env:GIT_USER_NAME } else { "AdminMod18" }
    git config user.email $email
    git config user.name $name
}

$remotes = @(git remote 2>$null)
if ($remotes -notcontains "origin") {
    git remote add origin $RemoteUrl
} else {
    git remote set-url origin $RemoteUrl
}

function Commit-Staged {
    param([string]$Message)
    $staged = @(git diff --cached --name-only)
    if ($staged.Count -gt 0) {
        git commit -m $Message
    }
}

if (Test-Path ".gitignore") { git add .gitignore }
if (Test-Path "tools\trunk-initial-push.ps1") { git add "tools\trunk-initial-push.ps1" }
Commit-Staged "chore: .gitignore y script de publicación trunk a GitHub"

$services = @(
    "auth-service",
    "user-service",
    "solicitud-service",
    "validation-service",
    "payment-service",
    "product-service",
    "order-service",
    "notification-service",
    "analytics-service",
    "config-service"
)

foreach ($svc in $services) {
    if (Test-Path $svc) {
        git add $svc
        Commit-Staged "feat($svc): microservicio Spring Boot (API, OpenAPI, Docker y configuración)"
    }
}

Write-Host "Historial reciente:" -ForegroundColor Green
if (git rev-parse --verify HEAD 2>$null) {
    git log --oneline -15
}

if ($DoPush) {
    git push -u origin main
    Write-Host "Push completado a origin/main." -ForegroundColor Green
} else {
    Write-Host "Siguiente paso (publicar trunk): git push -u origin main" -ForegroundColor Cyan
    Write-Host "O vuelve a ejecutar con -DoPush" -ForegroundColor Cyan
}
