package explorer.window.presenter;

import explorer.window.controller.VisualizationViewController;
import explorer.window.vistools.Axes;
import explorer.window.vistools.MyCamera;
import explorer.window.vistools.TransformUtils;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import java.util.List;

import static explorer.window.vistools.TransformUtils.applyGlobalRotation;

public class VisualizationViewPresenter {

    // constants copied from assignment06
    // initial view on the tripod is saved as Affine. It is also used as reset position.
    private static final Affine initialTransform = new Affine(
            1.0, 0.0, 0.0, 0.0,
            0.0, 0.0, -1.0, 0.0,
            0.0, 1.0, 0.0, 0.0
    );
    private static final int rotationStep = 10;
    private static final int translationStep = 5;
    private final Group contentGroup;
    private final MyCamera camera = new MyCamera();

    public VisualizationViewPresenter(VisualizationViewController visualizationViewController) {
        contentGroup = setupVisualisationPane(visualizationViewController);
        setupTripodPane(visualizationViewController, contentGroup);
        setupVisualizationViewButtons(visualizationViewController, contentGroup);
    }

    /**
     * Sets up a 3D sub-pane for rendering 3D objects within the application.
     * Creates and configures a 3D scene with lighting, camera, and background color.
     * Adds the configured subscene to the application's 3D drawing pane.
     *
     * @param controller The WindowController instance containing the 3D drawing pane
     *                   and other UI components associated with the application.
     */
    private Group setupVisualisationPane(VisualizationViewController controller) {
        Pane visualizationPane = controller.getVisualizationPane();
        Group contentGroup = new Group();
        Group root3d = new Group(contentGroup);

        var subScene = new SubScene(root3d, 600, 600, true, SceneAntialiasing.BALANCED);
        subScene.widthProperty().bind(visualizationPane.widthProperty());
        subScene.heightProperty().bind(visualizationPane.heightProperty());
        // make subScene background lightgrey
        subScene.setFill(Color.DARKGREY);

        // add camera
        subScene.setCamera(camera);

        // Add PointLight
        PointLight pointLight = new PointLight(Color.DARKGREY);
        pointLight.setTranslateX(100);
        pointLight.setTranslateY(-100);
        pointLight.setTranslateZ(-100);

        // Add AmbientLight
        AmbientLight ambientLight = new AmbientLight(Color.rgb(180, 180, 180));

        // Add lights to the root3d group
        root3d.getChildren().addAll(pointLight, ambientLight);

        visualizationPane.getChildren().add(subScene);

        // setup rotation via mouse:
        TransformUtils.setupMouseRotation(visualizationPane, contentGroup);
        //add zoom functionality via scrolling
        visualizationPane.setOnScroll(camera::zoomAndPanScrolling);

        // focus on contentGroup
        camera.setFocus(contentGroup);

        // load the human body parts
        contentGroup.getChildren().add(new Axes(20)); // TODO load human body into the contentPane
        contentGroup.getTransforms().setAll(initialTransform);

        return contentGroup;
    }

    /**
     * small tripod Pane to visualize the orientation of the Human Body Model in the lower left side
     * @param visualizationViewController
     * @param contentGroup
     */
    private void setupTripodPane(VisualizationViewController visualizationViewController, Group contentGroup) {
        Pane tripodPane = visualizationViewController.getTripodPane();

        Group tripodGroup = new Group();
        tripodGroup.getChildren().add(new Axes(10));

        // Copy transforms from contentGroup to tripodGroup initially
        tripodGroup.getTransforms().setAll(contentGroup.getTransforms());

        // Add listener to update tripodGroup transforms when contentGroup transforms change
        contentGroup.getTransforms().addListener(
                (ListChangeListener<Transform>) _ -> tripodGroup.getTransforms().setAll(contentGroup.getTransforms()));

        // subScene for tripod
        var tripodSubScene = new SubScene(tripodGroup, 600, 600, true, SceneAntialiasing.BALANCED);
        tripodSubScene.widthProperty().bind(tripodPane.widthProperty());
        tripodSubScene.heightProperty().bind(tripodPane.heightProperty());
        tripodSubScene.setFill(Color.TRANSPARENT);
        tripodSubScene.setMouseTransparent(true);
        PerspectiveCamera cam = new PerspectiveCamera(true);
        cam.setTranslateZ(-40);
        tripodSubScene.setCamera(cam);

        // Add AmbientLight
        tripodGroup.getChildren().add(new AmbientLight(Color.rgb(220, 220, 220)));

        tripodPane.getChildren().add(tripodSubScene);
    }

