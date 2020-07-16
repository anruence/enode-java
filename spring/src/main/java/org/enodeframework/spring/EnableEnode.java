package org.enodeframework.spring;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author anruence@gmail.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({EnodeConfigurationSelector.class, EnodeBeanInitialize.class})
public @interface EnableEnode {
    /**
     * Alias for {@link #basePackages}.
     * <p>Allows for more concise annotation declarations if no other attributes
     * are needed &mdash; for example, {@code @EnableEnode("org.my.pkg")}
     * instead of {@code @EnableEnode(basePackages = "org.my.pkg")}.
     */
    @AliasFor("basePackages")
    String[] value() default {};

    /**
     * Base packages to scan for annotated components.
     * <p>{@link #value} is an alias for (and mutually exclusive with) this
     * attribute.
     * <p>Use {@link #basePackageClasses} for a type-safe alternative to
     * String-based package names.
     */
    @AliasFor("value")
    String[] basePackages() default {};

    String[] scanBasePackages() default {};

    @AliasFor("basePackageClasses")
    Class<?>[] scanBasePackageClasses() default {};

    /**
     * Type-safe alternative to {@link #basePackages} for specifying the packages
     * to scan for annotated components. The package of each class specified will be scanned.
     * <p>Consider creating a special no-op marker class or interface in each package
     * that serves no purpose other than being referenced by this attribute.
     */
    @AliasFor("scanBasePackageClasses")
    Class<?>[] basePackageClasses() default {};
}