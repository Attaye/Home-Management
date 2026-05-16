# ITP Projekt – Digitale Hausverwaltung

**Autor:** Attaye Mustafa  
**Ausbildung:** Fachinformatiker Anwendungsentwicklung  
**Zeitraum:** 2025/2026  
**Projekttyp:** ITP-Abschlussprojekt

---

## Inhaltsverzeichnis

1. [Projektübersicht](#1-projektübersicht)
2. [Systemanforderungen](#2-systemanforderungen)
3. [Installation & Start](#3-installation--start)
4. [Softwarearchitektur](#4-softwarearchitektur)
5. [Datenbankschema](#5-datenbankschema)
6. [REST-API Endpunkte](#6-rest-api-endpunkte)
7. [Funktionen der GUI](#7-funktionen-der-gui)
8. [Import & Export](#8-import--export)
9. [Authentifizierung](#9-authentifizierung)

---

## 1. Projektübersicht

Die **Digitale Hausverwaltung** ist eine Webanwendung zur Verwaltung von Kunden und deren Zählerablesungen (Strom, Wasser, Heizung). Sie ersetzt die manuelle, papierbasierte Erfassung durch ein zentrales digitales System.

**Kernfunktionen:**
- Kunden und Ablesungen anlegen, bearbeiten, löschen und filtern
- Grafische Auswertung der Messdaten (Charts)
- Import und Export von Daten in JSON, CSV, XML und PDF
- Benutzeranmeldung mit Rollenverwaltung (Admin / User)

---

## 2. Systemanforderungen

| Komponente | Version |
|---|---|
| Java (JDK) | 21+ |
| Maven | 3.8+ |
| Node.js | 18+ |
| Angular CLI | 17+ |
| MariaDB | 10.6+ |
| Docker (optional) | 24+ |

---

## 3. Installation & Start

### 3.1 Datenbank starten (Docker)

```bash
cd docker
docker-compose up -d
```

Die Datenbank ist erreichbar unter:
- **Host:** `localhost:3306`
- **Datenbank:** `itp-datenbank`
- **Benutzer:** `admin`
- **Passwort:** `itpmariadb2025`

### 3.2 Backend starten

```bash
# Im Projektverzeichnis (wo pom.xml liegt)
mvn clean install
mvn exec:java -Dexec.mainClass="vorlage.App"
```

Der Server startet auf **http://localhost:8080**

Beim ersten Start werden automatisch:
- Alle Tabellen angelegt (`customer`, `reading`, `users`)
- Ein Standard-Admin angelegt: **admin / admin123**

### 3.3 Frontend starten

```bash
cd home-management
npm install
ng serve
```

Die Anwendung ist erreichbar unter **http://localhost:4200**

### 3.4 Erster Login

| Feld | Wert |
|---|---|
| Benutzername | `admin` |
| Passwort | `admin123` |

---

## 4. Softwarearchitektur

```
┌─────────────────────────────────────────────────────────┐
│                    FRONTEND (Angular 17)                 │
│  ┌──────────┐ ┌──────────┐ ┌───────────┐ ┌──────────┐  │
│  │Dashboard │ │ Kunden   │ │Ablesungen │ │ Reports  │  │
│  └──────────┘ └──────────┘ └───────────┘ └──────────┘  │
│  ┌──────────────────────────────────────────────────┐   │
│  │         HTTP + Bearer Token (AuthInterceptor)    │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────┬───────────────────────────────────┘
                      │ REST API (JSON)
                      │ http://localhost:8080
┌─────────────────────▼───────────────────────────────────┐
│                    BACKEND (Javalin + JDBC)              │
│  ┌─────────────┐ ┌──────────────┐ ┌──────────────────┐  │
│  │CustomerCtrl │ │ ReadingCtrl  │ │   AuthController  │  │
│  └─────────────┘ └──────────────┘ └──────────────────┘  │
│  ┌─────────────┐ ┌──────────────┐ ┌──────────────────┐  │
│  │ImportCtrl   │ │  UserCtrl    │ │  SetupController  │  │
│  └─────────────┘ └──────────────┘ └──────────────────┘  │
│  ┌──────────────────────────────────────────────────┐   │
│  │              JDBC (MariaDbConnection)            │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────┬───────────────────────────────────┘
                      │ JDBC
┌─────────────────────▼───────────────────────────────────┐
│                   MariaDB (Docker)                       │
│         customer | reading | users                       │
└─────────────────────────────────────────────────────────┘
```

### Paketstruktur Backend

```
src/main/java/
├── controller/
│   ├── AuthController.java       ← Login, Logout, /auth/me
│   ├── CustomerController.java   ← CRUD Kunden
│   ├── ReadingController.java    ← CRUD Ablesungen + Filter
│   ├── ImportController.java     ← PDF/CSV Datei-Import
│   ├── UserController.java       ← Admin: User anlegen/löschen
│   └── SetupController.java      ← DB zurücksetzen
├── dao/
│   ├── CustomerDao.java          ← JDBC Kunden
│   ├── ReadingDao.java           ← JDBC Ablesungen
│   └── UserDao.java              ← JDBC Benutzer
├── model/
│   ├── Customer.java
│   ├── Reading.java
│   ├── User.java
│   ├── KindOfMeter.java          ← Enum: STROM, WASSER, HEIZUNG
│   └── Gender.java               ← Enum: M, W, D, U
├── dto/
│   ├── CustomerResponse.java
│   ├── ReadingResponse.java
│   ├── LoginRequest.java
│   └── LoginResponse.java
├── databaseconnection/
│   ├── Idatabaseconnection.java
│   └── MariaDbConnection.java
└── util/
    ├── PasswordUtil.java         ← BCrypt Hashing
    ├── TokenStore.java           ← In-Memory Token-Verwaltung
    ├── DataConverter.java
    └── Importedcsvreader.java    ← Sprint-CSV Parser
```

### Paketstruktur Frontend

```
src/app/
├── core/
│   ├── models/          ← TypeScript Interfaces
│   ├── services/        ← HTTP-Services (Customer, Reading, Auth)
│   ├── guards/          ← AuthGuard
│   └── interceptors/    ← AuthInterceptor (Bearer Token)
├── features/
│   ├── auth/            ← Login-Seite
│   ├── dashboard/       ← Startseite mit Stats
│   ├── customers/       ← Kunden CRUD
│   ├── readings/        ← Ablesungen CRUD
│   ├── auswertung/      ← Charts (Chart.js)
│   ├── reports/         ← Import & Export
│   └── admin/           ← Benutzerverwaltung
└── shared/
    ├── components/      ← Navbar
    └── pipes/           ← KindCountPipe
```

---

## 5. Datenbankschema

```sql
-- Kunden
CREATE TABLE customer (
    id         CHAR(36)              PRIMARY KEY,
    first_name VARCHAR(100),
    last_name  VARCHAR(100),
    gender     ENUM('D','M','W','U'),
    birth_date DATE
);

-- Ablesungen
CREATE TABLE reading (
    id              CHAR(36)     PRIMARY KEY,
    date_of_reading DATE,
    meter_count     DOUBLE,
    meter_id        VARCHAR(100),
    comment         TEXT,
    kind_of_meter   ENUM('HEIZUNG','STROM','WASSER','UNBEKANNT'),
    customer_id     CHAR(36)     NULL,
    substitute      BOOLEAN,
    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE SET NULL
);

-- Benutzer
CREATE TABLE users (
    id       CHAR(36)     NOT NULL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,   -- BCrypt Hash
    role     VARCHAR(20)  NOT NULL DEFAULT 'USER'
);
```

**Beziehungen:**
- `reading.customer_id` → `customer.id` (n:1, ON DELETE SET NULL)
- Ein Kunde kann mehrere Ablesungen haben
- Wird ein Kunde gelöscht, bleiben Ablesungen erhalten (`customer_id = NULL`)

---

## 6. REST-API Endpunkte

**Base URL:** `http://localhost:8080`

### Authentifizierung
| Methode | Endpunkt | Beschreibung |
|---|---|---|
| POST | `/auth/login` | Anmelden → gibt Token zurück |
| POST | `/auth/logout` | Abmelden → Token wird gelöscht |
| GET | `/auth/me` | Eigene Benutzerinfos abrufen |

### Kunden
| Methode | Endpunkt | Beschreibung |
|---|---|---|
| GET | `/customers` | Alle Kunden abrufen |
| GET | `/customers/{uuid}` | Einzelnen Kunden abrufen |
| POST | `/customers` | Neuen Kunden anlegen |
| PUT | `/customers` | Kunden aktualisieren |
| DELETE | `/customers/{uuid}` | Kunden löschen |

### Ablesungen
| Methode | Endpunkt | Beschreibung |
|---|---|---|
| GET | `/readings` | Alle Ablesungen |
| GET | `/readings/{uuid}` | Einzelne Ablesung |
| GET | `/readingfiltered` | Gefiltert nach Kunde/Datum/Art |
| POST | `/readings` | Neue Ablesung anlegen |
| PUT | `/readings` | Ablesung aktualisieren |
| DELETE | `/readings/{uuid}` | Ablesung löschen |

### Import & Setup
| Methode | Endpunkt | Beschreibung |
|---|---|---|
| POST | `/import` | Datei-Upload (PDF/CSV) |
| DELETE | `/setupDB` | Datenbank zurücksetzen |

### Admin
| Methode | Endpunkt | Beschreibung |
|---|---|---|
| POST | `/admin/users` | Neuen Benutzer anlegen |
| DELETE | `/admin/users/{username}` | Benutzer löschen |

---

## 7. Funktionen der GUI

### Dashboard
- Statistik-Kacheln: Anzahl Kunden, Ablesungen, Schätzwerte, aktiv heute
- Letzte 6 Ablesungen mit Kundennamen
- Top-5 Kunden nach Ablesungsanzahl (Balkendiagramm)

### Kunden
- Liste mit Suchfunktion
- Detailansicht mit allen Ablesungen des Kunden
- Formular zum Erstellen und Bearbeiten
- Löschen mit Bestätigung

### Ablesungen
- Liste mit Filtern: Zählerart (Strom/Wasser/Heizung), Datum von/bis, Kundenname
- Detailansicht mit Kundeninformation
- Formular mit Kundenzuordnung, Zählerstand, Schätzwert-Toggle

### Auswertungen
- **Donut-Chart:** Verteilung der Ablesungen nach Zählerart
- **Linien-Chart:** Zählerstandsverlauf über Zeit (filterbar)
- **Balken-Chart:** Top-10 Kunden nach Ablesungsanzahl

### Reports
- Export als JSON, CSV oder XML
- Import von JSON, CSV, XML (App-Format) und PDF (Sprint-Format)
- Import-Historie der aktuellen Sitzung

---

## 8. Import & Export

### Export-Formate

**JSON** – Array oder kombiniertes Objekt:
```json
{
  "customers": [{ "id": "...", "firstName": "...", ... }],
  "readings":  [{ "id": "...", "kindOfMeter": "STROM", ... }]
}
```

**CSV** – Mit Sektions-Header:
```
# KUNDEN
id;firstName;lastName;gender;birthDate

# ABLESUNGEN
id;customerId;dateOfReading;kindOfMeter;meterCount;...
```

**XML** – Validierbar gegen `hausverwaltung.xsd` / `hausverwaltung.dtd`:
```xml
<export>
  <customers><customer>...</customer></customers>
  <readings><reading>...</reading></readings>
</export>
```

### Sprint-PDF Import
Die Sprint-Datendateien (`heizung.pdf`, `strom.pdf`, `wasser.pdf`) werden mit **Apache PDFBox** eingelesen und direkt in die Datenbank importiert.

---

## 9. Authentifizierung

- Passwörter werden mit **BCrypt** (Cost-Faktor 12) gehasht
- Nach Login wird ein **UUID-Token** generiert und im Server-Speicher abgelegt
- Der Token wird im `localStorage` des Browsers gespeichert
- Jeder HTTP-Request sendet automatisch `Authorization: Bearer <token>`
- Geschützte Routen werden durch den `AuthGuard` gesichert

**Rollen:**
| Rolle | Rechte |
|---|---|
| `USER` | Alle CRUD-Operationen |
| `ADMIN` | Zusätzlich: Benutzerverwaltung |

---

*ITP Projekt 2025/2026 – Attaye Mustafa*
