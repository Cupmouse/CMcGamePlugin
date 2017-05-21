package net.cupmouse.minecraft.game.manager;

import net.cupmouse.minecraft.PluginModule;
import net.cupmouse.minecraft.game.spleef.SpleefRoom;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface GameManager<T extends GameRoom> extends PluginModule {

    Collection<T> getRooms();

    Optional<T> getRoom(int roomNumber);
}
