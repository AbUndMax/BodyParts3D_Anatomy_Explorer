package explorer.window.presenter;

import explorer.model.treetools.AnatomyNode;
import explorer.model.AppConfig;
import explorer.model.ObjIO;
import explorer.window.GuiRegistry;
import explorer.window.command.Command;
import explorer.window.command.CommandManager;
import explorer.window.command.commands.*;
import explorer.window.selection.MeshSelectionManager;
import explorer.window.selection.SelectionBinder;
import explorer.window.controller.VisualizationViewController;
import explorer.window.vistools.*;
import explorer.window.vistools.animations.AnimationManager;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Presenter for the visualization view, responsible for initializing and configuring the 3D scene,
 * camera, animations, and user interaction controls within the explorer window.
 * Sets up rotation, zoom, and selection handlers, and manages mesh visibility and commands.
 */
public class VisualizationViewPresenter {

    // registry for all important and central presenter / manager classes
    private final GuiRegistry registry;

    // controller of this presenter class to access Nodes
    private final VisualizationViewController controller;

    // initial view on the tripod is saved as Affine. It is also used as reset position.
    public static final Affine INITIAL_TRANSFORM = new Affine(
            1.0, 0.0, 0.0, 0.0,
            0.0, 0.0, -1.0, 0.0,
            0.0, 1.0, 0.0, 0.0
    );

    // the rotation angle for one applied rotation step
    private static final int ROTATION_STEP = 10;

    // one camera instance on the subscene
    private final MyCamera camera = new MyCamera();

    // one central HumanBody instance - holds all loaded Meshes
    private final HumanBodyMeshes humanBodyMeshes = new HumanBodyMeshes();

    // the content group is used for transformation operations!
    private final Group contentGroup;

    // the anatomyGroup is the group that holds the current shown meshes
    private final Group anatomyGroup = new Group();

    // the animation manager
    private final AnimationManager animationManager;


    /**
     * Initializes the visualization view presenter by setting up the 3D visualization,
     * tripod pane, and user interaction buttons.
     *
     * @param registry the ControllerRegistry that holds all Controller instances.
     */
    public VisualizationViewPresenter(GuiRegistry registry) {
        this.registry = registry;
        this.controller = registry.getVisualizationViewController();

        animationManager = new AnimationManager(registry.getCommandManager());
        contentGroup = setupVisualisationPane(registry.getCommandManager());

        setupTripodPane();
        setupVisualizationViewButtons(registry.getCommandManager());
        setupClearSelectionButton(registry.getCommandManager());
        setupShowConceptButton(registry.getCommandManager());
        setupMeshRenderControls();
    }

    /**
     * Sets up a 3D sub-pane for rendering 3D objects within the application.
     * Creates and configures a 3D scene with lighting, camera, and background color.
     * Adds the configured subscene to the application's 3D drawing pane.
     */
    private Group setupVisualisationPane(CommandManager commandManager) {
        Pane visualizationPane = controller.getVisualizationPane();
        visualizationPane.getStyleClass().add("visualization-pane");
        Group contentGroup = new Group();
        Group root3d = new Group(contentGroup);

        int initWidth = 600;
        int initHeight = 600;
        var subScene = new SubScene(root3d, initWidth, initHeight, true, SceneAntialiasing.BALANCED);
        subScene.widthProperty().bind(visualizationPane.widthProperty());
        subScene.heightProperty().bind(visualizationPane.heightProperty());
        // set subScene background transparent and control the color via the visualizationPane in the css files
        subScene.setFill(Color.TRANSPARENT);

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
        TransformUtils.setupMouseRotation(visualizationPane, contentGroup, commandManager, animationManager);

        //add zoom functionality via scrolling
        visPaneOnScroll(visualizationPane, commandManager);

        // focus on anatomyGroup
        camera.setFocus(anatomyGroup);

        // add automatic centering each time the group gets changed
        anatomyGroup.getChildren().addListener((ListChangeListener<Node>) change -> {
            TransformUtils.centerGroupToItself(anatomyGroup);
        });

        // load the human body parts after the GUI is rendered
        Platform.runLater(this::loadHumanBody);
        contentGroup.getTransforms().setAll(INITIAL_TRANSFORM);

        return contentGroup;
    }

