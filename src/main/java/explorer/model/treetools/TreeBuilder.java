package explorer.model.treetools;

import java.io.*;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * This class was used only once to serialize the AnatomyNode tree to a kryo file for high performance tree Loading
 *
 * CLASS NOT NEEDED ANYMORE! - CLASS IS KEPT FOR CLARIFICATION WHERE THE .kryo FILES CAME FROM ONLY!
 */
class TreeBuilder {

    /**
     * Main method to serialize both the is-a and part-of trees.
     * Invokes the methods that build and save the respective trees to Kryo files.
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        serializeIsATree();
        serializePartOfTree();
    }

    /**
     * Builds and serializes the "is-a" anatomical tree.
     * Loads relationships and element parts, constructs the tree rooted at "FMA62955",
     * prints the tree in Newick format, and saves it using Kryo serialization.
     */
    private static void serializeIsATree() {
        String ISA_INCLUSION_RELATION_LIST_PATH = "PATH";
        String ISA_ELEMENTS_PARTS_PATH = "PATH";
        ArrayList<Relation> relations = loadRelationsFile(ISA_INCLUSION_RELATION_LIST_PATH);
        HashMap<String, ArrayList<String>> conceptIDToFileID = loadElementFile(ISA_ELEMENTS_PARTS_PATH);
        String ROOT_CONCEPT = "FMA62955";
        ConceptNode tree = createTree(relations, conceptIDToFileID, ROOT_CONCEPT);
        System.out.println(tree.toNewick()); // control tree

        KryoUtils.freezeTree(tree, "src/main/resources/serializedTrees/isA_tree.kryo");
    }

    /**
     * Builds and serializes the "part-of" anatomical tree.
     * Loads relationships and element parts, constructs the tree rooted at "FMA20394",
     * prints the tree in Newick format, and saves it using Kryo serialization.
     */
    private static void serializePartOfTree() {
        String PARTOF_INCLUSION_RELATION_LIST_PATH = "PATH";
        String PARTOF_ELEMENTS_PARTS_PATH = "PATH";
        ArrayList<Relation> relations = loadRelationsFile(PARTOF_INCLUSION_RELATION_LIST_PATH);
        HashMap<String, ArrayList<String>> conceptIDToFileID = loadElementFile(PARTOF_ELEMENTS_PARTS_PATH);
        String ROOT_CONCEPT = "FMA20394";
        ConceptNode tree = createTree(relations, conceptIDToFileID, ROOT_CONCEPT);
        System.out.println(tree.toNewick()); // control tree
        KryoUtils.freezeTree(tree, "src/main/resources/serializedTrees/partOf_tree.kryo");
    }

    private record Relation(String parentID, String parentName, String childID, String childName){}

    /**
     * Loads a tab-separated values file containing hierarchical anatomical relations.
     * Each line contain: parentID, parentName, childID, childName.
     * Skips the header and returns a list of Relation records.
     *
     * @param filePath path to the relation file
     * @return list of Relation objects
     *
     * SOURCE: assignment02
     */
    private static ArrayList<Relation> loadRelationsFile(String filePath) {
        ArrayList<Relation> relations = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
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

    /**
     * Loads a mapping from ConceptID to a list of associated FileIDs from a tab-separated file.
     * Each line must contain at least three fields, the third of which is stored.
     *
     * @param filePath path to the file containing ConceptID to FileID mappings
     * @return HashMap of ConceptID to list of FileIDs or null if an error occurs
     *
     * SOURCE: assignment02
     */
    private static HashMap<String, ArrayList<String>> loadElementFile(String filePath) {
        HashMap<String, ArrayList<String>> IDtoFilelist = new HashMap<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            if ((reader.readLine()==null)) {
                System.err.println("TreeLoader: load(): elementsFile empty.");
                return null;
            }
            String line;
            while ((line=reader.readLine()) != null) {
                String[] lineArr = line.split("\t");
                String conceptID = lineArr[0].trim();
                ArrayList<String> fileList = IDtoFilelist.containsKey(conceptID)
                        ? IDtoFilelist.get(conceptID)
                        : new ArrayList<>();

                fileList.add(lineArr[2].trim());
                IDtoFilelist.put(conceptID, fileList);
            }
        } catch (FileNotFoundException e) {
            System.err.println("loadElement: elementsFile not found.");
            return null;
        } catch (IOException e) {
            System.err.println("loadElement: load(): Something happened with elementsFile");
            return null;
        }
        return IDtoFilelist;
    }

    /**
     * Constructs a tree of AnatomyNode objects based on a list of relations and ConceptID to FileID mappings.
     * Each node is connected to its parent and children based on the relation list.
     *
     * @param relations list of parent-child relations
     * @param conceptIDToFileID map of ConceptID to FileIDs
     * @param rootConceptID the ID of the root node
     * @return root AnatomyNode of the constructed tree
     *
     * SOURCE: assignment01
     */
    private static ConceptNode createTree(ArrayList<Relation> relations, HashMap<String,
            ArrayList<String>> conceptIDToFileID, String rootConceptID) {

        HashMap<String, ConceptNode> idToNode = new HashMap<>();

        for (Relation relation : relations) {
            String parentID = relation.parentID();
            String parentName = relation.parentName();
            String childID = relation.childID();
            String childName = relation.childName();

            ConceptNode parentNode = idToNode.getOrDefault(parentID, new ConceptNode(parentID,
                                                                                     parentName,
                                                                                     new ArrayList<>(),
                                                                                     conceptIDToFileID.get(parentID)));

            ConceptNode childNode = idToNode.getOrDefault(childID, new ConceptNode(childID,
                                                                                   childName,
                                                                                   new ArrayList<>(),
                                                                                   conceptIDToFileID.get(childID)));
            parentNode.addChild(childNode);
            idToNode.putIfAbsent(relation.parentID(), parentNode);
            idToNode.putIfAbsent(relation.childID(), childNode);
        }

        return idToNode.get(rootConceptID);
    }
}
