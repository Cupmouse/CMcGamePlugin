package net.cupmouse.minecraft.game.creator.cmd.spleef;

import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.game.spleef.SpleefStageTemplateInfo;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.spongepowered.api.command.args.GenericArguments.*;

public class CCmdSpleefInfo implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(string(Text.of("option_id"))),
                    optional(string(Text.of("value"))))
            .executor(new CCmdSpleefInfo())
            .build();

    private Map<String, Function<SpleefStageTemplateInfo, ?>> getters = new HashMap<>();
    private Map<String, BiConsumer<SpleefStageTemplateInfo, String>> setters = new HashMap<>();

    private CCmdSpleefInfo() {
        // GETTER

        Function<SpleefStageTemplateInfo, String> nameGetter = SpleefStageTemplateInfo::getName;
        this.getters.put("name", nameGetter);
        this.getters.put("n", nameGetter);

        Function<SpleefStageTemplateInfo, String> descriptionGetter =
                SpleefStageTemplateInfo::getDescription;
        this.getters.put("description", descriptionGetter);
        this.getters.put("d", descriptionGetter);

        Function<SpleefStageTemplateInfo, String> versionGetter = SpleefStageTemplateInfo::getVersion;
        this.getters.put("version", versionGetter);
        this.getters.put("v", versionGetter);

        Function<SpleefStageTemplateInfo, String> buildersGetter = spleefStageTemplateInfo ->
                spleefStageTemplateInfo.getBuilders().stream().collect(Collectors.joining(","));
        this.getters.put("builders", buildersGetter);
        this.getters.put("b", buildersGetter);

        // SETTER

        BiConsumer<SpleefStageTemplateInfo, String> nameSetter = SpleefStageTemplateInfo::setName;
        this.setters.put("name", nameSetter);
        this.setters.put("n", nameSetter);

        BiConsumer<SpleefStageTemplateInfo, String> descriptionSetter = SpleefStageTemplateInfo::setDescription;
        this.setters.put("description", descriptionSetter);
        this.setters.put("d", descriptionSetter);

        BiConsumer<SpleefStageTemplateInfo, String> versionSetter = SpleefStageTemplateInfo::setVersion;
        this.setters.put("version", versionSetter);
        this.setters.put("v", versionSetter);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String infoId = args.<String>getOne("info_id").get();

        if (args.hasAny("value")) {
            // valueが設定されているならばオプションIDに設定する
            String value = args.<String>getOne("value").get();

            if (infoId.startsWith("builder.")) {
                // 作成者を追加もしくは変更するセッターを使わない例外コマンド

                int builderIndex;

                try {
                    builderIndex = Integer.parseInt(infoId.substring("builder.".length()));
                } catch (NumberFormatException e) {
                    throw new CommandException(Text.of(TextColors.RED, "✗0以上の整数を入力してください"), false);
                }

                List<String> builders = CreatorModule.getOrCreateBankOf(src)
                        .getSpleefSelectedTemplateOrThrow().getInfo().getBuilders();


                if (value.equals("\\")) {
                    // バックスペースのときは削除の意味、nullを設定する

                    if (builderIndex < 0|| builderIndex >= builders.size()) {
                        throw new CommandException(Text.of(TextColors.RED, String.format(
                                "✗%d個の作成者が設定されています、それ未満の値を入力してください", builderIndex))
                                , false);
                    }

                    String removed = builders.remove(builderIndex);

                    src.sendMessage(Text.of(TextColors.GOLD,
                            String.format("✓%d番目の作成者%sを削除しました", builderIndex, removed)));
                    return CommandResult.success();
                } else {
                    if (builders.size() == builderIndex) {
                        builders.add(builderIndex, value);
                    } else {
                        builders.set(builderIndex, value);
                    }
                    src.sendMessage(Text.of(TextColors.GOLD,
                            String.format("✓%d番目の作成者を%sに設定しました", builderIndex, value)));

                    return CommandResult.success();
                }
            } else {
                // セッターにセットしてもらう

                BiConsumer<SpleefStageTemplateInfo, String> setter = this.setters.get(infoId);
                if (setter == null) {
                    throw new CommandException(Text.of(TextColors.RED, "✗情報IDが間違っています"));
                }

                // テンプレートが選択されていない場合は例外が発生し続行されない
                SpleefStageTemplateInfo info =
                        CreatorModule.getOrCreateBankOf(src).getSpleefSelectedTemplateOrThrow().getInfo();

                // 値をセットしてもらう
                setter.accept(info, value);
            }

            src.sendMessage(Text.of(TextColors.GOLD, String.format("✓%sを%sに設定しました", infoId, value)));
        } else {
            // valueがないなら表示するだけ
            Function<SpleefStageTemplateInfo, ?> getter = this.getters.get(infoId);
            if (getter == null) {
                throw new CommandException(Text.of(TextColors.RED, "✗情報IDが間違っています"));
            }

            SpleefStageTemplateInfo info =
                    CreatorModule.getOrCreateBankOf(src).getSpleefSelectedTemplateOrThrow().getInfo();

            // ゲッターから値をもらう
            Object value = getter.apply(info);
            src.sendMessage(Text.of(TextColors.GOLD, infoId, " = ", value));
        }


        return CommandResult.success();
    }
}
