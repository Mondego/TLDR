package uci.ics.mondego.tldr.tool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class SurefireReportParser {

	private static ArrayList<String> Ekstazi = new ArrayList<String>();
	private static String directory;
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		directory = "/Users/demigorgan/TLDR_EXP/Ekstazi_log/2019-03-20-01-04/9afc2969a3b491476525e100a8a542027576952e/surefire-report.html";
		parseEkstazi(directory);
	}
		
	public static void parseEkstazi(String fileName){
		BufferedReader reader;
		String seed = "<td><a name=";
		try {
			reader = new BufferedReader(new FileReader(fileName));
			String line = reader.readLine();
			int i = 0;
			while (line != null) {
				if(line.contains(seed)){
					String temp = line.substring(seed.length()+1);
					temp = temp.substring(3);
					Ekstazi.add(temp.substring(0, temp.indexOf("\"")));
				}

				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void print(ArrayList<String> list){
		for(int i=0; i<list.size() ;i++){
			System.out.println(list.get(i));
		}
	}
}
