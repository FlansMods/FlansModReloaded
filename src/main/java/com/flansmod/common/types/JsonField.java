package com.flansmod.common.types;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface JsonField
{
	String Docs() default "";
	String AssetPathHint() default "";
	String ModifiedBy() default "";
	double Min() default -Double.MAX_VALUE;
	double Max() default Double.MAX_VALUE;
}
