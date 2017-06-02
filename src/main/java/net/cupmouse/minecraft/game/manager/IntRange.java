package net.cupmouse.minecraft.game.manager;

public class IntRange {

    public static final IntRange ACCEPT_ALL = new IntRange(null, null);

    private final Integer minNum;
    private final Integer maxNum;

    public IntRange(Integer minNum, Integer maxNum) {
        this.minNum = minNum;
        this.maxNum = maxNum;
    }

    public boolean isInRange(int num) {
        if (minNum != null && num < minNum) {
            return false;
        } else if (maxNum != null && num > maxNum) {
            return false;
        }

        return true;
    }

    public void checkInRange(int num) throws IllegalArgumentException {
        if (!isInRange(num)) {
            throw new IllegalArgumentException("範囲外です。適正("
                    + (minNum == null ? "-∞" : minNum) + "-" + (maxNum == null ? "+∞" : maxNum)
                    + ")入力値(" + num + ")");
        }
    }

    public static IntRange range(Integer minNum, Integer maxNum) {
        return new IntRange(minNum, maxNum);
    }
}
