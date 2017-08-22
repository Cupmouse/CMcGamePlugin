package net.cupmouse.minecraft.game;

import java.util.*;

public enum GameType {

    SPLEEF("spleef", "sp");

    private static Map<String, GameType> aliasesVsGametypes = new HashMap<>();
    public final List<String> aliases;

    GameType(String... aliases) {
        this.aliases = Collections.unmodifiableList(Arrays.asList(aliases));
    }

    public static GameType fromString(String string) {
        if (aliasesVsGametypes.size() == 0) {
            // キャッシュを初期化する
            for (GameType gameType : GameType.values()) {

                for (String alias : gameType.aliases) {
                    aliasesVsGametypes.put(alias, gameType);
                }
            }
        }

        return aliasesVsGametypes.get(string);
    }
}
