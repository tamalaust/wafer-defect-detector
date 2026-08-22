package com.iotml.wdd.predict;

import com.iotml.wdd.ingestion.entity.SensorReading;
import com.iotml.wdd.ingestion.repository.SensorReadingRepository;
import com.iotml.wdd.model.ModelBundle;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import smile.data.DataFrame;
import smile.data.Tuple;

import java.time.LocalDateTime;

/**
 * NOTE: the DataFrame/Tuple prediction call below is written against
 * Smile's documented SoftClassifier pattern -- predict(Tuple x, double[]
 * posteriori) fills posteriori with class probabilities and returns the
 * predicted label. Like ModelTrainer, this is the part most likely to need
 * adjusting against your exact Smile version; compile and share any errors.
 *
 * @Transactional here (not just on the repository call) is what lets
 * reading.getFeatures() -- a lazy collection -- load successfully inside
 * FeatureVectorBuilder without a LazyInitializationException.
 *
 * Every call to predict() now persists a Prediction row -- both /api/predict
 * and the scheduled batch job share this one method, so results are always
 * recorded regardless of which path triggered the scoring.
 */
@Service
public class PredictionService {

    private static final String MODEL_VERSION = "rf-v1";

    private final SensorReadingRepository sensorReadingRepository;
    private final PredictionRepository predictionRepository;
    private final ModelHolder modelHolder;
    private final ApplicationEventPublisher eventPublisher;
    private final FeatureVectorBuilder vectorBuilder = new FeatureVectorBuilder();

    public PredictionService(SensorReadingRepository sensorReadingRepository,
                             PredictionRepository predictionRepository,
                             ModelHolder modelHolder,
                             ApplicationEventPublisher eventPublisher) {
        this.sensorReadingRepository = sensorReadingRepository;
        this.predictionRepository = predictionRepository;
        this.modelHolder = modelHolder;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PredictionResult predict(Long readingId) {
        SensorReading reading = sensorReadingRepository.findById(readingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No reading found for id " + readingId));

        ModelBundle bundle = modelHolder.getModelBundle();

        double[] vector = vectorBuilder.build(reading, bundle.featureMeans(), bundle.featureCount());
        Tuple tuple = toTuple(vector, bundle.featureCount());

        double[] posteriori = new double[2];
        int label = bundle.model().predict(tuple, posteriori);

        String predictionLabel = (label == 1) ? "FAIL" : "PASS";
        double confidence = posteriori[label];

        LocalDateTime scoredAt = LocalDateTime.now();
        Prediction predictionEntity = new Prediction(
                reading, predictionLabel, confidence, MODEL_VERSION, scoredAt);
        predictionRepository.save(predictionEntity);

        eventPublisher.publishEvent(new PredictionEvent(
                String.valueOf(reading.getId()),
                predictionLabel,
                confidence,
                MODEL_VERSION,
                scoredAt));

        return new PredictionResult(String.valueOf(reading.getId()), predictionLabel, confidence);
    }

    private Tuple toTuple(double[] vector, int featureCount) {
        String[] columnNames = new String[featureCount];
        for (int i = 0; i < featureCount; i++) {
            columnNames[i] = "sensor_" + (i + 1);
        }

        DataFrame singleRow = DataFrame.of(new double[][]{vector}, columnNames);
        return singleRow.get(0);
    }
}