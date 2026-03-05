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

public class NexoraKits extends JavaPlugin implements Listener {
    
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
            player.sendMessage("");
            player.sendMessage(ChatColor.GREEN + "✓ Başlangıç kiti verildi!");
            player.sendMessage(ChatColor.GRAY + "  İyi oyunlar dileriz! " + ChatColor.GOLD + "❤");
            player.sendMessage("");
        }
    }
    
    private void giveStarterKit(Player player) {
        // Temel başlangıç itemleri
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
}
