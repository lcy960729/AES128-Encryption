package aes;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    DataTypeConverter dataTypeConverter = new DataTypeConverter();
    AES aes = new AES();

    private void init_layout() {
        init_RadioBtn();
        init_textArea();
        init_tableView();
    }

    private ToggleGroup mode_toggleGroup, keyInputType_ToggleGroup, textInputType_ToggleGroup;
    @FXML
    private RadioButton encryption_rb, decryption_rb, keyInputText_rb, keyInputHex_rb, textInputText_rb, textInputHex_rb;

    private void init_RadioBtn() {
        keyInputType_ToggleGroup = new ToggleGroup();
        keyInputText_rb.setToggleGroup(keyInputType_ToggleGroup);
        keyInputHex_rb.setToggleGroup(keyInputType_ToggleGroup);

        textInputType_ToggleGroup = new ToggleGroup();
        textInputText_rb.setToggleGroup(textInputType_ToggleGroup);
        textInputHex_rb.setToggleGroup(textInputType_ToggleGroup);

        mode_toggleGroup = new ToggleGroup();
        encryption_rb.setToggleGroup(mode_toggleGroup);
        encryption_rb.setSelected(true);

        decryption_rb.setToggleGroup(mode_toggleGroup);
        decryption_rb.setSelected(false);
    }

    @FXML
    private TextArea inputText_textArea, outputText_textArea, outputHex_textArea, key_textArea;

    private void init_textArea() {

    }

    @FXML
    private TableView<PrintAES_Result> roundKey_tableView;
    @FXML
    private TableColumn<PrintAES_Result, String> round_col, state_col, roundKey_col;
    private ObservableList<PrintAES_Result> printAES_resultObservableList;

    private void init_tableView() {
        printAES_resultObservableList = roundKey_tableView.getItems();

        round_col.setCellValueFactory(new PropertyValueFactory<>("round"));
        state_col.setCellValueFactory(new PropertyValueFactory<>("state"));
        roundKey_col.setCellValueFactory(new PropertyValueFactory<>("roundKey"));
    }

    @FXML
    private void run_btnClick(){
        int mode = encryption_rb.isSelected() ? 0 : 1;

        if (mode == 0){
            encryption();
        }else {
            decryption();
        }

        printAES_resultObservableList.clear();
        printAES_resultObservableList.addAll(aes.getPrintAES_results());

        roundKey_tableView.refresh();
        aes.clearPrintAES_results();
    }

    private void errorMsg(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Key Size is 128bit");
        alert.setContentText("Enter 16 hex values or 32 characters");
        alert.show();

        outputHex_textArea.setText(null);
        outputText_textArea.setText(null);
    }

    private void encryption(){
        String text = inputText_textArea.getText();
        String key = key_textArea.getText();

        //0 is Text, 1 is Hex
        int keyInputType = keyInputText_rb.isSelected() ? 0 : 1;
        int textInputType = textInputText_rb.isSelected() ? 0 : 1;

        if (!check_keySize(keyInputType, key))
            return;

        String result_hex = aes.run_Encryption_Or_Decryption(0, textInputType, text, keyInputType, key);
        outputHex_textArea.setText(result_hex);

        String result_text = dataTypeConverter.hexStringToString(result_hex);
        outputText_textArea.setText(result_text);
    }

    //0 is Text, 1 is Hex
    private void decryption(){
        String text = inputText_textArea.getText();
        String key = key_textArea.getText();

        //0 is Text, 1 is Hex
        int keyInputType = keyInputText_rb.isSelected() ? 0 : 1;
        int textInputType = textInputText_rb.isSelected() ? 0 : 1;

        check_keySize(keyInputType, key);

        String result_hex = aes.run_Encryption_Or_Decryption(1, textInputType, text, keyInputType, key);
        outputHex_textArea.setText(result_hex);

        String result_text = dataTypeConverter.hexStringToString(result_hex);
        outputText_textArea.setText(result_text);
    }

    private boolean check_keySize(int keyInputType, String key){
        if (keyInputType == 1 && key.replaceAll(" ", "").length() != 32){
            errorMsg();
            return false;
        }else if (keyInputType == 0 && key.length() != 16){
            errorMsg();
            return false;
        }

        return true;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        init_layout();
    }
}
