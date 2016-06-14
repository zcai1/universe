package GUTI;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.treeannotator.ImplicitsTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;

import GUT.GUTAnnotatedTypeFactory;
import checkers.inference.ConstraintManager;
import checkers.inference.InferenceAnnotatedTypeFactory;
import checkers.inference.InferenceTreeAnnotator;
import checkers.inference.InferrableAnnotatedTypeFactory;
import checkers.inference.InferrableChecker;
import checkers.inference.SlotManager;
import checkers.inference.VariableAnnotator;

public class GUTIAnnotatedTypeFactory extends GUTAnnotatedTypeFactory implements InferrableAnnotatedTypeFactory{

    public GUTIAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        // TODO Auto-generated constructor stub
    }

    @Override
    public VariableAnnotator getVariableAnnotator(
            InferenceAnnotatedTypeFactory typeFactory,
            AnnotatedTypeFactory realTypeFactory, InferrableChecker realChecker,
            SlotManager slotManager, ConstraintManager constraintManager) {
        // TODO Auto-generated method stub
        return new GUTIVariableAnnotator(typeFactory, realTypeFactory,
                realChecker, slotManager, constraintManager);
    }

    @Override
    public TreeAnnotator getInferenceTreeAnnotator(
            InferenceAnnotatedTypeFactory atypeFactory,
            InferrableChecker realChecker, VariableAnnotator variableAnnotator,
            SlotManager slotManager) {
        // TODO Auto-generated method stub
        // GUTI doesn't need it own tree annotator, and this method is called.
        // So returns default InferenceTreeAnnotator
        return new ListTreeAnnotator(new ImplicitsTreeAnnotator(atypeFactory),
                new InferenceTreeAnnotator(atypeFactory, realChecker, this,
                        variableAnnotator, slotManager));
    }

}
