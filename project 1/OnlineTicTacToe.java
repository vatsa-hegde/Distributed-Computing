import com.jcraft.jsch.*;
import java.util.Scanner;
import java.io.Console;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.util.HashSet;
import java.util.Set;
import java.net.BindException;


/**
 *
 * @author Shreevatsa Ganapathy Hegde
 */
public class OnlineTicTacToe implements ActionListener {

    private final int INTERVAL = 1000;         // 1 second
    private final int NBUTTONS = 9;            // #bottons
    private ObjectInputStream input = null;    // input from my counterpart
    private ObjectOutputStream output = null;  // output from my counterpart
    private JFrame window = null;              // the tic-tac-toe window
    private JButton[] button = new JButton[NBUTTONS]; // button[0] - button[9]
    private boolean[] myTurn = new boolean[1]; // T: my turn, F: your turn
    private String myMark = null;              // "O" or "X"
    private String yourMark = null;            // "X" or "O"
    int[][] winPatterns = {
        {0, 1, 2}, {3, 4, 5}, {6, 7, 8},        // rows
        {0, 3, 6}, {1, 4, 7}, {2, 5, 8},        // columns
        {0, 4, 8}, {2, 4, 6}                    // diagonals
    };

    boolean isBot = false; // true if the player is a bot  

    /**
     * Prints out the usage.
     */
    private static void usage( ) {
        System.err.
	    println( "Usage: java OnlineTicTacToe ipAddr ipPort(>=5000) [auto]" );
        System.exit( -1 );
    }

    /**
     * Prints out the track trace upon a given error and quits the application.
     * @param an exception 
     */
    private static void error( Exception e ) {
        e.printStackTrace();
        System.exit(-1);
    }

    /**
     * Starts the online tic-tac-toe game.
     * @param args[0]: my counterpart's ip address, args[1]: his/her port, (arg[2]: "auto")
     *        if args.length == 0, this Java program is remotely launched by JSCH.
     */
    public static void main( String[] args ) {

	if ( args.length == 0 ) {
	    // if no arguments, this process was launched through JSCH
	    try {
		OnlineTicTacToe game = new OnlineTicTacToe( );
	    } catch( IOException e ) {
		error( e );
	    }
	}
	else {
	    // this process wa launched from the user console.

	    // verify the number of arguments
	    if ( args.length != 2 && args.length != 3 ) {
		System.err.println( "args.length = " + args.length );
		usage( );
	    }

	    // verify the correctness of my counterpart address
	    InetAddress addr = null;
	    try {
		addr = InetAddress.getByName( args[0] );
	    } catch ( UnknownHostException e ) {
		error( e );
	    }
	    
	    // verify the correctness of my counterpart port
	    int port = 0;
	    try {
		port = Integer.parseInt( args[1] );
	    } catch (NumberFormatException e) {
		error( e );
	    }
	    if ( port < 5000 ) {
		usage( );
	    }
	    
	    // check args[2] == "auto"
	    if ( args.length == 3 && args[2].equals( "auto" ) ) {
		// auto play
		OnlineTicTacToe game = new OnlineTicTacToe( args[0] );
	    }
	    else { 
		// interactive play
		OnlineTicTacToe game = new OnlineTicTacToe( addr, port );
	    }
	}
    }

    /**
     * Is the constructor that is remote invoked by JSCH. It behaves as a server.
     * The constructor uses a Connection object for communication with the client.
     * It always assumes that the client plays first. 
     */
    public OnlineTicTacToe( ) throws IOException {
	// receive an ssh2 connection from a user-local master server.
	Connection connection = new Connection();
	input = connection.in;
	output = connection.out;

	// for debugging, always good to write debugging messages to the local file
	// don't use System.out that is a connection back to the client.
	PrintWriter logs = new PrintWriter( new FileOutputStream( "logs.txt" ) );
	logs.println( "Autoplay: got started." );
	logs.flush( );

	myMark = "X";   // auto player is always the 2nd.
	yourMark = "O"; 

    isBot = true; // this is the auto player

	// the main body of auto play.  
    myTurn[0] = false; // this is the auto player
    Set<Integer> set = new HashSet<>(); // to keep track of marked buttons
    while (true) {
            if (myTurn[0]) {
                int button =  getBestMove(set); // get the best move
                set.add(button);
                output.writeObject(button); // send the button id to the counterpart
                output.flush();
                logs.println("Autoplay marked" + button);
                logs.flush();
            }

            if (!myTurn[0]) {
                try {
                    int yourButton = (int) input.readObject(); // read the button id from the counterpart
                    set.add(yourButton); // add the button to the set
                    logs.println("Oppnent played " + yourButton);  // log the move
                    logs.flush();
                }
                catch (ClassNotFoundException e) {
                    error(e);
                }
            }
            myTurn[0] = !myTurn[0]; //Change turn
        }

    }

