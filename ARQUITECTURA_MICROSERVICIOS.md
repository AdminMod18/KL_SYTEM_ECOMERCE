# Arquitectura y microservicios — KL System Ecommerce (backend)

**Propósito:** documentar de forma homogénea **todos los microservicios** del repositorio, el **patrón arquitectónico base**, la **organización por capas/paquetes** y los **patrones de diseño** visibles en el código, para sustentación técnica y onboarding.

**Ámbito:** monorepo Gradle con proyectos Spring Boot independientes (`*-service`). El frontend vive en el repositorio `KL_SYTEM_ECOMERCE_FRONTEND`.

**Última revisión:** mayo de 2026.

---

## 1. Visión general

### 1.1 Estilo arquitectónico

- **Microservicios por dominio:** cada carpeta `*-service` es un desplegable autónomo (proceso JVM propio, puerto propio, ciclo de vida propio).
- **Backend for Frontend / API agregada en cliente:** el SPA suele usar una **base URL única** (proxy Vite o API Gateway) que enruta `/auth`, `/usuarios`, `/productos`, etc. hacia el micro correspondiente.
- **Arquitectura en capas (clean-ish / hexagonal light):** en todos los servicios se repite una separación conceptual coherente:
  - **Adaptador de entrada:** `controller` — REST, validación de DTOs (`jakarta.validation`), contratos HTTP.
  - **Aplicación / dominio:** `service`, `domain`, `command`, `facade`, `state`, etc. — reglas de negocio y orquestación.
  - **Adaptador de salida:** `repository` (Spring Data JPA), `integration` / `adapter` / `client` / `gateway` (HTTP hacia otros micros).
  - **Contratos API:** `dto` (entrada/salida), `entity` (persistencia donde aplica).
  - **Infra transversal:** `config`, `exception/GlobalExceptionHandler`, `OpenApiConfig`.

No todos los servicios tienen **todas** las carpetas; la tabla por micro (sección 4) detalla qué aplica.

### 1.2 Stack técnico común

| Aspecto | Elección habitual |
|--------|-------------------|
| Lenguaje / runtime | Java + Spring Boot |
| Build | Gradle (`build.gradle` por servicio) |
| API | REST + OpenAPI 3 (`/v3/api-docs`, Swagger UI donde está configurado) |
| Persistencia | PostgreSQL + Spring Data JPA en los servicios que tienen BD |
| Contenedor | `Dockerfile` por servicio; orquestación local en `docker-compose.yml` (raíz) |
| Errores HTTP | `GlobalExceptionHandler` + respuestas Problem Details / cuerpos coherentes según servicio |

### 1.3 Puertos en `docker-compose.yml` (referencia)

| Puerto | Servicio |
|--------|----------|
| 5432 | PostgreSQL |
| 9001 | auth-service |
| 9002 | user-service |
| 9003 | solicitud-service |
| 9004 | validation-service |
| 9005 | payment-service |
| 9006 | order-service |
| 9007 | product-service |
| 9008 | notification-service |
| 9009 | analytics-service |
| 9010 | admin-service |

**Nota:** `config-service` existe como módulo Gradle pero **no** está incluido en el `docker-compose.yml` actual; puede ejecutarse aparte para demos de configuración singleton.

---

## 2. Patrones de diseño y arquitectura (por frecuencia en el repo)

