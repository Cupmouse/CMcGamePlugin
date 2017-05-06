package net.cupmouse.minecraft.game.manager;

public enum GameRoomState {

    /**
     * ゲームの準備段階。
     * プレイヤーが足りないか、満員でないがプレイヤー数は足りているので、追加プレイヤーを待っているときの状態。
     * ゲームによって詳細な状態は異なる。
     */
    WAITING_PLAYERS,

    /**
     * ゲームに必要な条件を満たしているため、ゲームが始まろうとしている状態。
     */
    READY,

    /**
     * ゲーム中の状態。
     */
    IN_PROGRESS,

    /**
     * ゲームが終了したが、その終了したゲームに関するイベントが行われている状態。
     * 例えば、結果発表など。
     */
    FINISHED,

    /**
     * 次のゲームの準備をしている状態。
     */
    PREPARING,

    /**
     * ゲームを開催できない状態。
     */
    CLOSED;
}
