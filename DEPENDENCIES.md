# ITP Projekt – Abhängigkeiten & Technologien

**Projekt:** Digitale Hausverwaltung  
**Autor:** Attaye Mustafa

---

## Backend (Java / Maven)

### Frameworks & Server

| Library | Version | Zweck |
|---|---|---|
| **Javalin** | 5.6.3 | Leichtgewichtiger HTTP-Server für REST-API |
| **SLF4J** | (via Javalin) | Logging-Framework |

### Datenbank

| Library | Version | Zweck |
|---|---|---|
| **MariaDB JDBC Driver** | 3.x | JDBC-Treiber für MariaDB-Datenbankverbindung |
| **MariaDB** | 10.6+ | Relationale Datenbank (läuft in Docker) |

### JSON / Serialisierung

| Library | Version | Zweck |
|---|---|---|
| **Jackson Databind** | (via Javalin) | JSON Serialisierung/Deserialisierung |
| **Jackson Datatype JSR310** | 2.x | Unterstützung für Java `LocalDate` → `"2024-01-01"` |

### Sicherheit

| Library | Version | Zweck |
|---|---|---|
| **jBCrypt** | 0.4 | Passwort-Hashing mit BCrypt (Cost-Faktor 12) |

### PDF-Verarbeitung

| Library | Version | Zweck |
|---|---|---|
| **Apache PDFBox** | 2.0.31 | Text aus Sprint-PDF-Dateien extrahieren |

### Build & Tooling

| Tool | Version | Zweck |
|---|---|---|
| **Apache Maven** | 3.8+ | Build-Management, Dependency-Verwaltung |
| **JDK** | 21 | Java Development Kit |

### `pom.xml` Dependencies (Kurzübersicht)
```xml
<dependency> javalin 5.6.3 </dependency>
<dependency> mariadb-java-client </dependency>
<dependency> jackson-datatype-jsr310 </dependency>
<dependency> jbcrypt 0.4 </dependency>
<dependency> pdfbox 2.0.31 </dependency>
```

---

## Frontend (TypeScript / Node.js)

### Framework & Core

| Library | Version | Zweck |
|---|---|---|
| **Angular** | 17.3 | SPA-Framework (Standalone Components, Signals) |
| **TypeScript** | 5.4 | Typsichere JavaScript-Erweiterung |
| **RxJS** | 7.8 | Reaktive Programmierung (Observables, forkJoin) |
| **Zone.js** | 0.14 | Angular Change-Detection |

### UI & Styling

| Library | Version | Zweck |
|---|---|---|
| **SCSS** | (via Angular) | CSS-Präprozessor für Styling |
| **Chart.js** | 4.x | Grafische Auswertungen (Donut, Line, Bar Charts) |

### Angular Module (intern genutzt)

| Modul | Zweck |
|---|---|
| `@angular/router` | Client-seitiges Routing mit Lazy Loading |
| `@angular/common/http` | HTTP-Client für REST-API Aufrufe |
| `@angular/forms` | Reaktive Formulare (`ReactiveFormsModule`) |
| `@angular/platform-browser` | Browser-DOM-Integration |
| `@angular/animations` | Animationen |

### Build & Tooling

| Tool | Version | Zweck |
|---|---|---|
| **Angular CLI** | 17.3 | Projektgenerierung, Build, Dev-Server |
| **Node.js** | 18+ | JavaScript-Laufzeitumgebung |
| **npm** | 9+ | Paketmanager |
| **Webpack** | (via Angular) | Bundling & Code-Splitting |

---

## Infrastruktur

| Technologie | Version | Zweck |
|---|---|---|
| **Docker** | 24+ | Container für MariaDB-Datenbank |
| **Docker Compose** | 2.x | Multi-Container-Orchestrierung |

---

## XML-Schema

| Datei | Format | Zweck |
|---|---|---|
| `hausverwaltung.xsd` | XML Schema Definition | Validierung des XML-Export-Formats |
| `hausverwaltung.dtd` | Document Type Definition | Klassische DTD-Validierung |

---

## Kommunikationsprotokoll

| Technologie | Einsatz |
|---|---|
| **REST** | Kommunikation zwischen Frontend und Backend |
| **JSON** | Datenaustausch-Format für REST-API |
| **HTTP/1.1** | Transportprotokoll |
| **Bearer Token** | Authentifizierung (Authorization-Header) |
| **BCrypt** | Passwort-Hashing im Backend |
| **localStorage** | Token-Speicherung im Browser |

---

## Entwicklungsumgebung

| Tool | Zweck |
|---|---|
| **IntelliJ IDEA** | Backend-Entwicklung (Java) |
| **Angular DevTools** | Frontend-Debugging |
| **Bruno** | API-Testing (REST-Client) |
| **DBeaver / IntelliJ DB** | Datenbank-Verwaltung |
| **Git / GitHub** | Versionsverwaltung |

---

*ITP Projekt 2025/2026 – Attaye Mustafa*
