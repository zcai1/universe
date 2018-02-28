import universe.qual.Any;
import universe.qual.Peer;
import universe.qual.*;

class B {
	// :: fixable-error: (return.type.incompatible)
	@Rep Object foo (@Peer B this, @Any Object o) { return new @Rep Object();}
}

public class MethodInvocation {

	void foo(@Peer B b, @Rep Object op) {
		// :: fixable-error: (argument.type.incompatible)
		@Any Object o = b.foo(op);
	}


}
