package clerk;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        try {
            File file = new File("view/main.fxml");
            URL url = file.toURL();
            Parent root = new FXMLLoader(url).load();
            primaryStage.setTitle("Hello Clerk");
            Scene scene = new Scene(root, 900, 500);
            url = new File("view/css/main.css").toURL();
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent t) {
                    Platform.exit();
                    System.exit(0);
                }
            });
            scene.getStylesheets().add(url.toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
        }catch (Exception e){
            e.printStackTrace();
            Files.write(Paths.get("error.txt"), Arrays.toString(e.getStackTrace()).getBytes());
            JOptionPane.showMessageDialog(null, e.getStackTrace()+ "\n");
        }
    }


    public static void main(String[] args) throws IOException {
        launch(args);
    }
}
