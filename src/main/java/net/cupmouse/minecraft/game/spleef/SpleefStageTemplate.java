package net.cupmouse.minecraft.game.spleef;

import com.google.common.reflect.TypeToken;
import net.cupmouse.minecraft.worlds.WorldTagArea;
import net.cupmouse.minecraft.worlds.WorldTagLocation;
import net.cupmouse.minecraft.worlds.WorldTagRocation;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.block.BlockState;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class SpleefStageTemplate {

    private SpleefStageTemplateInfo info;
    private SpleefStageOptions defaultOptions;

    // これらはすべてステージテンプレート作成時の絶対位置（ワールドも含め）なので、相対位置ではない
    // そのために相対位置の基準点を決め、それを使って新しいステージを作成する
    private WorldTagLocation relativeBaseLocation;
    private List<WorldTagRocation> spawnRocations;
    private WorldTagRocation waitingSpawnRocation;

    private WorldTagArea groundArea;
    private WorldTagArea fightingArea;
    private WorldTagArea spectatorArea;

    private BlockState groundSample;

    private SpleefStageTemplate() {
    }

    public SpleefStageTemplateInfo getInfo() {
        return info;
    }

    public SpleefStageOptions getDefaultOptions() {
        return defaultOptions;
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

    public WorldTagRocation getWaitingSpawnRocation() {
        return waitingSpawnRocation;
    }

    public WorldTagArea getGroundArea() {
        return groundArea;
    }

    public WorldTagArea getFightingArea() {
        return fightingArea;
    }

    public WorldTagArea getSpectatorArea() {
        return spectatorArea;
    }

    public BlockState getGroundSample() {
        return groundSample;
    }

    /*
    ここからSetter
     */

    public void setRelativeBaseLocation(WorldTagLocation relativeBaseLocation) {
        this.relativeBaseLocation = relativeBaseLocation;
    }

    public void setWaitingSpawnRocation(WorldTagRocation waitingSpawnRocation) {
        this.waitingSpawnRocation = waitingSpawnRocation;
    }

    public void setGroundArea(WorldTagArea groundArea) {
        this.groundArea = groundArea;
    }

    public void setFightingArea(WorldTagArea fightingArea) {
        this.fightingArea = fightingArea;
    }

    public void setSpectatorArea(WorldTagArea spectatorArea) {
        this.spectatorArea = spectatorArea;
    }

    public void setGroundSample(BlockState groundSample) {
        this.groundSample = groundSample;
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
            template.waitingSpawnRocation =
                    value.getNode("waiting_spawn_rocation").getValue(TypeToken.of(WorldTagRocation.class));
            template.fightingArea =
                    value.getNode("fighting_area").getValue(TypeToken.of(WorldTagArea.class));
            template.groundArea =
                    value.getNode("ground_area").getValue(TypeToken.of(WorldTagArea.class));
            template.spectatorArea =
                    value.getNode("spectator_area").getValue(TypeToken.of(WorldTagArea.class));

            List<WorldTagRocation> rocationsSaved =
                    value.getNode("spawn_rocations").getList(TypeToken.of(WorldTagRocation.class));

            template.spawnRocations = new ArrayList<>();
            template.spawnRocations.addAll(rocationsSaved);

            template.groundSample =
                    value.getNode("ground_sample").getValue(TypeToken.of(BlockState.class));

            return template;
        }

        @Override
        public void serialize(TypeToken<?> type, SpleefStageTemplate obj, ConfigurationNode value)
                throws ObjectMappingException {
            value.getNode("default_options").setValue(TypeToken.of(SpleefStageOptions.class), obj.defaultOptions);
            value.getNode("info").setValue(TypeToken.of(SpleefStageTemplateInfo.class), obj.info);
            value.getNode("relative_base_location")
                    .setValue(TypeToken.of(WorldTagLocation.class), obj.relativeBaseLocation);
            value.getNode("waiting_spawn_rocation")
                    .setValue(TypeToken.of(WorldTagRocation.class), obj.waitingSpawnRocation);
            value.getNode("fighting_area").setValue(TypeToken.of(WorldTagArea.class), obj.fightingArea);
            value.getNode("ground_area").setValue(TypeToken.of(WorldTagArea.class), obj.groundArea);
            value.getNode("spectator_area").setValue(TypeToken.of(WorldTagArea.class), obj.spectatorArea);

            ConfigurationNode nodeSpawnRocations = value.getNode("spawn_rocations");
            for (int i = 0; i < obj.spawnRocations.size(); i++) {
                nodeSpawnRocations.getNode(i)
                        .setValue(TypeToken.of(WorldTagRocation.class), obj.spawnRocations.get(i));
            }

            value.getNode("ground_sample").setValue(TypeToken.of(BlockState.class), obj.groundSample);
        }
    }
}
