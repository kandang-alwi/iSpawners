package dev.jay.kacore.commands;

import dev.jay.kacore.KaCore;
import dev.jay.kacore.hook.impl.RedisBungeeHook;
import dev.jay.kacore.objects.Config;
import dev.jay.kacore.redis.entries.PlayerEntry;
import dev.jay.kacore.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

public class StaffListCMD extends Command {

    public StaffListCMD() {
        super("stafflist", "", "staff", "staffs");
    }

    @Override
    public void execute(CommandSender commandSender, String[] stringArray) {
        if (!commandSender.hasPermission("kacore.staff")) {
            commandSender.sendMessage(Config.PREFIX + Config.NO_PERMISSION);
            return;
        }
        KaCore kaCore = KaCore.getInstance();
        RedisBungeeHook redisBungeeHook = kaCore.getRedisBungeeHook();
        int staffCount = 0;
        ArrayList<String> staffList = new ArrayList<>();
        if (redisBungeeHook.isHooked()) {
            Set<PlayerEntry> staffMembers = redisBungeeHook.getStaffMembers();
            for (PlayerEntry playerEntry : staffMembers) {
                String message = "§5* " + (playerEntry.isHidden() ? "§7[HIDDEN] " : "") +
                        playerEntry.getRankPrefix() + playerEntry.getUsername() + " §eis on §a" +
                        playerEntry.getServerName().toUpperCase();
                staffList.add(message);
            }
            staffCount = staffMembers.size();
        } else {
            for (ProxiedPlayer proxiedPlayer : Utils.getPlayers()) {
                if (!proxiedPlayer.hasPermission("kacore.staff")) continue;
                String userPrefix = Utils.getUserPrefix(proxiedPlayer);
                String serverName = proxiedPlayer.getServer().getInfo().getName();
                String message = "§5* " + (kaCore.hiddenPlayers.contains(proxiedPlayer.getName()) ? "§7[HIDDEN] " : "") +
                        userPrefix + proxiedPlayer.getName() + " §eis on §a" + serverName.toUpperCase();
                staffList.add(message);
                staffCount++;
            }
        }

        // Mengurutkan daftar staf berdasarkan peringkat mereka
        staffList.sort(Comparator.comparingInt(StaffListCMD::getRankValue).reversed());



        commandSender.sendMessage("§dOnline Staff (" + staffCount + "):\n");
        commandSender.sendMessage(String.join("\n", staffList));
    }

    private static int getRankValue(String staff) {
        String rank = staff.substring(staff.indexOf("[") + 1, staff.indexOf("]"));
        switch (rank) {
            case "ᴄᴇᴏ": return 7;
            case "ᴍᴀɴᴀɢᴇʀ": return 6;
            case "ᴀᴅᴍɪɴ": return 5;
            case "ᴍᴏᴅ": return 4;
            case "ʜᴇʟᴘᴇʀ": return 3;
            case "ᴛᴇᴀᴍ": return 2;
            default: return 1;
        }
    }
}