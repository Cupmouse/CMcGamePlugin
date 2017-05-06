package net.cupmouse.minecraft.game.manager;

import java.util.Collection;
import java.util.Set;

public interface GameManager<T extends GameRoom> {

    Collection<T> getRooms();
}
