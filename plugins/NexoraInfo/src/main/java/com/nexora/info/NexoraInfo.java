package com.nexora.info;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

public class NexoraInfo extends JavaPlugin implements Listener {
    
    @Override
    public void onEnable() {
        getLogger().info("Nexora Info Plugin aktif!");
        Bukkit.getPluginManager().registerEvents(this, this);
        
        // Her saniye scoreboard güncelle
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateScoreboard(player);
            }
        }, 0L, 20L); // 20 tick = 1 saniye
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Nexora Info Plugin kapatıldı!");
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Hoşgeldin mesajı
        player.sendTitle(
            ChatColor.AQUA + "✦ " + ChatColor.BOLD + "NEXORA" + ChatColor.AQUA + " ✦",
            ChatColor.GRAY + "Hoşgeldin, " + ChatColor.WHITE + player.getName() + ChatColor.GRAY + "!",
            10, 70, 20
        );
        
        // Chat mesajı
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_GRAY + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "        ✦ NEXORA MINECRAFT SUNUCUSU ✦");
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "  Sunucumuza hoşgeldin " + ChatColor.WHITE + player.getName() + ChatColor.GRAY + "!");
        player.sendMessage(ChatColor.GRAY + "  İyi oyunlar dileriz! " + ChatColor.GOLD + "❤");
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_GRAY + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("");
        
        // Scoreboard oluştur
        updateScoreboard(player);
    }
    
    private void updateScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        
        Objective objective = board.registerNewObjective("nexora", "dummy", 
            ChatColor.AQUA + "" + ChatColor.BOLD + "✦ NEXORA ✦");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        // Boşluk
        Score space1 = objective.getScore(ChatColor.DARK_GRAY + "");
        space1.setScore(10);
        
        // Oyuncu sayısı
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();
        Score players = objective.getScore(ChatColor.WHITE + "👥 " + ChatColor.AQUA + onlinePlayers + ChatColor.GRAY + "/" + ChatColor.AQUA + maxPlayers);
        players.setScore(9);
        
        // Dünya günü ve zaman
        long worldTime = player.getWorld().getFullTime();
        long day = (worldTime / 24000) + 1;
        long timeOfDay = player.getWorld().getTime();
        String timeString = getTimeString(timeOfDay);
        Score dayTime = objective.getScore(ChatColor.WHITE + "📅 " + ChatColor.AQUA + "Gün " + day + " " + timeString);
        dayTime.setScore(8);
        
        // Boşluk
        Score space2 = objective.getScore(ChatColor.DARK_GRAY + " ");
        space2.setScore(7);
        
        // Koordinatlar
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();
        Score coords = objective.getScore(ChatColor.WHITE + "📍 " + ChatColor.GRAY + x + ", " + y + ", " + z);
        coords.setScore(6);
        
        // Ping
        int ping = player.getPing();
        String pingColor = ping < 50 ? ChatColor.GREEN + "" : ping < 100 ? ChatColor.YELLOW + "" : ChatColor.RED + "";
        Score pingScore = objective.getScore(ChatColor.WHITE + "📶 " + pingColor + ping + "ms");
        pingScore.setScore(5);
        
        // Boşluk
        Score space3 = objective.getScore(ChatColor.DARK_GRAY + "  ");
        space3.setScore(4);
        
        // IP
        Score ip = objective.getScore(ChatColor.GRAY + "194.105.5.37");
        ip.setScore(3);
        
        player.setScoreboard(board);
    }
    
    private String getTimeString(long time) {
        if (time >= 0 && time < 6000) {
            return ChatColor.YELLOW + "☀ Sabah";
        } else if (time >= 6000 && time < 12000) {
            return ChatColor.GOLD + "☀ Öğle";
        } else if (time >= 12000 && time < 13800) {
            return ChatColor.GOLD + "🌅 Akşam";
        } else if (time >= 13800 && time < 22200) {
            return ChatColor.DARK_BLUE + "🌙 Gece";
        } else {
            return ChatColor.DARK_GRAY + "🌙 Gece Yarısı";
        }
    }
}
