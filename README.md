# 📥 mga-messaging-consumer-service

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot)](https://spring.io/)
[![Java Version](https://img.shields.io/badge/Java-17-007396?logo=openjdk)](https://openjdk.org/)
[![Build Tool](https://img.shields.io/badge/Build-Apache%20Maven%203.9.x-C71A36?logo=apachemaven)](https://maven.apache.org/)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-Aiven%20Cloud-231F20?logo=apachekafka)](https://aiven.io/)

## 📌 Service Overview

`mga-messaging-consumer-service` is the event-driven consumer microservice that listens to Kafka topics hosted on Aiven Cloud.

It processes inbound domain events asynchronously and triggers real-time mobile push notifications to the MGA Members App and MGA Admin & Trainer App via Firebase Cloud Messaging (FCM).

### 🏛️ Architectural Features
* Event Listener Pipeline: Consumes asynchronous domain event messages continuously from dedicated Apache Kafka topics.
* Firebase FCM Integration: Integrates with Firebase Admin SDK to format and dispatch targeted push notifications to mobile devices.
* Standardized Data Contract: Imports domain models from `mga-all-models-jars` to ensure seamless deserialization of incoming event contracts.

---

## 🛠️ Technical Specifications
* Port: 8090
* Language/Build: Java 17 | Apache Maven 3.9.x
* Event Source: Apache Kafka (Aiven Cloud Managed Kafka)
* Integration: Firebase Cloud Messaging (FCM SDK)
* Shared Contract: mga-all-models-jars

---

## ⚙️ Build & Deployment Instructions
# Standard JVM Build & Run
mvn clean package
java -jar target/mga-messaging-consumer-service.jar

# Spring Boot Run
mvn spring-boot:run

```
