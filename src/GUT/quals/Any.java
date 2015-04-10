package GUT.quals;

import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.DefaultLocation;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeQualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Any modifier expresses no static ownership information, the referenced
 * object can have any owner.
 *
 * @author wmdietl
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_PARAMETER, ElementType.TYPE_USE })
@TypeQualifier
@SubtypeOf({})
@DefaultFor({ DefaultLocation.LOCAL_VARIABLE, DefaultLocation.RESOURCE_VARIABLE,
    DefaultLocation.IMPLICIT_UPPER_BOUNDS })
// @DefaultQualifierInHierarchy
/*
 * @ImplicitFor( trees={ Tree.Kind.CHAR_LITERAL, Tree.Kind.STRING_LITERAL })
 */
public @interface Any {}