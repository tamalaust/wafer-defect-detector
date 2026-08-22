import React, { useEffect, useRef, useState } from "react";

/**
 * PredictionDashboard
 * ------------------------------------------------------------------
 * Live view of wafer pass/fail predictions, fed by the real backend
 * SSE stream (GET /api/predictions/stream, named event "prediction").
 *
 * Event shape (PredictionEvent.java):
 *   { readingId, prediction: "PASS"|"FAIL", confidence, modelVersion, scoredAt }
 * ------------------------------------------------------------------
 */

const SSE_URL = "http://localhost:8080/api/predictions/stream";
const SSE_EVENT_NAME = "prediction";
const FEED_MAX_LENGTH = 50;
const HISTOGRAM_BUCKET_COUNT = 10;
const THROUGHPUT_WINDOW_MS = 60000;
// Matches the backend's wdd.batch.interval-ms (default 5000). The countdown
// is an approximation (resynced whenever real events arrive), not a
// perfectly-synced clock -- Spring's fixedDelay measures from the end of
// the previous tick, which this component has no way to observe directly.
const BATCH_INTERVAL_SECONDS = 5;

function bucketIndexFor(confidence) {
  const idx = Math.floor(confidence * HISTOGRAM_BUCKET_COUNT);
  return Math.min(Math.max(idx, 0), HISTOGRAM_BUCKET_COUNT - 1);
}

function emptyHistogram() {
  return Array.from({ length: HISTOGRAM_BUCKET_COUNT }, () => 0);
}