    /**
     * Is the constructor that, upon receiving the "auto" option,
     * launches a remote OnlineTicTacToe through JSCH. This
     * constructor always assumes that the local user should play
     * first. The constructor uses a Connection object for
     * communicating with the remote process.
     *
     * @param my auto counter part's ip address
     */
    public OnlineTicTacToe( String hostname ) {
        final int JschPort = 22;      // Jsch IP port

        Scanner keyboard = new Scanner( System.in );
        String username = null;
        String password = null;

	    // The JSCH establishment process is pretty much the same as Lab3.
        // Read the username and password from the console
        System.out.print("Username: ");
        username = keyboard.nextLine();
        Console console = System.console();
        password = new String(console.readPassword("Password: "));
        
        // The command to be executed on the remote server
        String cur_dir = System.getProperty("user.dir");
        String command = "java -cp " + cur_dir + "/jsch-0.1.54.jar:" + cur_dir + " OnlineTicTacToe";

        // establish an ssh2 connection to ip and run
        // Server there.
        Connection connection = new Connection( username, password,
						hostname, command );
        System.out.println("Connection established with " + hostname);

        // the main body of the master server
	    input = connection.in;
	    output = connection.out;   

	    // set up a window
	    makeWindow( true ); // I'm a former

        // start my counterpart thread
        Counterpart counterpart = new Counterpart();
        counterpart.start();
    }

    

    /**
     * Is the constructor that sets up a TCP connection with my counterpart,
     * brings up a game window, and starts a slave thread for listenning to
     * my counterpart.
     * @param my counterpart's ip address
     * @param my counterpart's port
     */
    public OnlineTicTacToe( InetAddress addr, int port ) {
        // set up a TCP connection with my counterpart
        System.out.println("Trying to Connect to " + addr + ":" + port);
        ServerSocket server = null;
        boolean isServer = false;
        boolean isBusy = false;
        // try to create a server socket
	    try {
	        server = new ServerSocket( port );
		    server.setSoTimeout(INTERVAL);
	    }catch ( BindException e){
            //BinedException is thrown when the port is already in use
            // meaning the server is already running
            isBusy = true;
        } catch ( Exception e ) {
	        error( e );
	    }

        Socket client = null;
	    while ( true ) {
            //check for localhost
            if(addr.getHostName().equals("localhost")){
                if(!isBusy){ 
                    // if the port is not busy meaning the server is not running, you are the server
                    // wait for a connection
                    try {
                        client = server.accept();
                    } catch (SocketTimeoutException e) {
                        // Timeout, continue waiting
                    } catch (IOException e) {
                        error(e);
                    }
                    // Check if a connection was established. If so, leave the loop
                    if (client != null) {
                    isServer = true;
                    break;
                    }
                }else{ 
                    // if the port is busy, Server is running, you are the client
                    // try to connect to the server
                    try {
                        client = new Socket( addr, port );
                    } catch (IOException e) {
                        // Connection failed, continue waiting
                    }
                    if (client != null) {
                    break;
                    }
                }
	      
	        } else {
                if (!isBusy) {
                    // if the port is not busy meaning the server is not running, you are the server
                    // wait for a connection
                    try {
                        client = server.accept();
                    } catch (SocketTimeoutException e) {
                        // Timeout, continue waiting
                    } catch (IOException e) {
                        error(e);
                    }
                    // Check if a connection was established. If so, leave the loop
                    if (client != null) {
                        isServer = true;
                        break;
                    }
                } 
                // if the port is busy, Server is running, you are the client
                // try to connect to the server
                try {
                    client = new Socket( addr, port );
                } catch (IOException e) {
                    // Connection failed, continue waiting
                }
                if (client != null) {
                    break;
                }
            
            }
        }
        try{
            System.out.println("Connected to " + client.getInetAddress() + ":" + client.getPort());
            // set up a window
            makeWindow( !isServer );
            // set up input and output streams
            output = new ObjectOutputStream( client.getOutputStream() );
            input = new ObjectInputStream( client.getInputStream() );
        } catch (Exception e){
            error(e);
        }
        // start my counterpart thread
        Counterpart counterpart = new Counterpart( );
        counterpart.start();
    }

