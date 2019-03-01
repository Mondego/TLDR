package uci.ics.mondego.tldr.changeanalyzer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import com.rfksystems.blake2b.Blake2b;
import com.rfksystems.blake2b.security.*;
import uci.ics.mondego.tldr.tool.Databases;

public class FileChangeAnalyzer extends ChangeAnalyzer{
	
	private final MessageDigest md;
	
	public FileChangeAnalyzer(String fileName) throws IOException, NoSuchAlgorithmException{
		super(fileName);
		Security.addProvider(new Blake2bProvider());
		md = MessageDigest.getInstance(Blake2b.BLAKE2_B_160);
		this.parse();
		this.closeRedis();
	}
	
	protected void parse() throws IOException {
		
		if(!this.exists(Databases.TABLE_ID_FILE,getEntityName())){
			String currentCheckSum = calculateChecksum();
			this.sync(Databases.TABLE_ID_FILE, this.getEntityName(), currentCheckSum);
			this.setChanged(true);
			logger.debug("New file "+this.getEntityName()+" added");
		}
		
		else{
			String prevCheckSum = this.getValue(Databases.TABLE_ID_FILE, this.getEntityName()); // get it from database;
			String currentCheckSum = calculateChecksum();
			if(!prevCheckSum.equals(currentCheckSum)){
				this.setChanged(true);
				this.sync(Databases.TABLE_ID_FILE, this.getEntityName(), currentCheckSum);
				logger.debug("File "+this.getEntityName()+" has changed");
			}
		}
	}
	
	private String calculateChecksum() throws IOException {
		InputStream fis = new FileInputStream(getEntityName());
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