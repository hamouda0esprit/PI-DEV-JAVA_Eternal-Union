package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import service.CodeExecutionService;

public class CodeEditorController {
    @FXML
    private TextArea codeEditor;
    
    @FXML
    private TextArea outputArea;
    
    @FXML
    private Button runButton;
    
    private final CodeExecutionService codeService = new CodeExecutionService();
    
    @FXML
    public void initialize() {
        // Set up the run button action
        runButton.setOnAction(event -> runCode());
        
        // Set up initial code template
        codeEditor.setText("// Write your Java code here\n" +
                          "System.out.println(\"Hello, World!\");");
    }
    
    private void runCode() {
        String code = codeEditor.getText();
        String result = codeService.compileAndRun(code);
        outputArea.setText(result);
    }
} 