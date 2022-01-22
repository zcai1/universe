import universe.qual.*;

public class ConstructorCall {

	class C {
		public C(Object o){}
	}

	void foo(@Peer Object op) {
		// :: fixable-error: (argument.type.incompatible)
		C c = new @Rep C(op);
	}
}
