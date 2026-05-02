# Flujo E2E: auth -> productos -> orden -> pago
# Requisitos: servicios en 9001, 9005, 9006, 9007. product-service con SPRING_PROFILES_ACTIVE=demo (catálogo sembrado).

$ErrorActionPreference = "Stop"

function Wait-OpenApi([string]$baseUrl, [string]$label, [int]$timeoutSec = 180) {
    $uri = "$baseUrl/v3/api-docs"
    $deadline = (Get-Date).AddSeconds($timeoutSec)
    while ((Get-Date) -lt $deadline) {
        try {
            Invoke-WebRequest -Uri $uri -UseBasicParsing -TimeoutSec 3 | Out-Null
            Write-Host "[listo] $label" -ForegroundColor DarkGray
            return
        } catch {
            Start-Sleep -Seconds 2
        }
    }
    throw "Timeout esperando OpenAPI: $label ($uri)"
}

Write-Host "Esperando servicios (OpenAPI)..." -ForegroundColor DarkGray
Wait-OpenApi "http://127.0.0.1:9001" "auth-service"
Wait-OpenApi "http://127.0.0.1:9005" "payment-service"
Wait-OpenApi "http://127.0.0.1:9006" "order-service"
Wait-OpenApi "http://127.0.0.1:9007" "product-service"
$baseAuth = "http://127.0.0.1:9001"
$basePay = "http://127.0.0.1:9005"
$baseOrder = "http://127.0.0.1:9006"
$baseProduct = "http://127.0.0.1:9007"

Write-Host "=== 1) Login ===" -ForegroundColor Cyan
$loginBody = '{"username":"admin","password":"admin123"}'
$login = Invoke-RestMethod -Uri "$baseAuth/auth/login" -Method Post -ContentType "application/json; charset=utf-8" -Body $loginBody
$null = $login.accessToken
Write-Host "Login OK (roles: $($login.roles -join ', '))"

Write-Host "=== 2) Productos ===" -ForegroundColor Cyan
$productos = Invoke-RestMethod -Uri "$baseProduct/productos" -Method Get
if ($productos.Count -lt 1) {
    throw "No hay productos. Levanta product-service con SPRING_PROFILES_ACTIVE=demo"
}
$p0 = $productos[0]
Write-Host "Producto id=$($p0.id) nombre=$($p0.nombre) precio=$($p0.precio)"
$sku = "DEMO-$($p0.id)"

Write-Host "=== 3) Orden (2 x precio del producto) ===" -ForegroundColor Cyan
$precio = [decimal]$p0.precio
$ordenBodyObj = @{
    clienteId = "cli-e2e-001"
    lineas    = @(
        @{ sku = $sku; cantidad = 2; precioUnitario = $precio }
    )
}
$ordenBody = $ordenBodyObj | ConvertTo-Json -Depth 5
$orden = Invoke-RestMethod -Uri "$baseOrder/orden" -Method Post -ContentType "application/json; charset=utf-8" -Body $ordenBody
Write-Host ($orden | ConvertTo-Json -Compress)

Write-Host "=== 4) Verificar desglose ===" -ForegroundColor Cyan
$expectedSub = 2 * $precio
$tax = [decimal]0.19
$comm = [decimal]0.05
$ship = [decimal]5.00
$expIva = [math]::Round($expectedSub * $tax, 4)
$expCom = [math]::Round($expectedSub * $comm, 4)
$expTotal = [math]::Round($expectedSub + $expIva + $expCom + $ship, 4)

function Assert-DecimalEq([string]$name, $a, $b) {
    $da = [decimal]$a
    $db = [decimal]$b
    if ($da -ne $db) { throw "Mismatch ${name}: esperado $db obtenido $da" }
}

Assert-DecimalEq "subtotalBase" $orden.subtotalBase $expectedSub
Assert-DecimalEq "montoIva" $orden.montoIva $expIva
Assert-DecimalEq "montoComision" $orden.montoComision $expCom
Assert-DecimalEq "montoEnvio" $orden.montoEnvio $ship
Assert-DecimalEq "total" $orden.total $expTotal
Write-Host "Desglose y total validados. total=$expTotal"

Write-Host "=== 5) Pago (monto = total) ===" -ForegroundColor Cyan
$pagoMonto = [decimal]$orden.total
$pagoBodyObj = @{
    tipo                           = "CONSIGNACION"
    monto                          = $pagoMonto
    referenciaCliente              = "ORDEN-$($orden.ordenId)"
    numeroComprobanteConsignacion  = "COMP-E2E-$($orden.ordenId)"
}
$pagoBody = $pagoBodyObj | ConvertTo-Json -Depth 5
$pago = Invoke-RestMethod -Uri "$basePay/pagos" -Method Post -ContentType "application/json; charset=utf-8" -Body $pagoBody
Write-Host ($pago | ConvertTo-Json -Compress)
if ([decimal]$pagoMonto -ne [decimal]$orden.total) { throw "Monto enviado no coincide con total de orden" }
if ($pago.estado -ne "RECIBIDO") { throw "Pago no en estado RECIBIDO: $($pago.estado)" }

Write-Host "`n=== E2E compra OK ===" -ForegroundColor Green
Write-Host "Revisa logs: auth (login), product (demo), order (Orden creada...), payment (Pago procesado...)"
