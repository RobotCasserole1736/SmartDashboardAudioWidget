package edu.wpi.first.smartdashboard.gui.elements;

import edu.wpi.first.smartdashboard.gui.elements.bindings.AbstractValueWidget;
import edu.wpi.first.smartdashboard.properties.ColorProperty;
import edu.wpi.first.smartdashboard.properties.Property;
import edu.wpi.first.smartdashboard.properties.StringProperty;
import edu.wpi.first.smartdashboard.types.DataType;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Nick Dunne
 */
public class AudioWidget extends AbstractValueWidget {

    /**
	 * 
	 */
	private static final long serialVersionUID = -8707963446918948908L;

	public static final String NAME = "Audio Widget";
    
	public static final DataType[] TYPES = {DataType.BOOLEAN};
    public final ColorProperty colorOnTrue = new ColorProperty(this, "Color to show when true", Color.GREEN);
    public final ColorProperty colorOnFalse = new ColorProperty(this, "Color to show when false", Color.RED);
    private JPanel valueField;
    private boolean value;

    private String audioPath = null;
    
    public final StringProperty pathProperty = new StringProperty(this, "Audio Path", "N/A");

    @Override
    public void init() {
        setPreferredSize(new Dimension(160, 120));
        audioPath = pathProperty.getValue();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        JLabel nameLabel = new JLabel(getFieldName());
        valueField = new JPanel();
        valueField.setPreferredSize(new Dimension(10, 10));

        add(valueField);
        add(nameLabel);
        revalidate();
        repaint();
    }

    @Override
    public void propertyChanged(Property property) {
        if (property == pathProperty) {
            audioPath = pathProperty.getValue();
        }
    }

    @Override
    public void setValue(final boolean value) {
    	this.value = value;
        valueField.setBackground(value ? colorOnTrue.getValue() : colorOnFalse.getValue());
        repaint();
        if(value && audioPath.compareTo("N/A") != 0)
        {
        	AudioThread audio = new AudioThread();
        	audio.start();
        }
    }
    
    //Sound playback
    public class AudioThread extends Thread{
    	public AudioThread(){
    		super("SD Audio Playback Thread");
    	}
    	
    	@Override
    	public void run(){
    		final int BUFFER_SIZE = 128000;
    	    File soundFile = null;
    	    AudioInputStream audioStream = null;
    	    AudioFormat audioFormat;
    	    SourceDataLine sourceLine = null;

	        try {
	            soundFile = new File(audioPath);
	        } catch (Exception e) {
	            e.printStackTrace();
	            System.exit(1);
	        }

	        try {
	            audioStream = AudioSystem.getAudioInputStream(soundFile);
	        } catch (Exception e){
	            e.printStackTrace();
	            System.exit(1);
	        }

	        audioFormat = audioStream.getFormat();

	        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
	        try {
	            sourceLine = (SourceDataLine) AudioSystem.getLine(info);
	            sourceLine.open(audioFormat);
	        } catch (LineUnavailableException e) {
	            e.printStackTrace();
	            System.exit(1);
	        } catch (Exception e) {
	            e.printStackTrace();
	            System.exit(1);
	        }

	        sourceLine.start();

	        int nBytesRead = 0;
	        byte[] abData = new byte[BUFFER_SIZE];
	        while (nBytesRead != -1) {
	            try {
	                nBytesRead = audioStream.read(abData, 0, abData.length);
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	            if (nBytesRead >= 0) {
	                @SuppressWarnings("unused")
	                int nBytesWritten = sourceLine.write(abData, 0, nBytesRead);
	            }
	        }

	        sourceLine.drain();
	        sourceLine.close();
    	}
    }
}