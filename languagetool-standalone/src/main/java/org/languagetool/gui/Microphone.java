package org.languagetool.gui;

import javax.sound.sampled.*;

import java.io.Closeable;
import java.io.File;

public class Microphone implements Closeable{
    private TargetDataLine targetDataLine;

    public enum CaptureState {
        PROCESSING_AUDIO, STARTING_CAPTURE, CLOSED;
    }
    CaptureState state;

    private AudioFileFormat.Type fileType;

    private File audioFile;

    public Microphone(AudioFileFormat.Type fileType) {
        setState(CaptureState.CLOSED);
        setFileType(fileType);
        initTargetDataLine();
    }

    public CaptureState getState() {
        return state;
    }

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

    public void captureAudioToFile(File audioFile) throws LineUnavailableException {
        setState(CaptureState.STARTING_CAPTURE);
        setAudioFile(audioFile);

        if(getTargetDataLine() == null){
            initTargetDataLine();
        }
        //Get Audio
        new Thread(new CaptureThread()).start();
    }

    public void captureAudioToFile(String audioFile) throws LineUnavailableException {
        File file = new File(audioFile);
        captureAudioToFile(file);
    }

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

    public void open(){
        if(getTargetDataLine() == null){
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

    public void close() {
        if (getState() == CaptureState.CLOSED) {
        } else {
            getTargetDataLine().stop();
            getTargetDataLine().close();
            setState(CaptureState.CLOSED);
        }
    }

    private class CaptureThread implements Runnable {
        public void run() {
            try {
                AudioFileFormat.Type fileType = getFileType();
                File audioFile = getAudioFile();
                open();
                AudioSystem.write(new AudioInputStream(getTargetDataLine()), fileType, audioFile);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}