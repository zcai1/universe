import universe.qual.Rep;
import universe.qual.Peer;

public class Test {
    Object foo;

    public void init() {
        this.foo = new @Rep Object();

        do_something(this.foo);
        if(foo == null) {
		}
    }

    void do_something(@Peer Object a) {}
}
