import universe.qual.*;

public class FieldAccess {

	class A {
		Object o;
	}

	void foo(A a) {
		// :: fixable-error: (assignment.type.incompatible)
		@Rep Object fo = a.o;
	}
}
