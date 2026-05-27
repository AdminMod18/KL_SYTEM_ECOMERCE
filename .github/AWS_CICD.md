# CI/CD Backend → AWS (GitHub Actions)

**Trunk-based:** rama única `main`. CI en PR y push a `main`; CD (deploy) solo en push a `main`.

## Dónde configurar credenciales AWS (importante)

Las variables que ves en **AWS CloudShell** (`AWS_CONTAINER_CREDENTIALS_*`, `AWS_EXECUTION_ENV=CloudShell`) **no** se copian a GitHub: son credenciales temporales del contenedor CloudShell y **caducan**.

Configura en GitHub (repo **KL_SYTEM_ECOMERCE**):

1. **Settings** → **Secrets and variables** → **Actions**
2. Pestaña **Secrets** → **New repository secret**:
   - `AWS_ACCESS_KEY_ID` — Access Key de un usuario IAM (o claves temporales exportadas)
   - `AWS_SECRET_ACCESS_KEY` — Secret correspondiente
3. Pestaña **Variables** → **New repository variable**:
   - `AWS_REGION` = `us-east-2` (tu región en CloudShell)
   - `ECS_CLUSTER`, `ECS_SERVICE_PREFIX`, `ECS_TASK_FAMILY_PREFIX`, `PROJECT_NAME`, etc.

**Obtener claves para CI desde CloudShell** (usuario con permisos IAM):

```bash
# Opción 1: crear access key para usuario IAM (consola IAM → Users → Security credentials)
# Opción 2: si ya tienes un perfil, exportar credenciales actuales (válidas ~1h si son de rol):
aws configure export-credentials --format env
# Copia AWS_ACCESS_KEY_ID y AWS_SECRET_ACCESS_KEY a GitHub Secrets (no las subas al repo).
```

**No** pegues credenciales en código, `.env`, ni en el chat. Solo **GitHub Secrets**.

**OIDC (producción):** en lugar de access keys, usa secret `AWS_ROLE_ARN` (ver trust policy en `infra/github-actions/`).

Pipelines en `.github/workflows/`:

| Workflow | Disparador | Acción |
|----------|------------|--------|
| `ci.yml` | PR / push a `main` o `develop` | Tests Gradle + build Docker por microservicio **modificado** |
| `deploy.yml` | Push a `main` / manual | Build → push **ECR** → despliegue **ECS Fargate** |

## Qué necesitas en AWS (sin SSM obligatorio)

GitHub Actions **no usa** tu `~/.aws/credentials` local. Opciones:

### Opción A — OIDC (recomendada, sin access keys)

1. Crear rol IAM `GitHubActionsDeployRole` con trust policy para `token.actions.githubusercontent.com`.
2. Políticas: `AmazonEC2ContainerRegistryPowerUser` (o push solo a repos ECR del proyecto), `AmazonECS_FullAccess` (o mínimo: `ecs:UpdateService`, `ecs:RegisterTaskDefinition`, `ecs:Describe*`, `iam:PassRole` sobre task execution role).
3. En GitHub → **Settings → Secrets**: `AWS_ROLE_ARN` = ARN del rol.

Ejemplo de trust policy (ajusta `ORG/REPO`):

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::ACCOUNT_ID:oidc-provider/token.actions.githubusercontent.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
        },
        "StringLike": {
          "token.actions.githubusercontent.com:sub": "repo:AdminMod18/KL_SYTEM_ECOMERCE:*"
        }
      }
    }
  ]
}
```

### Opción B — Access keys (rápida para demo)

Secrets en GitHub:

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`

Usuario IAM con las mismas capacidades ECR + ECS.

## Variables de repositorio (Settings → Variables)

| Variable | Ejemplo | Uso |
|----------|---------|-----|
| `AWS_REGION` | `us-east-2` | Región (tu despliegue actual) |
| `PROJECT_NAME` | `kl-ecommerce` | Prefijo ECR: `kl-ecommerce/auth-service` |
| `ENVIRONMENT` | `dev` | Etiquetado / convención |
| `ECS_CLUSTER` | `kl-ecommerce-prod-cluster` | Cluster ECS |
| `ECS_SERVICE_PREFIX` | `kl-ecommerce-prod` | Servicio ECS = `{prefix}-auth-service-svc` |
| `ECS_SERVICE_SUFFIX` | `-svc` | Sufijo del servicio en ECS (Terraform) |
| `ECS_TASK_FAMILY_PREFIX` | *(opcional)* | Task family = `{prefix}-auth-service` (sin `-svc`) |

**Convención en tu cuenta (prod):**

| Micro | Servicio ECS | Task definition |
|-------|----------------|-----------------|
| auth-service | `kl-ecommerce-prod-auth-service-svc` | `kl-ecommerce-prod-auth-service` |
| user-service | `kl-ecommerce-prod-user-service-svc` | `kl-ecommerce-prod-user-service` |
| … | `kl-ecommerce-prod-{micro}-svc` | `kl-ecommerce-prod-{micro}` |

Si no defines variables, el workflow usa estos valores por defecto.
| `ECS_WAIT_STABILITY` | `false` | `true` espera estabilidad (más lento) |

Alinea nombres con los outputs de tu Terraform (`modules/ecs`).

## ECR

El push falla con `name unknown` cuando el **nombre del repositorio en ECR no coincide** con el del pipeline.

Variable **`ECR_NAMING`** (opcional):

