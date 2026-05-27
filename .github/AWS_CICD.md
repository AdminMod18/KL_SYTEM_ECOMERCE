# CI/CD Backend → AWS (GitHub Actions)

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
| `AWS_REGION` | `us-east-1` | Región |
| `PROJECT_NAME` | `kl-ecommerce` | Prefijo ECR: `kl-ecommerce/auth-service` |
| `ENVIRONMENT` | `dev` | Etiquetado / convención |
| `ECS_CLUSTER` | `kl-ecommerce-dev-cluster` | Cluster ECS (output Terraform) |
| `ECS_SERVICE_PREFIX` | `kl-ecommerce-dev` | Servicio ECS = `{prefix}-auth-service` |
| `ECS_TASK_FAMILY_PREFIX` | `kl-ecommerce-dev` | Task family = `{prefix}-auth-service` |
| `ECS_WAIT_STABILITY` | `false` | `true` espera estabilidad (más lento) |

Alinea nombres con los outputs de tu Terraform (`modules/ecs`).

## ECR

Por cada microservicio debe existir el repositorio (Terraform `modules/ecr`):

- `kl-ecommerce/auth-service`
- `kl-ecommerce/user-service`
- … (11 repos)

El pipeline etiqueta imágenes con `latest` y el SHA del commit (`github.sha`).

## ECS

Convención esperada por servicio:

| Micro | ECS service (ejemplo) | Task family (ejemplo) | Container name |
|-------|----------------------|------------------------|----------------|
| auth-service | `kl-ecommerce-dev-auth-service` | `kl-ecommerce-dev-auth-service` | `auth-service` |

Si tu Terraform usa otros nombres, ajusta `ECS_SERVICE_PREFIX` / `ECS_TASK_FAMILY_PREFIX` o edita `deploy.yml`.

## Despliegue manual de todos los micros

Actions → **Deploy Backend AWS** → **Run workflow** → `deploy_all` = `true`.

## Flujo local vs cloud

- **Local:** `aws configure` + SDK en tu máquina.
- **GitHub:** solo OIDC o secrets; no hace falta SSM Parameter Store salvo que quieras guardar secretos de app en ECS (JWT, DB password) — eso va en task definition / Secrets Manager, no en el pipeline de build.
