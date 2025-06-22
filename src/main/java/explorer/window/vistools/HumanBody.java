package explorer.window.vistools;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.BiConsumer;

public class HumanBody extends Group{

    // connects fileID to a MeshView instance loaded from that fileID
    private final HashMap<String, MeshView> meshViews = new HashMap<>();

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

        if (objFiles == null) return;

        int total = objFiles.length;
        for (int i = 0; i < total; i++) {
            File objFile = objFiles[i];
            String fileName = objFile.getName();
            String id = fileName.substring(0, fileName.lastIndexOf('.'));

            TriangleMesh mesh = null;
            try {
                mesh = ObjParser.load(objFile.getPath());
            } catch (IOException e) {
                System.err.println("Error loading " + objFile.getPath());
            }

            // set materials
            MeshView meshView = new MeshView(mesh);
            PhongMaterial material = new PhongMaterial();
            material.setSpecularColor(Color.BLACK);
            material.setDiffuseColor(Color.DARKGREY);
            meshView.setMaterial(material);

            meshViews.put(id, meshView);
            this.getChildren().add(meshView);

            // report the progress of currently loaded files back
            int current = i + 1;
            if (progressCallback != null) {
                Platform.runLater(() -> progressCallback.accept(current, total));
            }
        }
    }
}
