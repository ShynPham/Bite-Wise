/**
 * Package for the BiteWise application, integrating JavaFX controllers
 * with the ONNX runtime for machine learning inference.
 */
package controller;

import ai.onnxruntime.*;
import detectionApp.AppLauncher;
import javafx.scene.control.*;
import model.Detection;
import model.HistoryEntry;
import model.MealDraftItem;
import utility.DetectionDrawer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import utility.HistoryManager;
import utility.MealDraftManager;
import utility.viewSwitcher;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.FloatBuffer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import model.NutritionInfo;
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
 * @author  Phu Pham
 */
public class InterferenceController {
    @FXML
    private Button saveButton;
    @FXML
    private Button historyButton;
    @FXML
    private Button clearButton;
    @FXML
    public Button settingsButton;

    // --- FXML Bindings (UI Elements) ---

    /**
     * FXML link to the "Choose Image" button.
     */
    @FXML
    private Button chooseImageButton;
    @FXML
    private Button heroChooseImageButton;
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
    @FXML
    private Label detectionCountLabel;
    @FXML
    private Label mealSummaryLabel;
    @FXML
    private VBox uploadEmptyState;
    @FXML
    private Label modelStatusLabel;
    @FXML
    private Label modelShapeLabel;
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
    private static final float DEFAULT_CONF_THRESH = 0.1f;
    private float confidenceThreshold = DEFAULT_CONF_THRESH;
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
     * Holds all the lastest results are each successful detection
     */
    private List<Detection> currentDetections = new ArrayList<>();

