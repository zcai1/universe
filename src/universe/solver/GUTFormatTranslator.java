package universe.solver;

import checkers.inference.solver.backend.encoder.ConstraintEncoderFactory;
import checkers.inference.solver.backend.encoder.vpa.VPAConstraintEncoder;
import checkers.inference.solver.backend.maxsat.MaxSatFormatTranslator;
import checkers.inference.solver.backend.maxsat.encoder.MaxSATConstraintEncoderFactory;
import checkers.inference.solver.frontend.Lattice;
import org.sat4j.core.VecInt;

public class GUTFormatTranslator extends MaxSatFormatTranslator {

    public GUTFormatTranslator(Lattice lattice) {
        super(lattice);

    }

    @Override
    protected ConstraintEncoderFactory<VecInt[]> createConstraintEncoderFactory() {
        return new MaxSATConstraintEncoderFactory(lattice, typeToInt, this){
            @Override
            public VPAConstraintEncoder<VecInt[]> createVPAConstraintEncoder() {
                return new GUTCombineConstraintEncoder(lattice, typeToInt);
            }
        };
    }
}
