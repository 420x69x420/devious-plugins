package net.unethicalite.scripts.api.launcher420;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Log {
    private static final String outputLogTag = "[LAUNCHERTAG]";
    public static void sendKeyValuePairToOutputStream(String key, String value) {
        String toJSON = formatToJson(key, value);
        log.info(outputLogTag+toJSON);
    }
    public static String formatToJson(String key, String value) {
        return "{" + "\"" + key + "\"" + ":" + "\"" + value + "\"" + "}";
    }
}
