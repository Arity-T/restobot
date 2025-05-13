package dev.tishenko.restobot.integration.tripadvisor;

public enum RadiusUnit {
    KM("km"),
    MI("mi"),
    M("m");

    private final String unit;

    RadiusUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return unit;
    }
}
