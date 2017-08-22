package net.cupmouse.minecraft.game.spleef;

import net.cupmouse.minecraft.game.manager.OptionId;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.time.LocalDateTime;
import java.util.List;

@ConfigSerializable
public class SpleefStageTemplateInfo {

    @OptionId({"name", "n"})
    @Setting("name")
    private String name;
    @OptionId({"description", "d"})
    @Setting("description")
    private String description;
    @OptionId({"version", "v"})
    @Setting("version")
    private String version;
    @OptionId({"builders", "b"})
    @Setting("builders")
    private List<String> builders;
    @OptionId({"unveiledTime", "ut"})
    @Setting("unveiled_time")
    private LocalDateTime unveiledTime;

}
