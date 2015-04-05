import Javalette.*;
import java.io.*;

public class PartA {
	public static void main(String args[]) {
		Yylex l = null;
		if (args.length != 1) {
			System.err.println("Usage: PartA <SourceFile>");
			System.exit(-1);	
		}
		try {
	 		l = new Yylex(new FileReader(args[0]));
			parser p = new parser(l);
			Javalette.Absyn.Program parse_tree = p.pProgram();
			new TypeChecker().typecheck(parse_tree);
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
