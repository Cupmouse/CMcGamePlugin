package net.cupmouse.minecraft.game.manager;

import net.cupmouse.minecraft.PluginModule;
import net.cupmouse.minecraft.game.spleef.SpleefRoom;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface GameManager<T extends GameRoom> extends PluginModule {

    Collection<T> getRooms();

    Set<Map.Entry<SpleefRoom, Integer>> getRoomsAndItsNumber();

    Optional<T> getRoom(int roomNumber);
}
