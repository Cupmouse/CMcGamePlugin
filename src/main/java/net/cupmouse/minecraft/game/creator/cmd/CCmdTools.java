package net.cupmouse.minecraft.game.creator.cmd;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.text.Text;

import java.util.Iterator;

public class CCmdTools implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(GenericArguments.none())
            .executor(new CCmdTools())
            .build();


    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(Text.of("プレイヤーのみ実行可能"));
        }

        Iterator<Inventory> iterator = ((Player) src).getInventory().query(Hotbar.class).slots().iterator();

        Inventory slot1 = iterator.next();
        slot1.set(ItemStack.of(ItemTypes.MAGMA, 1));
        Inventory slot2 = iterator.next();
        slot2.set(ItemStack.of(ItemTypes.PACKED_ICE, 1));
        Inventory slot3 = iterator.next();
        slot3.set(ItemStack.of(ItemTypes.WOOL, 1));

        return CommandResult.success();
    }
}
