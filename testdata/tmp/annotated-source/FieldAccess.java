import universe.qual.Self;
import universe.qual.Rep;
import universe.qual.*;

class A {
	@Rep Object o;
}

public class FieldAccess {
	void foo(@Self A a) {
		// :: fixable-error: (assignment.type.incompatible)
		@Rep Object fo = a.o;
	}
}
