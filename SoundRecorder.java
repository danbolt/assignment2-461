import javax.media.*;

import javax.swing.*;
import java.awt.Graphics;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

public class SoundRecorder extends JPanel
{
	public SoundRecorder()
	{
		//
	} //constructor
	
	public void paint(Graphics g)
	{
		//
	}
	
	public static void main(String[] args)
	{
		JFrame applicationFrame = new JFrame();
		applicationFrame.getContentPane().add(new SoundRecorder());
		
		applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		applicationFrame.setSize(300, 200);
		applicationFrame.setVisible(true);
	} //main

} //SoundRecorder class