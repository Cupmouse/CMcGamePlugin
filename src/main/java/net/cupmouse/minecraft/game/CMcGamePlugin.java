package net.cupmouse.minecraft.game;

import com.google.inject.Inject;
import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.HeartbeatModule;
import net.cupmouse.minecraft.PluginModule;
import net.cupmouse.minecraft.PongPingModule;
import net.cupmouse.minecraft.beam.BeamModule;
import net.cupmouse.minecraft.data.user.UserDataModule;
import net.cupmouse.minecraft.db.DatabaseModule;
import net.cupmouse.minecraft.game.data.user.GameUserDataModule;
import net.cupmouse.minecraft.worlds.WorldsModule;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.plugin.Plugin;

import java.nio.file.Path;

@Plugin(id = "cmcgameplugin", name = "CMcGamePlugin", version = "alpha-0.1",
        description = "CMc game server plugin",
        authors = "Cupmouse", url = "http://www.cupmouse.net/")
public class CMcGamePlugin {

    private final GameUserDataModule userm;

    @Inject
    public CMcGamePlugin(Game game, Logger logger, @ConfigDir(sharedRoot = false) Path configDir) {
        PluginModule[] moduleArray = {
                new DatabaseModule(),
                new HeartbeatModule(),
                new PongPingModule(),
                this.userm = new GameUserDataModule(),
                new WorldsModule(),
                new BeamModule()
        };

        CMcCore core = new CMcCore(game, this, logger, configDir, moduleArray);
    }

    public GameUserDataModule getUserm() {
        return userm;
    }
}
