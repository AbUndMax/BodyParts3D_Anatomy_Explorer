package explorer.window.vistools;

import explorer.model.treetools.AnatomyNode;
import explorer.model.treetools.TreeUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * imported and modified from assignment04
 */
public class DrawCladogram {

    /**
     * Generates and returns a graphical representation of a cladogram based on the given root node
     * and a map linking nodes to their corresponding coordinates. The method calculates the required
     * width and height for the diagram dynamically and in such a way that the label fontsize will be set to 12.
     * Further processed are delegated to an overloaded method.
     *
     * @param root the root node of the cladogram representing the starting point of the tree structure
     * @param nodePointMap a map associating each node in the tree with a 2D point representing its position
     * @return a {@code Group} object containing JavaFX graphical elements representing the entire cladogram
     */
    public static Group apply(AnatomyNode root, Map<AnatomyNode, Point2D> nodePointMap) {
        double lineSpacing = 14; // 12pt letters + 2px extra space
        int numberOfLeaves = TreeUtils.numberOfLeaves(root);
        double height = numberOfLeaves * lineSpacing;

        String longestString = calculateLongestStringInMap(nodePointMap);
        int maxNameLength = longestString.length();

        double approxCharWidth = 7.0; // avg char width at 12pt
        double labelPadding = 10.0;

        double width = TreeUtils.horizontalTreeDepth(root) * 0.8 + (maxNameLength * approxCharWidth) + labelPadding;
        return apply(root, nodePointMap, width, height);
    }

    /**
     * Generates and returns a graphical representation of a cladogram based on the specified
     * root node, a map linking nodes to their respective coordinates, and the desired display width and height.
     * This method computes a scaled mapping of the node positions to fit within the given dimensions,
     * creates JavaFX graphical elements for the tree structure, and organizes them into separate groups
     * for nodes, edges, and labels.
     *
     * @param root the root node of the cladogram representing the starting point of the tree structure
     * @param nodePointMap a map associating each node in the tree with a Point2D object representing its position
     * @param width the desired width of the display space for the entire cladogram
     * @param height the desired height of the display space for the entire cladogram
     * @return a Group object containing graphical elements (nodes, edges, labels) for the visual representation of the cladogram
     */
    public static Group apply(AnatomyNode root, Map<AnatomyNode, Point2D> nodePointMap, double width, double height) {
        Group nodes = new Group();
        Group edges = new Group();
        Group labels = new Group();

        int numberOfLeaves = TreeUtils.numberOfLeaves(root);

        //scale map to width and height
        Function<Point2D, Point2D> scaleFun = setupScaleFunction(nodePointMap.values(), width, height);
        Map<AnatomyNode, Point2D> scaledMap = nodePointMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> scaleFun.apply(e.getValue())));

        //generate the nodes, edges, labels
        generateGroupsRec(root, nodes, edges, labels, scaledMap, height, numberOfLeaves);

        nodes.getChildren().remove(1);

        return new Group(nodes, edges, labels);
    }


    public static void generateGroupsRec(AnatomyNode thisNode, Group nodes, Group edges, Group labels, Map<AnatomyNode, Point2D> nodePointMap, double height, int numberOfLeaves) {

        //add circle to nodes-group
        nodes.getChildren().add(createCircle(nodePointMap.get(thisNode)));

        // calculate font size
        double fontSize = calculateFontSize(height, numberOfLeaves);

        if (thisNode.getChildren().isEmpty()) {
            //if this is a leaf, add text to labels-group
            labels.getChildren().add(createLabel(thisNode, nodePointMap.get(thisNode), fontSize));
            return;
        } else {
            //for every child of this node: add an edge to it and recurse on it
            for (AnatomyNode child : thisNode.getChildren()) {
                edges.getChildren().add(createEdge(nodePointMap.get(thisNode), nodePointMap.get(child)));
                generateGroupsRec(child, nodes, edges, labels, nodePointMap, height, numberOfLeaves);
            }
        }
    }

    protected static double calculateFontSize(double height, int numberOfLeaves) {
        double spacing = height / numberOfLeaves;
        return Math.min(spacing * 0.8, 12);
    }

    protected static String calculateLongestStringInMap(Map<AnatomyNode, Point2D> nodePointMap) {
        return nodePointMap.keySet().stream()
                .map(AnatomyNode::getName)
                .max(Comparator.comparingInt(String::length))
                .orElse("");
    }


    /**
     * Below: some helper functions to create the javafx elements of the tree.
     */
    public static Polyline createEdge(Point2D a, Point2D b) {

        return new Polyline(a.getX(), a.getY(), a.getX(), b.getY(), b.getX(), b.getY());
    }

    public static Text createLabel(AnatomyNode node, Point2D p, double fontSize) {
        Text text = new Text(node.getName() + " ");
        text.applyCss();

        text.setFont(Font.font("Arial", fontSize));

        Bounds bounds = text.getLayoutBounds();
        double textHeight = bounds.getHeight();
        double textOffset = bounds.getMinY();
        text.setX(p.getX() + 4);
        text.setY(p.getY() - (textHeight / 2) - textOffset);

        return text;
    }

    public static Circle createCircle(Point2D p, double radius) {
        return new Circle(p.getX(), p.getY(), radius);
    }

    public static Circle createCircle(Point2D p) {
        return createCircle(p, 2);
    }

    /**
     * Creates a scaling function that maps input points to a scaled coordinate system
     * based on the minimum and maximum bounds of the provided points and the specified width and height.
     *
     * @param points a collection of Point2D objects representing the points to be scaled
     * @param width the target width for scaling
     * @param height the target height for scaling
     * @return a function that takes a Point2D object and maps it to a scaled Point2D
     */
    public static Function<Point2D,Point2D> setupScaleFunction(Collection<Point2D> points, double width,
                                                               double height) {
        var xMin=points.stream().mapToDouble(Point2D::getX).min().orElse(0.0);
        var xMax=points.stream().mapToDouble(Point2D::getX).max().orElse(0.0);
        var yMin=points.stream().mapToDouble(Point2D::getY).min().orElse(0.0);
        var yMax=points.stream().mapToDouble(Point2D::getY).max().orElse(0.0);
        return (Point2D p) -> new Point2D((p.getX() - xMin) / (xMax - xMin) * width,
                                          (p.getY() - yMin) / (yMax - yMin) * height);
    }
}
