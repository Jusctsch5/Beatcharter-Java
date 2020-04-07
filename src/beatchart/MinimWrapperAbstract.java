package beatchart;

import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.spi.AudioRecordingStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

abstract public class MinimWrapperAbstract {

    Minim minim;
    AudioPlayer audioPlayer;
    AudioRecordingStream stream;
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
    }

    abstract public float FindBPM(File iFile);

}
