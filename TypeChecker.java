import Javalette.Absyn.*;
import Javalette.*;
import java.util.HashMap;
import java.util.LinkedList;

public class TypeChecker {
  public static enum TypeCode {CInt,CDouble,CBool,CVoid,CString,CArrInt,CArrDouble,CArrBool}	
  //define class for function type, which is function signature
  public static class FunType {
         public LinkedList<TypeCode> args ;
         public TypeCode val ;
         public FunType(TypeCode val,LinkedList<TypeCode> args){
         	   
         	   this.args=args;
         	   this.val=val;
         	 
         	}
      public FunType(TypeCode val){

                this.args=null;
                this.val=val;
    
     }
    
         }
  
  /*annotation expression, used to comment the abstract syntax
   *tree, fisrt it wrap the original expression with a AnnoType instance
   * and type annotation, and then wrap the AnnoType. The reason, that we
   * wrap the expression twice is that we need use TypeCode form here, and
   * Type is used in AnnoType wrapper, so we wrap the AnnoType and comment it
   * as TypeCode
   */
  public static class AnnoExpr {
      public final TypeCode typecode_ ;
      public final Expr expr_ ;

   public AnnoExpr(TypeCode t, Expr e){
   	          typecode_ = t ; 
   	          expr_ = e ;
   	 } 
}
  
  /* used to track return statement in the function
  *  check whether it returns what it should return.
  */
   public static class AnnoStmt{
	      public final Object o ;
	      public final Stmt s;

	   public AnnoStmt(Object o,Stmt s){
	   	          this.o=o; 
	   	          this.s=s;
	   	 } 
   }
 
  /* Env denote the context of the program.Like store identification
   * name and value pairs, and store function and function signature
   * pairs
   */
  private static class Env { 
	//top hashMap is current valid context
  	private LinkedList<HashMap<String,TypeCode>> scopes;
    private HashMap<String,FunType> signature;
	  public Env() {
	    this.scopes = new LinkedList<HashMap<String,TypeCode>>();
	    this.signature=new HashMap<String,FunType>();
	     
	     //initialization of printInt,printDouble,printString functions
		 LinkedList<TypeCode> argInt=new LinkedList<TypeCode>();
	     argInt.addLast(TypeCode.CInt);
	     this.addFun("printInt",new FunType(TypeCode.CVoid,argInt));
	     
		 LinkedList<TypeCode> argDouble=new LinkedList<TypeCode>();
	     argDouble.addLast(TypeCode.CDouble);
	     this.addFun("printDouble",new FunType(TypeCode.CVoid,argDouble));
	     
         LinkedList<TypeCode> argStr=new LinkedList<TypeCode>();
	     argStr.addLast(TypeCode.CString);
	     this.addFun("printString",new FunType(TypeCode.CVoid,argStr));
	    	
	     //initialization of two read functions
	     this.addFun("readInt",new FunType(TypeCode.CInt));
	     this.addFun("readDouble",new FunType(TypeCode.CDouble));
	    
	  }
      
	  //get the type of specific variable
	  public TypeCode lookupVar(String x) {
	    for (HashMap<String,TypeCode> scope : scopes) {
		       TypeCode t = scope.get(x);
		    if (t != null)
		      return t;
	      }
	     throw new TypeException("Unknown variable " + x + ".");
      }
	  
	//get the signature of one specific function name
    public FunType lookupFun(String fun){
    	   FunType ft=signature.get(fun);
    	   if(ft!=null)
    	     return ft;
    	   else
    	     throw new TypeException("Unknown function  "+fun);
    	 } 
     
    //add new variable into current context
	public void addVar(String x, TypeCode t) {
	    if (scopes.getFirst().containsKey(x))
		      throw new TypeException("Variable " + x 
					+ " is already declared in this scope.");
	    else
	       scopes.getFirst().put(x,t);
	  }
	
	//add new function into current context
	  public void addFun(String x,FunType ft){
	  	
	  	if(signature.get(x)!=null)
	  	  throw new TypeException("function "+x+ " already exists ");
	  	else
	  	  signature.put(x,ft);  
	  	
	  	}

	   //enter a new block
	   public void enterScope() {
	     scopes.addFirst(new HashMap<String,TypeCode>());
    	 }
	   
