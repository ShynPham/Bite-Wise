/**
 * Package for the BiteWise application, integrating JavaFX controllers
 * with the ONNX runtime for machine learning inference.
 */
package controller;

import ai.onnxruntime.*;
import detectionApp.AppLauncher;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import model.Detection;
import utility.DetectionDrawer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.print.DocFlavor;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;


/**
 * detectionApp.Main JavaFX Controller for the BiteWise application.
 * This class handles:
 * - Automatically loading the ONNX YOLO model on startup.
 * - Processing user-selected images for inference.
 * - Running inference in a background thread.
 * - Pre-processing images (letterboxing) and post-processing model output (NMS).
 * - Calling utility classes to draw detections on the screen.
 */
public class InterferenceController {

    // --- FXML Bindings (UI Elements) ---

    /**
     * FXML link to the "Choose Image" button.
     */
    @FXML
    private Button chooseImageButton;
    /**
     * FXML link to the status label at the bottom.
     */
    @FXML
    private Label statusLabel;
    /**
     * FXML link to the main ImageView for displaying the user's image.
     */
    @FXML
    private ImageView imageView;
    /**
     * FXML link to the Canvas used to draw bounding boxes over the ImageView.
     */
    @FXML
    private Canvas overlayCanvas;
    /**
     * FXML link to the StackPane that holds the ImageView and Canvas.
     */
    @FXML
    private StackPane imageContainer;
    /**
     * FXML link to the loading spinner.
     */
    @FXML
    private ProgressIndicator progressIndicator;
    /**
     * FXML link to the TextArea results
     */
    @FXML
    private TextArea detectionResults;
    // --- ONNX Runtime Resources ---

    /**
     * The ONNX runtime environment.
     */
    private static OrtEnvironment env;
    /**
     * The ONNX model inference session.
     */
    private static OrtSession session;
    /**
     * A single-thread executor to run inference tasks in the background, keeping the UI responsive.
     */
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    // --- Model Properties ---

    /**
     * The expected input width for the model. Read from the model properties.
     */
    private int modelWidth;
    /**
     * The expected input height for the model. Read from the model properties.
     */
    private int modelHeight;
    /**
     * The number of classes the model can detect. Read from the model properties.
     */
    private int numClasses;
    /**
     * The name of the model's input node (e.g., "images"). Read from the model properties.
     */
    private String inputName;

    /**
     * The name of the ONNX model file.
     * This file MUST be placed in the project's resources folder
     */
    private static final String MODEL_RESOURCE_NAME = "/edu/utsa/cs3443/group7/bitewise/best.onnx";

    // --- Inference Settings ---

    /**
     * Confidence threshold: Detections below this score will be ignored.
     */
    private static final float CONF_THRESH = 0.1f;
    /**
     * Non-Max Suppression (NMS) threshold: Boxes with IoU above this value will be merged.
     */
    private static final float NMS_THRESH = 0.45f;

