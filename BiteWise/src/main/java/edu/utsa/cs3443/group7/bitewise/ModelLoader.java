package edu.utsa.cs3443.group7.bitewise;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

/**
 * A utility class responsible for loading an ONNX (Open Neural Network Exchange)
 * model from the file system.
 * <p>
 * This class provides a simple method to create an {@link OrtSession} which
 * is used to run inference with the model.
 */
public class ModelLoader {

    /**
     * Loads an ONNX model from the specified file path and prepares it for inference.
     *
     * @param modelPath The file system path to the .onnx model file.
     * @return An {@link OrtSession} instance ready for inference.
     * @throws OrtException If an error occurs during the loading of the model or
     * initialization of the ONNX runtime environment.
     */
    public OrtSession load(String modelPath) throws OrtException {
        // Get the singleton ONNX runtime environment
        OrtEnvironment env = OrtEnvironment.getEnvironment();

        // Create a new session options object (can be used to configure GPU, threads, etc.)
        OrtSession.SessionOptions opts = new OrtSession.SessionOptions();

        // Create and return the session from the environment, model path, and options.
        // The session options (opts) must not be closed until after createSession returns.
        return env.createSession(modelPath, opts);
    }
}