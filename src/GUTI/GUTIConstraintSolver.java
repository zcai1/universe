package GUTI;

import checkers.inference.model.Serializer;
import constraintsolver.ConstraintSolver;

public class GUTIConstraintSolver extends ConstraintSolver {

    @Override
    protected Serializer<?, ?> createSerializer(String value) {
        return new GUTIConstraintSerializer<>(value);
    }
}