| Patrón / concepto | Dónde aparece (ejemplos) |
|-------------------|---------------------------|
| **Strategy + Factory Method** | `payment-service`: `PaymentStrategy`, `PaymentStrategyFactory`, estrategias `CONSIGNACION`, `ONLINE`, `TARJETA`. |
| **Command + Invoker** | `order-service`: `ComandoOrden`, `ComandoColocarOrden`, `InvocadorComandosOrden`. |
| **Decorator / cadena de precios** | `order-service`: `CalculadorPrecioOrden` y decoradores (`ImpuestoVentaDecorador`, `ComisionMarketplaceDecorador`, `CargoLogisticoDecorador`, políticas de envío en `pricing/shipping`). |
| **State** | `solicitud-service`: estados de solicitud (`PendienteEstado`, `AprobadaEstado`, `ActivaEstado`, `DevueltaEstado`, etc.) y `SolicitudEstadoBehavior`. |
| **Chain of Responsibility** | `solicitud-service`: cadena de validación (`SolicitudValidationHandler` y handlers encadenados en `chain`). |
| **Builder** | `solicitud-service` (`SolicitudRegistroBuilder`); `product-service` (`ProductoBuilder`). |
| **Facade** | `validation-service` (`ValidacionCrediticiaFacade`); `solicitud-service` (`ConsultaSolicitudesDirectorFacade`). |
| **Adapter / Puerto** | `validation-service`: `port` + `adapter` (Datacrédito, CIFIN, judicial mock); `product-service`: `VendedorActivoPort` + gateway HTTP. |
| **Observer** | `notification-service`: `NotificacionObserver`, `NotificacionEventSubject`, observers por tipo de evento de solicitud/compra. |
| **Singleton (GoF / registro)** | `config-service`: `ConfigServiceRegistrySingleton` + API de metadatos. |
| **Composite** | `product-service`: jerarquía de categorías (`CategoriaComposite`, `CategoriaLeaf`, `CategoriaComponent`). |
| **Factory** | `user-service`: `UsuarioEntidadFactory`; `solicitud-service`: `SolicitudAdjuntoFactory`. |
| **Integration gateway / REST client** | Varios: `solicitud-service/integration`, `auth-service/integration`, `product-service/integration`. |

---

## 3. Organización típica de paquetes (`com.marketplace.<dominio>`)

Estructura **referencial** (ajustar según micro):

```
controller/     → REST
service/        → casos de uso / aplicación
repository/     → JPA
entity/         → entidades persistidas
dto/            → request/response API
domain/         → modelos de dominio (cuando no son solo entidades JPA)
config/         → beans, OpenAPI, seguridad, RestClient
exception/      → manejadores y excepciones de negocio
integration/    → clientes HTTP a otros micros (nombre variable: adapter, gateway, client)
```

---

## 4. Ficha por microservicio

### 4.1 auth-service (9001)

| Campo | Detalle |
|-------|---------|
| **Responsabilidad** | Autenticación: emisión/validación conceptual de JWT, login delegando verificación de credenciales, roles, sincronización con flujo vendedor/solicitud según configuración. |
| **BD propia** | No (delega en **user-service** y consultas a **solicitud-service** vía clientes HTTP). |
| **Capas principales** | `controller`, `service` (`AuthService`, `AuthServiceImpl`, `AuthServiceProxy`), `security` (`JwtTokenProvider`), `integration` (puentes a usuario/solicitud), `dto`, `exception`. |
| **Patrones / notas** | Fachada/proxy sobre implementación real; **integración** como adaptadores HTTP (`UserServiceRegisteredLoginBridge`, clientes de roles y promoción vendedor). |
| **Documentación API** | `OpenApiConfig` + controlador `AuthController`. |

---

### 4.2 user-service (9002)

| Campo | Detalle |
|-------|---------|
| **Responsabilidad** | Ciclo de vida de usuarios compradores/vendedores a nivel cuenta: alta, consulta, actualización; endpoints **internos** para verificación de credenciales y promoción de rol vendedor (consumidos por auth/solicitud). |
| **BD** | PostgreSQL (perfil `docker` en compose). |
| **Capas principales** | `controller` (público + `InternoCredencialesController`, `InternoVendedorController`), `service`, `repository`, `entity`, `dto` (+ `dto/interno`), `factory`, `config` (`PasswordEncoderConfig`). |
| **Patrones / notas** | **Factory** de entidad usuario; separación explícita API **interna** vs pública para micro-a-micro. |

---

### 4.3 solicitud-service (9003)

