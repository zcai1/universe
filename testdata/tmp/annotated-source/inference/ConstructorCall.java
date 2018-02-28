package inference;

import universe.qual.Rep;
import universe.qual.Any;
import universe.qual.Self;
import universe.qual.*;

class C {
	public @Self C(@Any Object o){}
}

public class ConstructorCall {

	void foo(@Peer Object op) {
		// :: fixable-error: (argument.type.incompatible)
		@Rep
		C c = new @Rep C(op);
	}
}
