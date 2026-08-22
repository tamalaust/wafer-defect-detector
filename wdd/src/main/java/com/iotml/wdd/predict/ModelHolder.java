package com.iotml.wdd.predict;

import com.iotml.wdd.model.ModelBundle;
import com.iotml.wdd.model.ModelPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Loads the trained model once at application startup and keeps it in
 * memory for the lifetime of the app -- per your call, not reloaded from
 * disk on every /api/predict request.
 */
@Component
public class ModelHolder {

    private final ModelBundle modelBundle;

    public ModelHolder(@Value("${wdd.model.path:models/wafer-defect-rf-v1.model}") String modelPath) {
        try {
            this.modelBundle = new ModelPersistence().load(Path.of(modelPath));
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to load model from " + modelPath
                            + " - has TrainerApp been run yet?", e);
        }
    }

    public ModelBundle getModelBundle() {
        return modelBundle;
    }
}