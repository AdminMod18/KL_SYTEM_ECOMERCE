# Arranca auth, payment, order y product (product con perfil demo) en ventanas minimizadas.
# Detén con Ctrl+C en cada ventana o cierra el proceso java de cada servicio.

$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
if (-not (Test-Path "$root\auth-service\gradlew.bat")) {
    throw "No se encontro auth-service bajo $root"
}
Write-Host "Raiz proyecto: $root"

function Start-Svc([string]$dir, [hashtable]$ExtraEnv) {
    $path = Join-Path $root $dir
    $envLines = @()
    foreach ($k in $ExtraEnv.Keys) {
        $v = $ExtraEnv[$k]
        $envLines += "`$env:$k='$v'"
    }
    $envPrefix = if ($envLines.Count) { ($envLines -join "; ") + "; " } else { "" }
    $cmd = "Set-Location '$path'; $envPrefix .\gradlew.bat bootRun --no-daemon"
    Start-Process powershell -ArgumentList @("-NoProfile", "-Command", $cmd) -WindowStyle Minimized
    Write-Host "Iniciado: $dir"
}

Start-Svc "auth-service" @{}
Start-Svc "payment-service" @{}
Start-Svc "order-service" @{}
Start-Svc "product-service" @{ SPRING_PROFILES_ACTIVE = "demo" }

Write-Host "Espera ~60-90 s a que arranquen los cuatro, luego ejecuta: .\scripts\e2e-compra.ps1"
