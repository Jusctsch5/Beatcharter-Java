package beatchart;

import ddf.minim.AudioPlayer;
import ddf.minim.AudioSample;
import ddf.minim.Minim;
import ddf.minim.MultiChannelBuffer;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;
import ddf.minim.spi.AudioRecordingStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class MinimWrapper {

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
        float BPM_SENSITIVITY = 0.05f;
        int fftSize = 512;

        String filename = iFile.getAbsolutePath();
        if (iFile.exists() == false) {
            System.out.println("file: " + filename + " does not exist");
        }

        stream  = minim.loadFileStream(filename, fftSize, false);
        if (stream == null) {
            System.out.println("Unable to load file as stream object " + filename);
        }

        stream.play();
        BeatDetect beatDetect = new BeatDetect(BeatDetect.FREQ_ENERGY, fftSize, stream.getFormat().getSampleRate());
        beatDetect.setSensitivity(BPM_SENSITIVITY);

        // Generate a Fourier Transform on the stream.
        FFT fft = new FFT(fftSize, stream.getFormat().getSampleRate());

        // Create the buffer we use for reading from the stream.
        MultiChannelBuffer buffer = new MultiChannelBuffer(fftSize, stream.getFormat().getChannels());

        // Figure out how many samples are in the stream so we can allocate the correct number of spectra.
        float songTime = stream.getMillisecondLength() / 1000f;
        int totalSamples = (int)(songTime * stream.getFormat().getSampleRate());
        float timePerSample = fftSize / stream.getFormat().getSampleRate();
        int totalChunks = (totalSamples / fftSize) + 1;

        System.out.println("Performing Beat Detection...");

        int lowFreq = fft.freqToIndex(300f);
        int highFreq = fft.freqToIndex(3000f);
        for(int chunkIdx = 0; chunkIdx < totalChunks; ++chunkIdx) {
            stream.read(buffer);
            float[] data = buffer.getChannel(0);
            float time = chunkIdx * timePerSample;
            // now analyze the left channel
            beatDetect.detect(data, time);
            fft.forward(data);
            // fft processing
            float avg = fft.calcAvg(300f, 3000f);
            float max = 0f;
            for(int b=lowFreq; b<=highFreq; b++) {
                float bandamp = fft.getBand(b);
                if( bandamp > max ) max = bandamp;
            }

            // store basic percussion times
            boolean isKick = beatDetect.isKick();
            boolean isHat = beatDetect.isHat();
            boolean isSnare = beatDetect.isSnare();
            boolean isOnset = beatDetect.isOnset();

            System.out.println("Time:" + time + " isKick:" + isKick + " isHat:" + isHat + " isSnare:" + isSnare + " isOnset:" + isOnset);
        }
        //   fullSong.trigger();
        return 0;
    }
};