| Campo | Detalle |
|-------|---------|
| **Responsabilidad** | Proceso de **solicitud de vendedor**: creación, validaciones, transiciones de estado, adjuntos, activación con pago, reputación/calificaciones, suscripción/mora (según reglas del caso de estudio), listados tipo director. |
| **BD** | PostgreSQL. |
| **Capas principales** | `controller`, `service`, `repository`, `entity`, `dto`, `state`, `chain`, `command`, `builder`, `factory`, `facade`, `integration`, `schedule`, `notification`, `storage`, `exception`. |
| **Patrones / notas** | **State** para transiciones; **Chain of Responsibility** en validación; **Command** en validación de creación; **Builder** en registro; **Facade** para consultas director; **Gateways** HTTP a validation, payment, notification, admin, user. |
| **Integraciones declaradas en compose** | validation (9004), payment (9005), notification (9008), admin (9010), user (9002). |

---

### 4.4 validation-service (9004)

| Campo | Detalle |
|-------|---------|
| **Responsabilidad** | Validación crediticia/policía para onboarding vendedor: orquestación Datacrédito/CIFIN (mock/adapters), políticas de umbral y clasificación, mock judicial. |
| **BD** | Sin persistencia pesada obligatoria en el árbol revisado (enfoque en procesamiento y mocks). |
| **Capas principales** | `controller`, `facade`, `policy`, `port`, `adapter`, `domain`, `dto`, `mock`, `config`, `exception`. |
| **Patrones / notas** | **Facade** (`ValidacionCrediticiaFacade`); **Ports & Adapters**; **Policy/Strategy** de clasificación (`PoliticaEstadoVendedor`, clasificadores por proveedor). |

---

### 4.5 payment-service (9005)

| Campo | Detalle |
|-------|---------|
| **Responsabilidad** | Cobro unificado `POST /pagos`: consignación, online simulado, tarjeta (demo). |
| **BD** | No en el diseño actual (respuesta de negocio en memoria/simulada). |
| **Capas principales** | `controller`, `service` (`PagoService`), `strategy`, `factory`, `dto`, `model` (`TipoPago`), `exception`. |
| **Patrones / notas** | **Strategy** por tipo de pago; **Factory** para resolver estrategia; caso de uso acotado y muy legible para sustentación académica. |

---

### 4.6 order-service (9006)

| Campo | Detalle |
|-------|---------|
| **Responsabilidad** | Creación de **órdenes de compra**, cálculo de totales (subtotal, IVA, comisión, envío), persistencia y consulta por cliente (`GET /orden(s)?clienteId=`). |
| **BD** | PostgreSQL. |
| **Capas principales** | `controller`, `service` (`OrdenApplicationService`), `command`, `domain`, `pricing` (+ `shipping`), `repository`, `entity`, `dto`, `config`, `exception`. |
| **Patrones / notas** | **Command** para colocar orden; **Decorator/cadena** para precios y cargos; **Desglose** explícito (`DesglosePrecioOrden`, calculadora). |

---

### 4.7 product-service (9007)

| Campo | Detalle |
|-------|---------|
| **Responsabilidad** | Catálogo de productos, altas, consultas; reglas de visibilidad ligadas a vendedor activo (integración solicitud); interacciones comunidad (preguntas/respuestas demo según implementación). |
| **BD** | PostgreSQL + scripts/migraciones en `resources/db` si aplica. |
| **Capas principales** | `controller`, `service`, `repository`, `entity`, `dto`, `builder`, `category` (composite), `integration`, `model`, `config`, `exception`. |
| **Patrones / notas** | **Builder** de producto; **Composite** de categorías; **Port + Gateway HTTP** para consultar estado vendedor en solicitud-service. |

---

### 4.8 notification-service (9008)

| Campo | Detalle |
|-------|---------|
| **Responsabilidad** | Registro/difusión de **eventos de notificación** (HTTP); modelo extensible con observers por tipo de evento (solicitud aprobada, mora, devolución, etc.). |
| **BD** | No impuesta en el diseño base revisado. |
| **Capas principales** | `controller`, `service`, `observer`, `dto`, `config`, `exception`. |
| **Patrones / notas** | **Observer** + **Subject** (`NotificacionEventSubject`); desacoplamiento entre publicación de evento y reacciones. |

