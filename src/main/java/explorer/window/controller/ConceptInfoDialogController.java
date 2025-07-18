package explorer.window.controller;

import explorer.model.treetools.ConceptNode;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.StackPane;

public class ConceptInfoDialogController {

    @FXML
    private StackPane treePane;

    @FXML
    private ChoiceBox<ConceptNode> nodeChoiceBox;

    @FXML
    private Button closeNodeInfoButton;

    public StackPane getTreePane() {
        return treePane;
    }

    public ChoiceBox<ConceptNode> getNodeChoiceBox() {
        return nodeChoiceBox;
    }

    public Button getCloseNodeInfoButton() {
        return closeNodeInfoButton;
    }

    public void initialize() {
        nodeChoiceBox.setVisible(false);
    }
}
