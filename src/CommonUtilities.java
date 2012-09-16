import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;


public class CommonUtilities {
	public static void outputFairplayCircuit(List<Gate> circuit, File outputFile,
			String[] headers){
			BufferedWriter fbw = null;
			try {
				fbw = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(outputFile), Charset.defaultCharset()));
				fbw.write(headers[0]);
				fbw.newLine();
				fbw.write(headers[1]);
				fbw.newLine();
				fbw.newLine();

				for(Gate g: circuit){
					fbw.write(g.toFairPlayString());
					fbw.newLine();
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally { 
				try {
					fbw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	
	public static void outputCUDACircuit(List<List<Gate>> layersOfGates, 
			File outputFile, String header) {
	BufferedWriter fbw = null;
	try {
		fbw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), Charset.defaultCharset()));
		
		fbw.write(header);
		fbw.newLine();

		/*
		 * Write the gates the the file, one layer at a time
		 */
		for(List<Gate> l: layersOfGates){
			// Write the size of the current layer
			fbw.write("*" + l.size()); 
			fbw.newLine();

			// Write the gates in this layer
			for(Gate g: l){
				String gateString = layersOfGates.indexOf(l) + " " + 
						g.toCUDAString();
				fbw.write(gateString);
				fbw.newLine();
			}
		}

	} catch (IOException e) {
		e.printStackTrace();
	} finally { 
		try {
			fbw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
}