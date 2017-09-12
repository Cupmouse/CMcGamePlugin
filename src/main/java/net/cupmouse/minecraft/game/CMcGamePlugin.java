package net.cupmouse.minecraft.game;

import com.google.inject.Inject;
import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.HeartbeatModule;
import net.cupmouse.minecraft.PluginModule;
import net.cupmouse.minecraft.PongPingModule;
import net.cupmouse.minecraft.beam.BeamModule;
import net.cupmouse.minecraft.db.DatabaseModule;
import net.cupmouse.minecraft.game.cmd.CommandModule;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.game.data.user.GameUserDataModule;
import net.cupmouse.minecraft.game.manager.GameException;
import net.cupmouse.minecraft.game.manager.GameRoom;
import net.cupmouse.minecraft.game.mod.ModeratorCommandModule;
import net.cupmouse.minecraft.game.spleef.SpleefManager;
import net.cupmouse.minecraft.game.spleef.SpleefRoom;
import net.cupmouse.minecraft.worlds.WorldTag;
import net.cupmouse.minecraft.worlds.WorldTagModule;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.gamerule.DefaultGameRules;
import org.spongepowered.api.world.storage.WorldProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static net.cupmouse.minecraft.CMcCore.stopEternally;

@Plugin(id = "cmcgameplugin", name = "CMcGamePlugin", version = "alpha-0.1.1",
        description = "CMc Minigame plugin",
        authors = "Cupmouse", url = "http://www.cupmouse.net/")
public class CMcGamePlugin {

