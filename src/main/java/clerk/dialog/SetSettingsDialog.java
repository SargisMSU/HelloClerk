package clerk.dialog;

import clerk.controllers.dialog.SetSettingsController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.prefs.Preferences;

public class SetSettingsDialog extends Dialog {

    public SetSettingsDialog(){

        Parent root = null;
        try {
            Preferences prefs = Preferences.userRoot().node("clerk");
            String gmail = prefs.get("email", "");
            String password = prefs.get("password", "");
            int start = prefs.getInt("start", 10);
            int period = prefs.getInt("period", 5);
            Boolean autoConflicts = prefs.getBoolean("auto", false);

            File file = new File("view/settings.fxml");
            URL url = file.toURL();
            FXMLLoader loader = new FXMLLoader(url);
            root = loader.load();
            SetSettingsController controller = loader.getController();
            Window window = getDialogPane().getScene().getWindow();
            window.setOnCloseRequest(ev -> window.hide());
            controller.setModel(gmail, password, start, period, autoConflicts);
            getDialogPane().setContent(root);
            setTitle("Настройки");
            show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
