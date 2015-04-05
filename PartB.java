import Javalette.*;
import java.io.*;

public class PartB {
	public static void main(String args[]) {
		if (args.length != 1) {
			System.err.println("Usage: PartB <SourceFile>");
			System.exit(-1);	
		} 
        
		Yylex l = null;
		try {
		 
	 		l = new Yylex(new FileReader(args[0]));
			parser p = new parser(l);
	 
			Javalette.Absyn.Program parse_tree = p.pProgram();
		    
		    String destination=args[0];
		    int len=destination.length();
		    destination=destination.substring(0,len-3);
		    
			new TypeChecker().typecheck(parse_tree);
			new LLVM().compile(parse_tree,destination);
			Runtime r = Runtime.getRuntime();
			r.exec("llvm-as "+args[0]+".ll");
			r.exec("llvm-link "+args[0]+".bc"+" ../lib/runtime.bc -o ../"+args[0]);
			System.err.println("OK");
		} catch (TypeException e) {
			System.err.println("ERROR");
			System.err.println(e.toString());
			System.exit(-1);
		} catch (RuntimeException e) {
			System.err.println("ERROR");
			System.err.println(e.toString());
			System.exit(-1);
		} catch (IOException e) {
			System.err.println(e.toString());
			System.exit(1);
		} catch (Throwable e) {
			System.err.println("ERROR");
			System.out.println("SYNTAX ERROR");
			System.out.println("At line " + String.valueOf(l.line_num()) 
					   + ", near \"" + l.buff() + "\" :");
			System.out.println("     " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}
