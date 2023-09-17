package net.unethicalite.scripts.runnermule;

import lombok.extern.slf4j.Slf4j;
import net.unethicalite.api.utils.MessageUtils;

import java.awt.*;

@Slf4j
public class Log {
    public static void log(String toLog) {
        log.info(toLog);
        MessageUtils.addMessage(toLog, Color.BLUE);
    }
    public static void updateTaskInfo(String toLog) {
        log.info(toLog);
        MessageUtils.addMessage(toLog, Color.BLUE);
        API.taskInfo = toLog;
    }
    private static final String outputLogTag = "[LAUNCHERTAG]";
    public static void sendKeyValuePairToOutputStream(String key, String value) {
        String toJSON = formatToJson(key, value);
        log.info(outputLogTag+toJSON);
    }
    public static String formatToJson(String key, String value) {
        return "{" + "\"" + key + "\"" + ":" + "\"" + value + "\"" + "}";
    }
}
