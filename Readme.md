# 📘 Meter Reading Backend – Java, Javalin & MariaDB

Dieses Backend implementiert ein REST‑API zur Verwaltung von Zählerständen (Readings) für Kunden.  
Es wurde mit **Java**, **Javalin** und **MariaDB** entwickelt und unterstützt vollständige CRUD‑Operationen sowie eine flexible Filter‑Abfrage.

---

## 🚀 Technologien

- **Java 17**
- **Javalin** (leichtgewichtiges Webframework)
- **MariaDB** / MySQL
- **JDBC**
- **Maven**

---

## 📂 Projektstruktur

src/main/java/
│
├── controller/
│   ├── ReadingController.java
│   ├── CustomerController.java
│   └── SetupController
├── dao/
│   ├── ReadingDao.java
│   ├── CustomerDao.java (falls vorhanden)
│   
├── databaseconnection/
│   ├── IdatabaseConnection
│   └── DatabaseConnection.java
│
├── model/
│   ├── Reading.java
│   ├── Customer.java (falls vorhanden)
│   ├── KindOfMeter.java
│   ├── ReadingResponse.java
│   └── CustomerResponse.java
│
├── util/
│   ├── ValidationUtil.java (optional)
│   ├── JsonUtil.java (optional)
│   └── DateUtil.java (optional)
│
├── Vorlage
│   ├── App.java
│   ├── CustomerMain.java (nur für Customer Dao Test Sprint-1)
│   ├── ReadingMain.java (nur für Reading Dao Test Sprint-1)
│   └──  Server.java

---

## 🗄️ Datenbankmodell

### Tabelle: `reading`

```sql
CREATE TABLE reading (
  id CHAR(36) PRIMARY KEY,
  date_of_reading DATE,
  meter_count DOUBLE,
  meter_id VARCHAR(100),
  comment TEXT,
  kind_of_meter ENUM('HEIZUNG','STROM','WASSER','UNBEKANNT'),
  customer_id CHAR(36),
  substitute TINYINT(1),
  FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE SET NULL
);
```
### Tabelle: `customer`

``````sql
CREATE TABLE customer (
  id CHAR(36) PRIMARY KEY,
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  gender ENUM('D', 'M', 'W', 'U'),
  birth_date DATE
);
``````   
### Query: `Testen getfilitered von Reading`

``````sql
SELECT *
FROM reading
WHERE customer_id = 'ec617965-88b4-4721-8158-ee36c38e4db3'
  AND date_of_reading >= '2020-08-20'
  AND date_of_reading <= '2020-12-31'
  AND kind_of_meter = "WASSER";
``````     

---

## 📁 controller/

Die Controller-Schicht enthält alle REST‑Endpoints.  
Sie ist verantwortlich für:

- Entgegennahme von HTTP‑Requests
- Validierung von Parametern
- Fehlerbehandlung
- Rückgabe von JSON‑Antworten
- Aufruf der DAO‑Methoden

### **ReadingController.java**
- POST `/readings`
- PUT `/readings`
- GET `/readings/{uuid}`
- DELETE `/readings/{uuid}`
- GET `/readings`
- GET `/readingfiltered`

### **CustomerController.java** 
- POST `/customers`
- PUT  `/customers`
- GET `/customers`
- GET `/customers/{uuid}`
- DELETE `/customers/{uuid}`

---



## 📁 dao/

Die DAO-Schicht (Data Access Objects) ist für den **direkten Datenbankzugriff** zuständig.

### **ReadingDao.java**
- CRUD‑Operationen für Readings
- Dynamische Filter‑Query
- `mapRow()` zur Umwandlung von SQL‑Daten in Java‑Objekte

### **CustomerDao.java**
- CRUD‑Operationen für Kunden
- Suche nach Kunden
- Validierung von Fremdschlüsseln

**Verantwortung:**  
➡️ Kommunikation mit MariaDB über JDBC

---

## 📁 databaseconnection/

### **DatabaseConnection.java**
- Baut die MariaDB‑Verbindung auf
- Erstellt Tabellen automatisch, falls sie fehlen
- Stellt eine Singleton‑Connection bereit
- Verhindert mehrfaches Öffnen der DB‑Verbindung

**Verantwortung:**  
➡️ Datenbank‑Setup & Connection‑Management

---

## 📁 model/

Enthält alle Datenmodelle (Entities), die im gesamten Projekt verwendet werden.

### **Reading.java**
- Repräsentiert einen Zählerstand
- Wird in Controller, DAO und JSON‑Serialisierung genutzt

### **Customer.java** *(falls vorhanden)*
- Repräsentiert einen Kunden

### **KindOfMeter.java**
- Enum: `WASSER`, `STROM`, `HEIZUNG`, `UNBEKANNT`

### **ReadingResponse.java**
- Wrapper für POST‑Requests
- Erwartet JSON‑Struktur:
  ```json
  { "reading": { ... } }
  
---
📄 App.java

- Der Einstiegspunkt des Backends.
- Verantwortlich für:
- Starten des Javalin‑Servers
- Registrieren aller Controller
- Initialisieren der DAOs
- Aufbau der Datenbankverbindung

Beispiel:

````java
public class App {
public static void main(String[] args) {
Javalin app = Javalin.create().start(8080);

        DatabaseConnection db = DatabaseConnection.getInstance();
        ReadingDao readingDao = new ReadingDao(db.getConnection());

        new ReadingController(app, readingDao);
    }
}
````

🧱 Architekturprinzipien

Separation of Concerns  
Jede Schicht hat eine klar definierte Aufgabe.

Single Responsibility Principle  
Jede Klasse macht genau eine Sache.

Modularität  
Controller ↔ DAO ↔ Model sind sauber getrennt.

Erweiterbarkeit  
Neue Endpoints oder Tabellen können leicht ergänzt werden.

Testbarkeit  
DAO und Controller können separat getestet werden.

🧭 Datenfluss (Request → Response)
````
Client → Controller → DAO → MariaDB → DAO → Controller → JSON Response  
````
🧪 Beispiel: Filter‑Request
````
GET /readingfiltered?customer=UUID&start=2020-08-20&end=2020-12-31&kindOfMeter=WASSER
````
✔️ Fazit

Das Projekt ist klar strukturiert, modular aufgebaut und leicht erweiterbar.
Die Architektur trennt API‑Logik, Datenbankzugriff und Datenmodelle sauber voneinander und bietet eine solide Grundlage für weitere Features.

---
```` code 
Wenn du möchtest, kann ich dir zusätzlich:

- eine **Architektur‑Grafik (Diagramm)** erstellen
- eine **Deployment‑Sektion** hinzufügen
- eine **API‑Dokumentation im Swagger‑Stil** schreiben
- oder eine **Frontend‑Integration‑Sektion** ergänzen

Sag einfach Bescheid, Mustafa.
````
