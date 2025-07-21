package explorer.window.controller;

import explorer.model.treetools.ConceptNode;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.StackPane;

public class ConceptInfoDialogController {

    @FXML
    private StackedBarChart<String, Number> NodeDegreeStackBarChart;

    @FXML
    private Label NumberOfMeshesLabel;

    @FXML
    private Label NumberOfSiblingsLabel;

    @FXML
    private Button closeNodeInfoButton;

    @FXML
    private Label leavesBelowLabel;

    @FXML
    private BarChart<String, Number> conceptsPerDepthBarChart;

    @FXML
    private Label depthFromRootLabel;

    @FXML
    private ChoiceBox<TreeItem<ConceptNode>> nodeChoiceBox;

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

    public ChoiceBox<TreeItem<ConceptNode>> getNodeChoiceBox() {
        return nodeChoiceBox;
    }

    public Button getCloseNodeInfoButton() {
        return closeNodeInfoButton;
    }

    public StackedBarChart<String, Number> getNodeDegreeStackBarChart() {
        return NodeDegreeStackBarChart;
    }

    public Label getNumberOfMeshesLabel() {
        return NumberOfMeshesLabel;
    }

    public Label getNumberOfSiblingsLabel() {
        return NumberOfSiblingsLabel;
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
        nodeChoiceBox.setVisible(false);
    }
}
