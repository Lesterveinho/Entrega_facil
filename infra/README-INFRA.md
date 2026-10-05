# Infra do projeto Delivery API — passo a passo

Guia para montar a infraestrutura completa (observabilidade) do projeto.

## Arquitetura

- A **aplicacao Spring Boot** roda no **HOST** (sua maquina), na porta **8080**.
- A **infra** (Prometheus, Grafana, Zipkin, Redis) roda em **containers Docker**.
- A comunicacao:
  - App  -> Zipkin: `http://localhost:9411` (envia os traces)
  - Prometheus -> App: `host.docker.internal:8080/actuator/prometheus` (coleta metricas)
  - Grafana -> Prometheus: `http://prometheus:9090` (datasource ja provisionado)

```
                +-------------------- Docker (infra/) --------------------+
  [App :8080] ->| Zipkin :9411                                            |
      ^   ^     | Prometheus :9090  --(datasource)-->  Grafana :3000       |
      |   |     | Redis :6379                                             |
      |   +-----| Prometheus faz scrape em host.docker.internal:8080      |
      +---------+--------------------------------------------------------+
```

---

## Itens necessarios e passo a passo

### 1) JDK 21 (obrigatorio)
> Recomendado a 21 LTS (bate com o curso). Evite a 26: Lombok/Spring Boot 4 podem quebrar.
1. Baixar o JDK 21 (Oracle: oracle.com/java/technologies/javase-downloads ou Eclipse Temurin: adoptium.net).
2. Instalar.
3. Variaveis de ambiente (Windows): criar `JAVA_HOME` = `C:\Program Files\Java\jdk-21` e adicionar `%JAVA_HOME%\bin` ao `Path`.
4. Testar: `java -version`  (deve mostrar 21.x.x).

### 2) Maven (obrigatorio) — ou usar o wrapper
- O projeto ja traz o **Maven Wrapper** (`mvnw` / `mvnw.cmd`), entao da pra usar `mvnw` sem instalar o Maven.
- Se quiser o Maven instalado: baixar o zip binario em maven.apache.org/download.cgi, extrair, criar `MAVEN_HOME` e adicionar `%MAVEN_HOME%\bin` ao `Path`. Testar: `mvn -v`.

### 3) Docker Desktop (obrigatorio para a infra)
1. Pre-requisitos (Windows 10/11 64-bit): **WSL 2** instalado e **virtualizacao ativada na BIOS**.
   - Instalar WSL 2 (PowerShell como admin): `wsl --install` e reiniciar.
2. Baixar o Docker Desktop: docker.com/products/docker-desktop e instalar (manter "Use WSL 2" marcado).
3. Reiniciar e abrir o Docker Desktop. Aguardar status "Docker is running".
4. Testar: `docker --version` e `docker run hello-world`.

### 4) Subir a infra (Prometheus + Grafana + Zipkin + Redis)
No terminal, dentro da pasta `infra/` deste projeto:
```bash
cd infra
docker compose up -d
```
Conferir: `docker compose ps` (os 4 containers devem estar "running").

### 5) Rodar a aplicacao (na raiz do projeto)
```bash
mvn spring-boot:run
```
(ou `mvnw spring-boot:run` / `start.sh` no Git Bash). A API sobe em http://localhost:8080.

---

## Verificacao de cada componente

| Componente   | URL                                         | O que conferir |
|--------------|---------------------------------------------|----------------|
| API (Swagger)| http://localhost:8080/swagger-ui.html       | Documentacao dos endpoints |
| API (H2)     | http://localhost:8080/h2-console            | JDBC `jdbc:h2:mem:deliverydb`, user `sa`, sem senha |
| Actuator     | http://localhost:8080/actuator/health       | status UP |
| Metricas     | http://localhost:8080/actuator/prometheus   | texto com metricas (formato Prometheus) |
| Prometheus   | http://localhost:9090/targets               | target `delivery-api` deve estar **UP** |
| Grafana      | http://localhost:3000                       | login admin/admin; datasource Prometheus ja configurado |
| Zipkin       | http://localhost:9411                       | apos chamar a API, buscar traces (Run Query) |
| Redis        | `docker exec -it delivery-redis redis-cli ping` | responde `PONG` |

### Grafana — importar dashboard
1. Acesse http://localhost:3000 (admin/admin).
2. Menu **Dashboards -> New -> Import**.
3. Informe um ID da galeria e selecione o datasource **Prometheus**:
   - **4701** — JVM (Micrometer)
   - **11378** ou **12900** — Spring Boot / Statistics
4. Import.

### Gerar traces no Zipkin
Faca algumas chamadas na API (ex.: `GET http://localhost:8080/api/restaurantes`), depois abra o Zipkin e clique em **Run Query**.

---

## Comandos uteis
```bash
docker compose ps            # status dos containers
docker compose logs -f       # logs ao vivo
docker compose down          # parar e remover os containers (mantem o volume do Grafana)
docker compose down -v       # parar e remover tambem os volumes (zera Grafana)
```

## Observacoes
- **Redis** sobe no container, mas a aplicacao ainda nao o consome (nao ha cache configurado no codigo). Esta disponivel para quando for implementar cache (precisaria de `spring-boot-starter-data-redis` + `@EnableCaching`). As linhas `spring.redis.*` estao comentadas no `application.properties`.
- **host.docker.internal** funciona no Docker Desktop (Windows/Mac). Em Linux puro, troque o target no `prometheus.yml` pelo IP do host (ou use `--add-host=host.docker.internal:host-gateway`).
- Tracing: o `pom.xml` usa `micrometer-tracing-bridge-brave` + `zipkin-reporter-brave`; o endpoint do Zipkin esta em `application.yml` (`management.zipkin.tracing.endpoint`), com `sampling.probability=1.0`.
