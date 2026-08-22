package com.iotml.wdd.predict;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/predict")
public class PredictController {

    private static final String MODEL_VERSION = "rf-v1";

    private final PredictionService predictionService;

    public PredictController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping
    public ResponseEntity<PredictResponse> predict(@RequestBody PredictRequest request) {
        PredictionResult result = predictionService.predict(Long.valueOf(request.readingId()));

        PredictResponse response = new PredictResponse(
                result.readingId(),
                result.prediction(),
                result.confidence(),
                MODEL_VERSION
        );

        return ResponseEntity.ok(response);
    }
}