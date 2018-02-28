import universe.qual.*;

class D<T extends Object>{}

public class TypeVariableUse{

	void foo() {
		D<String> D = new @Peer D<String>();
	}
}
