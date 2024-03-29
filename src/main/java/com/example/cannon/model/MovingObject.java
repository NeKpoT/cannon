package com.example.cannon.model;

import javafx.scene.canvas.GraphicsContext;
import org.jetbrains.annotations.NotNull;

/**
 * Game object that can move on surfaces and has a direction of sight.
 */
public class MovingObject extends RoundObject {
    /**
     * By how much an object rotates in one rotate call.
     */
    private static final double ROTATE_PRECISION = Math.toRadians(5);
    public static final double CANNON_LENGTH_MULTILIER = 1.3;

    private final int climbHeight;
    private final int speed;

    private boolean facesRight = true;
    private double angle = Math.toRadians(30);

    /**
     * Constructs new MovingObject
     * @param climbHeight how high can it climb when moving by 1 horizontally or fall without
     *                    finishing movement
     * @param speed how much it can move in one movement
     */
    MovingObject(int radius, int climbHeight, @NotNull Vector2 position, @NotNull World world, int speed) {
        super(radius, position, world);
        this.climbHeight = climbHeight;
        this.speed = speed;
    }

    /**
     * Makes object face right
     */
    protected void orientRight() {
        if (!facesRight) {
            flipAngle();
            facesRight = true;
        }
    }

    /**
     * Makes object face left
     */
    protected void orientLeft() {
        if (facesRight) {
            flipAngle();
            facesRight = false;
        }
    }

    private void flipAngle() {
        angle = Math.PI - angle;
    }

    /**
     * Tries to move a MovingObject by it's speed blocks to the right. Also makes object face right.
     * @return <code>false</code> if met an impassable obstruction or a deep enough pit
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean tryMoveRight() {
        orientRight();
        return tryMove(1);
    }

    /**
     * Tries to move a MovingObject by it's speed blocks to the left. Also makes object face left.
     * @return <code>false</code> if met an impassable obstruction or a deep enough pit
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean tryMoveLeft() {
        orientLeft();
        return tryMove(-1);
    }

    /**
     * Rotates object line of sight a bit to the left
     */
    public void rotateLeft() {
        angle += ROTATE_PRECISION;
    }
    /**
     * Rotates object line of sight a bit to the right
     */
    public void rotateRight() {
        angle -= ROTATE_PRECISION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(@NotNull GraphicsContext graphics) {
        super.render(graphics);

        Vector2 canvasPosition = getCanvasPosition();
        var cannonEnd = new Vector2(getRadius() * CANNON_LENGTH_MULTILIER, 0).rotated(-angle).sum(canvasPosition);

        graphics.strokeLine(canvasPosition.x, canvasPosition.y, cannonEnd.x, cannonEnd.y);

        var vectorToFront = new Vector2(getRadius(), 0);
        if (!facesRight) {
            vectorToFront.x = -vectorToFront.x;
        }
        Vector2 frontEnd = canvasPosition.sum(vectorToFront);
        Vector2 nearFrontEnd = canvasPosition.sum(vectorToFront.divided(2));
        graphics.strokeLine(nearFrontEnd.x, nearFrontEnd.y, frontEnd.x, frontEnd.y);
    }

    /**
     * @return angle of object's line of sight
     */
    public double getAngle() {
        return angle;
    }

    /**
     * Tries to move object in the direction <code>dx</code>
     * @param dx direction to move. Expected to by +-1
     * @return <code>false</code> if an obstruction was met
     */
    private boolean tryMove(int dx) {
        if (!isInWorld()) {
            throw new DeadObjectException();
        }

        for (int i = 0; i < speed; i++) {
            var positionBeforeMove = new Vector2(position);
            position.x += dx;
            for (int dy = 0; dy < climbHeight && detectTerrainCollision(); dy++) {
                position.y++;
            }

            if (detectTerrainCollision()) {
                position = positionBeforeMove;
                return false;
            }

            // if falls more than climbHeight, then interrupt movement
            var positionBeforeFall = new Vector2(position);
            //noinspection StatementWithEmptyBody
            while (fallOneStep() != 0 && (positionBeforeFall.y - position.y) < climbHeight);
            if ((positionBeforeFall.y - position.y) >= climbHeight) {
                position = positionBeforeFall;
                return false;
            }
        }
        return true;
    }
}
