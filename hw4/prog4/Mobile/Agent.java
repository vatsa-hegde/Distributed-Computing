package Mobile;
import java.io.*;
import java.rmi.*;
import java.lang.reflect.*;
import com.hazelcast.core.*;
import com.hazelcast.map.IMap;

/**
 * Mobile.Agent is the base class of all user-define mobile agents. It carries
 * an agent identifier, the next host IP and port, the name of the function to
 * invoke at the next host, arguments passed to this function, its class name,
 * and its byte code. It runs as an independent thread that invokes a given
 * function upon migrating the next host.
 *
 * @author  Munehiro Fukuda
 * @version %I% %G%
 * @since   1.0
 */
public class Agent implements Serializable, Runnable {
    // live data to carry with the agent upon a migration
    protected int agentId        = -1;    // this agent's identifier
    private String _hostname     = null;  // the next host name to migrate
    private String _function     = null;  // the function to invoke upon a move
    private int _port            = 0;     // the next host port to migrate
    private String[] _arguments  = null;  // arguments pass to _function
    private String _classname    = null;  // this agent's class name
    private byte[] _bytecode     = null;  // this agent's byte code

    private transient HazelcastInstance hazelcastInstance;

    public void setHazelcastInstance(HazelcastInstance hz) {
        this.hazelcastInstance = hz;
    }

    public HazelcastInstance getHazelcastInstance() {
        return this.hazelcastInstance;
    }

    /**
     * setPort( ) sets a port that is used to contact a remote Mobile.Place.
     * 
     * @param port a port to be set.
     */
    public void setPort( int port ) {
	this._port = port;
    }

    public int getPort( ) {
        return _port;
    }

    public String getHost( ) {
        return _hostname;
    }

    public void setHost(String hostname) {
        this._hostname = hostname;
    }

    public void setFunction(String function) {
        this._function = function;
    }


    /**
     * setId( ) sets this agent identifier: agentId.
     *
     * @param id an idnetifier to set to this agent.
     */
    public void setId( int id ) {
	this.agentId = id;
    }

    /**
     * getId( ) returns this agent identifier: agentId.
     *
     * @param this agent's identifier.
     */
    public int getId( ) {
	return agentId;
    }

    /**
     * getByteCode( ) reads a byte code from the file whosename is given in
     * "classname.class".
     *
     * @param classname the name of a class to read from local disk.
     * @return a byte code of a given class.
     */
    public static byte[] getByteCode( String classname ) {
	// create the file name
	String filename = classname.replace('.', '/') + ".class";

	// allocate the buffer to read this agent's bytecode in
	File file = new File( filename );
	byte[] bytecode = new byte[( int )file.length( )];

	// read this agent's bytecode from the file.
	try {
	    BufferedInputStream bis =
		new BufferedInputStream( new FileInputStream( filename ) );
	    bis.read( bytecode, 0, bytecode.length );
	    bis.close( );
	} catch ( Exception e ) {
	    e.printStackTrace( );
	    return null;
	}

	// now you got a byte code from the file. just return it.
	return bytecode;	
    }

    /**
     * getByteCode( ) reads this agent's byte code from the corresponding file.
     *
     * @return a byte code of this agent.
     */
    public byte[] getByteCode( ) {
	if ( _bytecode != null ) // bytecode has been already read from a file
	    return _bytecode; 
	
	// obtain this agent's class name and file name
	_classname = this.getClass( ).getName( );
	_bytecode = getByteCode( _classname );

	return _bytecode;
    }

    /**
     * run( ) is the body of Mobile.Agent that is executed upon an injection
     * or a migration as an independent thread. run( ) identifies the method 
     * with a given function name and arguments and invokes it. The invoked
     * method may include hop( ) that transfers this agent to a remote host or
     * simply returns back to run( ) that termiantes the agent.
     */
    public void run( ) {
        try {
            System.out.println("Agent " + agentId + " running on " + _hostname + ", invoking: " + _function);

            // get class and method to invoke
            Class<?> cls = this.getClass();
            if (_arguments == null) {
                Method method = cls.getMethod(_function); 
                method.invoke(this); // invoke method with no arguments
            } else {
                Method method = cls.getMethod(_function, String[].class);
                method.invoke(this, (Object) _arguments);  // cast to Object for varargs
            }
        } catch (InvocationTargetException e) {
            if (!(e.getCause() instanceof ThreadDeath)) {
                e.getCause().printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * hop( ) transfers this agent to a given host, and invoeks a given
     * function of this agent.
     *
     * @param hostname the IP name of the next host machine to migrate
     * @param function the name of a function to invoke upon a migration
     */    
    public void hop( String hostname, String function ) {
	hop( hostname, function, null );
    }

    /**
     * hop( ) transfers this agent to a given host, and invoeks a given
     * function of this agent as passing given arguments to it.
     *
     * @param hostname the IP name of the next host machine to migrate
     * @param function the name of a function to invoke upon a migration
     * @param args     the arguments passed to a function called upon a 
     *                 migration.
     */
    @SuppressWarnings( "deprecation" )
    public void hop( String hostname, String function, String[] args ) {
        this._hostname = hostname;
        this._function = function;
        this._arguments = args;

        // get this agent's class name and byte code
        _classname = this.getClass().getName();
        _bytecode = getByteCode();
        // serialize this agent to a byte array
        byte[] entity = serialize();

        try {
            // create a place object to transfer this agent
            String url = "rmi://" + _hostname + ":" + _port + "/place";
            PlaceInterface remote = (PlaceInterface) Naming.lookup(url);
            boolean result = remote.transfer(_classname, _bytecode, entity); // transfer this agent
            if (!result) {
                System.err.println("Transfer failed to " + url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread.currentThread().stop();

    }

    /**
     * serialize( ) serializes this agent into a byte array.
     *
     * @return a byte array to contain this serialized agent.
     */
    private byte[] serialize( ) {
	try {
	    // instantiate an object output stream.
	    ByteArrayOutputStream out = new ByteArrayOutputStream( );
	    ObjectOutputStream os = new ObjectOutputStream( out );
	    
	    // write myself to this object output stream
	    os.writeObject( this );

	    return out.toByteArray( ); // conver the stream to a byte array
	} catch ( IOException e ) {
	    e.printStackTrace( );
	    return null;
	}
    }
}