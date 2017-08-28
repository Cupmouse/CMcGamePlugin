package net.cupmouse.minecraft.game.spleef;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import net.cupmouse.minecraft.worlds.WorldTagArea;
import net.cupmouse.minecraft.worlds.WorldTagLocation;
import net.cupmouse.minecraft.worlds.WorldTagRocation;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class SpleefStageTemplate {

    private SpleefStageTemplateInfo info;
    private SpleefStageOptions defaultOptions;

    // これらはすべてステージテンプレート作成時の絶対位置（ワールドも含め）なので、相対位置ではない
    // そのために相対位置の基準点を決め、それを使って新しいステージを作成する
    private WorldTagLocation relativeBaseLocation;
    private WorldTagArea groundArea;
    private WorldTagArea fightingArea;
    private List<WorldTagRocation> spawnRocations;

    private SpleefStageTemplate() {
    }

    public SpleefStageOptions getDefaultOptions() {
        return defaultOptions;
    }

    public WorldTagArea getGroundArea() {
        return groundArea;
    }

    public WorldTagArea getFightingArea() {
        return fightingArea;
    }

    public WorldTagLocation getRelativeBaseLocation() {
        return relativeBaseLocation;
    }

    /**
     * なんと外部から変更可能
     * @return
     */
    public List<WorldTagRocation> getSpawnRocations() {
        return spawnRocations;
    }

    /*
    ここからSetter
     */

    public void setGroundArea(WorldTagArea groundArea) {
        this.groundArea = groundArea;
    }

    public void setFightingArea(WorldTagArea fightingArea) {
        this.fightingArea = fightingArea;
    }

    public void setRelativeBaseLocation(WorldTagLocation relativeBaseLocation) {
        this.relativeBaseLocation = relativeBaseLocation;
    }

    /*
        ロードとセーブ
     */
    public static SpleefStageTemplate createNew() {
        SpleefStageTemplate template = new SpleefStageTemplate();
        template.defaultOptions = new SpleefStageOptions();
        template.info = new SpleefStageTemplateInfo();
        template.spawnRocations = new ArrayList<>();

        return template;
    }

    static class Serializer implements TypeSerializer<SpleefStageTemplate> {

        @Override
        public SpleefStageTemplate deserialize(TypeToken<?> type, ConfigurationNode value)
                throws ObjectMappingException {
            SpleefStageTemplate template = new SpleefStageTemplate();

            template.defaultOptions =
                    value.getNode("default_options").getValue(TypeToken.of(SpleefStageOptions.class));
            template.info = value.getNode("info").getValue(TypeToken.of(SpleefStageTemplateInfo.class));
            template.relativeBaseLocation =
                    value.getNode("relative_base_location").getValue(TypeToken.of(WorldTagLocation.class));
            template.fightingArea =
                    value.getNode("fighting_area").getValue(TypeToken.of(WorldTagArea.class));
            template.groundArea =
                    value.getNode("ground_area").getValue(TypeToken.of(WorldTagArea.class));

            List<WorldTagRocation> rocationsSaved =
                    value.getNode("spawn_rocations").getList(TypeToken.of(WorldTagRocation.class));

            template.spawnRocations = new ArrayList<>();
            template.spawnRocations.addAll(rocationsSaved);

            return template;
        }

        @Override
        public void serialize(TypeToken<?> type, SpleefStageTemplate obj, ConfigurationNode value)
                throws ObjectMappingException {
            value.getNode("default_options").setValue(obj.defaultOptions);
            value.getNode("info").setValue(obj.info);
            value.getNode("relative_base_location").setValue(obj.relativeBaseLocation);
            value.getNode("fighting_area").setValue(obj.fightingArea);
            value.getNode("ground_area").setValue(obj.groundArea);
            value.getNode("spawn_rocations").setValue(obj.spawnRocations);
        }
    }
}
