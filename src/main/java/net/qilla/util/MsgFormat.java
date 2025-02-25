package net.qilla.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.data.PDRegistry;
import net.qilla.data.PlayerPunishment;
import org.jetbrains.annotations.NotNull;

public final class MsgFormat {

    private static PDRegistry pdRegistry = PDRegistry.getInstance();

    public static @NotNull Component registrationError() {
        return MiniMessage.miniMessage().deserialize("<red>There is a problem with your player profile!!")
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<blue>ℹ It is recommended that you contact an"))
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<blue>administrator for support regrading this."));
    }

    public static @NotNull Component whitelisted() {
        return MiniMessage.miniMessage().deserialize("<red>The server is currently closed from the public!")
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<yellow>Please come back later!"));
    }

    public static @NotNull Component overwriteConnection() {
        return MiniMessage.miniMessage().deserialize("<red>You logged in from another location!")
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<blue>ℹ If this was not done by you or someone you trust,")
                        .appendNewline().append(MiniMessage.miniMessage().deserialize("<blue>you may need to change your password.")));
    }

    public static @NotNull Component initialMute(@NotNull PlayerPunishment punishment) {
        String byUser = punishment.getBy() == null ? "Administrator" : pdRegistry.get(punishment.getBy()).getUsername();

        return MiniMessage.miniMessage().deserialize("<red>You have been muted by <yellow>" + byUser +
                "</yellow> for <yellow>" + punishment.getReason() +
                "</yellow>.\n<yellow>" + TimeUtil.remaining(punishment.getExpiration() - System.currentTimeMillis(), false) + "</yellow> remaining!</red>");
    }

    public static @NotNull Component initialMutePerm(@NotNull PlayerPunishment punishment) {
        String byUser = punishment.getBy() == null ? "Administrator" : pdRegistry.get(punishment.getBy()).getUsername();

        return MiniMessage.miniMessage().deserialize("<red>You have been permanently muted by <yellow>" + byUser +
                "</yellow> for <yellow>" + punishment.getReason());
    }

    public static @NotNull Component initialKick(@NotNull PlayerPunishment punishment) {
        String byUser = punishment.getBy() == null ? "Administrator" : pdRegistry.get(punishment.getBy()).getUsername();

        return MiniMessage.miniMessage().deserialize("<red>You have been removed from the server!</red>")
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<red>On " + "<yellow>⏳ " + TimeUtil.date(punishment.getOn()) + "<red> by</red> " + byUser + "</yellow>"))
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<yellow>" + punishment.getReason() + "</yellow>"))
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<green>⏳ You may rejoin shortly!</green>"));
    }

    public static @NotNull Component initialBlacklist(@NotNull PlayerPunishment punishment) {
        String byUser = punishment.getBy() == null ? "Administrator" : pdRegistry.get(punishment.getBy()).getUsername();

        return MiniMessage.miniMessage().deserialize("<red>⚠ You have been blacklisted from the server!</red>")
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<red>On " + "<yellow>⏳ " + TimeUtil.date(punishment.getOn()) + "<red> by</red> " + byUser + "</yellow>"))
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<red><yellow>\uD83D\uDD14 " + TimeUtil.remaining(punishment.getExpiration() - System.currentTimeMillis(), false) + "</yellow> remaining"))
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<yellow>" + punishment.getReason() + "</yellow>"))
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<blue>ℹ It is advised that you read and follow</blue>"))
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<blue>our server rules in the future.</blue>"))
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<gray>Contact an administrator if you believe this to be a mistake</gray>"));
    }

    public static @NotNull Component initialBlacklistPerm(@NotNull PlayerPunishment punishment) {
        String byUser = punishment.getBy() == null ? "Administrator" : pdRegistry.get(punishment.getBy()).getUsername();

        return MiniMessage.miniMessage().deserialize("<red>⚠ You have been permanently blacklisted from the server!</red>")
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<red>On " + "<yellow>⏳ " + TimeUtil.date(punishment.getOn()) + "<red> by</red> " + byUser + "</yellow>"))
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<yellow>" + punishment.getReason() + "</yellow>"))
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<blue>ℹ It is advised that you read and follow</blue>"))
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<blue>our server rules in the future.</blue>"))
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<gray>Contact an administrator if you believe this to be a mistake</gray>"));
    }

    public static @NotNull Component mutePerm(@NotNull PlayerPunishment punishment) {
        return MiniMessage.miniMessage().deserialize("<red>You are permanently muted for <yellow>" + punishment.getReason() + "</yellow>.");
    }

    public static @NotNull Component mute(@NotNull PlayerPunishment punishment) {
        return MiniMessage.miniMessage().deserialize("<red>You are still muted for <yellow>" + punishment.getReason() + "</yellow>.\n<yellow>" + TimeUtil.remaining(punishment.getExpiration() - System.currentTimeMillis(), false) + "</yellow> remaining!</red>");
    }

    public static @NotNull Component kick() {
        return MiniMessage.miniMessage().deserialize("<red>You have recently been removed from the server!</red>")
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<green>Please wait a bit before trying again!</green>"));
    }

    public static @NotNull Component blacklistPerm(PlayerPunishment punishment) {
        String byUser = punishment.getBy() == null ? "Administrator" : pdRegistry.get(punishment.getBy()).getUsername();

        return MiniMessage.miniMessage().deserialize("<red>⚠ You have been permanently blacklisted from the server!</red>")
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<red>On " + "<yellow>⏳ " + TimeUtil.date(punishment.getOn()) + "<red> by</red> " + byUser + "</yellow>"))
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<yellow>" + punishment.getReason() + "</yellow>"))
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<blue>ℹ It is advised that you read and follow</blue>"))
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<blue>our server rules in the future.</blue>"))
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<gray>Contact an administrator if you believe this to be a mistake</gray>"));
    }

    public static @NotNull Component blacklist(PlayerPunishment punishment) {
        String byUser = punishment.getBy() == null ? "Administrator" : pdRegistry.get(punishment.getBy()).getUsername();

        return MiniMessage.miniMessage().deserialize("<red>⚠ You have been blacklisted from the server!</red>")
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<red>On " + "<yellow>⏳ " + TimeUtil.date(punishment.getOn()) + "<red> by</red> " + byUser + "</yellow>"))
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<red><yellow>\uD83D\uDD14 " + TimeUtil.remaining(punishment.getExpiration() - System.currentTimeMillis(), false) + "</yellow> remaining"))
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<yellow>" + punishment.getReason() + "</yellow>"))
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<blue>ℹ It is advised that you read and follow</blue>"))
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<blue>our server rules in the future.</blue>"))
                .appendNewline()
                .appendNewline().append(MiniMessage.miniMessage().deserialize("<gray>Contact an administrator if you believe this to be a mistake</gray>"));
    }
}