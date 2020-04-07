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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.util.logging.Level.*;

public class MinimWrapper extends MinimWrapperAbstract{

    /**
     * FindBPM - finds bpm of song
     * @return float detected bpm
     */
    public float FindBPM(File iFile)
    {
        float BPM_SENSITIVITY = 0.05f;

        String filename = iFile.getAbsolutePath();
        if (iFile.exists() == false) {
            System.out.println("file: " + filename + " does not exist");
        }

        int fftSize = 512;
        stream  = minim.loadFileStream(filename, fftSize, false);
        if (stream == null) {
            System.out.println("Unable to load file as stream object " + filename);
        }

        stream.play();


        BeatDetect beatDetect = new BeatDetect(BeatDetect.FREQ_ENERGY, fftSize, stream.getFormat().getSampleRate());
        beatDetect.setSensitivity(BPM_SENSITIVITY);

        // Generate a Fourier Transform on the stream. (Complexity N^2)
        // Comments from: http://code.compartmental.net/minim/fft_class_fft.html
        // As an example, if you construct an FFT with a timeSize of 1024 and and a sampleRate of 44100 Hz,
        // then the spectrum will contain values for frequencies below 22010 Hz, which is the Nyquist frequency (half the sample rate).
        // If you ask for the value of band number 5, this will correspond to a frequency band centered on 5/1024 * 44100 = 0.0048828125 * 44100 = 215 Hz.
        // The width of that frequency band is equal to 2/1024, expressed as a fraction of the total bandwidth of the spectrum.
        // The total bandwidth of the spectrum is equal to the Nyquist frequency, which in this case is 22050, so the bandwidth is equal to about 50 Hz.
        // The function getFreq() allows you to query the spectrum with a frequency in Hz and the function getBandWidth() will return the bandwidth in Hz of each frequency band in the spectrum.
        float sampleRate = stream.getFormat().getSampleRate();
        BeatchartLogger.logger.log(INFO, "Using Fourier Transform with fftSize:" + fftSize + " sampleRate:" + sampleRate);
        FFT fft = new FFT(fftSize, sampleRate);

        // Figure out how many samples are in the stream so we can allocate the correct number of spectra.
        float songTime = stream.getMillisecondLength() / 1000f;
        int totalSamples = (int)(songTime * stream.getFormat().getSampleRate());
        float timePerFFTSample = fftSize / stream.getFormat().getSampleRate();
        BeatchartLogger.logger.log(INFO, "SongTime:" + songTime + " totalSamples:" + totalSamples + " timePerFFTSample:" + timePerFFTSample);

        int totalChunks = (totalSamples / fftSize) + 1;
        int samplesPerChunk = totalSamples / totalChunks;
        float timePerChunk = songTime / totalChunks;
        BeatchartLogger.logger.log(INFO, "totalChunks:" + totalChunks + " timePerChunk:" + timePerChunk + " samplesPerChunk:" + samplesPerChunk);

        // Create the buffer we use for reading from the stream.
        MultiChannelBuffer buffer = new MultiChannelBuffer(fftSize, stream.getFormat().getChannels());

        BeatchartLogger.logger.log(INFO, "Performing Beat Detection for file:" + iFile.getName());
        int lowFreq = fft.freqToIndex(300f);
        int highFreq = fft.freqToIndex(3000f);
        float currentBPM = 0;

        SubtatumMap kickSubtatumMap = new SubtatumMap();
        SubtatumMap hatSubtatumMap = new SubtatumMap();
        SubtatumMap snareSubtatumMap = new SubtatumMap();
        SubtatumMap onsetSubtatumMap = new SubtatumMap();

        SubtatumMap allSubtatumMap = new SubtatumMap();

        // Perform onset detection within chunks.
        float lastKick = 0.0f;
        float lastHat = 0.0f;
        float lastSnare = 0.0f;
        float lastAnything = 0.0f;
        for (int chunkIdx = 0; chunkIdx < totalChunks; chunkIdx++) {
            stream.read(buffer);
            float[] data = buffer.getChannel(0);
            float time = chunkIdx * timePerChunk;

            // now analyze the left channel
            beatDetect.detect(data, time);
            fft.forward(data);

            // store basic percussion times
            boolean isKick = beatDetect.isKick();
            boolean isHat = beatDetect.isHat();
            boolean isSnare = beatDetect.isSnare();
            boolean isOnset = beatDetect.isOnset();

            if (isKick) {
                if (lastKick != 0.0) {
                    float possibleBPM = 60 / (time - lastKick);
                    kickSubtatumMap.AddSubtatumToMap(possibleBPM);
                }
                lastKick = time;
            }
            if (isHat) {
                if (lastHat != 0.0) {
                    float possibleBPM = 60 / (time - lastHat);
                    hatSubtatumMap.AddSubtatumToMap(possibleBPM);
                }
                lastHat = time;
            }
            if (isSnare)  {
                if (lastSnare != 0.0) {
                    float possibleBPM = 60 / (time - lastSnare);
                    snareSubtatumMap.AddSubtatumToMap(possibleBPM);
                }
                lastSnare = time;
            }
            if (isSnare || isHat || isKick) {
                if (lastAnything != 0.0) {
                    float possibleBPM = 60 / (time - lastAnything);
                    allSubtatumMap.AddSubtatumToMap(possibleBPM);
                }
                lastAnything = time;
            }

            // BeatchartLogger.logger.log(INFO, "Time:" + time + " isKick:" + isKick + " isHat:" + isHat + " isSnare:" + isSnare + " isOnset:" + isOnset);
        }

        float kickBPM = kickSubtatumMap.GetMostCommonSubtatumInCommonRange().mid;
        float hatBPM = hatSubtatumMap.GetMostCommonSubtatumInCommonRange().mid;
        float snareBPM = snareSubtatumMap.GetMostCommonSubtatumInCommonRange().mid;
        float allBPM = allSubtatumMap.GetMostCommonSubtatumInCommonRange().mid;

        float kickBPM_C = kickSubtatumMap.GetMostCommonSubtatumInCollapsedMap().mid;
        float hatBPM_C = hatSubtatumMap.GetMostCommonSubtatumInCollapsedMap().mid;
        float snareBPM_C = snareSubtatumMap.GetMostCommonSubtatumInCollapsedMap().mid;
        float allBPM_C = allSubtatumMap.GetMostCommonSubtatumInCollapsedMap().mid;

        BeatchartLogger.logger.log(INFO, " kick:"   + kickBPM  +   " kickCollapsed:"   + kickBPM_C);
        BeatchartLogger.logger.log(INFO, " hat:"    + hatBPM   +   " hatCollapsed:"    + hatBPM_C);
        BeatchartLogger.logger.log(INFO, " snare:"  + snareBPM +   " snareCollapsed:"  + snareBPM_C);
        BeatchartLogger.logger.log(INFO, " all:"    + allBPM   +   " allCollapsed:"    + allBPM_C);

        // BeatchartLogger.logger.log(INFO, "kickMap:" + kickSubtatumMap.SubtatumMapToString());
        // BeatchartLogger.logger.log(INFO, "hatMap:" +  hatSubtatumMap.SubtatumMapToString());
        // BeatchartLogger.logger.log(INFO, "snareMap:" +  snareSubtatumMap.SubtatumMapToString());
        // BeatchartLogger.logger.log(INFO, "allMap:" +  allSubtatumMap.SubtatumMapToString());

        return kickBPM;
    }
};
