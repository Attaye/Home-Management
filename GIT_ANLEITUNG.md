# GitHub – Finaler Commit

## Voraussetzungen
```bash
git --version   # muss installiert sein
```

## Schritt-für-Schritt

### 1. Status prüfen – welche Dateien wurden geändert?
```bash
git status
```

### 2. Alle Änderungen zum Commit hinzufügen
```bash
git add .
```

### 3. Commit mit aussagekräftiger Nachricht
```bash
git commit -m "feat: Sprint 3 abgeschlossen

- Angular 17 Frontend mit CRUD (Kunden, Ablesungen)
- Login/Logout mit BCrypt + Token-Authentifizierung
- Admin-Panel für Benutzerverwaltung
- Import/Export: JSON, CSV, XML, PDF
- Grafische Auswertungen mit Chart.js (3 Charts)
- XML Schema (XSD + DTD)
- Docker Setup (Frontend + Backend + MariaDB)
- README.md und DEPENDENCIES.md Dokumentation
- UML-Sequenzdiagramme (CRUD + Login)"
```

### 4. Auf GitHub pushen
```bash
git push origin main
# oder falls Branch anders heißt:
git push origin Attaye-patch-2
```

### 5. Prüfen ob alles hochgeladen ist
```bash
git log --oneline -5
```

---

## Wichtige Dateien die committet werden sollten

```
✅ src/                          ← Java Backend
✅ home-management/src/          ← Angular Frontend
✅ home-management/package.json  ← npm Dependencies
✅ pom.xml                       ← Maven Dependencies
✅ docker/docker-compose.yml     ← Docker Setup
✅ Dockerfile.frontend           ← Frontend Docker
✅ Dockerfile.backend            ← Backend Docker
✅ nginx.conf                    ← Nginx Konfiguration
✅ README.md                     ← Dokumentation
✅ DEPENDENCIES.md               ← Libraries
✅ hausverwaltung.xsd            ← XML Schema
✅ hausverwaltung.dtd            ← DTD Schema
✅ sequenzdiagramm.puml          ← UML Diagramme
✅ .gitignore                    ← Git Ignore

❌ target/                       ← NICHT committen (Maven Build)
❌ node_modules/                 ← NICHT committen (npm)
❌ dist/                         ← NICHT committen (Angular Build)
```