       //leave a block
	    public void leaveScope() {
	    scopes.removeFirst();
	  }
    }
  
  // entrance of TypeChecker object, main function
  public void typecheck(Program p) {
		  Pro program=(Pro)p;
		  
		  Env   environment=new Env();
		  
		  //add function and signature pairs
		  for(TopDef d: program.listtopdef_){
		  	    LinkedList<TypeCode> argType_List=new LinkedList<TypeCode>();
		  	    FnDef df=(FnDef)d;
		  	    for(Arg g:df.listarg_){
		  	    	  Args ad=(Args)g;
		  	    	  argType_List.addLast(transferType(ad.type_,environment)); 
		  	    	}
		  	     FunType ft=new FunType(transferType(df.type_,environment),argType_List);
		  	     environment.addFun(df.ident_,ft);   
		  	}
		  
		  //check whether there is main function
		  FunType main=environment.lookupFun("main");
		  //check whether the argument type is int and function
		  //argument list is empty.
		  if(main.val!=TypeCode.CInt || main.args.size()!=0)
			  throw new TypeException("function main should have integer return value and void args");
		  
		  //type check each function 
		  for(TopDef d: program.listtopdef_){
		  	     environment.enterScope(); 
			     checkTopDef(d,environment);
			     environment.leaveScope();
		  	}
		}
  private void checkTopDef(TopDef d,Env environment){
	 	  d.accept(new TopDefChecker(),environment);
		   }
	private class TopDefChecker implements TopDef.Visitor<TopDef,Env>{
   	       public TopDef visit(Javalette.Absyn.FnDef d, Env environment)
          {
   	    	//remember the return type of current function
   	        environment.addVar("ThisFunction",(transferType(d.type_,environment)));
            //type check arguments
   	        for (Arg x : d.listarg_) {
          	   checkArg(x,environment);
             }
             //type check function body
             AnnoStmt s=checkBlock(d.block_,environment);
             
             /*s.o keep track on whether the return statement
             * return correct type
             */
             if(s.o==null&&environment.lookupVar("ThisFunction")!=TypeCode.CVoid){
            	 System.out.println(d.ident_);
            	 throw new TypeException("The function expects a return statement");
             }
             return null;
         }
   	}
	private void checkArg(Arg g,Env environment){
       g.accept(new argChecker(),environment);
    }   	
	private class argChecker implements Arg.Visitor<Object,Env>
    {
         public Object visit(Javalette.Absyn.Args p, Env environment)
         {
            environment.addVar(p.ident_,transferType(p.type_,environment));
            return null;
         }
  
	}
	 private AnnoStmt checkBlock(Block b,Env environment){
	        return b.accept(new blockChecker(),environment);
	 }
	private class blockChecker implements Block.Visitor<AnnoStmt,Env>{
	      public AnnoStmt visit(Javalette.Absyn.Blo p,Env environment)
         {
            environment.enterScope();
            AnnoStmt s=new AnnoStmt(null,null);
            boolean flag=false;
            for (int i=0;i<p.liststmt_.size();i++) {
            	/*if return appears not at the end of the function
            	* then throw an exception
            	*/
            	if(s.o!=null)
            	    flag=true;
            	  s=checkStmt(p.liststmt_.get(i),environment);
		          /*after checkStmt, all expression in the statement
            	  * has been annotated, so we need to set it back.
            	  */
            	  p.liststmt_.set(i,s.s);
		    	   
		   }
          environment.leaveScope();
          if(flag==true)
        	  return new AnnoStmt(flag,new BStmt(p));
          return new AnnoStmt(s.o,new BStmt(p));
        }

    }
	
