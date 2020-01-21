package uci.ics.mondego.tldr.tool;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileHashCalculator {
	private MessageDigest md;
	
	public FileHashCalculator() {
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Calculates the checksum of a file using MD5 algorithm.
	 * @param fileName: the name of the file
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException 
	 */
	public String calculateChecksum(String fileName) throws IOException, NoSuchAlgorithmException {
		
		InputStream fis = new FileInputStream(fileName);
        byte[] buffer = new byte[1024];
        int nread;        
        while ((nread = fis.read(buffer)) != -1) {
            md.update(buffer, 0, nread);
        }
        
        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        
        fis.close();
        return result.toString();
    }
}
