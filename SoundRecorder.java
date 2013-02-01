import javax.media.*;
import javax.media.format.AudioFormat;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;
import javax.media.control.StreamWriterControl;

import java.lang.Thread;

import javax.swing.*;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;
import java.text.DecimalFormat;
import javax.swing.BorderFactory;
import javax.swing.border.*;

import java.util.Vector;
import java.util.Date;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

public class SoundRecorder extends JPanel implements ActionListener
{
	private enum PlayerState
	{
		INVALID_STATE,
		NO_MEDIA_LOADED,
		MEDIA_LOADED,
		MEDIA_PLAYING,
		MEDIA_PLAYING_FORWARD,
		MEDIA_PLAYING_REVERSE,
		RECORDING
	}

	/* JMF stuff  */
	private Player audioPlayer = null;
	private final int sliderMax = 1000;
	
	/* Data co-related with recording */
	private Processor p = null;
	private DataSource source = null;
	private CaptureDeviceInfo di = null;
	private DataSink filewriter = null;
	private static int duration = 0;
	private static String MediaFileName = "";

	/* GUI Component classes  */
	private JMenuBar topMenuBar = null;
	private JSlider slider = null;
	private JLabel leftText = null;
	private JLabel rightText = null;
	private JButton playButton = null;
        private JButton stopButton = null;
        private JButton fastForwardButton = null;
        private JButton rewindButton = null;
        private JButton recordButton = null;

	private PlayerState state = PlayerState.INVALID_STATE;

	public SoundRecorder(JMenuBar newBar)
	{
		try
		{
			initCaptureDevice();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Error initalizing sound recorder");
		}

		topMenuBar = newBar;
		{
			JMenu fileMenu = new JMenu("File");
			fileMenu.setMnemonic(KeyEvent.VK_F);
			JMenuItem openFile = new JMenuItem("Open Media...");
			openFile.setActionCommand("openFile");
			openFile.addActionListener(this);
			JMenuItem closeFile = new JMenuItem("Close Media...");
			closeFile.setActionCommand("closeFile");
			closeFile.addActionListener(this);
			JMenuItem exitApp = new JMenuItem("Exit");
			exitApp.setActionCommand("exit");
			exitApp.addActionListener(this);
			fileMenu.add(openFile);
			fileMenu.add(closeFile);
			fileMenu.addSeparator();
			fileMenu.add(exitApp);
			topMenuBar.add(fileMenu);

			JMenu editMenu = new JMenu("Edit");
			editMenu.setMnemonic(KeyEvent.VK_E);
			topMenuBar.add(editMenu);
			
			JMenu optionsMenu = new JMenu("Options");
			optionsMenu.setMnemonic(KeyEvent.VK_O);
			topMenuBar.add(optionsMenu);

			JMenu helpMenu = new JMenu("Help");
			helpMenu.setMnemonic(KeyEvent.VK_H);
			topMenuBar.add(helpMenu);
		}
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JPanel topSection = new JPanel();
		topSection.setLayout(new BoxLayout(topSection, BoxLayout.X_AXIS));
		JPanel bottomSection = new JPanel();
		bottomSection.setLayout(new BoxLayout(bottomSection, BoxLayout.X_AXIS));
		
		leftText = new JLabel("0:0", SwingConstants.CENTER);
		rightText = new JLabel("0:0", SwingConstants.CENTER);
		leftText.setBorder(BorderFactory.createLoweredBevelBorder());
		rightText.setBorder(BorderFactory.createLoweredBevelBorder());
		slider = new JSlider(0, sliderMax, 0);
		topSection.add(leftText);
		topSection.add(slider);
		topSection.add(rightText);

		playButton = new JButton("Play");
		playButton.setActionCommand("playSong");
		playButton.addActionListener(this);
		bottomSection.add(playButton);
                stopButton = new JButton("Stop");
                stopButton.setActionCommand("stopSong");
                stopButton.addActionListener(this);
		bottomSection.add(stopButton);
		fastForwardButton = new JButton("Forward");
		fastForwardButton.setActionCommand("fastForward");
		fastForwardButton.addActionListener(this);
		bottomSection.add(fastForwardButton);
		rewindButton = new JButton("Rewind");
		rewindButton.setActionCommand("rewind");
		rewindButton.addActionListener(this);
		bottomSection.add(rewindButton);
		recordButton = new JButton("Record");
		recordButton.setActionCommand("startRecording");
		recordButton.addActionListener(this);
		bottomSection.add(recordButton);
		
		add(topSection);
		add(bottomSection);

		state = PlayerState.NO_MEDIA_LOADED;

		new Thread(new Updater(this)).start();
	} //constructor
	
