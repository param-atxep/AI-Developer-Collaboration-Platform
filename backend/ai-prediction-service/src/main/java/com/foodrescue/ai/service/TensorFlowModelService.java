package com.foodrescue.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service responsible for loading and running TensorFlow SavedModel inference.
 * Includes robust fallback logic when the TensorFlow model file is unavailable.
 */
@Service
public class TensorFlowModelService {

    private static final Logger log = LoggerFactory.getLogger(TensorFlowModelService.class);

    @Value("${ai.model.path:models/waste_prediction_model}")
    private String modelPath;

    @Value("${ai.model.version:1.0.0}")
    private String modelVersion;

    private final AtomicBoolean modelLoaded = new AtomicBoolean(false);
    private Object savedModelBundle; // org.tensorflow.SavedModelBundle when available

    @PostConstruct
    public void init() {
        loadModel();
    }

    /**
     * Attempt to load the TensorFlow SavedModel from the configured path.
     * If the model is not found or TensorFlow is not available, the service
     * gracefully degrades to signal callers to use the statistical fallback.
     */
    public void loadModel() {
        try {
            Path path = Paths.get(modelPath);
            if (!Files.exists(path)) {
                log.warn("TensorFlow model not found at path: {}. " +
                        "Statistical fallback will be used for predictions.", modelPath);
                modelLoaded.set(false);
                return;
            }

            // Attempt to load the model using reflection so the service compiles
            // even if TensorFlow native libraries are not present at runtime.
            Class<?> bundleClass = Class.forName("org.tensorflow.SavedModelBundle");
            var loadMethod = bundleClass.getMethod("load", String.class, String[].class);
            savedModelBundle = loadMethod.invoke(null, modelPath, new String[]{"serve"});
            modelLoaded.set(true);
            log.info("TensorFlow model loaded successfully from: {} (version: {})", modelPath, modelVersion);

        } catch (ClassNotFoundException e) {
            log.warn("TensorFlow runtime not available on classpath. " +
                    "Statistical fallback will be used for predictions.");
            modelLoaded.set(false);
        } catch (UnsatisfiedLinkError e) {
            log.warn("TensorFlow native libraries not found. " +
                    "Statistical fallback will be used for predictions. Error: {}", e.getMessage());
            modelLoaded.set(false);
        } catch (Exception e) {
            log.error("Failed to load TensorFlow model from: {}. " +
                    "Statistical fallback will be used. Error: {}", modelPath, e.getMessage());
            modelLoaded.set(false);
        }
    }

    /**
     * Run inference on the loaded TensorFlow model.
     *
     * @param features array of input features:
     *                 [dayOfWeek, month, isHoliday, historicalAvg, recentTrend,
     *                  weatherCode, specialEventFlag]
     * @return predicted waste in kg, or -1.0 if the model is unavailable
     */
    public double predict(float[] features) {
        if (!modelLoaded.get() || savedModelBundle == null) {
            return -1.0;
        }

        try {
            // Use reflection-based TensorFlow inference so compilation succeeds
            // even without TensorFlow native libraries.
            Class<?> bundleClass = savedModelBundle.getClass();
            var sessionMethod = bundleClass.getMethod("session");
            Object session = sessionMethod.invoke(savedModelBundle);

            Class<?> sessionClass = session.getClass();
            var runnerMethod = sessionClass.getMethod("runner");
            Object runner = runnerMethod.invoke(session);

            // Build input tensor
            Class<?> tensorClass = Class.forName("org.tensorflow.Tensor");
            Class<?> tensorsClass = Class.forName("org.tensorflow.Tensors");
            var createMethod = tensorsClass.getMethod("create", float[][].class);
            float[][] inputData = new float[][]{features};
            Object inputTensor = createMethod.invoke(null, (Object) inputData);

            // Feed input, fetch output, and run
            Class<?> runnerClass = runner.getClass();
            var feedMethod = runnerClass.getMethod("feed", String.class, tensorClass);
            var fetchMethod = runnerClass.getMethod("fetch", String.class);
            var runMethod = runnerClass.getMethod("run");

            feedMethod.invoke(runner, "serving_default_input", inputTensor);
            fetchMethod.invoke(runner, "StatefulPartitionedCall");
            var results = (java.util.List<?>) runMethod.invoke(runner);

            if (results != null && !results.isEmpty()) {
                Object outputTensor = results.get(0);
                var copyToMethod = outputTensor.getClass().getMethod("copyTo", float[][].class);
                float[][] output = new float[1][1];
                copyToMethod.invoke(outputTensor, (Object) output);

                // Close tensors
                if (inputTensor instanceof AutoCloseable) {
                    ((AutoCloseable) inputTensor).close();
                }
                if (outputTensor instanceof AutoCloseable) {
                    ((AutoCloseable) outputTensor).close();
                }

                return Math.max(0, output[0][0]);
            }

            return -1.0;

        } catch (Exception e) {
            log.error("TensorFlow inference failed. Falling back to statistical model. Error: {}",
                    e.getMessage());
            return -1.0;
        }
    }

    /**
     * Check whether the TensorFlow model is currently loaded and ready for inference.
     */
    public boolean isModelAvailable() {
        return modelLoaded.get();
    }

    /**
     * Get the current model version string.
     */
    public String getModelVersion() {
        return modelVersion;
    }

    /**
     * Reload the model from disk (e.g., after retraining).
     */
    public void reloadModel() {
        log.info("Reloading TensorFlow model...");
        closeModel();
        loadModel();
    }

    @PreDestroy
    public void closeModel() {
        if (savedModelBundle != null) {
            try {
                if (savedModelBundle instanceof AutoCloseable) {
                    ((AutoCloseable) savedModelBundle).close();
                }
            } catch (Exception e) {
                log.warn("Error closing TensorFlow model: {}", e.getMessage());
            }
            savedModelBundle = null;
            modelLoaded.set(false);
        }
    }
}
