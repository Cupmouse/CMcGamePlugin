package net.cupmouse.minecraft.game.spleef.stage;

import net.cupmouse.minecraft.game.manager.OptionId;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.time.LocalDateTime;
import java.util.List;

@ConfigSerializable
public class SpleefStageTemplateInfo {

    private String templateId;
    private String name;
    private String description;
    private String version;
    @OptionId({"builders"})
    @Setting("builders")
    private List<String> builders;
    private LocalDateTime unveiledTime;

}