    public static final WorldTag WORLD_TAG_LOBBY = WorldTag.byName("lobby");

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
    public void onServerAboutToStart(GameAboutToStartServerEvent event) {
        // ワールドを作成する
        // TODO 動きません https://github.com/SpongePowered/SpongeCommon/issues/1169

//        // Setting generator option from WorldInfo
//
//        if (archetype.getGeneratorType() == GeneratorTypes.FLAT) {
//            Optional<String> generatorSettingStringOpt = archetype.getGeneratorSettings().getString(DataQueries.WORLD_CUSTOM_SETTINGS);
//
//            // The key customSettings is used for just FLAT world for now (1.12).
//            // It contains settings in custom format for flat world also known as preset.
//            generatorSettingStringOpt.ifPresent(s -> this.generatorOptions = s);
//        } else {
//            if (archetype.getGeneratorSettings().) {
//                // If customSetting were not set, then it could be setting for the customized world type which you can change
//                // ore placement to terrain noise stuff and some other various things.
//
//            }
//        }
//        if (this.generatorOptions.isEmpty()) {
//            // No settings for world generator
//            this.generatorOptions = "";
//        }

//        // generatorSettingsはCUSTOMワールド生成を使用したときにjsonをぶっこむ、FLATのときはcustomSettingsがキーで
//        // 値はそのまま独自フォーマットを入れる
//        WorldArchetype worldArchetypeTemplate = WorldArchetype.builder()
//                .enabled(true)
//                .loadsOnStartup(true)
//                .keepsSpawnLoaded(true)
//                .commandsAllowed(true)
//                .dimension(DimensionTypes.OVERWORLD)
//                .gameMode(GameModes.SURVIVAL)
//                .difficulty(Difficulties.EASY)
//                .generateBonusChest(false)
//                .generateSpawnOnLoad(true)
//                .usesMapFeatures(false)
//                .hardcore(false)
//                // TODO これ以外無いのか
////                            .portalAgent(null)
//                .seed(0)
//                .generator(GeneratorTypes.FLAT)
//                .generatorSettings(DataContainer.createNew().set(DataQuery.of("customSettings"),
//                        "3;30*minecraft:bedrock;1"))
//                .build("cmc:template", "CMc World Template Settings");
//
//        try {
//            WorldProperties spleefWorldProperties = Sponge.getServer().createWorldProperties("game",
//                    WorldArchetype.builder().from(worldArchetypeTemplate)
//                            .difficulty(Difficulties.PEACEFUL)
//                            .build("cmc:spleef", "CMc Spleef World Settings"));
//
//            setGameRule(spleefWorldProperties);
//
//            Sponge.getServer().loadWorld(spleefWorldProperties);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void setGameRule(WorldProperties worldProperties) {
        worldProperties.setGameRule(DefaultGameRules.ANNOUNCE_ADVANCEMENTS, "false");
        worldProperties.setGameRule(DefaultGameRules.COMMAND_BLOCK_OUTPUT, "true");
        worldProperties.setGameRule(DefaultGameRules.DISABLE_ELYTRA_MOVEMENT_CHECK, "false");
        worldProperties.setGameRule(DefaultGameRules.DO_DAYLIGHT_CYCLE, "false");
        worldProperties.setGameRule(DefaultGameRules.DO_ENTITY_DROPS, "false");
        worldProperties.setGameRule(DefaultGameRules.DO_FIRE_TICK, "false");
        worldProperties.setGameRule(DefaultGameRules.DO_LIMITED_CRAFTING, "false");
        worldProperties.setGameRule(DefaultGameRules.DO_MOB_LOOT, "false");
        worldProperties.setGameRule(DefaultGameRules.DO_TILE_DROPS, "false");
        worldProperties.setGameRule(DefaultGameRules.DO_WEATHER_CYCLE, "false");
        worldProperties.setGameRule(DefaultGameRules.GAME_LOOP_FUNCTION, "false");
        worldProperties.setGameRule(DefaultGameRules.KEEP_INVENTORY, "false");
        worldProperties.setGameRule(DefaultGameRules.LOG_ADMIN_COMMANDS, "true");
        worldProperties.setGameRule(DefaultGameRules.MAX_COMMAND_CHAIN_LENGTH, "0");
        worldProperties.setGameRule(DefaultGameRules.MAX_ENTITY_CRAMMING, "4");
        worldProperties.setGameRule(DefaultGameRules.MOB_GRIEFING, "false");
        worldProperties.setGameRule(DefaultGameRules.NATURAL_REGENERATION, "true");
        worldProperties.setGameRule(DefaultGameRules.RANDOM_TICK_SPEED, "0"); // Disable random tick
        worldProperties.setGameRule(DefaultGameRules.REDUCED_DEBUG_INFO, "false");
        worldProperties.setGameRule(DefaultGameRules.SEND_COMMAND_FEEDBACK, "true");
        worldProperties.setGameRule(DefaultGameRules.SHOW_DEATH_MESSAGES, "false");
        worldProperties.setGameRule(DefaultGameRules.SPAWN_RADIUS, "0");
        worldProperties.setGameRule(DefaultGameRules.SPECTATORS_GENERATE_CHUNKS, "false");
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
        if (CreatorModule.isCreatorEnabled()) {
            // クリエイターモードがONならどんな変更も許される
            return;
        }
        UUID worldUniqueId = event.getTransactions().get(0).getOriginal().getWorldUniqueId();
        // TODO
        WorldTagModule.isThis(WORLD_TAG_LOBBY, worldUniqueId);

        // ロビーは権限を持っている人物しか壊せない
        Optional<Player> playerOptional = event.getCause().first(Player.class);

        if (!playerOptional.isPresent() || !playerOptional.get().hasPermission("cmc.game.modify_lobby")) {
            // 人間じゃないならどんな変更も不可
            // 人間でも権限ないと不可
            event.setCancelled(true);
        }
    }

    @Listener
    public void onEntityDamaged(DamageEntityEvent event) {
        // ダメージを受けない
        if (event.getTargetEntity() instanceof Player) {
            event.setBaseDamage(0);
        }
    }

    @Listener
    public void onClientConnect(ClientConnectionEvent.Login event) {
        // ロビーに戻す
        event.setToTransform(new Transform<>(WorldTagModule.getTaggedWorld(WORLD_TAG_LOBBY).get().getSpawnLocation()));
    }

    @Listener
    public void onServerStarted(GameStartedServerEvent event) {
        // プレイヤーを自動回復する。TODO Sponge対応待ち
        Task.builder().name("Player Healing")
                .intervalTicks(120)
                .execute(() -> {
                    for (Player player : Sponge.getServer().getOnlinePlayers()) {
                        player.foodLevel().set(20);
                        player.saturation().set(20D);
                        player.health().set(20D);
                    }
                }).submit(CMcCore.getPlugin());
    }

    @Listener
    public void onTNTPrimeSpawn(SpawnEntityEvent event) {
        for (Entity entity : event.getEntities()) {
            if (entity.getType() == EntityTypes.PRIMED_TNT || entity.getType() == EntityTypes.TNT_MINECART) {
                event.setCancelled(true);
                return;
            }
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
