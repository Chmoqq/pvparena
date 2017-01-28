package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.loadables.ArenaModule;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * <pre>PVP Arena UNINSTALL Command class</pre>
 * <p/>
 * A command to uninstall modules
 *
 * @author slipcor
 * @version v0.10.0
 */

public class PAA_Uninstall extends AbstractGlobalCommand {

    public PAA_Uninstall() {
        super(new String[]{"pvparena.cmds.uninstall"});
    }

    @Override
    public void commit(final CommandSender sender, final String[] args) {
        if (!hasPerms(sender)) {
            return;
        }

        if (!argCountValid(sender, args,
                new Integer[]{0, 1})) {
            return;
        }

        if (!PVPArena.instance.getConfig().getBoolean("update.modules", true)) {
            Arena.pmsg(sender, ChatColor.DARK_RED+Language.parse(MSG.ERROR_MODULE_UPDATE));
            return;
        }

        // pa install
        // pa install ctf

        final YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(PVPArena.instance.getDataFolder().getPath() + "/install.yml");
        } catch (final Exception e) {
        }

        if (args.length == 0) {
            listVersions(sender, config, null);
            return;
        }

        if (config.get(args[0]) != null) {
            listVersions(sender, config, args[1]);
            return;
        }

        final String name = args[0].toLowerCase();
        final ArenaGoal goal = PVPArena.instance.getAgm().getGoalByName(name);
        if (goal != null) {
            if (remove("pa_g_" + goal.getName().toLowerCase() + ".jar")) {
                PVPArena.instance.getAgm().reload();
                Arena.pmsg(sender, Language.parse(MSG.UNINSTALL_DONE, goal.getName()));
                return;
            }
            Arena.pmsg(sender, Language.parse(MSG.ERROR_UNINSTALL, goal.getName()));
            FileConfiguration cfg = PVPArena.instance.getConfig();
            List<String> toDelete = cfg.getStringList("todelete");
            if (toDelete == null){
                toDelete = new ArrayList<>();
            }
            toDelete.add("pa_g_" + goal.getName().toLowerCase() + ".jar");
            cfg.set("todelete", toDelete);
            PVPArena.instance.saveConfig();
            Arena.pmsg(sender, Language.parse(MSG.ERROR_UNINSTALL2));
            return;
        }
        final ArenaModule mod = PVPArena.instance.getAmm().getModByName(name);
        if (mod != null) {
            if (remove("pa_m_" + mod.getName().toLowerCase() + ".jar")) {
                PVPArena.instance.getAmm().reload();
                Arena.pmsg(sender, Language.parse(MSG.UNINSTALL_DONE, mod.getName()));
                return;
            }
            Arena.pmsg(sender, Language.parse(MSG.ERROR_UNINSTALL, mod.getName()));
            FileConfiguration cfg = PVPArena.instance.getConfig();
            List<String> toDelete = cfg.getStringList("todelete");
            if (toDelete == null){
                toDelete = new ArrayList<>();
            }
            toDelete.add("pa_m_" + goal.getName().toLowerCase() + ".jar");
            cfg.set("todelete", toDelete);
            PVPArena.instance.saveConfig();
            Arena.pmsg(sender, Language.parse(MSG.ERROR_UNINSTALL2));
        }
    }

    private void listVersions(final CommandSender sender, final YamlConfiguration cfg,
                              final String sub) {
        Arena.pmsg(sender, "--- PVP Arena Version Update information ---");
        Arena.pmsg(sender, "[" + ChatColor.COLOR_CHAR + "7uninstalled" + ChatColor.COLOR_CHAR + "r | " + ChatColor.COLOR_CHAR + "einstalled" + ChatColor.COLOR_CHAR + "r]");
        Arena.pmsg(sender, "[" + ChatColor.COLOR_CHAR + "coutdated" + ChatColor.COLOR_CHAR + "r | " + ChatColor.COLOR_CHAR + "alatest version" + ChatColor.COLOR_CHAR + "r]");
        if (sub == null || "arenas".equalsIgnoreCase(sub)) {
            Arena.pmsg(sender, ChatColor.COLOR_CHAR + "c--- Arena Goals ----> /goals");
            final Set<String> entries = cfg.getConfigurationSection("goals").getKeys(
                    false);
            for (final String key : entries) {
                final String value = cfg.getString("goals." + key);
                final ArenaGoal goal = PVPArena.instance.getAgm().getGoalByName(key);
                final boolean installed = goal != null;
                String version = null;
                if (installed) {
                    version = goal.version();
                }
                Arena.pmsg(sender, (installed ? ChatColor.COLOR_CHAR + "e" : ChatColor.COLOR_CHAR + "7")
                        + key
                        + ChatColor.COLOR_CHAR + "r - "
                        + (installed ? value.equals(version) ? ChatColor.COLOR_CHAR + "a" : ChatColor.COLOR_CHAR + "c"
                        : "") + value);
            }
        }
        if (sub == null || "mods".equalsIgnoreCase(sub)) {
            Arena.pmsg(sender, ChatColor.COLOR_CHAR + "a--- Arena Mods ----> /mods");
            final Set<String> entries = cfg.getConfigurationSection("mods").getKeys(
                    false);
            for (final String key : entries) {
                final String value = cfg.getString("mods." + key);
                final ArenaModule mod = PVPArena.instance.getAmm().getModByName(key);
                final boolean installed = mod != null;
                String version = null;
                if (installed) {
                    version = mod.version();
                }
                Arena.pmsg(sender, (installed ? ChatColor.COLOR_CHAR + "e" : ChatColor.COLOR_CHAR + "7")
                        + key
                        + ChatColor.COLOR_CHAR + "r - "
                        + (installed ? value.equals(version) ? ChatColor.COLOR_CHAR + "a" : ChatColor.COLOR_CHAR + "c"
                        : "") + value);
            }

        }
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    public static boolean remove(final String file) {
        String folder = null;
        if (file.startsWith("pa_g")) {
            folder = "/goals/";
        } else if (file.startsWith("pa_m")) {
            folder = "/mods/";
        }
        if (folder == null) {
            PVPArena.instance.getLogger().severe("unable to fetch file: " + file);
            return false;
        }
        final File destination = new File(PVPArena.instance.getDataFolder().getPath()
                + folder);

        final File destFile = new File(destination, file);

        boolean exists = destFile.exists();
        boolean deleted = false;
        if (exists) {
            deleted = destFile.delete();
            if (!deleted) {
                PVPArena.instance.getLogger().severe("could not delete file: " + file);
            }
        } else {
            PVPArena.instance.getLogger().warning("file does not exist: " + file);
        }

        return exists && deleted;
    }

    @Override
    public void displayHelp(final CommandSender sender) {
        Arena.pmsg(sender, Help.parse(HELP.UNINSTALL));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("uninstall");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!ui");
    }

    @Override
    public CommandTree<String> getSubs(final Arena nothing) {
        final CommandTree<String> result = new CommandTree<>(null);
        for (final String string : PVPArena.instance.getAgm().getAllGoalNames()) {
            result.define(new String[]{string});
        }
        for (final ArenaModule mod : PVPArena.instance.getAmm().getAllMods()) {
            result.define(new String[]{mod.getName()});
        }
        return result;
    }
}
