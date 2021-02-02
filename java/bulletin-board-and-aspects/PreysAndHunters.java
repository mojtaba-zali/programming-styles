import java.lang.reflect.Proxy;

/**
 * For this assignment you will create other classes, do not create unnecessary classes inside the same .java file.
 * Be sure that all your classes are in the root/default package or you might need to update the make file.
 */
public class PreysAndHunters {

    // Please DO NOT change those. OK :)
    public final static String ENABLE_COLORS = "enable.colors";
    public final static String ENABLE_STATS = "enable.stats";

    public static void main(String[] args) {

        /*
         * This code shows how you can check if the JVM flags that enable the aspects are set.
         * Note that those are flags not arguments, so either they are there or they aren't. 
         * Using '-Denable.colors=true' or '-Denable.colors=false' is conceptually wrong, so don't do it!
         * 
         * There might be other ways to achieve the same, so feel free to replace this code
         */

        boolean isColorsAspectEnabled = System.getProperties().containsKey( ENABLE_COLORS );
        boolean isGameStatsAspectEnabled = System.getProperties().containsKey( ENABLE_STATS );
        // isColorsAspectEnabled = isGameStatsAspectEnabled = true;

    	EventManager eventManager = new EventManager();
        
        if (isColorsAspectEnabled || isGameStatsAspectEnabled) {
            // ALESSIO: This is ok, but could have been better if each aspect was taken care of separately

            // ALESSIO: I cannot understand this patter: you create an aspect handler and pass the object to wrap and the options. Fine
            //          Bu then you call newInstace, and pass EXACTLY the same inputs as before. The implementation of newInstance, again
            //          creates an instance of aspectHandler ...
            AspectHandler aspectHandler = new AspectHandler(eventManager, isColorsAspectEnabled, isGameStatsAspectEnabled);
            EventInterface profiledEventManager = (EventInterface) aspectHandler.newInstance(eventManager, isColorsAspectEnabled, isGameStatsAspectEnabled);

            // ALESSIO: The following code could have been merged by simply using the interface: EventInterface
            new InputEvaluationManager(profiledEventManager);
            new BoardPositionsManager(profiledEventManager);
            new GameStatusManager(profiledEventManager);
            new BoardDrawingManager(profiledEventManager);
            new GameFramework(profiledEventManager);

            profiledEventManager.publish(new Object[]{"run", args});
        } else {
            new InputEvaluationManager(eventManager);
            new BoardPositionsManager(eventManager);
            new GameStatusManager(eventManager);
            new BoardDrawingManager(eventManager);
            new GameFramework(eventManager);
            
            eventManager.publish(new Object[]{"run", args});
        }
    }

}
