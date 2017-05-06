package net.cupmouse.minecraft.game.spleef;

import net.cupmouse.minecraft.game.manager.GameManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SpleefManager implements GameManager<SpleefRoom> {

    private Map<Integer, SpleefRoom> rooms = new HashMap<>();

    @Override
    public Collection<SpleefRoom> getRooms() {
        return rooms.values();
    }
}
