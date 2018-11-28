package uci.ics.mondego.tldr.extractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;

import com.github.javaparser.ast.visitor.VoidVisitorAdapter;



public class TreeBuilder {

	String file="";
	
	public TreeBuilder(){
	}
	
    public TreeBuilder(String file){
		this.file = file;
	}
    
    public void AST(){
    	
    	try {
			CompilationUnit cu = JavaParser.parse(new File(file));
			VoidVisitor<?> methodNameVisitor = new MethodNamePrinter();
			VoidVisitor<?> importVisitor = new ImportedPackagePrinter();
			VoidVisitor<?> packageVisitor = new PackageDeclarationPrinter();
			
			methodNameVisitor.visit(cu, null);
			importVisitor.visit(cu, null);
			
			
			// this one is to parse a particular statement
			
			//Statement expression = JavaParser.parseStatement("int a = 0;");
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }    	
}


class MethodNamePrinter extends VoidVisitorAdapter<Void> {

	 @Override
	 public void visit(MethodDeclaration md, Void arg) {
		 super.visit(md, arg);
		 System.out.println("Method Name Printed: " + md.getName());
	 }
 }



class ImportedPackagePrinter extends VoidVisitorAdapter<Void> {

	 @Override
	 public void visit(ImportDeclaration md, Void arg) {
		 super.visit(md, arg);
		 System.out.println("Imported Package Name Printed: " + md.getName());
	 }
}


class PackageDeclarationPrinter extends VoidVisitorAdapter<Void> {
	 @Override
	 public void visit(PackageDeclaration md, Void arg) {
		 super.visit(md, arg);
		 System.out.println("Package Name Printed: " + md.getName());
	 }
}

class FeildDeclarationPrinter extends VoidVisitorAdapter<Void> {

	 @Override
	 public void visit(FieldDeclaration md, Void arg) {
		 super.visit(md, arg);
		 //System.out.println("Method Name Printed: " + md.md.getName());
	 }
}
