package GUTI;

import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;

import checkers.inference.ConstraintManager;
import checkers.inference.InferenceAnnotatedTypeFactory;
import checkers.inference.InferrableChecker;
import checkers.inference.SlotManager;
import checkers.inference.VariableAnnotator;
import checkers.inference.model.VariableSlot;

public class GUTIVariableAnnotator extends VariableAnnotator {

    public GUTIVariableAnnotator(InferenceAnnotatedTypeFactory typeFactory,
            AnnotatedTypeFactory realTypeFactory, InferrableChecker realChecker,
            SlotManager slotManager, ConstraintManager constraintManager) {
        super(typeFactory, realTypeFactory, realChecker, slotManager,
                constraintManager);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void addClassBoundAnnotation(AnnotatedDeclaredType classType) {
        return;
    }

    @Override
    protected void getDeclBoundAndAddDeclarationConstraints(
            AnnotatedDeclaredType adt, VariableSlot primary) {
        return;
    }


}
