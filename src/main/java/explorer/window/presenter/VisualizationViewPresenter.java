package explorer.window.presenter;

import explorer.model.AnatomyNode;
import explorer.model.AppConfig;
import explorer.model.ObjIO;
import explorer.window.ControllerRegistry;
import explorer.window.controller.VisualizationViewController;
import explorer.window.vistools.Axes;
import explorer.window.vistools.HumanBody;
import explorer.window.vistools.MyCamera;
import explorer.window.vistools.TransformUtils;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static explorer.window.vistools.TransformUtils.applyGlobalRotation;

public class VisualizationViewPresenter {

    private final ControllerRegistry registry;
    private final VisualizationViewController visController;

    // constants copied from assignment06
    // initial view on the tripod is saved as Affine. It is also used as reset position.
    private static final Affine INITIAL_TRANSFORM = new Affine(
            1.0, 0.0, 0.0, 0.0,
            0.0, 0.0, -1.0, 0.0,
            0.0, 1.0, 0.0, 0.0
    );
    private static final int ROTATION_STEP = 10;

    private final MyCamera camera = new MyCamera();
    private final HumanBody humanBody = new HumanBody();
    private final Group contentGroup;

    /**
     * Initializes the visualization view presenter by setting up the 3D visualization,
     * tripod pane, and user interaction buttons.
     *
     * @param registry the ControllerRegistry that holds all Controller instances.
     */
    public VisualizationViewPresenter(ControllerRegistry registry) {
        this.registry = registry;
        this.visController = registry.getVisualizationViewController();

        contentGroup = setupVisualisationPane();
        setupTripodPane();
        setupVisualizationViewButtons();
    }

    /**
     * Sets up a 3D sub-pane for rendering 3D objects within the application.
     * Creates and configures a 3D scene with lighting, camera, and background color.
     * Adds the configured subscene to the application's 3D drawing pane.
     */
    private Group setupVisualisationPane() {
        Pane visualizationPane = visController.getVisualizationPane();
        Group contentGroup = new Group();
        Group root3d = new Group(contentGroup);

        var subScene = new SubScene(root3d, 600, 600, true, SceneAntialiasing.BALANCED);
        subScene.widthProperty().bind(visualizationPane.widthProperty());
        subScene.heightProperty().bind(visualizationPane.heightProperty());
        // set subScene background
        subScene.setFill(Color.LIGHTBLUE);

        // add camera
        subScene.setCamera(camera);

        // Add PointLight
        PointLight pointLight = new PointLight(Color.DARKGREY);
        // point Light is bound to camera position for always optimal scene illumination
        pointLight.translateXProperty().bind(camera.translateXProperty());
        pointLight.translateYProperty().bind(camera.translateYProperty());
        pointLight.translateZProperty().bind(camera.translateZProperty());

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

        // load the human body parts after the GUI is rendered
        Platform.runLater(this::loadHumanBody);
        contentGroup.getTransforms().setAll(INITIAL_TRANSFORM);

        return contentGroup;
    }

