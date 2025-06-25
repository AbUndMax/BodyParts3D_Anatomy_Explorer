package explorer.window.vistools;

import javafx.application.Platform;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.concurrent.atomic.AtomicInteger;

public class HumanBody extends Group{

    // meshSelection is like a SelectionModel for a humanBody instance
    private final MeshSelection meshSelection = new MeshSelection(this);
    private final ObservableSet<MeshView> selection = meshSelection.getSourceOfTruth();

    // connects fileID to a MeshView instance loaded from that fileID
    private final ConcurrentHashMap<String, MeshView> fileIdToMeshMap = new ConcurrentHashMap<>();

    // Shared default material for all MeshViews
    public static final PhongMaterial SHARED_DEFAULT_MATERIAL = new PhongMaterial();
    static {
        // setup default Material
        SHARED_DEFAULT_MATERIAL.setSpecularColor(Color.BLACK);
        SHARED_DEFAULT_MATERIAL.setDiffuseColor(Color.DARKGREY);
    }

    /**
     * Constructs a HumanBody object, initializes the mouse click selection behavior,
     * and sets up listeners to update the material of selected and deselected MeshView objects.
     */
    public HumanBody() {
        // Initialize MouseClick Listener that registers if a mesh gets added to the selection or removed
        this.setOnMouseClicked(event -> {
            Node clickedNode = event.getPickResult().getIntersectedNode();
            if (clickedNode instanceof MeshView meshView) {

                if (selection.contains(meshView)) {
                    selection.remove(meshView);
                } else {
                    selection.add(meshView);
                }
            }
        });

        // add a listener to the currentSelection list to make sure all selected nodes get colored
        // and all deselcted nodes get the default coloring back
        // TODO: eventually move this to the VisViewPresenter when a colorPicker is added to use choosen color!
        selection.addListener((SetChangeListener<Node>) change -> {
            if (change.wasAdded()) {
                Node addedNode = change.getElementAdded();
                if (addedNode instanceof MeshView meshView) {
                    PhongMaterial selectedMaterial = new PhongMaterial(Color.YELLOW);
                    selectedMaterial.setSpecularColor(Color.BLACK);
                    Platform.runLater(() -> meshView.setMaterial(selectedMaterial));
                }
            } else if (change.wasRemoved()) {
                Node removedNode = change.getElementRemoved();
                if (removedNode instanceof MeshView meshView) {
                    Platform.runLater(() -> meshView.setMaterial(SHARED_DEFAULT_MATERIAL));
                }
            }
        });
    }

    /**
     * Returns the mapping from file IDs to their corresponding MeshView objects.
     *
     * @return a ConcurrentHashMap mapping file IDs to MeshView instances.
     */
    public ConcurrentHashMap<String, MeshView> getFileIdToMeshMap() {
        return fileIdToMeshMap;
    }

    /**
     * Returns the MeshSelection object that manages the selection state of MeshView objects.
     *
     * @return the MeshSelection object used for selection management.
     */
    public MeshSelection getMeshSelection() {
        return meshSelection;
    }

    /**
     * Loads all .obj mesh files from the specified folder, creates corresponding MeshView objects,
     * applies the default material, and adds them to this group. Mesh loading progress is reported
     * via the provided callback.
     *
     * @param wavefrontFolder the path to the directory containing .obj files.
     * @param progressCallback a callback that receives the current progress and the total number of files to load.
     */
    public void loadMeshes(String wavefrontFolder, BiConsumer<Integer, Integer> progressCallback) {
        //TODO move .obj files in resources and apply grouping
        File folder = new File(wavefrontFolder);
        File[] objFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".obj"));
        if (objFiles == null || objFiles.length == 0) return;

        AtomicInteger counter = new AtomicInteger();
        int total = objFiles.length;

        // collect all meshes in a list and append them with addAll after all Meshes are parsed
        List<MeshView> collectedMeshes = Collections.synchronizedList(new ArrayList<>());

        // Parallel loading of meshes to speed up initial load up
        Arrays.stream(objFiles).parallel().forEach(objFile -> {
            String fileName = objFile.getName();
            String id = fileName.replace(".obj", "");

            TriangleMesh mesh;
            try {
                mesh = ObjParser.load(objFile.getPath());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            MeshView meshView = new MeshView(mesh);
            meshView.setMaterial(SHARED_DEFAULT_MATERIAL);
            meshView.setId(id);

            fileIdToMeshMap.put(id, meshView);

            collectedMeshes.add(meshView);

            if (progressCallback != null) {
                Platform.runLater(() -> progressCallback.accept(counter.incrementAndGet(), total));
            }
        });

        Platform.runLater(() -> this.getChildren().addAll(collectedMeshes));
    }
}
