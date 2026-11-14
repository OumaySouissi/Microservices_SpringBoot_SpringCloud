# Roadmap - TP3 Microservices (Spring Boot + Spring Cloud)

Goal: build the 4 services described in the lab sheet, in an order where every
step is verifiable on its own, then demo the load balancing through the proxy.

Each phase has a **Definition of Done (DoD)**. Do not start the next phase
until the DoD of the current one is reached - microservice debugging is much
harder when several layers are broken at once.

Legend: `[ ]` todo

---

## Phase 0 - Environment + repo hygiene

- [x] JDK 24 installed (compatible with Boot 3.5.x), `JAVA_HOME` set, `java -version` = 24
- [x] Spring Boot / Spring Cloud versions pinned once (3.5.16 + 2025.0.3)
- [x] Repo cloned, `.gitignore` in place, first commit pushed
- [x] Project generation method decided: multi-module parent POM + Maven wrapper

**DoD:** `java -version` OK, `./mvnw -v` OK, repo clean and pushed.

---

## Phase 1 - `product-service` alone (no cloud yet)

Reference: PDF "Microservice ProductService".

- [x] Generate `product-service` with: Web, Data JPA, H2, Actuator,
      Eureka Discovery Client, Config Client
- [x] `Product` entity (`id`, `name`) - **`jakarta.persistence`**, not `javax`
- [x] `ProductRepository extends JpaRepository<Product, Integer>`
- [x] `DummyDataCLR` `CommandLineRunner` seeding Pencil / Book / Eraser
- [x] `ProductRestService` with the 5 classic operations:
      `GET /produits`, `GET /produits/{id}`, `POST /produits`,
      `PUT /produits/{id}`, `DELETE /produits/{id}`
- [x] Actuator exposed: `management.endpoints.web.exposure.include=*` and
      `management.endpoint.health.show-details=always`
- [x] Three instances ready on 8080 / 8081 / 8082 (via `--server.port=...`)

**DoD:** `http://localhost:8080/produits` returns the 3 seeded products;
`/actuator/health` returns `UP`; a POST then GET round-trip works.

> The lab sheet returns `Iterable<Client>` - that is a typo, use `Product`.

---

## Phase 2 - `config-service` + externalised config

Reference: PDF "Microservice ConfigService".

- [ ] Generate `config-service` with Web + Config Server, `@EnableConfigServer`
- [ ] `server.port=8888`
- [ ] `myConfig/` directory used as config source
      (`native` profile, or `git` with `file:` URI + `git init`/commit inside)
- [ ] `myConfig/application.properties` → `vglobale=xxxxx`
- [ ] `myConfig/product-service.properties` → `sujet=tp.microservice@isetjb.rnu.tn`
- [ ] `product-service`: `spring.application.name=product-service`,
      `spring.config.import=optional:configserver:http://localhost:8888`
- [ ] `product-service` exposes `GET /messages` returning its `server.port`,
      `sujet` and `vglobale`

**DoD:** `http://localhost:8888/product-service/master` returns the merged
JSON (`vglobale` + `sujet`); `http://localhost:8080/messages` prints the right
port and both values, and the message differs on 8081 / 8082.

Note: config is read at startup only - restart instances after editing
`myConfig/`. Without `optional:`, a missing config server kills startup.

---

## Phase 3 - `discovery-service` (Eureka)

Reference: PDF "Microservice DiscoveryService".

- [ ] Generate `discovery-service` with Eureka Server + Config Client
      (`@EnableEurekaServer`)
- [ ] `myConfig/discovery-service.properties`:
      `server.port=8761`, `eureka.client.fetch-registry=false`,
      `eureka.client.register-with-eureka=false`
- [ ] `product-service`: `@EnableDiscoveryClient`
- [ ] Give each instance a unique id:
      `eureka.instance.instance-id=${spring.application.name}:${server.port}`

**DoD:** `http://localhost:8761` lists `PRODUCT-SERVICE` with 3 instances on
3 different ports (allow ~30 s for registry propagation).

---

## Phase 4 - `proxy-service` (Spring Cloud Gateway + LoadBalancer)

Reference: PDF "Microservice ProxyService".

- [ ] Generate `proxy-service`: **Gateway (reactive)**, LoadBalancer, Actuator,
      Eureka Discovery, Config Client
      (no `spring-boot-starter-web` in this one - it is WebFlux)
- [ ] `myConfig/proxy-service.properties` → `server.port=9999`
- [ ] Route: `/product-service/**` → `lb://product-service`, `StripPrefix=1`
- [ ] Verify the proxy itself is registered in Eureka

**DoD:** hitting `http://localhost:9999/product-service/messages` 6 times
shows the 3 ports alternating; `/product-service/produits` works through the
proxy too.

---

## Phase 5 - Robustness / demo quality

- [ ] Stop one instance → traffic only goes to the two remaining ones
- [ ] Add `spring.cloud.loadbalancer.` caching notes / `eureka.client.registry-fetch-interval-seconds`
      explanation in the report
- [ ] Optional: H2 console off in "prod" profile, log level per service
- [ ] Optional: `docker-compose` starting config → discovery → 3 products → proxy
- [ ] Optional: unit test on the controller (`@WebMvcTest`) + `@DataJpaTest`

**DoD:** the whole stack starts from scratch with one documented command
sequence, and the demo scenario in the README works.

---

## Phase 6 - Report / defence

- [ ] Architecture diagram (draw.io or Excalidraw), exported in `docs/`
- [ ] Screenshots: Eureka dashboard, `/messages` x3, proxy round-robin,
      config server JSON
- [ ] Explain *why* each Spring Cloud brick exists (config, discovery, gateway,
      client-side LB) - the lab is graded on the reasoning, not on the code
- [ ] Answers to classic questions: what happens if Eureka dies? why client-side
      load balancing? why does config need a restart? what is `lb://`?

---

## Known pitfalls (read before coding)

| Symptom | Cause | Fix |
| --- | --- | --- |
| `javax.persistence` not found | Sheet is Boot 2.x, we use Boot 3.x | use `jakarta.persistence` |
| Startup fails: `Could not resolve placeholder 'vglobale'` | config not loaded / name mismatch | `spring.application.name` must match `myConfig/<name>.properties`, keep `optional:` |
| `endpoints.health.sensitive` ignored | Boot 2.x property removed | `management.endpoint.health.show-details=always` |
| Gateway starts then 404 on every route | `spring-boot-starter-web` also on classpath | remove it, Gateway is reactive |
| Only 1 product instance in Eureka | same hostname, no `instance-id` | set `eureka.instance.instance-id=${spring.application.name}:${server.port}` |
| Round-robin not visible | registry cache / single request per refresh | wait ~30 s, hit the URL repeatedly |
| Config server 500 on `file:` repo | directory is not a git repo | `git init && git add . && git commit -m "config"` or use `native` profile |
| `Address already in use` | previous instance still running | `netstat -ano | findstr :8081` then kill the PID |

---

## Suggested git workflow

- `main` always runnable
- one branch per phase: `feat/product-service`, `feat/config-service`,
  `feat/discovery-service`, `feat/proxy-service`
- conventional commits: `feat(product-service): add CRUD endpoints`
- merge with `--no-ff` and delete the branch
