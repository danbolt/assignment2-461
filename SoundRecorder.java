import javax.media.*;

import java.lang.Thread;

import javax.swing.*;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

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
		MEDIA_PLAYING_REVERSE
	}

	/* JMF stuff  */
	private Player audioPlayer = null;
	private final int sliderMax = 1000;

	/* GUI Component classes  */
	private JMenuBar topMenuBar = null;
	private JSlider slider = null;
	private JLabel leftText = null;
	private JLabel rightText = null;

	private PlayerState state = PlayerState.INVALID_STATE;


	public SoundRecorder(JMenuBar newBar)
	{
		topMenuBar = newBar;
		{
			JMenu fileMenu = new JMenu("File");
			fileMenu.setMnemonic(KeyEvent.VK_F);
			JMenuItem openFile = new JMenuItem("Open Media...");
			openFile.setActionCommand("openFile");
			openFile.addActionListener(this);
			fileMenu.add(openFile);
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
		
		leftText = new JLabel("LEFT TEXT");
		rightText = new JLabel("RIGHT TEXT");
		slider = new JSlider(0, sliderMax, sliderMax/2);
		topSection.add(leftText);
		topSection.add(slider);
		topSection.add(rightText);
		
		JButton button;
		button = new JButton("Play");
		button.setActionCommand("playSong");
		button.addActionListener(this);
		bottomSection.add(button);
                button = new JButton("Pause");
                button.setActionCommand("pauseSong");
                button.addActionListener(this);
		bottomSection.add(button);
		button = new JButton("Forward");
		button.setActionCommand("fastForward");
		button.addActionListener(this);
		bottomSection.add(button);
		button = new JButton("Rewind");
		button.setActionCommand("rewind");
		button.addActionListener(this);
		bottomSection.add(button);
		button = new JButton("Record");
		button.setActionCommand("startRecording");
		button.addActionListener(this);
		bottomSection.add(button);
		
		add(topSection);
		add(bottomSection);

		state = PlayerState.NO_MEDIA_LOADED;

		new Thread(new Updater(this)).start();
	} //constructor

	public void actionPerformed(ActionEvent ex)
	{
		if ("openFile".equals(ex.getActionCommand()))
		{
			openMediaFile();
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
		if (state == PlayerState.MEDIA_PLAYING)
		{
			audioPlayer.stop();
			audioPlayer.setMediaTime(new Time(0));

			state = PlayerState.MEDIA_LOADED;
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
	
	public void updateGUI(Time currentSpot, Time currentDuration, float sliderPosition)
	{
		leftText.setText(((int)currentSpot.getSeconds() / 60) + ":" + ((int)currentSpot.getSeconds() % 60));
		rightText.setText(((int)currentDuration.getSeconds() / 60) + ":" + ((int)currentDuration.getSeconds() % 60));
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
		applicationFrame.setSize(500, 200);
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
				else if (recorder.state == PlayerState.MEDIA_LOADED)
				{
					recorder.updateGUI(recorder.audioPlayer.getMediaTime(), recorder.audioPlayer.getDuration(), (float)(recorder.audioPlayer.getMediaTime().getSeconds()/recorder.audioPlayer.getDuration().getSeconds()));
				}
				else if (recorder.state == PlayerState.MEDIA_PLAYING || recorder.state == PlayerState.MEDIA_PLAYING_FORWARD || recorder.state == PlayerState.MEDIA_PLAYING_REVERSE)
				{
					recorder.updateGUI(recorder.audioPlayer.getMediaTime(), recorder.audioPlayer.getDuration(), (float)(recorder.audioPlayer.getMediaTime().getSeconds()/recorder.audioPlayer.getDuration().getSeconds()));
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