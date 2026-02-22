# SP - Slimme Bibliotheek (GO! Scholengroep Antwerpen)

Dit project is ontwikkeld in opdracht van GO! Scholengroep Antwerpen.
Het doel van de applicatie is het realiseren van een "Slimme Bibliotheek" voor het beheren en uitlenen van boeken en materialen.

---

## Development

### Frontend

De frontend is gebouwd met Angular.

Zorg dat je Node.js geïnstalleerd hebt.

Start de lokale server met:

```bash
cd frontend
npm install
ng serve
```

---

### Backend

De backend is een Spring Boot applicatie die draait op Java 21.

Gebruik een IDE zoals Visual Studio Code.
Maven wordt gebruikt voor het beheren van dependencies en het builden van het project.

---

### Database

Het project maakt gebruik van een MySQL database.

Maak een database aan genaamd `slimme_bibliotheek`.
Controleer de database-connectiegegevens in:

```
src/main/resources/application.properties
```

---

## Technologieën & Versies

- Backend: Java 21 (Spring Boot)
- Frontend: Angular
- Database: MySQL 8.x
- Containerisatie: Docker
