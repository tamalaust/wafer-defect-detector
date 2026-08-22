# Wafer Defect Detector

Semiconductor wafer pass/fail classification service — ingestion, validation, and scoring pipeline for SECOM sensor data, built Java-heavy with in-JVM ML (no Python microservice).

 🚧 **Work in Progress**

## Planned Tech Stack

- **Backend:** Java, Spring Boot
- **Storage:** PostgreSQL (dev/prod)
- **ML:** Smile (pure Java ML library — Random Forest / Logistic Regression)
- **Dataset:** SECOM (UCI ML Repository)
- **Live updates:** Server-Sent Events (Spring `SseEmitter`)
- **Dashboard:** React + Recharts/Chart.js — live pass/fail counter, scrolling prediction feed, confidence histogram
- **Deployment:** Docker, Docker Compose, Kubernetes

## Roadmap

- [x] SECOM CSV ingestion pipeline (`/api/sensor-readings`)
- [x] Validation (missing value handling, range checks)
- [x] Offline model training with Smile
- [x] Prediction endpoint (`/api/predict`)
- [x] Scheduled batch scoring (streaming-style simulation)
- [ ] Unit tests (happy path)
- [x] Architecture, sequence, and user diagrams
- [ ] Full API documentation
- [x] Live event stream (`/api/events/stream`, SSE)
- [x] React dashboard — live pass/fail counter
- [x] React dashboard — scrolling feed + confidence histogram
- [ ] Dockerized deployment (Docker Compose)

## Known Limitations

- Dashboard state is not seeded on connect — a client joining mid-run starts from zero
  counts until new events arrive. Not addressed in the current scope; may be revisited in
  future development.

