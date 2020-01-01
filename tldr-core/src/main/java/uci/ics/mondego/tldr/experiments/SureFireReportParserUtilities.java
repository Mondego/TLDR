package uci.ics.mondego.tldr.experiments;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class SureFireReportParserUtilities {
	
	public static Set<String> testCases = new HashSet<String>();
	
	public SureFireReportParserUtilities(String url) {
		// TODO Auto-generated method stub
	   testCases = ParseSureFireReport(url, testCases);
	}
	
	public static Set<String> ParseSureFireReport(String fileName, Set<String> list){
		list.clear();
		BufferedReader reader =  null;
		String seed = "<td><a name=";
		try {
			reader = new BufferedReader(new FileReader(fileName));
			String line = reader.readLine();
			while (line != null) {
				if(line.contains(seed)){
					String temp = line.substring(seed.length()+1);
					if(temp.startsWith("TC_"))
						temp = temp.substring(3);	
					list.add(temp.substring(0, temp.indexOf("\"")));
				}
				// read next line
				line = reader.readLine();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return list;
		
	}
	
	
	public static void print(Set<String> list){
		Iterator<String> it = list.iterator();
		while(it.hasNext()){
			System.out.println(it.next());
		}
	}
}
