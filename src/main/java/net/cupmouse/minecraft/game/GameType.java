package net.cupmouse.minecraft.game;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum GameType {

    SPLEEF("spleef", "sp");

    public final List<String> aliases;

    GameType(String... aliases) {
        this.aliases = Collections.unmodifiableList(Arrays.asList(aliases));
    }
}
