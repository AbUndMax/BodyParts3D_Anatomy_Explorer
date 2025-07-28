package explorer.window.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class DebugWindowController {
    @FXML
    private ListView<String> SourceOfTruthList;

    @FXML
    private ListView<String> isAList;

    @FXML
    private ListView<String> partOfList;

    public ListView<String> getSourceOfTruthList() {
        return SourceOfTruthList;
    }

    public ListView<String> getIsAList() {
        return isAList;
    }

    public ListView<String> getPartOfList() {
        return partOfList;
    }
}
