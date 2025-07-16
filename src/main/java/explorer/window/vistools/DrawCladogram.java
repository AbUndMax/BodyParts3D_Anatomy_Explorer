package explorer.window.vistools;

import explorer.model.treetools.AnatomyNode;
import explorer.model.treetools.TreeUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

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
        double lineSpacing = 20; // 12pt letters + 2px extra space
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
        Map<AnatomyNode, Point2D> scaledMap =
                nodePointMap.entrySet().stream().collect(
                        Collectors.toMap(Map.Entry::getKey, e -> scaleFun.apply(e.getValue())));

        //generate the nodes, edges, labels
        generateGroupsRec(root, nodes, edges, labels, scaledMap, height, numberOfLeaves, true);

        return new Group(nodes, edges, labels);
    }


    /**
     * Recursively generates graphical components (nodes, edges, and labels) for the cladogram.
     * Adds a circle for each node, a label for each leaf, and lines connecting parent and child nodes.
     *
     * @param thisNode the current AnatomyNode being processed
     * @param nodes the JavaFX group collecting all node (circle) elements
     * @param edges the JavaFX group collecting all edge (line) elements
     * @param labels the JavaFX group collecting all label (text) elements
     * @param nodePointMap a map associating each node with its 2D position
     * @param height the total height of the cladogram display area
     * @param numberOfLeaves the number of leaf nodes in the tree
     * @param isRoot indicates whether this node is the root of the tree
     */
    public static void generateGroupsRec(AnatomyNode thisNode, Group nodes, Group edges, Group labels,
                                         Map<AnatomyNode, Point2D> nodePointMap, double height,
                                         int numberOfLeaves, boolean isRoot) {
        if (isRoot && !thisNode.getChildren().isEmpty()) {
            Point2D rootPoint = nodePointMap.get(thisNode);
            Point2D firstChildPoint = nodePointMap.get(thisNode.getChildren().getFirst());
            double xTarget = firstChildPoint.getX();
            Polyline rootLine = new Polyline(
                    rootPoint.getX(), rootPoint.getY(),
                    -xTarget, rootPoint.getY()
            );
            edges.getChildren().add(rootLine);
        }

        //add circle to nodes-group
        nodes.getChildren().add(createCircle(nodePointMap.get(thisNode), thisNode));

        // calculate font size
        double fontSize = calculateFontSize(height, numberOfLeaves);

        if (thisNode.getChildren().isEmpty()) {
            //if this is a leaf, add text to labels-group
            labels.getChildren().add(createLabel(thisNode, nodePointMap.get(thisNode), fontSize));
        } else {
            //for every child of this node: add an edge to it and recurse on it
            for (AnatomyNode child : thisNode.getChildren()) {
                edges.getChildren().add(createEdge(nodePointMap.get(thisNode), nodePointMap.get(child)));
                generateGroupsRec(child, nodes, edges, labels, nodePointMap, height, numberOfLeaves, false);
            }
        }
    }

    /**
     * Calculates the appropriate font size for leaf labels based on the overall height and
     * the number of leaves to ensure clear and non-overlapping text.
     *
     * @param height the total height of the diagram
     * @param numberOfLeaves the number of leaf nodes in the tree
     * @return the calculated font size, capped at 12
     */
    protected static double calculateFontSize(double height, int numberOfLeaves) {
        double spacing = height / numberOfLeaves;
        return Math.min(spacing * 0.8, 12);
    }

    /**
     * Finds and returns the longest node name from the provided node map.
     * Used for determining label width requirements.
     *
     * @param nodePointMap map of nodes to their positions
     * @return the longest node name string
     */
    protected static String calculateLongestStringInMap(Map<AnatomyNode, Point2D> nodePointMap) {
        return nodePointMap.keySet().stream()
                .map(AnatomyNode::getName)
                .max(Comparator.comparingInt(String::length))
                .orElse("");
    }


    /**
     * Below: some helper functions to create the javafx elements of the tree.
     */
    /**
     * Creates a polyline representing an edge between two nodes in the tree.
     * The line moves horizontally and vertically to form an L-shape.
     *
     * @param a the starting point
     * @param b the ending point
     * @return a JavaFX Polyline connecting the two points
     */
    public static Polyline createEdge(Point2D a, Point2D b) {
        return new Polyline(a.getX(), a.getY(), a.getX(), b.getY(), b.getX(), b.getY());
    }

    /**
     * Creates a JavaFX Text label for a leaf node and positions it to the right of the node.
     *
     * @param node the leaf node to be labeled
     * @param p the 2D position of the node
     * @param fontSize the font size to be used
     * @return a configured Text object
     */
    public static Text createLabel(AnatomyNode node, Point2D p, double fontSize) {
        Text text = new Text(node.getName());
        text.applyCss();

        text.setFont(Font.font("Arial", fontSize));

        Bounds bounds = text.getLayoutBounds();
        double textHeight = bounds.getHeight();
        double textOffset = bounds.getMinY();
        text.setX(p.getX() + 10);
        text.setY(p.getY() - (textHeight / 2) - textOffset);

        return text;
    }

    /**
     * Creates a JavaFX Circle representing a node.
     * A tooltip is attached displaying the node's name.
     *
     * @param p the position of the node
     * @param node the node to be represented
     * @return a Circle object representing the node
     */
    public static Circle createCircle(Point2D p, AnatomyNode node) {
        Circle circle = new Circle(p.getX(), p.getY(), 4);
        Tooltip tooltip = new Tooltip(node.getName());
        tooltip.setShowDelay(Duration.ZERO);
        Tooltip.install(circle, tooltip);
        return circle;
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
