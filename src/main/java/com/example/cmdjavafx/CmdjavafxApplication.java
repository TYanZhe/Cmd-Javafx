package com.example.cmdjavafx;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.MalformedURLException;

@SpringBootApplication
public class CmdjavafxApplication extends Application {

    private static CmdjavafxApplication instance;
    public static CmdjavafxApplication getInstance() {
        return instance;
    }

    private Stage primaryStage;

    public static void main(String[] args) {
        launch(CmdjavafxApplication.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        setStageApp(primaryStage, "/fxml/cmd.fxml");
    }

    private void setStageApp(Stage primaryStage, String locationFxml) throws IOException {
        instance = this;
        this.primaryStage = primaryStage;
        this.primaryStage.getIcons().add(new Image("/img/icon.png"));
        Parent root = FXMLLoader.load(getClass().getResource(locationFxml));
        this.primaryStage.setScene(new Scene(root));
        this.primaryStage.setResizable(false);
        this.primaryStage.show();
    }

    public void updateTitle(String title) {
        primaryStage.setTitle(title);
    }
}