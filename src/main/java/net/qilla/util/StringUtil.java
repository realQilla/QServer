package net.qilla.util;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class StringUtil {

    private StringUtil() {
    }

    /**
     * Pluralizes a string to have to be grammatically correct
     *
     * @param baseString The base string to check
     * @param amount     The integer to check
     *
     * @return Returns a formatted string with proper pluralization
     */

    public static @NotNull String pluralize(@NotNull String baseString, int amount) {
        return amount == 1 ? baseString : baseString.endsWith("s") ? baseString.concat("'") : baseString.concat("'s");
    }

    /**
     * Returns a string with the first letter capitalized and each other in lowercase.
     *
     * @param string The string to modify
     *
     * @return Returns a new capitalized string
     */

    public static @NotNull String toName(@NotNull String string) {
        return Arrays.stream(string.split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    /**
     * Formats a list into an ordered string list with each first letter capitalized and separated by a specified string
     *
     * @param list      The list to format
     * @param delimiter The format in which each string will be connected
     *
     * @return Returns a new capitalized string list
     */

    public static @NotNull String toNameList(@NotNull List<?> list, @NotNull String delimiter) {
        return list.stream()
                .map(Object::toString)
                .map(StringUtil::toName)
                .collect(Collectors.joining(delimiter));
    }

    /**
     * Formats a list into an ordered string list with each first letter capitalized and separated by a specified string
     *
     * @param list      The list to format
     * @param delimiter The format in which each string will be connected
     *
     * @return Returns a new capitalized string list
     */

    public static @NotNull String toLimitedNameList(@NotNull List<?> list, @NotNull String delimiter, int limit) {
        if(list.size() > limit) list = list.subList(0, limit);
        return list.stream()
                .map(Object::toString)
                .map(StringUtil::toName)
                .collect(Collectors.joining(delimiter));
    }

    /**
     * Create a new UUID with a fixed-length.
     *
     * @param length The length in characters to create an identifier with
     *
     * @return Returns a new shortened UUID.
     */

    public static @NotNull String uniqueIdentifier(int length) {
        length = Math.max(0, length);
        StringBuilder builder = new StringBuilder();

        while(builder.length() < length) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            builder.append(uuid);
        }

        return builder.substring(0, length);
    }
}