| Valor | Ejemplo para `analytics-service` |
|-------|----------------------------------|
| `auto` (default) | Busca en orden: `analytics-service`, `kl-ecommerce-analytics-service`, `kl-ecommerce/analytics-service`; si ninguno existe, **crea el primero**. |
| `service-only` | `analytics-service` (como en muchos módulos Terraform) |
| `flat` | `kl-ecommerce-analytics-service` |
| `namespaced` | `kl-ecommerce/analytics-service` |

En consola AWS: **ECR → Repositories** y copia el nombre exacto. Si usas Terraform con nombres simples (`auth-service`, `user-service`, …), deja `ECR_NAMING=service-only` o `auto`.

El pipeline puede **crear** el repo si no existe (requiere permiso `ecr:CreateRepository` en el usuario/rol de CI).

Imágenes: etiquetas `latest` y `<sha-del-commit>`.

## ECS

Convención esperada por servicio:

| Micro | ECS service (ejemplo) | Task family (ejemplo) | Container name |
|-------|----------------------|------------------------|----------------|
| auth-service | `kl-ecommerce-dev-auth-service` | `kl-ecommerce-dev-auth-service` | `auth-service` |

Si tu Terraform usa otros nombres, ajusta `ECS_SERVICE_PREFIX` / `ECS_TASK_FAMILY_PREFIX` o edita `deploy.yml`.

## Despliegue manual de todos los micros

Actions → **Deploy Backend AWS** → **Run workflow** → `deploy_all` = `true`.

Si el workflow muestra **éxito** pero el job **deploy** aparece **omitido (skipped)**, es porque el push solo tocó documentación o `.github/` sin código de microservicios. En ese caso usa **Run workflow** con `deploy_all=true`, o haz un push que modifique al menos una carpeta `*-service/`.

Los cambios solo en `.github/**` ahora disparan deploy de **los 11** microservicios automáticamente.

## Flujo local vs cloud

- **Local:** `aws configure` + SDK en tu máquina.
- **GitHub:** solo OIDC o secrets; no hace falta SSM Parameter Store salvo que quieras guardar secretos de app en ECS (JWT, DB password) — eso va en task definition / Secrets Manager, no en el pipeline de build.

## Tareas ECS con exit code 1 (runtime, no compilación)

Si el workflow de deploy es verde pero ECS muestra **Essential container exited** / código **1**:

1. Revisa **CloudWatch Logs** → `/ecs/kl-ecommerce/prod/{micro}`.
2. Errores frecuentes vistos en prod:

| Síntoma en logs | Causa | Acción |
|-----------------|-------|--------|
| `Cannot load driver class: org.postgresql.Driver` | JAR sin dependencia `postgresql` (ej. `admin-service`) | Añadir `runtimeOnly 'org.postgresql:postgresql'` y redesplegar imagen. |
| `UnknownHostException: ...rds.amazonaws.com:5432` | URL JDBC mal formada en la task definition | Corregir Terraform: la URL debe ser `jdbc:postgresql://HOST:5432/DB`, **no** `...:5432:5432/...`. |
| `The connection attempt failed` (sin UnknownHost) | RDS inaccesible (SG, subnets, credenciales) | Revisar security groups ECS→RDS y parámetro SSM del password. |
| `Task failed ELB health checks` con `Started ...Application` en logs | ALB comprueba antes de que arranque Spring (~90–120 s) | En el servicio ECS: `health_check_grace_period_seconds` ≥ **180**. En la task definition: `healthCheck.startPeriod` ≥ **150**. |
| `Estado incorrecto` con Actuator en logs | Ruta distinta a la del ALB | Exponer `GET /admin/actuator/health` (u la ruta del target group) con `spring-boot-starter-actuator`. |

**URL incorrecta actual (ejemplo):**

```text
jdbc:postgresql://kl-ecommerce-prod-postgres....amazonaws.com:5432:5432/kl_ecommerce
```

**URL correcta:**

```text
jdbc:postgresql://kl-ecommerce-prod-postgres....amazonaws.com:5432/kl_ecommerce
```

Script de corrección puntual en todas las task definitions (una vez, hasta arreglar Terraform):

```powershell
.\scripts\fix-ecs-datasource-url.ps1
```

Luego vuelve a desplegar con GitHub Actions (`deploy_all=true`) para que las imágenes incluyan Actuator y PostgreSQL donde aplique.

### Perfil `prod` en cada micro (código)

Cada `*-service` incluye `src/main/resources/application-prod.yml` con:

- **JPA + RDS:** `postgresql` en `build.gradle`, datasource PostgreSQL, Actuator en la ruta del ALB.
- **Sin JPA:** solo Actuator en la ruta del ALB.

| Micro | Health (ALB/ECS) |
|-------|------------------|
| auth-service | `/auth/actuator/health` :9001 |
| user-service | `/users/actuator/health` :9002 |
| solicitud-service | `/solicitudes/actuator/health` :9003 |
| validation-service | `/validation/actuator/health` :9004 |
| payment-service | `/payments/actuator/health` :9005 |
| order-service | `/orders/actuator/health` :9006 |
| product-service | `/products/actuator/health` :9007 |
| notification-service | `/notifications/actuator/health` :9008 |
| analytics-service | `/analytics/actuator/health` :9009 |
| admin-service | `/admin/actuator/health` :9010 |
| config-service | `/config/actuator/health` :9011 |

Scripts puntuales en AWS (hasta corregir Terraform): `scripts/fix-ecs-datasource-url.ps1`, `scripts/fix-ecs-health-grace-period.ps1`.
