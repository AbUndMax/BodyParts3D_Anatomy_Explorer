package explorer.window.vistools;

import explorer.model.AnatomyNode;
import explorer.model.treetools.TreeUtils;
import explorer.window.selection.MultipleMeshSelectionModel;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
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

public class HumanBody {

    // collect all meshes in a list and append them with addAll after all Meshes are parsed
    private final List<MeshView> collectedMeshes = Collections.synchronizedList(new ArrayList<>());

    // connects fileID to a MeshView instance loaded from that fileID
    private final ConcurrentHashMap<String, MeshView> fileIdToMeshMap = new ConcurrentHashMap<>();

    // meshSelection is interpreted as a SelectionModel for a humanBody instance
    private final MultipleMeshSelectionModel multipleMeshSelectionModel = new MultipleMeshSelectionModel(collectedMeshes);

    // list of meshes that are set to visible(false)
    private final LinkedList<MeshView> hiddenMeshes = new LinkedList<>();

    // Shared default material for all MeshViews
    public static final PhongMaterial SHARED_DEFAULT_MATERIAL = new PhongMaterial();
    static {
        // setup default Material
        SHARED_DEFAULT_MATERIAL.setSpecularColor(Color.BLACK);
        SHARED_DEFAULT_MATERIAL.setDiffuseColor(Color.DARKGREY);
    }

    /**
     * Returns the mapping from file IDs to their corresponding MeshView objects.
     *
     * @return a ConcurrentHashMap mapping file IDs to MeshView instances.
     */
    public ConcurrentHashMap<String, MeshView> getFileIdToMeshMap() {
        return fileIdToMeshMap;
    }

    public MeshView getMeshOfFileID(String fileId) {
        return fileIdToMeshMap.get(fileId);
    }

    /**
     * Returns the MeshSelection object that manages the selection state of MeshView objects.
     *
     * @return the MeshSelection object used for selection management.
     */
    public MultipleMeshSelectionModel getSelectionModel() {
        return multipleMeshSelectionModel;
    }

    public PhongMaterial getDefaultMaterial() {
        return SHARED_DEFAULT_MATERIAL;
    }

    public List<MeshView> getMeshes() {
        return collectedMeshes;
    }

    public LinkedList<MeshView> getHiddenMeshes() {
        return hiddenMeshes;
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
            // this Set will hold all names of AnatomyNodes that are associated with that Mesh
            meshView.setUserData(new HashSet<String>());

            fileIdToMeshMap.put(id, meshView);
            collectedMeshes.add(meshView);

            if (progressCallback != null) {
                Platform.runLater(() -> progressCallback.accept(counter.incrementAndGet(), total));
            }
        });
    }

    public void assignNames(TreeItem<AnatomyNode> isATreeRoot, TreeItem<AnatomyNode> partOfTreeRoot) {
        TreeUtils.preOrderTreeViewTraversal(partOfTreeRoot, this::assignNamesToMeshes);
        TreeUtils.preOrderTreeViewTraversal(isATreeRoot, this::assignNamesToMeshes);
    }

    private void assignNamesToMeshes(TreeItem<AnatomyNode> anatomyNodeTreeItem) {
        if (anatomyNodeTreeItem.isLeaf()) {
            AnatomyNode anatomyNode = anatomyNodeTreeItem.getValue();
            for (String fileID : anatomyNode.getFileIDs()) {
                MeshView mesh = fileIdToMeshMap.get(fileID);
                if (mesh.getUserData() instanceof HashSet<?> userData) {
                    @SuppressWarnings("unchecked") // not ideal, but since we wonÄt reuse the userData it suffices
                    HashSet<String> names = (HashSet<String>) userData;
                    names.add(anatomyNode.getName());
                }
            }
        }
    }
}
