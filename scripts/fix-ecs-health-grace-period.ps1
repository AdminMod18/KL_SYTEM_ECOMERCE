# Ajusta healthCheckGracePeriodSeconds en servicios ECS con ALB (Spring Boot tarda ~2 min en arrancar).
# Uso: .\scripts\fix-ecs-health-grace-period.ps1 [-GraceSeconds 180]

param([int]$GraceSeconds = 180)

$ErrorActionPreference = 'Stop'
$region = 'us-east-2'
$cluster = 'kl-ecommerce-prod-cluster'

$services = @(
    'auth-service', 'user-service', 'solicitud-service', 'validation-service',
    'payment-service', 'order-service', 'product-service', 'notification-service',
    'analytics-service', 'admin-service', 'config-service'
)

foreach ($svc in $services) {
    $ecsSvc = "kl-ecommerce-prod-$svc-svc"
    Write-Host "--- $ecsSvc (grace=${GraceSeconds}s) ---" -ForegroundColor Cyan
    try {
        aws ecs update-service `
            --cluster $cluster `
            --service $ecsSvc `
            --health-check-grace-period-seconds $GraceSeconds `
            --region $region `
            --query 'service.{name:serviceName,grace:healthCheckGracePeriodSeconds}' `
            --output json | Write-Host
    } catch {
        Write-Host "Omitido o error: $_" -ForegroundColor Yellow
    }
}

Write-Host "`nListo. En Terraform: health_check_grace_period_seconds = $GraceSeconds en cada aws_ecs_service con load_balancer." -ForegroundColor Cyan
