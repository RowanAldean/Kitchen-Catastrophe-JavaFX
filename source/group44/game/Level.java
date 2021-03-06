package group44.game;

import java.util.ArrayList;

import group44.entities.cells.Cell;
import group44.entities.cells.StepableCell;
import group44.entities.movableObjects.Enemy;
import group44.entities.movableObjects.MovableObject;
import group44.entities.movableObjects.Player;
import group44.exceptions.CollisionException;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;

/**
 * Level maintains all data and event handling in the game.
 *
 * @author Tomas Svejnoha
 * @version 1.0
 */
public class Level {
    /** Error message pattern when display size is invalid. */
    private static final String ERROR_DISPLAY_SIZE_ILLEGAL_ARGUMENT_EXCEPTION = "The displaySize must be odd and >= 3.";
    /** Error message pattern when collision occurs. */
    private static final String ERROR_COLLISION_EXCEPTION = "Unable to place %s in the grid [%d][%d].";
    /** The minimum display size allowed. */
    private static final int MIN_DISPLAY_SIZE = 3;

    /** Level id. */
    private int id;
    /** Custom level */
    private boolean custom;
    /** 2D game array. */
    private Cell[][] grid; // The 2D game array
    /** How many cells on X and Y display. */
    private int displaySize; // The size of the grid displayed
    /** The game player. */
    private Player player;
    /** The game enemies. */
    private volatile ArrayList<Enemy> enemies;
    /** Indicates whether the level is finished or not. */
    private boolean isFinished;
    /** Indicates under which circumstances the level finished. */
    private LevelFinishStatus finishStatus;
    /** The time taken by the player. */
    private long time;

    /**
     * Creates a new instance of {@link Level}.
     *
     * @param id
     *            the Id of the level
     * @param gridWidth
     *            width of the 2D array
     * @param gridHeight
     *            height of the 2D array
     * @param displaySize
     *            size of the grid displayed on screen
     * @param time
     *            time taken to play the level
     * @throws IllegalArgumentException
     *             If the display size is less than 3, is not odd, or exceeds a
     *             size of a grid.
     */
    public Level(int id, boolean isItCustom, int gridWidth, int gridHeight, int displaySize,
            int time) {
        this.id = id;
        this.custom = isItCustom;
        this.grid = new Cell[gridWidth][gridHeight];
        if (displaySize < MIN_DISPLAY_SIZE || displaySize > gridWidth
                || displaySize > gridHeight || displaySize % 2 != 1) {
            throw new IllegalArgumentException(
                    Level.ERROR_DISPLAY_SIZE_ILLEGAL_ARGUMENT_EXCEPTION);
        } else {
            this.displaySize = displaySize;
        }
        this.time = time;
        this.enemies = new ArrayList<>();
    }

    /**
     * Returns the time taken by the player.
     *
     * @return the time taken.
     */
    public long getTime() {
        return time;
    }

    /**
     * Sets the time taken by the player.
     *
     * @param time
     *            the time taken.
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * Indicates whether the player finished the level.
     *
     * @return true if finished; otherwise false.
     */
    public boolean isFinished() {
        return this.isFinished;
    }

    /**
     * Returns the status under which the level finished.
     *
     * @return the finish status.
     */
    public LevelFinishStatus getFinishStatus() {
        return this.finishStatus;
    }

    /**
     * Returns the Id of the {@link Level}.
     *
     * @return the level id
     */
    public int getId() {
        return this.id;
    }

    /**
     * Sets the Id of the {@link Level}.
     *
     * @param id
     *            the new id.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns a 2D array with all {@link Cell} in the {@link Level}.
     *
     * @return 2D array of {@link Cell}s
     */
    public Cell[][] getGrid() {
        /*
         * The only class we will possibly use this method is the
         * SmartTargetingEnemy. Using some kind of repository does not make
         * sence as the SmartTargetingEnemy should decide on its own.
         */
        return this.grid;
    }

    /**
     * Returns a width of the game.
     *
     * @return width of the game
     */
    public int getGridWidth() {
        return this.grid.length;
    }

    /**
     * Returns a height of the game.
     *
     * @return height of the game
     */
    public int getGridHeight() {
        return this.grid[0].length;
    }

