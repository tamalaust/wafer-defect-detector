# Wafer Defect Detector — Live Dashboard

Real-time view of wafer pass/fail predictions, fed by a Server-Sent Events (SSE) stream
from the `wafer-defect-detector` backend.

> 🚧 **Work in progress** — built alongside the backend's prediction pipeline; not yet
> tested against a full production run.

---

## What it shows

- **Live metrics** — total readings scored, pass count, fail count, fail rate, and a
  rolling predictions/min throughput figure
- **Confidence distribution** — a live-updating histogram (10 buckets, 0.0–1.0) of model
  confidence across all predictions received
- **Live feed** — most recent predictions, newest first, color-coded pass/fail with
  confidence shown per row

All three update from a single SSE connection — no polling, no separate API calls per
view.

---

## Tech Stack

- React (functional components, hooks — `useState`, `useEffect`, `useRef`)
- Native browser `EventSource` API for the SSE connection (no external SSE library)
- No chart library dependency — metrics and the histogram are plain CSS/inline styles

## Backend connection

The dashboard connects to:

```
http://localhost:8080/api/predictions/stream
```

hardcoded as an absolute URL in `PredictionDashboard.jsx` (not relative — the dev server
and the backend run on different origins/ports, so a relative path would resolve against
the frontend's own dev server instead of the backend).

The backend's `sensor_reading`/`sensor_feature`/`prediction` pipeline must already be
running and actively scoring readings (via `/api/predict` or the scheduled batch job) for
anything to appear — the dashboard only displays events as they're broadcast; it does not
trigger scoring itself.

**CORS:** the backend's `CorsConfig` must allow the dashboard's origin
(`http://localhost:5173` for Vite, `http://localhost:3000` for CRA) — already configured
on the backend for both.

---

## Live Indicators

Ambient decorative animation (a sweeping gradient band, a pulsing status dot) was removed
in favor of things that reflect *real* activity rather than looping regardless of what's
happening:

- **Live feed row entrance** (`rowIn` keyframe) — new predictions fade in and slide up
  slightly as they're added to the top of the feed. Runs once per row, only when a real
  event arrives (React remounts each `<li>` since its `key` changes every update, so the
  animation naturally replays without manual reset logic).
- **"Next tick in Ns" countdown** — next to the LIVE/DISCONNECTED indicator, counts down
  every second and resets to 5 whenever a real prediction event arrives. This is the
  clearest signal to someone browsing the repo that the page is actively running, not a
  static screenshot — a number visibly changing each second is harder to mistake for a
  frozen page than ambient CSS motion.

**Accuracy note:** the countdown approximates the backend's `wdd.batch.interval-ms`
(default 5s) but isn't a true sync — Spring's `@Scheduled(fixedDelay=...)` measures from
when the *previous* tick finished, not a fixed clock, and the frontend has no way to
observe that moment directly. Resetting to 5 whenever a real event arrives keeps it close
enough in practice (drifts by at most a couple seconds, then self-corrects on the next
real batch).

## Known Limitations

- No state-seeding on connect — a client that connects mid-run starts at zero counts;
  historical predictions from before the connection was opened aren't backfilled. Not
  addressed in current scope; may be revisited in future development.
- Backend URL is hardcoded, not environment-configurable — fine for local development,
  would need externalizing (e.g. `.env` + `import.meta.env`) before any deployed use.
- No authentication on the SSE endpoint or this dashboard.