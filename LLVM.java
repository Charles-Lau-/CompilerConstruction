import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
 
 
import Javalette.Absyn.*;
 
public class LLVM {
	//generate .ll file
	public String fileName="";
	public FileWriter fileWriter;
	
	//to store llvm instructions,global declaration should be put before formal code
	ArrayList<String> instruction=new ArrayList<String>();
	ArrayList<String> string_declaration=new ArrayList<String>();
	ArrayList<String> function_declaration=new ArrayList<String>();
	ArrayList<String>  after_entry = new ArrayList<String>();
	public static enum TypeCode {CInt,CDouble,CBool,CVoid,CString}	
	//global variable
	private Env environment=new Env();
   	
	//used to general different label.so that they do not mix up
	public static enum Label {
   		Then {public String toString(){return "Then";}},
        While {public String toString() {return "While";}},
        TRUE {public String toString() {return "TRUE";}},
        FALSE{public String toString(){return "FALSE";}}}	
        private static class NewLabel{

    	              private int while_counter;
                      private int true_counter;
                      private int false_counter;
                      private int then_counter;

              public String getLabel(String s){
                    if(s.equals("While"))
                          return Label.While.toString()+(++while_counter);
                    else if(s.equals("TRUE"))
                          return Label.TRUE.toString()+(++true_counter);
                    else if(s.equals("Then"))
                    	  return Label.Then.toString()+(++then_counter);
                    else
                         return Label.FALSE.toString()+(++false_counter);
} 
}
      //function type class
      public static class FunType {
          public LinkedList<Type> args ;
          public Type val ;
          public FunType(Type val,LinkedList<Type> args){
          	   
          	   this.args=args;
          	   this.val=val;
          	 
          	}
          public FunType(Type val){
         	 
         	 
         	 this.args=null;
         	 this.val=val;
          }
          }
      
      //environment 
      private  class Env { 
    	    /*first element of Arraylist is llvm name,second is type,HashMap record
    	     *mapping between variable and llvm representation.
    	     */
    	  	private LinkedList<HashMap<String,ArrayList<String>>> scopes;
    	    private HashMap<String,FunType> signature;
    	    //for every function , a NewLabel class track labels used in this function
    	    private LinkedList<NewLabel> Labels;
    	    /*maxvar is for register names like %1 %2 ,string_couter is for @str0,@str1,
    	     *variable_counter is for names like @i1 @i2,
    	     */
    	    private Integer maxvar;
    	    private Integer string_counter;
    	    private Integer variable_counter;
    	    
    		public Env() {
    			    this.scopes = new LinkedList<HashMap<String,ArrayList<String>>>();
    	            this.Labels=new LinkedList<NewLabel>(); 
    	            this.maxvar = 0 ;
    	            this.variable_counter=0;
    	            this.signature=new HashMap<String,FunType>();
    		        this.string_counter=0;
    		  }
    		public NewLabel getNewLabel(){
          	  return  Labels.getFirst();
             }  
    		  public ArrayList<String> lookupVar(String x) {
    		    for (HashMap<String,ArrayList<String>> scope : scopes) {
    			       ArrayList<String> t = scope.get(x);
    			       if (t != null)
    			         return t;
    		      }
    		     throw new TypeException("Unknown variable " + x + ".");
    	      }
    	    public FunType lookupFun(String fun){
    	    	   FunType ft=signature.get(fun);
    	    	   if(ft!=null)
    	    	     return ft;
    	    	   else
    	    	     throw new TypeException("Unknown function  "+fun);
    	    	 } 
              
    	      /* for given variable x with type t, generate corresponding llvm representation
    	       * and store  <x,<llvm-representation>> pairs
    	       */
    		  public void addVar(String x,Type t) {
    		    if (scopes.getFirst().containsKey(x))
    			      throw new TypeException("Variable " + x 
    						+ " is already declared in this scope.");
    		    else{
    		         ArrayList<String> arr=new ArrayList<String>();
    		         
    		         arr.add("%"+x+variable_counter++);
    		         
    		         arr.add(compileType(t,null));
    		         
    		         scopes.getFirst().put(x, arr);
    		         
    		  
    		    }
    		    }
    		  
    		  public void addFun(String x,FunType ft){
    		  	
    		  	if(signature.get(x)!=null)
    		  	  throw new TypeException("function "+x+ " already exists ");
    		  	else
    		  	  signature.put(x,ft);  
    		  	
    		  	}
    		  
    		  //update the llvm type of variable x to be pointer type
    		  public void updateVar(String x,String llvm_type){
    			  for (HashMap<String,ArrayList<String>> scope : scopes) {
   			         ArrayList<String> t = scope.get(x);
   			         if (t != null){
   			        	    t.set(1, llvm_type);
   			        	    scope.put(x, t);  
   			         }
   			                
   		      }
    		  }
    		  public void updateVarRegisterMapping(String x,String registerName){
    			  for (HashMap<String,ArrayList<String>> scope : scopes) {
   			         ArrayList<String> t = scope.get(x);
   			         if (t != null){
   			        	    t.set(0, registerName);
   			        	    scope.put(x, t);  
   			         }
   			                
   		      }
    		  }
    		  
