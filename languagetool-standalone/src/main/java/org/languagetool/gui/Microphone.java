package org.languagetool.gui;

import javax.sound.sampled.*;

import java.io.Closeable;
import java.io.File;

/***************************************************************************
 * Microphone class that contains methods to capture audio from microphone
 *
 * @author Luke Kuza, Aaron Gokaslan
 ***************************************************************************/
public class Microphone implements Closeable{

    /**
     * TargetDataLine variable to receive data from microphone
     */
    private TargetDataLine targetDataLine;


    /**
     * Enum for current Microphone state
     */
    public enum CaptureState {
        PROCESSING_AUDIO, STARTING_CAPTURE, CLOSED;
    }

    /**
     * Variable for enum
     */
    CaptureState state;

    /**
     * Variable for the audios saved file type
     */
    private AudioFileFormat.Type fileType;

    /**
     * Variable that holds the saved audio file
     */
    private File audioFile;

    /**
     * Constructor
     *
     * @param fileType File type to save the audio in<br>
     *                 Example, to save as WAVE use AudioFileFormat.Type.WAVE
     */
    public Microphone(AudioFileFormat.Type fileType) {
        setState(CaptureState.CLOSED);
        setFileType(fileType);
        initTargetDataLine();
    }

    /**
     * Gets the current state of Microphone
     *
     * @return PROCESSING_AUDIO is returned when the Thread is recording Audio and/or saving it to a file<br>
     *         STARTING_CAPTURE is returned if the Thread is setting variables<br>
     *         CLOSED is returned if the Thread is not doing anything/not capturing audio
     */
    public CaptureState getState() {
        return state;
    }

    /**
     * Sets the current state of Microphone
     *
     * @param state State from enum
     */
    private void setState(CaptureState state) {
        this.state = state;
    }

    public File getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(File audioFile) {
        this.audioFile = audioFile;
    }

    public AudioFileFormat.Type getFileType() {
        return fileType;
    }

    public void setFileType(AudioFileFormat.Type fileType) {
        this.fileType = fileType;
    }

    public TargetDataLine getTargetDataLine() {
        return targetDataLine;
    }


    public void setTargetDataLine(TargetDataLine targetDataLine) {
        this.targetDataLine = targetDataLine;
    }

    /**
     * Initializes the target data line.
     */
    private void initTargetDataLine(){
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, getAudioFormat());
        try {
            setTargetDataLine((TargetDataLine) AudioSystem.getLine(dataLineInfo));
        } catch (LineUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

    }


    /**
     * Captures audio from the microphone and saves it a file
     *
     * @param audioFile The File to save the audio to
     * @throws LineUnavailableException
     */
    public void captureAudioToFile(File audioFile) throws LineUnavailableException {
        setState(CaptureState.STARTING_CAPTURE);
        setAudioFile(audioFile);

        if(getTargetDataLine() == null){
            initTargetDataLine();
        }

        //Get Audio
        new Thread(new CaptureThread()).start();


    }

    /**
     * Captures audio from the microphone and saves it a file
     *
     * @param audioFile The fully path (String) to a file you want to save the audio in
     * @throws LineUnavailableException
     */
    public void captureAudioToFile(String audioFile) throws LineUnavailableException {
        File file = new File(audioFile);
        captureAudioToFile(file);
    }


    /**
     * The audio format to save in
     *
     * @return Returns AudioFormat to be used later when capturing audio from microphone
     */
    public AudioFormat getAudioFormat() {
        float sampleRate = 8000.0F;
        //8000,11025,16000,22050,44100
        int sampleSizeInBits = 16;
        //8,16
        int channels = 1;
        //1,2
        boolean signed = true;
        //true,false
        boolean bigEndian = false;
        //true,false
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    /**
     * Opens the microphone, starting the targetDataLine.
     * If it's already open, it does nothing.
     */
    public void open(){
        if(getTargetDataLine()==null){
            initTargetDataLine();
        }
        if(!getTargetDataLine().isOpen() && !getTargetDataLine().isRunning() && !getTargetDataLine().isActive()){
            try {
                setState(CaptureState.PROCESSING_AUDIO);
                getTargetDataLine().open(getAudioFormat());
                getTargetDataLine().start();
            } catch (LineUnavailableException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }
        }

    }

    /**
     * Close the microphone capture, saving all processed audio to the specified file.<br>
     * If already closed, this does nothing
     */
    public void close() {
        if (getState() == CaptureState.CLOSED) {
        } else {
            getTargetDataLine().stop();
            getTargetDataLine().close();
            setState(CaptureState.CLOSED);
        }
    }

    /**
     * Thread to capture the audio from the microphone and save it to a file
     */
    private class CaptureThread implements Runnable {

        /**
         * Run method for thread
         */
        public void run() {
            try {
                AudioFileFormat.Type fileType = getFileType();
                File audioFile = getAudioFile();
                open();
                AudioSystem.write(new AudioInputStream(getTargetDataLine()), fileType, audioFile);
                //Will write to File until it's closed.
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}