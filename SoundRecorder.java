import javax.media.*;

import javax.swing.*;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

public class SoundRecorder extends JPanel
{
	//GUI Component classes
	JMenuBar topMenuBar;

	public SoundRecorder(JMenuBar newBar)
	{
		topMenuBar = newBar;

		//add menus to topMenuBar
		{
			JMenu fileMenu = new JMenu("File");
			fileMenu.setMnemonic(KeyEvent.VK_F);
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
	
	public void paint(Graphics g)
	{
		//
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