    /**
     * The model output (e.g., index 0) will be mapped to this list (e.g., "Apple").
     */
    private static final String[] CLASS_NAMES = {
            "Apple", "Apple Pie", "Avocado", "Banana", "Basil Rice", "Beetroot", "Bell Pepper", "Bread",
            "Broccoli", "Bualoy", "Burger", "Cabbage", "Cake", "Carrot", "Cauliflower", "Cheese",
            "Cheesecake", "Chicken", "Chicken Rice", "Coconut", "Coke", "Cookie", "Corn",
            "Crispy Pork Kale", "Croissant", "Donut", "Egg", "Eggplant", "Fig", "Fish", "French Fries",
            "Grape", "Grapefruit", "Green Curry", "Grilled Chicken", "Hamburger", "Homok", "Hot Dog",
            "Ice Cream", "Jackfruit", "Japanese-style-pancake", "Jok", "Juice", "Khao kha moo", "Laab",
            "Laadna", "Lemon", "Lettuce", "Macaron", "Mango", "Muffin", "Mushroom", "Namprig",
            "Omelette", "Orange", "Padthai", "Pancake", "Papaya Salad", "Pasta", "Peach", "Pear",
            "Peas", "Pineapple", "Pizza", "Pomegranate", "Popcorn", "Pork Satay", "Potato", "Pretzel",
            "Pumpkin", "Radish", "Rice", "Salad", "Salmon", "Sandwich", "Shrimp", "Spinach", "Steak",
            "Strawberry", "Sukee", "Sweet Potato", "Tiramisu", "Tom Yum", "Tomato", "Waffle",
            "Watermelon", "Zucchini", "alooparatha-chapati", "baklava", "bakso", "bamia", "beans",
            "beef-curry", "beef-noodle", "beet", "besan_cheela", "bibimbap", "biriyani", "brus capusta",
            "cabbage", "carrot_eggs", "cayliflower", "celery", "chicken-n-egg-on-rice", "chicken-rice",
            "chicken_nuggets", "chinese_cabbage", "chinese_sausage", "chip-butty", "chole", "corn",
            "croquette", "cucumber", "curry", "dal", "dalmakhani", "dosa", "dosa-uttapam",
            "eels-on-rice", "eggplant", "falafel", "fasol", "fried eggplant", "fried-fish",
            "fried-noodle", "fried-rice", "fried_chicken", "fried_dumplings", "fried_eggs", "ganmodoki",
            "garlic", "ghanouj", "gratin", "grilled-eggplant", "grilled-pacific-saury-", "grilled-salmon",
            "gulab_jamun", "halawa", "hamburger", "hot pepper", "idli", "jiaozi", "kabsa", "khichdi",
            "khubz", "kofta", "kunafah", "kushari", "mansaf", "middle east burger", "middle east pizza",
            "middle east salad", "middle east soup", "miso-soup", "mujaddara", "mulukhiyah",
            "mung_bean_sprouts", "oden", "omelet", "omelette", "onion", "palakpaneer", "papad", "peas",
            "pilaf", "pita", "pizza_full", "pizza_half", "pizza_slice", "poha", "poori",
            "pork-cutlet-on-rice", "potage", "qatayef", "raisin-bread", "rajma", "ramen-noodle",
            "rasgulla", "rediska", "redka", "rendang", "roll-bread", "salad", "salmon-meuniere",
            "sambhar", "sambosak", "samosa", "sandwiches", "sashimi", "sausage", "sauteed-spinach",
            "sauteed-vegetables", "sfiha", "shawarma", "shish taouk", "soba-noodle", "spaghetti",
            "squash-patisson", "stew", "sukiyaki", "sushi", "takoyaki", "telur_balado", "tempura-bowl",
            "tempura-udon", "tensin-noodle", "teriyaki-grilled-fish", "toast", "triangle_hash_brown",
            "udon-noodle", "vada", "vegetable marrow", "vegetable-tempura", "water_spinach", "mac_and_cheese", "chocolate_cake",
            "ice_cream", "onion_rings", "nachos", "strawberry_shortcake", "tacos"
    };

    /**
     * Holds all nutrition data, using a lowercase food name as the key
     * for easy lookup.
     */
    private final Map<String, NutritionInfo> nutritionMap = new HashMap<>();


