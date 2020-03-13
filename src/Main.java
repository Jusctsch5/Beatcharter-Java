import beatchart.Beatchart;
import beatchart.BeatchartDecoder;
import builders.StepBuilder;
import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;
import java.io.File;

public class Main {

    // argument parser

    public static boolean containsArg(String[] args, String argname) {
        for(String s : args) {
            if( s.toLowerCase().equals(argname) ) return true;
        }
        return false;
    }
    public static void main(String[] args) {

        // create Options object
        Options options = new Options();
        options.addOption(new Option( "help", "print this message" ));
        options.addOption(new Option( "i", "input", true,"input file to generate steps for" ));
        options.addOption(new Option( "duration", "optional duration to generate steps for in the song. default - whole duration" ));
        options.addOption(new Option( "bpm", "optional manual bpm to set, otherwise will attempt to figure it out" ));
        options.addOption(new Option( "encoding", "type of encoding to perform (e.g., sm, ssc, beatmania" ));

        // Parse out options and handle them.
        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();
        CommandLine line = null;
        try {
            // parse the command line arguments
            line = parser.parse( options, args );
        }
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
            formatter.printHelp("Beatcharter", options);
            return;
        }

        if (line.hasOption("input") == false) {
            System.out.println("Need to provide input file.");
            formatter.printHelp("Beatcharter", options);
            return;
        }

        String inputFileName = line.getOptionValue("input");
        File inputFile = new File(inputFileName);
        if (inputFile == null || inputFile.exists() == false || inputFile.isFile() == false) {
            System.out.println(inputFileName + " is not an existing file.");
            formatter.printHelp("Beatcharter", options);
            return;
        }

        Integer bpm;
        if (line.hasOption("bpm")) {
            String bpmString = line.getOptionValue("bpm");
            bpm = Integer.parseInt(bpmString);
        } else {
            bpm = 0;
        }

        Integer startTime = 0;
        String outputFileName = FilenameUtils.removeExtension(inputFileName) + ".sm";
        String outputDir = "./";

        BeatchartDecoder decoder = new BeatchartDecoder();
        Beatchart chart = decoder.DecodeSong(inputFile, bpm);

        System.out.println("Generating SM for " + inputFile.toString() +   " to "  + outputDir + outputFileName);

        for (StepBuilder.StepDifficulty diff : StepBuilder.StepDifficulty.values()) {
            StepBuilder.Build(chart, diff, StepBuilder.ControllerType.PAD, false, true ,false);
        }
    }
}
