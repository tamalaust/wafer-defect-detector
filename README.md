# Wafer Defect Detector

Semiconductor wafer pass/fail classification service — ingestion, validation, and scoring pipeline for SECOM sensor data, built Java-heavy with in-JVM ML (no Python microservice).

> 🚧 **Work in Progress** — actively being built. Full API documentation, architecture diagrams, and setup instructions coming soon.

## Planned Tech Stack

- **Backend:** Java, Spring Boot
- **Storage:** PostgreSQL (dev/prod)
- **ML:** Smile (pure Java ML library — Random Forest / Logistic Regression)
- **Dataset:** SECOM (UCI ML Repository)
- **Live updates:** Server-Sent Events (Spring `SseEmitter`)
- **Dashboard:** React + Recharts/Chart.js — live pass/fail counter, scrolling prediction feed, confidence histogram
- **Deployment:** Docker, Docker Compose, Kubernetes

## Roadmap

- [ ] SECOM CSV ingestion pipeline (`/api/sensor-readings`)
- [ ] Validation (missing value handling, range checks)
- [ ] Offline model training with Smile
- [ ] Prediction endpoint (`/api/predict`)
- [ ] Scheduled batch scoring (streaming-style simulation)
- [ ] Unit tests (happy path)
- [ ] Architecture, sequence, and user diagrams
- [ ] Full API documentation
- [ ] Live event stream (`/api/events/stream`, SSE)
- [ ] React dashboard — live pass/fail counter
- [ ] React dashboard — scrolling feed + confidence histogram
- [ ] Dockerized deployment (Docker Compose)

## Known Limitations

- Dashboard state is not seeded on connect — a client joining mid-run starts from zero
  counts until new events arrive. Not addressed in the current scope; may be revisited in
  future development.

## Status

Currently in early development — Week 1 (ingestion & validation). Star/watch this repo for updates.
