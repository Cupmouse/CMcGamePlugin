package net.cupmouse.minecraft.game;

import com.google.inject.Inject;
import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.HeartbeatModule;
import net.cupmouse.minecraft.PluginModule;
import net.cupmouse.minecraft.PongPingModule;
import net.cupmouse.minecraft.beam.BeamModule;
import net.cupmouse.minecraft.db.DatabaseModule;
import net.cupmouse.minecraft.game.cmd.CommandModule;
import net.cupmouse.minecraft.game.creator.CreatorBank;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.game.data.user.GameUserDataModule;
import net.cupmouse.minecraft.game.manager.GameException;
import net.cupmouse.minecraft.game.manager.GameRoom;
import net.cupmouse.minecraft.game.mod.ModeratorCommandModule;
import net.cupmouse.minecraft.game.spleef.SpleefManager;
import net.cupmouse.minecraft.game.spleef.SpleefRoom;
import net.cupmouse.minecraft.worlds.WorldTag;
import net.cupmouse.minecraft.worlds.WorldTagModule;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.storage.WorldProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static net.cupmouse.minecraft.CMcCore.stopEternally;

@Plugin(id = "cmcgameplugin", name = "CMcGamePlugin", version = "alpha-0.1",
        description = "CMc game server plugin",
        authors = "Cupmouse", url = "http://www.cupmouse.net/")
public class CMcGamePlugin {

    private static final WorldTag WORLD_TAG_LOBBY = WorldTag.byName("lobby");

    private final CMcCore core;
    private final GameUserDataModule userm;
    private static Path configGamePath;
    private static GuiceObjectMapperFactory objectMapperFactory;
    private static HoconConfigurationLoader gameConfigLoader;
    private static CommentedConfigurationNode gameConfigNode;

    private static SpleefManager spleef;

    @Inject
    public CMcGamePlugin(PluginContainer pluginContainer, Logger logger, @ConfigDir(sharedRoot = false) Path configDir,
                         GuiceObjectMapperFactory objectMapperFactory) {
        CMcGamePlugin.objectMapperFactory = objectMapperFactory;
        PluginModule[] moduleArray = {
                new DatabaseModule(),
                new HeartbeatModule(),
                new WorldTagModule(),
                new PongPingModule(),
                this.userm = new GameUserDataModule(),
                new BeamModule(),
                spleef = new SpleefManager(),
                new ModeratorCommandModule(),
                new CommandModule(),
                new CreatorModule()
        };

        core = new CMcCore(this, pluginContainer, logger, configDir, moduleArray);
    }

    public static CommentedConfigurationNode getGameConfigNode() {
        return gameConfigNode;
    }

    public static SpleefManager getSpleef() {
        return spleef;
    }

    public GameUserDataModule getUserm() {
        return userm;
    }

    @Listener
    public void onPreInitialization(GamePreInitializationEvent event) {
        core.onPreInitialization(event);

//        WorldProperties worldProperties = Sponge.getServer().createWorldProperties("lobby",
//                WorldArchetype.builder()
//                        .generator(GeneratorTypes.FLAT)
//                        .generatorSettings(GeneratorTypes.FLAT.getGeneratorSettings().set(DataQuery.of())).build());
//        Sponge.getServer().loadWorld(worldProperties);

        configGamePath = CMcCore.getConfigDir().resolve("game.conf");

        // 設定ファイルが存在しない場合、jarファイル内のアセットフォルダからコピーする。
        if (!Files.exists(configGamePath)) {
            try {
                Files.createDirectories(CMcCore.getConfigDir());
                Sponge.getAssetManager().getAsset(this, "game.conf").get().copyToFile(configGamePath);
            } catch (IOException e) {
                e.printStackTrace();
                stopEternally();
            }
        }

        loadConfig();

        CMcCore.getLogger().info("ゲーム設定を読み込みました！");

        core.onPrePostInitialization();
    }

    @Listener
    public void onStoppedServer(GameStoppedServerEvent event) {
        saveConfig();
    }

    @Listener
    public void onClientDisconnected(ClientConnectionEvent.Disconnect event) {
        Optional<GameRoom> roomOptional = getRoomPlayerJoin(event.getTargetEntity());

        if (roomOptional.isPresent()) {
            try {
                roomOptional.get().tryLeaveRoom(event.getTargetEntity());
            } catch (GameException e) {
                e.printStackTrace();
            }
        }
    }

    @Listener(order = Order.FIRST)
    public void onBlockChange(ChangeBlockEvent event) {
        UUID worldUniqueId = event.getTransactions().get(0).getOriginal().getWorldUniqueId();
        WorldTagModule.isThis(WORLD_TAG_LOBBY, worldUniqueId);

        // ロビーは権限を持っている人物しか壊せない
        Optional<Player> playerOptional = event.getCause().first(Player.class);

        if (!playerOptional.isPresent() || !playerOptional.get().hasPermission("cmc.game.modify_lobby")) {
            // 人間じゃないならどんな変更も不可
            // 人間でも権限ないと不可
            event.setCancelled(true);
        }
    }

    public static Optional<GameRoom> getRoomPlayerJoin(Player player) {
        // TODO

        Optional<SpleefRoom> roomOptional = spleef.getRoomPlayerJoin(player);

        if (roomOptional.isPresent()) {
            return Optional.of(roomOptional.get());
        }

        return Optional.empty();
    }

    public static void reloadConfig() throws GameException, ObjectMappingException {
        spleef.save();
        saveConfig();
        loadConfig();
    }

    private static void saveConfig() {
        try {
            gameConfigLoader.save(gameConfigNode);
        } catch (IOException e) {
            e.printStackTrace();
            CMcCore.getLogger().warn("設定が保存できませんでした。[ゲーム設定]");
        }
    }

    private static void loadConfig() {
        // 設定をロードする
        gameConfigLoader = HoconConfigurationLoader.builder().setPath(configGamePath).build();

        try {
            CMcGamePlugin.gameConfigNode = gameConfigLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            stopEternally();
        }
    }
//
//    @Listener
//    public void onInitialization(GameInitializationEvent event) {
//        this.
//    }
}
