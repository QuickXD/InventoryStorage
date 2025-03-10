package it.quick.inventorybackup.database;

import java.util.UUID;

public class Backup {
    private final UUID playerUUID;
    private final String username;
    private final long timestamp;

    public Backup(UUID playerUUID, String username, long timestamp) {
        this.playerUUID = playerUUID;
        this.username = username;
        this.timestamp = timestamp;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getUsername() {
        return username;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getDateFormatted() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new java.util.Date(timestamp));
    }
}
