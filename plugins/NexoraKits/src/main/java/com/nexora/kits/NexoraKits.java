package com.nexora.kits;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NexoraKits extends JavaPlugin implements Listener {
    
    private Map<UUID, Long> kitCooldowns = new HashMap<>();
    private Map<UUID, Boolean> hasReceivedStarter = new HashMap<>();
    
    @Override
    public void onEnable() {
        getLogger().info("Nexora Kits Plugin aktif!");
        Bukkit.getPluginManager().registerEvents(this, this);
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Nexora Kits Plugin kapatıldı!");
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // İlk kez giriş yapıyorsa starter kit ver
        if (!player.hasPlayedBefore()) {
            giveStarterKit(player);
            player.sendMessage(ChatColor.GREEN + "✓ Başlangıç kiti verildi!");
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Bu komut sadece oyuncular tarafından kullanılabilir!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (command.getName().equalsIgnoreCase("kit")) {
            if (args.length == 0) {
                // Kit listesi
                player.sendMessage("");
                player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "✦ NEXORA KİTLER ✦");
                player.sendMessage("");
                player.sendMessage(ChatColor.GRAY + "  /kit starter " + ChatColor.DARK_GRAY + "- " + ChatColor.WHITE + "Başlangıç kiti");
                player.sendMessage(ChatColor.GRAY + "  /kit vip " + ChatColor.DARK_GRAY + "- " + ChatColor.AQUA + "VIP kiti");
                player.sendMessage("");
                return true;
            }
            
            String kitName = args[0].toLowerCase();
            
            switch (kitName) {
                case "starter":
                    if (!player.hasPermission("nexorakits.starter")) {
                        player.sendMessage(ChatColor.RED + "✗ Bu kiti alma yetkin yok!");
                        return true;
                    }
                    
                    if (!canUseKit(player, "starter", 3600000)) { // 1 saat cooldown
                        long remaining = getRemainingCooldown(player, "starter", 3600000);
                        player.sendMessage(ChatColor.RED + "✗ Bu kiti " + formatTime(remaining) + " sonra alabilirsin!");
                        return true;
                    }
                    
                    giveStarterKit(player);
                    player.sendMessage(ChatColor.GREEN + "✓ Başlangıç kiti verildi!");
                    setKitCooldown(player, "starter");
                    break;
                    
                case "vip":
                    if (!player.hasPermission("nexorakits.vip")) {
                        player.sendMessage(ChatColor.RED + "✗ Bu kit sadece VIP'ler içindir!");
                        return true;
                    }
                    
                    if (!canUseKit(player, "vip", 1800000)) { // 30 dakika cooldown
                        long remaining = getRemainingCooldown(player, "vip", 1800000);
                        player.sendMessage(ChatColor.RED + "✗ Bu kiti " + formatTime(remaining) + " sonra alabilirsin!");
                        return true;
                    }
                    
                    giveVIPKit(player);
                    player.sendMessage(ChatColor.AQUA + "✓ VIP kiti verildi!");
                    setKitCooldown(player, "vip");
                    break;
                    
                default:
                    player.sendMessage(ChatColor.RED + "✗ Bu kit bulunamadı! /kit yazarak kitleri görebilirsin.");
                    break;
            }
            
            return true;
        }
        
        return false;
    }
    
    private void giveStarterKit(Player player) {
        // Temel itemler
        player.getInventory().addItem(
            new ItemStack(Material.STONE_SWORD, 1),
            new ItemStack(Material.STONE_PICKAXE, 1),
            new ItemStack(Material.STONE_AXE, 1),
            new ItemStack(Material.STONE_SHOVEL, 1),
            new ItemStack(Material.COOKED_BEEF, 16),
            new ItemStack(Material.OAK_LOG, 32),
            new ItemStack(Material.TORCH, 16)
        );
    }
    
    private void giveVIPKit(Player player) {
        // VIP itemler
        player.getInventory().addItem(
            new ItemStack(Material.IRON_SWORD, 1),
            new ItemStack(Material.IRON_PICKAXE, 1),
            new ItemStack(Material.IRON_AXE, 1),
            new ItemStack(Material.IRON_SHOVEL, 1),
            new ItemStack(Material.IRON_HELMET, 1),
            new ItemStack(Material.IRON_CHESTPLATE, 1),
            new ItemStack(Material.IRON_LEGGINGS, 1),
            new ItemStack(Material.IRON_BOOTS, 1),
            new ItemStack(Material.COOKED_BEEF, 32),
            new ItemStack(Material.GOLDEN_APPLE, 4),
            new ItemStack(Material.TORCH, 64)
        );
    }
    
    private boolean canUseKit(Player player, String kitName, long cooldownMs) {
        String key = player.getUniqueId() + "_" + kitName;
        if (!kitCooldowns.containsKey(UUID.fromString(key.hashCode() + ""))) {
            return true;
        }
        
        long lastUse = kitCooldowns.getOrDefault(UUID.fromString(key.hashCode() + ""), 0L);
        return System.currentTimeMillis() - lastUse >= cooldownMs;
    }
    
    private long getRemainingCooldown(Player player, String kitName, long cooldownMs) {
        String key = player.getUniqueId() + "_" + kitName;
        long lastUse = kitCooldowns.getOrDefault(UUID.fromString(key.hashCode() + ""), 0L);
        long elapsed = System.currentTimeMillis() - lastUse;
        return cooldownMs - elapsed;
    }
    
    private void setKitCooldown(Player player, String kitName) {
        String key = player.getUniqueId() + "_" + kitName;
        kitCooldowns.put(UUID.fromString(key.hashCode() + ""), System.currentTimeMillis());
    }
    
    private String formatTime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return hours + " saat " + (minutes % 60) + " dakika";
        } else if (minutes > 0) {
            return minutes + " dakika " + (seconds % 60) + " saniye";
        } else {
            return seconds + " saniye";
        }
    }
}
