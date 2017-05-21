package net.cupmouse.minecraft.game.manager;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.TextMessageException;

public class GameRoomException extends TextMessageException {

    public GameRoomException() {
    }

    public GameRoomException(Text message) {
        super(message);
    }

    public GameRoomException(Text message, Throwable throwable) {
        super(message, throwable);
    }

    public GameRoomException(Throwable throwable) {
        super(throwable);
    }
}
