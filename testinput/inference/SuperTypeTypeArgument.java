import universe.qual.*;
import java.util.List;

class A<T> {
    T t;
}

public class World {
	protected void step(@Rep ContactEdge contactEdge) {
		@Any
		ContactConstraint contactConstraint = contactEdge.interaction;
	}
}
