package universe.solver;

import checkers.inference.solver.backend.encoder.ConstraintEncoderFactory;
import checkers.inference.solver.backend.encoder.combine.CombineConstraintEncoder;
import checkers.inference.solver.backend.maxsat.MaxSatFormatTranslator;
import checkers.inference.solver.backend.maxsat.encoder.MaxSATConstraintEncoderFactory;
import checkers.inference.solver.frontend.Lattice;
import checkers.inference.util.ConstraintVerifier;
import org.sat4j.core.VecInt;

public class GUTFormatTranslator extends MaxSatFormatTranslator {

    public GUTFormatTranslator(Lattice lattice) {
        super(lattice);

    }

    @Override
    protected ConstraintEncoderFactory<VecInt[]> createConstraintEncoderFactory(ConstraintVerifier verifier) {
        return new MaxSATConstraintEncoderFactory(lattice, verifier, typeToInt, this){
            @Override
            public CombineConstraintEncoder<VecInt[]> createCombineConstraintEncoder() {
                return new GUTCombineConstraintEncoder(lattice, verifier, typeToInt);
            }
        };
    }
}
