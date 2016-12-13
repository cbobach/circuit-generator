package fairplay.generators;

import fairplay.gate.Gate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cbobach on 31-10-16.
 *
 * This class is only meant to be extended by CircuitGenerators.
 * It is used at a collection of common functionality across generators. 
 */
class ExpandableCircuitGenerator implements CircuitGenerator {

    private String lineFeed = "\r\n";

    String outputFile = null;
    int numberOfGates = 0;
    int numberOfWires = 0;
    int numberOfCircuitInputWiresConstructor = 0;
    int numberOfCircuitInputWiresEvaluator = 0;
    int numberOfCircuitOutputWires = 0;
    int numberOfCircuitOutputWiresConstructor = 0;
    int numberOfCircuitOutputWiresEvaluator = 0;
    List<Gate> circuit = new ArrayList<>();
    int firstOutputWire = 0;

    @Override
    public void generateCircuitFile() {
        //        Creating and writing to output file
        Path file = Paths.get(outputFile);
        try {
            Files.write(file, getByteHeader());
            Files.write(file, getByteCircuit(), StandardOpenOption.APPEND);

            System.out.println(new String(getByteHeader(), StandardCharsets.UTF_8));
            System.out.printf("SUCCESS: Generated circuit file: %s\r\n", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] getByteHeader() {
        String str = numberOfGates + " " +
                numberOfWires + " " +
                firstOutputWire +
                lineFeed +
                numberOfCircuitInputWiresConstructor + " " +
                numberOfCircuitInputWiresEvaluator + " " +
                numberOfCircuitOutputWires + " " +
                numberOfCircuitOutputWiresConstructor + " " +
                numberOfCircuitOutputWiresEvaluator + " " +
                lineFeed + lineFeed;

        return str.getBytes();
    }

    private byte[] getByteCircuit() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (Gate g:
                circuit) {
            String str = g.toFairPlayString() + lineFeed;
            byte[] bytes = str.getBytes();
            output.write(bytes);
        }

        return output.toByteArray();
    }
}
