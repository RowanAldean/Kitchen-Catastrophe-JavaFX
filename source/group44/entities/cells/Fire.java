package group44.entities.cells;

import group44.Constants;
import group44.entities.movableObjects.MovableObject;
import group44.game.Level;

/**
 * Represents a fire in a Game.
 *
 * @author Tomas Svejnoha
 * @version 1.0
 */
public class Fire extends StepableCell {
    private static final String PARSE_PATTERN = "%s,%d,%d,%s";

    /**
     * Creates a new instance of {@link Fire}.
     *
     * @param level     - The {@link Level} where the {@link Fire} is located.
     * @param positionX - The position X of the {@link Fire} in the game.
     * @param positionY - The position Y of the {@link Fire} in the game.
     * @param imagePath - Path to the Image which represents the {@link Fire} in the game.
     */
    public Fire(Level level, int positionX, int positionY, String imagePath) {
        super(level, "Fire", positionX, positionY, imagePath);
    }

    /**
     * Interacts with {@link MovableObject} which stepped on the cell.
     *
     * @param object - The {@link MovableObject} which stepped in the {@link Fire}.
     */
    @Override
    protected void onStepped(MovableObject object) {
        object.die(this);
    }

    /**
     * Returns a String representation of the Ground.
     *
     * @return the string representation of the ground.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(String.format(PARSE_PATTERN, Constants.TYPE_FIRE, this.getPositionX(), this.getPositionY(),
                this.getImagePath()));

        return builder.toString();
    }
}