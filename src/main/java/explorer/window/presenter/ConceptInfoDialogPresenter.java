package explorer.window.presenter;

import explorer.model.Cladogram;
import explorer.model.treetools.ConceptNode;
import explorer.model.treetools.KryoUtils;
import explorer.model.treetools.TreeUtils;
import explorer.window.controller.ConceptInfoDialogController;
import explorer.window.vistools.DrawCladogram;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.util.*;

/**
 * Presenter class for the Concept Info Dialog view.
 * Handles user interactions and visual updates for the concept information dialog,
 * including chart displays and navigation between selected nodes.
 */
public class ConceptInfoDialogPresenter {

    private final ConceptInfoDialogController controller;
    private final TreeView<ConceptNode> treeView;

    /**
     * Constructs a new presenter for the Concept Info Dialog view.
     *
     * @param selectedItems the currently selected tree items
     * @param treeView the TreeView of concept tree
     * @param controller the controller managing the view
     */
    public ConceptInfoDialogPresenter(ObservableList<TreeItem<ConceptNode>> selectedItems,
                                      TreeView<ConceptNode> treeView,
                                      ConceptInfoDialogController controller) {

        this.controller = controller;
        this.treeView = treeView;

        // get the selectedItem
        TreeItem<ConceptNode> selectedItem = selectedItems.getFirst();

        // if there are multiple selected nodes,
        // setup a choiceBox that allows to switch between the multiple selected nodes
        if (selectedItems.size() > 1) {
            ChoiceBox<TreeItem<ConceptNode>> nodeChoiceBox = controller.getConceptChoiceBox();
            setupNodeChoiceBox(nodeChoiceBox, selectedItems);
            selectedItem = nodeChoiceBox.getValue();
        }

        // draw the Tabs
        redrawCharacteristicsTab(selectedItem);
        redrawNodeDegDistTab(selectedItem);
        redrawTreeTab(selectedItem);
    }

    /**
     * Configures the node choice box for switching between multiple selected nodes.
     * Adds all selected nodes, sets up the string converter for display, and defines the action handler.
     *
     * @param nodeChoiceBox the ChoiceBox to configure
     * @param selectedItems the items to populate the ChoiceBox with
     */
    private void setupNodeChoiceBox(ChoiceBox<TreeItem<ConceptNode>> nodeChoiceBox, ObservableList<TreeItem<ConceptNode>> selectedItems) {
        // Add all selected nodes to the choice box as selectable options
        for (TreeItem<ConceptNode> item : selectedItems) {
            nodeChoiceBox.getItems().add(item);
        }

        // String presentation in choiceBox
        nodeChoiceBox.setConverter(new StringConverter<TreeItem<ConceptNode>>() {
            @Override
            public String toString(TreeItem<ConceptNode> conceptNodeTreeItem) {
                return conceptNodeTreeItem.getValue().getName();
            }

            @Override
            public TreeItem<ConceptNode> fromString(String s) {
                return null;
            }
        });

        // set first selected Node by default
        nodeChoiceBox.setValue(nodeChoiceBox.getItems().getFirst());

        nodeChoiceBox.setOnAction(event -> {
            TreeItem<ConceptNode> selectedItem = nodeChoiceBox.getValue();
            redrawCharacteristicsTab(selectedItem);
            redrawNodeDegDistTab(selectedItem);
            redrawTreeTab(selectedItem);
        });

        nodeChoiceBox.setVisible(true);
    }