    /**
     * Configures scroll-based interactions for the visualization pane.
     * Handles live panning and zooming during scroll events and pushes capture commands on scroll end.
     *
     * @param visualizationPane the pane receiving scroll events
     * @param commandManager the manager to execute capture commands
     */
    private void visPaneOnScroll(Pane visualizationPane, CommandManager commandManager) {

        class ScrollStart {
            double zoom = 0;
            double translateX = 0;
            double translateY = 0;
        }

        ScrollStart scrollStart = new ScrollStart();

        // Record initial camera state on scroll start
        visualizationPane.setOnScrollStarted(event -> {
            scrollStart.zoom = camera.getTranslateZ();
            scrollStart.translateX = camera.getTranslateX();
            scrollStart.translateY = camera.getTranslateY();
        });

        // Apply live panning (Shift) or zoom based on scroll delta
        visualizationPane.setOnScroll(event -> {
            double deltaY = camera.translateValue(event.getDeltaY()) * 0.9;
            double deltaX = camera.translateValue(event.getDeltaX()) * 0.9;
            if (event.isShiftDown()) { // if shift is pressed, instead if zooming, we pan the camera
                camera.pan(deltaX, deltaY);
            }
            else {
                camera.zoom(deltaY);
            }
        });

        // On scroll end, push a capture command for the full scroll interaction
        visualizationPane.setOnScrollFinished(event -> {
            if (event.isShiftDown()) {
                double totalDeltaX = camera.getTranslateX() - scrollStart.translateX;
                double totalDeltaY = camera.getTranslateY() - scrollStart.translateY;
                if (totalDeltaX != 0 || totalDeltaY != 0) {
                    commandManager.executeCommand(new TranslateCaptureCommand(camera,
                                                                              scrollStart.translateX,
                                                                              scrollStart.translateY,
                                                                              camera.getTranslateX(),
                                                                              camera.getTranslateY()));
                }
            } else {
                double totalZoomDelta = camera.getTranslateZ() - scrollStart.zoom;
                if (totalZoomDelta != 0) {
                    commandManager.executeCommand(new ZoomCaptureCommand(camera,
                                                                         scrollStart.zoom,
                                                                         camera.getTranslateZ()));
                }
            }
        });
    }

