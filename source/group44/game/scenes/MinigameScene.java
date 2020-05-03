package group44.game.scenes;

import group44.controllers.LevelManager;
import group44.game.Level;
import group44.game.layoutControllers.MiniGameWindowController;
import group44.models.GTimer;
import group44.models.LevelObjectImage;
import group44.models.Profile;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static group44.Constants.WINDOW_HEIGHT;
import static group44.Constants.WINDOW_WIDTH;

public class MinigameScene {
    private final Level currentLevel;
    private final Profile currentProfile;
    private Scene scene;
    private final Stage primaryStage;
    private MiniGameWindowController myController;
    private GTimer sceneTimer = new GTimer();

    private ArrayList<String> userOrder = new ArrayList<>();

    public MinigameScene(Stage primaryStage, Level currentLevel, Profile currentProfile) {
        this.primaryStage = primaryStage;
        this.currentLevel = currentLevel;
        this.currentProfile = currentProfile;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                .getResource("/group44/game/layouts/MiniGameWindow.fxml"));
        try {
            Parent root = fxmlLoader.load();

            // Setting the stage and adding my custom style to it.
            root.getStylesheets().add("group44/resources/application.css");
            root.setId("root");
            scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

            // Loading the controller
            this.myController = fxmlLoader.getController();

            // Adding the key listener to the ingredients VBox.
            myController.getBurgerSelect().addEventFilter(KeyEvent.KEY_PRESSED, event -> ingredientKeyEvent(event));

            // Drawing the game
            primaryStage.setScene(scene);
            primaryStage.show();

            sceneTimer.startTimer(myController.getTimerLabel(), currentLevel.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        primaryStage.setTitle("Kitchen Catastrophe");
    }

    private void ingredientKeyEvent(KeyEvent event) {
        switch (event.getCode()) {
            case ENTER:
                ImageView selectedImageView = (ImageView) scene.focusOwnerProperty().get();
                LevelObjectImage selectedImage = (LevelObjectImage) selectedImageView.getImage();
                String selectedImageLabel = selectedImage.getLabel();
                userOrder.add(selectedImageLabel);
                checkWin();
                break;
        }
    }

    private void checkWin() {
        List<String> correctOrdering = myController.getCorrectOrder();
        int correctMatches = 0;
        //Compare every element in order.
        for(int i = 0; i < userOrder.size(); i++){
            if(userOrder.get(i).equals(correctOrdering.get(i))){
                correctMatches ++;
            }
            else{
                System.out.println("WIPED");
                userOrder.clear();
                myController.placeRandomBurgerParts();
            }
        }
        if (correctMatches == correctOrdering.size()) {
            sceneTimer.pauseTimer();
            this.currentLevel.setTime(GTimer.getCurrentTimeTaken());
            try {
                LevelManager.save(this.currentLevel, this.currentProfile.getId());
            } catch (IOException e) {
                e.printStackTrace();
            }
            new GameScene(primaryStage, currentLevel, currentProfile);
        }
    }
}
