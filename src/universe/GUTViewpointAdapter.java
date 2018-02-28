package universe;


import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.util.FrameworkViewpointAdapter;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ErrorReporter;

import javax.lang.model.element.AnnotationMirror;

import static universe.GUTChecker.ANY;
import static universe.GUTChecker.BOTTOM;
import static universe.GUTChecker.LOST;
import static universe.GUTChecker.PEER;
import static universe.GUTChecker.REP;
import static universe.GUTChecker.SELF;

public class GUTViewpointAdapter extends FrameworkViewpointAdapter {

    @Override
    protected AnnotationMirror getModifier(AnnotatedTypeMirror atm, AnnotatedTypeFactory f) {
        assert atm != null;
        return atm.getAnnotationInHierarchy(ANY);
    }

    @Override
    protected AnnotationMirror combineModifierWithModifier(AnnotationMirror recvModifier, AnnotationMirror declModifier,
            AnnotatedTypeFactory f) {
        assert recvModifier != null;
        assert declModifier != null;

        if (AnnotationUtils.areSame(recvModifier, SELF)) {
            return declModifier;
        } else if (AnnotationUtils.areSame(declModifier, ANY)) {
            return ANY;
        } else if (AnnotationUtils.areSame(declModifier, LOST)) {
            return LOST;
        } else if (AnnotationUtils.areSame(declModifier, BOTTOM)) {
            return BOTTOM;
        } else if (AnnotationUtils.areSame(declModifier, PEER)) {
            if (AnnotationUtils.areSame(recvModifier, PEER)) {
                return PEER;
            } else if (AnnotationUtils.areSame(recvModifier, REP)) {
                return REP;
            }
        }
        return LOST;
    }
}