    		   //when do string declaration, a string name is desired
    		   public String getStringName(){
    			     String name="@str"+this.string_counter;
    			     this.string_counter++;
    			     return name;
    		   }
    		   
    		   //get name for register like %1 , %2.
               public String getRegister(){
            	   String s="%"+this.maxvar;
            	   maxvar++;
            	   return s;
               }
               
    		   public void enterScope() {
    		     scopes.addFirst(new HashMap<String,ArrayList<String>>());
    	    	 }

    		   public void leaveScope() {
    		    scopes.removeFirst();
    		  }
    		   
    		    public void enterFunction(){
    	 
    	    		Labels.addFirst(new NewLabel());
    	    	}
    		    
    		    //different function could have the same variable name and register name
    	    	public void leaveFunction(){
    	    		this.maxvar=0;
    	    		this.variable_counter=0;
    	    		Labels.removeFirst();
    	    	}
    	    }
      
      //emit code for normal instruction
      private void emit(String code){
    	   instruction.add(code+"\r\n");
      }
      
      //emit code for function and global string declaration
      private void emit(String global_declaration,String mode){
    	    if(mode.equals("string")){
    	    	//avoid multiple declaration
    	        if(!string_declaration.contains(global_declaration+"\r\n"))
    	    	string_declaration.add(global_declaration+"\r\n");
    	        
    	    }
    	    else if(mode.equals("function")){
    	    	//avoid multiple declaration
    	    	if(!function_declaration.contains(global_declaration+"\r\n")){
    	    	   function_declaration.add(global_declaration+"\r\n");
    	    	}
    	    }
    	    else if(mode.equals("afterEntry")){
    	    	after_entry.add(global_declaration+"\r\n");
    	    }
    	    else 
    	    	throw new RuntimeException("parameter of mode could just be string or function");
      }
     

