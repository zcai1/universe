package GUT.qual;

import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.DefaultLocation;
import org.checkerframework.framework.qual.ImplicitFor;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TargetLocations;
//import org.checkerframework.framework.qual.TypeQualifier;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedPrimitiveType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
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
@Target({ ElementType.TYPE_PARAMETER, ElementType.TYPE_USE })
@TargetLocations({DefaultLocation.EXPLICIT_LOWER_BOUNDS,
    DefaultLocation.EXPLICIT_UPPER_BOUNDS})
//@TypeQualifier
@SubtypeOf({ Self.class, Rep.class })
@DefaultFor({ DefaultLocation.IMPLICIT_LOWER_BOUNDS })
@ImplicitFor(trees = { Tree.Kind.NULL_LITERAL, Tree.Kind.STRING_LITERAL },
        typeClasses = { AnnotatedPrimitiveType.class })
public @interface Bottom {}
