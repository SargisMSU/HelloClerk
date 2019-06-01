package clerk.dialog;

import clerk.controllers.dialog.DateTimePickerController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class DateTimePickerDialog extends Dialog {

    public DateTimePickerDialog(DateTimePickerController.OnDateSetListener listener, long date, boolean isStart) {
        Parent root = null;
        try {
            File file = new File("view/date_time_picker.fxml");
            URL url = file.toURL();
            FXMLLoader loader = new FXMLLoader(url);
            root = loader.load();
            DateTimePickerController controller = loader.getController();
            controller.setModel(listener, date, isStart);
            getDialogPane().setContent(root);
            Window window = getDialogPane().getScene().getWindow();
            window.setOnCloseRequest(event -> window.hide());
            show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}