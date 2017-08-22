package net.cupmouse.minecraft.game.spleef;

import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Collections;
import java.util.List;

@ConfigSerializable
public class SpleefStageTemplateInfo {

    private String name;
    private String description;
    private String version;
    private List<String> builders;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getVersion() {
        return version;
    }

    public List<String> getBuilders() {
        return Collections.unmodifiableList(builders);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setBuilders(List<String> builders) {
        this.builders = builders;
    }
}
