package net.cupmouse.minecraft.game.manager;

import java.util.Set;

public interface GameManager<T extends GameRoom> {

    Set<T> getRooms();
}
