package GUTI;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.framework.source.SupportedLintOptions;

import javax.annotation.processing.SupportedOptions;

import checkers.inference.BaseInferrableChecker;
import checkers.inference.InferenceChecker;
import checkers.inference.InferenceVisitor;


/**
 * The main checker class for the Generic Universe Types checker.
 *
 * @author wmdietl
 */
// Keep these synchronized with the superclass.
@SupportedOptions( { "warn" } )
@SupportedLintOptions({"allowLost", "checkOaM", "checkStrictPurity"})
public class GUTIChecker extends BaseInferrableChecker {

    @Override
    public BaseAnnotatedTypeFactory createRealTypeFactory() {
        // Return the GUTIAnnotatedTypeFactory so that it can carry
        // GUTIVariableAnnotator
        return new GUTIAnnotatedTypeFactory(this);
    }

    @Override
    public InferenceVisitor<?, ?> createVisitor(InferenceChecker checker,
            BaseAnnotatedTypeFactory factory, boolean infer) {
        return new GUTIVisitor(this, checker, factory, infer);
    }

    @Override
    public boolean withCombineConstraints() {
        return true;
    }

}