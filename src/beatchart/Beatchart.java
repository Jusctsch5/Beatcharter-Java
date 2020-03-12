package beatchart;

import java.io.File;

public class Beatchart {
    String name;
    File file;
    float bpm;
    float songStartOffsetSeconds;
    float songEndSeconds;

    public Beatchart(File iFile, float iBPM) {
        name = iFile.toString();
        file = iFile;
        bpm = iBPM;
        songStartOffsetSeconds = 0;
        songEndSeconds = 0;
    }

}
