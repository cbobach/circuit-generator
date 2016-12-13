package fairplay;

import fairplay.generators.IdentityCircuitGenerator;
import fairplay.generators.OptimizedIdentityCircuitGenerator;
import fairplay.generators.TagCircuitGenerator;

/**
 * Created by cbobach on 27-10-16.
 *
 * Generator for generating Fairplay circuit files
 */
public class Generator {
    private static final String TAG_CIRCUIT = "-tag";
    private static final String ID_CIRCUIT = "-id";
    private static final String ID_CIRCUIT_OPT = "-idopt";

    private static String[] args;

    /**
     * Main for running fairplay generator
     * @param args [method] [outputFile] [inputSize] [securityParameter]
     */
    public static void main(String... args) {
        Generator.args = args;

        if (args.length < 1)
            printHelp();

        String method = args[0];

        switch (method) {
            case TAG_CIRCUIT:
                if (args.length != 4)
                    printHelp();

                String outputFile = args[1];
                int inputSize = Integer.parseInt(args[2]);
                int securityParameter = Integer.parseInt(args[3]);

                new TagCircuitGenerator(inputSize, securityParameter, outputFile).generateCircuitFile();
                System.exit(1);
            case ID_CIRCUIT:
                if (args.length != 4)
                    printHelp();

                outputFile = args[1];
                inputSize = Integer.parseInt(args[2]);
                securityParameter = Integer.parseInt(args[3]);

                new IdentityCircuitGenerator(inputSize, securityParameter, outputFile).generateCircuitFile();
                System.exit(1);
            case ID_CIRCUIT_OPT:
                if (args.length != 4)
                    printHelp();

                outputFile = args[1];
                inputSize = Integer.parseInt(args[2]);
                securityParameter = Integer.parseInt(args[3]);

                new OptimizedIdentityCircuitGenerator(inputSize, securityParameter, outputFile).generateCircuitFile();
                System.exit(1);
            default:
                printHelp();
                break;
        }
    }

    private static void printHelp() {
        System.out.println("");
        System.out.println("Use: mvn exec:java -Dexec.args='[method] [outputFile] [inputSize] [securityParameter]'");
        System.out.println("\t -tag\t [outputFile] [inputSize] [securityParameter]");
        System.out.println("\t -id\t [outputFile] [inputSize] [securityParameter]");
        System.out.println("\t -idopt\t [outputFile] [inputSize] [securityParameter]");
        System.out.println("");
        System.out.println("Args.length: " + args.length);
        System.exit(0);
    }
}