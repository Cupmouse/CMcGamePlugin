package net.cupmouse.minecraft.game.manager;

import net.cupmouse.minecraft.PluginModule;

import java.util.Collection;
import java.util.Set;

public interface GameManager<T extends GameRoom> extends PluginModule {

    Collection<T> getRooms();
}