    /**
     * Redraws and updates the Characteristics tab UI elements for the given node.
     * Updates labels, calculates statistics, and refreshes the pie and bar charts.
     *
     * @param selectedItem the TreeItem representing the selected concept node
     */
    private void redrawCharacteristicsTab(TreeItem<ConceptNode> selectedItem) {
        ConceptNode selectedConcept = selectedItem.getValue();

        controller.getSelectedConceptLabel().setText(selectedConcept.getName());
        TreeItem<ConceptNode> parent = selectedItem.getParent();
        controller.getParentConceptLabel().setText(parent == null ? "No parent!" : parent.getValue().getName());

        int depthFromRoot = TreeUtils.calculateDepthToRoot(selectedItem);
        controller.getDepthFromRootLabel().setText(String.valueOf(depthFromRoot));
        controller.getNumberOfChildsLabel().setText(String.valueOf(selectedConcept.getChildren().size()));
        controller.getNumberOfSiblingsLabel().setText(String.valueOf(parent == null ? 0 : parent.getChildren().size() - 1));
        controller.getNumberOfMeshesLabel().setText(String.valueOf(selectedConcept.getFileIDs().size()));

        int subTreeSize = TreeUtils.calculateTreeSize(selectedItem);
        controller.getSubtreeSizeLabel().setText(String.valueOf(subTreeSize));
        controller.getSubtreeHeightLabel().setText(String.valueOf(TreeUtils.horizontalTreeDepth(selectedConcept)));
        int leavesInSubtree = TreeUtils.numberOfLeaves(selectedConcept);
        controller.getNumberLeavesLabel().setText(String.valueOf(leavesInSubtree));
        int totalLeaves = TreeUtils.numberOfLeaves(treeView.getRoot().getValue());
        double percentage = ((double) leavesInSubtree / totalLeaves) * 100;
        controller.getLeavesBelowLabel().setText(String.format(percentage == 100 ? "%.0f %%" : "%.2f %%", percentage));


        // setup of subtree coverage pieChart:
        drawCoveragePie(subTreeSize);
        drawNodePerDepthPlot(selectedItem, depthFromRoot);

    }

    /**
     * Draws or updates the pie chart representing subtree coverage relative to the full tree.
     * Configures data and tooltips for each pie section.
     *
     * @param subTreeSize the size of the currently selected subtree
     */
    private void drawCoveragePie(int subTreeSize) {
        int totalTreeSize = TreeUtils.calculateTreeSize(treeView.getRoot());
        PieChart coveragePie = controller.getSubtreeCoveragePieChart();
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Subtree", subTreeSize),
                new PieChart.Data("Rest", totalTreeSize - subTreeSize)
        );

        coveragePie.setData(pieData);

