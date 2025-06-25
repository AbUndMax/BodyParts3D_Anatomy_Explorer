package explorer.window.vistools;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.ScrollEvent;

/**
 * The MyCamera class extends the PerspectiveCamera class, providing a customized
 * configuration suitable for managing a 3D scene. This class supports operations
 * such as resetting the camera's position and focusing on the entirety of a given 3D figure.#
 *
 * SOURCE: The class itself is copied from assignment06 but smaller refactors were applied!
 */
public class MyCamera extends PerspectiveCamera {

    // reset Position the camera returns to after pressing the reset button
    private double resetPositionInZ = -200;
    private double zoomFactor = 1;
    private final int MAX_ZOOM_IN = 0;
    private final DoubleProperty maxZoomOut = new SimpleDoubleProperty(-400);
    private double translationValue = 5;

    public DoubleProperty getMaxZoomOut() {
        return maxZoomOut;
    }

    /**
     * Constructs an instance of the MyCamera class, extending PerspectiveCamera
     * with specific initial configuration.
     *
     * This camera is configured to:
     * - Start with a field of view set by the superclass constructor with the 'true' parameter, enabling vertical field of view.
     * - Set a far clipping plane distance of 10,000 units to control the maximum visible distance.
     * - Set a near clipping plane distance of 0.1 units to control the minimum visible distance.
     * - Position the camera away from the origin (in the Z-axis) by applying a translation
     *   defined by a predefined reset position, allowing a default overview of the 3D scene.
     */
    public MyCamera() {
        super(true);
        this.setFarClip(10000);
        this.setNearClip(0.1);
        this.setTranslateZ(resetPositionInZ); // back away from the origin ...
    }

    /**
     * Focuses a specific javaFX Group by applying {@link #focusFullFigure} and adding a listener
     * that applies the same function each time the figure changes
     * @param figure to focus on
     */
    public void setFocus(Group figure) {
        focusFullFigure(figure);
        figure.getChildren().addListener((ListChangeListener<Node>) change -> {
            this.focusFullFigure(figure);
        });
    }

    /**
     * Resets the camera's position to its predefined initial state.
     *
     * The method adjusts the camera's translation along the X, Y, and Z axes
     * to restore its default position:
     * - The Z-axis translation is reset to the value of `resetPositionInZ`,
     *   which represents the camera's original position along the depth axis.
     * - The X and Y-axis translations are reset to zero, centering the camera
     *   horizontally and vertically in the 3D scene.
     *
     * This is typically used to return the camera to an overview position
     * after it has been moved or adjusted during user interaction or other operations.
     */
    public void resetView(){
        this.setTranslateZ(resetPositionInZ);
        this.setTranslateY(0);
        this.setTranslateX(0);
    }

    /**
     * Adjusts the camera's position and view to focus on the entirety
     * of the provided 3D figure. The method calculates the optimal
     * distance required to encompass the entire figure within the
     * camera's field of view (FOV) and adjusts the camera's position
     * accordingly to provide additional free space around the figure.
     *
     * @param figure The 3D group to be focused on by the camera.
     *               This group represents the object whose bounds
     *               are used to calculate the necessary adjustments
     *               for the camera's position.
     */
    public void focusFullFigure(Group figure) {
        Bounds bounds = figure.getBoundsInParent();
        double depth = bounds.getDepth() / 2;
        double width = bounds.getWidth() / 2;
        double height = bounds.getHeight() / 2;
        double longestEdge = Math.max(depth, Math.max(width, height));
        double fovY = this.getFieldOfView();

        double requiredDistance = (longestEdge) / Math.tan(Math.toRadians(fovY / 2));
        requiredDistance *= 1.3; // add some free space to the FOV

        this.resetPositionInZ = -requiredDistance;
        this.zoomFactor = Math.max(1, longestEdge * 0.1);
        this.maxZoomOut.set(resetPositionInZ * 1.4);
        this.translationValue = longestEdge * 0.1;
        this.resetView();
    }

    public void zoomIn() {
        double newPosition = this.getTranslateZ() + zoomFactor * 10;
        this.setTranslateZ(Math.min(newPosition, MAX_ZOOM_IN));
    }

    public void zoomOut() {
        double newPosition = this.getTranslateZ() - zoomFactor * 10;
        this.setTranslateZ(Math.max(newPosition, maxZoomOut.getValue()));
    }

    public void zoom(double zoom) {
        double newPosition = zoom > 0 ? this.getTranslateZ() + zoomFactor : this.getTranslateZ() - zoomFactor;
        this.setTranslateZ(Math.min(Math.max(newPosition, maxZoomOut.getValue()), MAX_ZOOM_IN));
    }

    /**
     * Handles zoom functionality for 3D navigation in response to scroll events.
     * Depending on user input, adjusts either the Z-axis or Y-axis translation
     * of the camera to zoom and pan within the 3D scene.
     *
     * @param event The scroll event containing details such as scroll direction,
     *              scroll distance, and modifier keys that are pressed.
     */
    public void zoomAndPanScrolling(ScrollEvent event) {
        double deltaY = translate(event.getDeltaY()) * 0.9;
        double deltaX = translate(event.getDeltaX()) * 0.9;
        if (event.isShiftDown()) { // if shift is pressed, instead if zooming, we pan the camera
            this.pan(deltaX, deltaY);
        }
        else this.zoom(deltaY);
    }

    /**
     * calculates a translation value based on the current zoom level to ensure that translation is equal
     * for all distances!
     * @param value
     * @return translation step
     */
    private double translate(double value) {
        if (value > 0) {
            return translationValue * this.getTranslateZ() / maxZoomOut.getValue();
        } else if (value < 0) {
            return -translationValue * this.getTranslateZ() / maxZoomOut.getValue();
        } else {
            return 0;
        }
    }

    public void pan(double x, double y) {
        this.setTranslateX(this.getTranslateX() + translate(x));
        this.setTranslateY(this.getTranslateY() + translate(y));
    }
}
