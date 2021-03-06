package group44.game.scenes;

import group44.Constants;
import group44.controllers.AudioManager;
import group44.controllers.Leaderboard;
import group44.controllers.LevelManager;
import group44.entities.collectableItems.CollectableItem;
import group44.entities.collectableItems.TokenAccumulator;
import group44.exceptions.CollisionException;
import group44.exceptions.ParsingException;
import group44.game.Level;
import group44.game.LevelFinishStatus;
import group44.game.layoutControllers.MainGameWindowController;
import group44.models.GTimer;
import group44.models.Profile;
import group44.models.Record;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import static group44.Constants.WINDOW_HEIGHT;
import static group44.Constants.WINDOW_WIDTH;

/**
 * This class displays the scene where the game happens. It sets up the timer,
 * calls the configuration of the user input, sets up the menu and draws the
 * game.
 *
 * @author Mihai, Tomas Svejnoha
 * @version 1.0
 */
public class GameScene {

    // The canvas in the GUI. This needs to be a global variable
    // (in this setup) as we need to access it in different methods.
    private Canvas canvas;

    // The controller associated with the specific fxml file and specific GameScene.
    private static MainGameWindowController myController;

    private boolean canMove = true;

    // The window itself.
    private static Stage primaryStage;
    // Current level displayed.
    private static Level currentLevel;
    // Current player.
    private static Profile currentProfile;
    // Clock
    private static GTimer timer = new GTimer();

