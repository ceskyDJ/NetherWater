package cz.ceskydj.netherwater.database;

public enum WaterSource {
    BUCKET("bucket"),
    ICE("ice"),
    DISPENSER("dispenser");

    private String name;

    WaterSource(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
