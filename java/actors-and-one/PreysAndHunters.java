import java.util.Arrays;

/**
 * For this assignment you will create other classes, do not create unecessary classes inside the same .java file.
 * Be sure that all your classes are in the root/default package or you might need to update the make file.
 */
public class PreysAndHunters {
    
    public static void main(String[] args) {

        // this ACTOR includes the composition of two functions: 1. evaluating inputs 2. producing board positions
        BoardPositionsManager boardPositionsManager = new BoardPositionsManager();

        // this ACTOR also contains the composition of 1. calculating game statuses 2. building the boards
        BoardDrawingManager boardDrawingManager = new BoardDrawingManager();

        // this ACTOR orchestrates the whole computation
        GameController gameController = new GameController();

        ActivePHObject.send(boardPositionsManager, new Object[]{"init", args, boardDrawingManager, gameController});
        ActivePHObject.send(gameController, new Object[]{"run", boardPositionsManager});

        // ALESSIO: Missing synchronization among main and the actors

    }

}