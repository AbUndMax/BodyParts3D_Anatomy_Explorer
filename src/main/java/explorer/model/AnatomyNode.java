package explorer.model;

import java.util.LinkedList;
import java.util.stream.Collectors;

public class AnatomyNode {

    private String conceptID;
    private String name;
    private LinkedList<String> fileIDs;
    private LinkedList<AnatomyNode> children;

    public AnatomyNode(String conceptId, String name, LinkedList<AnatomyNode> children, LinkedList<String> fileIds) {
        this.conceptID = conceptId;
        this.name = name;
        this.children = children;
        this.fileIDs = fileIds;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public String getConceptID() {
        return conceptID;
    }

    public String getName() {
        return name;
    }

    public LinkedList<String> getFileIDs() {
        return fileIDs;
    }

    public LinkedList<AnatomyNode> getChildren() {
        return children;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFileIDs(LinkedList<String> fileIDs) {
        this.fileIDs = fileIDs;
    }

    public void addFileID(String objPath) {
        this.fileIDs.add(objPath);
    }

    public void setChildren(LinkedList<AnatomyNode> children) {
        this.children = children;
    }

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

    public String toString() {
        return this.name;
    }
}