    /**
     * sets up all the buttons in the visualizationView
     * @param visualizationViewController
     */
    private void setupVisualizationViewButtons(VisualizationViewController visualizationViewController, Group contentGroup) {
        // Record-Type for keeping the actions better together
        // with a small pun ... DirAction... DIRAction ... DIRECTION ... got it? ☚(ﾟヮﾟ☚)
        record DirAction(Button btn, Point3D rotAxis, double rotAngle, Point3D trans) {}

        // All directions and its corresponding buttons
        List<DirAction> actions = List.of(
                // Left UP
                new DirAction(visualizationViewController.getButtonCntrlLeftUp(),
                              new Point3D(1,0,1),   -rotationStep,
                              new Point3D(translationStep, translationStep, 0)),
                // UP
                new DirAction(visualizationViewController.getButtonCntrlUp(),
                              new Point3D(1,0,0),   -rotationStep,
                              new Point3D(0, translationStep, 0)),
                // Right UP
                new DirAction(visualizationViewController.getButtonCntrlRightUp(),
                              new Point3D(1,1,0),   -rotationStep,
                              new Point3D(-translationStep, translationStep, 0)),
                // Left DOWN
                new DirAction(visualizationViewController.getButtonCntrlLeftDown(),
                              new Point3D(1,1,0),    rotationStep,
                              new Point3D(translationStep, -translationStep, 0)),
                // DOWN
                new DirAction(visualizationViewController.getButtonCntrlDown(),
                              new Point3D(1,0,0),    rotationStep,
                              new Point3D(0, -translationStep, 0)),
                // Right DOWN
                new DirAction(visualizationViewController.getButtonCntrlRightDown(),
                              new Point3D(1,0,1),    rotationStep,
                              new Point3D(-translationStep, -translationStep, 0)),
                // LEFT
                new DirAction(visualizationViewController.getButtonCntrlLeft(),
                              new Point3D(0,1,0),    rotationStep,
                              new Point3D(translationStep,0, 0)),
                // RIGHT
                new DirAction(visualizationViewController.getButtonCntrlRight(),
                              new Point3D(0,1,0),   -rotationStep,
                              new Point3D(-translationStep,0, 0))
        );

        // Alle Action-Handler in der Schleife setzen
        for (DirAction a : actions) {
            a.btn().setOnAction(e -> {
                if (visualizationViewController.getRadioRotation().isSelected()) {
                    applyGlobalRotation(contentGroup, a.rotAxis(), a.rotAngle());
                } else {
                    Point3D v = a.trans();
                    camera.pan(v.getX(), v.getY());
                }
            });
        }

        // set zoom functions
        visualizationViewController.getButtonCntrlReset().setOnAction(e -> resetView(contentGroup));
        setupZoomSlider(visualizationViewController);
    }

    /**
     * sets up the zoom slider and its bidirectional binding of the camera position
     * @param visualizationViewController
     */
    private void setupZoomSlider(VisualizationViewController visualizationViewController) {
        Slider slider = visualizationViewController.getZoomSlider();

        DoubleProperty sliderValue = new SimpleDoubleProperty();

        slider.setMin(0); // max zoom in is always 0
        slider.maxProperty().bind(camera.getMaxZoomOut().multiply(-1)); // max zoom out is dynamic based on figure

        // slider reacts to camera changes
        camera.translateZProperty().addListener((obs, oldVal, newVal) -> {
            sliderValue.set(-newVal.doubleValue());
        });

        // camera reacts to slider changes
        sliderValue.addListener((obs, oldVal, newVal) -> {
            camera.setTranslateZ(-newVal.doubleValue());
        });

        slider.valueProperty().bindBidirectional(sliderValue);
    }

    protected void rotateContentGroupUp() {
        applyGlobalRotation(contentGroup, new Point3D(1, 0, 0), -rotationStep);
    }

    protected void rotateContentGroupDown() {
        applyGlobalRotation(contentGroup, new Point3D(1, 0, 0), rotationStep);
    }

    protected void rotateContentGroupLeft() {
        applyGlobalRotation(contentGroup, new Point3D(0, 1, 0), rotationStep);
    }

    protected void rotateContentGroupRight() {
        applyGlobalRotation(contentGroup, new Point3D(0,1, 0), -rotationStep);
    }

    protected void zoomIntoContentGroup() {
        camera.zoomIn();
    }

    protected void zoomOutContentGroup() {
        camera.zoomOut();
    }

    /**
     * Resets the view of the 3D scene to its initial state by adjusting
     * the camera position and resetting the transformations of the content group.
     *
     * @param contentGroup The group of 3D content whose transformations will be reset.
     */
    protected void resetView(Group contentGroup) {
        camera.resetView();
        contentGroup.getTransforms().setAll(initialTransform);
    }
}
