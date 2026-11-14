# TP3 - Microservices avec Spring Boot et Spring Cloud

Implementation of the ISETJB SOA lab (TP3): four independently deployable
Spring Boot services that talk to each other through Spring Cloud
(configuration, discovery, routing / load balancing).

> **Status:** scaffolding only - no service code has been written yet.
> See [ROADMAP.md](ROADMAP.md) for the plan and
> [docs/environment-setup.md](docs/environment-setup.md) for the toolchain.

## Target architecture

```
                        +---------------------+
   browser / client --> |   Proxy Service     |   :9999
                        |  Spring Cloud       |
                        |  Gateway + LB       |
                        +----------+----------+
                                   |  lb://product-service
                                   v
                        +---------------------+
                        |  Discovery Service  |   :8761   (Eureka registry)
                        +----------+----------+
                                   ^ register / discover
                                   |
             +---------------------+---------------------+
             |                     |                     |
      +------+------+       +------+------+       +------+------+
      | Product     |       | Product     |       | Product     |
      | Service #1  |       | Service #2  |       | Service #3  |
      |  :8080      |       |  :8081      |       |  :8082      |
      +-------------+       +-------------+       +-------------+

                        +---------------------+
                        |  Config Service     |   :8888
                        |  (myConfig repo)    |  <- all services read their
                        +---------------------+     properties from here
```

## Services

| Service            | Port(s)           | Spring stack                                                        | Role                                                                 |
| ------------------ | ----------------- | ------------------------------------------------------------------- | -------------------------------------------------------------------- |
| `config-service`   | 8888              | Web + Config Server (`@EnableConfigServer`)                          | Centralised configuration (`myConfig/`), shared + per-service files   |
| `discovery-service`| 8761              | Eureka Server + Config Client                                       | Service registry, no self-registration                                |
| `product-service`  | 8080, 8081, 8082  | Web, Data JPA, H2, Actuator, Eureka Discovery, Config Client         | Business API: `/produits` (CRUD), `/messages` (which instance answered) |
| `proxy-service`    | 9999              | Gateway (reactive), LoadBalancer, Actuator, Eureka, Config Client    | Single entry point, `lb://` routing + round-robin over instances      |

## Repository layout (planned)

```
tp3-soa-microservices/
├─ config-service/
│  └─ src/main/resources/myConfig/        # the "config git repo"
│     ├─ application.properties           # vglobale=...
│     ├─ product-service.properties       # sujet=...
│     ├─ discovery-service.properties     # server.port=8761, no self-register
│     └─ proxy-service.properties         # server.port=9999
├─ discovery-service/
├─ product-service/
├─ proxy-service/
├─ docs/
│  └─ environment-setup.md
├─ ROADMAP.md
└─ README.md
```

Each service stays an **independent Maven project** (own `pom.xml`, own
`mvnw`) so it can be built, versioned and deployed on its own, as required by
the microservice approach. A parent aggregator `pom.xml` is optional later.

## Run order (once implemented)

1. `config-service` (8888) - everything else imports its config.
2. `discovery-service` (8761).
3. `product-service` x3 (8080, 8081, 8082).
4. `proxy-service` (9999).

Acceptance URLs:

- `http://localhost:8888/product-service/master` → JSON with `vglobale` + `sujet`
- `http://localhost:8761` → 3 `PRODUCT-SERVICE` instances, 1 `PROXY-SERVICE`
- `http://localhost:8080/produits` → Pencil / Book / Eraser
- `http://localhost:9999/product-service/messages` → port rotates 8080 / 8081 / 8082
