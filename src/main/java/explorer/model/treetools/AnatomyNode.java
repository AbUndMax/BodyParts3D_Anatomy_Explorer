package explorer.model.treetools;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Represents a node in the anatomical structure tree.
 * Each node contains a concept ID, a name, associated file IDs, and child nodes.
 * Provides functionality to manage child nodes and to export the structure in Newick format.
 */
public class AnatomyNode {

    private String conceptID;
    private String name;
    private ArrayList<String> fileIDs;
    private ArrayList<AnatomyNode> children;

    /**
     * Constructs an AnatomyNode with the specified concept ID, name, child nodes, and file IDs.
     *
     * @param conceptId the unique identifier of the anatomical concept
     * @param name the display name of the node
     * @param children the list of child nodes
     * @param fileIds the list of associated file IDs
     */
    public AnatomyNode(String conceptId, String name, ArrayList<AnatomyNode> children, ArrayList<String> fileIds) {
        this.conceptID = conceptId;
        this.name = name;
        this.children = children;
        this.fileIDs = fileIds;
    }

    /**
     * Checks whether the node is a leaf (has no children).
     *
     * @return true if the node has no children, false otherwise
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }

    /**
     * @return the concept ID
     */
    public String getConceptID() {
        return conceptID;
    }

    /**
     * @return the name of the node
     */
    public String getName() {
        return name;
    }

    /**
     * @return the list of associated file IDs with the node
     */
    public ArrayList<String> getFileIDs() {
        return fileIDs;
    }

    /**
     * @return the list of child nodes
     */
    public ArrayList<AnatomyNode> getChildren() {
        return children;
    }

    /**
     * Sets the name of the node.
     *
     * @param name the new name of the node
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the file IDs associated with the node.
     *
     * @param fileIDs the new list of file IDs
     */
    public void setFileIDs(ArrayList<String> fileIDs) {
        this.fileIDs = fileIDs;
    }

    /**
     * Adds a file ID to the list of associated file IDs.
     *
     * @param objPath the file ID to add
     */
    public void addFileID(String objPath) {
        this.fileIDs.add(objPath);
    }

    /**
     * Sets the list of child nodes.
     *
     * @param children the new list of child nodes
     */
    public void setChildren(ArrayList<AnatomyNode> children) {
        this.children = children;
    }

    /**
     * Adds a child node to the list of children.
     *
     * @param child the child node to add
     */
    public void addChild(AnatomyNode child) {
        children.add(child);
    }

    /**
     * Converts the tree structure rooted at the current node into Newick format.
     *
     * @return a String representing the tree structure in Newick format, ending with a semicolon
     */
    public String toNewick() {
        return buildNewick(this) + ";";
    }

    /**
     * Recursively builds a Newick representation of the tree structure starting from the given node.
     * The Newick format represents tree structures using nested, parenthetical notation.
     *
     * Only non-filtered nodes are saved!
     *
     * @param node the root node for which the Newick representation is to be built
     * @return a String representing the subtree rooted at the given node in Newick format
     */
    private String buildNewick(AnatomyNode node) {
        if (node.isLeaf()) {
            return node.name;
        } else {
            String childrenNewick = node.getChildren().stream()
                    .map(this::buildNewick)
                    .collect(Collectors.joining(","));
            return "(" + childrenNewick + ")" + node.getName();
        }
    }

    /**
     * Returns the name of the node as its string representation.
     *
     * @return the name of the node
     */
    public String toString() {
        return this.name;
    }
}
