package net.unethicalite.scripts.runecrafter;

import net.runelite.api.util.Text;
import net.unethicalite.scripts.runecrafter.leaves.Crafting;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CrafterFileOperations {
    private static final Path runnersFilePath = Paths.get(System.getProperty("user.home")+"\\.openosrs\\plugins\\runners.txt");
    private static final Path craftersFilePath = Paths.get(System.getProperty("user.home")+"\\.openosrs\\plugins\\crafters.txt");
    private static final Path runnerSettingsPath = Paths.get(System.getProperty("user.home")+"\\.openosrs\\plugins\\runner_settings.txt");
    private static final Path lockPath = Paths.get(System.getProperty("user.home")+"\\.openosrs\\plugins\\runnersLockFile.txt");
    public static boolean writeEntry(String entry) {
        String cleaned = Text.toJagexName(entry);
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock lock = channel.tryLock()) {
            if (lock == null) {
                return false;
            }
            // Once locked, append entry to file
            Files.write(craftersFilePath, Arrays.asList(cleaned), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean removeEntry(String entry) {
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock lock = channel.tryLock()) {
            if (lock == null) {
                return false;
            }
            // Once locked, read all lines, filter out the entry to be removed, and write back
            List<String> allLines = Files.readAllLines(craftersFilePath);
            List<String> updatedLines = allLines.stream().filter(line -> !line.equals(entry)).collect(Collectors.toList());
            Files.write(craftersFilePath, updatedLines);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<String> readAllRunners() {
        try {
            return Files.readAllLines(runnersFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
    public static void updateAllSettings() {
        try {
            for (String i : Files.readAllLines(runnerSettingsPath)) {
                if (i.contains("=")) {
                    String[] stuff = i.split("=");
                    if (stuff[0].contains("craftworld")) {
                        Crafting.craftWorld = Integer.parseInt(stuff[1]);
                    }
                    if (stuff[0].contains("altar")) {
                        Crafting.altar = (stuff[1].contains("air") ? Altar.AIR : Altar.BODY);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