    /**
     * Initializes a miniature tripod scene that reflects the orientation of the main model.
     * Displays the tripod in the lower left corner of the visualization view.
     */
    private void setupTripodPane() {
        Pane tripodPane = controller.getTripodPane();

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
    private void setupVisualizationViewButtons(CommandManager commandManager) {
        // Record-Type for keeping the actions better together
        // with a small pun ... DirAction... DIRAction ... DIRECTION ... got it? ☚(ﾟヮﾟ☚)
        record DirAction(Button btn, Command rotateCmd, Command translateCmd) {}

        // All directions and its corresponding buttons
        List<DirAction> actions = List.of(
                // Left UP
                new DirAction(controller.getButtonCntrlLeftUp(),
                              new RotateCommand(contentGroup, new Point3D(1,0,1), -ROTATION_STEP),
                              new TranslateCommand(camera, 1, 1)),
                // UP
                new DirAction(controller.getButtonCntrlUp(),
                              new RotateCommand(contentGroup, new Point3D(1,0,0), -ROTATION_STEP),
                              new TranslateCommand(camera, 0, 1)),
                // Right UP
                new DirAction(controller.getButtonCntrlRightUp(),
                              new RotateCommand(contentGroup, new Point3D(1,1,0), -ROTATION_STEP),
                              new TranslateCommand(camera, -1, 1)),
                // Left DOWN
                new DirAction(controller.getButtonCntrlLeftDown(),
                              new RotateCommand(contentGroup, new Point3D(1,1,0), ROTATION_STEP),
                              new TranslateCommand(camera, 1, -1)),
                // DOWN
                new DirAction(controller.getButtonCntrlDown(),
                              new RotateCommand(contentGroup, new Point3D(1,0,0), ROTATION_STEP),
                              new TranslateCommand(camera, 0, -1)),
                // Right DOWN
                new DirAction(controller.getButtonCntrlRightDown(),
                              new RotateCommand(contentGroup, new Point3D(1,0,1), ROTATION_STEP),
                              new TranslateCommand(camera, -1, -1)),
                // LEFT
                new DirAction(controller.getButtonCntrlLeft(),
                              new RotateCommand(contentGroup, new Point3D(0,1,0), ROTATION_STEP),
                              new TranslateCommand(camera, -1 ,0)),
                // RIGHT
                new DirAction(controller.getButtonCntrlRight(),
                              new RotateCommand(contentGroup, new Point3D(0,1,0), -ROTATION_STEP),
                              new TranslateCommand(camera, -1, 0))
        );

        // Setup all direction Buttons using a loop
        for (DirAction action : actions) {
            action.btn().setOnAction(e -> {
                Command cmd = controller.getRadioRotation().isSelected()
                        ? action.rotateCmd()
                        : action.translateCmd();
                commandManager.executeCommand(cmd);
            });
        }

        // set zoom functions
        controller.getButtonCntrlReset().setOnAction(e -> resetView(commandManager));
        setupZoomSlider(commandManager);

        // set animation buttons
        controller.getExplosionMenuItem().setOnAction(event -> {
            animationManager.explosion(anatomyGroup, camera);
        });

        controller.getPulseMenuItem().setOnAction(event -> {
            animationManager.pulse(anatomyGroup);
        });

        controller.getContRotateMenuItem().setOnAction(event -> {
            animationManager.contRotation(contentGroup,
                                          1,
                                          new Affine(contentGroup.getTransforms().getFirst()),
                                          new Point3D(0, 1, 0));
        });

        // set undo / redo functions
        Button undo = controller.getUndoButton();
        undo.setOnAction(event -> commandManager.undo());
        Button redo = controller.getRedoButton();
        redo.setOnAction(event -> commandManager.redo());

        // Bind disable properties to CommandManager's last undo/redo command
        undo.disableProperty().bind(commandManager.getLastUndoCommand().isNull());
        redo.disableProperty().bind(commandManager.getLastRedoCommand().isNull());

    }

    /**
     * Configures the zoom slider to control and reflect the camera's zoom level.
     * Binds the slider to the camera's translateZ property and records zoom start/end positions
     * to push a ZoomCaptureCommand on slider release.
     *
     * @param commandManager the manager to execute capture commands
     */
    private void setupZoomSlider(CommandManager commandManager) {
        Slider slider = controller.getZoomSlider();

        DoubleProperty sliderValue = new SimpleDoubleProperty();

        slider.setMin(0); // max zoom in is always 0
        slider.maxProperty().bind(camera.getMaxZoomOut().multiply(-1)); // max zoom out is dynamic based on figure

        // slider reacts to camera changes
        camera.translateZProperty().addListener((obs, oldVal, newVal) -> {
            sliderValue.set(-newVal.doubleValue());
        });

        // setOnMousepressed and Released for Undo / Redo functionality
        double[] startZoom = {0};

        // Record initial zoom position on slider press
        slider.setOnMousePressed(e -> {
            startZoom[0] = camera.getTranslateZ();
        });

        // Push ZoomCaptureCommand if zoom has changed
        slider.setOnMouseReleased(e -> {
            double endZoom = camera.getTranslateZ();
            if (startZoom[0] != endZoom) {
                commandManager.executeCommand(new ZoomCaptureCommand(camera, startZoom[0], endZoom));
            }
        });

        // camera reacts to slider changes
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            camera.setTranslateZ(-newVal.doubleValue()); // Live-Zoom without command
        });

