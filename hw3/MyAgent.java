import Mobile.*;

/**
 * MyAgent is a test mobile agent that is injected to the 1st Mobile.Place
 * platform to print the breath message, migrates to the 2nd platform to
 * say "Hello!", and even moves to the 3rd platform to say "Oi!".
 * 
 * @author  Munehiro Fukuda
 * @version %I% %G%
 * @since   1.0
 */
public class MyAgent extends Agent {
    public int hopCount = 0;
    public String[] destination = null;
    
    /**
     * The consturctor receives a String array as an argument from 
     * Mobile.Inject.
     *
     * @param args arguments passed from Mobile.Inject to this constructor
     */
    public MyAgent( String[] args ) {
	    destination = args;
    }

    /**
     * init( ) is the default method called upon an agent inject.
     */
    public void init( ) {
        System.out.println( "agent( " + agentId + ") invoked init: " +
                    "hop count = " + hopCount +
                    ", next dest = " + destination[hopCount] );
        String[] args = new String[1];
        args[0] = "Hello!";
        // Hop to the first destination
        hop(destination[hopCount], "step", args);
    
    }
    
    /**
     * step( ) is invoked upon an agent migration to destination[0] after 
     * init( ) calls hop( ).
     * 
     * @param args arguments passed from init( ).
     */
    public void step( String[] args ) {
        hopCount++;
        // The agent is migrated to destination[1] after this method if
        // destination[1] is not null.
        if (hopCount < destination.length) {
            System.out.println( "agent( " + agentId + ") invoked step: " +
                    "hop count = " + hopCount +
                    ", next dest = " + destination[hopCount] + 
                    ", message = " + args[0] );
            args[0] = "Oi!";
            hop(destination[hopCount], "jump", args);
        } else {
            System.out.println( "agent( " + agentId + ") invoked fall: " +
                    "hop count = " + hopCount +
                    ", message = " + args[0] );
        }

    }

    /**
     * jump( ) is invoked upon an agent migration to destination[1] after
     * step( ) calls hop( ).
     *
     * @param args arguments passed from step( ).
     */
    public void jump( String[] args ) {
        hopCount++;
        // The agent is migrated to destination[2] after this method if destination[2]
        // is not null.
        if (hopCount < destination.length) {
            System.out.println( "agent( " + agentId + ") invoked jump: " +
                    "hop count = " + hopCount +
                    ", next dest = " + destination[hopCount] + 
                    ", message = " + args[0] );
            args[0] = "Bye!";
            hop(destination[hopCount], "fall", args);
        } else {
            System.out.println( "agent( " + agentId + ") invoked fall: " +
                    "hop count = " + hopCount +
                    ", message = " + args[0] );
        }
    }

    /**
     * fall( ) is invoked upon an agent migration to destination[2] after
     * jump( ) calls hop( ).
     *
     * @param args arguments passed from jump( ).
     */
    public void fall( String[] args ) {
        hopCount++;
        System.out.println( "agent( " + agentId + ") invoked fall: " +
                    "hop count = " + hopCount +
                    ", message = " + args[0] );
    }
}
