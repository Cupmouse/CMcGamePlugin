package net.cupmouse.minecraft.game.creator.cmd.spleef;

import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.game.spleef.SpleefStageOptions;
import net.cupmouse.minecraft.util.CEThrowableDualConsumer;
import net.cupmouse.minecraft.util.CEThrowableFunction;
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
import java.util.Map;

import static org.spongepowered.api.command.args.GenericArguments.*;

public class CCmdSpleefOption implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(string(Text.of("option_id"))),
                    optional(string(Text.of("value"))))
            .executor(new CCmdSpleefOption())
            .build();

    private Map<String, CEThrowableFunction<SpleefStageOptions, ?>> getters = new HashMap<>();
    private Map<String, CEThrowableDualConsumer<SpleefStageOptions, String>> setters = new HashMap<>();

    private CCmdSpleefOption() {
        // GETTER

        CEThrowableFunction<SpleefStageOptions, Object> gameTimeGetter = SpleefStageOptions::getGameTime;
        this.getters.put("gameTime", gameTimeGetter);
        this.getters.put("gt", gameTimeGetter);

        CEThrowableFunction<SpleefStageOptions, Object> minimumPlayerCountGetter =
                SpleefStageOptions::getMinimumPlayerCount;
        this.getters.put("minimumPlayerCount", minimumPlayerCountGetter);
        this.getters.put("mpc", minimumPlayerCountGetter);


        // SETTER

        IntSetter defaultGameTimeSetter = new IntSetter((optionsMutable, integer) -> {
            if (integer < 1) {
                throw new CommandException(Text.of(TextColors.RED, "✗整数で1以上を指定してください"));
            }

            optionsMutable.setGameTime(integer);
        });
        this.setters.put("gameTime", defaultGameTimeSetter);
        this.setters.put("gt", defaultGameTimeSetter);

        IntSetter minimumPlayerCountSetter = new IntSetter((stage, integer) -> {
            if (integer < 2) {
                throw new CommandException(Text.of(TextColors.RED, "✗整数で2以上を指定してください"), false);
            }

            stage.setMinimumPlayerCount(integer);
        });

        this.setters.put("minimumPlayerCount", minimumPlayerCountSetter);
        this.setters.put("mpc", minimumPlayerCountSetter);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String optionId = args.<String>getOne("option_id").get();

        if (args.hasAny("value")) {
            // valueが設定されているならばオプションIDに設定する
            String value = args.<String>getOne("value").get();

            CEThrowableDualConsumer<SpleefStageOptions, String> setter = this.setters.get(optionId);
            if (setter == null) {
                throw new CommandException(Text.of(TextColors.RED, "✗オプションIDが間違っています"));
            }

            // テンプレートが選択されていない場合は例外が発生し続行されない
            SpleefStageOptions options =
                    CreatorModule.getOrCreateBankOf(src).getSpleefSelectedTemplateOrThrow().getDefaultOptions();

            // 値をセットしてもらう
            setter.accept(options, value);

            src.sendMessage(Text.of(TextColors.GOLD, String.format("✓%sを%sに設定しました", optionId, value)));
        } else {
            // valueが設定されていないならオプションIDに設定されている内容を表示する
            CEThrowableFunction<SpleefStageOptions, ?> getter = this.getters.get(optionId);
            if (getter == null) {
                throw new CommandException(Text.of(TextColors.RED, "✗オプションIDが間違っています"));
            }

            SpleefStageOptions options =
                    CreatorModule.getOrCreateBankOf(src).getSpleefSelectedTemplateOrThrow().getDefaultOptions();

            // ゲッターから値をもらう
            Object value = getter.apply(options);
            src.sendMessage(Text.of(TextColors.GOLD, optionId, " = ", value));
        }

        return CommandResult.success();
    }

    private static class IntSetter implements CEThrowableDualConsumer<SpleefStageOptions, String> {

        private final CEThrowableDualConsumer<SpleefStageOptions, Integer> setter;

        IntSetter(CEThrowableDualConsumer<SpleefStageOptions, Integer> setter) {
            this.setter = setter;
        }

        @Override
        public void accept(SpleefStageOptions options, String s) throws CommandException {
            int i;

            try {
                i = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                throw new CommandException(Text.of(TextColors.RED, "✗整数値を入力してください。"), false);
            }

            this.setter.accept(options, i);
        }
    }
}
