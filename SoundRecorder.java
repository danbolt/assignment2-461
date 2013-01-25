import javax.media.*;

import javax.swing.*;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

public class SoundRecorder extends JPanel
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
	} //constructor
	
	private void openAudioFile() throws java.io.IOException, java.net.MalformedURLException, javax.media.MediaException
	{
		FileDialog fd = new FileDialog ((JFrame)getParent(), "Open media file...", FileDialog.LOAD);
		fd.setVisible(true);
		File f = new File(fd.getDirectory(), fd.getFile());
		
		audioPlayer = Manager.createRealizedPlayer(f.toURI().toURL());
	}

	public static void main(String[] args)
	{
		JMenuBar bar = new JMenuBar();
		JFrame applicationFrame = new JFrame();
		applicationFrame.getContentPane().add(new SoundRecorder(bar));
		
		applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		applicationFrame.setJMenuBar(bar);
		applicationFrame.setSize(300, 200);
		applicationFrame.setTitle("Sound Tool");
		applicationFrame.setResizable(false);
		applicationFrame.setLocation(300, 300);
		applicationFrame.setVisible(true);
	} //main

} //SoundRecorder class