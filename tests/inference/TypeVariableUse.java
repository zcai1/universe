import universe.qual.*;

public class TypeVariableUse{

	class D<T extends Object>{}

	void foo() {
		// :: fixable-error: (assignment.type.incompatible)
		@Rep D<String> D = new D<String>();
	}
}
