package explorer.window.presenter;

import explorer.model.Cladogram;
import explorer.model.treetools.ConceptNode;
import explorer.model.treetools.TreeUtils;
import explorer.window.GuiRegistry;
import explorer.window.controller.ConceptInfoDialogController;
import explorer.window.vistools.DrawCladogram;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.util.*;

/**
 * Presenter class for the NodeInfo view.
 * Manages user interactions and visual updates of the cladogram based on selected nodes.
 */
public class ConceptInfoDialogPresenter {

    private final ConceptInfoDialogController controller;
    private final TreeItem<ConceptNode> treeViewRoot;

    /**
     * Creates a new presenter for the NodeInfo view.
     *
     * @param selectedItems the currently selected tree items
     * @param controller the controller managing the view
     * @param registry the global GUI registry (unused here, but available for future use)
     */
    public ConceptInfoDialogPresenter(ObservableList<TreeItem<ConceptNode>> selectedItems,
                                      TreeItem<ConceptNode> treeViewRoot,
                                      ConceptInfoDialogController controller) {

        this.controller = controller;
        this.treeViewRoot = treeViewRoot;

        // get the selectedItem
        TreeItem<ConceptNode> selectedItem = selectedItems.getFirst();

        // if there are multiple selected nodes,
        // setup a choiceBox that allows to switch between the multiple selected nodes
        if (selectedItems.size() > 1) {
            ChoiceBox<TreeItem<ConceptNode>> nodeChoiceBox = controller.getNodeChoiceBox();
            setupNodeChoiceBox(nodeChoiceBox, selectedItems);
            selectedItem = nodeChoiceBox.getValue();
        }

        // draw the Tabs
        redrawCharacteristicsTab(selectedItem);
        redrawNodeDegDistTab(selectedItem);
        redrawTreeTab(selectedItem);
    }

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
        int totalLeaves = TreeUtils.numberOfLeaves(treeViewRoot.getValue());
        double percentage = ((double) leavesInSubtree / totalLeaves) * 100;
        controller.getLeavesBelowLabel().setText(String.format(percentage == 100 ? "%.0f %%" : "%.2f %%", percentage));


        // setup of subtree coverage pieChart:
        drawCoveragePie(subTreeSize);
        drawNodePerDepthPlot(selectedItem, depthFromRoot);

    }

    private void drawCoveragePie(int subTreeSize) {
        int totalTreeSize = TreeUtils.calculateTreeSize(treeViewRoot);
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

    //TODO
    private void redrawNodeDegDistTab(TreeItem<ConceptNode> selectedItem) {
        
    }

    /**
     * Redraws the cladogram visualization for the given node.
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
