package net.clgd.ccemux.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigProperty {
	/**
	 * A human-readable description for the given config property. Defaults to an empty string.
	 */
	public String value() default "";

	/**
	 * The {@link net.clgd.ccemux.config.ConfigPropertyHandler
	 * ConfigPropertyHandler} implementation to use to de/serialize this config
	 * property.
	 */
	public Class<? extends ConfigPropertyHandler<?>> handler() default AutoPropertyHandler.class;
}
