# Environment setup (Windows)

What this lab needs, what is already on the machine, and how to fill the gaps.

## 1. Inventory of the current machine

| Tool | Needed | Found | Verdict |
| --- | --- | --- | --- |
| JDK 21 (LTS) | yes | Java 24 (`C:\Program Files\Java`), JDK 25 (Adoptium) | **install 21**, 24/25 not the target for Boot 3.5 tutorials |
| Maven 3.9+ | yes | not installed | use the **Maven wrapper** (`mvnw`) shipped by start.spring.io |
| Spring Tool Suite 4 | recommended by the sheet | not installed | optional: VS Code + "Extension Pack for Java" works fine |
| VS Code | nice to have | installed | OK |
| Docker Desktop | optional | installed | only for the optional docker-compose phase |
| Git | yes | 2.51 (`C:\utilitaires\Git`) | OK |
| GitHub CLI (`gh`) | optional | not installed | not required: repo already created, plain `git` is enough |
| Node.js / Python | no | installed | not used in this TP |

---

## 2. Install JDK 21

The lab code needs a JDK that Spring Boot 3.5 officially supports. Java 24 and
25 are *too new* for a course setup (some libraries lag behind) - install
Temurin 21 LTS next to them and point the project at it.

Option A - installer (simplest):

1. Download **Eclipse Temurin JDK 21 (x64, .msi)** from
   <https://adoptium.net/temurin/releases/?version=21>
2. Install with "Set JAVA_HOME variable" and "Add to PATH" enabled.

Option B - if you install more JDKs later, keep them side by side and switch
`JAVA_HOME` per project instead of globally.

Verify (new terminal):

```powershell
java -version      # must print 21.x
echo $env:JAVA_HOME
```

If `java -version` still shows 24, the old `C:\Program Files\Common Files\Oracle\Java\javapath`
entry wins in PATH: put `%JAVA_HOME%\bin` **before** it, or remove the Oracle
`javapath` entry.

---

## 3. Maven - use the wrapper, do not install Maven

Every project generated on <https://start.spring.io> contains `mvnw`,
`mvnw.cmd` and `.mvn/wrapper/`. Nothing else is needed:

```powershell
cd product-service
.\mvnw.cmd -v          # prints Maven 3.9.x + Java 21
.\mvnw.cmd spring-boot:run
```

Install a standalone Maven only if you want `mvn` globally:

```powershell
# optional
# download apache-maven-3.9.x-bin.zip, unzip to C:\utilitaires\apache-maven-3.9.9
# add C:\utilitaires\apache-maven-3.9.9\bin to PATH
```

---

## 4. IDE: STS 4 or VS Code

**Spring Tool Suite 4** (matches the lab sheet, has the Boot Dashboard to start
several instances with one click):

1. <https://spring.io/tools> → download the Windows x64 installer
2. Unzip/install, then `File > Import > Existing Maven Projects`
3. Boot Dashboard (bottom) is where you run/duplicate service configurations -
   exactly what the PDF describes for ports 8081 and 8082.

**VS Code** (already installed, lighter):

1. Install extensions: *Extension Pack for Java*, *Spring Boot Extension Pack*
2. `Ctrl+Shift+P` → `Spring Initializr: Create a Maven Project`
3. Run services from the *Spring Boot Dashboard* panel.

Do not use both to generate the same project - pick one and stick to it.

---

## 5. Pinning versions (do this once, in the whole repo)

The lab sheet is written for Spring Boot 2.x (`javax.persistence`,
`endpoints.health.sensitive`, `management.security.enabled`). Those are gone in
Boot 3.x, so pin a modern, coherent pair and translate the snippets:

| Item | Version |
| --- | --- |
| Java | 21 (LTS) |
| Spring Boot | 3.5.x |
| Spring Cloud | 2025.0.x |
| Spring Cloud Config / Eureka / Gateway | pulled by the Cloud BOM |

`start.spring.io` already generates a compatible `spring-boot-starter-parent`
and `spring-cloud.version` property - keep what it gives you, and use the same
pair in the 4 projects so the BOMs do not fight.

Translation table for the sheet:

