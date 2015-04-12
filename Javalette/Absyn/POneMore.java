package Javalette.Absyn; // Java Package generated by the BNF Converter.

public class POneMore extends PostFix {
  public final Expr expr_;

  public POneMore(Expr p1) { expr_ = p1; }

  public <R,A> R accept(Javalette.Absyn.PostFix.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof Javalette.Absyn.POneMore) {
      Javalette.Absyn.POneMore x = (Javalette.Absyn.POneMore)o;
      return this.expr_.equals(x.expr_);
    }
    return false;
  }

  public int hashCode() {
    return this.expr_.hashCode();
  }


}