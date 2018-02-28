package universe;

import universe.qual.Any;
import universe.qual.Bottom;
import universe.qual.Lost;
import universe.qual.Peer;
import universe.qual.Rep;
import universe.qual.Self;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.util.TreePath;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.DefaultTypeHierarchy;
import org.checkerframework.framework.type.QualifierHierarchy;
import org.checkerframework.framework.type.TypeHierarchy;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.type.typeannotator.ListTypeAnnotator;
import org.checkerframework.framework.type.typeannotator.TypeAnnotator;
import org.checkerframework.framework.util.ViewpointAdapter;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.javacutil.TypesUtils;

import javax.lang.model.element.AnnotationMirror;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static universe.GUTChecker.ANY;
import static universe.GUTChecker.PEER;
import static universe.GUTChecker.REP;
import static universe.GUTChecker.SELF;

/**
 * Apply viewpoint adaptation and add implicit annotations to "this" and
 * "super".
 *
 * @author wmdietl
 */
public class GUTAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

    public GUTAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker, true);

        addAliasedAnnotation(org.jmlspecs.annotation.Peer.class, PEER);
        addAliasedAnnotation(org.jmlspecs.annotation.Rep.class, REP);
        addAliasedAnnotation(org.jmlspecs.annotation.Readonly.class, ANY);

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

    @Override
    protected ViewpointAdapter<?> createViewpointAdapter() {
        return new GUTViewpointAdapter();
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

    protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
        Set<Class<? extends Annotation>> annotations = new HashSet<>(
                Arrays.asList(Any.class, Lost.class,
                        Peer.class, Rep.class, Self.class, Bottom.class));
        return Collections.unmodifiableSet(annotations);
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
            TreePath path = atypeFactory.getPath(node);
            if (path != null) {
                final TreePath parentPath = path.getParentPath();
                if (parentPath != null) {
                    final Tree parentNode = parentPath.getLeaf();
                    if (TreeUtils.isClassTree(parentNode)) {
                        p.replaceAnnotation(SELF);
                    }
                }
            }
            return super.visitParameterizedType(node, p);
        }
    }
}
