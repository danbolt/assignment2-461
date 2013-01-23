import javax.media.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

public class ExampleJMF
{
	private Player audioPlayer = null;
	
	public ExampleJMF(URL url) throws IOException, NoPlayerException, CannotRealizeException
	{
		audioPlayer = Manager.createRealizedPlayer(url);
	}

	public ExampleJMF(File file) throws IOException, NoPlayerException, CannotRealizeException
	{
		this(file.toURI().toURL());
	}
	
	public void play()
	{
		audioPlayer.start();
	}
	
	public void stop()
	{
		audioPlayer.stop();
		audioPlayer.close();
	}

	public static void main(String[] args)
	{
		System.out.println("Audioplayer test started");

                ExampleJMF example;
		File audioFile = new File(args[0]);
		
		try
		{
			example = new ExampleJMF(audioFile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		
		example.play();

		//example.stop();
	}
}
