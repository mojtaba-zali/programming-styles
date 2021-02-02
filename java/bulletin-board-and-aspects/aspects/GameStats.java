package aspects;

/**
 * This utility class should be used by Aspects' code as shared data for holding
 * observations during the execution. If you call it from the main method you are doing something wrong.
 * 
 * @author gambi
 *
 */
public class GameStats {

    public static enum FishStatus {
        ALIVE, SAFE, DEAD;
    }

    public final static String OPENING_TOKEN = ">>>>>";

    public static int totalInputs = 0;
    public static int totalMoves = 0;
    public static int totalInvalid = 0;
    public static int[] pawMoves = new int[] { 0, 0, 0, 0, 0, 0 };
    public static FishStatus[] fishesStatus = new FishStatus[] { FishStatus.ALIVE, FishStatus.ALIVE, FishStatus.ALIVE,
            FishStatus.ALIVE };

    /**
     * Print to output an "easy-to-parse" version of the statistics. You need to
     * call this just before the main method returns using an aspect. Calling this
     * method directly from the main method is a violation of the style.
     */
    public static void printStats() {
        // First line is the OPENING TOKEN
        System.out.println(OPENING_TOKEN);
        // Next line shows the "totals"
        System.out.println("TotalInputs=" + totalInputs + ",ValidMoves=" + totalMoves + ",InvalidMoves=" + totalInvalid);
        // Next line shows the moves for each paws
        StringBuffer sb = new StringBuffer();
        for (int pawIndex = 1; pawIndex <= 6; pawIndex++) {
            sb.append(pawIndex + "=" + pawMoves[pawIndex - 1]);
            sb.append(",");
        }
        System.out.println(sb.toString());
        // Next line show that status of each fish
        sb = new StringBuffer();
        for (int pawIndex = 2; pawIndex <= 5; pawIndex++) {
            sb.append(pawIndex + "=" + fishesStatus[pawIndex - 2]);
            sb.append(",");
        }
        System.out.println(sb.toString());
    }
}
