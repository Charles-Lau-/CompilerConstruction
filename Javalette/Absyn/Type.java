package Javalette.Absyn; // Java Package generated by the BNF Converter.

public abstract class Type implements java.io.Serializable {
  public abstract <R,A> R accept(Type.Visitor<R,A> v, A arg);
  public interface Visitor <R,A> {
    public R visit(Javalette.Absyn.Int p, A arg);
    public R visit(Javalette.Absyn.Doub p, A arg);
    public R visit(Javalette.Absyn.Bool p, A arg);
    public R visit(Javalette.Absyn.Void p, A arg);
    public R visit(Javalette.Absyn.Array p, A arg);

  }

}
