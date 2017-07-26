package GUT;


import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.util.FrameworkViewpointAdaptor;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.ErrorReporter;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeKind;

public class GUTViewpointAdaptor extends FrameworkViewpointAdaptor{

    @Override
    protected AnnotationMirror getModifier(AnnotatedTypeMirror atm, AnnotatedTypeFactory f) {
        assert atm != null;
        GUTAnnotatedTypeFactory gutATF = (GUTAnnotatedTypeFactory)f;

        if (atm.hasEffectiveAnnotation(gutATF.PEER)) {
            return gutATF.PEER;
        }
        if (atm.hasEffectiveAnnotation(gutATF.REP)) {
            return gutATF.REP;
        }
        if (atm.hasEffectiveAnnotation(gutATF.ANY)) {
            return gutATF.ANY;
        }
        if (atm.hasEffectiveAnnotation(gutATF.SELF)) {
            return gutATF.SELF;
        }
        if(atm.hasEffectiveAnnotation(gutATF.BOTTOM)){
            return gutATF.BOTTOM;
        }
        if (atm.hasEffectiveAnnotation(gutATF.VPLOST)) {
            return gutATF.VPLOST;
        }
        if (atm.hasEffectiveAnnotation(gutATF.LOST)) {
            return gutATF.LOST;
        }
        return null;
    }

    @Override
    protected AnnotationMirror combineModifierWithModifier(AnnotationMirror recvModifier, AnnotationMirror declModifier,
            AnnotatedTypeFactory f) {
        assert recvModifier != null;
        assert declModifier != null;

        GUTAnnotatedTypeFactory gutATF = (GUTAnnotatedTypeFactory)f;
        if (AnnotationUtils.areSame(recvModifier, gutATF.SELF)) {
            return declModifier;
        } else if (AnnotationUtils.areSame(declModifier, gutATF.SELF)) {
            return recvModifier;
        } else if (AnnotationUtils.areSame(declModifier, gutATF.PEER)) {
            if (AnnotationUtils.areSame(recvModifier, gutATF.PEER)) {
                return gutATF.PEER;
            } else if (AnnotationUtils.areSame(recvModifier, gutATF.SELF)) {
                return gutATF.PEER;
            } else if (AnnotationUtils.areSame(recvModifier, gutATF.BOTTOM)) {
                return gutATF.PEER;
            } else if (AnnotationUtils.areSame(recvModifier, gutATF.REP)) {
                return gutATF.REP;
            } else {
                return gutATF.VPLOST;
            }
        } else if (AnnotationUtils.areSame(declModifier, gutATF.REP)) {
            if (AnnotationUtils.areSame(recvModifier, gutATF.SELF)) {
                return gutATF.REP;
            } else {
                return gutATF.VPLOST;
            }
        } else if (AnnotationUtils.areSame(declModifier, gutATF.ANY)) {
            return gutATF.ANY;
        } else if (AnnotationUtils.areSame(declModifier, gutATF.LOST)) {
            return gutATF.LOST;
        } else if (AnnotationUtils.areSame(declModifier, gutATF.VPLOST)) {
            return gutATF.VPLOST;
        } else if (AnnotationUtils.areSame(declModifier, gutATF.BOTTOM)) {
            return gutATF.BOTTOM;
        } else {
            ErrorReporter.errorAbort("Unexpected qualifier combination: " + recvModifier + " |> " + declModifier);
            return null;
        }
    }

    @Override
    public boolean shouldBeAdapted(AnnotatedTypeMirror type, Element element) {
        if (type.getKind() != TypeKind.DECLARED && type.getKind() != TypeKind.ARRAY) {
            return false;
        }
        if (element.getKind() == ElementKind.LOCAL_VARIABLE
                || element.getKind() == ElementKind.PARAMETER) {
            return false;
        }
        return true;
    }
}
