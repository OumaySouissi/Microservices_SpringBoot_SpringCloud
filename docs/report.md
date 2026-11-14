# TP3 SOA - Microservices Architecture & Demonstration Report

## 1. Architecture Diagram

![Architecture Diagram](architecture-diagram.svg)

```mermaid
flowchart TD
    Client["HTTP Client (Browser / Postman)"] -->|:9999 /product-service/**| Gateway["proxy-service :9999<br/>(Spring Cloud Gateway + LoadBalancer)"]
    
    Gateway -.->|Fetch Registry| Eureka["discovery-service :8761<br/>(Eureka Server)"]
    
    P1["product-service #1 :8080"] -.->|Register Instance| Eureka
    P2["product-service #2 :8081"] -.->|Register Instance| Eureka
    P3["product-service #3 :8082"] -.->|Register Instance| Eureka
    
    Gateway ===>|Round-Robin lb://product-service| P1
    Gateway ===>|Round-Robin lb://product-service| P2
    Gateway ===>|Round-Robin lb://product-service| P3
    
    Config["config-service :8888<br/>(Spring Cloud Config Native /myConfig)"] -.->|Import Config| P1
    Config -.->|Import Config| P2
    Config -.->|Import Config| P3
    Config -.->|Import Config| Gateway
    Config -.->|Import Config| Eureka
```

---

## 2. Spring Cloud LoadBalancer & Caching Notes (Phase 5)

- **Default Cache:** `Spring Cloud LoadBalancer` caches instance lists retrieved from Eureka in memory.
- **Failover / Propagation latency:**
  - Eureka clients refresh their local instance cache periodically governed by `eureka.client.registry-fetch-interval-seconds` (default: 30 seconds).
  - When an instance is stopped or dies, there is a small propagation window (~30s) during which the gateway cache may still route to the terminated instance until the next registry sync evicts it.
  - Setting `eureka.client.registry-fetch-interval-seconds=5` in development speeds up failover detection.

---

## 3. Demonstration Screenshots (Phase 6)

### 3.1 Eureka Service Registry Dashboard (`http://localhost:8761`)
Shows `PRODUCT-SERVICE` with all 3 instances (`8080`, `8081`, `8082`) registered:

![Eureka Dashboard](screenshots/01-eureka-dashboard.png)

### 3.2 Centralized Config Server (`http://localhost:8888/product-service/master`)
Shows merged configuration (`vglobale=xxxxx` + `sujet=tp.microservice@isetjb.rnu.tn`):

![Config Server JSON](screenshots/02-config-server-master.png)

### 3.3 Microservice `/messages` on each instance
- **Port 8080:**
  ![Messages 8080](screenshots/03-messages-port-8080.png)
- **Port 8081:**
  ![Messages 8081](screenshots/04-messages-port-8081.png)
- **Port 8082:**
  ![Messages 8082](screenshots/05-messages-port-8082.png)

### 3.4 API Gateway Load Balancing & Routing (`http://localhost:9999`)
- **Messages endpoint load-balanced (`/product-service/messages`):**
  ![Gateway Load Balancing](screenshots/06-proxy-loadbalanced-messages.png)

- **Products endpoint routed (`/product-service/produits`):**
  ![Gateway Products](screenshots/07-proxy-loadbalanced-produits.png)
