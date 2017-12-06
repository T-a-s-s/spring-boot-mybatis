package br.com.tokiomarine.relatorio;

import java.io.FileWriter;
import java.io.IOException;

import com.opencsv.CSVWriter;

public class Teste {

	public static void main(String[] args) {
		CSVWriter csvWriter;
		try {
			csvWriter = new CSVWriter(new FileWriter("example.csv"), ';', '"', "\n");
			csvWriter.writeNext(new String[] { "2;4", "end", "Male", "24" });
			csvWriter.writeNext(new String[] { "2", "con", "Male", "24" });
			csvWriter.writeNext(new String[] { "3", "jane", "Female", "18" });
			csvWriter.writeNext(new String[] { "4", "ryo", "Male", "28" });
			csvWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
