# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

MSTAN Shop — REST backend for an e-commerce MVP. Java 17, Spring Boot 3.3.4, Spring Data JPA,
Spring Security (JWT + Google OAuth2), MapStruct, Flyway, PostgreSQL. README.md (in French) has
full setup/auth/API documentation — read it for onboarding details not repeated here.

## Commands

```bash
# Setup (one-time): copy env template, create local DB
cp .env.dev .env
createdb mstanshop_dev

# Build
./mvnw.cmd clean compile          # Windows
./mvnw clean compile              # Linux/Mac

# Run (reads .env automatically via spring-dotenv)
./mvnw.cmd spring-boot:run

# Tests
./mvnw.cmd test                              # all tests
./mvnw.cmd test -Dtest=ClassName             # single test class
./mvnw.cmd test -Dtest=ClassName#methodName  # single test method

# Package
./mvnw.cmd clean package
```

There is currently no `src/test` directory — no tests exist yet in this codebase.

App runs on `http://localhost:8080`. Swagger UI at `/swagger-ui.html`, OpenAPI JSON at `/v3/api-docs`.

## Architecture

**Feature-based package structure.** Each business domain is its own package under
`com.mstan.shop`, with its own layered sub-packages (`controller`, `service`, `repository`,
`entity`, `dto`, `mapper` — not every feature has all of them). Current features:

- `auth/` — register, login, refresh token, password reset, Google OAuth2
- `user/` — user profile
- `category/` — product categories (admin CRUD + public read)
- `product/` — catalog, image upload, variants, Open Graph sharing
- `cart/` — persistent cart, guest cart merge on login/register
- `order/` — orders, stock adjustment, history, guest orders
- `payment/` — payment records + Fapshi mobile money integration
- `promo/` — promo codes / category promotions
- `review/` — product reviews
- `stock/` — scheduled low-stock alert job (`StockAlertScheduler`, cron via `app.stock.alert-cron`)
- `common/` — cross-cutting concerns, shared by all features (see below)

`common/` holds infrastructure used across features:
- `common/security/` — JWT filter/service, `SecurityUser` (Spring Security `UserDetails` adapter), auth entry point / access-denied handlers
- `common/config/` — `SecurityConfig` (route access rules), `DataSeeder` (creates roles, admin account, demo data on startup), `AsyncConfig`, `JpaAuditingConfig`, `WebConfig`, `OpenApiConfig`
- `common/exception/` — domain exceptions + `GlobalExceptionHandler` (`@ControllerAdvice`) mapping them to `ApiError` responses
- `common/email/` — `EmailService` + email data DTOs (order confirmation, low-stock alerts)
- `common/util/` — `BaseEntity` (shared id/audit fields), slug/currency/order-number helpers, `CategoryPromotionResolver`, `PagedResponse`

When adding a new feature, mirror this package layout rather than adding to an existing feature
package or to `common/`.

**Database migrations (Flyway, not Hibernate DDL).** `spring.jpa.hibernate.ddl-auto` is `validate`
in every profile — Hibernate checks entities match the schema but never modifies it. Flyway owns
schema evolution via versioned SQL in `src/main/resources/db/migration/`, applied automatically at
startup before Hibernate initializes.
- Never modify a migration file that has already been applied (`V1__baseline_schema.sql` is the
  reference baseline and must never change).
- For any schema change, add a new `Vn__description.sql` file with the next sequential number —
  check the existing `Vn__*.sql` files in that directory for the current max `n` first.
- The target database must already exist (`createdb ...`); Flyway creates tables in an empty DB
  but not the DB itself.

**Configuration.** `application.yml` holds shared config; `application-dev.yml` /
`application-prod.yml` hold profile-specific values (both use PostgreSQL — only connection
values differ). Active profile via `SPRING_PROFILES_ACTIVE` (default `dev`). All secrets/URLs are
env-var driven with dev defaults inlined in `application.yml` (e.g. `${JWT_SECRET:default-value}`)
and loaded from a git-ignored `.env` file via spring-dotenv — `.env.dev` and `.env.prod` are the
versioned templates. When adding a new externally-configurable value, follow this same
`${ENV_VAR:default}` pattern and add the var to the relevant `.env.*` template.

**Auth model.** JWT access + refresh tokens (`JwtService`, `JwtAuthenticationFilter`), plus Google
OAuth2 login. Route access is centralized in `SecurityConfig`: `/api/auth/**` and public
GET product/category endpoints are open; `/api/cart/**`, `/api/orders/**`, `/api/users/**` require
authentication; `/api/admin/**` requires `ROLE_ADMIN`. Guest checkout and guest-cart-merge (on
login/register) exist alongside authenticated flows — check `cart`/`order` services for the merge
logic before assuming a user is always present.

## Extending this file

Keep this file in sync with the codebase as it evolves:
- Adding a new top-level feature package → add it to the architecture list above with a one-line
  purpose (not its internal file structure — that's discoverable).
- Adding a new Flyway migration → no CLAUDE.md change needed, the numbering rule above already
  covers it.
- Introducing a new cross-cutting concern in `common/` (e.g. caching, rate limiting, a new
  notification channel) → add a bullet under the relevant `common/` sub-list.
- Adding a real test suite → replace the "no tests exist yet" note with the actual test command
  and any test-specific setup (test DB, profiles, fixtures).
- Only record architecture that requires reading multiple files to piece together, or non-obvious
  conventions (like the Flyway/Hibernate split) — skip anything a directory listing or a single
  file already makes obvious.
