import java.util.LinkedList;
import java.util.Queue;

public abstract class ActivePHObject extends Thread {

    public Queue<Object[]> queue;
    protected boolean stopMe;

    ActivePHObject() {
        this.setName(this.getClass().getSimpleName());
        stopMe = false;
        queue = new LinkedList<>();
        this.start();
    }

    @Override
    public void run() {
        while(!stopMe) {
            // ALESSIO: This smells fishy, why not simply queue.isEmpty() ?!
            if (this.queue.peek() == null) {
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // ALESSIO: How does this thread ever stop ?!
                }
                continue;
            }
            Object[] message = this.queue.remove();

            this.dispatch(message);

            String str = (String) message[0];
            if (str.equals("die"))
                this.stopMe = true;
        }
    }

    // ALESSIO: This violates the style because it requires that the actor method is invoked directly!
    public static void send(ActivePHObject receiver, Object[] message) {
        receiver.queue.add(message);
    }

    protected abstract void dispatch(Object[] message);

}
