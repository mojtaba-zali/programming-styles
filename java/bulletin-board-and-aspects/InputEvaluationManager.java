import java.util.ArrayList;
import java.util.Arrays;

public class InputEvaluationManager {
    public EventInterface eventManager;
    public ArrayList<Boolean> evaluatedInputList;

    public InputEvaluationManager(EventInterface ev) {
        evaluatedInputList = new ArrayList<>();
        eventManager = ev;
        eventManager.subscribe("evaluateInput", this::updateInvalidInputs);
    }

    public void updateInvalidInputs(Object[] event) {
        String[] args = (String[]) event[1];
        evaluatedInputList.add(false);
        for (int i = 0; i <= args.length ; i++) {
            evaluatedInputList.add(false);
            evaluatedInputList.set(i+1, inputIsInvalid(args, i));
        }
        eventManager.publish(new Object[]{"updateBoardPositions", evaluatedInputList, args});
    }

    public static boolean inputIsInvalid(String[] args, int round) {
        return round < args.length && !Arrays.asList(new String[]{"1", "2", "3", "4", "5", "6"}).contains(args[round]);
    }

}