    /**
     * This method is called automatically by JavaFX after the FXML file is loaded.
     * It is used to automatically load the ONNX model in a background thread.
     */
    @FXML
    public void initialize() {
        bindScannerStageSize();
        detectionResults.setText("");
        detectionCountLabel.setText("0 found");
        mealSummaryLabel.setText("Meal estimate: 0 cal");
        saveButton.setDisable(true);
        clearButton.setDisable(true);
        uploadEmptyState.setVisible(true);
        modelStatusLabel.setText("Loading best.onnx");
        modelShapeLabel.setText("Private food detection runs on this device.");
        updateUIForTask(true, "Loading model...");
        setUploadButtonsDisabled(false);

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
                    statusLabel.setText("Model loaded: best.onnx");
                    modelStatusLabel.setText("best.onnx ready");
                    modelShapeLabel.setText(modelWidth + "x" + modelHeight + " input | " + numClasses + " food classes");
                    setUploadButtonsDisabled(false); // Enable image buttons
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Model load error: " + e.getMessage());
                    modelStatusLabel.setText("Model unavailable");
                    modelShapeLabel.setText("Upload preview still works, but detection needs best.onnx.");
                    setUploadButtonsDisabled(false);
                });
            } finally {
                Platform.runLater(() -> updateUIForTask(false, ""));
            }
        });
    }

    private void bindScannerStageSize() {
        imageView.fitWidthProperty().bind(imageContainer.widthProperty().subtract(24));
        imageView.fitHeightProperty().bind(imageContainer.heightProperty().subtract(24));
        overlayCanvas.widthProperty().addListener((observable, oldValue, newValue) -> centerOverlayCanvas());
        overlayCanvas.heightProperty().addListener((observable, oldValue, newValue) -> centerOverlayCanvas());
    }

    private void centerOverlayCanvas() {
        overlayCanvas.setTranslateX(0);
        overlayCanvas.setTranslateY(0);
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

    }


    /**
     * Called when the "Choose Image" button is clicked.
     * Opens a FileChooser, then triggers the inference pipeline in a background thread.
     */
    @FXML
    protected void onChooseImage() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp"));
        Stage stage = (Stage) imageContainer.getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        if (file == null) return; // User cancelled

        if (session == null) {
            previewImageWithoutDetection(file);
            return;
        }

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
                    this.currentDetections = dets;
                    // 6. Logic that process the output into a textfield


                    // 6.1 Update UI on the JavaFX Application Thread
                    Platform.runLater(() -> {
                        statusLabel.setText("Found " + dets.size() + " detections.");
                        // Use the DetectionDrawer utility to draw the boxes
                        DetectionDrawer.draw(imageView, overlayCanvas, imageContainer, bimg, dets, CLASS_NAMES);
                        detectionResults.setText(buildNutritionString(dets));
                        detectionCountLabel.setText(dets.size() + (dets.size() == 1 ? " found" : " found"));
                        mealSummaryLabel.setText(buildMealSummary(dets));
                        uploadEmptyState.setVisible(false);
                        clearButton.setDisable(false);
                        // Set the save button to enable is the current detection detect something
                        saveButton.setDisable(currentDetections.isEmpty());
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

    private String buildMealSummary(List<Detection> dets) {
        int calories = 0;
        int withNutrition = 0;

        for (Detection d : dets) {
            NutritionInfo info = getNutritionForDetection(d);
            if (info != null) {
                int itemCalories = info.getNutritionAsInt();
                if (itemCalories > 0) {
                    calories += itemCalories;
                    withNutrition++;
                }
            }
        }

        if (dets.isEmpty()) {
            return "Meal estimate: 0 cal";
        }
        return "Meal estimate: " + calories + " cal from " + withNutrition + " item(s)";
    }

    private NutritionInfo getNutritionForDetection(Detection detection) {
        if (detection.classID() < 0 || detection.classID() >= CLASS_NAMES.length) {
            return null;
        }

        String foodName = CLASS_NAMES[detection.classID()];
        NutritionInfo info = nutritionMap.get(foodName.toLowerCase());
        if (info == null) {
            String firstPart = foodName.split(" ")[0];
            info = nutritionMap.get(firstPart.toLowerCase());
        }
        return info;
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
        setUploadButtonsDisabled(isRunning);

        if (isRunning) {
            statusLabel.setText(status);
        }
    }

    private void previewImageWithoutDetection(File file) {
        imageView.setImage(new Image(file.toURI().toString()));
        overlayCanvas.getGraphicsContext2D().clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
        uploadEmptyState.setVisible(false);
        clearButton.setDisable(false);
        saveButton.setDisable(true);
        detectionResults.setText("Image uploaded. Food detection will be available after the model finishes loading.");
        detectionCountLabel.setText("0 found");
        mealSummaryLabel.setText("Meal estimate: 0 cal");
        statusLabel.setText("Image uploaded. Model is still loading or unavailable.");
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
            if (bestScore >= confidenceThreshold) {
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

    public Button getHistoryButton() {
        return historyButton;
    }

    public void setHistoryButton(Button historyButton) {
        this.historyButton = historyButton;
    }


    /**
     * A private inner class to hold the results of the pre-processing step.
     * This allows returning multiple values (the float data, scale, and padding)
     * from the `letterboxAndPreprocess` method.
     */
    private record PreprocessResult(float[] data, float scale, int dx, int dy) {
    }

    @FXML
    private void handleSettingsButtonClick() {
        viewSwitcher.switchScene("settings-screen.fxml");
    }

    private void setUploadButtonsDisabled(boolean disabled) {
        chooseImageButton.setDisable(disabled);
        if (heroChooseImageButton != null) {
            heroChooseImageButton.setDisable(disabled);
        }
    }

    @FXML
    private void handleDashboardButtonClick() {
        viewSwitcher.switchScene("coach-dashboard.fxml");
    }

    @FXML
    private void onSaveDetections() {
        if (currentDetections == null || currentDetections.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Save Error", "No detections to save.");
            return;
        }

        List<MealDraftItem> draftItems = new ArrayList<>();
        for (Detection detection : currentDetections) {
            String foodName = getFoodName(detection);
            NutritionInfo info = getNutritionForDetection(detection);
            if (info == null) {
                info = new NutritionInfo("N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "N/A");
            }
            draftItems.add(new MealDraftItem(foodName, info, detection.score(), 1.0));
        }

        MealDraftManager.setDraft(draftItems);
        boolean openedReview = viewSwitcher.switchScene("meal-review.fxml");
        if (!openedReview) {
            saveDetectionsDirectly();
        }
    }

    private void saveDetectionsDirectly() {
        if (currentDetections == null || currentDetections.isEmpty()) {
            return;
        }

        // 1. Load the existing history
        List<HistoryEntry> history = HistoryManager.loadHistory();

        String detectionTime = LocalDateTime.now().toString(); // Get a timestamp

        // 2. Add all current detections to the history
        for (Detection d : currentDetections) {
            String foodName = CLASS_NAMES[d.classID()];

            // Look up nutrition info (using your existing map)
            NutritionInfo info = nutritionMap.get(foodName.toLowerCase());
            if (info == null) {
                String firstPart = foodName.split(" ")[0];
                info = nutritionMap.get(firstPart.toLowerCase());
            }
            if (info == null) {
                // If still no info, create a blank one
                info = new NutritionInfo("N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "N/A");
            }

            // Create and add the new history entry
            HistoryEntry newEntry = new HistoryEntry(foodName, detectionTime, info);
            history.add(newEntry);
        }

        // 3. Save the newly modified list back to the file
        HistoryManager.saveHistory(history);

        // 4. Notify user
        showAlert(Alert.AlertType.INFORMATION, "Success", "Detections have been saved to your history.");
        saveButton.setDisable(true); // Disable button after saving
    }

    private String getFoodName(Detection detection) {
        if (detection.classID() >= 0 && detection.classID() < CLASS_NAMES.length) {
            return CLASS_NAMES[detection.classID()];
        }
        return "Unknown";
    }

    @FXML
    private void onClearScan() {
        currentDetections = new ArrayList<>();
        imageView.setImage(null);
        overlayCanvas.getGraphicsContext2D().clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
        detectionResults.clear();
        detectionCountLabel.setText("0 found");
        mealSummaryLabel.setText("Meal estimate: 0 cal");
        statusLabel.setText("Ready for a new image.");
        saveButton.setDisable(true);
        clearButton.setDisable(true);
        uploadEmptyState.setVisible(true);
    }

    @FXML
    private void onHistoryClick() {
        viewSwitcher.switchScene("bite-history.fxml");
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
