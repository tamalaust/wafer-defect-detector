package com.iotml.wdd.ingestion.web;

public record BulkIngestResponse(
        int received,
        int saved
) {
}