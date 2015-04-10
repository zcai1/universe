package GUT;

import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedArrayType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedTypeVariable;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedWildcardType;
import org.checkerframework.framework.type.AnnotatedTypeReplacer;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.Pair;
import org.checkerframework.javacutil.TypesUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;


/**
 * Utility class that contains Universe type helper methods.
 *
 * @author wmdietl
 */
public class GUTQualifierUtils {

    /**
     * Combine two Universe modifiers.
     *
     * @param recv the receiver Universe modifier.
     * @param decl the declared Universe modifier.
     * @return the combined Universe modifier.
     */
    private static AnnotationMirror combineModifierWithModifier(GUTAnnotatedTypeFactory atypeFactory,
            AnnotationMirror recv, AnnotationMirror decl) {
        assert recv != null;
        assert decl != null;

        if (AnnotationUtils.areSame(recv, atypeFactory.SELF)) {
            return decl;
        } else if (AnnotationUtils.areSame(recv, atypeFactory.PEER) &&
                AnnotationUtils.areSame(decl, atypeFactory.PEER)) {
            return atypeFactory.PEER;
        } else if (AnnotationUtils.areSame(recv, atypeFactory.REP) &&
                AnnotationUtils.areSame(decl, atypeFactory.PEER)) {
            return atypeFactory.REP;
        } else if (AnnotationUtils.areSame(decl, atypeFactory.ANY)) {
            return atypeFactory.ANY;
        } else {
            return atypeFactory.LOST;
        }
    }

    // Used to limit the recursion in upper bounds
    private static boolean isTypeVarExtends = false;

    /**
     * Combine a Universe modifier with a type.
     *
     * @param recv the receiver Universe modifier.
     * @param decl the declared type.
     * @return the combined type.
     *
     * TODO: unsupported TypeKinds create assertion violation.
     */
    public static AnnotatedTypeMirror combineModifierWithType(GUTAnnotatedTypeFactory atypeFactory,
            AnnotationMirror recv, AnnotatedTypeMirror decl) {
        assert recv != null;
        assert decl != null;

        if (decl.getKind().isPrimitive()) {
            // Make sure that primitive types have type bottom
            decl.replaceAnnotation(atypeFactory.BOTTOM);
            return decl;
        } else if (decl.getKind() == TypeKind.TYPEVAR ) {
            if (!isTypeVarExtends) {
                isTypeVarExtends = true;

                AnnotatedTypeVariable atv = (AnnotatedTypeVariable) decl.shallowCopy(true);
                Map<AnnotatedTypeMirror, AnnotatedTypeMirror> mapping = new HashMap<AnnotatedTypeMirror, AnnotatedTypeMirror>();

                AnnotatedTypeMirror resUpper = combineModifierWithType(atypeFactory, recv, atv.getUpperBound());
                mapping.put(atv.getUpperBound(), resUpper);

                AnnotatedTypeMirror result = AnnotatedTypeReplacer.replace(atv, mapping);
                isTypeVarExtends = false;
                return result;
            }
            return decl;
        } else if (decl.getKind() == TypeKind.DECLARED) {
            // Create a copy
            AnnotatedDeclaredType declaredType = (AnnotatedDeclaredType) decl.shallowCopy(true);

            if (TypesUtils.isString(declaredType.getUnderlyingType())) {
                declaredType.replaceAnnotation(atypeFactory.ANY);
                return declaredType;
            }

            Map<AnnotatedTypeMirror, AnnotatedTypeMirror> mapping = new HashMap<AnnotatedTypeMirror, AnnotatedTypeMirror>();

            // Get the combined main modifier
            AnnotationMirror mainModifier = getUniverseModifier(atypeFactory, declaredType);
            AnnotationMirror combinedMainModifier = combineModifierWithModifier(atypeFactory, recv, mainModifier);

            // Get the combined type arguments
            for (AnnotatedTypeMirror typeArgument : declaredType.getTypeArguments()) {
                AnnotatedTypeMirror combinedTypeArgument = combineModifierWithType(atypeFactory, recv, typeArgument);
                mapping.put(typeArgument, combinedTypeArgument);
            }

            // Construct result type
            AnnotatedTypeMirror result = AnnotatedTypeReplacer.replace(declaredType, mapping);
            result.replaceAnnotation(combinedMainModifier);

            return result;
        } else if (decl.getKind() == TypeKind.ARRAY) {
            // Create a copy
            AnnotatedArrayType  arrayType = (AnnotatedArrayType) decl.shallowCopy(true);
            // Get the combined main modifier
            AnnotationMirror mainModifier = getUniverseModifier(atypeFactory, arrayType);
            AnnotationMirror combinedMainModifier = combineModifierWithModifier(atypeFactory, recv, mainModifier);

            // Construct result type
            arrayType.replaceAnnotation(combinedMainModifier);

            // TODO: combine element type
            AnnotatedTypeMirror elemType = arrayType.getComponentType();
            AnnotatedTypeMirror combinedElemType = combineModifierWithType(atypeFactory, recv, elemType);

            arrayType.setComponentType(combinedElemType);
            return arrayType;
        } else if (decl.getKind() == TypeKind.WILDCARD) {
            // Create a copy
            AnnotatedWildcardType wildType = (AnnotatedWildcardType) decl.shallowCopy(true);
            Map<AnnotatedTypeMirror, AnnotatedTypeMirror> mapping = new HashMap<AnnotatedTypeMirror, AnnotatedTypeMirror>();

            // There is no main modifier for a wildcard

            AnnotatedTypeMirror combined;
            if (wildType.getExtendsBound() != null) {
                combined = combineModifierWithType(atypeFactory, recv, wildType.getExtendsBound());
                mapping.put(wildType.getExtendsBound(), combined);
            }
            if (wildType.getSuperBound() != null) {
                combined = combineModifierWithType(atypeFactory, recv, wildType.getSuperBound());
                mapping.put(wildType.getSuperBound(), combined);
            }

            // Construct result type
            AnnotatedTypeMirror result = AnnotatedTypeReplacer.replace(wildType, mapping);
            return result;
        } else if (decl.getKind() == TypeKind.NULL) {
            // Happens for lower bounds.
            return decl;
        } else {
            System.err.println("Unknown result.getKind(): " + decl.getKind());
            assert false;
            return null;
            // TODO throw new UnsupportedTypeException(type.getKind().toString());
        }
    }

