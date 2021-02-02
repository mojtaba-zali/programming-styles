// ALESSIO: Was this class really necessary? Why can't you trigger the "evaluateInput" directly
//          from main?
public class GameFramework {
    private EventInterface eventManager;

    public GameFramework(EventInterface ev) {
        eventManager = ev;
        ev.subscribe("run", this::run);
    }

    private void run(Object[] event) {
        eventManager.publish(new Object[]{"evaluateInput", event[1]});
    }
}
