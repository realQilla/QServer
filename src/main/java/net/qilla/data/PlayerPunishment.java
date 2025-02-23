package net.qilla.data;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.util.TimeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public final class PlayerPunishment {

    public static final long PERMANENT = -1;
    public static final long KICK_LENGTH = 15000;

    private final PunishmentType type;
    private final long on;
    private final UUID by;
    private final String reason;
    private long expiration;
    private boolean forceExpired;

    public PlayerPunishment(@NotNull PunishmentType type, long on, @Nullable UUID by, @NotNull String reason, long expiration) {
        this.type = type;
        this.on = on;
        this.by = by;
        this.reason = reason;
        this.expiration = expiration < 0 ? -1 : Math.max(System.currentTimeMillis(), expiration);
    }

    public @NotNull PunishmentType getType() {
        return type;
    }

    public long getOn() {
        return on;
    }

    public @Nullable UUID getBy() {
        return by;
    }

    public @NotNull String getReason() {
        return reason;
    }

    public long getExpiration() {
        return expiration;
    }

    public boolean isPermanent() {
        return expiration == -1;
    }

    public boolean isExpired() {
        return !this.isPermanent() && System.currentTimeMillis() > expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = Math.max(System.currentTimeMillis(), expiration);
    }

    public void setExpired() {
        this.expiration = System.currentTimeMillis();
        this.forceExpired = true;
    }

    public static @NotNull Component initialMsg(@NotNull PlayerPunishment punishment) {
        String byUser = punishment.by == null ? "Administrator" : PDRegistry.getInstance().get(punishment.by).getUsername();

        return switch(punishment.type) {
            case MUTE -> MiniMessage.miniMessage().deserialize("<red>You have been muted by <yellow>" + byUser + "</yellow> for <yellow>" + punishment.reason + "</yellow> until <yellow>" + TimeUtil.date(punishment.expiration) + "</yellow>!</red>");
            case KICK -> MiniMessage.miniMessage().deserialize("<red>You have been removed from the server!</red>")
                    .appendNewline()
                    .appendNewline().append(MiniMessage.miniMessage().deserialize("<red>On " + "<yellow>⏳ " + TimeUtil.date(punishment.on) + "<red> by</red> " + byUser + "</yellow>"))
                    .appendNewline()
                    .appendNewline().append(MiniMessage.miniMessage().deserialize("<yellow>" + punishment.reason + "</yellow>"))
                    .appendNewline()
                    .appendNewline().append(MiniMessage.miniMessage().deserialize("<green>⏳ You may rejoin shortly!</green>"));
            case BLACKLIST -> punishment.isPermanent() ?
                    MiniMessage.miniMessage().deserialize("<red>⚠ You have been permanently blacklisted from the server!</red>")
                            .appendNewline()
                            .appendNewline().append(MiniMessage.miniMessage().deserialize("<red>On " + "<yellow>⏳ " + TimeUtil.date(punishment.on) + "<red> by</red> " + byUser + "</yellow>"))
                            .appendNewline()
                            .appendNewline().append(MiniMessage.miniMessage().deserialize("<yellow>" + punishment.reason + "</yellow>"))
                            .appendNewline()
                            .appendNewline().append(MiniMessage.miniMessage().deserialize("<blue>ℹ It is advised that you read and follow</blue>"))
                            .appendNewline().append(MiniMessage.miniMessage().deserialize("<blue>our server rules in the future.</blue>"))
                            .appendNewline()
                            .appendNewline().append(MiniMessage.miniMessage().deserialize("<gray>Contact an administrator if you believe this to be a mistake</gray>"))
                    :
                    MiniMessage.miniMessage().deserialize("<red>⚠ You have been blacklisted from the server!</red>")
                            .appendNewline()
                            .appendNewline().append(MiniMessage.miniMessage().deserialize("<red>On " + "<yellow>⏳ " + TimeUtil.date(punishment.on) + "<red> by</red> " + byUser + "</yellow>"))
                            .appendNewline().append(MiniMessage.miniMessage().deserialize("<red><yellow>\uD83D\uDD14 " + TimeUtil.remaining(punishment.expiration - System.currentTimeMillis(), false) + "</yellow> remaining"))
                            .appendNewline()
                            .appendNewline().append(MiniMessage.miniMessage().deserialize("<yellow>" + punishment.reason + "</yellow>"))
                            .appendNewline()
                            .appendNewline().append(MiniMessage.miniMessage().deserialize("<blue>ℹ It is advised that you read and follow</blue>"))
                            .appendNewline().append(MiniMessage.miniMessage().deserialize("<blue>our server rules in the future.</blue>"))
                            .appendNewline()
                            .appendNewline().append(MiniMessage.miniMessage().deserialize("<gray>Contact an administrator if you believe this to be a mistake</gray>"));
        };
    }

    public static @NotNull Component futureMsg(@NotNull PlayerPunishment punishment) {
        String byUser = punishment.by == null ? "Administrator" : PDRegistry.getInstance().get(punishment.by).getUsername();

        return switch(punishment.type) {
            case MUTE -> MiniMessage.miniMessage().deserialize("<red>You are still muted for <yellow>" + punishment.reason + "</yellow> until <yellow>" + TimeUtil.date(punishment.expiration) + "</yellow>!</red>");
            case KICK -> MiniMessage.miniMessage().deserialize("<red>You have recently been removed from the server!</red>")
                    .appendNewline()
                    .appendNewline().append(MiniMessage.miniMessage().deserialize("<green>Please wait a bit before trying again!</green>"));
            case BLACKLIST -> punishment.isPermanent() ?
                    MiniMessage.miniMessage().deserialize("<red>⚠ You have been permanently blacklisted from the server!</red>")
                            .appendNewline()
                            .appendNewline().append(MiniMessage.miniMessage().deserialize("<red>On " + "<yellow>⏳ " + TimeUtil.date(punishment.on) + "<red> by</red> " + byUser + "</yellow>"))
                            .appendNewline()
                            .appendNewline().append(MiniMessage.miniMessage().deserialize("<yellow>" + punishment.reason + "</yellow>"))
                            .appendNewline()
                            .appendNewline().append(MiniMessage.miniMessage().deserialize("<blue>ℹ It is advised that you read and follow</blue>"))
                            .appendNewline().append(MiniMessage.miniMessage().deserialize("<blue>our server rules in the future.</blue>"))
                            .appendNewline()
                            .appendNewline().append(MiniMessage.miniMessage().deserialize("<gray>Contact an administrator if you believe this to be a mistake</gray>"))
                    :
                    MiniMessage.miniMessage().deserialize("<red>⚠ You have been blacklisted from the server!</red>")
                            .appendNewline()
                            .appendNewline().append(MiniMessage.miniMessage().deserialize("<red>On " + "<yellow>⏳ " + TimeUtil.date(punishment.on) + "<red> by</red> " + byUser + "</yellow>"))
                            .appendNewline().append(MiniMessage.miniMessage().deserialize("<red><yellow>\uD83D\uDD14 " + TimeUtil.remaining(punishment.expiration - System.currentTimeMillis(), false) + "</yellow> remaining"))
                            .appendNewline()
                            .appendNewline().append(MiniMessage.miniMessage().deserialize("<yellow>" + punishment.reason + "</yellow>"))
                            .appendNewline()
                            .appendNewline().append(MiniMessage.miniMessage().deserialize("<blue>ℹ It is advised that you read and follow</blue>"))
                            .appendNewline().append(MiniMessage.miniMessage().deserialize("<blue>our server rules in the future.</blue>"))
                            .appendNewline()
                            .appendNewline().append(MiniMessage.miniMessage().deserialize("<gray>Contact an administrator if you believe this to be a mistake</gray>"));
        };
    }

    public enum PunishmentType {
        MUTE,
        KICK,
        BLACKLIST,
    }
}