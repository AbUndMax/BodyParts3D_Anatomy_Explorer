package explorer.window.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class LoggerWindowController {

    @FXML
    private Button clearLogButton;

    @FXML
    private Button closeWindowButton;

    @FXML
    private Button exportLogButton;

    @FXML
    private TextArea logTextArea;

    public Button getClearLogButton() {
        return clearLogButton;
    }

    public Button getCloseWindowButton() {
        return closeWindowButton;
    }

    public Button getExportLogButton() {
        return exportLogButton;
    }

    public TextArea getLogTextArea() {
        return logTextArea;
    }
}