    //entrance of this class
	public void compile(Program p,String fn) throws IOException{
		  //create target file
		  this.fileName=fn;
		  File file=new File(fn+".ll");  
		  fileWriter=new FileWriter(file);
		  
		  
          Pro program=(Pro)p;
          //declare external function
          emit("declare i32* @calloc(i32, i32)","function");
		  for(TopDef d: program.listtopdef_){
		  	    LinkedList<Type> argType_List=new LinkedList<Type>();
		  	    FnDef df=(FnDef)d;
		  	    for(Arg g:df.listarg_){
		  	    	  Args ad=(Args)g;
		  	    	  argType_List.addLast(ad.type_); 
		  	    	}
		  	     FunType ft=new FunType(df.type_,argType_List);
		  	     environment.addFun(df.ident_,ft);   
		  	}
    	   
		  for(TopDef d: program.listtopdef_){
		  	     environment.enterScope();
	             environment.enterFunction();
	            
			     compileTopDef(d,null);
			     
			     environment.leaveFunction();
			     environment.leaveScope();
			   
		  	}
		  
		   //write llvm instruction from memory into file
		   try{   
		   for(String s:string_declaration)
			     fileWriter.write(s);
		   for(String f:function_declaration)
			     fileWriter.write(f);
		  
		   for(String instru:instruction){
			     fileWriter.write(instru);
		   }
		   }
		   catch(Exception e){
			   e.printStackTrace();
		   }
		   fileWriter.flush();
		   fileWriter.close();
      }
      private void compileTopDef(TopDef d,Object obj){
	 	   d.accept(new TopDefCompiler(),obj);
		   
      }
      private class TopDefCompiler implements TopDef.Visitor<Object,Object>{
  	       public Object visit(Javalette.Absyn.FnDef d, Object obj)
         {
  	        //store  return type of function
  	    	environment.addVar("ThisFunction",d.type_);
  	        
  	        String function_signature="define "+compileType(d.type_,environment)+" @"+d.ident_+"(";
            String args="";
  	        for (Arg x : d.listarg_) {
         	   args+=compileArg(x,obj)+",";
             }
  	        if(args!="") 
              args=args.substring(0,args.length()-1);
  	          
  	        args+=")";
  	        
            function_signature=function_signature+args.toString();
        
            emit(function_signature);
            emit("{");
            emit("entry:");
            if(!after_entry.isEmpty()){
            	for(String s:after_entry)
            		instruction.add(s);
            	after_entry.clear();
            }
            //check whether the function body is empty
            if(((Blo)d.block_).liststmt_.size()!=0)
                 compileBlock(d.block_,obj);
            else
            	emit("ret "+compileType(d.type_,null));
            emit("}");
                       
            return null;
        }
  	}
      private String compileArg(Arg a,Object obj){
    	      return a.accept(new argCompiler(), obj);
      }
  	 private class argCompiler implements Arg.Visitor<String,Object>
    {
         public String visit(Javalette.Absyn.Args p, Object obj)
         {
         
        	
        	environment.addVar(p.ident_,p.type_);
        	ArrayList<String> arr=environment.lookupVar(p.ident_);
            String type = arr.get(1);
            String name = arr.get(0);
        	if(arr.get(1).contains("*")){
        		String register = "%"+p.ident_+environment.variable_counter++;
        		emit(register +"=alloca "+arr.get(1),"afterEntry");
        		emit("store "+arr.get(1)+" "+arr.get(0)+","+arr.get(1)+"* "+register,"afterEntry");
        		environment.updateVarRegisterMapping(p.ident_,register);
            	environment.updateVar(p.ident_, arr.get(1)+"*");
            }
            return type+" "+name;
         }
  
	}
  	 //use return string of every method to let different statment communicate with each other
  	 //so that we know whether a statement is empty or return statement
  	 private String compileBlock(Block b,Object obj){
  		    return b.accept(new blockCompiler(),obj);
  		 
  	 }
  	 private class blockCompiler implements Block.Visitor<String,Object>{
  		  public String visit(Javalette.Absyn.Blo p,Object obj)
          {
  			 String response="";
             environment.enterScope();
             
             if(p.liststmt_.size()==0){
                 response="unreachable";
             }
             
             else{
            	 for (int i=0;i<p.liststmt_.size();i++) {
                   response=compileStmt(p.liststmt_.get(i),obj);
                   if(response=="unreachable")
                	   break;
 		     }
          }
           environment.leaveScope();
           return response;
         }
  		 
  		 
  	 }
  	 private String compileStmt(Stmt s,Object obj){
  		     return s.accept(new stmtCompiler(),obj);
  	 }
  	 private class stmtCompiler implements Stmt.Visitor<String,Object>{
  	   public String visit(Javalette.Absyn.Empty p, Object objt)
       {
  	         return "empty statement";
       }
        public String visit(Javalette.Absyn.BStmt p, Object obj)
       {
            String response=compileBlock(p.block_,environment);
  		    return response;
        }
  	  public String visit(Javalette.Absyn.Decl p, Object obj)
        {
  	    for (Item x : p.listitem_) {
          	 if(x instanceof NoInit){
                   NoInit nx=(NoInit)x;
          		   environment.addVar(nx.ident_,p.type_);
          		   ArrayList<String> l=environment.lookupVar(nx.ident_);
          		   emit(l.get(0)+"=alloca "+l.get(1));
          		   //if no initilization , then set default value
          		   String t = compileType(p.type_,null);
          		   String value="";
          		   if(t=="i1")
          			   value="i1 0";
          		   else if(t=="i32")
          			   value="i32 0";
          		   else if(t=="double")
          			   value="double 0.0";
          		   else
          			   value= t+" 0";
          		   //update the type to pointer
          		   environment.updateVar(nx.ident_, l.get(1)+"*");

          		   emit("store "+value+","+l.get(1)+" "+l.get(0));
        		   
          	 }else{ 
          		 Init ix=(Init)x;
          	     String value=compileExpr(ix.expr_,null);
          		 
          	     environment.addVar(ix.ident_,p.type_);
         		 ArrayList<String> l=environment.lookupVar(ix.ident_);
         		 emit(l.get(0)+"=alloca "+l.get(1));
         		 //update the type to pointer
         		 environment.updateVar(ix.ident_, l.get(1)+"*");
         		  
       		     //update the value for the variable
                 emit("store "+value+","+l.get(1)+" "+l.get(0));
                 
          	 }
  		 }

          return "";
        }
  	    public String visit(Javalette.Absyn.Ass p, Object obj)
        { 
  	    	 
              String value=compileExpr(p.expr_,null);
              
              ArrayList<String> l=environment.lookupVar(p.ident_);
              
              //check the variable is a pointer or not
  	          if(l.get(1).endsWith("*"))
  	        	  emit("store "+value+","+l.get(1)+" "+l.get(0));
  	          else{
  	        	  String register=environment.getRegister();
  	        	  emit(register+"=alloca "+l.get(1));
  	        	  emit("store "+value+","+l.get(1)+" "+register);
  	              
  	        	  //now the mapping between variable and llvm_name is updated
  	        	  environment.updateVar(p.ident_, register+"*");
  	          }
              return "";
        }
  	    public String visit(Javalette.Absyn.Incr p, Object obj)
        {

             ArrayList<String> l=environment.lookupVar(p.ident_);
             if(l.get(1).contains("*")){
               String register1=environment.getRegister();
               emit(register1+"=load "+l.get(1)+" "+l.get(0));
  		       String register2=environment.getRegister();
               emit(register2+"=add i32 "+register1+","+"1");
               emit("store i32 "+register2+","+l.get(1)+"  "+l.get(0));
             }
             else{
            	   String register1=environment.getRegister();
  	        	   emit(register1+"=alloca "+l.get(1));
  	        	   
  	        	   String register2=environment.getRegister();
          	       emit(register2+"=add "+l.get(1)+" "+l.get(0)+","+"1");
  	        	   
          	       emit("store i32 "+register2+","+l.get(1)+"* "+register1);
  	              
  	        	   environment.updateVar(p.ident_, register1+"*");
             }
             return "";
        }
  	   public String visit(Javalette.Absyn.Decr p, Object obj)
        {
           ArrayList<String> l=environment.lookupVar(p.ident_);
           if(l.get(1).contains("*")){
           String register1=environment.getRegister();
           emit(register1+"=load "+l.get(1)+" "+l.get(0));
		     String register2=environment.getRegister();
           emit(register2+"=add i32 "+register1+","+"-1");
           emit("store i32 "+register2+","+l.get(1)+"  "+l.get(0));
           }
           else{
          	  String register1=environment.getRegister();
	        	  String register2=environment.getRegister();
          	      emit(register1+"=alloca "+l.get(1),"afterEntry");
	        	  emit(register2+"=add "+l.get(1)+" "+l.get(0)+","+"-1","afterEntry");
	        	  emit("store i32 "+register2+","+l.get(1)+"* "+register1,"afterEntry");
	              
	        	  environment.updateVar(p.ident_, register1+"*");
           }
           return "";
        }
  	  public String visit(Javalette.Absyn.Ret p, Object obj)
       {            
  		  
             String value=compileExpr(p.expr_,null);
             emit("ret "+value);  
             return "return";
       }
  	  public String visit(Javalette.Absyn.VRet p,Object obj)
       {     
  		     emit("ret void");
  	         return "return";
       }
  	   public String visit(Javalette.Absyn.Cond p, Object obj)
        {
  		    String value=compileExpr(p.expr_,null);
  		    NewLabel l=environment.getNewLabel();
		    String   t_label=l.getLabel("TRUE");
		    String   then_label=l.getLabel("Then");
  		    emit("br "+value+",label %"+t_label+",label %"+then_label);
  		    
  		    emit(t_label+":");
  		    String response=compileStmt(p.stmt_,environment);
  		    //if the statement is not a return-statement, then we should emit "br" instruction
  		    if(!response.equals("return"))
  		      emit("br label %"+then_label);
  		    
  		    emit(then_label+":");
  		    
  		    //to decide whether this then label is unreachable
  		    //Only one possibility: expr is constant true and there is a return statement in if-block
  		    if(value.equals("i1 1")&&response.equals("return"))
  		    {	
  		    	emit("unreachable");
  		        //communicate with other statement
  		    	return "return";
  		    }
  		    else
  		        return "";
       }
      public String visit(Javalette.Absyn.CondElse p, Object obj)
       {
    	  
		    String value=compileExpr(p.expr_,null);
	 
  		    NewLabel l=environment.getNewLabel();
  		    String   then_label=l.getLabel("Then");
  	  	    String   t_label=l.getLabel("TRUE");
  		    String   f_label=l.getLabel("False");
  		    
  		    emit("br "+value+",label %"+t_label+",label %"+f_label);
  		    
  		    emit(t_label+":");
  		    String response1=compileStmt(p.stmt_1,null);
  		    //if there is no return statement inside, then jump to then: label
  		    if(!response1.equals("return"))
  		    	emit("br label %"+then_label);    		   
  		    
  		    emit(f_label+":");
  		    String response2=compileStmt(p.stmt_2,null);
  		    if(!response2.equals("return"))
  		    	emit("br label %"+then_label);
  		   
  		    if(!response1.equals("return") ||!response2.equals("return"))
  		        emit(then_label+":");
  		  
  		    //to decide whether then:label is reachable
  		    //only two possibilities that then:label is not reachable
  		    if(value.equals("i1 1")&&response1.equals("return"))
  		    	emit("unreachable");
  		    else if(value.equals("i1 0")&&response2.equals("return"))
  		        emit("unreachable");
  		    
            if(response1.equals("return")||response2.equals("return"))
                return "return";
            else
            	return "";
       }
  		    public String visit(Javalette.Absyn.While p,Object obj)
        {
         

  	  		   
  	  		    
  		    	NewLabel l=environment.getNewLabel();
  	  		    String  w_label=l.getLabel("While");
  	  		    String   t_label=l.getLabel("TRUE");
  	  		    String   f_label=l.getLabel("False");
  	  		    
  	  		    emit("br label %"+w_label);
  	  		    
  	  		    emit(w_label+":");
  	  		    String value=compileExpr(p.expr_,null);
  	  		    emit("br "+value+",label %"+t_label+",label %"+f_label);
  	  		   
  	  		    emit(t_label+":");
  	  		    String response=compileStmt(p.stmt_,environment);
  	  		    
  	  		    if(!response.equals("return"))
  	  		    	emit("br label %"+w_label);
  	  		    
  	  		    emit(f_label+":");
  	  		    if(response.equals("return")&&value.equals("i1 1"))
  	  		    	{
  	  		    	emit("unreachable");
  	  		    	return "unreachable";
  	  		    	}
  	  		    else
  	  		    	return "";
       }
       public String visit(Javalette.Absyn.SExp p,Object obj)
       {
    	   compileExpr(p.expr_,null);
          
  	       return "";
    
       }
	@Override
	public String visit(Ass2 p, Object arg) {
		ArrayList<String> arr = environment.lookupVar(p.ident_);
		String type = arr.get(1).substring(0,arr.get(1).length()-2);
		String type_ptr = type+"*";
		
		String index = compileExpr(p.expr_1,"").split(" ")[1];
		
		try{
			int idex=Integer.parseInt(index);
			index = Integer.toString((idex+1));
		}
		catch(NumberFormatException e){
			String regis = environment.getRegister();
			emit(regis+"=add i32 1,"+index);
			index = regis;
		}
		String value = compileExpr(p.expr_2,"");
		
		String r1 = environment.getRegister();
		emit(r1+"=load "+arr.get(1)+" "+arr.get(0));
		String r2 = environment.getRegister();
		emit(r2+"=getelementptr "+type_ptr+" "+r1+","+type+" "+index);
		emit("store "+value+","+type_ptr+" "+r2);
		 
		return "";
	}
	@Override
	public String visit(ForLoop p, Object arg) {
		environment.enterScope();
		NewLabel label = environment.getNewLabel();
		String w_label = label.getLabel("While");
		String t_label  = label.getLabel("TRUE");
		String f_label = label.getLabel("FALSE");
		environment.addVar("counter", new Int());
		 ArrayList<String> c=environment.lookupVar("counter");
 		 emit(c.get(0)+"=alloca "+c.get(1));
 		environment.updateVar("counter", c.get(1)+"*");
        emit("store i32 0,"+c.get(1)+" "+c.get(0)); 
		String len=compileExpr(new ArrayLen(p.ident_2),null).split(" ")[1];
		
		
		//store ident1
		environment.addVar(p.ident_1,p.type_);
 		 ArrayList<String> l=environment.lookupVar(p.ident_1);
 		 emit(l.get(0)+"=alloca "+l.get(1));
 		 //update the type to pointer
 		environment.updateVar(p.ident_1, l.get(1)+"*");
 		 //update the value for the variable
        emit("store i32 0,"+l.get(1)+" "+l.get(0));
		//store end
        emit("br label %"+w_label);
        emit(w_label+":");
        String counter = environment.getRegister();
        String cond = environment.getRegister();
        emit(counter+"=load "+c.get(1)+" "+c.get(0));
		emit(cond+"=icmp slt i32 "+counter+","+len);
		emit("br i1 "+cond+",label %"+t_label+",label %"+f_label);
		emit(t_label+":");
		ArrayList<String> arr = environment.lookupVar(p.ident_2);
		String type = arr.get(1).substring(0,arr.get(1).length()-2);
		String type_ptr = type+"*";
		
		String r1 = environment.getRegister();
		emit(r1+"=load "+arr.get(1)+" "+arr.get(0));
	    String r0 = environment.getRegister();
		String r2 = environment.getRegister();
		emit(r0+"=add i32 1,"+counter);
		emit(r2+"=getelementptr "+type_ptr+" "+r1+","+type+" "+r0);
		String r3 = environment.getRegister();
		emit(r3+"=load "+type_ptr+" "+r2);
		emit("store "+type+" "+r3+","+l.get(1)+" "+l.get(0));
		compileStmt(p.stmt_,arg);
		emit("store i32 "+r0+","+c.get(1)+" "+c.get(0));
		emit("br label %"+w_label);
		emit(f_label+":");
		environment.leaveScope();
		return null;
	}
       
  		 
  	 }
  	 //return string of compileExpr is "llvm_type llvm_name"
  	 //Exception:if the bool expression is constant, i1 0 or i1 1 will be returned
  	 //String t is llvm_type
  	 private String compileExpr(Expr e,String t){
  		     return e.accept(new exprCompiler(),t);
  	 }
     private class exprCompiler implements Expr.Visitor<String, String>{
    	 public String visit(AnnoType p, String t) {
    		 String tt="";
       		 
    		 if(p.type_!=null)
     			tt=compileType(p.type_,null); 
     		 //since in typechecker we set the type of string is null , so it is vital to check here.
    		 else
     			tt="String";
     		return compileExpr(p.expr_,tt);
     }
     	public String visit(Javalette.Absyn.EVar p,String t)
       {
     	  ArrayList<String> arr=environment.lookupVar(p.ident_);
     	  if(arr.get(1).contains("*")){
     		  String register=environment.getRegister();
     	      emit(register+"=load "+arr.get(1)+"  "+arr.get(0));
     	      return  t+" "+register;
     	  }
     	  else 
     		  return arr.get(1)+" "+arr.get(0);
     	  
       }
 	   public String visit(Javalette.Absyn.ELitInt p,String t)
     {
        return "i32 "+p.integer_;
 	  
     }
     public String visit(Javalette.Absyn.ELitDoub p,String t)
     { 
     	return "double "+p.double_;
     }
     public String visit(Javalette.Absyn.ELitTrue p,String t)
     { 
     	return "i1 "+1;
     }
     public String visit(Javalette.Absyn.ELitFalse p,String t)
     { 
     	return "i1 "+0;
     }
     public String visit(Javalette.Absyn.EString p,String t)
     { 
    	String str_name=environment.getStringName();
    	int length=p.string_.length()+1;
     	emit(str_name+"=internal constant "+"["+length+" x i8"+"] c\""+p.string_+"\\"+"00\"","string");
     	String register=environment.getRegister();
     	
        emit(register+"=bitcast ["+length+" x i8]* "+str_name+" to i8*");
     	return  "i8* "+register;
     }
     public String visit(Javalette.Absyn.EApp p, String t)
     {
    	if(p.ident_.equals("printInt")){
    		String value=compileExpr(p.listexpr_.get(0),t);
    		emit("declare void @printInt(i32)","function");
    	   	emit("call void @printInt("+value+")");
    	   	return null;
    	}
    	else if(p.ident_.equals("printString")){
    	   
    	   String value=compileExpr(p.listexpr_.get(0),t);
    	   emit("declare void @printString(i8*)","function");
    	   emit("call void @printString("+value+")");
    	   return null;
    	}
    	else if(p.ident_.equals("readInt")){
    		String register=environment.getRegister();
    		emit("declare i32 @readInt()","function");
    		emit(register+"=call i32 @readInt()");
    		return "i32 "+register;
    	}
    	else if(p.ident_.endsWith("readDouble")){
    		String register=environment.getRegister();
    		emit("declare double @readDouble()","function");
    		emit(register+"=call double @readDouble()");
    	    return "double "+register;
    	}
    	else if(p.ident_.endsWith("printDouble")){
    		String value=compileExpr(p.listexpr_.get(0),t);
    		emit("declare void @printDouble(double)","function");
    	   	emit("call void @printDouble("+value+")");
    	   	return null;
    	}
    	else{
           FunType ft=environment.lookupFun(p.ident_);
           String  func="call "+compileType(ft.val,null)+" @"+p.ident_+"(";
          
           for(Expr e:p.listexpr_){
             String value=compileExpr(e,null);
             func+=value+",";
           }
           if(p.listexpr_.size()!=0)
             func=func.substring(0, func.length()-1);
           
           func+=")";
           
           if(ft.val instanceof Javalette.Absyn.Void){
        	   emit(func);
        	   return null;
        	   }
           else{
        	   String register=environment.getRegister();
        	   emit(register+"="+func);
        	   return compileType(ft.val,null)+" "+register;
               }
    	}
     }
       
      
       public String visit(Javalette.Absyn.Neg p,String t)
     {
    	     
             String value=compileExpr(p.expr_,t);
            
             String register=environment.getRegister();
             if(t.equals("i32"))
               emit(register+"=sub i32 0"+","+value.split(" ")[1]);
             else
               emit(register+"=fsub double 0.0"+","+value.split(" ")[1]);
             
             return t+" "+register;
     }
     public String visit(Javalette.Absyn.Not p,String t)
     {
 	          String value=compileExpr(p.expr_,t);
 	          String register=environment.getRegister();
 	          emit(register+"=xor i1 1,"+value.split(" ")[1]);
 	          
 	          return t+" "+register;
     }
 	 public String visit(Javalette.Absyn.EMul p,String t)
     {
            String value1=compileExpr(p.expr_1,null);
            String value2=compileExpr(p.expr_2,null);
           
            AnnoType a_p=(AnnoType)p.expr_1;
            String oper=compileMulop(p.mulop_,compileType(a_p.type_,null));
            String register=environment.getRegister();
            
            emit(register+"="+oper+" "+value1+","+value2.split(" ")[1]); 
            
 		    return t+" "+register;
     }
     public String visit(Javalette.Absyn.EAdd p, String t)
     {
    	 String value1=compileExpr(p.expr_1,null);
  		 String value2=compileExpr(p.expr_2,null);
  		 
  		 
  		 AnnoType a_p=(AnnoType)p.expr_1;
         String oper=compileAddop(p.addop_,compileType(a_p.type_,null));
         String register=environment.getRegister();
         emit(register+"="+oper+" "+value1+","+value2.split(" ")[1]); 
        
         
        
         
		 return t+" "+register;
   }
     public String visit(Javalette.Absyn.ERel p,String t)
     {  
    	 
         String value1=compileExpr(p.expr_1,null);
         String value2=compileExpr(p.expr_2,null);
         
         if(value1.equals("i1 1"))
        	 if(value2.equals("i1 1"))
        		 return "i1 1";
        	 else if(value2.equals("i1 0"))
        		 return "i1 0";
         else if(value1.equals("i1 0"))
        	 if(value2.equals("i1 1"))
        		 return "i1 0";
        	 else if(value2.equals("i1 0"))
        		 return "i1 1";
         
         AnnoType a_p=(AnnoType)p.expr_1;
         String oper=compileRel(p.relop_,compileType(a_p.type_,null));
         String register=environment.getRegister();
         emit(register+"="+oper+" "+value1+","+value2.split(" ")[1]); 
         
		 return t+" "+register;
     }
     public String visit(Javalette.Absyn.EAnd p,String t)
     {
 	   String value1=compileExpr(p.expr_1,null);
 	    
 	   NewLabel l=environment.getNewLabel();
	   String   t1_label=l.getLabel("TRUE");
	   String   f1_label=l.getLabel("False");
	   String  then_label = l.getLabel("Then");
	   String  var=environment.getRegister();
	   emit(var+"=alloca i1");
	   
 	   emit("br "+value1+",label %"+t1_label+",label %"+f1_label);
	   //true then just evaluate next expression
 	   emit(t1_label+":");
 	   String value2=compileExpr(p.expr_2,null);
 	   String reg = environment.getRegister(); 
 	   emit(reg+"=and "+value1+","+value2.split(" ")[1]);
 	   emit("store i1 "+reg+",i1* "+var);
 	   emit("br label %"+then_label);
 	   //false then just branch to then label
 	   emit(f1_label+":");
 	   emit("store "+value1+",i1* "+var);
 	   emit("br label %"+then_label);
 	   emit(then_label+":");
 	   //both in true branch and false branch, value is stored
 	   //in variable var, then in then branch, we just load it
 	   String register = environment.getRegister();
 	   emit(register+"= load i1* "+var);
 	   return "i1 "+register;
     }
     public String visit(Javalette.Absyn.EOr p,String t)
     {
    	  String value1=compileExpr(p.expr_1,null);
    	  NewLabel l=environment.getNewLabel();
   	      String   t1_label=l.getLabel("TRUE");
   	      String   f1_label=l.getLabel("False");
   	      String  then_label = l.getLabel("Then");
   	      String  var=environment.getRegister();
   	      emit(var+"=alloca i1");
   	 
   	      emit("br "+value1+",label %"+t1_label+",label %"+f1_label);
	      //false then just evaluate next expression
 	      emit(f1_label+":");
 	      String value2=compileExpr(p.expr_2,null);
 	      String reg = environment.getRegister(); 
 	      emit(reg+"=or "+value1+","+value2.split(" ")[1]);
 	      emit("store i1 "+reg+",i1* "+var);
 	      emit("br label %"+then_label);
 	      //true then just branch to then label
 	      emit(t1_label+":");
 	      emit("store "+value1+",i1* "+var);
 	      emit("br label %"+then_label);
 	      emit(then_label+":");
 	      //both in true branch and false branch, value is stored
 	      //in variable var, then in then branch, we just load it
 	      String register = environment.getRegister();
 	      emit(register+"= load i1* "+var);
 	      return "i1 "+register;
 	}
	@Override
	public String visit(NewArray p, String arg) {
		String size;
		String num = compileExpr(p.expr_,arg).split(" ")[1];
		try{	
	 	 size= (new Integer(num)+1)+"";
		}
		catch(NumberFormatException e){
		    String register = environment.getRegister();
			emit(register+"=add i32 1,"+num);
			size = register;
		}
	 	String t = compileType(p.type_,null);
		String addr = environment.getRegister();
		String returnValue="";
		if(t.equals("i1")){
			emit(addr+"=call i8* @calloc(i32 "+size+",i32 1)");
			returnValue = "i8*";
			t="i8";
		 }
		else if(t.equals("i32")){
			emit(addr+"=call i32* @calloc(i32 "+size+",i32 4)");
			returnValue = "i32*";
		}
		else{
			emit(addr+"=call i64* @calloc(i32 "+size+",i32 8)");
			returnValue = "i64*";
			t="i64";
		}
		String r1 = environment.getRegister();
		emit(r1+"=getelementptr "+returnValue+" "+addr+","+t+" 0");
		emit("store "+t+" "+num+","+returnValue+" "+r1);
		return returnValue+" "+addr;
	}
	@Override
	public String visit(ArrayLen p, String arg) {
		ArrayList<String> array= environment.lookupVar(p.ident_);
		String r1 = environment.getRegister();
		emit(r1+"=load "+array.get(1)+" "+array.get(0));
		String r2 = environment.getRegister();
		String type = array.get(1).substring(0,array.get(1).length()-2);
		String type_ptr = type+"*";
		 
		emit(r2+"=getelementptr "+type_ptr+r1+","+type+" 0");
		String r3 = environment.getRegister();
		emit(r3+"=load "+type_ptr+" "+r2);
		if(type.equals("i8")||type.equals("i32"))
			return "i32 "+r3;
		else{
			String r4 = environment.getRegister();
			emit(r4+"=fptoui double "+r3+"to i32");
			return  "i32 "+r4;
		}
	}
	@Override
	public String visit(ArrayEle p, String arg) {
		ArrayList<String> arr = environment.lookupVar(p.ident_);
		String type = arr.get(1).substring(0,arr.get(1).length()-2);
		String type_ptr = type+"*";
		String index = compileExpr(p.expr_,arg).split(" ")[1];
		try{
			int idex=Integer.parseInt(index);
			index = Integer.toString((idex+1));
		}
		catch(NumberFormatException e){
			String regis = environment.getRegister();
			emit(regis+"=add i32 1,"+index);
			index = regis;
		}
		String r1 = environment.getRegister();
		emit(r1+"=load "+arr.get(1)+" "+arr.get(0));
		 
		String r2 = environment.getRegister();
		emit(r2+"=getelementptr "+type_ptr+" "+r1+","+type+" "+index);
		String r3 = environment.getRegister();
		emit(r3+"=load "+type_ptr+" "+r2);
		return type+" "+r3;
	}
     }
     private String compileMulop(MulOp m,String t){
    	      return m.accept(new mulopCompiler(), t);
     }
     public class mulopCompiler implements MulOp.Visitor<String,String>
     {
       public String visit(Javalette.Absyn.Times p,String t)
       {
              if(t.startsWith("i"))
            	  return "mul";
              else
            	  return "fmul";
       }
       public String visit(Javalette.Absyn.Div p,String t)
       {
    	   if(t.startsWith("i"))
         	  return "sdiv";
           else
         	  return "fdiv";
       }
       public String visit(Javalette.Absyn.Mod p,String t)
       {
    	   if(t.startsWith("i"))
         	  return "srem";
           else
         	  return "frem";
       }

     }
      private String compileAddop(AddOp a,String t){
    	    return a.accept(new addopCompiler(),t);
      }
      private class addopCompiler  implements AddOp.Visitor<String,String>{
    	  public String visit(Javalette.Absyn.Plus p,String t)
    	    {
    	      if(t.startsWith("i"))
                   return "add";
    	      else
    	    	  return "fadd";
    	    }
    	    public String visit(Javalette.Absyn.Minus p, String t)
    	    {
    	    	  if(t.startsWith("i"))
                      return "sub";
       	         else
       	    	      return "fsub";    	   
    	    	  }
      }
      private String compileRel(RelOp r,String t){
    	  return r.accept(new relCompiler(), t);
      }
      public class relCompiler implements RelOp.Visitor<String,String>
      {
        public String visit(Javalette.Absyn.LTH p,String t)
        {
                 if(t.startsWith("i"))
                	 return "icmp slt";
                 else
                	 return "fcmp olt";
        } 
        public String visit(Javalette.Absyn.LE p, String t)
        {
        	  if(t.startsWith("i"))
             	 return "icmp sle";
              else
             	 return "fcmp ole";
        }
        public String visit(Javalette.Absyn.GTH p, String t)
        {
        	  if(t.startsWith("i"))
             	 return "icmp sgt";
              else
             	 return "fcmp ogt";

        }
        public String visit(Javalette.Absyn.GE p,String t)
        {
        	  if(t.startsWith("i"))
             	 return "icmp sge";
              else
             	 return "fcmp oge";

        }
        public String visit(Javalette.Absyn.EQU p,String t)
        {
        	  if(t.startsWith("i"))
             	 return "icmp eq";
              else
             	 return "fcmp oeq";

        }
        public String visit(Javalette.Absyn.NE p, String t)
        {
        	  if(t.startsWith("i"))
             	 return "icmp ne";
              else
             	 return "fcmp one";

        }

      }
      private String compileType(Type t,Object obj){
   	   
  	    return t.accept(new typeCompiler(),obj);
     }	
     private class typeCompiler implements Type.Visitor<String,Object>
     {
       public String visit(Javalette.Absyn.Bool p,Object obj)
      {
               return "i1";

      }
      public String visit(Javalette.Absyn.Int p, Object obj)
      {      
      	      return "i32";
      }
      public String visit(Javalette.Absyn.Doub p, Object obj)
      {
              return "double";
      }
      public String visit(Javalette.Absyn.Void p, Object obj)
      {
             return "void";
      }
	 
	  public String visit(Array p, Object arg) {
		  	 String t = compileType(p.type_,arg);
		     if(t.equals("i1"))
		    	 return "i8*";
		     else if(t.equals("i32"))
		    	 return "i32*";
		     else
		    	 return "i64*";
	}
  	
  		
     }
     }
 
 