    /**
     * This method is called automatically by JavaFX after the FXML file is loaded.
     * It is used to automatically load the ONNX model in a background thread.
     */
    @FXML
    public void initialize() {
        updateUIForTask(true, "Loading model...");
        chooseImageButton.setDisable(true); // Disable button until model is loaded

        // Run model loading in a background thread
        executor.submit(() -> {
            try {
                if (session != null) session.close();
                if (env == null) env = OrtEnvironment.getEnvironment();
                OrtSession.SessionOptions opts = new OrtSession.SessionOptions();

                // Load model from resources as a stream
                InputStream modelStream = getClass().getResourceAsStream(MODEL_RESOURCE_NAME);
                if (modelStream == null) {
                    throw new RuntimeException("Cannot find model in resources: " + MODEL_RESOURCE_NAME);
                }

                // ONNX runtime needs a file path, so we copy the stream to a temp file
                Path tempFile = Files.createTempFile("model-", ".onnx");
                Files.copy(modelStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
                modelStream.close();

                // Load the session from the temporary file
                session = env.createSession(tempFile.toAbsolutePath().toString(), opts);
                readModelProperties();

                // Clean up the temp file
                Files.delete(tempFile);

                // Load the nutrition data vales
                loadNutritionData();

                // Update UI on the JavaFX Application Thread
                Platform.runLater(() -> {
                    statusLabel.setText("Model loaded: " + MODEL_RESOURCE_NAME);
                    chooseImageButton.setDisable(false); // Enable image button
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Model load error: " + e.getMessage()));
            } finally {
                Platform.runLater(() -> updateUIForTask(false, ""));
            }
        });
    }

    /**
     * Loads and parses the food_nutrition.json file from resources
     * and populates the nutritionMap.
     */
    private void loadNutritionData() {
        try (InputStream is = getClass().getResourceAsStream("/edu/utsa/cs3443/group7/bitewise/food_nutrition.json")) {
            if (is == null) {
                throw new RuntimeException("Cannot find nutrition JSON in resources.");
            }

            // Read the file into a string
            String jsonText = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            // Parse the string into a JSON array
            JSONArray jsonArray = new JSONArray(jsonText);

            // Loop through each food object in the array
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                NutritionInfo info = new NutritionInfo(obj);

                // Store it in the map with a lowercase key for easy matching
                nutritionMap.put(info.name().toLowerCase(), info);
            }

            // Print to console to confirm it loaded
            System.out.println("Loaded " + nutritionMap.size() + " nutrition entries.");

        } catch (Exception e) {
            // If this fails, the app can still run, but nutrition facts won't show
            e.printStackTrace();
            Platform.runLater(() -> statusLabel.setText("Model loaded, but nutrition data failed."));
        }
    }

    /**
     * Reads the model's input and output metadata (like image size and class count)
     * from the loaded ONNX session.
     *
     * @throws OrtException If there is an error reading the model properties.
     */
    private void readModelProperties() throws OrtException {
        // Get input node info
        Map<String, NodeInfo> inputInfoMap = session.getInputInfo();
        inputName = session.getInputNames().iterator().next();
        TensorInfo inputTensorInfo = (TensorInfo) inputInfoMap.get(inputName).getInfo();
        long[] shape = inputTensorInfo.getShape();
        modelHeight = (int) shape[2];
        modelWidth = (int) shape[3];

        // Get output node info
        Map<String, NodeInfo> outputInfoMap = session.getOutputInfo();
        String outputName = session.getOutputNames().iterator().next();
        TensorInfo outputTensorInfo = (TensorInfo) outputInfoMap.get(outputName).getInfo();

        // Assumes output shape is [batch, 4 + numClasses, predictions]
        // We subtract 4 (cx, cy, w, h) to get the number of classes
        numClasses = (int) outputTensorInfo.getShape()[1] - 4;

        // Validation check
        if (numClasses != CLASS_NAMES.length) {
            Platform.runLater(() -> {
                statusLabel.setText("Error: Model class count (" + numClasses +
                        ") != Java class list (" + CLASS_NAMES.length + ")");
                chooseImageButton.setDisable(true); // Disable if counts mismatch
            });
        }
    }


    /**
     * Called when the "Choose Image" button is clicked.
     * Opens a FileChooser, then triggers the inference pipeline in a background thread.
     */
    @FXML
    protected void onChooseImage() {
        if (session == null) {
            statusLabel.setText("Please load a model first.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp"));
        Stage stage = (Stage) chooseImageButton.getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        if (file == null) return; // User cancelled

        updateUIForTask(true, "Processing image...");

        // Run inference in background thread
        executor.submit(() -> {
            try {
                // 1. Read and pre-process image
                BufferedImage bimg = ImageIO.read(file);
                PreprocessResult prep = letterboxAndPreprocess(bimg, modelWidth, modelHeight);

                // 2. Create ONNX input tensor
                long[] shape = new long[]{1, 3, modelHeight, modelWidth};
                OnnxTensor input = OnnxTensor.createTensor(env, FloatBuffer.wrap(prep.data), shape);
                Map<String, OnnxTensor> inputs = Collections.singletonMap(inputName, input);

                // 3. Run inference
                try (OrtSession.Result results = session.run(inputs)) {
                    // 4. Process the output
                    float[][][] rawOutput3D = (float[][][]) results.get(0).getValue();
                    float[][] rawOutput = rawOutput3D[0]; // Get the first (and only) batch
                    float[][] transposedOutput = transpose(rawOutput); // Convert [208][8400] to [8400][208]

                    // 5. Post-process (NMS, coordinate conversion)
                    List<Detection> dets = postprocess(transposedOutput, prep.scale, prep.dx, prep.dy, bimg.getWidth(), bimg.getHeight());

                    // 6. Logic that process the output into a textfield
                    Platform.runLater(() -> {
                        statusLabel.setText("Found " + dets.size() + " detections.");
                        DetectionDrawer.draw(imageView, overlayCanvas, imageContainer, bimg, dets, CLASS_NAMES);

                        // Build the results string and set it to the text area
                        if (dets.isEmpty()) {
                            detectionResults.setText("No food items detected.");
                        } else {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Found ").append(dets.size()).append(" item(s):\n\n");
                            for (Detection d : dets) {
                                String foodName = "Unknown";
                                if (d.classID() >= 0 && d.classID() < CLASS_NAMES.length) {
                                    foodName = CLASS_NAMES[d.classID()];
                                }
                                sb.append(String.format("- %s (%.2f%%)\n", foodName, d.score() * 100));
                            }
                            detectionResults.setText(sb.toString());
                        }
                    });

                    // 6. Update UI on the JavaFX Application Thread
                    Platform.runLater(() -> {
                        statusLabel.setText("Found " + dets.size() + " detections.");
                        // Use the DetectionDrawer utility to draw the boxes
                        DetectionDrawer.draw(imageView, overlayCanvas, imageContainer, bimg, dets, CLASS_NAMES);
                        detectionResults.setText(buildNutritionString(dets));
                    });
                } finally {
                    input.close(); // Ensure tensor is closed
                }
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                Platform.runLater(() -> statusLabel.setText("Inference error: " + e.getMessage()));
            } finally {
                Platform.runLater(() -> updateUIForTask(false, "")); // Re-enable button
            }
        });
    }

    /**
     * Helper method for writing the detctionResults into textArea,
     * including nutritional information
     */
    private String buildNutritionString(List<Detection> dets) {
        if (dets.isEmpty()) {
            return "No food items detected.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(dets.size()).append(" item(s):\n\n");

        for (Detection d : dets) {
            // Get the food  name from the class list
            String foodName = "Unknown";
            if (d.classID() >= 0 && d.classID() < CLASS_NAMES.length) {
                foodName = CLASS_NAMES[d.classID()];
            }

            // Add the main detection line
            sb.append(String.format("► %s (%.2f%%)\n", foodName, d.score() * 100));

            // --- NUTRITION LOOKUP (IMPROVED LOGIC) ---

            // 1. First, check for the FULL name
            NutritionInfo info = nutritionMap.get(foodName.toLowerCase());

            if (info != null) {
                // If we find the full name (e.g., "Apple"), show its info
                sb.append(info.toString());
            } else {
                // 2. If no full name, check for parts (e.g., "Apple Pie")
                String[] foodParts = foodName.split(" ");
                if (foodParts.length > 1) {
                    for (String part : foodParts) {
                        // Look up the lowercase version of the part
                        NutritionInfo partInfo = nutritionMap.get(part.toLowerCase());
                        if (partInfo != null) {
                            // If we find a part, append its nutrition info
                            sb.append(" (" + part + "):\n"); // Add a sub-header
                            sb.append(partInfo.toString());
                        }
                    }
                }
            }
            sb.append("\n"); // Add a space before the next item
        }
        return sb.toString();
    }

    /**
     * Transposes a 2D float array.
     * Assumes input is [channels][predictions] and outputs [predictions][channels].
     *
     * @param data The 2D array to transpose.
     * @return A new transposed 2D array.
     */
    private float[][] transpose(float[][] data) {
        int rows = data.length;
        int cols = data[0].length;
        float[][] transposed = new float[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = data[i][j];
            }
        }
        return transposed;
    }

    /**
     * Helper method to update the UI state (buttons, spinner) during a task.
     *
     * @param isRunning True if a task is starting, false if it has finished.
     * @param status    The status message to display.
     */
    private void updateUIForTask(boolean isRunning, String status) {
        progressIndicator.setVisible(isRunning);
        chooseImageButton.setDisable(isRunning);

        // If the model isn't loaded, the button should remain disabled
        if (session == null) {
            chooseImageButton.setDisable(true);
        }

        if (isRunning) {
            statusLabel.setText(status);
        }
    }

    /**
     * Pre-processes the input image.
     * This involves:
     * 1. Letterboxing: Resizing the image to fit 640x640 while maintaining aspect ratio.
     * 2. Padding: Filling unused space with gray.
     * 3. Normalization: Converting 0-255 pixel values to 0.0-1.0 floats.
     * 4. Reformatting: Converting from HWC (Height, Width, Channel) to NCHW (Batch, Channel, Height, Width).
     *
     * @param src     The source BufferedImage.
     * @param targetW The model's target width (e.g., 640).
     * @param targetH The model's target height (e.g., 640).
     * @return A PreprocessResult object containing the float array and scaling info.
     */
    private PreprocessResult letterboxAndPreprocess(BufferedImage src, int targetW, int targetH) {
        int srcW = src.getWidth();
        int srcH = src.getHeight();

        // Calculate scaling factor to maintain aspect ratio
        float r = Math.min((float) targetW / srcW, (float) targetH / srcH);
        int newW = Math.round(srcW * r);
        int newH = Math.round(srcH * r);

        // Calculate padding
        int dx = (targetW - newW) / 2;
        int dy = (targetH - newH) / 2;

        // Create a new image with gray padding
        BufferedImage resized = new BufferedImage(targetW, targetH, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = resized.createGraphics();
        g.setColor(java.awt.Color.GRAY);
        g.fillRect(0, 0, targetW, targetH);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        // Draw the scaled source image onto the gray background
        g.drawImage(src, dx, dy, newW, newH, null);
        g.dispose();

        // Convert the image to a float[] array in NCHW format
        float[] out = new float[3 * targetW * targetH];
        int[] rgb = resized.getRGB(0, 0, targetW, targetH, null, 0, targetW);
        for (int i = 0; i < rgb.length; i++) {
            int px = rgb[i];
            float rC = ((px >> 16) & 0xFF) / 255.0f; // Red
            float gC = ((px >> 8) & 0xFF) / 255.0f;  // Green
            float bC = (px & 0xFF) / 255.0f;       // Blue

            // NCHW format: R...R, G...G, B...B
            out[i] = rC;                         // R channel
            out[i + targetW * targetH] = gC;     // G channel
            out[i + 2 * targetW * targetH] = bC; // B channel
        }
        return new PreprocessResult(out, r, dx, dy);
    }

    /**
     * Post-processes the raw model output.
     * This involves:
     * 1. Iterating through all predictions.
     * 2. Finding the class with the highest score for each prediction.
     * 3. Applying the confidence threshold.
     * 4. Converting bounding box coordinates from model space (e.g., 640x640)
     * back to the original image's coordinate space.
     * 5. Running Non-Max Suppression (NMS).
     *
     * @param preds The [8400][208] transposed output from the model.
     * @param scale The scaling factor used during pre-processing.
     * @param dx    The x-padding used during pre-processing.
     * @param dy    The y-padding used during pre-processing.
     * @param origW The original image width.
     * @param origH The original image height.
     * @return A list of final, filtered Detection objects.
     */
    private List<Detection> postprocess(float[][] preds, float scale, int dx, int dy, int origW, int origH) {
        List<Detection> boxes = new ArrayList<>();

        for (float[] p : preds) {
            int bestClass = -1;
            float bestScore = 0f;

            // Find the best class score.
            // Class scores start at index 4 (after cx, cy, w, h)
            for (int c = 0; c < numClasses; c++) {
                if (p[4 + c] > bestScore) {
                    bestScore = p[4 + c];
                    bestClass = c;
                }
            }

            // Apply confidence threshold
            if (bestScore >= CONF_THRESH) {
                float cx = p[0];
                float cy = p[1];
                float w = p[2];
                float h = p[3];

                // Convert coordinates from 640x640 space back to original image space
                float x1 = (cx - w / 2f - dx) / scale;
                float y1 = (cy - h / 2f - dy) / scale;
                float x2 = (cx + w / 2f - dx) / scale;
                float y2 = (cy + h / 2f - dy) / scale;

                // Clamp coordinates to image bounds
                x1 = Math.max(0, Math.min(x1, origW - 1));
                y1 = Math.max(0, Math.min(y1, origH - 1));
                x2 = Math.max(0, Math.min(x2, origW - 1));
                y2 = Math.max(0, Math.min(y2, origH - 1));

                boxes.add(new Detection(x1, y1, x2, y2, bestScore, bestClass));
            }
        }

        // Remove overlapping boxes
        return nonMaxSuppression(boxes, NMS_THRESH);
    }


    /**
     * Performs Non-Max Suppression (NMS) to filter overlapping bounding boxes.
     *
     * @param dets      A list of all detections above the confidence threshold.
     * @param iouThresh The Intersection over Union (IoU) threshold.
     * @return A new list of filtered, non-overlapping detections.
     */
    private List<Detection> nonMaxSuppression(List<Detection> dets, float iouThresh) {
        dets.sort((a, b) -> Float.compare(b.score(), a.score())); // Sort by score, highest first
        List<Detection> out = new ArrayList<>();
        boolean[] removed = new boolean[dets.size()];
        for (int i = 0; i < dets.size(); i++) {
            if (removed[i]) continue;
            Detection a = dets.get(i);
            out.add(a); // Keep this box

            // Check all subsequent boxes
            for (int j = i + 1; j < dets.size(); j++) {
                if (removed[j]) continue;
                // If box 'j' overlaps too much with box 'a', remove it
                if (iou(a, dets.get(j)) > iouThresh) {
                    removed[j] = true;
                }
            }
        }
        return out;
    }

    /**
     * Calculates the Intersection over Union (IoU) of two Detection objects.
     *
     * @param a The first detection.
     * @param b The second detection.
     * @return The IoU score (a float between 0.0 and 1.0).
     */
    private float iou(Detection a, Detection b) {
        // Get coordinates of the intersection rectangle
        float xi1 = Math.max(a.x1(), b.x1());
        float yi1 = Math.max(a.y1(), b.y1());
        float xi2 = Math.min(a.x2(), b.x2());
        float yi2 = Math.min(a.y2(), b.y2());

        float inter = Math.max(0, xi2 - xi1) * Math.max(0, yi2 - yi1);
        float areaA = (a.x2() - a.x1()) * (a.y2() - a.y1());
        float areaB = (b.x2() - b.x1()) * (b.y2() - b.y1());

        // Calculate Union = AreaA + AreaB - Intersection
        return inter / (areaA + areaB - inter);
    }

    /**
     * Shuts down the ExecutorService and closes ONNX resources.
     * This method is called by {@link AppLauncher#stop()}.
     */
    public static void shutdown() {
        executor.shutdownNow();
        try {
            if (session != null) session.close();
        } catch (Exception ignored) {
        }
        try {
            if (env != null) env.close();
        } catch (Exception ignored) {
        }
    }

    @FXML
    public void SettingsSelected(javafx.event.ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/edu/utsa/cs3443/group7/bitewise/settings-screen.fxml"
            ));
            Parent settingsRoot = loader.load();

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            Scene scene = new Scene(settingsRoot);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load settings-screen.fxml");
        }
    }

