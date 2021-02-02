package net.kunmc.lab.operationsyncplugin;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandListener implements CommandExecutor, TabCompleter {

    private Operationsyncplugin operationsyncplugin;

    public CommandListener(Operationsyncplugin plugin) {
        this.operationsyncplugin = plugin;
    }

    public void register() {
        Bukkit.getPluginCommand("sync").setExecutor(this);
        Bukkit.getPluginCommand("sync").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender,Command command,String label,String[] args){
        if (args.length < 1) {
            sender.sendMessage(new StringBuilder().append(ChatColor.RED).append("引数が足りません！").toString());
            return true;
        }

        switch (args[0]) {
            case "king":
                if (args.length < 2) {
                    sender.sendMessage(new StringBuilder().append(ChatColor.RED).append("プレイヤーを指定してください！").toString());
                    return true;
                }
                Player player = Bukkit.getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage(new StringBuilder().append(ChatColor.RED).append("プレイヤーが見つかりません！").toString());
                    return true;
                }
                operationsyncplugin.setKingID(player.getName());
                sender.sendMessage(new StringBuilder().append(ChatColor.GREEN).append("同期元のプレイヤーを ").append(player.getName()).append(" に設定しました").toString());
                break;
            case "activate":
                if (operationsyncplugin.isActive()) {
                    sender.sendMessage(new StringBuilder().append(ChatColor.RED).append("既に起動しています！").toString());
                } else {
                    operationsyncplugin.setActive(true);
                    sender.sendMessage(new StringBuilder().append(ChatColor.GREEN).append("操作同期 開始").toString());
                    if (operationsyncplugin.getKing() == null) {
                        sender.sendMessage(new StringBuilder().append(ChatColor.RED).append("[注意] 同期元のプレイヤーがセットされていません！").toString());
                    }
                }
                break;
            case "inactivate":
                if (!operationsyncplugin.isActive()) {
                    sender.sendMessage(new StringBuilder().append(ChatColor.RED).append("まだ起動していません！").toString());
                } else {
                    operationsyncplugin.setActive(false);
                    sender.sendMessage(new StringBuilder().append(ChatColor.GREEN).append("操作同期 停止").toString());
                }
                break;
            case "status":
                sender.sendMessage(new StringBuilder()
                        .append(String.format("状態: %s", operationsyncplugin.isActive() ? ChatColor.GREEN + "起動中" : ChatColor.RED + "停止中"))
                        .append("\n")
                        .append(String.format(ChatColor.RESET + "同期元プレイヤー: %s", operationsyncplugin.getKing() == null ? ChatColor.RED + "未設定" : operationsyncplugin.getKing().getName()))
                        .toString());
                break;
            default:
                sender.sendMessage(new StringBuilder().append(ChatColor.RED).append("無効な引数です！").toString());
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {

        List<String> suggestions = null;

        switch (args.length) {
            case 1:
                suggestions = new ArrayList<>(Arrays.asList("activate", "inactivate", "king", "status"));
                break;
            default:
                break;
        }

        return suggestions;
    }

}
