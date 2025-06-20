package explorer.model.treeBuilder;

import explorer.model.AnatomyNode;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * This class was used only once to serialize the AnatomyNode tree to a kryo file for high performance tree Loading
 */
public class PartOfTreeBuilder {

    public PartOfTreeBuilder() {
        LinkedList<Relation> relations = loadPartOfRelations();
        HashMap<String, LinkedList<String>> conceptIDToFileID = loadElementPartOf();
        AnatomyNode tree = createPartOfTree(relations, conceptIDToFileID);
        System.out.println(tree.toNewick());

        KryoFreezer.freezeTree(tree, "src/main/resources/serializedTress/partOf_tree.kryo");
    }

    public record Relation(String parentID, String parentName, String childID, String childName){}

    // Function to load a tab seperated values file of format:
    // parentID \t parentName \t childID \t childName
    // and returns a list of instantiated Relation objects
    public static LinkedList<Relation> loadPartOfRelations() {
        LinkedList<Relation> relations = new LinkedList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/tree/partOf/partof_inclusion_relation_list.txt"))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                String parentID = parts[0].trim();
                String parentName = parts[1].trim();
                String childID = parts[2].trim();
                String childName = parts[3].trim();
                relations.add(new Relation(parentID, parentName, childID, childName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return relations;
    }

    // Function that creates a Tree structure based on the Relation List
    public static AnatomyNode createPartOfTree(LinkedList<Relation> relations, HashMap<String, LinkedList<String>> conceptIDToFileID) {
        HashMap<String, AnatomyNode> idToNode = new HashMap<>();

        for (Relation relation : relations) {
            String parentID = relation.parentID();
            String parentName = relation.parentName();
            String childID = relation.childID();
            String childName = relation.childName();

            AnatomyNode parentNode = idToNode.getOrDefault(parentID, new AnatomyNode(parentID, parentName, new LinkedList<>(), conceptIDToFileID.get(parentID)));
            AnatomyNode childNode = idToNode.getOrDefault(childID, new AnatomyNode(childID, childName, new LinkedList<>(), conceptIDToFileID.get(childID)));
            parentNode.addChild(childNode);
            idToNode.putIfAbsent(relation.parentID(), parentNode);
            idToNode.putIfAbsent(relation.childID(), childNode);
        }

        // root concept == "FMA20394" (for part-of tree)
        return idToNode.get("FMA20394");
    }

    private static HashMap<String, LinkedList<String>> loadElementPartOf() {
        HashMap<String, LinkedList<String>> IDtoFilelist = new HashMap<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/tree/partOf/partof_element_parts.txt"));
            if ((reader.readLine()==null)) {
                System.err.println("TreeLoader: load(): elementsFile empty.");
                return null;
            }
            String line;
            while ((line=reader.readLine()) != null) {
                String[] lineArr = line.split("\t");
                String conceptID = lineArr[0].trim();
                LinkedList<String> fileList = IDtoFilelist.containsKey(conceptID) ? IDtoFilelist.get(conceptID) : new LinkedList<String>();
                fileList.add(lineArr[2].trim());
                IDtoFilelist.put(conceptID, fileList);
            }
        } catch (FileNotFoundException e) {
            System.err.println("TreeLoader: load(): elementsFile not found.");
            return null;
        } catch (IOException e) {
            System.err.println("TreeLoader: load(): Something happened with elementsFile");
            return null;
        }
        return IDtoFilelist;
    }


}
