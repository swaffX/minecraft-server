package com.nexora.chat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NexoraChat extends JavaPlugin implements Listener {
    
    private boolean chatMuted = false;
    private Map<UUID, Long> lastMessageTime = new HashMap<>();
    private static final long COOLDOWN_MS = 1000; // 1 saniye cooldown
    
    @Override
    public void onEnable() {
        getLogger().info("Nexora Chat Plugin aktif!");
        Bukkit.getPluginManager().registerEvents(this, this);
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Nexora Chat Plugin kapatıldı!");
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // Chat muted kontrolü
        if (chatMuted && !player.hasPermission("nexorachat.bypass")) {
            player.sendMessage(ChatColor.RED + "✗ Chat şu anda kapalı!");
            event.setCancelled(true);
            return;
        }
        
        // Anti-spam kontrolü
        if (!player.hasPermission("nexorachat.bypass")) {
            long currentTime = System.currentTimeMillis();
            if (lastMessageTime.containsKey(player.getUniqueId())) {
                long lastTime = lastMessageTime.get(player.getUniqueId());
                if (currentTime - lastTime < COOLDOWN_MS) {
                    player.sendMessage(ChatColor.RED + "✗ Çok hızlı yazıyorsun! Biraz bekle.");
                    event.setCancelled(true);
                    return;
                }
            }
            lastMessageTime.put(player.getUniqueId(), currentTime);
        }
        
        // Renkli yazma desteği
        if (player.hasPermission("nexorachat.color")) {
            message = ChatColor.translateAlternateColorCodes('&', message);
        }
        
        // Chat formatı
        String rank = getRank(player);
        String format = rank + " " + ChatColor.WHITE + player.getName() + 
                       ChatColor.DARK_GRAY + " » " + ChatColor.GRAY + message;
        
        event.setFormat(format);
    }
    
    private String getRank(Player player) {
        // Rütbe kontrolü (NexoraRanks ile entegre olacak)
        if (player.hasPermission("nexora.owner")) {
            return ChatColor.DARK_RED + "" + ChatColor.BOLD + "[OWNER]";
        } else if (player.hasPermission("nexora.admin")) {
            return ChatColor.RED + "" + ChatColor.BOLD + "[ADMIN]";
        } else if (player.hasPermission("nexora.mod")) {
            return ChatColor.GOLD + "" + ChatColor.BOLD + "[MOD]";
        } else if (player.hasPermission("nexora.vip")) {
            return ChatColor.AQUA + "" + ChatColor.BOLD + "[VIP]";
        } else {
            return ChatColor.GRAY + "[OYUNCU]";
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("clearchat")) {
            if (!sender.hasPermission("nexorachat.clearchat")) {
                sender.sendMessage(ChatColor.RED + "✗ Bu komutu kullanma yetkin yok!");
                return true;
            }
            
            // Chat'i temizle
            for (int i = 0; i < 100; i++) {
                Bukkit.broadcastMessage("");
            }
            
            Bukkit.broadcastMessage(ChatColor.GREEN + "✓ Chat " + 
                (sender instanceof Player ? ((Player) sender).getName() : "CONSOLE") + 
                " tarafından temizlendi!");
            return true;
        }
        
        if (command.getName().equalsIgnoreCase("mutechat")) {
            if (!sender.hasPermission("nexorachat.mutechat")) {
                sender.sendMessage(ChatColor.RED + "✗ Bu komutu kullanma yetkin yok!");
                return true;
            }
            
            chatMuted = !chatMuted;
            
            if (chatMuted) {
                Bukkit.broadcastMessage(ChatColor.RED + "✗ Chat " + 
                    (sender instanceof Player ? ((Player) sender).getName() : "CONSOLE") + 
                    " tarafından kapatıldı!");
            } else {
                Bukkit.broadcastMessage(ChatColor.GREEN + "✓ Chat " + 
                    (sender instanceof Player ? ((Player) sender).getName() : "CONSOLE") + 
                    " tarafından açıldı!");
            }
            return true;
        }
        
        return false;
    }
}