export default function PredictionDashboard() {
  const [totalScored, setTotalScored] = useState(0);
  const [passCount, setPassCount] = useState(0);
  const [failCount, setFailCount] = useState(0);
  const [feed, setFeed] = useState([]);
  const [histogram, setHistogram] = useState(emptyHistogram());
  const [connected, setConnected] = useState(false);
  const [throughput, setThroughput] = useState(0);
  const [nextTickIn, setNextTickIn] = useState(BATCH_INTERVAL_SECONDS);

  const throughputRef = useRef([]);
  const eventSourceRef = useRef(null);

  const connect = () => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
    }

    const es = new EventSource(SSE_URL);
    eventSourceRef.current = es;

    es.onopen = () => setConnected(true);
    es.onerror = () => setConnected(false); // EventSource auto-retries underneath

    es.addEventListener(SSE_EVENT_NAME, (message) => {
      let event;
      try {
        event = JSON.parse(message.data);
      } catch (e) {
        console.error("Failed to parse SSE event", message.data, e);
        return;
      }

      const isFail = event.prediction === "FAIL";

      setTotalScored((prev) => prev + 1);
      setPassCount((prev) => (isFail ? prev : prev + 1));
      setFailCount((prev) => (isFail ? prev + 1 : prev));
      setFeed((prev) => [event, ...prev].slice(0, FEED_MAX_LENGTH));

      setHistogram((prev) => {
        const next = [...prev];
        const idx = bucketIndexFor(event.confidence);
        next[idx] = next[idx] + 1;
        return next;
      });

      throughputRef.current.push(Date.now());
      setNextTickIn(BATCH_INTERVAL_SECONDS); // real event arrived -- resync the countdown
    });
  };

  useEffect(() => {
    connect();
    return () => eventSourceRef.current?.close();
  }, []);

  useEffect(() => {
    const countdown = setInterval(() => {
      setNextTickIn((prev) => (prev <= 1 ? BATCH_INTERVAL_SECONDS : prev - 1));
    }, 1000);
    return () => clearInterval(countdown);
  }, []);

  useEffect(() => {
    const t = setInterval(() => {
      const now = Date.now();
      throughputRef.current = throughputRef.current.filter(
        (ts) => now - ts < THROUGHPUT_WINDOW_MS
      );
      setThroughput(throughputRef.current.length);
    }, 1000);
    return () => clearInterval(t);
  }, []);

  const failRate = totalScored === 0 ? 0 : (failCount / totalScored) * 100;
  const maxHistogramCount = Math.max(...histogram, 1);

  return (
    <div style={styles.page}>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=IBM+Plex+Mono:wght@400;500;600&family=Inter:wght@400;500;600&display=swap');
        @keyframes rowIn {
          from { opacity: 0; transform: translateY(-4px); }
          to { opacity: 1; transform: translateY(0); }
        }
      `}</style>

      <div style={styles.panel}>
        <header style={styles.header}>
          <div>
            <div style={styles.eyebrow}>WAFER DEFECT DETECTION &middot; LIVE PREDICTIONS</div>
            <h1 style={styles.title}>Prediction Monitor</h1>
          </div>
          <div style={styles.statusWrap}>
            <span
              style={{
                ...styles.statusDot,
                background: connected ? "var(--ok)" : "var(--danger)",
              }}
            />
            <span style={styles.statusText}>{connected ? "LIVE" : "DISCONNECTED"}</span>
            {connected && (
              <span style={styles.countdownText}>next tick in {nextTickIn}s</span>
            )}
          </div>
        </header>

        <div style={styles.metricsGrid}>
          <Metric label="Total scored" value={totalScored.toLocaleString()} accent="var(--cyan)" />
          <Metric label="Pass" value={passCount.toLocaleString()} accent="var(--ok)" />
          <Metric label="Fail" value={failCount.toLocaleString()} accent="var(--danger)" />
          <Metric
            label="Fail rate"
            value={`${failRate.toFixed(2)}%`}
            accent={failRate > 15 ? "var(--danger)" : "var(--amber)"}
          />
          <Metric label="Throughput" value={`${throughput}/min`} accent="var(--text-dim)" />
        </div>

        <section style={styles.histogramSection}>
          <div style={styles.feedHeader}>Confidence distribution</div>
          <div style={styles.histogramRow}>
            {histogram.map((count, i) => (
              <div
                key={i}
                style={styles.histogramBarWrap}
                title={`${(i / HISTOGRAM_BUCKET_COUNT).toFixed(1)}-${((i + 1) / HISTOGRAM_BUCKET_COUNT).toFixed(1)}: ${count}`}
              >
                <div
                  style={{
                    ...styles.histogramBar,
                    height: `${(count / maxHistogramCount) * 100}%`,
                  }}
                />
              </div>
            ))}
          </div>
          <div style={styles.histogramAxis}>
            <span>0.0</span>
            <span>0.5</span>
            <span>1.0</span>
          </div>
        </section>

        <section style={styles.feedSection}>
          <div style={styles.feedHeader}>Live feed</div>
          {feed.length === 0 ? (
            <div style={styles.feedEmpty}>Waiting for predictions...</div>
          ) : (
            <ul style={styles.feedList}>
              {feed.slice(0, 8).map((event, i) => (
                <li key={`${event.readingId}-${i}`} style={{ ...styles.feedRow, animation: "rowIn 220ms ease-out" }}>
                  <span
                    style={{
                      ...styles.feedDot,
                      background: event.prediction === "FAIL" ? "var(--danger)" : "var(--ok)",
                    }}
                  />
                  <span
                    style={{
                      ...styles.feedType,
                      color: event.prediction === "FAIL" ? "var(--danger)" : "var(--ok)",
                    }}
                  >
                    {event.prediction}
                  </span>
                  <span style={styles.feedId}>reading #{event.readingId}</span>
                  <span style={styles.feedConfidence}>{(event.confidence * 100).toFixed(0)}%</span>
                </li>
              ))}
            </ul>
          )}
        </section>

        <button type="button" onClick={connect} style={styles.toggleBtn}>
          Reconnect
        </button>
      </div>
    </div>
  );
}

function Metric({ label, value, accent }) {
  return (
    <div style={styles.metricCard}>
      <div style={styles.metricLabel}>{label}</div>
      <div style={{ ...styles.metricValue, color: accent }}>{value}</div>
    </div>
  );
}

const styles = {
  page: {
    "--bg": "#0B0E11",
    "--panel": "#11161B",
    "--panel-2": "#161C22",
    "--border": "#232B32",
    "--cyan": "#4FD1E8",
    "--amber": "#F5A623",
    "--ok": "#3ECF8E",
    "--danger": "#E8555F",
    "--text": "#E8ECEF",
    "--text-dim": "#8A97A3",
    minHeight: "100vh",
    background: "var(--bg)",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    padding: "24px",
    fontFamily: "'Inter', sans-serif",
    color: "var(--text)",
  },
  panel: {
    position: "relative",
    overflow: "hidden",
    width: "100%",
    maxWidth: "680px",
    background: "var(--panel)",
    border: "1px solid var(--border)",
    borderRadius: "10px",
    padding: "28px",
  },
  header: {
    display: "flex",
    alignItems: "flex-start",
    justifyContent: "space-between",
    marginBottom: "24px",
    position: "relative",
  },
  eyebrow: {
    fontFamily: "'IBM Plex Mono', monospace",
    fontSize: "11px",
    letterSpacing: "0.10em",
    color: "var(--text-dim)",
    marginBottom: "6px",
  },
  title: { fontSize: "20px", fontWeight: 600, margin: 0 },
  statusWrap: { display: "flex", alignItems: "center", gap: "8px", marginTop: "4px" },
  statusDot: {
    width: "8px",
    height: "8px",
    borderRadius: "50%",
    display: "inline-block",
  },
  statusText: {
    fontFamily: "'IBM Plex Mono', monospace",
    fontSize: "11px",
    letterSpacing: "0.08em",
    color: "var(--text-dim)",
  },
  countdownText: {
    fontFamily: "'IBM Plex Mono', monospace",
    fontSize: "11px",
    color: "var(--cyan)",
    fontVariantNumeric: "tabular-nums",
  },
  metricsGrid: {
    display: "grid",
    gridTemplateColumns: "repeat(5, 1fr)",
    gap: "10px",
    marginBottom: "20px",
    position: "relative",
  },
  metricCard: {
    background: "var(--panel-2)",
    border: "1px solid var(--border)",
    borderRadius: "8px",
    padding: "12px",
  },
  metricLabel: { fontSize: "11px", color: "var(--text-dim)", marginBottom: "6px" },
  metricValue: {
    fontFamily: "'IBM Plex Mono', monospace",
    fontSize: "20px",
    fontWeight: 600,
    fontVariantNumeric: "tabular-nums",
  },
  histogramSection: {
    borderTop: "1px solid var(--border)",
    paddingTop: "16px",
    marginBottom: "16px",
    position: "relative",
  },
  histogramRow: {
    display: "flex",
    alignItems: "flex-end",
    gap: "4px",
    height: "70px",
    marginTop: "8px",
  },
  histogramBarWrap: {
    flex: 1,
    height: "100%",
    display: "flex",
    alignItems: "flex-end",
  },
  histogramBar: {
    width: "100%",
    minHeight: "2px",
    background: "var(--cyan)",
    borderRadius: "2px 2px 0 0",
    transition: "height 200ms ease-out",
  },
  histogramAxis: {
    display: "flex",
    justifyContent: "space-between",
    fontFamily: "'IBM Plex Mono', monospace",
    fontSize: "10px",
    color: "var(--text-dim)",
    marginTop: "4px",
  },
  feedSection: { borderTop: "1px solid var(--border)", paddingTop: "16px", position: "relative" },
  feedHeader: { fontSize: "12px", color: "var(--text-dim)", marginBottom: "10px" },
  feedEmpty: { fontSize: "13px", color: "var(--text-dim)", fontStyle: "italic" },
  feedList: { listStyle: "none", margin: 0, padding: 0, display: "flex", flexDirection: "column", gap: "8px" },
  feedRow: {
    display: "flex",
    alignItems: "center",
    gap: "10px",
    fontSize: "13px",
    fontFamily: "'IBM Plex Mono', monospace",
  },
  feedDot: { width: "6px", height: "6px", borderRadius: "50%", flexShrink: 0 },
  feedType: { minWidth: "44px", fontWeight: 600 },
  feedId: { color: "var(--text-dim)", flex: 1 },
  feedConfidence: { color: "var(--text-dim)" },
  toggleBtn: {
    marginTop: "20px",
    background: "transparent",
    border: "1px solid var(--border)",
    color: "var(--text-dim)",
    fontSize: "12px",
    padding: "8px 12px",
    borderRadius: "6px",
    cursor: "pointer",
    fontFamily: "'Inter', sans-serif",
  },
};