    /**
     * Adds {@link Cell} in the grid to the specific location.
     *
     * @param x
     *            position X of the {@link Cell}
     * @param y
     *            position Y of the {@link Cell}
     * @param cell
     *            the {@link Cell} to place in the grid
     * @throws CollisionException
     *             when trying to rewrite existing cell in the grid
     */
    public void addCell(int x, int y, Cell cell) throws CollisionException {
        if (this.grid[x][y] != null) {
            throw new CollisionException(String.format(
                    Level.ERROR_COLLISION_EXCEPTION, cell.getTitle(), x, y));
        }

        this.grid[x][y] = cell;
        if (cell instanceof StepableCell) {
            StepableCell stepableCell = ((StepableCell) cell);
            MovableObject object = stepableCell.getMovableObject();
            if (object instanceof Player) {
                this.player = (Player) object;
            }
            if (object instanceof Enemy) {
                this.enemies.add((Enemy) object);
            }
        }
    }

    /**
     * Returns the instance of {@link Player} in the game.
     *
     * @return the player.
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Returns all enemies in the game.
     *
     * @return list of enemies.
     */
    public ArrayList<Enemy> getEnemies() {
        return this.enemies;
    }

    /**
     * Returns a current position of the {@link Player}.
     *
     * @return the {@link Player}'s position
     */
    public Position getPlayerPosition() {
        return new Position(this.player.getPositionX(),
                this.player.getPositionY());
    }

    /**
     * Draws the cell in the active game area.
     *
     * @param gc
     *            {@link GraphicsContext} to which the game is drawn
     */
    public void draw(GraphicsContext gc) {
        double cellWidth = gc.getCanvas().getWidth() / this.displaySize;
        double cellHeight = gc.getCanvas().getHeight() / this.displaySize;
        double cellSize = Math.min(cellWidth, cellHeight);

        Area activeArea = this.getActiveArea();

        Cell[][] area = getActiveAreaCell(activeArea);
        int areaWidth = area.length;
        int areaHeight = area[0].length;

        for (int x = 0; x < areaWidth; x++) {
            for (int y = 0; y < areaHeight; y++) {

                Cell cell = area[x][y];
                if (cell != null) {
                    cell.draw(gc, x * cellSize, y * cellSize, cellSize,
                            cellSize);
                }
            }
        }
    }

    /**
     * Passes the {@link KeyEvent} to the {@link Player}. Also moves all
     * enemies.
     *
     * @param event
     *            the {@link KeyEvent}
     */
    public void keyDown(KeyEvent event) {
        if (this.isFinished == false) {
            this.player.keyDown(event);
            this.player.move();
            this.moveEnemies();
        }
    }

    /**
     * Moves all enemies in the game.
     */
    private void moveEnemies() {
        //Avoids any concurrent modification issues with enemy death by using a shallow clone of the games enemies.
        ArrayList<Enemy> enemiesClone = (ArrayList<Enemy>) this.enemies.clone();
        for (Enemy enemy : enemiesClone) {
            enemy.move();
        }
    }

    /**
     * Returns the active {@link Area} of the game.
     *
     * @return the active area of the game
     */
    private Area getActiveArea() {
        int centerX = this.player.getPositionX();
        int centerY = this.player.getPositionY();

        if (centerX < this.displaySize / 2) {
            centerX = this.displaySize / 2;
        }
        if (centerY < this.displaySize / 2) {
            centerY = this.displaySize / 2;
        }

        int gridWidth = this.grid.length;
        int gridHeight = this.grid[1].length;

        if (centerX > (gridWidth - 1) - this.displaySize / 2) {
            centerX = (gridWidth - 1) - this.displaySize / 2;
        }
        if (centerY > (gridHeight - 1) - this.displaySize / 2) {
            centerY = (gridHeight - 1) - this.displaySize / 2;
        }

        return new Area(centerX - this.displaySize / 2,
                centerY - this.displaySize / 2, centerX + this.displaySize / 2,
                centerY + this.displaySize / 2);
    }

    private Cell[][] getActiveAreaCell(Area activeArea) {
        Cell[][] array = new Cell[this.displaySize][this.displaySize];

        int width = activeArea.getX2() - activeArea.getX1();
        int height = activeArea.getY2() - activeArea.getY1();

        for (int x = 0; x < width + 1; x++) {
            for (int y = 0; y < height + 1; y++) {
                array[x][y] = this.grid[x + activeArea.getX1()][y
                        + activeArea.getY1()];
            }
        }

        return array;
    }

    /**
     * Finished the current level.
     *
     * @param status
     *            level finish status.
     */
    public void finish(LevelFinishStatus status) {
        System.out.println("Level.finish()");
        this.isFinished = true;
        this.finishStatus = status;
    }

    /**
     * States if a level is a custom level or not
     * @return
     */
    public Boolean isCustom() {
        return this.custom;
    }
}
