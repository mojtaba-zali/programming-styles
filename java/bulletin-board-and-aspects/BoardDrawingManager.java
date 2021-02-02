import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BoardDrawingManager {
    private EventInterface eventManager;
    private final int BOARD_INDEX = 4;
    private final int BOAT_POSITION = -1;
    private final int SHADED_CELL_POSITION = 6;

    public BoardDrawingManager(EventInterface ev) {
        eventManager = ev;

        // ALESSIO: This seems like an awfully long list of events... But I guess it made easier to apply aspects, so its fine.
        eventManager.subscribe("drawingBoard", this::drawBoard);
        eventManager.subscribe("drawMargin", this::drawMargin);
        eventManager.subscribe("drawOutsideBoarder", this::drawOutsideBoarder);
        eventManager.subscribe("drawOutsideBanner", this::drawOutsideBanner);
        eventManager.subscribe("drawMiddleLines", this::drawMiddleLines);
        eventManager.subscribe("drawMessageContent", this::drawMessageContent);
        eventManager.subscribe("drawBoat", this::drawBoat);

        // ALESSIO: What's draw river?
        eventManager.subscribe("drawRiver", this::drawRiver);
        eventManager.subscribe("drawSea", this::drawSea);
        eventManager.subscribe("drawPaws", this::drawPaws);
        eventManager.subscribe("drawNewLine", this::drawNewLine);
    }

    private void drawBoard(Object[] event) {
        String[] args = (String[]) event[1];
        ArrayList<Boolean> invalidInputs = (ArrayList<Boolean>) event[2];
        ArrayList<int[]> boardPositions = (ArrayList<int[]>) event[3];
        ArrayList<String> gameStateList = (ArrayList<String>) event[4];

        for (int i = 0; i <= args.length +1; i++) {
            String[] messageContent = {"", ""}; // containing the content of two lines of messages
            boolean messageShown = invalidInputs.get(i) || !gameStateList.get(i).equals("gameContinue");

            if (messageShown)
                messageContent = prepareMessageContent(boardPositions.get(i)[BOARD_INDEX], gameStateList.get(i), args, i, invalidInputs.get(i));

            // DRAWING.
            // ALESSIO: This is something I cannot understand. This events is produced AND consumed by the same Thing, so why
            //          do you need it at all? This is unecessary as it would be enough to call this.drawOutsideBoarder(...)
            //          Also because you captured that behaviour in the method, you could use the method as join point for the aspect...
            eventManager.publish(new Object[]{"drawOutsideBoarder", true, boardPositions.get(i)[BOARD_INDEX], messageContent[0].length(), messageShown});

            if (messageShown) {
                eventManager.publish(new Object[]{"drawOutsideBanner", true, messageContent[0].length() - 2});
                eventManager.publish(new Object[]{"drawMessageContent", messageContent[0]});
                eventManager.publish(new Object[]{"drawMessageContent", messageContent[1]});
                eventManager.publish(new Object[]{"drawOutsideBanner", false, messageContent[0].length() - 2});
            }
            else
                eventManager.publish(new Object[]{"drawMiddleLines", boardPositions, i});

            eventManager.publish(new Object[]{"drawOutsideBoarder", false, boardPositions.get(i)[BOARD_INDEX], messageContent[0].length(), messageShown});

            if (!gameStateList.get(i).equals("gameContinue"))
                break;
        }
    }

    private String[] prepareMessageContent(int boardLength, String gameStatus, String[] inputs, int round, boolean invalidInput) {
        // preparing the message content
        final Map<String, String[]> messages = new HashMap<>(){{
            put("fishesWin", new String[]{" Go team fish! ", " Finally free. "});
            put("huntersWin", new String[]{" The fishing was good; it's ", " the catching that was bad. "});
            put("tie", new String[]{" Nice tie ", " LOL! "});
            put("missingInput", new String[]{" Missing inputs. ", " The game ends!! "});
            put("invalidInput", new String[]{" The provided input is not valid! ", "Offending input: "});
        }};
        final int ACTUAL_BOARD_LENGTH = 9 + 9 + 4 * boardLength; // based on the number of characters
        String[] selectedMessage = (invalidInput ? messages.get("invalidInput") : messages.get(gameStatus));
        String[] messageContent = {"", ""}; // containing the content of two lines of messages

        // preparing lines corresponding to the fourth and fifth lines
        for (int j = 0; j <= 1; j++) {
            String message = selectedMessage[j];
            int messageBoxSize;
            if (invalidInput && j == 1) { // APPEND INVALID INPUT IN MESSAGE
                message += (inputs[round-1].length() > 3) ? inputs[round-1].substring(0, 3) + "..." : inputs[round-1];
                messageBoxSize = (ACTUAL_BOARD_LENGTH - selectedMessage[0].length() < 0) ? selectedMessage[0].length() - message.length() : ACTUAL_BOARD_LENGTH - message.length();
            }else
                messageBoxSize = ACTUAL_BOARD_LENGTH - message.length();
            messageBoxSize = Math.max(messageBoxSize, 0);
            int margin = messageBoxSize / 2;
            messageContent[j] = "│";
            messageContent[j] += String.join("", Collections.nCopies((messageBoxSize % 2 != 0 ? margin+1 : margin), " ")); // In case the message cannot be perfectly centered
            messageContent[j] += message;
            messageContent[j] += String.join("", Collections.nCopies(margin, " "));
            messageContent[j] += "│";
        }
        return messageContent;
    }

    private void drawMargin(Object[] event) {
        int messageContentLength = (int) event[1];
        int boardLength = (int) event[2];
        boolean messageShown = (boolean) event[3];

        // calculating the margin size based on the length of the message box
        int actualBoardLength = 9 + 9 + 4 * boardLength;
        var marginLength = (messageContentLength - actualBoardLength) / 2;
        if (messageShown)
            System.out.print(String.join("", Collections.nCopies(marginLength, " ")));
    }

    private void drawMessageContent(Object[] event) {
        String messageContent = (String) event[1];
        System.out.print(messageContent);
        eventManager.publish(new Object[]{"drawNewLine"});
    }

    private void drawOutsideBoarder(Object[] event) {
        boolean topLine = (boolean) event[1];
        int lineLength = (int) event[2];
        int messageLength = (int) event[3];
        boolean messageShown = (boolean) event[4];
        StringBuilder line;

        if (topLine) {
            line = new StringBuilder("╔════════╤");
            line.append("═══╤".repeat(Math.max(0, lineLength)));
            line.append("═══════╗");
        } else {
            line = new StringBuilder("╚════════╧");
            line.append("═══╧".repeat(Math.max(0, lineLength)));
            line.append("═══════╝");
        }
        eventManager.publish(new Object[]{"drawMargin", messageLength, lineLength, messageShown});
        System.out.print(line);
        eventManager.publish(new Object[]{"drawMargin", messageLength, lineLength, messageShown});
        eventManager.publish(new Object[]{"drawNewLine"});
    }

    private void drawOutsideBanner(Object[] event) {
        boolean topLine = (boolean) event[1];
        int lineLength = (int) event[2];

        StringBuilder line;
        if (topLine) {
            line = new StringBuilder("┌");
            line.append("─".repeat(Math.max(0, lineLength)));
            line.append("┐");
        } else {
            line = new StringBuilder("└");
            line.append("─".repeat(Math.max(0, lineLength)));
            line.append("┘");
        }
        System.out.print(line);
        eventManager.publish(new Object[]{"drawNewLine"});
    }

    private void drawMiddleLines(Object[] event) {
        // drawing four middle lines, messages or board
        ArrayList<int[]> currentBoardPosition = (ArrayList<int[]>) event[1];
        int index = (int) event[2];

        for (int i = 0; i < 4; i++) {

            //////////////// draw boat section
            System.out.print("║ ");
            int fishIndex = (i == 1 ? 1 : 3);
            eventManager.publish(new Object[]{"drawBoat", i, currentBoardPosition.get(index)[fishIndex-1] == BOAT_POSITION, currentBoardPosition.get(index)[fishIndex] == BOAT_POSITION, fishIndex});
            if (i == 0) {
                eventManager.publish(new Object[]{"drawPaws", 1});
                System.out.print(" │");
            }
            else if (i==3) {
                eventManager.publish(new Object[]{"drawPaws", 6});
                System.out.print(" │");
            }
            else
                System.out.print(" │");

            //////////////// draw river section
            eventManager.publish(new Object[]{"drawRiver", index, currentBoardPosition, i});

            //////////////// draw sea section
            eventManager.publish(new Object[]{"drawSea", index, currentBoardPosition, i});

            eventManager.publish(new Object[]{"drawNewLine"});
        }
    }

    private void drawBoat(Object[] event) {
        int line = (int) event[1];
        boolean topPawsCaught = (boolean) event[2];
        boolean bottomPawsCaught = (boolean) event[3];
        int fishIndex = (int) event[4];

        switch (line) {
            case 0:
                System.out.print(" ┌──┐");
                break;
            case 1:
            case 2:
                System.out.print(" │");
                if (topPawsCaught)
                    eventManager.publish(new Object[]{"drawPaws", fishIndex+1});
                else
                    System.out.print(" ");
                if (bottomPawsCaught)
                    eventManager.publish(new Object[]{"drawPaws", fishIndex+2});
                else
                    System.out.print(" ");
                System.out.print("│ ");
                break;
            case 3:
                System.out.print(" └──┘");
                break;
        }
    }

    private void drawRiver(Object[] event) {
        int index = (int) event[1];
        ArrayList<int[]> currentBoardPosition = (ArrayList<int[]>) event[2];
        int paw = (int) event[3];

        for (int j = 0; j < currentBoardPosition.get(index)[4]; j++) {
            if (j == currentBoardPosition.get(index)[4] - SHADED_CELL_POSITION && j != currentBoardPosition.get(index)[paw])
                System.out.print(" ▓ │");
            else if (j == currentBoardPosition.get(index)[paw]) {
                System.out.print(" ");
                eventManager.publish(new Object[]{"drawPaws", paw+2});
                System.out.print(" │");
            }
            else
                System.out.print("   │");
        }
    }

    private void drawSea(Object[] event) {
        int index = (int) event[1];
        ArrayList<int[]> currentBoardPosition = (ArrayList<int[]>) event[2];
        int paw = (int) event[3];

        if (currentBoardPosition.get(index)[paw] == currentBoardPosition.get(index)[4]) {
            System.out.print("   ");
            eventManager.publish(new Object[]{"drawPaws", paw + 2});
            System.out.print("   ║");
        }
        else
            System.out.print("       ║");
    }

    private void drawPaws(Object[] event) {
        System.out.print((int) event[1]);
    }

    private void drawNewLine(Object[] event) {
        System.out.println();
    }
}
