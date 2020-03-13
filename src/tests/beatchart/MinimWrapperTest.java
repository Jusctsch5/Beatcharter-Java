package beatchart;

import org.junit.Test;

import java.io.File;

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
        CheckBPMOfFile("./samples/batleh.mp3",      111.0f);
        CheckBPMOfFile("./samples/dixie.mp3",       114.0f);
        CheckBPMOfFile("./samples/evelina.mp3",     120.0f);
        CheckBPMOfFile("./samples/grandfather.mp3", 115.0f);
        CheckBPMOfFile("./samples/tenting.mp3",     144.0f);
    }

    private void CheckBPMOfFile(String fileName, float correctBPM) throws Exception {
        File inFile = new File(fileName);
        assertNotNull(inFile);
        assertTrue(inFile.exists());
        AssertWithinOneBPM(wrapper.FindBPM(inFile), 111);
    }

    private void AssertWithinOneBPM(float foundBPM, float correctBPM) {
        float diff = abs(foundBPM - correctBPM);
        assertThat(foundBPM, allOf(greaterThan(correctBPM-1), lessThan(correctBPM+1)));
    }
};
