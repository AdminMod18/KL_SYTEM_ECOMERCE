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
| `ECS_CLUSTER` | `kl-ecommerce-dev-cluster` | Cluster ECS (output Terraform) |
| `ECS_SERVICE_PREFIX` | `kl-ecommerce-dev` | Servicio ECS = `{prefix}-auth-service` |
| `ECS_TASK_FAMILY_PREFIX` | `kl-ecommerce-dev` | Task family = `{prefix}-auth-service` |
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

## Flujo local vs cloud

- **Local:** `aws configure` + SDK en tu máquina.
- **GitHub:** solo OIDC o secrets; no hace falta SSM Parameter Store salvo que quieras guardar secretos de app en ECS (JWT, DB password) — eso va en task definition / Secrets Manager, no en el pipeline de build.
