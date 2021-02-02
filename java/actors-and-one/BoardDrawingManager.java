import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BoardDrawingManager extends ActivePHObject {

    private Object[] value;
    private GameController gameController;

    protected void dispatch(Object[] message) {
        if (message[0].equals("init"))
            this.init(message);
        else if (message[0].equals("drawBoard")) {
            // this function composition could be called in another method (similar to boardPositionManager).
            this.bind(this::updateGameStatus).bind(this::buildBoard).unwrap();
        }
    }

    private void init(Object[] message) {
        this.value = (Object[]) message[1]; // this is wrapping.
        this.gameController = (GameController) message[2];
    }

    private BoardDrawingManager bind(FunctionCallable function) {
        this.value = function.call(this.value);
        return this;
    }

    public void unwrap() {
        send(this.gameController, new Object[]{"print", this.value[0]});
    }

    //
    // these "functions" are presented here, in this class, with the aim of achieving more logical organization in the program.
    //
    private Object[] updateGameStatus(Object[] inputs) {
        String[] args = (String[]) inputs[0];
        ArrayList<Boolean> invalidInputs = (ArrayList<Boolean>) inputs[1];
        ArrayList<int[]> boardPositions = (ArrayList<int[]>) inputs[2];
        ArrayList<String> gameStatusesList = new ArrayList<>();
        for (int i = 0; i < boardPositions.size(); i++) {
            final int BOAT_POSITION = -1;
            final int BOARD_POSITION = 4;
            int freeFish = 0;
            int caughtFish = 0;
            for (int j = 0; j < boardPositions.get(i).length - 1; j++) {
                if (boardPositions.get(i)[j] == BOAT_POSITION)
                    caughtFish += 1;
                else if (boardPositions.get(i)[j] == boardPositions.get(i)[BOARD_POSITION])
                    freeFish += 1;
            }
            String gameStatus = "";
            if (i > args.length)
                gameStatus = "missingInput";
            else if (freeFish + caughtFish < 4)
                gameStatus = "gameContinue";
            else if (freeFish >= 3)
                gameStatus = "fishesWin";
            else if (caughtFish >= 3)
                gameStatus = "huntersWin";
            else if (freeFish == 2 && caughtFish == 2)
                gameStatus = "tie";
            gameStatusesList.add(gameStatus);
            if (!gameStatus.equals("gameContinue")) break;
        }
        return new Object[]{args, invalidInputs, boardPositions, gameStatusesList};
    }

    private Object[] buildBoard(Object[] inputs) {
        String[] args = (String[]) inputs[0];
        ArrayList<Boolean> invalidInputs = (ArrayList<Boolean>) inputs[1];
        ArrayList<int[]> boardPositions = (ArrayList<int[]>) inputs[2];
        ArrayList<String> gameStatusesList = (ArrayList<String>) inputs[3];
        ArrayList<ArrayList<String>> boards = new ArrayList<>();

        for (int i = 0; i <= args.length +1; i++) {
            ArrayList<String> currentBoard = new ArrayList<>();
            String margin = ""; // using for alignment when messages required to be shown
            String[] messageContent = {"", ""}; // containing the content of two lines of messages
            boolean messageShown = invalidInputs.get(i) || !gameStatusesList.get(i).equals("gameContinue");
            final Map<String, String[]> messages = new HashMap<>(){{
                put("fishesWin", new String[]{" Go team fish! ", " Finally free. "});
                put("huntersWin", new String[]{" The fishing was good; it's ", " the catching that was bad. "});
                put("tie", new String[]{" Nice tie ", " LOL! "});
                put("missingInput", new String[]{" Missing inputs. ", " The game ends!! "});
                put("invalidInput", new String[]{" The provided input is not valid! ", "Offending input: "});
            }};
            final int BOARD_INDEX = 4;

            // PREPARE MESSAGE CONTENT
            if (messageShown) {
                final int ACTUAL_BOARD_LENGTH = 9 + 9 + 4 * boardPositions.get(i)[BOARD_INDEX]; // based on the number of characters
                String[] selectedMessage = (invalidInputs.get(i) ? messages.get("invalidInput") : messages.get(gameStatusesList.get(i)));
                // preparing lines corresponding to the fourth and fifth lines
                for (int j = 0; j <= 1; j++) {
                    String message = selectedMessage[j];
                    int messageBoxSize;
                    if (invalidInputs.get(i) && j == 1) { // APPEND INVALID INPUT IN MESSAGE
                        message += (args[i-1].length() > 3) ? args[i-1].substring(0, 3) + "..." : args[i-1];
                        messageBoxSize = (ACTUAL_BOARD_LENGTH - selectedMessage[0].length() < 0) ? selectedMessage[0].length() - message.length() : ACTUAL_BOARD_LENGTH - message.length();
                    }else
                        messageBoxSize = ACTUAL_BOARD_LENGTH - message.length();
                    messageBoxSize = Math.max(messageBoxSize, 0);
                    int marginCount = messageBoxSize / 2;
                    messageContent[j] = "│";
                    messageContent[j] += String.join("", Collections.nCopies((messageBoxSize % 2 != 0 ? marginCount+1 : marginCount), " ")); // In case the message cannot be perfectly centered
                    messageContent[j] += message;
                    messageContent[j] += String.join("", Collections.nCopies(marginCount, " "));
                    messageContent[j] += "│";
                }
                int actualBoardLength = 9 + 9 + 4 * boardPositions.get(i)[BOARD_INDEX];
                var marginLength = (messageContent[0].length() - actualBoardLength) / 2;
                margin = String.join("", Collections.nCopies(marginLength, " "));
            }

            // DRAWING
            // draw top boarder
            StringBuilder line;
            line = new StringBuilder("╔════════╤");
            line.append("═══╤".repeat(Math.max(0, boardPositions.get(i)[BOARD_INDEX])));
            line.append("═══════╗");
            currentBoard.add(margin + line + margin);
            // draw four middle lines
            if (messageShown) {
                line = new StringBuilder("┌");
                line.append("─".repeat(Math.max(0, messageContent[0].length() - 2)));
                line.append("┐");
                currentBoard.add(line.toString());
                currentBoard.add(messageContent[0]);
                currentBoard.add(messageContent[1]);
                line = new StringBuilder("└");
                line.append("─".repeat(Math.max(0, messageContent[0].length() - 2)));
                line.append("┘");
                currentBoard.add(line.toString());
            }
            else {
                final int BOAT_POSITION = -1;
                final int SHADED_CELL_POSITION = 6;
                for (int j = 0; j < 4; j++) {
                    //////////////// draw boat section
                    var boat = "";
                    switch (j) {
                        case 0:
                            boat = "┌──┐1";
                            break;
                        case 1:
                        case 2:
                            var fishIndex = (j == 1 ? 1 : 3);
                            boat = "│" + (boardPositions.get(i)[fishIndex-1] == BOAT_POSITION ? fishIndex+1 : " ");
                            boat += (boardPositions.get(i)[fishIndex] == BOAT_POSITION ? fishIndex+2 : " ") + "│ ";
                            break;
                        case 3:
                            boat = "└──┘6";
                            break;
                    }
                    line = new StringBuilder("║  " + boat + " │");
                    //////////////// draw river section
                    for (int k = 0; k < boardPositions.get(i)[4]; k++) {
                        if (k == boardPositions.get(i)[4] - SHADED_CELL_POSITION && k != boardPositions.get(i)[j])
                            line.append(" ▓ │");
                        else if (k == boardPositions.get(i)[j])
                            line.append(" ").append(j + 2).append(" │");
                        else
                            line.append("   │");
                    }
                    //////////////// draw sea section
                    if (boardPositions.get(i)[j] == boardPositions.get(i)[4])
                        line.append("   ").append(j + 2).append("   ║");
                    else
                        line.append("       ║");
                    currentBoard.add(line.toString());
                }


            }
            // draw bottom boarder
            line = new StringBuilder("╚════════╧");
            line.append("═══╧".repeat(Math.max(0, boardPositions.get(i)[BOARD_INDEX])));
            line.append("═══════╝");
            currentBoard.add(margin + line + margin);
            boards.add(currentBoard);
            if (!gameStatusesList.get(i).equals("gameContinue"))
                break;
        }
        return new Object[]{boards};
    }
}
