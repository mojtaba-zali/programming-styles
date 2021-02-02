public interface EventInterface {
    void subscribe(String event_type, MethodOperator handler);
    void publish(Object[] event);
}
