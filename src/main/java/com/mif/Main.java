package com.mif;

import com.mif.rm.RealMachine;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.InputStream;

public class Main extends Application {

    String params;



    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("REALMACHINE");

        TextField paramsField = new TextField("PRESS THE B00T");
        paramsField.setEditable(false);

        Button startBtn = getStartButton();
        startBtn.setOnAction(event -> {
            this.params = paramsField.getText();
            bootRealMachine(primaryStage);
        });

        HBox inputHbox = new HBox(paramsField);
        HBox buttonHbox = new HBox(startBtn);

        inputHbox.setAlignment(Pos.CENTER);
        buttonHbox.setAlignment(Pos.CENTER);

        VBox vBox = new VBox(inputHbox, buttonHbox);

        vBox.setMargin(inputHbox, new Insets(5, 5, 5, 5));
        vBox.setMargin(buttonHbox, new Insets(5, 5, 5, 5));

        Scene startScene = new Scene(vBox, 200, 200);

        primaryStage.setScene(startScene);
        primaryStage.show();
    }

    private Button getStartButton() {
        String btnText = "BOOT UP MACHINE";
        Button button = new Button(btnText);
        button.setPrefSize(50, 50);

        InputStream inputStream = getClass().getResourceAsStream("/images/b00t.png");

        if (inputStream == null)
            return button;

        Image image = new Image(inputStream);
        ImageView imageView = new ImageView(image);

        button = new Button(btnText, imageView);
        button.setPrefSize(50, 50);
        button.setStyle("-fx-content-display: bottom;");

        return button;
    }

    private void bootRealMachine(Stage primaryStage) {
        RealMachine rm = new RealMachine(primaryStage, this.params);
//        rm.run(primaryStage);
    }
}