    /**
     * Creates a 3x3 window for the tic-tac-toe game
     * @param true if this window is created by the former, (i.e., the
     *        person who starts first. Otherwise false.
     */
    private void makeWindow( boolean amFormer ) {
        myTurn[0] = amFormer;
        myMark = ( amFormer ) ? "O" : "X";    // 1st person uses "O"
        yourMark = ( amFormer ) ? "X" : "O";  // 2nd person uses "X"

        // create a window
        window = new JFrame("OnlineTicTacToe(" +
                ((amFormer) ? "former)" : "latter)" ) + myMark );
        window.setSize(300, 300);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLayout(new GridLayout(3, 3));

	// initialize all nine cells.
        for (int i = 0; i < NBUTTONS; i++) {
            button[i] = new JButton();
            window.add(button[i]);
            button[i].addActionListener(this);
        }

	// make it visible
        window.setVisible(true);
    }

    /**
     * Marks the i-th button with mark ("O" or "X")
     * @param the i-th button
     * @param a mark ( "O" or "X" )
     * @param true if it has been marked in success
     */
    private boolean markButton( int i, String mark ) {
	if ( button[i].getText( ).equals( "" ) ) {
	    button[i].setText( mark );
	    button[i].setEnabled( false );
	    return true;
	}
	return false;
    }

    /**
     * Checks which button has been clicked
     * @param an event passed from AWT 
     * @return an integer (0 through to 8) that shows which button has been 
     *         clicked. -1 upon an error. 
     */
    private int whichButtonClicked( ActionEvent event ) {
	for ( int i = 0; i < NBUTTONS; i++ ) {
	    if ( event.getSource( ) == button[i] )
		return i;
	}
	return -1;
    }

    /**
     * Checks if the i-th button has been marked with mark( "O" or "X" ).
     * @param the i-th button
     * @param a mark ( "O" or "X" )
     * @return true if the i-th button has been marked with mark.
     */
    private boolean buttonMarkedWith( int i, String mark ) {
	return button[i].getText( ).equals( mark );
    }

    /**
     * Pops out another small window indicating that mark("O" or "X") won!
     * @param a mark ( "O" or "X" )
     */
    private void showWon( String mark ) {
	JOptionPane.showMessageDialog( null, mark + " won!" );	
    }

    /**
     * Is called by AWT whenever any button has been clicked. You have to:
     * <ol>
     * <li> check if it is my turn,
     * <li> check which button was clicked with whichButtonClicked( event ),
     * <li> mark the corresponding button with markButton( buttonId, mark ),
     * <li> send this informatioin to my counterpart,
     * <li> checks if the game was completed with 
     *      buttonMarkedWith( buttonId, mark ) 
     * <li> shows a winning message with showWon( )
     */
    public void actionPerformed( ActionEvent event ) {
        if(!myTurn[0]){
            return;
        }
        int button = whichButtonClicked(event); // check which button was clicked
        if(button == -1) return;
        if(markButton(button, myMark)){
            try {
                output.writeObject(button); // send the button id to the counterpart
                output.flush();
                myTurn[0] = false; // change turn
            }catch(IOException e){
                error(e);
            }
            // Check if the current player has won
            if (checkWin(myMark)) {
                showWon(myMark);
                window.setEnabled(false); 
                restart();  // To restrat the game
                // System.exit(0);
            }
            // Check if the game is a draw
            if (checkDraw()) {
                JOptionPane.showMessageDialog(null, "It's a draw!");
                window.setEnabled(false);
                restart(); // To restrat the game
                // System.exit(0);
                return;
            }
        }
    }
    /**
     * Checks if the game is won with the given mark.
     * @param a mark ( "O" or "X" )
     * @return true if the game is completed with the given mark.
     */