        slider.valueProperty().bindBidirectional(sliderValue);
    }

    /**
     * Loads the human body model asynchronously from the saved path. Displays a progress bar while loading.
     * If the path is missing or invalid, prompts the user to select the correct model directory.
     */
    public void loadHumanBody() {
        AtomicReference<String> wavefrontPath = new AtomicReference<>(AppConfig.loadLastPath());

        // if the path is invalid overlay the visualization pane with a load button
        if (wavefrontPath.get() == null || !new File(wavefrontPath.get()).isDirectory()) {
            BorderPane overlay = new BorderPane();
            overlay.setStyle("-fx-background-color: rgba(255,255,255,0.8);");

            Button openButton = new Button("Select 'isa_BP3D_4.0_obj_99' folder to load model");
            openButton.setOnAction(e -> {
                File path = ObjIO.openDirectoryChooser();
                AppConfig.saveLastPath(path.getAbsolutePath());
                controller.getVisualizationStackPane().getChildren().remove(overlay);
                loadHumanBody();
            });

            overlay.setCenter(openButton);
            controller.getVisualizationStackPane().getChildren().add(overlay);
            return;
        }

        ProgressBar progressBar = new ProgressBar(0);
        StackPane visualizationStack = controller.getVisualizationStackPane();
        visualizationStack.getChildren().add(progressBar);

        // use a task for save load handling -> visualize progress via progressBar
        String finalWavefrontPath = wavefrontPath.get();
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() {
                humanBodyMeshes.loadMeshes(finalWavefrontPath, this::updateProgress);
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                visualizationStack.getChildren().remove(progressBar);

                // add humanBody to the contentGroup
                anatomyGroup.getChildren().addAll(humanBodyMeshes.getMeshes());
                contentGroup.getChildren().add(anatomyGroup);
                resetView(null); // initial reset should not used as Command

                // bind the TreeViews to the MeshSelection
                TreeView<AnatomyNode> isATreeView = registry.getSelectionViewController().getTreeViewIsA();
                TreeView<AnatomyNode> partOfTreeView = registry.getSelectionViewController().getTreeViewPartOf();
                ListView<Label> listView = registry.getSelectionViewController().getSelectionListView();
                // map FileIDs to Meshes
                humanBodyMeshes.mapFileIDsToMeshes(isATreeView.getRoot(), partOfTreeView.getRoot());
                // actual binding
                SelectionBinder binder = registry.getSelectionBinder();
                binder.bindTreeView(isATreeView);
                binder.bindTreeView(partOfTreeView);
                binder.bindListView(listView, controller.getSelectionColorPicker());
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
     * Configures the 'Clear Selection' button to clear all mesh, tree view, and search selections.
     * Executes a ClearSelectionCommand when clicked.
     *
     * @param commandManager the manager to execute the clear command
     */
    private void setupClearSelectionButton(CommandManager commandManager) {
        controller.getClearSelectionButton().setOnAction(e -> {
            commandManager.executeCommand(
                    new ClearSelectionCommand(humanBodyMeshes,
                                              registry.getSelectionViewController().getTreeViewIsA(),
                                              registry.getSelectionViewController().getTreeViewPartOf(),
                                              registry.getSelectionViewController().getTextFieldSearchBar()));
        });
    }

    /**
     * Configures the 'Show Concept' button and its related menu items to display, add, or remove anatomical meshes.
     * Includes functionality to:
     * - Show selected meshes with animations cleared.
     * - Add selected meshes to the currently displayed ones.
     * - Remove selected meshes from the current view.
     * - Display the entire human body model.
     * Also manages enabling/disabling the 'Show Full Human Body' option based on the current display state.
     */
    private void setupShowConceptButton(CommandManager commandManager) {

        // Main button action: Show selected meshes and clear running animations
        controller.getShowConceptButton().setOnAction(e -> {
            Set<Node> meshesToShow = selectedMeshes();
            if (!meshesToShow.isEmpty()) {
                animationManager.clearAnimations();
                commandManager.executeCommand(
                        new ShowConceptCommand(meshesToShow, anatomyGroup, humanBodyMeshes, true));
            }
        });

        // Add selected meshes to the currently displayed meshes without clearing the view
        controller.getAddToCurrentShowMenuItem().setOnAction(event -> {
            commandManager.executeCommand(
                    new ShowConceptCommand(selectedMeshes(), anatomyGroup, humanBodyMeshes, false)
            );
        });

        // Remove selected meshes from the currently displayed meshes and clear running animations
        controller.getRemoveFromCurrentShowMenuItem().setOnAction(event -> {
            animationManager.clearAnimations();
            commandManager.executeCommand(
                    new RemoveConceptCommand(selectedMeshes(), anatomyGroup));
        });

        // Show all human body meshes and clear running animations
        controller.getShowFullHumanBodyMenuItem().setOnAction(event -> {
            animationManager.clearAnimations();
            commandManager.executeCommand(
                    new ShowConceptCommand(new HashSet<>(humanBodyMeshes.getMeshes()), anatomyGroup,
                                           humanBodyMeshes, true)
            );
        });

        // Disable 'Show Full Human Body' option if the full body is already displayed
        anatomyGroup.getChildren().addListener((ListChangeListener<Node>) change -> {
            boolean humanBodyShown = new HashSet<>(anatomyGroup.getChildren())
                    .containsAll(humanBodyMeshes.getMeshes());
            controller.getShowFullHumanBodyMenuItem().setDisable(humanBodyShown);
        });
    }

    /**
     * Retrieves the list of meshes associated with the currently selected AnatomyNode items
     * in the last focused TreeView.
     *
     * @return an ArrayList of MeshView objects to be displayed.
     */
    private HashSet<Node> selectedMeshes() {
        ObservableList<TreeItem<AnatomyNode>> selectedItems =
                registry.getSelectionViewPresenter().getLastFocusedTreeView().getSelectionModel().getSelectedItems();
        HashSet<Node> meshesToDraw = new HashSet<>();
        // Collect meshes corresponding to the selected nodes in the TreeView
        for (TreeItem<AnatomyNode> selectedItem : selectedItems) {
            meshesToDraw.addAll(humanBodyMeshes.getMeshesOfFilesIDs(selectedItem.getValue().getFileIDs()));
        }
        return meshesToDraw;
    }

    /**
     * Configures controls for mesh rendering options, including color picker, draw modes,
     * and hide/reset hide buttons. Sets up listeners to update mesh appearance and handle interactions.
     */
    private void setupMeshRenderControls() {
        ColorPicker colorPicker = controller.getSelectionColorPicker();
        RadioButton line = controller.getRadioLines();
        ToggleGroup drawMode = controller.getDrawMode();
        ToggleButton hideMode = controller.getHideModeToggle();
        Button resetHide = controller.getResetHideButton();

        MeshSelectionManager meshSelectionModel = humanBodyMeshes.getSelectionModel();

        // Update mesh materials when selection changes
        humanBodyMeshes.getSelectionModel().addListener(change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (MeshView meshView : change.getAddedSubList()) {
                        PhongMaterial selectedMaterial = new PhongMaterial(colorPicker.getValue());
                        selectedMaterial.setSpecularColor(Color.BLACK);

                        Platform.runLater(() -> {
                            meshView.setDrawMode(DrawMode.FILL);
                            meshView.setMaterial(selectedMaterial);
                        });
                    }
                } else if (change.wasRemoved()) {
                    for (MeshView meshView : change.getRemoved()) {
                        Platform.runLater(() -> {
                            meshView.setDrawMode(line.isSelected() ? DrawMode.LINE : DrawMode.FILL);
                            meshView.setMaterial(humanBodyMeshes.getDefaultPhongMaterial());
                        });
                    }
                }
            }
        });

        // Update draw mode of unselected meshes when draw mode changes i.e from FILL to LINE and vice versa
        drawMode.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                RadioButton selected = (RadioButton) newToggle;

                meshSelectionModel.traverseUnselectedMeshes(mesh -> mesh.setDrawMode((DrawMode) selected.getUserData()));
            }
        });

        setupMeshClickability(hideMode, resetHide, registry.getCommandManager());
    }

    /**
     * Enables click-based selection, hiding, and clearing of individual meshes on the 3D content group.
     * Differentiates between clicks and drags, and executes appropriate commands.
     *
     * @param hideMode toggle button to enable hide mode
     * @param resetHide button to reset all hidden meshes
     * @param commandManager manager to execute mesh commands
     */
    private void setupMeshClickability(ToggleButton hideMode, Button resetHide, CommandManager commandManager) {
        ArrayList<MeshView> hiddenMeshes = humanBodyMeshes.getHiddenMeshes();

        double[] mousePressX = new double[1];
        double[] mousePressY = new double[1];

        // Save mouse press coordinates for click vs. drag detection
        contentGroup.setOnMousePressed(event -> {
            mousePressX[0] = event.getScreenX();
            mousePressY[0] = event.getScreenY();
        });

        // Determine click vs drag; handle mesh selection, hide, or clear based on mode
        contentGroup.setOnMouseReleased(event -> {
            double mouseReleaseX = event.getScreenX();
            double mouseReleaseY = event.getScreenY();

            double distance = Math.hypot(mouseReleaseX - mousePressX[0], mouseReleaseY - mousePressY[0]);

            // if distance is small, its a klick and not a drag event!
            // drag events are reserved for rotation / translation
            if (distance < 5) {
                Node clickedNode = event.getPickResult().getIntersectedNode();
                if (clickedNode instanceof MeshView meshView) {
                    if (hideMode.isSelected()) {
                        commandManager.executeCommand(new HideMeshCommand(meshView, hiddenMeshes));
                    }
                    else if (humanBodyMeshes.getSelectionModel().isSelected(meshView)){
                        commandManager.executeCommand(new ClearSelectedMeshCommand(humanBodyMeshes.getSelectionModel(),
                                                                                   meshView));
                    }
                    else {
                        commandManager.executeCommand(new SelectMeshCommand(humanBodyMeshes.getSelectionModel(),
                                                                            meshView));
                    }
                }
            }
        });

        // Execute ResetHideCommand to restore all hidden meshes
        resetHide.setOnAction(event -> {
            commandManager.executeCommand(new ResetHideCommand(hiddenMeshes));
        });
    }

    /**
     * Rotates the 3D content group upward along the X-axis.
     *
     * @param commandManager the manager to execute the translate command
     */
    protected void rotateContentGroupUp(CommandManager commandManager) {
        commandManager.executeCommand(new RotateCommand(contentGroup, new Point3D(1, 0, 0), -ROTATION_STEP));
    }

    /**
     * Rotates the 3D content group downward along the X-axis.
     *
     * @param commandManager the manager to execute the translate command
     */
    protected void rotateContentGroupDown(CommandManager commandManager) {
        commandManager.executeCommand(new RotateCommand(contentGroup, new Point3D(0, 1, 0), ROTATION_STEP));
    }

    /**
     * Rotates the 3D content group to the left along the Y-axis.
     *
     * @param commandManager the manager to execute the translate command
     */
    protected void rotateContentGroupLeft(CommandManager commandManager) {
        commandManager.executeCommand(new RotateCommand(contentGroup, new Point3D(0, 1, 0), ROTATION_STEP));
    }

    /**
     * Rotates the 3D content group to the right along the Y-axis.
     *
     * @param commandManager the manager to execute the translate command
     */
    protected void rotateContentGroupRight(CommandManager commandManager) {
        commandManager.executeCommand(new RotateCommand(contentGroup, new Point3D(0,1, 0), -ROTATION_STEP));
    }

    /**
     * Translates (pans) the camera upward along the Y-axis.
     *
     * @param commandManager the manager to execute the translate command
     */
    protected void translateContentGroupUp(CommandManager commandManager) {
        commandManager.executeCommand(new TranslateCommand(camera, 0, 1));
    }

    /**
     * Translates (pans) the camera downward along the Y-axis.
     *
     * @param commandManager the manager to execute the translate command
     */
    protected void translateContentGroupDown(CommandManager commandManager) {
        commandManager.executeCommand(new TranslateCommand(camera, 0, -1));
    }

    /**
     * Translates (pans) the camera to the left along the X-axis.
     *
     * @param commandManager the manager to execute the translate command
     */
    protected void translateContentGroupLeft(CommandManager commandManager) {
        commandManager.executeCommand(new TranslateCommand(camera, 1, 0));
    }

    /**
     * Translates (pans) the camera to the right along the X-axis.
     *
     * @param commandManager the manager to execute the translate command
     */
    protected void translateContentGroupRight(CommandManager commandManager) {
        commandManager.executeCommand(new TranslateCommand(camera, -1, 0));
    }

    /**
     * Zooms in the camera on the 3D content.
     *
     * @param commandManager the manager to execute the translate command
     */
    protected void zoomIntoContentGroup(CommandManager commandManager) {
        commandManager.executeCommand(new ZoomCommand(camera, 1));
    }

    /**
     * Zooms out the camera from the 3D content.
     *
     * @param commandManager the manager to execute the translate command
     */
    protected void zoomOutContentGroup(CommandManager commandManager) {
        commandManager.executeCommand(new ZoomCommand(camera, -1));
    }

    /**
     * Resets the view of the 3D scene to its initial state by adjusting
     * the camera position and resetting the transformations of the content group.
     *
     * @param commandManager the manager to execute the translate command
     */
    protected void resetView(CommandManager commandManager) {
        if (commandManager != null) {
            animationManager.clearAnimations();
            commandManager.executeCommand(new ResetViewCommand(contentGroup, camera));
        }
    }

    /**
     * Returns the current HumanBody model containing meshes and selection state.
     *
     * @return the HumanBody instance
     */
    public HumanBodyMeshes getHumanBody() {
        return humanBodyMeshes;
    }
}