	/* ---- THIS METHOD initCaptureDevice() was not written by Daniel Savage.
	 * ---- It was taken from the SimpleAudioRecorder.java file provided on
	 * ---- the Connex page.
	 */
	private void initCaptureDevice() throws Exception
	{
		//Query the device manager for available audio caputure devices which support
		//linear, 44100Hz, 16 bit, stereo audio capture
		Vector deviceList = CaptureDeviceManager.getDeviceList(new AudioFormat(AudioFormat.LINEAR, 44100, 16, 2));
		if (deviceList.size() > 0)
		{
			di = (CaptureDeviceInfo)deviceList.firstElement();
		}
		else
		{
			// Exit if such a device is not found
			System.exit(-1);
		}

		try
		{
			// Create a processor to convert from raw format to a file format
			// Notice that we are NOT starting the datasources, but letting the
			//  processor take care of this for us.
			p = Manager.createProcessor(di.getLocator());
		}
		catch (NoProcessorException ex)
		{
			System.exit(-1);
		}
		
		
		//Configure the processor and wait till it is finished configuring
		p.configure();
		waitForState(p, Processor.Configured);
		
		//Set content descriptor - pay attention to what formats can go in what containers
		p.setContentDescriptor(new FileTypeDescriptor(FileTypeDescriptor.WAVE));
		
		//Initiate realization of the processor and wait for it to be realized
		p.realize();
		waitForState(p, Processor.Realized);
		
		//Get the data output so we can output it to a file
		source = p.getDataOutput();
	}
	
	/* ---- THIS METHOD waitForState(Player, int) was not written by Daniel Savage.
	 * ---- It was taken from the SimpleAudioRecorder.java file provided on
	 * ---- the Connex page.
	 */
	private void waitForState(Player player, int state) {
    	
		// Fast abort if state is already the desired one
    		if (player.getState() == state) {
        		return;
    		}

    		long startTime = new Date().getTime(); //could also use System.currentTimeMillis()

    		long timeout = 10 * 1000;

    		final Object waitListener = new Object();

    		ControllerListener cl = new ControllerListener() {

        		public void controllerUpdate(ControllerEvent ce) {
            			synchronized (waitListener) {
                			waitListener.notifyAll();
            			}
        		}
    		};
    
		try {
        		player.addControllerListener(cl);

        		// Make sure we wake up every 500ms to check for timeouts and in case we miss a signal
        		synchronized (waitListener) {
            			while (player.getState() != state && new Date().getTime() - startTime < timeout) {
                			try {
                    				waitListener.wait(500);
                			} catch (InterruptedException ex) {
                    				System.err.println("Interrupted Exception!");
                			}
            			}
        		}
    		} finally {
        		// No matter what else happens, we want to remove this
        		player.removeControllerListener(cl);
    		}
	}

	public void actionPerformed(ActionEvent ex)
	{
		if ("openFile".equals(ex.getActionCommand()))
		{
			openMediaFile();
		}
		else if ("closeFile".equals(ex.getActionCommand()))
		{
			closeMediaFile();
		}
		else if ("playSong".equals(ex.getActionCommand()))
		{
			playMediaFile();
		}
		else if ("pauseSong".equals(ex.getActionCommand()))
		{
			pauseMediaFile();
		}
		else if ("fastForward".equals(ex.getActionCommand()))
		{
			fastForwardMediaFile();
		}
		else if ("rewind".equals(ex.getActionCommand()))
		{
			rewindMediaFile();
		}
		else if ("stopSong".equals(ex.getActionCommand()))
		{
			stopMediaFile();
		}
		else if ("startRecording".equals(ex.getActionCommand()))
		{
			startRecording();
		}
		else if ("exit".equals(ex.getActionCommand()))
		{
			System.exit(0);
		}
		else
		{
			System.out.println("'" + ex.getActionCommand() + "' has not been initalized yet");
		}
	} //actionPerformed