    public boolean checkWin(String mark){
        for (int[] pattern : winPatterns) {
            if (buttonMarkedWith(pattern[0], mark) &&
                buttonMarkedWith(pattern[1], mark) &&
                buttonMarkedWith(pattern[2], mark)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the game is completed  in a draw with the given mark.
     * @param a mark ( "O" or "X" )
     * @return true if the game is completed with the given mark.
     */
    public boolean checkDraw(){
        for (int i = 0; i < NBUTTONS; i++) {
            if (button[i].getText().equals("")) {
                return false;
            }
        }
        return true;
    }

    /**
     * This is a reader thread that keeps reading fomr and behaving as my
     * counterpart.
     */
    private class Counterpart extends Thread {

	/**
	 * Is the body of the Counterpart thread.
	 */
        @Override
        public void run( ) {
        try{
            while(true){
                Object obj = input.readObject();
                if(obj instanceof Integer){
                    int button = (Integer) obj;
                    markButton(button, yourMark);
                
                 // Check if the current player has won
                    if(checkWin(yourMark)){
                        showWon(yourMark);
                        window.setEnabled(false);
                        restart(); // To restrat the game
                        // System.exit(0);
                        break;   
                    }
                    // Check if the game is a draw
                    if(checkDraw()){
                        JOptionPane.showMessageDialog(null, "It's a draw!");
                        window.setEnabled(false);
                        restart(); // To restrat the game
                        // System.exit(0);
                        break;
                    }
                    // Change turn
                    myTurn[0] = true;
                } 
            }
        }catch(Exception e){
            error(e);
        }
        }
    }

    /**
     * restarts feature.
     */
    private void restart() {
        // Ask the user whether he wants to restart the game
        int choice = JOptionPane.showOptionDialog(null, "<html>Wanna restart?</html>", "Game Over",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
        int opponentChoice = -1;
        if (!isBot)   {
            try {
                output.writeObject(choice); // send the choice to the counterpart
                output.flush();
            
            while(opponentChoice == -1){
                // Read the opponent's choice    
                    opponentChoice = (Integer) input.readObject(); 
                }
                }  catch(Exception e){
                    error(e);
                }
            if(opponentChoice != JOptionPane.NO_OPTION){
                JOptionPane.showMessageDialog(null, "<html>Opponent has chosen to stop the game</html>", "Game Over",
                        JOptionPane.PLAIN_MESSAGE);
                System.exit(0);
            }
        }  else {
            // If the player is a bot, set the  choice to YES
            choice = JOptionPane.YES_OPTION;
            output.writeObject(choice); // send the choice to the player
            output.flush();
        }

        // Checking user's choice
        if (choice == JOptionPane.YES_OPTION) {
            
            // Reset the game 
            for (int i = 0; i < NBUTTONS; i++) {
                button[i].setText("");
                button[i].setEnabled(true);
            }
            if (myTurn[0]) {
                window.setEnabled(true); // Enable the window for the player
            } else {
                window.setEnabled(false); // Disable the window for the player
            }
        } else if (choice == JOptionPane.NO_OPTION) {
            // Display the defined message and exit the game 
            JOptionPane.showMessageDialog(null, "<html>Thanks for playing!</html>", "Game Over",
                    JOptionPane.PLAIN_MESSAGE);
            System.exit(0);
        }
    }

    /**
     * Checks if there's a winning or blocking move available
     * @param mark the mark to check for ("O" or "X")
     * @return the index of the winning or blocking move, or -1 if none exists
     */
    private int getBestMove(Set<Integer> set) {
        // 1. Try to win with current move
        for (int[] pattern : winPatterns) {
            int move = findWinningMove(pattern, myMark, set);
            if (move != -1) return move;
        }

        // 2. Try to block opponent from winning
        for (int[] pattern : winPatterns) {
            int move = findWinningMove(pattern, yourMark, set);
            if (move != -1) return move;
        }

        // 3. Take center if it's available
        if (!set.contains(4)) return 4;

        // 4. Take a corner if available
        for (int i : new int[]{0, 2, 6, 8}) {
            if (!set.contains(i)) return i;
        }

        // 5. Take a side if no better options
        for (int i : new int[]{1, 3, 5, 7}) {
            if (!set.contains(i)) return i;
        }

        return -1; // No move possible (shouldn't happen during valid game)
    }


    /**
     * Checks if there's a winning or blocking move available
     * @param mark the mark to check for ("O" or "X")
     * @return the index of the winning or blocking move, or -1 if none exists
     */

    private int findWinningMove(int[] pattern, String mark, Set<Integer> set) {
        int count = 0;
        int emptyIndex = -1;
        
        for (int i : pattern) {
            String text = button[i] != null ? button[i].getText() : "";
            
            if (text.equals(mark)) {
                count++; // Count how many of our marks are in this pattern
            } else if (text.equals("") && !set.contains(i)) {
                emptyIndex = i; // Store the empty spot to possibly place a mark
            }
        }

        // If two spots are taken and one is empty, return that move
        return (count == 2 && emptyIndex != -1) ? emptyIndex : -1;
    }


}

