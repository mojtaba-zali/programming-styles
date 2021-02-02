import java.util.ArrayList;
import java.util.Arrays;

public class BoardPositionsManager extends ActivePHObject{

    private Object[] value;
    private BoardDrawingManager boardDrawingManager;
    private GameController gameController;

    protected void dispatch(Object[] message) {
        if (message[0].equals("init"))
        // ALESSIO: This is not implemented using the One?
            this.init(message);
        else if (message[0].equals("calculateBoardPositions")) {
            // ALESSIO: This is not implemented using the One?
            this.calculateBoardPositions();
        }
        else
            send(this.boardDrawingManager, message); // forward
    }

    private void init(Object[] message) {
        // This is the wrapping. It could also be placed in another function called UNIT. I have put it here for less complexity.
        this.value = (Object[]) message[1];

        // required to send message later
        this.boardDrawingManager = (BoardDrawingManager) message[2];
        this.gameController = (GameController) message[3];
    }

    private BoardPositionsManager bind(FunctionCallable function) {
        this.value = function.call(this.value);
        return this;
    }

    private void unwrap() {
        send(this.boardDrawingManager, new Object[]{"init", this.value, this.gameController});
        send(this.boardDrawingManager, new Object[]{"drawBoard"});
    }

    private void calculateBoardPositions() {
        // This method is responsible for COMPOSING. I believe this could have also handled in dispatch.
        // I see but "this" is not a monad, so your implementation only "pretends" to follow the One Style...
        this.bind(this::evaluateInputs).bind(this::updateBoardPositions).unwrap();
    }

    //
    // these "functions" are presented here, in this class, with the aim of achieving more logical organization in the program.
    //
    private Object[] evaluateInputs(Object[] inputs) {
        String[] args = (String[]) inputs;
        ArrayList<Boolean> evaluatedInputList = new ArrayList<>();
        evaluatedInputList.add(false);
        for (int i = 0; i <= args.length ; i++) {
            evaluatedInputList.add(false);
            evaluatedInputList.set(i+1, i < args.length && !Arrays.asList(new String[]{"1", "2", "3", "4", "5", "6"}).contains(args[i]));
        }
        return new Object[]{args, evaluatedInputList};
    }

    private Object[] updateBoardPositions(Object[] inputs) {
        String[] args = (String[]) inputs[0];
        ArrayList<Boolean> invalidInputs = (ArrayList<Boolean>) inputs[1];
        ArrayList<int[]> boardPositions = new ArrayList<>();
        boardPositions.add(new int[]{5, 5, 5, 5, 11}); // fishes are initially located in 6th position and board length is 11

        for (int i = 0; i <= args.length; i++) {
            int[] currentBoardPositions = boardPositions.get(i).clone();
            // CHECK THE VALIDITY OF THE CURRENT INPUT
            if (invalidInputs.get(i+1)) {
                boardPositions.add(currentBoardPositions);
                continue;
            }
            if (i >= args.length) {
                boardPositions.add(currentBoardPositions);
            }
            else {
                final int BOAT_POSITION = -1;
                final int BOARD_POSITION_INDEX = 4;
                final int dice = Integer.parseInt(args[i]);
                // if 1/6 is drawn, the hunter boat moves OR after the hunter capture a fish, when the number to the fish is drawn
                if (dice == 1 || dice == 6 || currentBoardPositions[dice - 2] == BOAT_POSITION) {
                    for (int j = 0; j < currentBoardPositions.length; j++) {
                        if (currentBoardPositions[j] != BOAT_POSITION)
                            currentBoardPositions[j] -= 1;
                    }
                }
                // If the number to the fish is received, then another fish can move by one step. CLOSEST TO SAFETY + SMALLEST NUMBER
                else if (currentBoardPositions[dice - 2] == currentBoardPositions[BOARD_POSITION_INDEX]) {
                    var minIndex = -1;
                    for (int j = 3; j >= 0 ; j--) {
                        if (currentBoardPositions[j] == currentBoardPositions[BOARD_POSITION_INDEX])
                            continue;
                        else if (minIndex == -1)
                            minIndex = j;
                        else if (currentBoardPositions[j] >= currentBoardPositions[minIndex])
                            minIndex = j;
                    }
                    currentBoardPositions[minIndex] += 1;
                }
                else {
                    currentBoardPositions[dice - 2] += 1;
                }
                boardPositions.add(currentBoardPositions);
            }
        }
        return new Object[]{args, invalidInputs, boardPositions};
    }

}