        for (PieChart.Data data : pieData) {
            double pct = data.getPieValue() / totalTreeSize * 100;
            Tooltip tooltip = new Tooltip(String.format("%.2f %%", pct));
            tooltip.setShowDelay(Duration.ZERO);
            Tooltip.install(data.getNode(), tooltip);
        }
    }

    /**
     * Draws or updates the bar chart showing the number of nodes per depth in the subtree.
     * Maintains smooth animation by updating only necessary bars and removing deprecated ones.
     *
     * @param selectedItem the TreeItem for which to visualize the depth distribution
     * @param depthFromRoot the depth of the selected node from the tree root
     */
    private void drawNodePerDepthPlot(TreeItem<ConceptNode> selectedItem, int depthFromRoot) {
        // setup nodes per depth graph:
        // Key: depth, value: number of nodes
        Map<Integer, Integer> nodesPerDepth = TreeUtils.countNodesPerDepth(selectedItem);
        BarChart<String, Number> nodePerDepthChart = controller.getConceptsPerDepthBarChart();

        // the following code was build with the help of AI and modified to get the wanted behavior of the animation!
        XYChart.Series<String, Number> series;
        // Sortiere of categories by ascending depth for initial draw
        List<Integer> sortedDepths = new ArrayList<>(nodesPerDepth.keySet());
        Collections.sort(sortedDepths);

        // if the chart gets drawn the first time
        if (nodePerDepthChart.getData().isEmpty()) {
            series = new XYChart.Series<>();

            // add the sorted depths to the series data
            for (int depth : sortedDepths) {
                String depthLabel = String.valueOf(depth + depthFromRoot);
                series.getData().add(new XYChart.Data<>(depthLabel, nodesPerDepth.get(depth)));
            }

            nodePerDepthChart.getData().add(series);

        // if the Plot was already once drawn:
        // I update existing bars and remove bars that are not needed.
        // this is necessary to make the animation smooth when changing between Concepts via the nodeChoiceBox
        } else {
            series = nodePerDepthChart.getData().getFirst();
            // map the Charts for better access if I update them when changing between concepts
            Map<String, XYChart.Data<String, Number>> dataMap = new HashMap<>();
            for (XYChart.Data<String, Number> data : series.getData()) {
                dataMap.put(data.getXValue(), data);
            }

            // save relevant depths for current Node -> this is used to distinguish between
            // needed and deprecated bars (i.e. collect which bars should be drawn)
            Set<String> validLabels = new HashSet<>();
            for (int depth : sortedDepths) {
                String depthLabel = String.valueOf(depth + depthFromRoot);
                validLabels.add(depthLabel);

                if (dataMap.containsKey(depthLabel)) {
                    dataMap.get(depthLabel).setYValue(nodesPerDepth.get(depth));

                } else {
                    series.getData().add(new XYChart.Data<>(depthLabel, nodesPerDepth.get(depth)));
                }
            }

            // dismiss bars that aren't part of the selected Concept
            series.getData().removeIf(data -> !validLabels.contains(data.getXValue()));

            // Sort the series list by depth
            series.getData().sort(Comparator.comparingInt(d -> Integer.parseInt(d.getXValue())));
        }
    }

    /**
     * Redraws the Node Degree Distribution tab for the given node.
     *
     * @param selectedItem the TreeItem whose degree distribution should be visualized
     */
    private void redrawNodeDegDistTab(TreeItem<ConceptNode> selectedItem) {
        Map<Integer, Double> fullTreeData;
        if (treeView.getId().equals("treeViewIsA")) {
            fullTreeData = KryoUtils.thawIntegerMapFromKryo("/serializedMaps/isA_NodeDegrees.kryo");
        } else {
            fullTreeData = KryoUtils.thawIntegerMapFromKryo("/serializedMaps/partOf_NodeDegrees.kryo");
        }

        Map<Integer, Double> subTreeData = TreeUtils.computeNormalizedNodeDegreeDistribution(selectedItem);

        List<Integer> allDegrees = new ArrayList<>();
        // fullTree contains always all degrees. Subtree can only have a maxum thta is the same to the fullTree
        int max = Collections.max(fullTreeData.keySet());
        for (int i = 0; i < max + 1; i++) {
            allDegrees.add(i);
        }

        drawHistogramPlot(selectedItem, allDegrees, fullTreeData, subTreeData);
        drawLogLogPlot(selectedItem, allDegrees, fullTreeData, subTreeData);
    }

    private void drawHistogramPlot(TreeItem<ConceptNode> selectedItem,
                                   List<Integer> allDegrees,
                                   Map<Integer, Double> fullTreeData,
                                   Map<Integer, Double> subTreeData) {

        BarChart<String, Number> nodeDegreeHistogram = controller.getNodeDegreeHistogramChart();

        XYChart.Series<String, Number> fullSeries = new XYChart.Series<>();
        fullSeries.setName("Full Tree");

        XYChart.Series<String, Number> subSeries = new XYChart.Series<>();
        subSeries.setName("Subtree");

        for (Integer degree : allDegrees) {
            Double fullCount = fullTreeData.getOrDefault(degree, 0.0);
            Double subCount = subTreeData.getOrDefault(degree, 0.0);
            String degreeLabel = String.valueOf(degree);

            fullSeries.getData().add(new XYChart.Data<>(degreeLabel, fullCount));
            subSeries.getData().add(new XYChart.Data<>(degreeLabel, subCount));
        }

        // For animation again, actualize existing bars or add new ones
        ObservableList<XYChart.Series<String, Number>> existingSeries = nodeDegreeHistogram.getData();

        if (existingSeries.isEmpty()) {
            nodeDegreeHistogram.getData().addAll(fullSeries, subSeries);

        } else {
            XYChart.Series<String, Number> existingSub = existingSeries.get(1);

            Map<String, XYChart.Data<String, Number>> dataMap = new HashMap<>();
            for (XYChart.Data<String, Number> data : existingSub.getData()) {
                dataMap.put(data.getXValue(), data);
            }

            Set<String> validKeys = new HashSet<>();
            for (XYChart.Data<String, Number> data : subSeries.getData()) {
                String x = data.getXValue();
                validKeys.add(x);
                if (dataMap.containsKey(x)) {
                    dataMap.get(x).setYValue(data.getYValue());
                } else {
                    existingSub.getData().add(new XYChart.Data<>(x, data.getYValue()));
                }
            }
            // Remove obsolete points not in current subSeries
            existingSub.getData().removeIf(d -> !validKeys.contains(d.getXValue()));
        }
    }

    private void drawLogLogPlot(TreeItem<ConceptNode> selectedItem,
                                List<Integer> allDegrees,
                                Map<Integer, Double> fullTreeData,
                                Map<Integer, Double> subTreeData) {

        ScatterChart<Number, Number> nodeDegreeScatter = controller.getNodeDegreeLogLogChart();

        XYChart.Series<Number, Number> fullSeries = new XYChart.Series<>();
        fullSeries.setName("Full Tree");

        XYChart.Series<Number, Number> subSeries = new XYChart.Series<>();
        subSeries.setName("Subtree");

        for (Integer k : allDegrees) {
            double pkFull = fullTreeData.getOrDefault(k, 0.0);
            double pkSub = subTreeData.getOrDefault(k, 0.0);

            if (k > 0 && pkFull > 0) {
                fullSeries.getData().add(new XYChart.Data<>(Math.log10(k), Math.log10(pkFull)));
            }
            if (k > 0 && pkSub > 0) {
                subSeries.getData().add(new XYChart.Data<>(Math.log10(k), Math.log10(pkSub)));
            }
        }

        ObservableList<XYChart.Series<Number, Number>> existingSeries = nodeDegreeScatter.getData();

        // Same update pattern and logic like above with the histogram, but changed for scatterplot
        if (existingSeries.isEmpty()) {
            nodeDegreeScatter.getData().addAll(fullSeries, subSeries);
        } else {
            // Only update Subtree series (series index 1)
            XYChart.Series<Number, Number> existingSub = existingSeries.get(1);

            Map<Number, XYChart.Data<Number, Number>> dataMap = new HashMap<>();
            for (XYChart.Data<Number, Number> data : existingSub.getData()) {
                dataMap.put(data.getXValue(), data);
            }

            Set<Number> validKeys = new HashSet<>();
            for (XYChart.Data<Number, Number> data : subSeries.getData()) {
                Number x = data.getXValue();
                validKeys.add(x);
                if (dataMap.containsKey(x)) {
                    dataMap.get(x).setYValue(data.getYValue());
                } else {
                    existingSub.getData().add(new XYChart.Data<>(x, data.getYValue()));
                }
            }
            // Remove obsolete points not in current subSeries
            existingSub.getData().removeIf(d -> !validKeys.contains(d.getXValue()));
        }
    }

    /**
     * Redraws and updates the cladogram visualization for the given node.
     * Clears and replaces the content of the tree pane with the current layout.
     *
     * @param selectedItem the root node of the tree to display
     */
    private void redrawTreeTab(TreeItem<ConceptNode> selectedItem) {
        ConceptNode selectedConcept = selectedItem.getValue();
        StackPane treePane = controller.getTreePane();

        Map<ConceptNode, Point2D> map = Cladogram.layoutUniformEdgeLength(selectedConcept);
        Group group = DrawCladogram.apply(selectedConcept, map, treePane);

        treePane.getChildren().clear();
        treePane.getChildren().add(group);
    }
}