    /**
     * Combine two types.
     *
     * @param recv the receiver type.
     * @param decl the declared type.
     * @return the combined type.
     */
    public static AnnotatedTypeMirror combineTypeWithType(GUTAnnotatedTypeFactory atypeFactory,
            AnnotatedTypeMirror recv, AnnotatedTypeMirror decl) {
        assert recv != null;
        assert decl != null;

        // System.out.println("combTT recv: " + recv);
        // System.out.println("combTT decl: " + decl);

        if (recv.getKind() == TypeKind.TYPEVAR) {
            recv = ((AnnotatedTypeVariable)recv).getUpperBound();
            // System.out.println("combTT using upper bound: " + recv);
        }

        AnnotationMirror firstMainModifier = getUniverseModifier(atypeFactory, recv);
        AnnotatedTypeMirror combinedType = combineModifierWithType(atypeFactory, firstMainModifier, decl);

        combinedType = substituteTVars(atypeFactory, recv, combinedType);

        // System.out.println("combTT comb: " + combinedType);
        return combinedType;
    }

    private static AnnotatedTypeMirror substituteTVars(GUTAnnotatedTypeFactory atypeFactory,
            AnnotatedTypeMirror lhs, AnnotatedTypeMirror rhs) {
        if (rhs.getKind() == TypeKind.TYPEVAR) {
            // method type variables will never be found and no special case needed; maybe as optimization.
            AnnotatedTypeVariable rhsTypeVar = (AnnotatedTypeVariable) rhs.shallowCopy(true);

            if (lhs.getKind() == TypeKind.DECLARED) {
                rhs = getTypeVariableSubstitution(atypeFactory, (AnnotatedDeclaredType) lhs, rhsTypeVar);
            }
            // else TODO: the receiver might be another type variable... should we do something?
        } else if (rhs.getKind() == TypeKind.DECLARED) {
            // Create a copy
            AnnotatedDeclaredType declaredType = (AnnotatedDeclaredType) rhs.shallowCopy(true);
            Map<AnnotatedTypeMirror, AnnotatedTypeMirror> mapping = new HashMap<AnnotatedTypeMirror, AnnotatedTypeMirror>();

            for (AnnotatedTypeMirror typeArgument : declaredType.getTypeArguments()) {
                AnnotatedTypeMirror substTypeArgument = substituteTVars(atypeFactory, lhs, typeArgument);
                mapping.put(typeArgument, substTypeArgument);
            }

            // Construct result type
            rhs = AnnotatedTypeReplacer.replace(declaredType, mapping);
        } else if (rhs.getKind() == TypeKind.WILDCARD) {
            AnnotatedWildcardType wildType = (AnnotatedWildcardType) rhs.shallowCopy(true);
            Map<AnnotatedTypeMirror, AnnotatedTypeMirror> mapping = new HashMap<AnnotatedTypeMirror, AnnotatedTypeMirror>();

            AnnotatedTypeMirror substBound;

            if (wildType.getExtendsBound()!=null) {
                substBound = substituteTVars(atypeFactory, lhs, wildType.getExtendsBound());
                mapping.put(wildType.getExtendsBound(), substBound);
            }
            if (wildType.getSuperBound()!=null) {
                substBound = substituteTVars(atypeFactory, lhs, wildType.getSuperBound());
                mapping.put(wildType.getSuperBound(), substBound);
            }

            rhs = AnnotatedTypeReplacer.replace(wildType, mapping);
        } else if (rhs.getKind() == TypeKind.ARRAY) {
            // Create a copy
            AnnotatedArrayType arrayType = (AnnotatedArrayType) rhs.shallowCopy(true);

            Map<AnnotatedTypeMirror, AnnotatedTypeMirror> mapping = new HashMap<AnnotatedTypeMirror, AnnotatedTypeMirror>();

            AnnotatedTypeMirror compType = arrayType.getComponentType();
            AnnotatedTypeMirror substTypeArgument = substituteTVars(atypeFactory, lhs, compType);
            mapping.put(compType, substTypeArgument);

            // Construct result type
            rhs = AnnotatedTypeReplacer.replace(arrayType, mapping);
        } else if (rhs.getKind().isPrimitive() ||
                rhs.getKind() == TypeKind.NULL) {
            // nothing to do for primitive types and the null type
        } else {
            System.out.println("GUTQualifierUtils::substituteTVars: What should be done with: " + rhs + " of kind: " + rhs.getKind());
        }

        return rhs;
    }

