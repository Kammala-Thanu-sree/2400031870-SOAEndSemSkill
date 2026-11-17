# Ride-sharing platform: Round-robin load balancing

This Spring Boot demo uses:
- Eureka Discovery Server: registers and discovers Driver Service instances.
- Driver Service: run two copies on ports 8081 and 8082.
- Spring Cloud Gateway: exposes one URL on port 8080.
- Spring Cloud LoadBalancer: distributes gateway requests across discovered instances (round-robin).

## Requirements
- JDK 17
- Maven 3.8+
- Internet access for the first Maven build (to download dependencies)

## Import into Spring Tool Suite / Eclipse
1. Extract this ZIP file.
2. Open STS/Eclipse.
3. Select **File -> Import -> Maven -> Existing Maven Projects**.
4. Browse to the extracted `ride-sharing-round-robin` folder.
5. Select the root `pom.xml` (the three modules should appear).
6. Click **Finish**, then wait for Maven dependencies to download.
7. If needed, right-click the root project -> **Maven -> Update Project**.

## Build once
Open a terminal in the folder containing the root `pom.xml` and run:

```bash
mvn clean package -DskipTests
```

## Run in this exact order

Open four terminals. Keep each process running.

### Terminal 1: Eureka Discovery Server
From the project root:
```bash
mvn -pl discovery-server spring-boot:run
```
Open http://localhost:8761 and confirm the Eureka dashboard loads.

### Terminal 2: Driver Service instance 1
From the project root:
```bash
mvn -pl driver-service spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

### Terminal 3: Driver Service instance 2
From the project root:
```bash
mvn -pl driver-service spring-boot:run -Dspring-boot.run.arguments=--server.port=8082
```

Wait about 20-30 seconds for both instances to register. On the Eureka dashboard, check that `DRIVER-SERVICE` has two registered instances.

### Terminal 4: API Gateway
From the project root:
```bash
mvn -pl api-gateway spring-boot:run
```

## Verify round-robin distribution

Send repeated GET requests through the gateway, not directly to the driver ports.

Browser:
- http://localhost:8080/drivers

PowerShell (Windows):
```powershell
1..10 | ForEach-Object { Invoke-RestMethod http://localhost:8080/drivers }
```

Command Prompt / macOS / Linux:
```bash
for i in {1..10}; do curl http://localhost:8080/drivers; echo; done
```

Expected response alternates approximately between:
```json
{"service":"driver-service","message":"Driver service is responding","instancePort":"8081"}
{"service":"driver-service","message":"Driver service is responding","instancePort":"8082"}
```

Check the two Driver Service terminal windows. Their console logs should show requests handled by port 8081 and port 8082. With two healthy instances and no other traffic, requests should alternate in round-robin order. The first instance may vary.

## Important troubleshooting
- **Gateway returns 503 / no instances available:** make sure Eureka is running, both driver instances are running, and `DRIVER-SERVICE` shows two instances in Eureka. Wait 20-30 seconds after starting services.
- **Port already in use:** stop the other process or choose another port; if you change driver ports, use distinct values.
- **Maven command not found:** install Maven and add its `bin` directory to PATH, or use the Maven wrapper if you add one.
- **Java version error:** configure STS and terminal to use JDK 17.
- **Gateway must use `lb://driver-service`:** this is what enables service discovery and client-side load balancing; do not hard-code `localhost:8081`.

## Project flow
Client -> API Gateway (8080) -> Eureka-discovered Driver Service instances (8081, 8082)
