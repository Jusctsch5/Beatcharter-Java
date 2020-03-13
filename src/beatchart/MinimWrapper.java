package beatchart;

import ddf.minim.AudioPlayer;
import ddf.minim.AudioSample;
import ddf.minim.Minim;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.spi.AudioRecordingStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class MinimWrapper {

    Minim minim;
    AudioPlayer audioPlayer;
    AudioRecordingStream audioRecordingStream;
    BeatDetect beatDetect;
    float eRadius;

     // Required methods for using Minim.
    public String sketchPath( String fileName ) {
        return fileName;
    }

    public InputStream createInput(String fileName ) {
        try {
            return new FileInputStream(new File(fileName));
        } catch(Exception e) {
            return null;
        }
    }

    public void init()
    {
        minim = new Minim(this);

        /*
        audioPlayer = minim.loadFile(filename, 2048);
        if (audioPlayer == null) {
            System.out.println("Unable to load file as AudioPlayer song " + filename);
        }
        audioPlayer.play();
        */


        // a beat detection object song SOUND_ENERGY mode with a sensitivity of 10 milliseconds
        // beatDetect = new BeatDetect();

        //ellipseMode(RADIUS);
        eRadius = 20;
    }

    public enum FindBPMStrategy
    {
        AutoStep;
    }
    /**
     * FindBPM - finds bpm of song
     * @return float detected bpm
     */
    public float FindBPM(File iFile)
    {
        String filename = iFile.getAbsolutePath();
        if (iFile.exists() == false) {
            System.out.println("file: " + filename + " does not exist");
        }

        int fftSize = 512;
        audioRecordingStream  = minim.loadFileStream(filename, fftSize, false);
        if (audioRecordingStream == null) {
            System.out.println("Unable to load file as AudioRecordingStream object " + filename);
        }

        audioRecordingStream.play();


        //   fullSong.trigger();
        return 0;
    }
};