    private static AnnotatedTypeMirror getTypeVariableSubstitution(GUTAnnotatedTypeFactory atypeFactory,
            AnnotatedDeclaredType type, AnnotatedTypeVariable var) {
        // System.out.println("Looking for " + var + " in type " + type);

        Pair<AnnotatedDeclaredType, Integer> res = findDeclType(type, var);

        if (res == null) {
            return var;
        }

        AnnotatedDeclaredType decltype = res.first;
        int foundindex = res.second;

        if (!decltype.wasRaw()) {
            List<AnnotatedTypeMirror> tas = decltype.getTypeArguments();
            // assert foundindex < tas.size()) {
            // CAREFUL: return a copy, as we want to modify the type later.
            return tas.get(foundindex).shallowCopy(true);
        } else {
            // TODO: use upper bound instead of var
            // type.getTypeArguments()
            // System.out.println("Raw Type: " + decltype);
            if (!var.getUpperBound().isAnnotatedInHierarchy(atypeFactory.ANY)) {
                // TODO: hmm, seems to be needed :-(
                var.getUpperBound().addAnnotation(atypeFactory.PEER);
            }
            return var.getUpperBound();
        }
    }

    private static Pair<AnnotatedDeclaredType, Integer> findDeclType(AnnotatedDeclaredType type, AnnotatedTypeVariable var) {
        // System.out.println("Finding " + var + " in type " + type);

        Element varelem = var.getUnderlyingType().asElement();

        DeclaredType dtype = type.getUnderlyingType();
        TypeElement el = (TypeElement) dtype.asElement();
        List<? extends TypeParameterElement> tparams = el.getTypeParameters();
        int foundindex = 0;

        for (TypeParameterElement tparam: tparams) {
            if (tparam.equals(varelem) ||
                    //TODO: comparing by name!!!???
                    // Sometimes "E" and "E extends Object" are compared, which do not match by "equals".
                    tparam.getSimpleName().equals(varelem.getSimpleName())) {
                // we found the right index!
                break;
            }
            ++foundindex;
        }

        if (foundindex>=tparams.size()) {
            // didn't find the desired type :-(
            for(AnnotatedDeclaredType sup : type.directSuperTypes()) {
                Pair<AnnotatedDeclaredType, Integer> res = findDeclType(sup, var);

                if(res!=null) {
                    return res;
                }
            }
            // we reach this point if the variable wasn't found in any recursive call.
            return null;
        }

        return Pair.of(type, foundindex);
    }


    /**
     * Returns the first Universe modifier on the annotated type.
     *
     * @param type an annotated type.
     * @return the first Universe modifier or null if there's no Universe modifier.
     */
    public static AnnotationMirror getUniverseModifier(GUTAnnotatedTypeFactory atypeFactory, AnnotatedTypeMirror type) {
        assert type != null;

        if (!type.isAnnotatedInHierarchy(atypeFactory.ANY)) {
            // Hmm, why do we get here with an unannotated type?
            // Seems to happen for arrays?
            if (GUTChecker.isAnyDefault(type) ) {
                return atypeFactory.ANY;
            } else {
                return atypeFactory.PEER;
            }
        }

        if (type.hasEffectiveAnnotation(atypeFactory.PEER)) {
            return atypeFactory.PEER;
        }
        if (type.hasEffectiveAnnotation(atypeFactory.REP)) {
            return atypeFactory.REP;
        }
        if (type.hasEffectiveAnnotation(atypeFactory.ANY)) {
            return atypeFactory.ANY;
        }
        if (type.hasEffectiveAnnotation(atypeFactory.SELF)) {
            return atypeFactory.SELF;
        }

        return atypeFactory.LOST;

        // TODO: for some reason the simpler statement below does not work!
        // It gives a different AnnotationMirror for different invocations.
        // Defaulting ensures that there is always one Universe annotation.
        // return type.getAnnotations().iterator().next();
    }


    /**
     * Checks whether type has multiple Universe modifiers on the top level.
     *
     * @param type a type.
     * @return true if type has multiple Universe modifiers, false otherwise.
     */
    public static boolean hasMultipleModifiers(AnnotatedTypeMirror type) {
        assert type != null;

        return type.getAnnotations().size() > 1;
    }
}
