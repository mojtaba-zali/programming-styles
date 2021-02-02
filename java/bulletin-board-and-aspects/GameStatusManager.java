import java.util.ArrayList;

public class GameStatusManager {
    private EventInterface eventManager;
    private ArrayList<String> gameStatusesList;

    public GameStatusManager(EventInterface ev) {
        gameStatusesList = new ArrayList<>();
        eventManager = ev;
        eventManager.subscribe("updateGameStatus", this::updateGameStatus);
    }

    private void updateGameStatus(Object[] event) {
        String[] args = (String[]) event[1];
        ArrayList<Boolean> invalidInputs = (ArrayList<Boolean>) event[2];
        ArrayList<int[]> boardPositions = (ArrayList<int[]>) event[3];
        for (int i = 0; i < boardPositions.size(); i++) {
            String gameStatus = checkGameStatus(boardPositions.get(i),i,args.length);
            gameStatusesList.add(gameStatus);
            if (gameStatus != "gameContinue") break;
        }
        // ALESSIO: Maybe a better name could be "drawBoard" to describe the act of drawing it?
        eventManager.publish(new Object[]{"drawingBoard", args, invalidInputs, boardPositions, gameStatusesList});
    }

    private String checkGameStatus(int[] currentBoardPositions, int round, int inputsLength) {
        final int BOAT_POSITION = -1;
        final int BOARD_POSITION = 4;
        int freeFish = 0;
        int caughtFish = 0;
        for (int i = 0; i < currentBoardPositions.length - 1; i++) {
            if (currentBoardPositions[i] == BOAT_POSITION)
                caughtFish += 1;
            else if (currentBoardPositions[i] == currentBoardPositions[BOARD_POSITION])
                freeFish += 1;
        }
        if (round > inputsLength)
            return "missingInput";
        else if (freeFish + caughtFish < 4)
            return "gameContinue";
        else if (freeFish >= 3)
            return "fishesWin";
        else if (caughtFish >= 3)
            return "huntersWin";
        else if (freeFish == 2 && caughtFish == 2)
            return "tie";
        return "gameContinue";
    }

}
