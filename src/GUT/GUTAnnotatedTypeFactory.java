package GUT;

import GUT.qual.*;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedArrayType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedTypeVariable;
import org.checkerframework.framework.type.AnnotatedTypeParameterBounds;
import org.checkerframework.framework.type.AnnotatedTypeReplacer;
import org.checkerframework.framework.type.DefaultTypeHierarchy;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.TypeHierarchy;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.type.typeannotator.ListTypeAnnotator;
import org.checkerframework.framework.type.typeannotator.TypeAnnotator;
import org.checkerframework.framework.util.AnnotatedTypes;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.Pair;
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.javacutil.TypesUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVariable;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.UnaryTree;
/**
 * Apply viewpoint adaptation and add implicit annotations to "this" and
 * "super".
 *
 * @author wmdietl
 */
public class GUTAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

    // Only public so that the GUTI package can access these fields.
    // Maybe merge the packages again?
    // TODO: change to getters?
    public AnnotationMirror ANY, PEER, REP, LOST, VPLOST, SELF, BOTTOM, PURE;

    public GUTAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker, true);
        ANY = AnnotationUtils.fromClass(elements, Any.class);
        PEER = AnnotationUtils.fromClass(elements, Peer.class);
        REP = AnnotationUtils.fromClass(elements, Rep.class);
        LOST = AnnotationUtils.fromClass(elements, Lost.class);
        VPLOST = AnnotationUtils.fromClass(elements, VPLost.class);
        SELF = AnnotationUtils.fromClass(elements, Self.class);
        BOTTOM = AnnotationUtils.fromClass(elements, Bottom.class);
        PURE = AnnotationUtils.fromClass(elements, Pure.class);

        addAliasedAnnotation(org.jmlspecs.annotation.Peer.class, PEER);
        addAliasedAnnotation(org.jmlspecs.annotation.Rep.class, REP);
        addAliasedAnnotation(org.jmlspecs.annotation.Readonly.class, ANY);
        // see GUTVisitor.isPure for handling of pure

        this.postInit();
    }

    /**
     * The type of "this" is always "self".
     */
    @Override
    public AnnotatedDeclaredType getSelfType(Tree tree) {
        AnnotatedDeclaredType type = super.getSelfType(tree);
        if (type != null) {
            type.replaceAnnotation(SELF);
        }
        return type;
    }

    /**
     * Adapt the type of field accesses.
     */
    @Override
    public void postAsMemberOf(AnnotatedTypeMirror type,
            AnnotatedTypeMirror owner, Element element) {
        assert type != null;
        assert owner != null;
        assert element != null;

        if (type.getKind() != TypeKind.DECLARED
                && type.getKind() != TypeKind.ARRAY) {
            // nothing to do
            return;
        }
        if (element.getKind() == ElementKind.LOCAL_VARIABLE
                || element.getKind() == ElementKind.PARAMETER) {
            // the type of local variables and parameters also do not need to
            // change
            return;
        }

        // TODO: why is this fromElement and not getAnnotatedType??
        AnnotatedTypeMirror decltype = this.fromElement(element);

        /*
          System.out.println("\npostasmemberof:");
          System.out.println("in type: " + type);
          System.out.println("decl type: " + decltype);
          System.out.println("owner: " + owner);
          System.out.println("element: " + element);
         */

        // Combine annotations
        AnnotatedTypeMirror combinedType = GUTQualifierUtils.combineTypeWithType(this, owner, decltype);
        // System.out.println("combined type: " + combinedType);

        // Replace annotations
        type.replaceAnnotation(GUTQualifierUtils.getUniverseModifier(this, combinedType));
        // System.out.println("result: " + type.toString());

        // TODO: HACK: we needed to make setTypeArguments public to work
        // around a limitation of the framework.
        // If the method had a return value, like constructor/methodFromUse, we
        // could probably get around this.

        if (type.getKind() == TypeKind.DECLARED
                && combinedType.getKind() == TypeKind.DECLARED) {
            AnnotatedDeclaredType declaredType = (AnnotatedDeclaredType) type;
            AnnotatedDeclaredType declaredCombinedType = (AnnotatedDeclaredType) combinedType;
            declaredType.setTypeArguments(declaredCombinedType.getTypeArguments());
        } else if (type.getKind() == TypeKind.ARRAY
                && combinedType.getKind() == TypeKind.ARRAY) {
            AnnotatedArrayType arrayType = (AnnotatedArrayType) type;
            AnnotatedArrayType arrayCombinedType = (AnnotatedArrayType) combinedType;
            arrayType.setComponentType(arrayCombinedType.getComponentType());
        }

        // System.out.println("out type: " + type);
    }


    @Override
    public List<AnnotatedTypeParameterBounds> typeVariablesFromUse(
            AnnotatedDeclaredType type, TypeElement element) {

        List<AnnotatedTypeParameterBounds> orig = super.typeVariablesFromUse(type, element);
        List<AnnotatedTypeParameterBounds> res = new LinkedList<>();
        AnnotationMirror firstMainModifier = GUTQualifierUtils.getUniverseModifier(this, type);

        for (AnnotatedTypeParameterBounds atpb : orig) {
            AnnotatedTypeMirror lower = GUTQualifierUtils.combineModifierWithType(this, firstMainModifier, atpb.getLowerBound());
            AnnotatedTypeMirror upper = GUTQualifierUtils.combineModifierWithType(this, firstMainModifier, atpb.getUpperBound());
            AnnotatedTypeParameterBounds newatv = new AnnotatedTypeParameterBounds(upper, lower);
            res.add(newatv);
        }
        return res;
    }

    /*
     * TODO: unify constructorFromUse and methodFromUse to share more code.
     */

    /**
     * Adapt the types of constructors.
     *
     * @param tree
     *            the new class tree.
     * @return the modified constructor.
     */
    @Override
    public Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> constructorFromUse(NewClassTree tree) {
        assert tree != null;

        // using super would substitute too much
        // AnnotatedExecutableType constructor = super.constructorFromUse(tree);

        ExecutableElement ctrElt = TreeUtils.elementFromUse(tree);
        // TODO: why is this fromElement and not getAnnotatedType??
        AnnotatedExecutableType constructor = this.fromElement(ctrElt);

        Map<AnnotatedTypeMirror, AnnotatedTypeMirror> mappings = new HashMap<AnnotatedTypeMirror, AnnotatedTypeMirror>();

        // Get the result type
        AnnotatedTypeMirror resultType = getAnnotatedType(tree.getIdentifier());

        // Modify parameters
        for (AnnotatedTypeMirror parameterType : constructor.getParameterTypes()) {
            AnnotatedTypeMirror combinedType = GUTQualifierUtils.combineTypeWithType(this, resultType, parameterType);
            mappings.put(parameterType, combinedType);
        }

        // TODO: upper bounds, throws?
        /*for (AnnotatedTypeVariable v : constructor.getTypeVariables() ) {
            System.out.println("v: " + v);
          }*/

        constructor = (AnnotatedExecutableType) AnnotatedTypeReplacer.replace(constructor, mappings);

        // determine substitution for constructor type variables
        Map<TypeVariable, AnnotatedTypeMirror> typeVarMapping =
            AnnotatedTypes.findTypeArguments(processingEnv, this, tree, ctrElt, constructor);

        List<AnnotatedTypeMirror> typeargs = new LinkedList<AnnotatedTypeMirror>();

        if (!typeVarMapping.isEmpty()) {
            for (AnnotatedTypeVariable tv : constructor.getTypeVariables()) {
                typeargs.add(typeVarMapping.get(tv.getUnderlyingType()));
            }

            constructor = (AnnotatedExecutableType) typeVarSubstitutor.substitute(typeVarMapping, constructor);
        }

        // System.out.println("adapted constructor: " + constructor);

        return Pair.of(constructor, typeargs);
    }

    /**
     * Adapt the types of methods.
     *
     * @param tree
     *            the method invocation tree.
     * @return the modified method invocation.
     */
    @Override
    public Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> methodFromUse(MethodInvocationTree tree) {
        assert tree != null;

        // Calling super would already substitute type variables and doesn't
        // work!
        // AnnotatedExecutableType method = super.methodFromUse(tree);

        // Set the receiver
        //AnnotatedTypeMirror receiverType = null;
        AnnotatedTypeMirror receiverType = getReceiverType(tree);
        ExecutableElement methodElt = TreeUtils.elementFromUse(tree);
        AnnotatedExecutableType method = this.fromElement(methodElt);
        this.annotateImplicit(methodElt, method);

        Map<AnnotatedTypeMirror, AnnotatedTypeMirror> mappings = new HashMap<AnnotatedTypeMirror, AnnotatedTypeMirror>();

        // Modify type parameters
        for (AnnotatedTypeVariable typaramType : method.getTypeVariables()) {
		//System.out.println("typearamType: "+ typaramType);
		AnnotatedTypeMirror atm = typaramType.getLowerBound();
		//System.out.println("typaram lb " + (atm != null ? "ATM: " + atm : "REAL NULL"));
		// System.out.println("Method parameter in: " + parameterType);
		AnnotatedTypeMirror combined = GUTQualifierUtils.combineTypeWithType(this, receiverType, typaramType.getUpperBound());
		mappings.put(typaramType.getUpperBound(), combined);
		combined = GUTQualifierUtils.combineTypeWithType(this, receiverType, typaramType.getLowerBound());
		mappings.put(typaramType.getLowerBound(), combined);
		//System.out.println("Method parameter out: " + combined);
        }

        // Modify parameters
        for (AnnotatedTypeMirror parameterType : method.getParameterTypes()) {
            // System.out.println("Method parameter in: " + parameterType);
            AnnotatedTypeMirror combined = GUTQualifierUtils.combineTypeWithType(this, receiverType, parameterType);
            mappings.put(parameterType, combined);
            // System.out.println("Method parameter out: " + combined);
        }

        // Modify return type
        AnnotatedTypeMirror returnType = getAnnotatedType(method.getElement()).getReturnType();
        if (returnType.getKind() != TypeKind.VOID) {
            AnnotatedTypeMirror combinedType = GUTQualifierUtils.combineTypeWithType(this, receiverType, returnType);

            // Careful: we need to use the original return type for the mapping, otherwise the substitution doesn't work
            // TODO: might this be a problem anywhere else? Why is this happening here?
            AnnotatedTypeMirror origRet = method.getReturnType();
            mappings.put(origRet, combinedType);
        }

        // TODO: upper bounds, throws?

        method = (AnnotatedExecutableType) AnnotatedTypeReplacer.replace(method, mappings);

        // determine substitution for method type variables
        Map<TypeVariable, AnnotatedTypeMirror> typeVarMapping =
                AnnotatedTypes.findTypeArguments(processingEnv, this, tree, methodElt, method);
        List<AnnotatedTypeMirror> typeargs = new LinkedList<AnnotatedTypeMirror>();

        if (!typeVarMapping.isEmpty()) {
            for (AnnotatedTypeVariable tv : method.getTypeVariables()) {
                typeargs.add(typeVarMapping.get(tv.getUnderlyingType()));
            }

            method = (AnnotatedExecutableType) typeVarSubstitutor.substitute(typeVarMapping, method);
        }

        // System.out.println("adapted method: " + method);

        return Pair.of(method, typeargs);
    }

    /**
     * Create our own TreeAnnotator.
     * @return the new TreeAnnotator.
     */
    @Override
    protected TreeAnnotator createTreeAnnotator() {
        TreeAnnotator fromAbove = super.createTreeAnnotator();
        return new ListTreeAnnotator(
                new GUTTreeAnnotator(),
                fromAbove
                );
    }

    /**
     * Create our own TypeAnnotator.
     * @return the new TreeAnnotator.
     */
    @Override
    protected TypeAnnotator createTypeAnnotator() {
        TypeAnnotator fromAbove = super.createTypeAnnotator();
        return new ListTypeAnnotator(
                fromAbove,
                new GUTTypeAnnotator()
                );
    }

    @Override
    protected TypeHierarchy createTypeHierarchy() {
        return new GUTTypeHierarchy(checker, getQualifierHierarchy());
    }

    /**
     * Currently only needed to add the "self" modifier to "super".
     */
    private class GUTTreeAnnotator extends TreeAnnotator {

        private GUTTreeAnnotator() {
            super(GUTAnnotatedTypeFactory.this);
        }

        /**
         * Add @Self to "super" identifier.
         *
         * @param node
         *            the identifier node.
         * @param p
         *            the AnnotatedTypeMirror to modify.
         */
        @Override
        public Void visitIdentifier(IdentifierTree node, AnnotatedTypeMirror p) {
            assert node != null;
            assert p != null;

            // There's no "super" kind, so make a string comparison.
            if (node.getName().contentEquals("super")) {
                p.replaceAnnotation(SELF);
            }

            return super.visitIdentifier(node, p);
        }

        @Override
        public Void visitParameterizedType(ParameterizedTypeTree node, AnnotatedTypeMirror p) {
        	//System.out.println("visitParameterizedType: " + node + " " + p);
        	Tree parent = getPath(node).getParentPath().getLeaf();
        	if (TreeUtils.isClassTree(parent)) {
        		p.replaceAnnotation(SELF);        		
        	}
        	return super.visitParameterizedType(node, p);
        }

        @Override
        public Void visitBinary(BinaryTree node, AnnotatedTypeMirror p) {
            // System.out.println("Binary: ");
            return super.visitBinary(node, p);
        }

        @Override
        public Void visitUnary(UnaryTree node, AnnotatedTypeMirror p) {
            // System.out.println("Unary: ");
            return super.visitUnary(node, p);
        }

        @Override
        public Void visitCompoundAssignment(CompoundAssignmentTree node,
                AnnotatedTypeMirror p) {
            // System.out.println("Compound: ");
            return super.visitCompoundAssignment(node, p);
        }

        @Override
        public Void visitTypeCast(TypeCastTree node, AnnotatedTypeMirror p) {
            // System.out.println("Cast: ");
            return super.visitTypeCast(node, p);
        }

        @Override
        public Void visitNewArray(NewArrayTree tree, AnnotatedTypeMirror type) {
            // System.out.println("Array type: " + type);
            super.visitNewArray(tree, type);
            // ((AnnotatedArrayType)type).getComponentType().addAnnotation(GUTChecker.BOTTOM);
            // System.out.println("Final Array type: " + type);
            return null;
        }

    }

    /**
     */
    private class GUTTypeAnnotator extends TypeAnnotator {

        public GUTTypeAnnotator() {
            super(GUTAnnotatedTypeFactory.this);
        }
/*
        @Override
        public Void visitDeclared(AnnotatedDeclaredType type, Void p) {
            if (!type.isAnnotatedInHierarchy(ANY)) {
                if (GUTChecker.isAnyDefault(type)) {
                    type.addAnnotation(ANY);
                } else {
                    type.addAnnotation(PEER);
                }
            }
            return super.visitDeclared(type, p);
        }

        @Override
        public Void visitArray(AnnotatedArrayType type, Void p) {
            if (!type.isAnnotatedInHierarchy(ANY)) {
                if (GUTChecker.isAnyDefault(type)) {
                    type.addAnnotation(ANY);
                } else {
                    type.addAnnotation(PEER);
                }
            }
            return super.visitArray(type, p);
        }
        */
    }

    /*
    private final class GUTQualifierHierarchy extends GraphQualifierHierarchy {
        public GUTQualifierHierarchy(GraphQualifierHierarchy hierarchy) {
            super(hierarchy);
        }

        @Override
        public boolean isSubtype(AnnotationMirror rhs, AnnotationMirror lhs) {
            return rhs == null || lhs == null
                   || super.isSubtype(rhs, lhs);
        }
    }
    */

    private final class GUTTypeHierarchy extends DefaultTypeHierarchy {
        public GUTTypeHierarchy(BaseTypeChecker checker, QualifierHierarchy qualifierHierarchy) {
            super(checker, qualifierHierarchy,
                    checker.hasOption("ignoreRawTypeArguments"),
                    checker.hasOption("invariantArrays"));
        }

        @Override
        public boolean isSubtype(AnnotatedTypeMirror sub, AnnotatedTypeMirror sup) {
            if (sub.getUnderlyingType().getKind().isPrimitive() ||
                    sup.getUnderlyingType().getKind().isPrimitive() ) {
                // TODO: Ignore boxing/unboxing
                return true;
            }
            if (TypesUtils.isString(sub.getUnderlyingType()) ||
                    TypesUtils.isString(sup.getUnderlyingType())) {
                return true;
            }

            return super.isSubtype(sub, sup);
        }

        @Override
        protected boolean isAnnoSubtype(AnnotationMirror subtypeAnno, AnnotationMirror supertypeAnno,
                boolean annosCanBeEmtpy) {
            if (subtypeAnno == null || supertypeAnno == null) {
                // TODO: lower bounds of wildcards in method parameters do not get defaulted correctly!
                return true;
            }

            return super.isAnnoSubtype(subtypeAnno, supertypeAnno, annosCanBeEmtpy);
        }

        /*
        @Override
        protected boolean isSubtypeAsTypeArgument(AnnotatedTypeMirror rhs, AnnotatedTypeMirror lhs) {
            return super.isSubtypeAsTypeArgument(rhs, lhs);
        }

        @Override
        protected boolean isSubtypeTypeArguments(AnnotatedDeclaredType rhs, AnnotatedDeclaredType lhs) {
            return super.isSubtypeTypeArguments(rhs, lhs);
        }
        */
    }
}
