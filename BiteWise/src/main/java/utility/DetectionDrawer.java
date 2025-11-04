package utility;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import model.Detection;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.util.List;

/**
 * A utility class with the only purposed of drawing object detection results onto JavaFX canvas.
 * <p>
 *     This class provides a static method {@link #draw(ImageView, Canvas, StackPane, BufferedImage, List, String[])}
 *     to render a source image in an {@link ImageView} and overlay bounding boxes and labels on seperate {@link java.awt.Canvas}
 * </p>
 */
public final class DetectionDrawer {

    /**
     * Pre-defined color of the bounding boxes
     */
    private static final Color[] COLORS = {
            Color.rgb(255, 0, 0), // RED
            Color.rgb(0, 255, 0), // Green
            Color.rgb(0, 0, 255), //Blue
            Color.rgb(255, 255, 0), // Yellow
            Color.rgb(0, 255, 255), // Cyan
            Color.rgb(255, 0, 255), // Magenta
            Color.rgb(128, 0, 0), // Dark Red
            Color.rgb(0, 128, 0), // Dark Green
            Color.rgb(0, 0, 128) // Dark blue
    };

     /**
     * Draws the source image and all detection overlays on the JavaFX application thread.
     * <p>
     * This method correctly scales the detection coordinates (which are relative to the
     * original {@link BufferedImage}) to match the displayed size of the image within
     * the {@link ImageView}.
     *
     * @param imageView      The {@link ImageView} component that will display the base image.
     * @param overlayCanvas  The {@link Canvas} component used to draw detection boxes and labels over the image.
     * @param imageContainer The {@link StackPane} (or other layout container) holding both the
     * ImageView and Canvas. This is used to accurately calculate the
     * ImageView's displayed bounds
     * @param src            The source {@link BufferedImage} to display.
     * @param detections     A {@link List} of {@link Detection} objects to be drawn.
     * @param classNames     An array of strings where the index corresponds to the {@code clazz}
     * field in a {@link Detection} object.
     */

    /**
     * Converts a {@link java.awt.image.BufferedImage} (AWT) to a
     * {@link javafx.scene.image.WritableImage} (JavaFX).
     *
     * @param buffImg The source {@link BufferedImage}.
     * @return A {@link WritableImage} that can be used in JavaFX components.
     */
    private static WritableImage bufferedImageToFX(BufferedImage buffImg) {
        // Create a new WritableImage with the same dimensions
        WritableImage wr = new WritableImage(buffImg.getWidth(), buffImg.getHeight());

        // Get all ARGB pixel data from the BufferedImage
        int[] pixels = buffImg.getRGB(0, 0, buffImg.getWidth(), buffImg.getHeight(), null, 0, buffImg.getWidth());

        // Define the pixel format (Integer ARGB)
        PixelFormat<IntBuffer> pf = PixelFormat.getIntArgbInstance();

        // Write the pixel data into the WritableImage's PixelWriter
        wr.getPixelWriter().setPixels(0, 0, buffImg.getWidth(), buffImg.getHeight(), pf, pixels, 0, buffImg.getWidth());

        return wr;
    }
    
    public static void draw(ImageView imageView, Canvas overlayCanvas, StackPane imageContainer, BufferedImage src,
                           List <Detection> detections, String[] classNames) {
        // If there no images, then exit
        if (src == null) {
            return;
        }

        // Ensure all UI updates are performed on the JavaFX Application Thread
        Platform.runLater(() -> {
            // Convert the AWT BufferedImage to a JavaFX WritableImage
            WritableImage fxImage = bufferedImageToFX(src);
            imageView.setImage(fxImage);

            // Force the container to update its layout to get accurate bounds
            imageContainer.layout();

            // Get the actual displayed bounds of the ImageView
            double ivW = imageView.getBoundsInParent().getWidth();
            double ivH = imageView.getBoundsInParent().getHeight();

            // Get the original image dimensions
            double imgW = fxImage.getWidth();
            double imgH = fxImage.getHeight();
            if (imgW <= 0 || imgH <= 0) return; // Avoid division by zero

            // Calculate the scaling factor to fit the image within the ImageView (maintaining aspect ratio)
            double scale = Math.min(ivW / imgW, ivH / imgH);

            // The final dimensions of the image as displayed on screen
            double dispW = imgW * scale;
            double dispH = imgH * scale;

            // Resize the canvas to match the exact displayed image dimensions
            overlayCanvas.setWidth(dispW);
            overlayCanvas.setHeight(dispH);

            // Get the graphics context to draw on the canvas
            GraphicsContext gc = overlayCanvas.getGraphicsContext2D();

            // Clear the canvas from any previous drawings
            gc.clearRect(0, 0, dispW, dispH);

            // If there are no detections, we are done
            if (detections == null || detections.isEmpty()) return;

            // Calculate the scaling factors to map original image coordinates to canvas coordinates
            double scaleX = dispW / src.getWidth();
            double scaleY = dispH / src.getHeight();

            // Iterate over each detection and draw it
            for (Detection d : detections) {
                // Select a color based on the class ID
                Color color = COLORS[d.classID() % COLORS.length];
                gc.setStroke(color);
                gc.setLineWidth(2.5);

                // Scale the detection coordinates from the original image to the canvas
                double x = d.x1() * scaleX;
                double y = d.y1() * scaleY;
                double w = (d.x2() - d.x1()) * scaleX;
                double h = (d.y2() - d.y1()) * scaleY;

                // Draw the bounding box
                gc.strokeRect(x, y, w, h);

                // --- Draw Label ---
                String label = classNames[d.classID()] + " " + String.format("%.2f", d.score());
                gc.setFill(color);
                gc.setFont(Font.font("sans-serif", 14));

                // Position the text box
                double textX = x + 2;
                // Position text above the box, but if it's at the top edge, move it inside
                double textY = y > 15 ? y - 5 : y + 15;

                // Draw a semi-transparent black background for the text
                gc.setGlobalAlpha(0.7);
                gc.setFill(Color.BLACK);
                // Estimate background width based on font size and label length
                gc.fillRect(textX - 2, textY - 15, gc.getFont().getSize() * (label.length() * 0.6), 20);
                gc.setGlobalAlpha(1.0); // Reset alpha

                // Draw the text (label and score)
                gc.setFill(Color.WHITE);
                gc.fillText(label, textX, textY);
            }
        });
    }
}
