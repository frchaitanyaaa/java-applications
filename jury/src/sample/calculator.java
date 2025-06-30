package sample;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class calculator extends Application {
    public void start(Stage stage) {
       
        Label title = new Label("Welcome to Calculator");

        TextField tf1 = new TextField();
        tf1.setPromptText("Enter 1st number");

        TextField tf2 = new TextField();
        tf2.setPromptText("Enter 2nd number");

    
        Button add = new Button("+");
        Button minus = new Button("-");
        Button multiply = new Button("*");

      
        Label result = new Label();

        
        add.setOnAction(e -> {
            try {
                double num1 = Double.parseDouble(tf1.getText());
                double num2 = Double.parseDouble(tf2.getText());
                double sum = num1 + num2;
                result.setText("Result: " + sum);
            } catch (NumberFormatException ex) {
                result.setText("Please enter valid numbers.");
            }
           
        });
        minus.setOnAction(e -> {
            try {
                double num1 = Double.parseDouble(tf1.getText());
                double num2 = Double.parseDouble(tf2.getText());
                double sum1 = num1-num2;
                result.setText("Result: " + sum1);
            } catch (NumberFormatException ex) {
                result.setText("Please enter valid numbers.");
            }
        });
        multiply.setOnAction(e -> {
            try {
                double num1 = Double.parseDouble(tf1.getText());
                double num2 = Double.parseDouble(tf2.getText());
                double sum2 = num1*num2;
                result.setText("Result: " + sum2);
            } catch (NumberFormatException ex) {
                result.setText("Please enter valid numbers.");
            }
        });

       
        VBox root = new VBox(10);
        root.getChildren().addAll(title, tf1, tf2, add, minus,  multiply, result);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20;");

        
        Scene scene = new Scene(root, 250, 500);
        stage.setTitle("Simple Calculator");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
