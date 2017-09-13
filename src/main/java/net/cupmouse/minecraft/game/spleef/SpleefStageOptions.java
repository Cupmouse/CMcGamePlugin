package net.cupmouse.minecraft.game.spleef;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

// 今のところプレイヤーがステージのデフォルト設定を変更できない。
public class SpleefStageOptions {

    private int itemRandomConstant;

    SpleefStageOptions() {
    }

    private int gameTime;
    private int minimumPlayerCount;

    public int getGameTime() {
        return gameTime;
    }

    public void setGameTime(int gameTime) {
        this.gameTime = gameTime;
    }

    public void setItemRandomConstant(int itemRandomConstant) {
        this.itemRandomConstant = itemRandomConstant;
    }

    public int getMinimumPlayerCount() {
        return minimumPlayerCount;
    }

    public void setMinimumPlayerCount(int minimumPlayerCount) {
        this.minimumPlayerCount = minimumPlayerCount;
    }

    public int getItemRandomConstant() {
        return itemRandomConstant;
    }

    public SpleefStageOptions copy() {
        SpleefStageOptions copied = new SpleefStageOptions();

        copied.gameTime = this.gameTime;
        copied.minimumPlayerCount = this.minimumPlayerCount;
        copied.itemRandomConstant = this.itemRandomConstant;

        return copied;
    }

    static class Serializer implements TypeSerializer<SpleefStageOptions> {

        @Override
        public SpleefStageOptions deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
            SpleefStageOptions options = new SpleefStageOptions();

            options.gameTime = value.getNode("game_time").getInt();
            options.minimumPlayerCount = value.getNode("minimum_player_count").getInt();
            options.itemRandomConstant = value.getNode("item_random_constant").getInt();

            return options;
        }

        @Override
        public void serialize(TypeToken<?> type, SpleefStageOptions obj, ConfigurationNode value) throws ObjectMappingException {
            value.getNode("game_time").setValue(obj.gameTime);
            value.getNode("minimum_player_count").setValue(obj.minimumPlayerCount);
            value.getNode("item_random_constant").setValue(obj.itemRandomConstant);
        }
    }
}
