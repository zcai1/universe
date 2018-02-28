import universe.qual.*;

class Test {
	@Rep Object trusted;
	@Any Object untrusted;

	Object foo() {
		return untrusted;
	}

	void bar() {
		trusted = foo();
	}

}
