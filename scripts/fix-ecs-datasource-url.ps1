# Corrige SPRING_DATASOURCE_URL con puerto duplicado (:5432:5432) en task definitions ECS.
# Uso: .\scripts\fix-ecs-datasource-url.ps1

$ErrorActionPreference = 'Stop'
$region = 'us-east-2'
$cluster = 'kl-ecommerce-prod-cluster'
$bad = ':5432:5432/'
$good = ':5432/'
$stripKeys = @(
    'taskDefinitionArn', 'revision', 'status', 'requiresAttributes',
    'compatibilities', 'registeredAt', 'registeredBy', 'deregisteredAt'
)
$utf8 = New-Object System.Text.UTF8Encoding $false

$services = @(
    'auth-service', 'user-service', 'solicitud-service', 'validation-service',
    'payment-service', 'order-service', 'product-service', 'notification-service',
    'analytics-service', 'admin-service', 'config-service'
)

foreach ($svc in $services) {
    $ecsSvc = "kl-ecommerce-prod-$svc-svc"
    Write-Host "`n--- $svc ---" -ForegroundColor Cyan

    $tdArn = aws ecs describe-services --cluster $cluster --services $ecsSvc --region $region `
        --query 'services[0].taskDefinition' --output text 2>$null
    if (-not $tdArn -or $tdArn -eq 'None') {
        Write-Host "Servicio no encontrado: $ecsSvc" -ForegroundColor Yellow
        continue
    }

    $full = aws ecs describe-task-definition --task-definition $tdArn --region $region --output json | ConvertFrom-Json
    $raw = $full.taskDefinition | ConvertTo-Json -Depth 30 -Compress
    if ($raw -notlike "*$bad*") {
        Write-Host "URL ya correcta" -ForegroundColor Gray
        continue
    }

    $obj = $raw.Replace($bad, $good) | ConvertFrom-Json
    foreach ($k in $stripKeys) { $obj.PSObject.Properties.Remove($k) | Out-Null }

    $tmp = Join-Path $env:TEMP "ecs-td-$svc.json"
    $jsonPath = $tmp -replace '\\', '/'
    [System.IO.File]::WriteAllText($tmp, ($obj | ConvertTo-Json -Depth 30 -Compress), $utf8)

    $newArn = aws ecs register-task-definition --region $region --cli-input-json "file://$jsonPath" `
        --query 'taskDefinition.taskDefinitionArn' --output text
    aws ecs update-service --cluster $cluster --service $ecsSvc --task-definition $newArn `
        --region $region --force-new-deployment --output text --query 'service.serviceName' | Out-Null
    Write-Host "Desplegado: $newArn" -ForegroundColor Green
    Remove-Item $tmp -ErrorAction SilentlyContinue
}

Write-Host "`nListo. Revisa tareas en ECS y logs en CloudWatch." -ForegroundColor Cyan