	private AnnoStmt checkStmt(Stmt s,Env environment){
		      return s.accept(new stmtChecker(),environment);
	  }
	private class stmtChecker implements Stmt.Visitor<AnnoStmt,Env>{
	    public AnnoStmt visit(Javalette.Absyn.Empty p, Env environment)
     {
	    return new AnnoStmt(null,p);
     }
      public AnnoStmt visit(Javalette.Absyn.BStmt p, Env environment)
     {
        AnnoStmt s=checkBlock(p.block_,environment);
		return new AnnoStmt(s.o,s.s);
      }
	  public AnnoStmt visit(Javalette.Absyn.Decl p, Env environment)
      {  
	    for (int i=0;i<p.listitem_.size();i++) {
        	 Item x=p.listitem_.get(i);
        	 //check whether it is initialization or declaration
	    	 if(x instanceof NoInit){
                 NoInit nx=(NoInit)x;
        		 environment.addVar(nx.ident_,transferType(p.type_,environment));
        	      
        	 }else{ 
        		 Init ix=(Init)x;
        		 AnnoExpr ae=checkExpr(ix.expr_,transferType(p.type_,environment),environment);
                 environment.addVar(ix.ident_,transferType(p.type_,environment));
        	     p.listitem_.set(i, new Init(ix.ident_,ae.expr_));
        	 }
		 }

        return new AnnoStmt(null,p);
      }
	    public AnnoStmt visit(Javalette.Absyn.Ass p, Env environment)
      { 
            AnnoExpr e=inferExpr(p.expr_,environment);
            TypeCode t=environment.lookupVar(p.ident_);
			if (e.typecode_ != t) {
	                throw new TypeException(PrettyPrinter.print(p.expr_) 
				                    + " has type " + e.typecode_ 
				                    + " expected " + t);
	                        }
            return new AnnoStmt(null,new Ass(p.ident_,e.expr_));     
           
      }
	    public AnnoStmt visit(Javalette.Absyn.Incr p, Env environment)
      {
            TypeCode type=environment.lookupVar(p.ident_);
			if(type!=TypeCode.CInt){
			     	 throw new TypeException(PrettyPrinter.print(p)+":  the operant must be integer");
			}
	      
           return new AnnoStmt(null,p);
      }
	   public AnnoStmt visit(Javalette.Absyn.Decr p, Env environment)
      {
            TypeCode type=environment.lookupVar(p.ident_); 
			if(type!=TypeCode.CInt){
			     	 throw new TypeException(PrettyPrinter.print(p)+":the operant must be integer");
			}
	      
           return new AnnoStmt(null,p);
      }
	  public AnnoStmt visit(Javalette.Absyn.Ret p, Env environment)
     {
           TypeCode type=environment.lookupVar("ThisFunction");            
           AnnoExpr e=checkExpr(p.expr_,type,environment);
           return new AnnoStmt("returnStatement",new Javalette.Absyn.Ret(e.expr_));
     }
	  public AnnoStmt visit(Javalette.Absyn.VRet p,Env environment)
     {
		  TypeCode type=environment.lookupVar("ThisFunction");
		  if(type!=TypeCode.CVoid)
			  throw new TypeException("The return value should be "+type.name());
          return new AnnoStmt(null,p);
     }
	   public AnnoStmt visit(Javalette.Absyn.Cond p, Env environment)
      {
		   AnnoExpr e=inferExpr(p.expr_,environment);
		    //condition in if statement could be int,bool or double
	        if(e.typecode_==TypeCode.CBool||e.typecode_==TypeCode.CInt||e.typecode_==TypeCode.CDouble);
	        else
	          throw new TypeException("The condition of while statement must be boolean value");
          
	      AnnoStmt s=checkStmt(p.stmt_,environment);
          Boolean  b=isConstant(p.expr_);
        
          /*The logic here is a little bit complex. If the condition is constant
           *true, then the s.o in p.stmt_ is import, if the condition is constant
           *false or the condition is not constant, then the s.o of this statement
           *does not depend on p.stmt_
           */
          if(b)
        	   return new AnnoStmt(checkConstant(p.expr_)?s.o:null,new Cond(e.expr_,s.s));
          else
        	   return new AnnoStmt(null,new Cond(e.expr_,s.s));
     }
    public AnnoStmt visit(Javalette.Absyn.CondElse p, Env environment)
     {
    	 AnnoExpr e=inferExpr(p.expr_,environment);
    	//condition in if statement could be int,bool or double
         if(e.typecode_!=TypeCode.CBool && e.typecode_ != TypeCode.CInt && e.typecode_ != TypeCode.CDouble) 
              	  throw new TypeException("The condition of while statement must be boolean value");
          
          AnnoStmt s1=checkStmt(p.stmt_1,environment); 
		  AnnoStmt s2=checkStmt(p.stmt_2,environment);
		  
		  /*if there are return statement in true branch, then
		  * check whether the condition is constant.
		  */
		  if(s1.o!=null && s2.o==null){
			  Boolean b=isConstant(p.expr_);
			  if(b){
				 return new AnnoStmt(checkConstant(p.expr_)?s1.o:s2.o,new CondElse(e.expr_,s1.s,s2.s));
			  }  
			else
		         return new AnnoStmt(s2.o,new CondElse(e.expr_,s1.s,s2.s));
		  }
		  
		  /*if there are return statement in false branch, then
	       * check whether the condition is constant.
		  */
		  else if(s1.o==null&&s2.o!=null){
			   Boolean b=isConstant(p.expr_);
			   if(b){
	                return new AnnoStmt(checkConstant(p.expr_)?s1.o:s2.o,new CondElse(e.expr_,s1.s,s2.s));
				   }
			   else
				    return new AnnoStmt(s1.o,new CondElse(e.expr_,s1.s,s2.s));
		  }
		  else{
			  return new AnnoStmt(s1.o,new CondElse(e.expr_,s1.s,s2.s));
		  }
		  }
    
