package com.nexora.ranks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class NexoraRanks extends JavaPlugin implements Listener {
    
    @Override
    public void onEnable() {
        getLogger().info("Nexora Ranks Plugin aktif!");
        Bukkit.getPluginManager().registerEvents(this, this);
        
        // Her 5 dakikada bir rütbeleri güncelle
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateRank(player);
            }
        }, 0L, 6000L); // 6000 tick = 5 dakika
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Nexora Ranks Plugin kapatıldı!");
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        updateRank(player);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("rank")) {
            Player target;
            
            if (args.length == 0) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Konsol için oyuncu adı belirtmelisin!");
                    return true;
                }
                target = (Player) sender;
            } else {
                target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "✗ Oyuncu bulunamadı!");
                    return true;
                }
            }
            
            String rank = getRankName(target);
            String rankColor = getRankColor(target);
            int playTime = target.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 / 3600; // Saat cinsinden
            
            sender.sendMessage("");
            sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "✦ RÜTBE BİLGİSİ ✦");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.GRAY + "  Oyuncu: " + ChatColor.WHITE + target.getName());
            sender.sendMessage(ChatColor.GRAY + "  Rütbe: " + rankColor + rank);
            sender.sendMessage(ChatColor.GRAY + "  Oyun Süresi: " + ChatColor.AQUA + playTime + " saat");
            sender.sendMessage(ChatColor.GRAY + "  Sonraki Rütbe: " + getNextRank(target));
            sender.sendMessage("");
            
            return true;
        }
        
        if (command.getName().equalsIgnoreCase("setrank")) {
            if (!sender.hasPermission("nexoraranks.setrank")) {
                sender.sendMessage(ChatColor.RED + "✗ Bu komutu kullanma yetkin yok!");
                return true;
            }
            
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Kullanım: /setrank <oyuncu> <rütbe>");
                return true;
            }
            
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "✗ Oyuncu bulunamadı!");
                return true;
            }
            
            String rank = args[1].toLowerCase();
            
            // Eski yetkiyi kaldır
            removeAllRanks(target);
            
            // Yeni yetki ver
            switch (rank) {
                case "owner":
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + target.getName() + " permission set nexora.owner true");
                    break;
                case "admin":
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + target.getName() + " permission set nexora.admin true");
                    break;
                case "mod":
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + target.getName() + " permission set nexora.mod true");
                    break;
                case "vip":
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + target.getName() + " permission set nexora.vip true");
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "✗ Geçersiz rütbe! (owner, admin, mod, vip)");
                    return true;
            }
            
            sender.sendMessage(ChatColor.GREEN + "✓ " + target.getName() + " oyuncusuna " + rank + " rütbesi verildi!");
            target.sendMessage(ChatColor.GREEN + "✓ Rütben " + rank + " olarak güncellendi!");
            
            return true;
        }
        
        return false;
    }
    
    private void updateRank(Player player) {
        // Manuel rütbesi varsa otomatik rütbe verme
        if (player.hasPermission("nexora.owner") || 
            player.hasPermission("nexora.admin") || 
            player.hasPermission("nexora.mod") || 
            player.hasPermission("nexora.vip")) {
            return;
        }
        
        int playTime = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 / 3600; // Saat
        
        // Otomatik rütbe sistemi (oyun süresine göre)
        if (playTime >= 100) {
            // LEGEND (100+ saat)
            if (!player.hasPermission("nexora.legend")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set nexora.legend true");
                player.sendMessage(ChatColor.GOLD + "✦ Tebrikler! " + ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "LEGEND" + ChatColor.GOLD + " rütbesine yükseldin!");
            }
        } else if (playTime >= 50) {
            // MASTER (50+ saat)
            if (!player.hasPermission("nexora.master")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set nexora.master true");
                player.sendMessage(ChatColor.GOLD + "✦ Tebrikler! " + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "MASTER" + ChatColor.GOLD + " rütbesine yükseldin!");
            }
        } else if (playTime >= 20) {
            // CHAMPION (20+ saat)
            if (!player.hasPermission("nexora.champion")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set nexora.champion true");
                player.sendMessage(ChatColor.GOLD + "✦ Tebrikler! " + ChatColor.BLUE + "" + ChatColor.BOLD + "CHAMPION" + ChatColor.GOLD + " rütbesine yükseldin!");
            }
        } else if (playTime >= 5) {
            // WARRIOR (5+ saat)
            if (!player.hasPermission("nexora.warrior")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set nexora.warrior true");
                player.sendMessage(ChatColor.GOLD + "✦ Tebrikler! " + ChatColor.GREEN + "" + ChatColor.BOLD + "WARRIOR" + ChatColor.GOLD + " rütbesine yükseldin!");
            }
        }
    }
    
    private String getRankName(Player player) {
        if (player.hasPermission("nexora.owner")) return "OWNER";
        if (player.hasPermission("nexora.admin")) return "ADMIN";
        if (player.hasPermission("nexora.mod")) return "MODERATOR";
        if (player.hasPermission("nexora.vip")) return "VIP";
        if (player.hasPermission("nexora.legend")) return "LEGEND";
        if (player.hasPermission("nexora.master")) return "MASTER";
        if (player.hasPermission("nexora.champion")) return "CHAMPION";
        if (player.hasPermission("nexora.warrior")) return "WARRIOR";
        return "ROOKIE";
    }
    
    private String getRankColor(Player player) {
        if (player.hasPermission("nexora.owner")) return ChatColor.DARK_RED + "" + ChatColor.BOLD;
        if (player.hasPermission("nexora.admin")) return ChatColor.RED + "" + ChatColor.BOLD;
        if (player.hasPermission("nexora.mod")) return ChatColor.GOLD + "" + ChatColor.BOLD;
        if (player.hasPermission("nexora.vip")) return ChatColor.AQUA + "" + ChatColor.BOLD;
        if (player.hasPermission("nexora.legend")) return ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD;
        if (player.hasPermission("nexora.master")) return ChatColor.DARK_PURPLE + "" + ChatColor.BOLD;
        if (player.hasPermission("nexora.expert")) return ChatColor.BLUE + "" + ChatColor.BOLD;
        if (player.hasPermission("nexora.active")) return ChatColor.GREEN + "" + ChatColor.BOLD;
        return ChatColor.GRAY + "";
    }
    
    private String getNextRank(Player player) {
        int playTime = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 / 3600;
        
        if (player.hasPermission("nexora.owner") || player.hasPermission("nexora.admin") || 
            player.hasPermission("nexora.mod") || player.hasPermission("nexora.vip")) {
            return ChatColor.GOLD + "Manuel Rütbe";
        }
        
        if (playTime >= 100) return ChatColor.GOLD + "Maksimum Rütbe!";
        if (playTime >= 50) return ChatColor.LIGHT_PURPLE + "LEGEND " + ChatColor.GRAY + "(100 saat)";
        if (playTime >= 20) return ChatColor.DARK_PURPLE + "MASTER " + ChatColor.GRAY + "(50 saat)";
        if (playTime >= 5) return ChatColor.BLUE + "CHAMPION " + ChatColor.GRAY + "(20 saat)";
        return ChatColor.GREEN + "WARRIOR " + ChatColor.GRAY + "(5 saat)";
    }
    
    private void removeAllRanks(Player player) {
        String[] ranks = {"owner", "admin", "mod", "vip", "legend", "master", "champion", "warrior"};
        for (String rank : ranks) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission unset nexora." + rank);
        }
    }
}
