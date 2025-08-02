package explorer.window.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextArea;

public class LoggerWindowController {

    @FXML
    private Button clearLogButton;

    @FXML
    private Button closeWindowButton;

    @FXML
    private SplitMenuButton exportLogButton;

    @FXML
    private MenuItem exportPreviousSessionMenuItem;

    @FXML
    private TextArea logTextArea;

    public Button getClearLogButton() {
        return clearLogButton;
    }

    public Button getCloseWindowButton() {
        return closeWindowButton;
    }

    public SplitMenuButton getExportLogButton() {
        return exportLogButton;
    }

    public MenuItem getExportPreviousSessionMenuItem() {
        return exportPreviousSessionMenuItem;
    }

    public TextArea getLogTextArea() {
        return logTextArea;
    }
}