	    public AnnoStmt visit(Javalette.Absyn.While p, Env environment)
      {
       
          AnnoExpr e=inferExpr(p.expr_,environment);
          //condition in if statement could be int,bool or double
          if(e.typecode_!=TypeCode.CBool&&e.typecode_!=TypeCode.CInt&&e.typecode_!=TypeCode.CDouble) 
          	  throw new TypeException("The condition of while statement must be boolean value");
 
          AnnoStmt s=checkStmt(p.stmt_,environment);
          Boolean b=isConstant(p.expr_);
          if(b){
        	  Boolean t=checkConstant(p.expr_);
        	  
        	  if(t)
        		  if(s.o==null)
                    throw new TypeException("infinite loop in"+PrettyPrinter.print(p));
        		  else
        			return new AnnoStmt(s.o,new While(e.expr_,s.s));
        	  else
        		  return new AnnoStmt(null,new While(e.expr_,s.s));
          }
         
          return new AnnoStmt(null,new While(e.expr_,s.s));
     }
     public AnnoStmt visit(Javalette.Absyn.SExp p,Env environment)
     {
        AnnoExpr e=inferExpr(p.expr_,environment);
        
		return new AnnoStmt(null,new SExp(e.expr_));
  
     }
	@Override
	public AnnoStmt visit(ForLoop p, Env environment) {
		TypeCode tArr = environment.lookupVar(p.ident_2);
		TypeCode t = transferType(p.type_,environment);
		boolean c1 = t==TypeCode.CInt && tArr==TypeCode.CArrInt;
		boolean c2 = t==TypeCode.CDouble && tArr==TypeCode.CArrDouble;
		boolean c3 = t==TypeCode.CBool && tArr==TypeCode.CArrBool;
		if(c1||c2||c3){
			environment.enterScope();
			environment.addVar(p.ident_1, t);
			AnnoStmt s = checkStmt(p.stmt_,environment);
			environment.leaveScope();
			return new AnnoStmt(s.o,p);
		}
			
		else
			throw new TypeException("type mismatch in for loop	");
	}
	@Override
	public AnnoStmt visit(Ass2 p, Env environment) {
		AnnoExpr e=inferExpr(p.expr_2,environment); 
		TypeCode t=environment.lookupVar(p.ident_);
        boolean c1 = e.typecode_ ==TypeCode.CInt && t==TypeCode.CArrInt;
		boolean c2 = e.typecode_==TypeCode.CDouble && t==TypeCode.CArrDouble;
		boolean c3 = e.typecode_==TypeCode.CBool && t==TypeCode.CArrBool;
		if (!(c1||c2||c3)) { 
                throw new TypeException(PrettyPrinter.print(p.expr_2) 
			                    + " has type " + e.typecode_ 
			                    + " expected " + t);
                        }
        return new AnnoStmt(null,new Ass2(p.ident_,p.expr_1,e.expr_));  
		 
	}
     }
    private AnnoExpr  checkExpr(Expr e,TypeCode t,Env environment){
	 	   AnnoExpr tf=inferExpr(e,environment);
	 	  try{
	 	   if (tf.typecode_ != t) {
	                      throw new TypeException(PrettyPrinter.print(tf.expr_) 
				                    + " has type " + tf 
				                    + " expected " + t);
	                        }
	 	  }
	 	  catch(Exception e1){
	 		   e1.printStackTrace();
	 	  }
	 	   return tf;
	 }
	private Boolean checkConstant(Expr e){
	       if(e instanceof ELitTrue)
	    	   return   true;
	       else
	    	   return false;
	}
	private Boolean isConstant(Expr e){
		 if(e instanceof ELitTrue || e instanceof ELitFalse)
			 return true;
		 else
			 return false;
	}
     
