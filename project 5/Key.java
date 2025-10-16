/**
 * Key is used among Workers to obtain the /lock znode for non-interruptibly
 * accessing the /tasks znode and its children several times.
 */

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.AsyncCallback.DataCallback;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.AsyncCallback.StringCallback;
import org.apache.zookeeper.AsyncCallback.VoidCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.data.Stat;

public class Key {
    private ZooKeeper zk;                     // ZooKeeper connected to Workers
    private static Object syncObject = null;  // Used to suspend a worker

    /**
     * Is the constructor that accepts a worker's ZooKeeper object and sets up
     * a synchronization object with itself.
     * @param zk_init a calling worker's ZooKeeper object
     */
    public Key( ZooKeeper zk_init ) {
		this.zk = zk_init;
		syncObject = this;
    }

	/**
	 * lock( ) tries to create /lock znode in ZooKeeper. If successful, it
	 * returns to the caller. Otherwise, it sets a watch on /lock and sleeps on
	 * syncObject. When notified by the watch, it wakes up and tries to create
	 * /lock again until successful.
	 */
    public void lock( ) {
		while ( true ) {
			try {
				String lock = zk.create( "/lock",
						null,
						Ids.OPEN_ACL_UNSAFE,
						CreateMode.EPHEMERAL );
				
				// upon a successful creation of /lock, I got the lock.
				if ( lock != null && lock.equals( "/lock" ) ) {
					System.out.println( lock + " acquired" );
					return;
				}
				System.err.println( lock + " error" ); // shouldn't happen
				
				} catch( KeeperException keeperexception ) {
					// /lock has been already created by someone
					System.err.println( "/lock locked already by someone else" );
				try {
					Stat stat = zk.exists("/lock", lockWatcher);
					if (stat != null) {

						synchronized (this) {
							this.wait();
						}
						System.out.println("/lock notified");
					}
				} catch ( Exception another ) {
					another.printStackTrace();
				}
			} catch( Exception others ) { }
		}
    }


	// lockWatcher is set on /lock when lock( ) finds /lock already created.
    Watcher lockWatcher = new Watcher( ) {
		public void process( WatchedEvent event ) {
			System.out.println( event.toString( ) );
			if ( event.getType( ) == EventType.NodeDeleted ) {
				synchronized (Key.this) {
						Key.this.notify();
					}
				System.out.println( "/lock unlocked informed" );
			}
		}
    };

	/**
	 * unlock( ) deletes /lock znode, thus releasing the lock.
	 */
    public void unlock( ) {
		try {
			zk.delete( "/lock", 0 );
		} catch( Exception e ) {
			System.err.println( e.toString( ) );
			return;
		}
		System.out.println( "/lock released" );
    }
}
