# 🧾 Retail IoT Event Processing Platform

---

## 🎯 Goal

Build an **enterprise-grade event processing system** for retail IoT devices with:

* Multi-device support
* Canonical event model
* Resilience (Retry, Circuit Breaker, DLQ)
* Observability (Metrics, Actuator)
* Extensible architecture (Kafka-ready)

---

## 🏗️ Architecture

```
Device (RFID / IoT Device)
        |
        v
+----------------------+
| Ingestion Service    |
| - REST API           |
| - Adapter Layer      |
| - Validation         |
| - Publisher          |
+----------------------+
        |
        v
+----------------------+
| Event Publisher      |
| - Resilience4j       |
|   (Retry + CB)       |
| - DLQ fallback       |
+----------------------+
        |
        v
+----------------------+
| Inventory Service    |
| - Process event      |
| - Idempotency        |
| - Metrics            |
+----------------------+
        |
        v
+----------------------+
| Database             |
| - H2 (current)       |
| - PostgreSQL (future)|
+----------------------+
```

---

## 📦 Module Structure

```
retail-device-platform
│
├── common-lib
│   └── CommonEvent (shared model)
│
├── ingestion-service
│   ├── controller
│   ├── service
│   ├── adapter
│   ├── factory
│   ├── publisher
│   ├── config
│   ├── entity (DLQ)
│   ├── repository (DLQ)
│   └── static (api-explorer.html)
│
└── inventory-service
    ├── controller
    ├── service
    ├── entity
    ├── repository
    └── metrics
```

---

## 🔑 Core Concepts Implemented

### 1️⃣ Canonical Event Model

```
com.example.common.model.CommonEvent
```

**Fields:**

* `eventId`
* `deviceId`
* `eventType`
* `timestamp`
* `payload (Map<String, Object>)`

👉 All downstream services depend ONLY on this model

---

### 2️⃣ Adapter Pattern

**Interface:**

```
DeviceEventAdapter
```

**Implementations:**

* `RfidAdapter`
* `IotDeviceAdapter`

👉 Converts vendor-specific payload → `CommonEvent`

---

### 3️⃣ Factory Pattern

**Class:**

```
AdapterFactory
```

```java
adapterFactory.getAdapter(vendorType);
```

👉 Dynamically selects adapter

---

## 🚀 Ingestion Flow

```
Request → Adapter → CommonEvent → Publisher → Response
```

---

## 🚀 Event Publishing (Enterprise)

**Class:**

```
RestEventPublisher
```

### Features

✅ **Retry (Resilience4j)**

* 3 attempts
* exponential backoff

✅ **Circuit Breaker**

* opens on failure threshold
* protects downstream

✅ **Fallback → DLQ**

* failed events stored

---

## 💀 Dead Letter Queue (DLQ)

### Entity

```
DlqEventEntity
```

### Repository

```
DlqEventRepository
```

### Service

```
DlqService
```

**Methods:**

* `getAllDlqEvents()`
* `getDlqEventById()`
* `reprocess(eventId)`

### Controller

```
GET  /api/dlq
GET  /api/dlq/{eventId}
POST /api/dlq/{eventId}/reprocess
```

---

## 💀 Dead Letter Queue (DLQ) - Lifecycle Tracking

### 🆕 Status Enum


FAILED
REPROCESSING
REPROCESSED_SUCCESS
REPROCESSED_FAILED


### 🆕 Additional Fields

* `status`
* `retryCount`
* `lastRetriedAt`
* `resolvedAt`
* `lastError`

---

### 🔁 Reprocess Flow


DLQ → REPROCESSING → SUCCESS / FAILED


👉 DLQ entries are **NOT deleted** (audit requirement)

---

## 📊 Publisher Metrics (Enhanced)

* `publishedSuccessCount`
* `publishedFailureCount`
* `totalDlqRoutedCount` (historical)
* `currentDlqBacklogCount` (current)
* `reprocessSuccessCount`
* `reprocessFailureCount`

---

## 🧪 Metrics Dashboard

**File:**


metrix.html


### Features

* Publisher metrics visualization
* Inventory metrics visualization
* DLQ backlog vs historical tracking
* Reprocess success/failure tracking
* Auto-refresh dashboard

---

## 🚀 Future Architecture (Kafka)


Ingestion → Kafka → Consumer → Processor → DB
↓
DLQ → Replay Service

---

## 📊 Inventory Service

### Responsibilities

* Process events
* Apply idempotency
* Store events
* Expose metrics

---

### 🔁 Idempotency

```java
@Id
@Column(unique = true)
private String eventId;
```

👉 Prevents duplicate processing

---

### 📈 Metrics Tracked

* `processedEvents`
* `failedEvents`
* `duplicateEvents`

---

### Endpoints

```
GET /inventory/metrics
GET /inventory/events
GET /inventory/events/{eventId}
```

---

## 📡 Actuator

Enabled in both services:

```
GET /actuator/health
GET /actuator/info
```

---

## ⚙️ Configuration

```properties
inventory.service.process.url=http://localhost:8081/inventory/process
```

---

## 🔁 Resilience4j Configuration

```properties
resilience4j.retry.instances.inventoryPublisher.max-attempts=3
resilience4j.retry.instances.inventoryPublisher.wait-duration=1s

resilience4j.circuitbreaker.instances.inventoryPublisher.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.inventoryPublisher.wait-duration-in-open-state=30s
```

---

## 🧪 UI (Custom API Explorer + Simulator)

**File:**

```
api-explorer.html
```

**Location:**

```
ingestion-service/src/main/resources/static
```

### Features

✅ API Explorer
✅ Multi-device simulation

* RFID
* IOT_DEVICE

✅ Dynamic payload generation
✅ Failure simulation

---

### 🔥 Failure Simulation

**UI Payload:**

```json
{
  "simulateFailure": true
}
```

**Backend Logic:**

```java
if (simulateFailure == true) {
    throw new RuntimeException("Simulated failure");
}
```

👉 Triggers:

* Retry
* Circuit Breaker
* DLQ

---

## 🌐 CORS

```java
@CrossOrigin(origins = "*")
```

---

## 🔄 Complete Flow

### ✅ Success Flow

```
UI → Ingestion → Adapter → Publisher → Inventory → DB
```

### ❌ Failure Flow

```
UI → Ingestion → Publisher → FAIL
    → Retry → FAIL
    → Circuit Breaker
    → DLQ DB
```

### 🔁 Reprocess Flow

```
DLQ → Reprocess API → Publisher → Inventory → DB
```

---

## 🧠 Enterprise Features Covered

* ✅ Adapter Pattern
* ✅ Factory Pattern
* ✅ Canonical Model
* ✅ Microservices Architecture
* ✅ Retry
* ✅ Circuit Breaker
* ✅ DLQ
* ✅ Idempotency
* ✅ Metrics
* ✅ Actuator
* ✅ Externalized Config
* ✅ UI-based Simulator

---

## ❗ Future Enhancements

* Kafka integration
* PostgreSQL migration
* Prometheus + Grafana
* Unit tests
* Authentication (JWT/OAuth2)
* API Gateway

---

## 🚀 CI/CD (GitLab)

Pipeline defined in:

```
.gitlab-ci.yml
```

### Stages:

* Validate
* Build
* Test
* Package

---

## 🧠 Interview One-Liner

> Built a scalable IoT event processing platform using Spring Boot microservices with adapter-based payload normalization, Resilience4j-based retry and circuit breaker, DLQ with reprocessing capability, idempotent persistence, and a custom UI simulator for multi-device event testing.

---

## 👩‍💻 Author

**Kanchana K M**

---