    /**
     * Initializes a miniature tripod scene that reflects the orientation of the main model.
     * Displays the tripod in the lower left corner of the visualization view.
     */
    private void setupTripodPane() {
        Pane tripodPane = visController.getTripodPane();

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
     * Configures directional movement buttons and zoom controls for the 3D view.
     */
    private void setupVisualizationViewButtons() {
        // Record-Type for keeping the actions better together
        // with a small pun ... DirAction... DIRAction ... DIRECTION ... got it? ☚(ﾟヮﾟ☚)
        record DirAction(Button btn, Point3D rotAxis, double rotAngle, Point3D trans) {}

        // All directions and its corresponding buttons
        List<DirAction> actions = List.of(
                // Left UP
                new DirAction(visController.getButtonCntrlLeftUp(),
                              new Point3D(1,0,1),   -ROTATION_STEP,
                              new Point3D(1, 1, 0)),
                // UP
                new DirAction(visController.getButtonCntrlUp(),
                              new Point3D(1,0,0),   -ROTATION_STEP,
                              new Point3D(0, 1, 0)),
                // Right UP
                new DirAction(visController.getButtonCntrlRightUp(),
                              new Point3D(1,1,0),   -ROTATION_STEP,
                              new Point3D(-1, 1, 0)),
                // Left DOWN
                new DirAction(visController.getButtonCntrlLeftDown(),
                              new Point3D(1,1,0), ROTATION_STEP,
                              new Point3D(1, -1, 0)),
                // DOWN
                new DirAction(visController.getButtonCntrlDown(),
                              new Point3D(1,0,0), ROTATION_STEP,
                              new Point3D(0, -1, 0)),
                // Right DOWN
                new DirAction(visController.getButtonCntrlRightDown(),
                              new Point3D(1,0,1), ROTATION_STEP,
                              new Point3D(-1, -1, 0)),
                // LEFT
                new DirAction(visController.getButtonCntrlLeft(),
                              new Point3D(0,1,0), ROTATION_STEP,
                              new Point3D(1,0, 0)),
                // RIGHT
                new DirAction(visController.getButtonCntrlRight(),
                              new Point3D(0,1,0),   -ROTATION_STEP,
                              new Point3D(-1,0, 0))
        );

        // Alle Action-Handler in der Schleife setzen
        for (DirAction a : actions) {
            a.btn().setOnAction(e -> {
                if (visController.getRadioRotation().isSelected()) {
                    applyGlobalRotation(contentGroup, a.rotAxis(), a.rotAngle());
                } else {
                    Point3D v = a.trans();
                    camera.pan(v.getX(), v.getY());
                }
            });
        }

        // set zoom functions
        visController.getButtonCntrlReset().setOnAction(e -> resetView());
        setupZoomSlider();
    }

    /**
     * sets up the zoom slider and its bidirectional binding of the camera position
     */
    private void setupZoomSlider() {
        Slider slider = visController.getZoomSlider();

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

    /**
     * Loads the human body model asynchronously from the saved path. Displays a progress bar while loading.
     * If the path is missing or invalid, prompts the user to select the correct model directory.
     */
    private void loadHumanBody() {
        AtomicReference<String> wavefrontPath = new AtomicReference<>(AppConfig.loadLastPath());

        // if the path is invalid overlay the visualization pane with a load button
        if (wavefrontPath.get() == null || !new File(wavefrontPath.get()).isDirectory()) {
            BorderPane overlay = new BorderPane();
            overlay.setStyle("-fx-background-color: rgba(255,255,255,0.8);");

            Button openButton = new Button("Select 'isa_BP3D_4.0_obj_99' folder to load model");
            openButton.setOnAction(e -> {
                File path = ObjIO.openDirectoryChooser();
                AppConfig.saveLastPath(path.getAbsolutePath());
                visController.getVisualizationStackPane().getChildren().remove(overlay);
                loadHumanBody();
            });
            // TODO: eventually add "download" button that allows to fetch the files

            overlay.setCenter(openButton);
            visController.getVisualizationStackPane().getChildren().add(overlay);
            return;
        }

        ProgressBar progressBar = new ProgressBar(0);
        StackPane visualizationStack = visController.getVisualizationStackPane();
        visualizationStack.getChildren().add(progressBar);

        // use a task for save load handling -> visualize progress via progressBar
        String finalWavefrontPath = wavefrontPath.get();
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() {
                humanBody.loadMeshes(finalWavefrontPath, this::updateProgress);
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                visualizationStack.getChildren().remove(progressBar);
                TransformUtils.centerGroupToItself(humanBody);

                // add humanBody to the contentGroup
                contentGroup.getChildren().add(humanBody);

                // bind the TreeViews to the MeshSelection
                TreeView<AnatomyNode> isATreeView = registry.getSelectionViewController().getTreeViewIsA();
                TreeView<AnatomyNode> partOfTreeView = registry.getSelectionViewController().getTreeViewPartOf();
                ListView<String> listView = registry.getSelectionViewController().getSelectionListView();
                humanBody.getMeshSelection().bindTreeView(isATreeView);
                humanBody.getMeshSelection().bindTreeView(partOfTreeView);
                humanBody.getMeshSelection().bindListView(listView);
                humanBody.assignNames(isATreeView.getRoot(), partOfTreeView.getRoot());
            }

            @Override
            protected void failed() {
                super.failed();
                visualizationStack.getChildren().remove(progressBar);
                getException().printStackTrace();
            }
        };

        progressBar.progressProperty().bind(loadTask.progressProperty());
        new Thread(loadTask, "HumanBody-Loader").start();
    }

    /**
     * Rotates the 3D content group upward along the X-axis.
     */
    protected void rotateContentGroupUp() {
        applyGlobalRotation(contentGroup, new Point3D(1, 0, 0), -ROTATION_STEP);
    }

    /**
     * Rotates the 3D content group downward along the X-axis.
     */
    protected void rotateContentGroupDown() {
        applyGlobalRotation(contentGroup, new Point3D(1, 0, 0), ROTATION_STEP);
    }

    /**
     * Rotates the 3D content group to the left along the Y-axis.
     */
    protected void rotateContentGroupLeft() {
        applyGlobalRotation(contentGroup, new Point3D(0, 1, 0), ROTATION_STEP);
    }

    /**
     * Rotates the 3D content group to the right along the Y-axis.
     */
    protected void rotateContentGroupRight() {
        applyGlobalRotation(contentGroup, new Point3D(0,1, 0), -ROTATION_STEP);
    }

    /**
     * Zooms in the camera on the 3D content.
     */
    protected void zoomIntoContentGroup() {
        camera.zoomIn();
    }

    /**
     * Zooms out the camera from the 3D content.
     */
    protected void zoomOutContentGroup() {
        camera.zoomOut();
    }

    /**
     * Resets the view of the 3D scene to its initial state by adjusting
     * the camera position and resetting the transformations of the content group.
     */
    protected void resetView() {
        camera.resetView();
        contentGroup.getTransforms().setAll(INITIAL_TRANSFORM);
    }
}