    private AnnoExpr inferExpr(Expr e,Env environment){
	  
	       return e.accept(new exprInferer(),environment);
	}
    	
    private  class exprInferer implements  Expr.Visitor<AnnoExpr,Env>{
    public AnnoExpr visit(AnnoType p, Env arg) {
    	 	return new AnnoExpr(transferType(p.type_,arg),p);
    }
    	public AnnoExpr visit(Javalette.Absyn.EVar p,Env environment)
      {
    	 TypeCode t=environment.lookupVar(p.ident_);
         return new AnnoExpr(t,new AnnoType(transferTypeCode(t),p));
      }
	   public AnnoExpr visit(Javalette.Absyn.ELitInt p, Env environment)
    {
       return new AnnoExpr(TypeCode.CInt,new AnnoType(transferTypeCode(TypeCode.CInt),p));
     
    }
    public AnnoExpr visit(Javalette.Absyn.ELitDoub p,Env environment)
    { 
    	return new AnnoExpr(TypeCode.CDouble,new AnnoType(transferTypeCode(TypeCode.CDouble),p));
    }
    public AnnoExpr visit(Javalette.Absyn.ELitTrue p,Env environment)
    { 
    	return new AnnoExpr(TypeCode.CBool,new AnnoType(transferTypeCode(TypeCode.CBool),p));
    }
    public AnnoExpr visit(Javalette.Absyn.ELitFalse p,Env enterScope)
    { 
    	return new AnnoExpr(TypeCode.CBool,new AnnoType(transferTypeCode(TypeCode.CBool),p));
    }
    public AnnoExpr visit(Javalette.Absyn.EString p,Env environment)
    { 
    	return new AnnoExpr(TypeCode.CString,new AnnoType(null,p));
    }
	
	public AnnoExpr visit(Javalette.Absyn.EApp p, Env environment)
    {
       FunType ft=environment.lookupFun(p.ident_);
       LinkedList<TypeCode> ft_list=ft.args;
       int len;
       if(ft_list==null)
         len=0;
       else
         len=ft_list.size();
       
       TypeCode type1=null;
       TypeCode type2=null;
       if(len==p.listexpr_.size()){
      	 
        for (int i=0;i<len;i++) {
           
           AnnoExpr e=inferExpr(p.listexpr_.get(i),environment);
            
           type1=e.typecode_;
           
           p.listexpr_.set(i,new AnnoType(transferTypeCode(type1),e.expr_));
           
           type2=ft_list.get(i);
           
           if(type1!=type2){
           	 throw new TypeException("u have given the wrong type of arguments of function "+p.ident_);
           	}
        }
     }
      else 
         throw new TypeException("the function "+p.ident_+" is not given right number of arguments");

      return new AnnoExpr(ft.val,new AnnoType(transferTypeCode(ft.val),p));
    }
      public AnnoExpr visit(Javalette.Absyn.Neg p,Env environment)
    {
    	AnnoExpr e=inferExpr(p.expr_,environment);
        if((e.typecode_ == TypeCode.CInt)||(e.typecode_ == TypeCode.CDouble)){
        	 return  new AnnoExpr(e.typecode_,new AnnoType(transferTypeCode(e.typecode_),new Neg(e.expr_)));    	
		}   
		else
		    throw new TypeException("operant of Neg must be numberic");
        
       
    }
    public AnnoExpr visit(Javalette.Absyn.Not p,Env environment)
    {
	        AnnoExpr e=inferExpr(p.expr_,environment);      
            if(e.typecode_==TypeCode.CInt || e.typecode_==TypeCode.CDouble || e.typecode_==TypeCode.CBool)
                  return new AnnoExpr(e.typecode_,new AnnoType(transferTypeCode(e.typecode_),new Not(e.expr_)));
				  else  
				   throw new TypeException("operant of Not must be boolean value");
    }
	 public AnnoExpr visit(Javalette.Absyn.EMul p,Env environment)
    {
           AnnoExpr e1=inferExpr(p.expr_1,environment);
           AnnoExpr  e2=inferExpr(p.expr_2,environment);
           
		   if(e1.typecode_==TypeCode.CInt&&e2.typecode_==TypeCode.CInt){
           	     return new AnnoExpr(TypeCode.CInt,new AnnoType(transferTypeCode(TypeCode.CInt),new EMul(e1.expr_,p.mulop_,e2.expr_)));
           	}
           else if((!(p.mulop_ instanceof Mod))&& e1.typecode_==TypeCode.CDouble&&e2.typecode_==TypeCode.CDouble){
           	
           	  return new AnnoExpr(TypeCode.CDouble,new AnnoType(transferTypeCode(TypeCode.CDouble),new EMul(e1.expr_,p.mulop_,e2.expr_)));
 
           	}
           else throw new TypeException("the operant of multification must be both int or double");
    }
	 
