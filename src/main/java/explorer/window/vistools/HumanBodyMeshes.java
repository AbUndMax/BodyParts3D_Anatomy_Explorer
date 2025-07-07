package explorer.window.vistools;

import explorer.model.AnatomyNode;
import explorer.model.treetools.TreeUtils;
import explorer.window.selection.MeshSelectionManager;
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

/**
 * Manages loading, mapping, and visibility of 3D meshes representing human anatomy.
 * Loads .obj files into MeshView instances, maintains mappings from file IDs to meshes,
 * and provides selection and hidden mesh management.
 */
public class HumanBodyMeshes {

    // collect all meshes in a list and append them with addAll after all Meshes are parsed
    private final List<MeshView> collectedMeshes = Collections.synchronizedList(new ArrayList<>());

    // connects fileID to a MeshView instance loaded from that fileID
    private final ConcurrentHashMap<String, MeshView> fileIdToMeshMap = new ConcurrentHashMap<>();

    // meshSelection is interpreted as a SelectionModel for a humanBody instance
    private final MeshSelectionManager meshSelectionManager = new MeshSelectionManager(collectedMeshes);

    // list of meshes that are set to visible(false)
    private final ArrayList<MeshView> hiddenMeshes = new ArrayList<>();

    // Shared default material for all MeshViews -> this lifts a heavy load since only one Material has to be managed
    // and thus memory is saved
    public static final PhongMaterial SHARED_DEFAULT_MATERIAL = new PhongMaterial();
    static {
        // setup default Material
        SHARED_DEFAULT_MATERIAL.setSpecularColor(Color.BLACK);
        SHARED_DEFAULT_MATERIAL.setDiffuseColor(Color.DARKGREY);
    }

    /**
     * @return the mapping from file IDs to their corresponding MeshView objects.
     */
    public ConcurrentHashMap<String, MeshView> getFileIdToMeshMap() {
        return fileIdToMeshMap;
    }

    /**
     * Retrieves the MeshView associated with the given file ID.
     *
     * @param fileId the file ID whose mesh is requested
     * @return the MeshView corresponding to the file ID, or null if not found
     */
    public MeshView getMeshOfFileID(String fileId) {
        return fileIdToMeshMap.get(fileId);
    }

    /**
     * Retrieves a list of MeshView objects corresponding to the provided list of file IDs.
     *
     * @param fileIds the list of file IDs
     * @return an ArrayList of MeshView objects for the specified IDs
     */
    public ArrayList<MeshView> getMeshesOfFilesIDs(List<String> fileIds) {
        ArrayList<MeshView> meshes = new ArrayList<>();
        for (String fileId : fileIds) {
            meshes.add(getMeshOfFileID(fileId));
        }
        return meshes;
    }

    /**
     * @return the MeshSelectionManager that manages the selection state of MeshView objects.
     */
    public MeshSelectionManager getSelectionModel() {
        return meshSelectionManager;
    }

    /**
     * @return list of all loaded MeshView objects for the human body model
     */
    public List<MeshView> getMeshes() {
        return collectedMeshes;
    }

    /**
     * @return an ArrayList of currently hidden MeshView objects
     */
    public ArrayList<MeshView> getHiddenMeshes() {
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

    /**
     * Associates anatomy node names with their corresponding meshes by traversing two anatomy trees.
     * Processes both 'part-of' and 'is-a' hierarchies to populate mesh userData sets.
     *
     * @param isATreeRoot the root of the 'is-a' anatomy tree
     * @param partOfTreeRoot the root of the 'part-of' anatomy tree
     */
    public void mapFileIDsToMeshes(TreeItem<AnatomyNode> isATreeRoot, TreeItem<AnatomyNode> partOfTreeRoot) {
        TreeUtils.preOrderTreeViewTraversal(partOfTreeRoot, this::mapFileIDsToMeshes);
        TreeUtils.preOrderTreeViewTraversal(isATreeRoot, this::mapFileIDsToMeshes);
    }

    /**
     * Helper method that processes a single AnatomyNode TreeItem.
     * If the node is a leaf, adds its name to the MeshView userData set for each of its file IDs.
     *
     * @param anatomyNodeTreeItem the TreeItem to process
     */
    private void mapFileIDsToMeshes(TreeItem<AnatomyNode> anatomyNodeTreeItem) {
        if (anatomyNodeTreeItem.isLeaf()) {
            AnatomyNode anatomyNode = anatomyNodeTreeItem.getValue();
            for (String fileID : anatomyNode.getFileIDs()) {
                MeshView mesh = fileIdToMeshMap.get(fileID);
                if (mesh.getUserData() instanceof HashSet<?> userData) {
                    @SuppressWarnings("unchecked") // not ideal, but since we won't reuse the userData it suffices
                    HashSet<String> names = (HashSet<String>) userData;
                    names.add(anatomyNode.getName());
                }
            }
        }
    }
}
