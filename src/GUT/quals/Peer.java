package GUT.quals;

import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.ImplicitFor;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeQualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sun.source.tree.Tree;

/**
 * The referenced object has the same owner as the current object.
 *
 * @author wmdietl
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_PARAMETER, ElementType.TYPE_USE })
@TypeQualifier
@DefaultQualifierInHierarchy
// TODO: why is this not happening?
@ImplicitFor(trees = { Tree.Kind.NEW_ARRAY })
@SubtypeOf({ Lost.class })
public @interface Peer {}