    /**
     * This is the main method that loads everything required to draw the scene.
     *
     * @param primaryStage   represents the window where the stages are displayed.
     * @param currentLevel   current level the user is playing.
     * @param currentProfile the current user profile to use.
     */
    public GameScene(Stage primaryStage, Level currentLevel,
                     Profile currentProfile) {
        AudioManager.playGameMusic();
        GameScene.primaryStage = primaryStage;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                .getResource("/group44/game/layouts/MainGameWindow.fxml"));
        try {
            Parent root = fxmlLoader.load();
            // Setting the stage and adding my custom style to it.
            root.getStylesheets().add("group44/resources/application.css");
            root.setId("root");
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            // Loading the controller
            MainGameWindowController tempController = fxmlLoader
                    .getController();
            setController(tempController);

            GameScene.currentLevel = currentLevel;
            // Setting the canvas
            setCanvas(myController.getCanvas());
            // Adding the key listener to the scene.
            scene.addEventFilter(KeyEvent.KEY_PRESSED,
                    event -> processKeyEvent(event));
            // Drawing the game
            drawGame();
            primaryStage.setScene(scene);
            primaryStage.show();
            updateInventory();
            GameScene.currentProfile = currentProfile;
            timer.startTimer(myController.getTimeLabel(),
                    currentLevel.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        primaryStage.setTitle("Kitchen Catastrophe");
    }

    /**
     * Launches the minigame and maintains the game state.
     */
    public static void launchMinigame() {
        timer.pauseTimer();
        currentLevel.setTime(GTimer.getCurrentTimeTaken());
        try {
            LevelManager.save(currentLevel, currentProfile.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        new MinigameScene(primaryStage, currentLevel, currentProfile);
    }

    /**
     * Adding the listeners to the menu buttons. It also makes the player unable
     * to move while the menu is closed. Here the time of the player needs to be
     * stopped as well.
     */
    private void setUpMenu() {
        this.canMove = false;
        timer.pauseTimer();
        myController.getResumeButton().setOnMouseClicked(this::setUpResume);
        myController.getRestartButton().setOnMouseClicked(this::setUpRestart);
        myController.getHomeButton().setOnMouseClicked(this::setUpHome);
        AudioManager.setGameVolume(0.0);
    }

    /**
     * Defining behaviour for the click on the resume button.Resumes the game
     * state and the time.
     *
     * @param event This is the event for the click on the resume button.
     */
    private void setUpResume(MouseEvent event) {
        timer.resumeTimer();
        myController.getMenuBox()
                .setVisible(!myController.getMenuBox().isVisible());
        this.canMove = true;
    }

    /**
     * Defining behaviour for the click on the restart button.Restarts the game
     * and the time.
     *
     * @param event This is the event for the click on the restart button.
     */
    private void setUpRestart(MouseEvent event) {
        timer.startTimer(myController.getTimeLabel(), 0);

        // Delete all temp files
        LevelManager.deleteTempData(currentLevel.getId(),
                currentProfile.getId());

        Level newLevel = null;
        try {
            newLevel = LevelManager.load(currentLevel.getId()); // TODO:
            // @Bogdan
            // Mihai
            // -
            // TESTING
        } catch (FileNotFoundException | ParsingException | CollisionException e) {
            e.printStackTrace();
        }
        new GameScene(primaryStage, newLevel, currentProfile);
    }

    /**
     * Defining behaviour for the click on the home button.Sends the player to
     * the home screen.
     *
     * @param event This is the event for the click on the restart button.
     */
    private void setUpHome(MouseEvent event) {
        timer.pauseTimer();
        try {
            currentLevel.setTime(GTimer.getCurrentTimeTaken());
            LevelManager.save(currentLevel, currentProfile.getId());
        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Saving current progress failed.");
            alert.setContentText("Please play the level again.");
            alert.showAndWait();
        }
        new MainMenuScene(primaryStage);
    }

    /**
     * This method is called when the game has ended. It shows the top 3 times
     * and your time.
     */
    private void showTimes(LevelFinishStatus status) {
        timer.stopTimer();
        ButtonType levelSelector = new ButtonType("Level Selector",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType mainMenu = new ButtonType("Main Menu",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType restart = new ButtonType("Try Again",
                ButtonBar.ButtonData.OK_DONE);
        Alert a1 = new Alert(AlertType.NONE, "default Dialog", levelSelector,
                mainMenu, restart);
        a1.setHeight(400);
        a1.setWidth(500);


        if (status == LevelFinishStatus.GoalReached) {
            //Turn the volume down for the ending
            AudioManager.setGameVolume(0.05);
            //Play winning sound
            AudioManager.playSound(Constants.WIN_SOUND);
            myController.getOnScreenMessage().setTextFill(Paint.valueOf("green"));
            myController.getOnScreenMessage().textProperty().setValue("You've completed the level!" + "\n" + "WELL DONE!!");
            myController.getOnScreenMessage().setVisible(true);
            a1.setTitle("Congrats on finishing the level!");
            Leaderboard.addOrUpdate(currentProfile.getId(),
                    currentLevel.getId(), GTimer.getCurrentTimeTaken());
            ObservableList<Record> top3Records = Leaderboard
                    .getTopThreeRecords(currentLevel.getId());

            // Is in TOP 3?
            boolean isInTop3 = Leaderboard.isInTopThreeRecords(
                    currentProfile.getId(), currentLevel.getId());

            StringBuilder builder = new StringBuilder();
            for (Record record : top3Records) {
                builder.append(record.toString() + "\n");
            }

            a1.setContentText("Top times and your time: \n");
            if (isInTop3) {
                a1.setContentText(
                        "Top times and your time: \n" + builder.toString());
            } else {
                a1.setContentText("Top times and your time: \n"
                        + builder.toString() + Leaderboard.getRecord(
                        currentProfile.getId(), currentLevel.getId()));
            }
        } else {
            //Stop game music
            AudioManager.pauseGameMusic();
            //Play sad trombone death music
            AudioManager.playSound(Constants.DIED_MUSIC);
            //Display death message
            myController.getOnScreenMessage().textProperty().setValue("YOU COULDN'T HANDLE THIS KITCHEN! Rest In Peace");
            myController.getOnScreenMessage().setVisible(true);
            //Display post-death menu
            a1.setTitle("And then I took an arrow to the knee!");
            a1.setContentText(
                    "Just a suggestion: \n Practice makes it perfect! \n");
        }
        canMove = false;
        Optional<ButtonType> result = a1.showAndWait();
        if (!result.isPresent()) {

        } else {
            if (result.get() == levelSelector) {
                LevelSelectorScene ls = new LevelSelectorScene(primaryStage,
                        currentProfile);
            } else {
                if (result.get() == mainMenu) {
                    MainMenuScene ms = new MainMenuScene(primaryStage);
                } else {
                    if (result.get() == restart) {
                        setUpRestart(new MouseEvent(null, 1, 1, 1, 1, null, 1,
                                canMove, canMove, canMove, canMove, canMove,
                                canMove, canMove, canMove, canMove, canMove,
                                null));
                    }
                }
            }
        }
    }

    /**
     * This method gets the onScreenMessage label which is displayed in the UI.
     *
     * @return the onScreenMessage JavaFX label
     */
    public static Label getOnScreenMessage() {
        return myController.getOnScreenMessage();
    }

    /**
     * Updates the on screen message label and its corresponding picture.
     *
     * @param message   The label text
     * @param imagePath An imagepath to have an image below the label text
     */
    public static void setOnScreenMessage(String message, String imagePath) {
        ImageView iconImage = new ImageView(new File(imagePath).toURI().toString());
        iconImage.setFitWidth(50);
        iconImage.setFitHeight(50);
        iconImage.setPreserveRatio(true);
        myController.getOnScreenMessage().setGraphic(iconImage);
        myController.getOnScreenMessage().setContentDisplay(ContentDisplay.BOTTOM);
        myController.getOnScreenMessage().textProperty().setValue(message);
    }

    /**
     * Updates the Players inventory, used when collecting an item
     * in the {@link group44.entities.movableObjects.Player} class.
     */
    private static void loadInventory() {
        myController.clearInventory();
        for (CollectableItem item : currentLevel.getPlayer().getInventory()) {
            if (item instanceof TokenAccumulator) {
                updateTokens(((TokenAccumulator) item).getTokensCount());
            } else {
                myController.addInventoryIcon(item.getImageURL());
            }
        }
    }

    /**
     * Adds a new item to the graphical inventory.
     * @param item The item to be added.
     */
    public static void updateInventory(CollectableItem item){
        myController.addInventoryIcon(item.getImageURL());
    }

    /**
     * This method sets the globally available controller to the current
     * controller.
     *
     * @param tempController The current controller.
     */
    private void setController(MainGameWindowController tempController) {
        myController = tempController;
    }

    /**
     * This method sets the globally available canvas to the current canvas.
     *
     * @param canvas The current canvas.
     */
    private void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    /**
     * This method draws every non movable object onto the screen.
     */
    private void drawGame() {
        // Get the Graphic Context of the canvas. This is what we draw
        // on.
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Clear canvas
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        currentLevel.draw(gc);
    }

    /**
     * This method updates the token amount shown in the GameScene
     *
     * @param tokens the number of tokens to update to.
     */
    public static void updateTokens(int tokens) {
        myController.getTokenAmount().setText(String.valueOf(tokens));
    }

    /**
     * This method should be called when the game has ended. The player time
     * should be sent as a parameter to store it. Then an alert with the top 3
     * times and the player time will show.
     */
    private void endGame() {
        timer.pauseTimer();
        LevelManager.deleteTempData(currentLevel.getId(),
                currentProfile.getId());
        this.showTimes(currentLevel.getFinishStatus());
    }

    /**
     * This method handles the keyboard input.
     *
     * @param event Passes in the events from the keyboard.
     */
    private void processKeyEvent(KeyEvent event) {

        switch (event.getCode()) {
            case ESCAPE: {
                if (canMove) {
                    canMove = false;
                    timer.pauseTimer();
                    // Escape key was pressed. So show the
                    // menu.
                    myController.getMenuBox()
                            .setVisible(!myController.getMenuBox().isVisible());
                    // Setting up the menu controls.
                    setUpMenu();
                } else {
                    timer.resumeTimer();
                    myController.getMenuBox()
                            .setVisible(!myController.getMenuBox().isVisible());
                    canMove = true;
                    AudioManager.playGameMusic();
                }
                break;
            }

            // All keys going to the level
            case UP:
            case DOWN:
            case LEFT:
            case RIGHT:
                if (canMove) {
                    currentLevel.keyDown(event);
                }
                break;

            default:
                // Do nothing
                break;
        }
        // Redraw game as the player may have moved.
        drawGame();
        // Consume the event. This means we mark it as dealt
        // with. This stops other GUI nodes (buttons etc) responding to it.
        event.consume();

        if (currentLevel.isFinished()) {
            this.endGame();
        }
    }
}
