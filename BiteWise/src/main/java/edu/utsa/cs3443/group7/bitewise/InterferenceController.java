package edu.utsa.cs3443.group7.bitewise;

import ai.onnxruntime.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
// --- NEW IMPORTS ---
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
// ---------------------
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InterferenceController {
    // FXML bindings
    @FXML private Button chooseImageButton;
    @FXML private Label statusLabel;
    @FXML private ImageView imageView;
    @FXML private Canvas overlayCanvas;
    @FXML private StackPane imageContainer;
    @FXML private ProgressIndicator progressIndicator;

    // ONNX Runtime resources
    private OrtEnvironment env;
    private OrtSession session;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Model properties
    private int modelWidth;
    private int modelHeight;
    private int numClasses; // Will be read from model
    private String inputName;

    // --- NEW ---
    // The name of AI model file.
    // Place this file in your project's resources folder
    // (e.g., src/main/resources/edu/utsa/cs3443/group7/bitewise/best.onnx)
    private static final String MODEL_RESOURCE_NAME = "best.onnx";

    // Inference settings
    private static final float CONF_THRESH = 0.1f;
    private static final float NMS_THRESH = 0.45f;

    // 211 custom food classes
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


    @FXML
    public void initialize() {
        // --- THIS IS THE NEW MODEL LOADING CODE ---
        updateUIForTask(true, "Loading model...");
        chooseImageButton.setDisable(true); // Disable until model is loaded

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

                session = env.createSession(tempFile.toAbsolutePath().toString(), opts);
                readModelProperties();

                // Clean up the temp file
                Files.delete(tempFile);

                Platform.runLater(() -> {
                    statusLabel.setText("Model loaded: " + MODEL_RESOURCE_NAME);
                    chooseImageButton.setDisable(false); // Enable image button
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> statusLabel.setText("Model load error: " + e.getMessage()));
            } finally {
                Platform.runLater(() -> updateUIForTask(false, ""));
            }
        });
        // --- END OF NEW CODE ---
    }

    @FXML
    protected void onLoadModel() {
        // This method is no longer used, the logic is in initialize()
        // You can delete this method and the "loadModelButton" from your FXML file
        statusLabel.setText("Model is already loaded automatically.");
    }

    // ... (Rest of your code: readModelProperties, onChooseImage, transpose, updateUIForTask, etc. remains exactly the same) ...

    private void readModelProperties() throws OrtException {
        Map<String, NodeInfo> inputInfoMap = session.getInputInfo();
        inputName = session.getInputNames().iterator().next();
        TensorInfo inputTensorInfo = (TensorInfo) inputInfoMap.get(inputName).getInfo();
        long[] shape = inputTensorInfo.getShape();
        modelHeight = (int) shape[2];
        modelWidth = (int) shape[3];
        Map<String, NodeInfo> outputInfoMap = session.getOutputInfo();
        String outputName = session.getOutputNames().iterator().next();
        TensorInfo outputTensorInfo = (TensorInfo) outputInfoMap.get(outputName).getInfo();
        numClasses = (int) outputTensorInfo.getShape()[1] - 4;
    }

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
        if (file == null) return;
        updateUIForTask(true, "Processing image...");
        executor.submit(() -> {
            try {
                BufferedImage bimg = ImageIO.read(file);
                PreprocessResult prep = letterboxAndPreprocess(bimg, modelWidth, modelHeight);
                long[] shape = new long[]{1, 3, modelHeight, modelWidth};
                OnnxTensor input = OnnxTensor.createTensor(env, FloatBuffer.wrap(prep.data), shape);
                try (OrtSession.Result results = session.run(Collections.singletonMap(inputName, input))) {
                    float[][][] rawOutput3D = (float[][][]) results.get(0).getValue();
                    float[][] rawOutput = rawOutput3D[0];
                    float[][] transposedOutput = transpose(rawOutput);
                    List<Detection> dets = postprocess(transposedOutput, prep.scale, prep.dx, prep.dy, bimg.getWidth(), bimg.getHeight());
                    Platform.runLater(() -> {
                        statusLabel.setText("Found " + dets.size() + " detections.");
                        DetectionDrawer.draw(imageView, overlayCanvas, imageContainer, bimg, dets, CLASS_NAMES);
                    });
                } finally {
                    input.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> statusLabel.setText("Inference error: " + e.getMessage()));
            } finally {
                Platform.runLater(() -> updateUIForTask(false, ""));
            }
        });
    }

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

    private void updateUIForTask(boolean isRunning, String status) {
        progressIndicator.setVisible(isRunning);
        // We only control the chooseImageButton now
        chooseImageButton.setDisable(isRunning);
        if (isRunning) {
            statusLabel.setText(status);
        }
    }

    private PreprocessResult letterboxAndPreprocess(BufferedImage src, int targetW, int targetH) {
        int srcW = src.getWidth();
        int srcH = src.getHeight();
        float r = Math.min((float) targetW / srcW, (float) targetH / srcH);
        int newW = Math.round(srcW * r);
        int newH = Math.round(srcH * r);
        int dx = (targetW - newW) / 2;
        int dy = (targetH - newH) / 2;
        BufferedImage resized = new BufferedImage(targetW, targetH, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = resized.createGraphics();
        g.setColor(java.awt.Color.GRAY);
        g.fillRect(0, 0, targetW, targetH);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, dx, dy, newW, newH, null);
        g.dispose();
        float[] out = new float[3 * targetW * targetH];
        int[] rgb = resized.getRGB(0, 0, targetW, targetH, null, 0, targetW);
        for (int i = 0; i < rgb.length; i++) {
            out[i] = ((rgb[i] >> 16) & 0xFF) / 255.0f; // R
            out[i + targetW * targetH] = ((rgb[i] >> 8) & 0xFF) / 255.0f;  // G
            out[i + 2 * targetW * targetH] = (rgb[i] & 0xFF) / 255.0f;       // B
        }
        return new PreprocessResult(out, r, dx, dy);
    }

    private List<Detection> postprocess(float[][] preds, float scale, int dx, int dy, int origW, int origH) {
        List<Detection> boxes = new ArrayList<>();

        for (float[] p : preds) {
            int bestClass = -1;
            float bestScore = 0f;

            // The class scores start at index 4 in each prediction
            for (int c = 0; c < numClasses; c++) {
                if (p[4 + c] > bestScore) {
                    bestScore = p[4 + c];
                    bestClass = c;
                }
            }

            if (bestScore >= CONF_THRESH) {
                float cx = p[0];
                float cy = p[1];
                float w = p[2];
                float h = p[3];

                float x1 = (cx - w / 2f - dx) / scale;
                float y1 = (cy - h / 2f - dy) / scale;
                float x2 = (cx + w / 2f - dx) / scale;
                float y2 = (cy + h / 2f - dy) / scale;

                x1 = Math.max(0, Math.min(x1, origW - 1));
                y1 = Math.max(0, Math.min(y1, origH - 1));
                x2 = Math.max(0, Math.min(x2, origW - 1));
                y2 = Math.max(0, Math.min(y2, origH - 1));

                boxes.add(new Detection(x1, y1, x2, y2, bestScore, bestClass));
            }
        }

        return nonMaxSuppression(boxes, NMS_THRESH);
    }

    private List<Detection> nonMaxSuppression(List<Detection> dets, float iouThresh) {
        dets.sort((a, b) -> Float.compare(b.score(), a.score()));
        List<Detection> out = new ArrayList<>();
        boolean[] removed = new boolean[dets.size()];
        for (int i = 0; i < dets.size(); i++) {
            if (removed[i]) continue;
            Detection a = dets.get(i);
            out.add(a);
            for (int j = i + 1; j < dets.size(); j++) {
                if (removed[j]) continue;
                if (iou(a, dets.get(j)) > iouThresh) removed[j] = true;
            }
        }
        return out;
    }

    private float iou(Detection a, Detection b) {
        float xi1 = Math.max(a.x1(), b.x1());
        float yi1 = Math.max(a.y1(), b.y1());
        float xi2 = Math.min(a.x2(), b.x2());
        float yi2 = Math.min(a.y2(), b.y2());
        float inter = Math.max(0, xi2 - xi1) * Math.max(0, yi2 - yi1);
        float areaA = (a.x2() - a.x1()) * (a.y2() - a.y1());
        float areaB = (b.x2() - b.x1()) * (b.y2() - b.y1());
        return inter / (areaA + areaB - inter);
    }

    public void shutdown() {
        executor.shutdownNow();
        try { if (session != null) session.close(); } catch (Exception ignored) {}
        try { if (env != null) env.close(); } catch (Exception ignored) {}
    }

    private static class PreprocessResult {
        final float[] data;
        final float scale;
        final int dx, dy;
        PreprocessResult(float[] data, float scale, int dx, int dy) {
            this.data = data;
            this.scale = scale;
            this.dx = dx;
            this.dy = dy;
        }
    }
}