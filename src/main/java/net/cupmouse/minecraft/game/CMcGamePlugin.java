package net.cupmouse.minecraft.game;

import com.google.inject.Inject;
import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.HeartbeatModule;
import net.cupmouse.minecraft.PluginModule;
import net.cupmouse.minecraft.PongPingModule;
import net.cupmouse.minecraft.beam.BeamModule;
import net.cupmouse.minecraft.db.DatabaseModule;
import net.cupmouse.minecraft.game.data.user.GameUserDataModule;
import net.cupmouse.minecraft.worlds.WorldTagModule;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.cupmouse.minecraft.CMcCore.stopEternally;

@Plugin(id = "cmcgameplugin", name = "CMcGamePlugin", version = "alpha-0.1",
        description = "CMc game server plugin",
        authors = "Cupmouse", url = "http://www.cupmouse.net/")
public class CMcGamePlugin {

    private final CMcCore core;
    private final GameUserDataModule userm;
    private Path configGamePath;
    private HoconConfigurationLoader gameConfigLoader;
    private static CommentedConfigurationNode gameConfigNode;

    @Inject
    public CMcGamePlugin(Logger logger, @ConfigDir(sharedRoot = false) Path configDir) {
        PluginModule[] moduleArray = {
                new DatabaseModule(),
                new HeartbeatModule(),
                new WorldTagModule(),
                new PongPingModule(),
                this.userm = new GameUserDataModule(),
                new BeamModule()
        };

        core = new CMcCore(this, logger, configDir, moduleArray);
    }

    public static CommentedConfigurationNode getGameConfigNode() {
        return gameConfigNode;
    }

    public GameUserDataModule getUserm() {
        return userm;
    }

    @Listener
    public void onPreInitialization(GamePreInitializationEvent event) {
        this.configGamePath = CMcCore.getConfigDir().resolve("game.conf");

        // 設定ファイルが存在しない場合、jarファイル内のアセットフォルダからコピーする。
        if (!Files.exists(configGamePath)) {
            try {
                Files.createDirectories(CMcCore.getConfigDir());
                Sponge.getAssetManager().getAsset(this, "common.conf").get().copyToFile(configGamePath);
            } catch (IOException e) {
                e.printStackTrace();
                stopEternally();
            }
        }

        // 設定をロードする
        this.gameConfigLoader = HoconConfigurationLoader.builder().setPath(configGamePath).build();

        try {
            CMcGamePlugin.gameConfigNode = gameConfigLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            stopEternally();
        }



        CMcCore.getLogger().info("ゲーム設定を読み込みました！");
    }
//
//    @Listener
//    public void onInitialization(GameInitializationEvent event) {
//        this.
//    }
}
