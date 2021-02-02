import aspects.ColorPalette;
import aspects.GameStats;
import java.lang.reflect.*;
import java.util.ArrayList;

public class AspectHandler implements InvocationHandler {

    // The object here is the "eventManager"
    private final Object obj;
    private boolean colorAspectEnabled;
    private boolean statAspectEnabled;
    private boolean lastStateIsBoat = false;

    public Object newInstance(Object obj, boolean isColorsAspectEnabled, boolean isGameStatsAspectEnabled) {
        return Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(), 
                new AspectHandler(obj, isColorsAspectEnabled, isGameStatsAspectEnabled));
    }

    public AspectHandler(Object obj, boolean isColorsAspectEnabled, boolean isGameStatsAspectEnabled) {
        this.colorAspectEnabled = isColorsAspectEnabled;
        this.statAspectEnabled = isGameStatsAspectEnabled;
        this.obj = obj;
    }

    /**
     * ALESSIO: This method could have been refactored a bit but all in all looks fine.
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        /////////////// BEFORE INVOCATION OF THE METHODS - WHEN COLOR ASPECT ENABLED

        if (method.getName() == "publish" && colorAspectEnabled) {
            // ALESSIO: Inspect the event
            Object[] event = (Object[]) args[0];

            if (event[0] == "drawMargin")
                System.out.print(ColorPalette.RESET);
            if (event[0] == "drawOutsideBoarder") {
                System.out.print(ColorPalette.BOARD_BACKGROUND);
                System.out.print(ColorPalette.BOARD_FOREGROUND);
            }
            if (event[0] == "drawMessageContent" || event[0] == "drawOutsideBanner") {
                System.out.print(ColorPalette.BANNER_BACKGROUND);
                System.out.print(ColorPalette.BANNER_FOREGROUND);
            }
            if (event[0] == "drawBoat") {
                System.out.print(ColorPalette.BOAT_BACKGROUND);
                System.out.print(ColorPalette.BOAT_FOREGROUND);
                lastStateIsBoat = true;
            }
            if (event[0] == "drawPaws") {
                if ((int) event[1] == 1 || (int) event[1] == 6) {
                    System.out.print(ColorPalette.BOAT_BACKGROUND);
                    System.out.print(ColorPalette.FISHER);
                }
                else
                    System.out.print(ColorPalette.FISH);
            }
            if (event[0] == "drawNewLine") {
                System.out.print(ColorPalette.RESET);
            }
        }

        /////////////// INVOCATION

        Object result = method.invoke(obj, args);

        /////////////// AFTER INVOCATION OF THE METHODS

        // COLOR ASPECT ENABLED
        if (method.getName() == "publish" && colorAspectEnabled) {
            // adjusting the colors after method invocation
            Object[] event = (Object[]) args[0];
            if (event[0] == "drawMargin") {
                System.out.print(ColorPalette.BOARD_BACKGROUND);
                System.out.print(ColorPalette.BOARD_FOREGROUND);
            }
            if (event[0] == "drawPaws" && !lastStateIsBoat)
                System.out.print(ColorPalette.BOARD_FOREGROUND);
            if (event[0] == "drawPaws" && ((int) event[1] == 1 || (int) event[1] == 6))
                    System.out.print(ColorPalette.BOARD_BACKGROUND);
            if (event[0] == "drawBoat" || event[0] == "drawNewLine") {
                System.out.print(ColorPalette.BOARD_BACKGROUND);
                System.out.print(ColorPalette.BOARD_FOREGROUND);
                lastStateIsBoat = false;
            }
            if (event[0] == "drawMessageContent" || event[0] == "drawOutsideBanner") {
                System.out.print(ColorPalette.BANNER_BACKGROUND);
                System.out.print(ColorPalette.BANNER_FOREGROUND);
            }
        }

        // STAT ASPECT ENABLED
        if (method.getName() == "publish" && statAspectEnabled) {
            // computing and printing the stats
            Object[] event = (Object[]) args[0];
            // ALESSIO: This is the last event right? So you know that at this point there will be no more computations
            //      and you can output your statistics easily. What it is not really convincing, is that ALL the data that you need
            //      to compute the stats are someone incapsulated in this very last message... The idea instead was to apply the stats
            //      aspect in different places in the code, not just here.
            if (event[0] == "drawingBoard") {
                String[] sequenceInputs = (String[]) event[1];
                ArrayList<Boolean> invalidInputs = (ArrayList<Boolean>) event[2];
                ArrayList<int[]> boardPositions = (ArrayList<int[]>) event[3];
                ArrayList<String> gameStateList = (ArrayList<String>) event[4];
                int processedInputNo;
                // compute total number and the number of processed inputs
                GameStats.totalInputs = sequenceInputs.length;
                if (gameStateList.get(gameStateList.size()-1).equals("missingInput"))
                    processedInputNo = gameStateList.size() - 2;
                else
                    processedInputNo = gameStateList.size() - 1;
                // compute valid and invalid moves
                for (int i = 1; i <= processedInputNo; i++)
                    if (invalidInputs.get(i))
                        GameStats.totalInvalid += 1;
                    else
                        GameStats.totalMoves += 1;
                // compute the final status of fishes
                int[] finalPositions = boardPositions.get(gameStateList.size()-1);
                for (int i = 0; i < 4; i++) {
                    if (finalPositions[i] == -1)
                        GameStats.fishesStatus[i] = GameStats.FishStatus.DEAD;
                    else if (finalPositions[i] == finalPositions[4])
                        GameStats.fishesStatus[i] = GameStats.FishStatus.SAFE;
                    else
                        GameStats.fishesStatus[i] = GameStats.FishStatus.ALIVE;
                }
                // compute paw moves
                for (int i = 1; i <= processedInputNo; i++) {
                    for (int j = 0; j < 5; j++) {
                        if (boardPositions.get(i)[j] < boardPositions.get(i-1)[j] && j == 4)
                            GameStats.pawMoves[0] = GameStats.pawMoves[5] += 1; // board shrunk means hunters moved
                        else if (boardPositions.get(i)[j] > boardPositions.get(i-1)[j])
                            GameStats.pawMoves[j+1] += 1;
                    }
                }
                // ALESSIO: This is a violation of the style. You are mixing together the two aspects, which instead
                //          should be kept separated.
                if (colorAspectEnabled)
                    System.out.print(ColorPalette.RESET);
                GameStats.printStats();
            }
        }
        return result;
    }
}