    /**
     * A private inner class to hold the results of the pre-processing step.
     * This allows returning multiple values (the float data, scale, and padding)
     * from the `letterboxAndPreprocess` method.
     */
    private record PreprocessResult(float[] data, float scale, int dx, int dy) {
    }

    /**
     * A private inner class to hold the results of the pre-processing step.
     */
    private record NutritionPreprocessResult(float[] data, float scale, int dx, int dy) {
    }

    /**
     * NEW: A private record to hold nutritional data from the JSON file.
     */
    private record NutritionInfo(String name, String calories, String totalFat,
                                 String satFat, String cholesterol,
                                 String sodium, String protein) {

        /**
         * A helper constructor to parse a JSONObject.
         */
        public NutritionInfo(org.json.JSONObject obj) {
            this(
                    obj.optString("name", "N/A"),
                    obj.optString("calories", "N/A"),
                    obj.optString("total_fat", "N/A"),
                    obj.optString("saturated_fat", "N/A"),
                    obj.optString("cholesterol", "N/A"),
                    obj.optString("sodium", "N/A"),
                    obj.optString("protein", "N/A")
            );
        }

        /**
         * Formats the nutrition data as a clean string for the TextArea.
         */
        @Override
        public String toString() {
            return "  - Calories: " + calories + "\n" +
                    "  - Total Fat: " + totalFat + "\n" +
                    "  - Sat. Fat: " + satFat + "\n" +
                    "  - Cholesterol: " + cholesterol + "\n" +
                    "  - Sodium: " + sodium + "\n" +
                    "  - Protein: " + protein + "\n";
        }
    }
}