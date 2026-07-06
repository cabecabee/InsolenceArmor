package me.cabeca.insolencearmor.util;

import org.bukkit.command.CommandSender;

public abstract class PermissionUtil {
    public static boolean canUse(CommandSender sender, String permission){
        return sender.hasPermission(permission) || sender.isOp();
    }
}
