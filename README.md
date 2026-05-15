# Proselyte System - учебный проект (Individuals API + Persons Service)

**Стек**: Java 24, Spring Boot 3.5, WebFlux, Spring Security (JWT, OAuth2 Resource Server), OpenFeign, MapStruct, Lombok, Springdoc OpenAPI, Actuator + Micrometer Prometheus, OpenTelemetry (OTLP), Testcontainers, Docker Compose, Grafana + Prometheus + Tempo + Loki, Keycloak.

> Репозиторий предназначен для обучения: демонстрирует аутентификацию через Keycloak, оркестрацию регистрации пользователя и хранение персональных данных в отдельном сервисе, а также базовую наблюдаемость (метрики и трассировки).
---
## Содержание
* Архитектура
* Состав репозитория
* Быстрый старт
* Ручной запуск (без Makefile)
* Порты и сервисы
* Конфигурация (ENV/`application.yml`)
* API и OpenAPI
* Наблюдаемость
* Тестирование
* Ветки и стратегия
* Типовые ошибки и устранение
* Структура каталогов
---
## Архитектура
## Диаграммы архитектуры (C4 + PlantUML)

Файлы находятся в каталоге `architecture/`:
- **Container (уровень системы)** - [architecture/container.puml](architecture/container.puml)
- **Component - Individuals API** - [architecture/individuals-api-component.puml](architecture/individuals-api-component.puml)

Рендерить можно любым PlantUML‑совместимым плагином/CI, файлы используют библиотеку **C4‑PlantUML** (включение по URL).

**Регистрация пользователя** (упрощенно):
1. `POST /v1/auth/registration` -> Individuals API валидирует запрос.
2.  API создаёт пользователя в Keycloak и получает `access_token/refresh_token`.

**Метрики**: Actuator + Micrometer экспонирует данные для Prometheus. Есть кастомный счётчик логинов: `individual_app_login_count_total` (см. `LoginMetricAspect`).
**Трассировки**: OpenTelemetry OTLP -> Tempo -> Grafana Explore.
---
## Состав репозитория
```
proselyte-system/
├─ api/                       # Individuals API (WebFlux)
│  ├─ src/main/java/net/proselyte/api/
│  │  ├─ rest/AuthRestControllerV1.java         # /v1/auth/* (login/refresh/me/registration)
│  │  ├─ service/{UserService,TokenService}.java
│  │  ├─ client/KeycloakClient.java             # вызовы в Keycloak
│  │  ├─ mapper/{TokenResponseMapper, ...}.java # MapStruct маппинги
│  │  ├─ aspect/LoginMetricAspect.java          # инкремент метрики логина
│  │  ├─ metric/LoginCountTotalMetric.java
│  │  └─ config/{SecurityConfig, KeycloakProperties, ...}.java
│  ├─ src/main/resources/{application.yml,logback.xml}
│  ├─ openapi/{individual-api.yaml,keycloak-api.yaml}
│  ├─ src/test/... (Testcontainers: Keycloak, WireMock, Postgres)
│  ├─ Dockerfile
│  └─ .env (переменные доступа к Nexus snapshots)
│
├─ architecture/                                # архитектурные артефакты
│
├─ infrastructure/
│  ├─ keycloak/realm-config.json                # импорт realm "individual"
│  ├─ grafana/provisioning/{datasources,dashboards}
│  ├─ prometheus/prometheus.yml
│  ├─ tempo/tempo.yaml
│  └─ loki/loki-config.yaml
│
├─ postman/
│  ├─ individuals_api_postman_collection.json       # postman коллекция для тестирования individuals-api
│  └─ persons_api_postman_collection.json           # postman коллекция для тестирования persons-api
├─ docker-compose.yml
└─ Makefile
```

Ключевые зависимости (ядро): Spring Boot 3.5, Spring WebFlux, Spring Security, spring-cloud-openfeign, springdoc-openapi, Micrometer Prometheus, OpenTelemetry SDK/Auto, MapStruct, Lombok, Testcontainers, WireMock, Keycloak Admin Client.

---
## Быстрый старт
> Требуется: **Docker** (compose), **JDK 24**, `make`. Порты по умолчанию: 8080 (Keycloak), 8091 (API), 8092 (Persons), 9090 (Prometheus), 3000 (Grafana), 3200 (Tempo), 3100 (Loki), 8081 (Nexus).
### Вариант A - через `Makefile` (рекомендуется)

