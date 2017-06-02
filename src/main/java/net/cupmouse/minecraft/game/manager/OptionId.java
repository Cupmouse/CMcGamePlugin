package net.cupmouse.minecraft.game.manager;

public @interface OptionId {

    String[] value();

    String getter() default "";

    String setter() default "";
}
