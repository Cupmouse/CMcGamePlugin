package net.cupmouse.minecraft.game.manager;

public @interface IntOptionId {

    String[] value();

    String getter() default "";

    String setter() default "";
}
