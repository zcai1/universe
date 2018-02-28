package universe.qual;

import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.ImplicitFor;
import org.checkerframework.framework.qual.LiteralKind;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TargetLocations;
import org.checkerframework.framework.qual.TypeUseLocation;

import javax.lang.model.type.TypeKind;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * The bottom of the type hierarchy is only used internally.
 *
 * @author wmdietl
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
@TargetLocations({TypeUseLocation.EXPLICIT_LOWER_BOUND})
@SubtypeOf({ Self.class, Rep.class })
@DefaultFor({ TypeUseLocation.IMPLICIT_LOWER_BOUND })
@ImplicitFor(literals = { LiteralKind.ALL},
        types = { TypeKind.INT, TypeKind.BYTE, TypeKind.SHORT, TypeKind.BOOLEAN,
                TypeKind.LONG, TypeKind.CHAR, TypeKind.FLOAT, TypeKind.DOUBLE },
        typeNames={String.class, Double.class, Boolean.class, Byte.class,
                Character.class, Float.class, Integer.class, Long.class, Short.class, Number.class,
                BigDecimal.class, BigInteger.class}
        )
public @interface Bottom {}
