package net.cupmouse.minecraft.game.spleef;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.Collections;
import java.util.List;

public class SpleefStageTemplateInfo {

    private String name;
    private String description;
    private String version;
    private List<String> builders;

    SpleefStageTemplateInfo() {
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getVersion() {
        return version;
    }

    /**
     * 変更できる
     * @return
     */
    public List<String> getBuilders() {
        return builders;
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

    static class Serializer implements TypeSerializer<SpleefStageTemplateInfo> {
        @Override
        public SpleefStageTemplateInfo deserialize(TypeToken<?> type, ConfigurationNode value)
                throws ObjectMappingException {
            SpleefStageTemplateInfo info = new SpleefStageTemplateInfo();

            info.name = value.getNode("name").getString();
            info.description = value.getNode("description").getString();
            info.version = value.getNode("version").getString();
            info.builders = value.getNode("builders").getList(TypeToken.of(String.class));

            return info;
        }

        @Override
        public void serialize(TypeToken<?> type, SpleefStageTemplateInfo obj, ConfigurationNode value)
                throws ObjectMappingException {
            value.getNode("name").setValue(obj.name);
            value.getNode("description").setValue(obj.description);
            value.getNode("version").setValue(obj.version);
            value.getNode("builders").setValue(obj.builders);
        }
    }

}
