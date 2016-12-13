package fairplay.parsers;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import fairplay.gate.GateConstructor;
import fairplay.common.InputGateType;


public class CUDAParser implements CircuitParser<List<GateConstructor>> {

	private File circuitFile;
	private int numberOfInputs;
	private int numberOfOutputs;
	private int numberOfANDGates;
	private int numberOfWires;
	private String headerLine;

	public CUDAParser(File circuitFile) {
		this.circuitFile = circuitFile;
	}

	public List<List<GateConstructor>> getGates() {
		List<List<GateConstructor>> layersOfGates = new ArrayList<List<GateConstructor>>();
		boolean firstLine = true;

		InputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(circuitFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, Charset.defaultCharset());
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

		try {
			String line;
			//hack to skip first line
			List<GateConstructor> currentLayer = null;
			while ((line = bufferedReader.readLine()) != null) {
				if (line.isEmpty()){
				} else if (firstLine) {
					headerLine = line;
					String[] split = headerLine.split(" ");
					numberOfInputs = Integer.parseInt(split[0]);
					numberOfInputs = Integer.parseInt(split[1]);
					numberOfWires = Integer.parseInt(split[2]);
					numberOfANDGates = Integer.parseInt(split[5]);


					firstLine = false;
				} else if (line.startsWith("*")) {
					currentLayer = new ArrayList<GateConstructor>();
					layersOfGates.add(currentLayer);
				} else {
					// Parse each gate line and count numberOfNonXORGates
					GateConstructor g = new GateConstructor(line, InputGateType.CUDA);
					currentLayer.add(g);
				}
			}
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return layersOfGates;
	}

	public String[] getHeaders() {
		return new String[]{headerLine};
	}

	public File getCircuitFile() {
		return circuitFile;
	}

	@Override
	public int getNumberOfInputs() {
		return numberOfInputs;
	}

	@Override
	public int getNumberOfOutputs() {
		return numberOfOutputs;
	}

	@Override
	public int getNumberOfANDGates() {
		return numberOfANDGates;
	}

	/*
	 * Unavailible
	 */
	@Override
	public int getNumberOfP1Inputs() {
		return 0;
	}

	/*
	 * Unavailible
	 */
	@Override
	public int getNumberOfP2Inputs() {
		return 0;
	}

	@Override
	public int getNumberOfWires() {
		return numberOfWires;
	}

}
