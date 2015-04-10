package GUT.quals;

import org.checkerframework.framework.qual.ImplicitFor;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeQualifier;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedPrimitiveType;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sun.source.tree.Tree;

/**
 * The bottom of the type hierarchy is only used internally.
 *
 * @author wmdietl
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
@TypeQualifier
@SubtypeOf({ Self.class, Rep.class })
@ImplicitFor(trees = { Tree.Kind.NULL_LITERAL, Tree.Kind.STRING_LITERAL },
        typeClasses = { AnnotatedPrimitiveType.class })
public @interface Bottom {}
