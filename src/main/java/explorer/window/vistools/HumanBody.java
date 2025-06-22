package explorer.window.vistools;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import java.io.File;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class HumanBody extends Group{

    // connects fileID to a MeshView instance loaded from that fileID
    private final ConcurrentHashMap<String, MeshView> meshViews = new ConcurrentHashMap<>();

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

        PhongMaterial sharedMaterial = new PhongMaterial();
        sharedMaterial.setSpecularColor(Color.BLACK);
        sharedMaterial.setDiffuseColor(Color.DARKGREY);

        AtomicInteger counter = new AtomicInteger();
        int total = objFiles.length;

        // collect all meshes in a list and append them with addAll after all Meshes are parsed
        List<MeshView> collectedMeshes = Collections.synchronizedList(new ArrayList<>());

        // Parallel loading of meshes to speed up initial load up
        Arrays.stream(objFiles).parallel().forEach(objFile -> {
            String fileName = objFile.getName();
            String id = fileName.substring(0, fileName.lastIndexOf('.'));

            TriangleMesh mesh;
            try {
                mesh = ObjParser.load(objFile.getPath());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            MeshView meshView = new MeshView(mesh);
            meshView.setMaterial(sharedMaterial);

            meshViews.put(id, meshView);

            collectedMeshes.add(meshView);

            if (progressCallback != null) {
                Platform.runLater(() -> progressCallback.accept(counter.incrementAndGet(), total));
            }
        });

        Platform.runLater(() -> this.getChildren().addAll(collectedMeshes));
    }
}
