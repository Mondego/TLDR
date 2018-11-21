package uci.ics.mondego.tldr;

import uci.ics.mondego.tldr.extractor.ASTBuilder;
import uci.ics.mondego.tldr.extractor.Scanner;
import uci.ics.mondego.tldr.tool.RedisHandler;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
       Scanner sc = new Scanner("/Users/demigorgan/Sourcerer");
       
       RedisHandler rd = new RedisHandler();
       
       
       rd.insert("tocu", "123");
       rd.insert("tocu", "121");
       System.out.println(rd.get("tocu"));
       
    }
}