```bash
# В корне репозитория
make all
# Эквивалентно: make up && make build-artifacts && make start
```

Что делает:
* поднимает инфраструктуру (Keycloak + Postgres, Prometheus, Grafana, Tempo, Loki, экспортеры);
* стартует сервисы.

Проверка:
```bash
curl -f http://localhost:8091/actuator/health   # Individuals API
curl -f http://localhost:8080/                  # Keycloak
```

### Smoke-тест (curl)
```bash
# Регистрация
curl -s -X POST http://localhost:8091/v1/auth/registration \
  -H 'Content-Type: application/json' \
  -d '{
        "firstName":"John","lastName":"Doe",
        "email":"user1@example.com",
        "password":"Qwerty_123","confirm_password":"Qwerty_123",
        "passport_number":"4010 123456","phone_number":"+1 555 123 4567",
        "address":{"address":"Main St 1","zip_code":"10001","city":"NY","country_code":"US"}
      }'

# Логин
curl -s -X POST http://localhost:8091/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"user1@example.com","password":"Qwerty_123"}'

# Me
ACCESS=... # подставьте access_token из ответа
curl -s -H "Authorization: Bearer $ACCESS" http://localhost:8091/v1/auth/me
```
---
## Ручной запуск (без Makefile)

1. **Инфраструктура (только инфра в Docker)**

   Вариант A (рекомендуется):

   ```bash
   make infra  # поднимет: nexus keycloak person-postgres prometheus grafana tempo loki
   ```

   Вариант B (чистый docker-compose):

   ```bash
   docker compose up -d nexus keycloak person-postgres prometheus grafana tempo loki
   ```

   Дождитесь готовности Nexus (`http://localhost:8081`) и Keycloak (`http://localhost:8080`).

3. **Запуск сервисов из IDE/CLI (локально, без Docker)**

    * Persons Service:

      ```bash
      cd person-service
      ./gradlew bootRun   # порт 8092
      ```
    * Individuals API:

      ```bash
      cd api
      ./gradlew bootRun   # порт 8091
      ```
---
## Порты и сервисы

| Сервис             | Порт (host) | Описание                           |
| ------------------ | ----------- | ---------------------------------- |
| Individuals API    | 8091        | WebFlux API `/v1/auth/*`, Actuator |
| Keycloak           | 8080        | Dev-мод, импорт realm `individual` |
| Prometheus         | 9090        | Метрики                            |
| Grafana            | 3000        | Dashboard + Explore                |
| Tempo              | 3200        | Хранилище трассировок (OTLP)       |
| Loki               | 3100        | Хранилище логов                    |

Prometheus уже сконфигурирован на сбор `/actuator/prometheus` у `api:8091` и `person-service`; Grafana **автопровиженит** датасорсы Prometheus/Loki/Tempo.
---
## Конфигурация (ENV/`application.yml`)

### Individuals API (`api/src/main/resources/application.yml`)

Ключевые параметры (дефолты):

* `server.port=8091`
* `application.keycloak.serverUrl=${KEYCLOAK_URL:http://localhost:8080}`
* `spring.security.oauth2.resourceserver.jwt.issuer-uri=${application.keycloak.serverUrl}/realms/${application.keycloak.realm}`
* Feign‑клиенты:

    * `auth.name`, `auth.url` -> **имена должны быть валидным hostname** (`[a-z0-9-]`)
    * `person.name=${PERSONS_API_NAME:person}`, `person.url=${PERSONS_API_URL:http://localhost:8092}`
* Actuator/Prometheus включён.
* OTEL OTLP endpoint: `OTLP_EXPORTER_ENDPOINT` (по умолчанию `http://localhost:4318`).

> Строка должна быть **в одну линию**:
>
> ```yaml
> otel.exporter.otlp.endpoint: ${OTLP_EXPORTER_ENDPOINT:http://localhost:4318}
> ```

### Persons Service (`person-service/src/main/resources/application.yml`)

* `server.port=8092`
* Datasource: Postgres на `localhost:5434`
* Flyway включён; JPA + Hibernate; метрики и OTLP аналогично API.

### Nexus/SDK