---

### 4.9 analytics-service (9009)

| Campo | Detalle |
|-------|---------|
| **Responsabilidad** | Ingesta/consulta de **métricas/KPIs** (eventos de métrica) para vistas tipo BAM/dashboard. |
| **BD** | PostgreSQL (perfil docker). |
| **Capas principales** | `controller`, `service`, `repository`, `entity`, `dto`, `model`, `config`, `exception`. |
| **Patrones / notas** | CRUD + agregaciones de lectura; capas clásicas **controller → service → repository**. |

---

### 4.10 admin-service (9010)

| Campo | Detalle |
|-------|---------|
| **Responsabilidad** | Parámetros de sistema, auditoría y logs **demo** para panel administrativo; seed inicial opcional. |
| **BD** | En memoria o ligera según configuración del módulo (revisar `application` del servicio). |
| **Capas principales** | `controller`, `service`, `repository`, `entity`, `dto`, `config` (`AdminSeedRunner`), `exception`. |
| **Patrones / notas** | Servicio de soporte operativo; consumido por solicitud-service para sincronización de parámetros. |

---

### 4.11 config-service (puerto según `application`; no en compose raíz)

| Campo | Detalle |
|-------|---------|
| **Responsabilidad** | Demostración de **metadatos singleton** y registro de configuración (`SingletonMetadatosController`). |
| **Capas principales** | `controller`, `singleton`, `config`. |
| **Patrones / notas** | **Singleton** explícito (`ConfigServiceRegistrySingleton`) — útil para explicar patrón creacional en sustentación. |

---

## 5. Comunicación entre microservicios (resumen)

- **Síncrona HTTP:** `RestClient` / propiedades `INTEGRACION_*_BASE_URL`, `USER_SERVICE_BASE_URL`, etc. (ver variables en `docker-compose.yml`).
- **Consistencia:** cada servicio posee su propio modelo de datos donde hay BD; no hay “tabla compartida” entre micros fuera de la convención de mismo servidor Postgres con **schemas/bases** definidos por aplicación (en compose se usa una base `kl_db` — en producción conviene separar por schema o instancia según política).
- **Seguridad micro-a-micro:** secretos internos de ejemplo (`USER_SERVICE_INTERNAL_SECRET`) en compose — en producción usar vault, mTLS o tokens de servicio.

---

## 6. Cómo sustentar cada micro (guía breve)

1. **Contexto delimitado:** qué problema de negocio resuelve (una frase).
2. **API:** abrir OpenAPI del servicio (`/v3/api-docs` o Swagger UI) y listar recursos principales.
3. **Flujo feliz:** desde `Controller` → `Service` / `Facade` / `Command` → `Repository` o `Integration`.
4. **Patrones:** señalar 1–2 patrones del código real (clases concretas citadas en la sección 4).
5. **Despliegue:** puerto, dependencias en compose, variables críticas.
6. **Pruebas:** localizar `src/test` del servicio (`*IntegrationTest`, tests unitarios de políticas/fachadas).

---

## 7. Referencias de archivos clave en raíz del repo

| Archivo | Uso |
|---------|-----|
| `docker-compose.yml` | Mapa de servicios, puertos, variables de integración |
| `ESTADO_PROYECTO.md` | Estado funcional front/back (actualizar si cambia el alcance) |
| `*/Dockerfile` | Imagen por microservicio |
| `*/src/main/resources/application.yml` | Configuración por servicio (perfil local) |
| `*/src/main/resources/application-docker.yml` | Configuración cuando `SPRING_PROFILES_ACTIVE=docker` |

---

*Documento pensado para sustentación académica y documentación de equipo. Mantener alineado cuando se agreguen microservicios o se modifique el compose.*
