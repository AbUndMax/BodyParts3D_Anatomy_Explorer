package explorer.window.controller;

import explorer.model.treetools.ConceptNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.Chart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.StackPane;

public class ConceptInfoDialogController {

    @FXML
    private BarChart<String, Number> nodeDegreeHistogramChart;

    @FXML
    private ScatterChart<Number, Number> nodeDegreeLogLogChart;

    @FXML
    private ChoiceBox<ChartWrapper> plotChoiceBox;

    @FXML
    private Label plotTitle;

    @FXML
    private Label numberOfMeshesLabel;

    @FXML
    private Label numberOfSiblingsLabel;

    @FXML
    private Button closeNodeInfoButton;

    @FXML
    private Label leavesBelowLabel;

    @FXML
    private BarChart<String, Number> conceptsPerDepthBarChart;

    @FXML
    private Label depthFromRootLabel;

    @FXML
    private ChoiceBox<TreeItem<ConceptNode>> conceptChoiceBox;

    @FXML
    private Label numberLeavesLabel;

    @FXML
    private Label numberOfChildsLabel;

    @FXML
    private Label parentConceptLabel;

    @FXML
    private Label selectedConceptLabel;

    @FXML
    private PieChart subtreeCoveragePieChart;

    @FXML
    private Label subtreeHeightLabel;

    @FXML
    private Label subtreeSizeLabel;

    @FXML
    private StackPane treePane;

    public StackPane getTreePane() {
        return treePane;
    }

    public ChoiceBox<TreeItem<ConceptNode>> getConceptChoiceBox() {
        return conceptChoiceBox;
    }

    public Button getCloseNodeInfoButton() {
        return closeNodeInfoButton;
    }

    public BarChart<String, Number> getNodeDegreeHistogramChart() {
        return nodeDegreeHistogramChart;
    }

    public Label getNumberOfMeshesLabel() {
        return numberOfMeshesLabel;
    }

    public Label getNumberOfSiblingsLabel() {
        return numberOfSiblingsLabel;
    }

    public Label getLeavesBelowLabel() {
        return leavesBelowLabel;
    }

    public BarChart<String, Number> getConceptsPerDepthBarChart() {
        return conceptsPerDepthBarChart;
    }

    public Label getDepthFromRootLabel() {
        return depthFromRootLabel;
    }

    public Label getNumberLeavesLabel() {
        return numberLeavesLabel;
    }

    public Label getNumberOfChildsLabel() {
        return numberOfChildsLabel;
    }

    public Label getParentConceptLabel() {
        return parentConceptLabel;
    }

    public Label getSelectedConceptLabel() {
        return selectedConceptLabel;
    }

    public PieChart getSubtreeCoveragePieChart() {
        return subtreeCoveragePieChart;
    }

    public Label getSubtreeHeightLabel() {
        return subtreeHeightLabel;
    }

    public Label getSubtreeSizeLabel() {
        return subtreeSizeLabel;
    }

    public void initialize() {
        setupPlotChoiceBox();
    }

    public ScatterChart<Number, Number> getNodeDegreeLogLogChart() {
        return nodeDegreeLogLogChart;
    }

    private void setupPlotChoiceBox() {
        ObservableList<ChartWrapper> chartOptions = FXCollections.observableArrayList();
        chartOptions.add(new ChartWrapper(nodeDegreeHistogramChart,
                                          "Subtree vs. Full Relation: Relative Node Degree Distribution",
                                          "Relative Histogram"));
        chartOptions.add(new ChartWrapper(nodeDegreeLogLogChart,
                                          "Subtree vs. Full Relation: log-log Node Degree Distribution",
                                          "Log-Log Scatter"));
        plotChoiceBox.setItems(chartOptions);

        plotChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.chart.setVisible(false);
                oldValue.chart.setManaged(false);
            }
            newValue.chart.setVisible(true);
            newValue.chart.setManaged(true);
            plotTitle.setText(newValue.title);
        });

        plotChoiceBox.setValue(chartOptions.getFirst());
    }

    private record ChartWrapper(Chart chart, String title, String choiceBoxString) {
        @Override
        public String toString() {
            return choiceBoxString;
        }
    }
}
