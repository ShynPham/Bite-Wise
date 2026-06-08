package model;

/**
 * Represents a single object detection result from a model.
 * <p>
 * This class is an immutable data holder for the bounding box coordinates,
 * the confidence score, and the classID of a detected object.
 *
 * @param x1      The x-coordinate of the top-left corner of the bounding box.
 * @param y1      The y-coordinate of the top-left corner of the bounding box.
 * @param x2      The x-coordinate of the bottom-right corner of the bounding box.
 * @param y2      The y-coordinate of the bottom-right corner of the bounding box.
 * @param score   The confidence score of the detection, typically ranging from [0.0, 1.0].
 * @param classID The class ID of the detected object.
 *
 * @author Phu Pham
 */
public record Detection(float x1, float y1, float x2, float y2, float score, int classID) {

    /**
     * Returns a human-readable string representation of the detection.
     *
     * @return A string formatted as "Detection[class=..., score=..., box=(x1,y1,x2,y2)]"
     */
    @Override
    public String toString() {
        return "Detection[class=" + classID + ", score=" + score + ", box=(" + x1 + "," + y1 + "," + x2 + "," + y2 + ")]";
    }
}