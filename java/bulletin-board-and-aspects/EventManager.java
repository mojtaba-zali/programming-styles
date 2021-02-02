import java.util.HashMap;
import java.util.Map;

public class EventManager implements EventInterface {
    // ALESSIO: This design is not optinal as it does not allow more than one handler to be notifies about the same event!
    //          The correct form would have been: private static Map<String, List<MethodOperator>> subscriptions
    //          Of course, also `publish` need an update to make sure EventManager notifies all the subscribers
    private static Map<String, MethodOperator> subscriptions;

    public EventManager() {
        subscriptions = new HashMap<>();
    }

    
    // 
    public void subscribe(String event_type, MethodOperator handler) {
        subscriptions.put(event_type, handler);
    }

    public void publish(Object[] event) {
        String event_type = (String) event[0];
        if (subscriptions.containsKey(event_type))
            subscriptions.get(event_type).perform(event);
    }
}
