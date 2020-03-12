package beatchart;

import java.io.File;

public class BeatchartDecoder {
    /**
     * BeatchartDecoder - perform decoding of beats and audio to generate
     *  an object that can be handled by the StepBuilder
     * @param song - File to decode beats + audio from
     * @return SongObject processed object describing the song
     */

    public Beatchart DecodeSong(File song, float bpm) {

        MinimWrapper wrapper = new MinimWrapper();
        wrapper.init(song.getName());
        if (bpm == 0) {
            bpm = wrapper.FindBPM();
        }

        Beatchart object = new Beatchart(song, bpm);
        return object;
    }
}
