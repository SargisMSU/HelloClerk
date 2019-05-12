package clerk.controllers.dialog;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.prefs.Preferences;

public class SetSettingsController {

    @FXML
    Button btnOk, btnCancell;
    @FXML
    TextField textFieldGmail, textFieldStart, textFieldPeriod;
    @FXML
    PasswordField passwordField;
    @FXML
    CheckBox autoConflictsCheckBox;

    public void setModel(String gmail, String password, Integer start, Integer period, Boolean auto) {
        textFieldGmail.setText(gmail);
        passwordField.setText(password);
        textFieldStart.setText(start.toString());
        textFieldPeriod.setText(period.toString());
        autoConflictsCheckBox.setSelected(auto);
    }

    public void onOkClicked(){
        Preferences prefs = Preferences.userRoot().node("clerk");
        prefs.put("email", textFieldGmail.getText().trim());
        prefs.put("password", passwordField.getText().trim());
        prefs.putInt("start", Integer.parseInt(textFieldStart.getText().trim()));
        prefs.putInt("period", Integer.parseInt(textFieldPeriod.getText().trim()));
        prefs.putBoolean("auto", autoConflictsCheckBox.isSelected());
        btnOk.getScene().getWindow().hide();
    }

    public void onCancellClicked(){
        btnOk.getScene().getWindow().hide();
    }
}
