import java.util.ArrayList;

public class BoardPositionsManager {
    private EventInterface eventManager;
    private ArrayList<int[]> boardPositions;

    public BoardPositionsManager(EventInterface ev) {
        boardPositions = new ArrayList<>();
        eventManager = ev;
        eventManager.subscribe("updateBoardPositions", this::updateBoardPositions);
    }

    private void updateBoardPositions(Object[] event) {
        ArrayList<Boolean> invalidInputs = (ArrayList<Boolean>) event[1];
        String[] args = (String[]) event[2];
        boardPositions.add(new int[]{5, 5, 5, 5, 11}); // fishes are initially located in 6th position and board length is 11

        for (int i = 0; i <= args.length; i++) {
            int[] currentBoardPositions = boardPositions.get(i).clone();
            // CHECK THE VALIDITY OF THE CURRENT INPUT
            if (invalidInputs.get(i+1)) {
                boardPositions.add(currentBoardPositions);
                continue;
            }
            if (i >= args.length)
                boardPositions.add(currentBoardPositions);
            else
                boardPositions.add(updateCurrentBoardPosition(Integer.parseInt(args[i]), currentBoardPositions));
        }
        eventManager.publish(new Object[]{"updateGameStatus", args, invalidInputs, boardPositions});
    }

    private int[] updateCurrentBoardPosition(int dice, int[] currentBoardPositions) {
        final int BOAT_POSITION = -1;
        final int BOARD_POSITION_INDEX = 4;
        // if 1/6 is drawn, the hunter boat moves OR after the hunter capture a fish, when the number to the fish is drawn
        if (dice == 1 || dice == 6 || currentBoardPositions[dice - 2] == BOAT_POSITION) {
            for (int i = 0; i < currentBoardPositions.length; i++) {
                if (currentBoardPositions[i] != BOAT_POSITION)
                    currentBoardPositions[i] -= 1;
            }
        }
        // If the number to the fish is received, then another fish can move by one step. CLOSEST TO SAFETY + SMALLEST NUMBER
        else if (currentBoardPositions[dice - 2] == currentBoardPositions[BOARD_POSITION_INDEX]) {
            var minIndex = -1;
            for (int i = 3; i >= 0 ; i--) {
                if (currentBoardPositions[i] == currentBoardPositions[BOARD_POSITION_INDEX])
                    continue;
                else if (minIndex == -1)
                    minIndex = i;
                else if (currentBoardPositions[i] >= currentBoardPositions[minIndex])
                    minIndex = i;
            }
            currentBoardPositions[minIndex] += 1;
        }
        else {
            currentBoardPositions[dice - 2] += 1;
        }
        return currentBoardPositions;
    }

}
