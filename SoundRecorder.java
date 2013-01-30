import javax.media.*;

import javax.swing.*;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

public class SoundRecorder extends JPanel implements ActionListener
{
	/* JMF classes  */
	private Player audioPlayer = null;
	/* END JMF classes */

	/* GUI Component classes  */
	private JMenuBar topMenuBar = null;
	/* END GUI Component classes */
	
	private enum PlayerState
	{
		INVALID_STATE,
		MEDIA_LOADED,
		MEDIA_PLAYING
	}

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

		JSlider audioSlider = new JSlider(0, 1000, 0);
		add(audioSlider);
		
		JButton button;
		button = new JButton("Play");
		button.setActionCommand("playSong");
		button.addActionListener(this);
		add(button);
                button = new JButton("Stop");
                button.setActionCommand("stopSong");
                button.addActionListener(this);
		add(button);
		button = new JButton("Forward");
		button.setActionCommand("fastForward");
		button.addActionListener(this);
		add(button);
		button = new JButton("Rewind");
		button.setActionCommand("rewind");
		button.addActionListener(this);
		add(button);
		button = new JButton("Record");
		button.setActionCommand("startRecording");
		button.addActionListener(this);
		add(button);
	} //constructor

	public void actionPerformed(ActionEvent ex)
	{
		try
		{
			if ("openFile".equals(ex.getActionCommand()))
			{
				openAudioFile();
			}
			else if ("playSong".equals(ex.getActionCommand()))
			{
				audioPlayer.start();
			}
			else if ("stopSong".equals(ex.getActionCommand()))
			{
				audioPlayer.stop();
			}
			else
			{
				System.out.println("'" + ex.getActionCommand() + "' has not been initalized yet");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	} //actionPerformed

	private void openAudioFile() throws java.io.IOException, java.net.MalformedURLException, javax.media.MediaException
	{
		FileDialog fd = new FileDialog ((JFrame)(SwingUtilities.getWindowAncestor(this)), "Open media file...", FileDialog.LOAD);
		fd.setVisible(true);
		File f = new File(fd.getDirectory(), fd.getFile());
		
		audioPlayer = Manager.createRealizedPlayer(f.toURI().toURL());
	} //openAudioFile

	public static void main(String[] args)
	{
		JMenuBar bar = new JMenuBar();
		JFrame applicationFrame = new JFrame();
		applicationFrame.getContentPane().add(new SoundRecorder(bar));
		
		applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		applicationFrame.setJMenuBar(bar);
		applicationFrame.setSize(350, 200);
		applicationFrame.setTitle("Sound Tool");
		applicationFrame.setResizable(false);
		applicationFrame.setLocation(300, 300);
		applicationFrame.pack();
		applicationFrame.setVisible(true);
	} //main

} //SoundRecorder class