* `api/.env` содержит `NEXUS_URL/NEXUS_USERNAME/NEXUS_PASSWORD` (локально `admin/admin`).
* `person-service` публикует артефакт клиента `net.proselyte:person-api:*` (генерируется из `openapi/person-api.yaml`).
* Individuals API использует эти SDK через OpenFeign.
---

## API и OpenAPI

### Individuals API (порт 8091)

* `POST /v1/auth/registration` - регистрация (создание Person в `person-service` + создание пользователя в Keycloak) -> `TokenResponse`
* `POST /v1/auth/login` - логин по email/паролю -> `TokenResponse`
* `POST /v1/auth/refresh-token` - обновление токена -> `TokenResponse`
* `GET /v1/auth/me` - инфо о текущем пользователе (JWT Bearer)

OpenAPI: `api/openapi/individual-api.yaml`. Swagger UI:
`http://localhost:8091/swagger-ui/index.html`


OpenAPI: `person-service/openapi/person-api.yaml`. Swagger UI:
`http://localhost:8092/swagger-ui/index.html`
---

## Наблюдаемость

* **Метрики**: `http://localhost:8091/actuator/prometheus`, `http://localhost:8092/actuator/prometheus`
  Кастомная метрика: `individual_app_login_count_total` (инкремент после успешного логина).
* **Prometheus**: `http://localhost:9090`
* **Grafana**: `http://localhost:3000` (admin/admin). Датасорсы Prometheus/Loki/Tempo провиженятся из `infrastructure/grafana/provisioning`.
* **Трассировки**: отправляются по OTLP HTTP в `Tempo` (порт 4318), смотрятся в Grafana -> Explore -> Tempo.
* **Логи**: Loki развёрнут; для доставки логов приложений используйте promtail или logback‑аппендер для Loki (в репозитории базовая конфигурация Loki уже есть).
---

## Тестирование

* **Unit/Integration tests** в модуле `api`.

* В интеграционных тестах поднимаются **Testcontainers**: Postgres, **Keycloak**.

* Запуск:

  ```bash
  cd api
  ./gradlew clean test
  ```

  Требуется Docker.

---

## Postman:

* [INDIVIDUALS_API_POSTMAN_COLLECTION](postman/individuals_api_postman_collection.json)


---
## Типовые ошибки и устранение

**1) `IllegalStateException: Service id not legal hostname (${auth.name})`**
Имена Feign‑клиентов валидируются как **hostname** -> только `[a-z0-9-]`. Нельзя использовать подчёркивания и заглавные буквы.

```yaml
# application.yml - правильно
auth:
  name: keycloak-auth
  url: http://localhost:8080

person:
  name: person
  url: http://localhost:8092
```

И в `@FeignClient`:

```java
@FeignClient(
  name = "${auth.name:keycloak-auth}",
  url  = "${auth.url:http://localhost:8080}"
)
```

> `contextId` можно опустить либо тоже держать в формате `a-z-0-9`.

**2) Неправильный формат OTEL endpoint в YAML**
Должно быть в **одной строке**:

```yaml
otel:
  exporter:
    otlp:
      endpoint: ${OTLP_EXPORTER_ENDPOINT:http://localhost:4318}
      protocol: http/protobuf
```

**3) Keycloak долго стартует/realm не импортировался**
Проверьте логи контейнера `keycloak`. В compose Keycloak запускается в dev‑режиме с `--import-realm` и конфигом `infrastructure/keycloak/realm-config.json`. Повторный импорт не выполняется, если realm уже существует.

**4) Testcontainers падает - нет Docker**
Для интеграционных тестов нужен локальный Docker. В CI можно отключить их флагом `-x test` или профилем.

---
## Структура каталогов

* `api/` - Individuals API (WebFlux). Аутентификация, координация регистрации, интеграция с Keycloak и Persons Service, OpenAPI (`individual-api.yaml`, `keycloak-api.yaml`), интеграционные тесты (Keycloak/WireMock), кастомные метрики.
* `infrastructure/` - конфигурации Grafana/Prometheus/Tempo/Loki и Keycloak realm.
* `docker-compose.yml` - инфраструктура + сервисы.
* `Makefile` - сценарии `make up/build-artifacts/start/stop/logs/rebuild`.
---
