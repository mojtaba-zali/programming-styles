import java.util.ArrayList;

/**
 * ALESSIO This class does not make use of any One?!
 */
public class GameController extends ActivePHObject {

    private BoardPositionsManager boardPositionsManager;

    protected void dispatch(Object[] message) {
        if (message[0].equals("run"))
            this.run(message);
        else if (message[0].equals("print"))
            this.print(message);
        else
            throw new RuntimeException("Message not understood " + message[0]);
    }

    private void run(Object[] message) {
        this.boardPositionsManager = (BoardPositionsManager) message[1];
        send(this.boardPositionsManager, new Object[]{"calculateBoardPositions"});
    }

    private void print(Object[] message) {
        ArrayList<ArrayList<String>> boards = (ArrayList<ArrayList<String>>) message[1];
        for(ArrayList<String> board : boards) {
            for(String line : board) {
                System.out.println(line);
            }
        }
        send(this.boardPositionsManager, new Object[]{"die"});
        this.stopMe = true;
    }

}
