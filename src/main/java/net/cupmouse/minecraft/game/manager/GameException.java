package net.cupmouse.minecraft.game.manager;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.TextMessageException;

public class GameException extends TextMessageException {

    public GameException() {
    }

    public GameException(Text message) {
        super(message);
    }

    public GameException(Text message, Throwable throwable) {
        super(message, throwable);
    }

    public GameException(Throwable throwable) {
        super(throwable);
    }
}
