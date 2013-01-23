
import javax.media.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * A Simple audio player class for playing audio files using JMF.
 * This class is able to play any audio formats that the JMF supports, 
 * including (but not limited to:) mp3, rmf, aiff, wav, au, and midi.
 * See java.sun.com/products/java-media/jmf/2.1.1/formats.html for
 * more information on supported file formats in JMF.
 */
public class SimpleAudioPlayer {

    /**
     * The Player object which is used to play an audio file.
     */
    private Player audioPlayer = null;

    /**
     * Creates a new instance of the simple audio player.
     * @param url The url of the audio file to play.
     * @throws IOException Indicates a problem opening the file.
     * @throws NoPlayerException JMF was unable to create a player for
     * the file type specified.
     * @throws CannotRealizeException Indicates a problem in realizing the
     * player.
     */
    public SimpleAudioPlayer(URL url) throws IOException, NoPlayerException, 
        CannotRealizeException {
        audioPlayer = Manager.createRealizedPlayer(url);
    }

    /**
     * Creates a new instance of the simple audio player.
     * @param file The audio file to play.
     * @throws IOException Indicates a problem opening the file.
     * @throws NoPlayerException JMF was unable to create a player for
     * the file type specified.
     * @throws CannotRealizeException Indicates a problem in realizing the
     * player.
     */
    public SimpleAudioPlayer(File file) throws IOException, NoPlayerException, 
        CannotRealizeException {
        this(file.toURI().toURL());
    }

    /**
     * Plays the audio file.
     */
    public void play() {
        audioPlayer.start();
    }

    /**
     * Stops and closes the audio file.
     */
    public void stop() {
        audioPlayer.stop();
        audioPlayer.close();
    }

    /**
     * Prints a usage message to System.out for how to use this class
     * through the command line.
     */
    public static void printUsage() {
        System.out.println("Usage: java SimpleAudioPlayer audioFile");
    }

    /**
     * Allows the user to run the class through the command line.
     * Only one argument is allowed, which is the file name of 
     * the audio file.
     */
    public static void main(String[] args) {
        try {
            if (args.length == 1) {
                /* Creates and uses a file reference for the audio file,
                   if a url reference is desired, then this line needs to
                   change.
                */
                File audioFile = new File(args[0]);
                SimpleAudioPlayer player = new SimpleAudioPlayer(audioFile);
                
                System.out.println();
                System.out.println("-> Playing file '" + 
                                   audioFile.getAbsolutePath() + "'");
                System.out.println("   Press the Enter key to exit");
                player.play();

                // wait for the user to press Enter to proceed.
                System.in.read();
                System.out.println("-> Exiting");
                player.stop();
            } else {
                printUsage();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.exit(0);
    }
}
