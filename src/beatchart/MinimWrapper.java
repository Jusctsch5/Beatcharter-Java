package beatchart;

import ddf.minim.AudioPlayer;
import ddf.minim.AudioSample;
import ddf.minim.Minim;
import ddf.minim.analysis.BeatDetect;

public class MinimWrapper {

    Minim minim;
    AudioPlayer song;
    AudioSample fullSong;
    BeatDetect beat;
    float eRadius;

    public void init(String iFileName)
    {
        // size(200, 200, P3D);
        minim = new Minim(this);
        // song = minim.loadFile(iFileName, 2048);
        // song.play();

        // Load the whole song in memory
        fullSong = minim.loadSample(iFileName);
        // a beat detection object song SOUND_ENERGY mode with a sensitivity of 10 milliseconds
        // beat = new BeatDetect();

        //ellipseMode(RADIUS);
        eRadius = 20;
    }

    /**
     * FindBPM - finds bpm of song
     * @return float detected bpm
     */
    public float FindBPM()
    {
        fullSong.trigger();
        return 0;
    }
}
