package GUT.qual;

import org.checkerframework.framework.qual.SubtypeOf;
//import org.checkerframework.framework.qual.TypeQualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ownership information is not expressible from the current viewpoint. Only
 * used internally.
 *
 * @author wmdietl
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_PARAMETER, ElementType.TYPE_USE })
//@TypeQualifier
@SubtypeOf({ Any.class })
public @interface Lost {}
