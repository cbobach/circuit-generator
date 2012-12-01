import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;


public class VerilogToFairplayConverter implements Runnable {
	private File circuitFile;
	private File outputFile;
	private int numberOfInputs;
	private int numberOfOutputs;
	private String firstHeader;
	private String secondHeader;
	private HashMap<String, Integer> stringMap;
	private HashMap<String, Integer> inputMap;
	private int wireCount;

	List<Gate> leftOutputGates;
	List<Gate> rightOutputGates;
	List<Gate> outputGates;

	int maxOutputWire;

	public VerilogToFairplayConverter(File circuitFile, File outputFile){
		this.circuitFile = circuitFile;
		this.outputFile = outputFile;
		stringMap = new HashMap<String, Integer>();
		inputMap = new HashMap<String, Integer>();

		leftOutputGates = new ArrayList<Gate>();
		rightOutputGates = new ArrayList<Gate>();
		outputGates = new ArrayList<Gate>();

		maxOutputWire = 0;

	}

	@Override
	public void run() {
		List<String> gateStrings = getAnalyzedCircuit();
		List<Gate> res = getGates(gateStrings);
		incrementGates(res);
		firstHeader = res.size() + " " + CommonUtilities.getWireCount(res);
		//TODO: Must be based on input wires!!!!
		secondHeader = 0 + " " + numberOfInputs + " " + "0" + " " + numberOfOutputs;
		String[] headers = {firstHeader, secondHeader};
		CommonUtilities.outputFairplayCircuit(res, outputFile, headers);

	}

	private List<String> getAnalyzedCircuit() {
		List<String> res = new ArrayList<String>();

		try {
			BufferedReader fbr = new BufferedReader(new InputStreamReader(
					new FileInputStream(circuitFile), Charset.defaultCharset()));
			String line = "";
			String lastLine = "";
			int inputIncNumber = 0;
			while ((line = fbr.readLine()) != null) {
				line = StringUtils.trim(line);

				if (line.isEmpty() || line.startsWith("//") || line.startsWith("module")
						|| line.startsWith("endmodule") || line.startsWith("wire")){
					continue;
				} else if (line.startsWith("input [")) {
					String[] split = line.split(" ");
					String inputInfo = split[1];
					for (int i = 2; i < split.length; i ++) {
						inputMap.put(split[i].substring(0, split[i].length() -1), inputIncNumber);
						int inputNumber = getInputOutputNumber(inputInfo);
						inputIncNumber += inputNumber + 1;
					}
					continue;
				} else if (line.startsWith("output [")) {
					String[] split = line.split(" ");
					String outputInfo = split[1];
					int outputNumber = getInputOutputNumber(outputInfo);
					numberOfOutputs = outputNumber + 1;
					continue;
				} else {
					if(!line.endsWith(";")) {
						lastLine = lastLine + " " + line;
						continue;
					} else {
						line = lastLine + " " + line;
						line = StringUtils.trim(line);
						lastLine = "";
					}
					line = line.replace(" )", ")");
					line = line.replace("] [", "][");
					res.add(line);
				}
			}
			numberOfInputs = inputIncNumber;
			fbr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	private List<Gate> getGates(List<String> gates) {
		List<Gate> res = new ArrayList<Gate>();
		//Constructs the constant 1 wire at second first wire after input
		String nAND1 = "2 1 0 0 " + numberOfInputs + " 1110";
		String nAND2 = "2 1 0 " + numberOfInputs + " " + (numberOfInputs + 1) + " 1110";
		Gate g1 = new Gate(nAND1);
		Gate g2 = new Gate(nAND2);
		res.add(g1);
		res.add(g2);
		
		//Parsing of gates begins here
		for (String line: gates) {

			String[] split = line.split(" ");

			String inputs;
			String outputs = "1";
			if (split[1].endsWith("I")) {
				inputs = "1";
			} else {
				inputs = "2";
			}

			String leftWire = "";
			String rightWire = "";
			String outputWire = "";
			String boolTable = "";
			if(split[0].equals("XOR2HS")) {
				boolTable = "0110";
			}
			if (split[0].equals("AN2")) {
				boolTable = "0001";
			}
			if (split[0].equals("INV1S")) {
				inputs = "2";
				boolTable = "0110";
				leftWire = getWire(split[2]);
				rightWire = Integer.toString(numberOfInputs + 1);

				outputWire = getWire(split[4]);

			} else {
				leftWire = getWire(split[2]);
				rightWire = getWire(split[4]);
				outputWire = getWire(split[6]);
			}

			boolean leftOutputFlag = false;
			boolean rightOutputFlag = false;
			boolean outputFlag = false;

			if (leftWire.startsWith("o")) {
				leftWire = leftWire.substring(1);
				leftOutputFlag = true;
			}
			if (rightWire.startsWith("o")) {
				rightWire = rightWire.substring(1);
				rightOutputFlag = true;
			}
			if (outputWire.startsWith("o")) {
				outputWire = outputWire.substring(1);
				outputFlag = true;
			}

			String gateString = inputs + " " + outputs + " " + leftWire +
					" " + rightWire + " " + outputWire + " " + boolTable;

			Gate g = new Gate(gateString);
			maxOutputWire = Math.max(maxOutputWire, g.getOutputWireIndex());

			res.add(g);
			if (leftOutputFlag) {
				leftOutputGates.add(g);
			}
			if (rightOutputFlag) {
				rightOutputGates.add(g);
			}
			if (outputFlag) {
				outputGates.add(g);
			}
		}

		return res;
	}
	
	private void incrementGates(List<Gate> res) {
		int incNumber = maxOutputWire + 1;

		for (Gate g: leftOutputGates) {
			g.setLeftWireIndex(g.getLeftWireIndex() + incNumber);
		}
		for (Gate g: rightOutputGates) {
			g.setRightWireIndex(g.getRightWireIndex() + incNumber);
		}
		for (Gate g: outputGates) {
			g.setOutputWireIndex(g.getOutputWireIndex() + incNumber);
		}
	}
	
	//Cases [31:0] or [0:511]
	private int getInputOutputNumber(String s) {
		String res;
		String[] split = s.split(":");
		if (split[0].equals("[0")) {
			res = split[1].substring(0, split[1].length() - 1);
		} else {
			res = split[0].substring(1);
		}
		return Integer.parseInt(res);
	}

	private String getWire(String s) {
		s = s.replace(",", "");
		s = s.replace(");", "");
		for (Map.Entry<String, Integer> entry: inputMap.entrySet()) {
			String inputID = "(" + entry.getKey();
			int incNumber = entry.getValue();
			if (s.startsWith(inputID)) {
				int i = Integer.parseInt(s.substring(inputID.length() + 1, s.length() - 2));
				i += incNumber;
				return Integer.toString(i);
			}
		}
		if (s.startsWith("(input_i")) {
			return s.substring(9, s.length() - 2);
		} else if (s.startsWith("(output_o")) {
			return "o" + s.substring(10, s.length() - 2);
		} else {
			return Integer.toString(getWireNumber(s) + numberOfInputs + 2);
		}
	}

	private int getWireNumber(String s) {
		if(stringMap.get(s) != null) {
			return stringMap.get(s);
		} else {
			stringMap.put(s, wireCount++);
			return wireCount - 1;
		}
	}
}