| Sheet (Boot 2.x) | Boot 3.5 |
| --- | --- |
| `javax.persistence.*` | `jakarta.persistence.*` |
| `endpoints.health.sensitive=false` | `management.endpoint.health.show-details=always` |
| `management.security.enabled=false` | `management.endpoints.web.exposure.include=*` |
| `spring-cloud-starter-gateway` | same artifact (reactive Gateway) - check the Cloud 2025 release notes if Maven complains |
| `-server.port=8081` (program arg) | `--server.port=8081` |

---

## 6. Generate the 4 projects

Manual route (recommended, full control, works with both IDEs):

<https://start.spring.io> - one project per service:

| Project | Group | Artifact | Dependencies |
| --- | --- | --- | --- |
| product-service | `isetjb.soa.tpmicro` | `product-service` | Spring Web, Spring Data JPA, H2, Actuator, Eureka Discovery Client, Config Client |
| config-service | `isetjb.soa.tpmicro` | `config-service` | Spring Web, Config Server |
| discovery-service | `isetjb.soa.tpmicro` | `discovery-service` | Eureka Server, Config Client |
| proxy-service | `isetjb.soa.tpmicro` | `proxy-service` | Gateway, LoadBalancer, Actuator, Eureka Discovery Client, Config Client |

Keep the generated `mvnw`, `mvnw.cmd`, `.mvn/` and `.gitattributes` in git.
Then place each folder at the repo root (`tp3_soa\product-service\`, ...).

CLI alternative with `curl` (no IDE needed):

```powershell
curl.exe "https://start.spring.io/starter.zip?type=maven-project&language=java&bootVersion=3.5.6&javaVersion=21&groupId=isetjb.soa.tpmicro&artifactId=product-service&name=product-service&packageName=isetjb.soa.tpmicro.productservice&dependencies=web,data-jpa,h2,actuator,cloud-eureka,cloud-config-client" -o product-service.zip
```

(bump `bootVersion` to whatever start.spring.io currently offers; the
dependency ids are the ones in the `dependencies=` list of the site).

---

## 7. Ports to reserve

| Port | Service |
| --- | --- |
| 8888 | config-service |
| 8761 | discovery-service (Eureka) |
| 8080 / 8081 / 8082 | product-service instances |
| 9999 | proxy-service |

Check availability before a demo:

```powershell
netstat -ano | findstr ":8080 :8081 :8082 :8761 :8888 :9999"
# kill a leftover process
taskkill /PID <pid> /F
```

Windows may reserve port ranges (Hyper-V / WSL); if a port looks free but the
app says "Address already in use", run:

```powershell
netsh interface ipv4 show excludedportrange protocol=tcp
```

---

## 8. Running the three instances (once product-service exists)

PowerShell, three terminals:

```powershell
cd product-service
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8082"
```

Or, from the packaged jar (lighter on RAM, closer to real deployment):

```powershell
.\mvnw.cmd -q -DskipTests package
java -jar target\product-service-0.0.1-SNAPSHOT.jar --server.port=8081
java -jar target\product-service-0.0.1-SNAPSHOT.jar --server.port=8082
```

In STS: duplicate the run configuration, add the program argument
`--server.port=8081` in the *Arguments* tab (that is section
"Plusieurs Instances du Microservice ProductService" of the sheet).

---

## 9. Git / GitHub

Repo: `https://github.com/OumaySouissi/tp3-soa-microservices` (private).
Credentials already cached by Git Credential Manager on this machine.

```powershell
git config --global user.name  "OumaySouissi"
git config --global user.email "oumaymasouissi51@gmail.com"
git remote -v
git status
git add .
git commit -m "chore: project scaffolding and roadmap"
git push
```

Rules for this repo:

- never commit `target/`, `.classpath`, `.project`, `.settings/` (covered by `.gitignore`)
- never commit secrets; config lives in `myConfig/` and is *meant* to be readable
- one branch per phase (`feat/product-service`, ...), merged into `main` when the
  phase DoD is met

---

## 10. Quick check before starting Phase 1

```powershell
java -version                 # 21
cd product-service; .\mvnw.cmd -v
curl.exe -s https://start.spring.io   # network OK
```

If all three pass, the environment is ready.
