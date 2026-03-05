package com.nexora.announcer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NexoraAnnouncer extends JavaPlugin {
    
    private List<String> announcements = new ArrayList<>();
    private Random random = new Random();
    private int currentIndex = 0;
    
    @Override
    public void onEnable() {
        getLogger().info("Nexora Announcer Plugin aktif!");
        
        // Duyuruları yükle
        loadAnnouncements();
        
        // Her 5 dakikada bir duyuru yap (6000 tick = 5 dakika)
        Bukkit.getScheduler().runTaskTimer(this, this::sendAnnouncement, 6000L, 6000L);
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Nexora Announcer Plugin kapatıldı!");
    }
    
    private void loadAnnouncements() {
        announcements.add(ChatColor.GOLD + "💰 " + ChatColor.YELLOW + "/daily " + 
            ChatColor.GRAY + "komutuyla günlük ödülünü alabilirsin!");
        
        announcements.add(ChatColor.GOLD + "🛒 " + ChatColor.YELLOW + "/shop " + 
            ChatColor.GRAY + "komutuyla mağazayı açabilirsin!");
        
        announcements.add(ChatColor.GOLD + "📋 " + ChatColor.YELLOW + "/quests " + 
            ChatColor.GRAY + "komutuyla günlük görevlerini görebilirsin!");
        
        announcements.add(ChatColor.GOLD + "💵 " + ChatColor.YELLOW + "/balance " + 
            ChatColor.GRAY + "komutuyla bakiyeni kontrol edebilirsin!");
        
        announcements.add(ChatColor.GOLD + "👑 " + ChatColor.YELLOW + "/baltop " + 
            ChatColor.GRAY + "komutuyla en zenginleri görebilirsin!");
        
        announcements.add(ChatColor.GOLD + "💸 " + ChatColor.YELLOW + "/pay <oyuncu> <miktar> " + 
            ChatColor.GRAY + "komutuyla başka oyunculara para gönderebilirsin!");
        
        announcements.add(ChatColor.GOLD + "🏆 " + ChatColor.YELLOW + "/achievements " + 
            ChatColor.GRAY + "komutuyla başarılarını görebilirsin!");
        
        announcements.add(ChatColor.GOLD + "🎯 " + ChatColor.GRAY + "Günlük görevleri tamamlayarak " + 
            ChatColor.GREEN + "bonus ödül " + ChatColor.GRAY + "kazanabilirsin!");
        
        announcements.add(ChatColor.GOLD + "⚒ " + ChatColor.GRAY + "Elindeki eşyayı satmak için " + 
            ChatColor.YELLOW + "/sell " + ChatColor.GRAY + "veya " + 
            ChatColor.YELLOW + "/sellall " + ChatColor.GRAY + "kullan!");
        
        announcements.add(ChatColor.GOLD + "🎮 " + ChatColor.GRAY + "Sunucu IP: " + 
            ChatColor.AQUA + "194.105.5.37");
        
        announcements.add(ChatColor.GOLD + "✨ " + ChatColor.GRAY + "Oyun içinde sohbet ederken " + 
            ChatColor.YELLOW + "&c, &a, &b " + ChatColor.GRAY + "gibi renk kodları kullanabilirsin!");
        
        announcements.add(ChatColor.GOLD + "🌟 " + ChatColor.GRAY + "Daha fazla oynayarak " + 
            ChatColor.YELLOW + "rütbe " + ChatColor.GRAY + "atlayabilirsin!");
    }
    
    private void sendAnnouncement() {
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            return; // Kimse yoksa duyuru yapma
        }
        
        // Sırayla duyuru yap
        String announcement = announcements.get(currentIndex);
        currentIndex = (currentIndex + 1) % announcements.size();
        
        // Tüm oyunculara gönder
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "  NEXORA " + 
            ChatColor.DARK_GRAY + "» " + announcement);
        Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        Bukkit.broadcastMessage("");
    }
}
