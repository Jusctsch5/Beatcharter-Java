package beatchart;

import ddf.minim.Minim;

import java.util.ArrayList;
import gnu.trove.list.array.TFloatArrayList;

public class MinimWrapperPhroot {

    public static boolean DEBUG_STEPS = false;
    public static float MAX_BPM = 170f, MIN_BPM = 70f, BPM_SENSITIVITY = 0.05f, STARTSYNC = 0.0f;
    public static double TAPSYNC = -0.11;
    public static boolean USETAPPER = false, HARDMODE = false, UPDATESM = false;

    public static final int KICKS = 0, ENERGY = 1, SNARE = 2, HAT = 3;

    
    // collected song data
    private final TFloatArrayList[] manyTimes = new TFloatArrayList[4];
    private final TFloatArrayList[] fewTimes = new TFloatArrayList[4];

    TFloatArrayList calculateDifferences(TFloatArrayList arr, float timeThreshold) {
        TFloatArrayList diff = new TFloatArrayList();
        int currentlyAt = 0;
        while(currentlyAt < arr.size() - 1) {
            float mytime = arr.getQuick(currentlyAt);
            int oldcurrentlyat = currentlyAt;
            for(int i=currentlyAt+1;i<arr.size();i++) {
                float diffcheck = arr.getQuick(i) - mytime;
                if( diffcheck >= timeThreshold ) {
                    diff.add(diffcheck);
                    currentlyAt = i;
                    break;
                }
            }
            if( oldcurrentlyat == currentlyAt ) break;
        }
        return diff;
    }

    float getDifferenceAverage(TFloatArrayList arr) {
        float avg = 0f;
        for(int i=0;i<arr.size()-1;i++) {
            avg += Math.abs(arr.getQuick(i+1) - arr.getQuick(i));
        }
        if( arr.size() <= 1 ) return 0f;
        return avg / arr.size()-1;
    }

    float getMostCommon(TFloatArrayList arr, float threshold, boolean closestToInteger) {
        ArrayList<TFloatArrayList> values = new ArrayList<>();
        for(int i=0;i<arr.size();i++) {
            float val = arr.get(i);
            // check for this value in our current lists
            boolean notFound = true;
            for(int j=0;j<values.size();j++) {
                TFloatArrayList tal = values.get(j);
                for(int k=0;k<tal.size();k++) {
                    float listValue = tal.get(k);
                    if( Math.abs(listValue - val) < threshold ) {
                        notFound = false;
                        tal.add(val);
                        break;
                    }
                }
                if( notFound == false ) break;
            }
            // if it wasn't found, start a new list
            if( notFound ) {
                TFloatArrayList newList = new TFloatArrayList();
                newList.add(val);
                values.add(newList);
            }
        }
        // get the longest list
        int longest = 0;
        TFloatArrayList longestList = null;
        for(int i=0;i<values.size();i++) {
            TFloatArrayList check = values.get(i);
            if( check.size() > longest ||
                    check.size() == longest && getDifferenceAverage(check) < getDifferenceAverage(longestList) ) {
                longest = check.size();
                longestList = check;
            }
        }
        if( longestList == null ) return -1f;
        if( longestList.size() == 1 && values.size() > 1 ) {
            // one value only, no average needed.. but what to pick?
            // just pick the smallest one... or integer, if we want that instead
            if( closestToInteger ) {
                float closestIntDiff = 1f;
                float result = arr.getQuick(0);
                for(int i=0;i<arr.size();i++) {
                    float diff = Math.abs(Math.round(arr.getQuick(i)) - arr.getQuick(i));
                    if( diff < closestIntDiff ) {
                        closestIntDiff = diff;
                        result = arr.getQuick(i);
                    }
                }
                return result;
            } else {
                float smallest = 99999f;
                for(int i=0;i<arr.size();i++) {
                    if( arr.getQuick(i) < smallest ) smallest = arr.getQuick(i);
                }
                return smallest;
            }
        }
        // calculate average
        float avg = 0f;
        for(int i=0;i<longestList.size();i++) {
            avg += longestList.get(i);
        }
        return avg / longestList.size();
    }

    public float getBestOffset(float timePerBeat, TFloatArrayList times, float groupBy) {
        TFloatArrayList offsets = new TFloatArrayList();
        for(int i=0;i<times.size();i++) {
            offsets.add(times.getQuick(i) % timePerBeat);
        }
        return getMostCommon(offsets, groupBy, false);
    }

    public void AddCommonBPMs(TFloatArrayList common, TFloatArrayList times, float doubleSpeed, float timePerSample) {
        float commonBPM = 60f / getMostCommon(calculateDifferences(times, doubleSpeed), timePerSample, true);
        if( commonBPM > MAX_BPM ) {
            common.add(commonBPM * 0.5f);
        } else if( commonBPM < MIN_BPM / 2f ) {
            common.add(commonBPM * 4f);
        } else if( commonBPM < MIN_BPM ) {
            common.add(commonBPM * 2f);
        } else common.add(commonBPM);
    }

}
