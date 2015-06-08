import Javalette.*;
import java.io.*;

public class PartC {
	public static void main(String args[]) {
		String path="//home//pengkun//tester//testsuite//extensions//arrays1//";
		File f=new File(path);
	    args=f.list();
  
        
		Yylex l = null;
		try {
			for(int i=0;i<args.length;i++){
				if(!args[i].endsWith("jl") )
					continue; 
	 		l = new Yylex(new FileReader(path+args[i]));
			parser p = new parser(l);
			Javalette.Absyn.Program parse_tree = p.pProgram();
			 String destination=args[i];
			    int len=destination.length();
			    destination=destination.substring(0,len-3);
			new TypeChecker().typecheck(parse_tree);
			new LLVM().compile(parse_tree,path+destination);
			
			System.err.println("OK");
			}
		} catch (TypeException e) {
			System.err.println("ERROR");
			System.err.println(e.toString());
			e.printStackTrace();
			System.exit(-1);
		} catch (RuntimeException e) {
			System.err.println("ERROR");
			System.err.println(e.toString());
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			System.err.println(e.toString());
			e.printStackTrace();
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
