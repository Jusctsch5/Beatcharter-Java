package beatchart;

import org.junit.Test;

import java.io.File;
import java.util.logging.Level;

import static java.lang.Math.abs;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MinimWrapperTest {

    MinimWrapper wrapper;
    @Test
    public void BPMTest() throws Exception {
        float bpm = 0;
        wrapper = new MinimWrapper();
        wrapper.init();
        BeatchartLogger.logger.setLevel(Level.ALL);
        CheckBPMOfFile("./samples/sample1.mp3",     184.1f);
        //CheckBPMOfFile("./samples/dixieland.mp3",   114.0f);
        //CheckBPMOfFile("./samples/batleh.mp3",      111.0f);
        // CheckBPMOfFile("./samples/evelina.mp3",     120.0f);
        //CheckBPMOfFile("./samples/grandfather.mp3", 115.0f);
        //CheckBPMOfFile("./samples/tenting.mp3",     144.0f);
    }

    private void CheckBPMOfFile(String fileName, float correctBPM) throws Exception {
        File inFile = new File(fileName);
        assertNotNull(inFile);
        assertTrue(inFile.exists());
        AssertWithinAllowedOffset(wrapper.FindBPM(inFile), correctBPM, 2.0f);
    }

    private void AssertWithinAllowedOffset(float foundBPM, float correctBPM, float offsetBPM) {
        float diff = abs(foundBPM - correctBPM);
        assertThat(foundBPM, allOf(greaterThan(correctBPM - offsetBPM), lessThan(correctBPM + offsetBPM)));
    }
};
