package net.cupmouse.minecraft.game.creator.cmd.spleef;

import net.cupmouse.minecraft.game.creator.cmd.CCmdArguments;
import net.cupmouse.minecraft.game.spleef.SpleefRoom;
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

public class CCmdSpleefStageOption implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(spleefStageId(Text.of("stage_id"))),
                    onlyOne(string(Text.of("key"))),
                    optional(string(Text.of("value"))))
            .executor(new CCmdSpleefStageOption())
            .build();

    private Map<String, CEThrowableFunction<SpleefRoom, ?>> getters = new HashMap<>();
    private Map<String, CEThrowableDualConsumer<SpleefRoom, String>> setters = new HashMap<>();

    private CCmdSpleefStageOption() {
        // GETTER

        CEThrowableFunction<SpleefRoom, Object> defaultGameTimeGetter =
                spleefRoom -> spleefRoom.stage.getDefaultGameTime();
        this.getters.put("defaultGameTime", defaultGameTimeGetter);
        this.getters.put("dgt", defaultGameTimeGetter);

        CEThrowableFunction<SpleefRoom, Object> minimumPlayerCountGetter =
                spleefRoom -> spleefRoom.stage.getMinimumPlayerCount();
        this.getters.put("minimumPlayerCount", minimumPlayerCountGetter);
        this.getters.put("mpc", minimumPlayerCountGetter);


        // SETTER

        IntSetter defaultGameTimeSetter = new IntSetter((spleefRoom, integer) -> {
            if (integer < 1) {
                throw new CommandException(Text.of(TextColors.RED, "✗1以上を指定してください。"));
            }

            spleefRoom.stage.setDefaultGameTime(integer);
        });
        this.setters.put("defaultGameTime", defaultGameTimeSetter);
        this.setters.put("dgt", defaultGameTimeSetter);

        IntSetter minimumPlayerCountSetter = new IntSetter((spleefRoom, integer) -> {
            if (integer < 2) {
                throw new CommandException(Text.of(TextColors.RED, "✗2以上を指定してください。"), false);
            }

            spleefRoom.stage.setMinimumPlayerCount(integer);
        });

        this.setters.put("minimumPlayerCount", minimumPlayerCountSetter);
        this.setters.put("mpc", minimumPlayerCountSetter);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        SpleefRoom spleefRoom = args.<SpleefRoom>getOne("stage_id").get();
        String key = args.<String>getOne("key").get();

        if (args.hasAny("value")) {
            String value = args.<String>getOne("value").get();

            CEThrowableDualConsumer<SpleefRoom, String> setter = this.setters.get(key);
            if (setter == null) {
                throw new CommandException(Text.of(TextColors.RED, "✗キーが存在しません。"));
            }

            // セットする
            setter.accept(spleefRoom, value);

            src.sendMessage(Text.of(TextColors.AQUA, "✓", key, "を", value, "に設定しました。"));
        } else {
            CEThrowableFunction<SpleefRoom, ?> getter = this.getters.get(key);
            if (getter == null) {
                throw new CommandException(Text.of(TextColors.RED, "✗キーが存在しません。"));
            }

            Object value = getter.apply(spleefRoom);

            src.sendMessage(Text.of(TextColors.AQUA, key, " = ", value));
        }

        return CommandResult.success();
    }

    private static class IntSetter implements CEThrowableDualConsumer<SpleefRoom, String> {

        private final CEThrowableDualConsumer<SpleefRoom, Integer> setter;

        public IntSetter(CEThrowableDualConsumer<SpleefRoom, Integer> setter) {
            this.setter = setter;
        }

        @Override
        public void accept(SpleefRoom spleefRoom, String s) throws CommandException {
            int i;

            try {
                i = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                throw new CommandException(Text.of(TextColors.RED, "✗整数値を入力してください。"), false);
            }

            this.setter.accept(spleefRoom, i);
        }
    }
}