    public AnnoExpr visit(Javalette.Absyn.EAdd p, Env environment)
    {
           AnnoExpr e1=inferExpr(p.expr_1,environment);
           AnnoExpr  e2=inferExpr(p.expr_2,environment);
           
		   if(e1.typecode_==TypeCode.CInt&&e2.typecode_==TypeCode.CInt){
         	 return new AnnoExpr(TypeCode.CInt,new AnnoType(transferTypeCode(TypeCode.CInt),new EAdd(e1.expr_,p.addop_,e2.expr_)));
            }
           else if(e1.typecode_==TypeCode.CDouble&&e2.typecode_==TypeCode.CDouble){
         	 return new AnnoExpr(TypeCode.CDouble,new AnnoType(transferTypeCode(TypeCode.CDouble),new EAdd(e1.expr_,p.addop_,e2.expr_)));
           }
           else throw new TypeException("the operant of addition must be both int or double");
    }
    
    public AnnoExpr visit(Javalette.Absyn.ERel p,Env environment)
    {
    	    AnnoExpr e1=inferExpr(p.expr_1,environment);
        	AnnoExpr e2=inferExpr(p.expr_2,environment);
            
        
           if(e1.typecode_==TypeCode.CInt&&e2.typecode_==TypeCode.CInt){
           	return new AnnoExpr(TypeCode.CBool,new AnnoType(transferTypeCode(TypeCode.CBool),new ERel(e1.expr_,p.relop_,e2.expr_)));
        	 }
           else if(e1.typecode_==TypeCode.CDouble&&e2.typecode_==TypeCode.CDouble){
           	
        	 	return new AnnoExpr(TypeCode.CBool,new AnnoType(transferTypeCode(TypeCode.CBool),new ERel(e1.expr_,p.relop_,e2.expr_)));
        	      
           	}
           else if(e1.typecode_==TypeCode.CBool&&e2.typecode_==TypeCode.CBool){
        	   if(p.relop_ instanceof EQU || p.relop_ instanceof NE){
        		 	return new AnnoExpr(TypeCode.CBool,new AnnoType(transferTypeCode(TypeCode.CBool),new ERel(e1.expr_,p.relop_,e2.expr_)));
        		      
               }
        	   else
        		  throw new TypeException("the operant of relation is not right");
           }
           else throw new TypeException("the operant of relation is not right");
    }
    
