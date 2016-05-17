package GUTI;

import org.checkerframework.framework.type.QualifierHierarchy;

import java.util.Collection;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;

import checkers.inference.InferenceSolution;
import checkers.inference.model.Constraint;
import checkers.inference.model.Serializer;
import checkers.inference.model.Slot;
import constraintsolver.ConstraintSolver;

public class GUTIConstraintSolver extends ConstraintSolver {


    @Override
    public InferenceSolution solve(Map<String, String> configuration,
            Collection<Slot> slots, Collection<Constraint> constraints,
            QualifierHierarchy qualHierarchy,
            ProcessingEnvironment processingEnvironment) {

        configure(configuration);
        Elements elements = realBackEnd.processingEnvironment.getElementUtils();
        Serializer<?, ?> defaultSerializer = createSerializer(backEndType,
                elements);
        realBackEnd = createBackEnd(backEndType, configuration, slots,
                constraints, qualHierarchy, processingEnvironment,
                defaultSerializer);
        return solve();
    }

    protected Serializer<?, ?> createSerializer(String value,
            Elements elements) {
        return new GUTIConstraintSerializer<>(value, elements);
    }
}
