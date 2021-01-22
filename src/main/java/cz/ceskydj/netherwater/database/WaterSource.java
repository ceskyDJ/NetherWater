package cz.ceskydj.netherwater.database;

public enum WaterSource {
    BUCKET("bucket"),
    ICE("ice"),
    DISPENSER("dispenser"),
    WORLD_EDIT("world-edit");

    private final String name;

    WaterSource(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
