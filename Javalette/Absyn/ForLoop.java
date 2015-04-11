package Javalette.Absyn; // Java Package generated by the BNF Converter.

public class ForLoop extends Stmt {
  public final Type type_;
  public final String ident_1, ident_2;
  public final Stmt stmt_;

  public ForLoop(Type p1, String p2, String p3, Stmt p4) { type_ = p1; ident_1 = p2; ident_2 = p3; stmt_ = p4; }

  public <R,A> R accept(Javalette.Absyn.Stmt.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof Javalette.Absyn.ForLoop) {
      Javalette.Absyn.ForLoop x = (Javalette.Absyn.ForLoop)o;
      return this.type_.equals(x.type_) && this.ident_1.equals(x.ident_1) && this.ident_2.equals(x.ident_2) && this.stmt_.equals(x.stmt_);
    }
    return false;
  }

  public int hashCode() {
    return 37*(37*(37*(this.type_.hashCode())+this.ident_1.hashCode())+this.ident_2.hashCode())+this.stmt_.hashCode();
  }


}