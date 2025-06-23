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
import java.util.function.BiConsumer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import static javafx.collections.FXCollections.observableSet;

public class HumanBody extends Group{

    // connects fileID to a MeshView instance loaded from that fileID
    private final ConcurrentHashMap<String, MeshView> meshViews = new ConcurrentHashMap<>();

    private final ObservableSet<Node> currentSelection = observableSet();

    public static final PhongMaterial SHARED_DEFAULT_MATERIAL;
    static {
        SHARED_DEFAULT_MATERIAL = new PhongMaterial();
        SHARED_DEFAULT_MATERIAL.setSpecularColor(Color.BLACK);
        SHARED_DEFAULT_MATERIAL.setDiffuseColor(Color.DARKGREY);
    }

    public HumanBody() {
        // Initialize MouseClick Listener that registers if a mesh gets added to the selection or removed
        this.setOnMouseClicked(event -> {
            Node clickedNode = event.getPickResult().getIntersectedNode();
            if (clickedNode instanceof MeshView meshView) {

                if (currentSelection.contains(meshView)) {
                    currentSelection.remove(meshView);
                } else {
                    currentSelection.add(meshView);
                }
            }
        });

        // add a listener to the currentSelection list to make sure all selected nodes get colored
        // and all deselcted nodes get the default coloring back
        currentSelection.addListener((SetChangeListener<Node>) change -> {
            if (change.wasAdded()) {
                Node addedNode = change.getElementAdded();
                if (addedNode instanceof MeshView meshView) {
                    PhongMaterial selectedMaterial = new PhongMaterial(Color.YELLOW);
                    selectedMaterial.setSpecularColor(Color.BLACK);
                    meshView.setMaterial(selectedMaterial);
                }
            } else if (change.wasRemoved()) {
                Node removedNode = change.getElementRemoved();
                if (removedNode instanceof MeshView meshView) {
                    meshView.setMaterial(SHARED_DEFAULT_MATERIAL);
                }
            }
        });
    }

    /**
     * Retrieves the MeshView associated with the given file ID.
     *
     * @param fileID the identifier corresponding to the .obj file
     * @return the MeshView for the given file ID, or null if not found
     */
    public MeshView getMeshOfFile(String fileID) {
        return meshViews.get(fileID);
    }

    /**
     * @return the observable list of currently selected nodes
     */
    public ObservableSet<Node> getCurrentSelection() {
        return currentSelection;
    }

    /**
     * Loads all .obj mesh files from the specified folder, creates corresponding MeshView objects,
     * applies default material, and adds them to this group. Reports progress via the provided callback.
     *
     * @param wavefrontFolder the path to the directory containing .obj files
     * @param progressCallback a callback receiving the current and total number of loaded files (used for progress updates)
     */
    public void loadMeshes(String wavefrontFolder, BiConsumer<Integer, Integer> progressCallback) {
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

            meshViews.put(id, meshView);

            collectedMeshes.add(meshView);

            if (progressCallback != null) {
                Platform.runLater(() -> progressCallback.accept(counter.incrementAndGet(), total));
            }
        });

        Platform.runLater(() -> this.getChildren().addAll(collectedMeshes));
    }
}
