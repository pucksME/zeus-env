package zeus.specification;

public abstract class Specification {
  Context context;
  Action action;

  public Specification(Context context, Action action) {
    this.context = context;
    this.action = action;
  }

  public abstract boolean verify();

  public Action getAction() {
    return action;
  }
}
