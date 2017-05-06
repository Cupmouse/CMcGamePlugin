package net.cupmouse.minecraft.game.spleef;

import net.cupmouse.minecraft.game.manager.GameManager;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class SpleefManager implements GameManager{


    private HashMap<Integer, SpleefRoom> rooms;

    @Override
    public Set getRooms() {
        return rooms;
    }
}