	private void openMediaFile()
	{
		try
		{
			FileDialog fd = new FileDialog ((JFrame)(SwingUtilities.getWindowAncestor(this)), "Open media file...", FileDialog.LOAD);
			fd.setVisible(true);
			File f = new File(fd.getDirectory(), fd.getFile());
			
			audioPlayer = Manager.createRealizedPlayer(f.toURI().toURL());
			
			state = PlayerState.MEDIA_LOADED;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	} //openAudioFile
	
	private void closeMediaFile()
	{
		if (state == PlayerState.MEDIA_PLAYING || state == PlayerState.MEDIA_PLAYING_FORWARD || state == PlayerState.MEDIA_PLAYING_REVERSE)
		{
			audioPlayer.stop();
			audioPlayer.close();
		}

		audioPlayer = null;
		
		state = PlayerState.NO_MEDIA_LOADED;
	} //closeMediaFile()

	private void playMediaFile()
	{
		if (state == PlayerState.MEDIA_LOADED)
		{
			audioPlayer.setRate(1.0f);
			audioPlayer.start();

			state = PlayerState.MEDIA_PLAYING;
		}
		else if (state == PlayerState.MEDIA_PLAYING_FORWARD || state == PlayerState.MEDIA_PLAYING_REVERSE)
		{
			state = PlayerState.MEDIA_PLAYING;
			
			audioPlayer.stop();
			audioPlayer.setRate(1.0f);
			audioPlayer.start();
		}
	}
	
	private void pauseMediaFile()
	{
		if (state == PlayerState.MEDIA_PLAYING || state == PlayerState.MEDIA_PLAYING_FORWARD || state == PlayerState.MEDIA_PLAYING_REVERSE)
		{
			audioPlayer.stop();

			state = PlayerState.MEDIA_LOADED;
		}
	}

	private void stopMediaFile()
	{
		if (state == PlayerState.MEDIA_PLAYING || state == PlayerState.MEDIA_PLAYING_FORWARD || state == PlayerState.MEDIA_PLAYING_REVERSE)
		{
			audioPlayer.stop();
			audioPlayer.setMediaTime(new Time(0));

			state = PlayerState.MEDIA_LOADED;
		}
		else if (state == PlayerState.RECORDING)
		{
			p.stop();
			p.close();
			
			try
			{
				//exhaust buffer
				Thread.sleep(1000);
			}
			catch (Exception e)
			{
				//
			}
			
			try
			{
				//Stop recording to the file and close it
				filewriter.stop();
			}
			catch (Exception e)
			{
				state = PlayerState.INVALID_STATE;
			}

			filewriter.close();
			
			state = PlayerState.NO_MEDIA_LOADED;
		}
	}
	
	private void fastForwardMediaFile()
	{
		if (state == PlayerState.MEDIA_PLAYING || state == PlayerState.MEDIA_PLAYING_REVERSE)
		{
			state = PlayerState.MEDIA_PLAYING_FORWARD;
			
			audioPlayer.stop();
			audioPlayer.setRate(1.5f);
			audioPlayer.start();
		}
	}
	
	private void rewindMediaFile()
	{
		if (state == PlayerState.MEDIA_PLAYING || state == PlayerState.MEDIA_PLAYING_FORWARD)
		{
			state = PlayerState.MEDIA_PLAYING_REVERSE;
			
			audioPlayer.stop();
			audioPlayer.setRate(-2.5f);
			audioPlayer.start();
		}
	}
	
	private void startRecording()
	{
		if (state == PlayerState.MEDIA_PLAYING || state == PlayerState.MEDIA_PLAYING_FORWARD || state == PlayerState.MEDIA_PLAYING_REVERSE)
		{
			audioPlayer.stop();
			audioPlayer.setMediaTime(new Time(0));
			audioPlayer.close();
		}

		state = PlayerState.RECORDING;

		FileDialog fd = new FileDialog((JFrame)(SwingUtilities.getWindowAncestor(this)), "Save recorded file to...", FileDialog.SAVE);
		fd.setVisible(true);
		File f = new File(fd.getDirectory(), fd.getFile());
		System.out.println("fd.getDirectory(): " + fd.getDirectory());
                System.out.println("fd.getFile(): " + fd.getFile());
                MediaFileName = fd.getDirectory() + fd.getFile();
		
		// create a File protocol MediaLocator with the location of the
		// file to which the data is to be written
		try
		{
			MediaLocator dest = new MediaLocator("file://"+MediaFileName);
			// create a datasink to do the file writing
			filewriter = Manager.createDataSink(source, dest);
		}
		catch (NoDataSinkException ex)
		{
			System.out.println("error1");
			System.exit(-1);
		}
		catch (SecurityException ex)
		{
			System.out.println("error3");
			System.exit(-1);
		}

		StreamWriterControl swc = (StreamWriterControl)
		p.getControl("javax.media.control.StreamWriterControl");

		if (swc != null)
		{
			swc.setStreamSizeLimit(5000000);
		}

		p.start();

		try
		{
			filewriter.open();
			filewriter.start();
		}
		catch (Exception e)
		{
			state = PlayerState.INVALID_STATE;
		}
	}
	
	public void updateGUI(Time currentSpot, Time currentDuration, float sliderPosition)
	{
		leftText.setText("Position: " + ((int)currentSpot.getSeconds() / 60) + ":" + ((int)currentSpot.getSeconds() % 60));
		rightText.setText("Length: " + ((int)currentDuration.getSeconds() / 60) + ":" + ((int)currentDuration.getSeconds() % 60));

		switch (state)
		{
			case INVALID_STATE:
			playButton.setEnabled(false);
			stopButton.setEnabled(false);
			fastForwardButton.setEnabled(false);
			rewindButton.setEnabled(false);
			recordButton.setEnabled(false);
			leftText.setText("Position: -:-");
			rightText.setText("Length: -:-");
			break;
			case NO_MEDIA_LOADED:
			playButton.setEnabled(false);
			stopButton.setEnabled(false);
			fastForwardButton.setEnabled(false);
			rewindButton.setEnabled(false);
			recordButton.setEnabled(true);
			leftText.setText("Position: -:-");
			rightText.setText("Length: -:-");
			break;
			case MEDIA_LOADED:
			playButton.setEnabled(true);
			stopButton.setEnabled(false);
			fastForwardButton.setEnabled(true);
			rewindButton.setEnabled(true);
			recordButton.setEnabled(true);
			break;
			case MEDIA_PLAYING:
			playButton.setEnabled(false);
			stopButton.setEnabled(true);
			fastForwardButton.setEnabled(true);
			rewindButton.setEnabled(true);
			recordButton.setEnabled(false);
			break;
			case MEDIA_PLAYING_FORWARD:
			playButton.setEnabled(true);
			stopButton.setEnabled(true);
			fastForwardButton.setEnabled(false);
			rewindButton.setEnabled(true);
			recordButton.setEnabled(false);
			break;
			case MEDIA_PLAYING_REVERSE:
			playButton.setEnabled(true);
			stopButton.setEnabled(true);
			fastForwardButton.setEnabled(true);
			rewindButton.setEnabled(false);
			recordButton.setEnabled(false);
			break;
			case RECORDING:
			playButton.setEnabled(false);
			stopButton.setEnabled(true);
			fastForwardButton.setEnabled(false);
			rewindButton.setEnabled(false);
			recordButton.setEnabled(false);
			rightText.setText("Length: -:-");
			break;
			default:
			break;
		}

		slider.setValue((int)(sliderPosition * sliderMax));
		repaint();
	}

	public static void main(String[] args)
	{
		JMenuBar bar = new JMenuBar();
		JFrame applicationFrame = new JFrame();
		applicationFrame.getContentPane().add(new SoundRecorder(bar));
		
		applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		applicationFrame.setJMenuBar(bar);
		applicationFrame.setSize(500, 300);
		applicationFrame.setTitle("Sound Tool");
		applicationFrame.setResizable(false);
		applicationFrame.setLocation(300, 300);
		applicationFrame.pack();
		applicationFrame.setVisible(true);
	} //main

	private class Updater implements Runnable
	{
		private SoundRecorder recorder = null;
		
		public Updater(SoundRecorder newRecorder)
		{
			recorder = newRecorder;
		}

		public void run()
		{
			while(true)
			{
				if (recorder.state == PlayerState.INVALID_STATE)
				{
					System.out.println("INVALID STATE");
					continue;
				}
				else if (recorder.state == PlayerState.NO_MEDIA_LOADED)
				{
					recorder.updateGUI(new Time(0), new Time(0), 0.0f);
				}
				else if (recorder.state == PlayerState.MEDIA_LOADED)
				{
					recorder.updateGUI(new Time(0), recorder.audioPlayer.getDuration(), (float)(recorder.audioPlayer.getMediaTime().getSeconds()/recorder.audioPlayer.getDuration().getSeconds()));
				}
				else if (recorder.state == PlayerState.MEDIA_PLAYING || recorder.state == PlayerState.MEDIA_PLAYING_FORWARD || recorder.state == PlayerState.MEDIA_PLAYING_REVERSE)
				{
					recorder.updateGUI(recorder.audioPlayer.getMediaTime(), recorder.audioPlayer.getDuration(), (float)(recorder.audioPlayer.getMediaTime().getSeconds()/recorder.audioPlayer.getDuration().getSeconds()));
				}
				else if (recorder.state == PlayerState.RECORDING)
				{
					recorder.updateGUI(recorder.p.getMediaTime(), new Time(0), 0.0f);
				}

				try
				{
					Thread.sleep(100);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

} //SoundRecorder class