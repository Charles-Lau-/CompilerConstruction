package Javalette;
import Javalette.Absyn.*;
/** BNFC-Generated Abstract Visitor */
public class AbstractVisitor<R,A> implements AllVisitor<R,A> {
/* Program */
    public R visit(Javalette.Absyn.Pro p, A arg) { return visitDefault(p, arg); }
    public R visitDefault(Javalette.Absyn.Program p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }
/* TopDef */
    public R visit(Javalette.Absyn.FnDef p, A arg) { return visitDefault(p, arg); }
    public R visitDefault(Javalette.Absyn.TopDef p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }
/* Arg */
    public R visit(Javalette.Absyn.Args p, A arg) { return visitDefault(p, arg); }
    public R visitDefault(Javalette.Absyn.Arg p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }
/* Block */
    public R visit(Javalette.Absyn.Blo p, A arg) { return visitDefault(p, arg); }
    public R visitDefault(Javalette.Absyn.Block p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }
/* Stmt */
    public R visit(Javalette.Absyn.Empty p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.BStmt p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.Decl p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.Ass p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.Ass2 p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.Incr p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.Decr p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.Ret p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.VRet p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.Cond p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.CondElse p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.While p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.SExp p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.ForLoop p, A arg) { return visitDefault(p, arg); }
    public R visitDefault(Javalette.Absyn.Stmt p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }
/* Item */
    public R visit(Javalette.Absyn.NoInit p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.Init p, A arg) { return visitDefault(p, arg); }
    public R visitDefault(Javalette.Absyn.Item p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }
/* Type */
    public R visit(Javalette.Absyn.Int p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.Doub p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.Bool p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.Void p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.Array p, A arg) { return visitDefault(p, arg); }
    public R visitDefault(Javalette.Absyn.Type p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }
/* Expr */
    public R visit(Javalette.Absyn.EVar p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.ELitInt p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.ELitDoub p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.ELitTrue p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.ELitFalse p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.EApp p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.EString p, A arg) { return visitDefault(p, arg); }

    public R visit(Javalette.Absyn.Neg p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.Not p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.NewArray p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.ArrayLen p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.ArrayEle p, A arg) { return visitDefault(p, arg); }

    public R visit(Javalette.Absyn.EMul p, A arg) { return visitDefault(p, arg); }

    public R visit(Javalette.Absyn.EAdd p, A arg) { return visitDefault(p, arg); }

    public R visit(Javalette.Absyn.ERel p, A arg) { return visitDefault(p, arg); }

    public R visit(Javalette.Absyn.EAnd p, A arg) { return visitDefault(p, arg); }

    public R visit(Javalette.Absyn.AnnoType p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.EOr p, A arg) { return visitDefault(p, arg); }

    public R visitDefault(Javalette.Absyn.Expr p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }
/* AddOp */
    public R visit(Javalette.Absyn.Plus p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.Minus p, A arg) { return visitDefault(p, arg); }
    public R visitDefault(Javalette.Absyn.AddOp p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }
/* MulOp */
    public R visit(Javalette.Absyn.Times p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.Div p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.Mod p, A arg) { return visitDefault(p, arg); }
    public R visitDefault(Javalette.Absyn.MulOp p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }
/* RelOp */
    public R visit(Javalette.Absyn.LTH p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.LE p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.GTH p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.GE p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.EQU p, A arg) { return visitDefault(p, arg); }
    public R visit(Javalette.Absyn.NE p, A arg) { return visitDefault(p, arg); }
    public R visitDefault(Javalette.Absyn.RelOp p, A arg) {
      throw new IllegalArgumentException(this.getClass().getName() + ": " + p);
    }

}
