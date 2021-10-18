package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.data.DataGenerator;
import sample.manager.CountryManager;
import sample.utils.MailConnectionUtils;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("ui/reger.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();

        MailConnectionUtils.getInstance().init();
        DataGenerator.init();
        CountryManager.getInstance().init();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
