package net.cupmouse.minecraft.game.creator.cmd.spleef;

import net.cupmouse.minecraft.game.spleef.SpleefStage;
import net.cupmouse.minecraft.game.spleef.SpleefStageTemplate;
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

import static net.cupmouse.minecraft.game.creator.cmd.CCmdArguments.spleefStageId;
import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.optional;
import static org.spongepowered.api.command.args.GenericArguments.string;

public class CCmdSpleefOption implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(string(Text.of("key"))),
                    optional(string(Text.of("value"))))
            .executor(new CCmdSpleefOption())
            .build();

    private Map<String, CEThrowableFunction<SpleefStage, ?>> getters = new HashMap<>();
    private Map<String, CEThrowableDualConsumer<SpleefStage, String>> setters = new HashMap<>();

    private CCmdSpleefOption() {
        // GETTER

        CEThrowableFunction<SpleefStage, Object> defaultGameTimeGetter = SpleefStageTemplate::getDefaultOptions;
        this.getters.put("defaultGameTime", defaultGameTimeGetter);
        this.getters.put("dgt", defaultGameTimeGetter);

        CEThrowableFunction<SpleefStage, Object> minimumPlayerCountGetter = SpleefStage::getMinimumPlayerCount;
        this.getters.put("minimumPlayerCount", minimumPlayerCountGetter);
        this.getters.put("mpc", minimumPlayerCountGetter);


        // SETTER

        IntSetter defaultGameTimeSetter = new IntSetter((stage, integer) -> {
            if (integer < 1) {
                throw new CommandException(Text.of(TextColors.RED, "✗1以上を指定してください。"));
            }

            stage.setDefaultGameTime(integer);
        });
        this.setters.put("defaultGameTime", defaultGameTimeSetter);
        this.setters.put("dgt", defaultGameTimeSetter);

        IntSetter minimumPlayerCountSetter = new IntSetter((stage, integer) -> {
            if (integer < 2) {
                throw new CommandException(Text.of(TextColors.RED, "✗2以上を指定してください。"), false);
            }

            stage.setMinimumPlayerCount(integer);
        });

        this.setters.put("minimumPlayerCount", minimumPlayerCountSetter);
        this.setters.put("mpc", minimumPlayerCountSetter);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        SpleefStage spleefStage = args.<SpleefStage>getOne("stage_id").get();
        String key = args.<String>getOne("key").get();

        if (args.hasAny("value")) {
            String value = args.<String>getOne("value").get();

            CEThrowableDualConsumer<SpleefStage, String> setter = this.setters.get(key);
            if (setter == null) {
                throw new CommandException(Text.of(TextColors.RED, "✗キーが存在しません。"));
            }

            // セットする
            setter.accept(spleefStage, value);

            src.sendMessage(Text.of(TextColors.AQUA, "✓", key, "を", value, "に設定しました。"));
        } else {
            CEThrowableFunction<SpleefStage, ?> getter = this.getters.get(key);
            if (getter == null) {
                throw new CommandException(Text.of(TextColors.RED, "✗キーが存在しません。"));
            }

            Object value = getter.apply(spleefStage);

            src.sendMessage(Text.of(TextColors.AQUA, key, " = ", value));
        }

        return CommandResult.success();
    }

    private static class IntSetter implements CEThrowableDualConsumer<SpleefStage, String> {

        private final CEThrowableDualConsumer<SpleefStage, Integer> setter;

        public IntSetter(CEThrowableDualConsumer<SpleefStage, Integer> setter) {
            this.setter = setter;
        }

        @Override
        public void accept(SpleefStage stage, String s) throws CommandException {
            int i;

            try {
                i = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                throw new CommandException(Text.of(TextColors.RED, "✗整数値を入力してください。"), false);
            }

            this.setter.accept(stage, i);
        }
    }
}
