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
	
	public static Set<String> Ekstazi = new HashSet<String>();
	public static Set<String> TLDR = new HashSet<String>();
	private static ArrayList<String> ChangedOrNewEntity = new ArrayList<String>();
	private static ArrayList<String> ChangedOrNewTest = new ArrayList<String>();
	private static Set<String> inEkstaziNotInTLDR = new HashSet<String>();
	private static ArrayList<String> inTLDRNotInEkstazi = new ArrayList<String>();
	private static ArrayList<String> bothInTLDRandEkstazi = new ArrayList<String>();

	
	public SureFireReportParserUtilities() {
		

		// TODO Auto-generated method stub
	   Ekstazi = ParseSureFireReport
		  ("/Users/demigorgan/Desktop/commons-math/target/site/surefire-report.html", Ekstazi);
		TLDR = ParseSureFireReport
			("/Users/demigorgan/TLDR_EXP/COMPLETE/jedis/24/afad308e29df790e4d789d4538783794d4a7d1e6/TLDR/surefire-report.html", TLDR);
		
		inEkstaziNotInTLDR();				
		inTLDRNotInEkstazi();
		bothEkstaziandInTLDR();
						
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
	
	public static void removeNewTests(ArrayList<String> list){
		for(int i=0;i<ChangedOrNewTest.size() ;i++){
			list.remove(ChangedOrNewTest.get(i));
		}
	}
	
	public static ArrayList<String> parseTLDR(ArrayList<String> list, String fileName){
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(fileName));
			String line = reader.readLine();
			int i = 0;
			while (line != null) {
				i++;
				if(line.indexOf("(") > 0){
					list.add(line.substring(0, line.indexOf("(")));		
				}
				else
					list.add(line);
				// read next line
				line = reader.readLine();
			}
			reader.close();
			return list;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static void inTLDRNotInEkstazi(){
		Iterator<String> tldr = TLDR.iterator();
		
		while(tldr.hasNext()){
			boolean found = false;
			String tok = tldr.next();
			Iterator<String> ekstazi = Ekstazi.iterator();
			while(ekstazi.hasNext()){				
				if(tok.equals(ekstazi.next())){
					found = true;
					break;
				}
			}
			if(!found)
				inTLDRNotInEkstazi.add(tok);
		}
	}
	
	private static void inEkstaziNotInTLDR(){
		Iterator<String> ekstazi = Ekstazi.iterator();

		while(ekstazi.hasNext()){
			boolean found = false;
			Iterator<String> tldr = TLDR.iterator();
			String e = ekstazi.next();
			while(tldr.hasNext()){	
				String t = tldr.next();
				if(e.equals(t)){
					found = true;
					break;
				}
			}
			if(!found)
				inEkstaziNotInTLDR.add(e);
		}
	}
	
	private static void bothEkstaziandInTLDR(){
		Iterator<String> ekstazi = Ekstazi.iterator();
		while(ekstazi.hasNext()){
			Iterator<String> tldr = TLDR.iterator();
			String e = ekstazi.next();

			while(tldr.hasNext()){	
				String t = tldr.next();
				if(e.equals(t)){
					bothInTLDRandEkstazi.add(e);
					break;
				}
			}
		}
	}
	
	public static void print(Set<String> list){
		Iterator<String> it = list.iterator();
		while(it.hasNext()){
			System.out.println(it.next());
		}
	}
}
