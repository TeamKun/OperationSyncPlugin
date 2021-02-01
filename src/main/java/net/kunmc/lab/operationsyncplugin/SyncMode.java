package net.kunmc.lab.operationsyncplugin;

public enum SyncMode {

    ALL("all", "完全同期"),
    ADD("add", "加算"),;

    private final String id;
    private final String name;

    SyncMode(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
