This project is a **Kafka-based error monitoring pipeline** built with **Spring Boot** and **Spring Kafka**.
It consists of several services that work together:

1. **log-producer**
   * Spring Boot service that publishes `LogEvent` messages (JSON) to a Kafka topic, e.g. `logs.raw`.
   * Each `LogEvent` contains things like `service`, `message`, `traceId`, etc.

2. **log-filter**
   * A **Kafka Streams** application.
   * Consumes records from `logs.raw`.
   * Deserializes them into `LogEvent`.
   * Applies filtering logic (e.g. keeps only ERROR/important events).
   * Writes the filtered events to an alerts topic, e.g. `alerts`.

3. **alert-sink**
   * Spring Boot consumer service.
   * Listens to the alerts topic.
   * For each `LogEvent`:

     * If a Slack webhook URL is configured, sends a formatted message to Slack via `RestTemplate`.
     * If no webhook is configured, just logs the alert to the console.
   * If sending fails (exception in `onAlert`), it publishes the same `LogEvent` to a **DLT (dead-letter topic)** using `KafkaTemplate<String, LogEvent>` so failed alerts are not lost.

---

## Tech Stack
* **Language:** Java 21
* **Framework:** Spring Boot 3.x
* **Messaging:** Apache Kafka, Kafka Streams
* **Build Tool:** Maven
* **Alerting:** Slack Incoming Webhook
* **Others:** Spring Actuator, Lombok, Jackson

---

## Running Kafka via Docker Compose
Start Kafka:

```bash
docker-compose up -d
```

---

## Build & Run
From the project root:

### 1. Build all modules
```bash
mvn clean install
```

### 2. Run log-producer
```bash
cd log-producer
mvn spring-boot:run
```

### 3. Run log-filter
```bash
cd log-filter
mvn spring-boot:run
```

### 4. Run alert-sink
```bash
cd alert-sink
mvn spring-boot:run
```

Make sure Kafka is running before starting the apps.

---

## Slack Webhook Setup
1. Open your Slack workspace in browser.
2. Go to **Apps** → search for **Incoming WebHooks** (or create a Slack app with an incoming webhook).
3. Configure a channel (e.g. `#alerts`) for the webhook.
4. Copy the generated **Webhook URL**.
5. Set this URL in `alert-sink` configuration:

   ```yaml
   app:
     slackWebhook: "https://hooks.slack.com/services/XXXX/YYYY/ZZZZ"
   ```

The `AlertConsumer` will send messages like:
```java
var payload = Map.of(
  "text", " ***" + event.service() + " " + event.message()
          + " (traceId=" + event.traceId() + ")"
);
```
---

## Usage
1. Start all three services and Kafka.
2. Use the **log-producer** to send a test `LogEvent` (for example via its REST endpoint).
3. The event is written to `logs.raw`.
4. **log-filter** consumes the event, applies filter rules, and (if matched) sends it to `alerts`.
5. **alert-sink** consumes from `alerts`:

   * Sends an alert message to Slack, **or**
   * Logs to console if no webhook is configured, **or**
   * Sends to `alerts-dlt` Dead Letter Topic if Slack call fails.

You can inspect topics (`logs.raw`, `alerts`, `alerts-dlt`) using Kafka CLI tools or a UI like `kafdrop`.