    public AnnoExpr visit(Javalette.Absyn.EAnd p, Env environment)
    {
	   AnnoExpr e1=checkExpr(p.expr_1,TypeCode.CBool,environment);
	   AnnoExpr e2=checkExpr(p.expr_2,TypeCode.CBool,environment);
        
	 	return new AnnoExpr(TypeCode.CBool,new AnnoType(transferTypeCode(TypeCode.CBool),new EAnd(e1.expr_,e2.expr_)));
    }
    public AnnoExpr visit(Javalette.Absyn.EOr p,Env environment)
    {
       AnnoExpr e1=checkExpr(p.expr_1,TypeCode.CBool,environment);
	   AnnoExpr e2=checkExpr(p.expr_2,TypeCode.CBool,environment);
        
   	return new AnnoExpr(TypeCode.CBool,new AnnoType(transferTypeCode(TypeCode.CBool),new EOr(e1.expr_,e2.expr_)));
    }
	@Override
	public AnnoExpr visit(NewArray p, Env environment) {
		AnnoExpr e = checkExpr(p.expr_,TypeCode.CInt,environment);
		TypeCode t = transferType(p.type_,environment);
		if(t.equals(TypeCode.CInt))
		    return new AnnoExpr(TypeCode.CArrInt,new AnnoType(p.type_,p));
		else if(t.equals(TypeCode.CDouble))
		    return new AnnoExpr(TypeCode.CArrDouble,new AnnoType(p.type_,p));
		else if(t.equals(TypeCode.CBool))
		    return new AnnoExpr(TypeCode.CArrBool,new AnnoType(p.type_,p));
		else
			throw new TypeException("error declaration of new array with type "+ t);
	}
	@Override
	public AnnoExpr visit(ArrayLen p, Env arg) {
		 
		return new AnnoExpr(TypeCode.CInt,new AnnoType(transferTypeCode(TypeCode.CInt),p));
	}
	@Override
	public AnnoExpr visit(ArrayEle p, Env environment) {
		AnnoExpr e = checkExpr(p.expr_,TypeCode.CInt,environment);
		TypeCode t= environment.lookupVar(p.ident_);
		if(t==TypeCode.CArrInt)
			return new AnnoExpr(TypeCode.CInt,new ArrayEle(p.ident_,e.expr_)); 
		else if(t==TypeCode.CArrDouble)
			return new AnnoExpr(TypeCode.CDouble,new ArrayEle(p.ident_,e.expr_));
		else

			return new AnnoExpr(TypeCode.CBool,new ArrayEle(p.ident_,e.expr_));
		}
	
	}	
	private   Type     transferTypeCode(TypeCode tc){
	    if(tc.equals(TypeCode.CBool)){
	    	
	    	return new Bool();
	    }
	    else if(tc.equals(TypeCode.CDouble)){
	    	return new Doub();
	    }
	    else if(tc.equals(TypeCode.CInt)){
	       return new Int();
	    }
	    else if(tc.equals(TypeCode.CBool))
	      return new Javalette.Absyn.Void();
	    else
	    { 
	    	if(tc.equals(TypeCode.CArrInt))
	    	 return new Javalette.Absyn.Array(new Int());
	    	else if(tc.equals(TypeCode.CArrDouble))
	    	 return new Javalette.Absyn.Array(new Doub());
	    	else
	    	 return new Javalette.Absyn.Array(new Bool());
	    }
}
   private TypeCode transferType(Type t,Env environment){
	   
	    return t.accept(new typeTransfer(),environment);
   }	
   private class typeTransfer implements Type.Visitor<TypeCode,Env>
   {
     public TypeCode visit(Javalette.Absyn.Bool p, Env arg)
    {
             return TypeCode.CBool;

    }
    public TypeCode visit(Javalette.Absyn.Int p, Env arg)
    {      
    	      return TypeCode.CInt;
    }
    public TypeCode visit(Javalette.Absyn.Doub p, Env arg)
    {
            return TypeCode.CDouble;
    }
    public TypeCode visit(Javalette.Absyn.Void p, Env arg)
    {
           return TypeCode.CVoid;
    }
	@Override
	public TypeCode visit(Array p, Env arg) {
			if(transferType(p.type_,arg)==TypeCode.CInt)
				return TypeCode.CArrInt;
			else if(transferType(p.type_,arg)==TypeCode.CDouble)
				return TypeCode.CArrDouble;
			else if(transferType(p.type_,arg)==TypeCode.CBool)
				return TypeCode.CArrBool;
			else
				throw new TypeException("array must be int,double or bool");
	}
 
		
   }
}