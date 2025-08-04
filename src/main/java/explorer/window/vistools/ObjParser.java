package explorer.window.vistools;


import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * OBJ parser
 * SOURCE: Daniel Huson, 5.2025
 */
public class ObjParser {
    /**
     * loads a 3D object from an OBJ file into a mesh, triangulating, if necessary.
     * Does not support the full OBJ syntax, but suffices for the OBJ files used in the course
     * @param filePath path to file
     * @return mesh
     * @throws IOException problem reading or parsing file
     */
    public static TriangleMesh load(String filePath) throws IOException {
        var vertices = new ArrayList<Float>();
        var normals = new ArrayList<Float>();
        var texCoords = new ArrayList<Float>();
        var faces = new ArrayList<Integer>();

        var hasNormals=-1;

        try (var br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                var tokens = line.trim().split("\\s+");
                if (tokens.length == 0) continue;

                switch (tokens[0]) {
                    case "v":
                        vertices.add(Float.parseFloat(tokens[1]));
                        vertices.add(Float.parseFloat(tokens[2]));
                        vertices.add(Float.parseFloat(tokens[3]));
                        break;
                    case "vn":
                        normals.add(Float.parseFloat(tokens[1]));
                        normals.add(Float.parseFloat(tokens[2]));
                        normals.add(Float.parseFloat(tokens[3]));
                        break;
                    case "vt":
                        texCoords.add(Float.parseFloat(tokens[1]));
                        texCoords.add(1 - Float.parseFloat(tokens[2])); // flip V
                        break;

                    case "f":
                        var n = tokens.length - 1;
                        var vIndices = new int[n];
                        var tIndices = new int[n];
                        var nIndices = new int[n];

                        for (var i = 0; i < n; i++) {
                            var parts = tokens[i + 1].split("/");
                            vIndices[i] = Integer.parseInt(parts[0]) - 1;
                            if (parts.length >= 2 && !parts[1].isEmpty()) {
                                tIndices[i] = Integer.parseInt(parts[1]) - 1;
                            } else {
                                tIndices[i] = 0;
                            }
                            if (parts.length == 3 && !parts[2].isEmpty()) {
                                if (hasNormals == 0)
                                    throw new IOException("some faces have normals, others don't");
                                else hasNormals = 1;
                                nIndices[i] = Integer.parseInt(parts[2]) - 1;
                            } else {
                                if (hasNormals == 1)
                                    throw new IOException("some faces have normals, others don't");
                                else hasNormals = 0;
                            }
                        }
                        if (n == 3) {
                            faces.add(vIndices[0]);
                            if (hasNormals == 1)
                                faces.add(nIndices[0]);
                            faces.add(tIndices[0]);
                            faces.add(vIndices[1]);
                            if (hasNormals == 1)
                                faces.add(nIndices[1]);
                            faces.add(tIndices[1]);
                            faces.add(vIndices[2]);
                            if (hasNormals == 1)
                                faces.add(nIndices[2]);
                            faces.add(tIndices[2]);
                        } else if (n > 3) {
                            for (int i = 1; i < n - 1; i++) {
                                faces.add(vIndices[0]);
                                if (hasNormals == 1)
                                    faces.add(nIndices[0]);
                                faces.add(tIndices[0]);
                                faces.add(vIndices[i]);
                                if (hasNormals == 1)
                                    faces.add(nIndices[i]);
                                faces.add(tIndices[i]);
                                faces.add(vIndices[i + 1]);
                                if (hasNormals == 1)
                                    faces.add(nIndices[i + 1]);
                                faces.add(tIndices[i + 1]);
                            }
                        }
                        break;
                }
            }
        }
        if (texCoords.isEmpty()) {
            texCoords.add(0.0f);
            texCoords.add(0.0f);
        }

        var mesh = new TriangleMesh();
        mesh.getPoints().setAll(toFloatArray(vertices));
        mesh.getTexCoords().setAll(toFloatArray(texCoords));
        mesh.getFaces().setAll(toIntArray(faces));

        if(!normals.isEmpty()) {
            mesh.getNormals().setAll(toFloatArray(normals));
            mesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
        }
        return mesh;
    }

    private static float[] toFloatArray(List<Float> list) {
        var array = new float[list.size()];
        for (var i = 0; i < list.size(); i++) array[i] = list.get(i);
        return array;
    }

    private static int[] toIntArray(List<Integer> list) {
        var array = new int[list.size()];
        for (var i = 0; i < list.size(); i++) array[i] = list.get(i);
        return array;
    }
}

