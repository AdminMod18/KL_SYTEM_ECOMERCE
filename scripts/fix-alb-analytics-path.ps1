# Regla ALB para /analytics/* → analytics-service.
# CloudFront (función strip-api-prefix) convierte /api/analytics/kpis → /analytics/kpis en el ALB.
# Sin esta regla el ALB responde 404 y CloudFront devuelve index.html del SPA.
# Uso: .\scripts\fix-alb-analytics-path.ps1

$ErrorActionPreference = 'Stop'
$region = 'us-east-2'
$listenerArn = aws elbv2 describe-listeners --region $region `
    --load-balancer-arn (aws elbv2 describe-load-balancers --region $region --names kl-ecommerce-prod-alb --query 'LoadBalancers[0].LoadBalancerArn' --output text) `
    --query "Listeners[?Port==``80``].ListenerArn | [0]" --output text

if (-not $listenerArn -or $listenerArn -eq 'None') {
    throw 'No se encontró listener HTTP:80 del ALB kl-ecommerce-prod-alb'
}

$tgArn = aws elbv2 describe-target-groups --region $region --names prod-analytic-fc5026-tg `
    --query 'TargetGroups[0].TargetGroupArn' --output text 2>$null
if (-not $tgArn -or $tgArn -eq 'None') {
    $tgArn = aws elbv2 describe-target-groups --region $region `
        --query "TargetGroups[?contains(TargetGroupName, 'analytic')].TargetGroupArn | [0]" --output text
}
if (-not $tgArn -or $tgArn -eq 'None') {
    throw 'No se encontró target group de analytics-service'
}

$existing = aws elbv2 describe-rules --region $region --listener-arn $listenerArn --output json | ConvertFrom-Json
$already = $existing.Rules | Where-Object {
    ($_.Conditions | Where-Object { $_.Field -eq 'path-pattern' -and ($_.Values -contains '/analytics' -or $_.Values -contains '/analytics/*') })
}
if ($already) {
    Write-Host "Regla /analytics/* ya existe (prioridad $($already.Priority))." -ForegroundColor Green
    exit 0
}

$used = @($existing.Rules | Where-Object { $_.Priority -ne 'default' } | ForEach-Object { [int]$_.Priority })
$priority = 95
while ($used -contains $priority) { $priority++ }

$ruleArn = aws elbv2 create-rule --region $region --listener-arn $listenerArn --priority $priority `
    --conditions Field=path-pattern,Values='/analytics','/analytics/*' `
    --actions Type=forward,TargetGroupArn=$tgArn `
    --query 'Rules[0].RuleArn' --output text

Write-Host "Creada regla ALB prioridad $priority -> analytics ($ruleArn)" -ForegroundColor Green
Write-Host 'Prueba: curl https://TU_CLOUDFRONT/api/analytics/kpis (debe ser JSON, no HTML)' -ForegroundColor Cyan
