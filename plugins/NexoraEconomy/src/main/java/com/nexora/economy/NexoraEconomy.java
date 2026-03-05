package com.nexora.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NexoraEconomy extends JavaPlugin implements Listener {
    
    private static NexoraEconomy instance;
    private Map<UUID, Double> balances = new ConcurrentHashMap<>();
    private Map<UUID, Long> dailyCooldowns = new ConcurrentHashMap<>();
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private File dataFile;
    
    // Fiyatlar
    private static final double STARTING_BALANCE = 1000.0;
    private static final double DAILY_REWARD = 500.0;
    private static final long DAILY_COOLDOWN = 24 * 60 * 60 * 1000; // 24 saat
    
    // Otomatik satış fiyatları
    private static final Map<Material, Double> AUTO_SELL_PRICES = new HashMap<>();
    
    static {
        // Değerli madenler
        AUTO_SELL_PRICES.put(Material.DIAMOND, 100.0);
        AUTO_SELL_PRICES.put(Material.GOLD_INGOT, 50.0);
        AUTO_SELL_PRICES.put(Material.IRON_INGOT, 20.0);
        AUTO_SELL_PRICES.put(Material.EMERALD, 150.0);
        AUTO_SELL_PRICES.put(Material.NETHERITE_INGOT, 500.0);
        
        // Cevherler
        AUTO_SELL_PRICES.put(Material.DIAMOND_ORE, 100.0);
        AUTO_SELL_PRICES.put(Material.DEEPSLATE_DIAMOND_ORE, 100.0);
        AUTO_SELL_PRICES.put(Material.GOLD_ORE, 50.0);
        AUTO_SELL_PRICES.put(Material.DEEPSLATE_GOLD_ORE, 50.0);
        AUTO_SELL_PRICES.put(Material.IRON_ORE, 20.0);
        AUTO_SELL_PRICES.put(Material.DEEPSLATE_IRON_ORE, 20.0);
        AUTO_SELL_PRICES.put(Material.EMERALD_ORE, 150.0);
        AUTO_SELL_PRICES.put(Material.DEEPSLATE_EMERALD_ORE, 150.0);
        AUTO_SELL_PRICES.put(Material.COAL_ORE, 5.0);
        AUTO_SELL_PRICES.put(Material.DEEPSLATE_COAL_ORE, 5.0);
        AUTO_SELL_PRICES.put(Material.LAPIS_ORE, 10.0);
        AUTO_SELL_PRICES.put(Material.DEEPSLATE_LAPIS_ORE, 10.0);
        AUTO_SELL_PRICES.put(Material.REDSTONE_ORE, 8.0);
        AUTO_SELL_PRICES.put(Material.DEEPSLATE_REDSTONE_ORE, 8.0);
    }
    
    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Nexora Economy Plugin aktif!");
        
        // Data klasörü oluştur
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        
        dataFile = new File(getDataFolder(), "balances.json");
        
        // Verileri yükle
        loadData();
        
        // Event listener
        Bukkit.getPluginManager().registerEvents(this, this);
        
        // Otomatik kaydetme (her 5 dakikada)
        Bukkit.getScheduler().runTaskTimer(this, this::saveData, 6000L, 6000L);
    }
    
    @Override
    public void onDisable() {
        saveData();
        getLogger().info("Nexora Economy Plugin kapatıldı!");
    }
    
    public static NexoraEconomy getInstance() {
        return instance;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // İlk kez giriş yapıyorsa başlangıç parası ver
        if (!balances.containsKey(player.getUniqueId())) {
            balances.put(player.getUniqueId(), STARTING_BALANCE);
            player.sendMessage(ChatColor.GREEN + "✓ Hoşgeldin! Başlangıç paran: " + 
                ChatColor.GOLD + formatMoney(STARTING_BALANCE));
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();
        
        // Otomatik satış kontrolü
        if (AUTO_SELL_PRICES.containsKey(blockType)) {
            double price = AUTO_SELL_PRICES.get(blockType);
            addBalance(player.getUniqueId(), price);
            
            player.sendMessage(ChatColor.GOLD + "💰 +" + formatMoney(price) + 
                ChatColor.GRAY + " (" + getBlockName(blockType) + ")");
        }
    }
    
    private String getBlockName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
    
    // API Methods
    public double getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, 0.0);
    }
    
    public void setBalance(UUID uuid, double amount) {
        balances.put(uuid, Math.max(0, amount));
        saveData();
    }
    
    public void addBalance(UUID uuid, double amount) {
        double current = getBalance(uuid);
        setBalance(uuid, current + amount);
    }
    
    public void removeBalance(UUID uuid, double amount) {
        double current = getBalance(uuid);
        setBalance(uuid, current - amount);
    }
    
    public boolean hasBalance(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }
    
    public String formatMoney(double amount) {
        return String.format("%.2f₺", amount);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("balance")) {
            return handleBalance(sender, args);
        }
        
        if (command.getName().equalsIgnoreCase("pay")) {
            return handlePay(sender, args);
        }
        
        if (command.getName().equalsIgnoreCase("baltop")) {
            return handleBalTop(sender);
        }
        
        if (command.getName().equalsIgnoreCase("eco")) {
            return handleEco(sender, args);
        }
        
        if (command.getName().equalsIgnoreCase("daily")) {
            return handleDaily(sender);
        }
        
        return false;
    }
    
    private boolean handleBalance(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Konsol için oyuncu adı belirtmelisin!");
                return true;
            }
            
            Player player = (Player) sender;
            double balance = getBalance(player.getUniqueId());
            
            sender.sendMessage("");
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "💰 BAKİYEN");
            sender.sendMessage(ChatColor.GRAY + "Para: " + ChatColor.GREEN + formatMoney(balance));
            sender.sendMessage("");
            
            return true;
        }
        
        // Başka oyuncunun bakiyesini göster
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "✗ Oyuncu bulunamadı!");
            return true;
        }
        
        double balance = getBalance(target.getUniqueId());
        sender.sendMessage(ChatColor.GRAY + target.getName() + " bakiye: " + 
            ChatColor.GREEN + formatMoney(balance));
        
        return true;
    }

    
    private boolean handlePay(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Bu komut sadece oyuncular tarafından kullanılabilir!");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Kullanım: /pay <oyuncu> <miktar>");
            return true;
        }
        
        Player player = (Player) sender;
        Player target = Bukkit.getPlayer(args[0]);
        
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "✗ Oyuncu bulunamadı!");
            return true;
        }
        
        if (target.getUniqueId().equals(player.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "✗ Kendine para gönderemezsin!");
            return true;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "✗ Geçersiz miktar!");
            return true;
        }
        
        if (amount <= 0) {
            sender.sendMessage(ChatColor.RED + "✗ Miktar 0'dan büyük olmalı!");
            return true;
        }
        
        if (!hasBalance(player.getUniqueId(), amount)) {
            sender.sendMessage(ChatColor.RED + "✗ Yetersiz bakiye!");
            return true;
        }
        
        // Para transferi
        removeBalance(player.getUniqueId(), amount);
        addBalance(target.getUniqueId(), amount);
        
        player.sendMessage(ChatColor.GREEN + "✓ " + target.getName() + " oyuncusuna " + 
            ChatColor.GOLD + formatMoney(amount) + ChatColor.GREEN + " gönderildi!");
        target.sendMessage(ChatColor.GREEN + "✓ " + player.getName() + " sana " + 
            ChatColor.GOLD + formatMoney(amount) + ChatColor.GREEN + " gönderdi!");
        
        return true;
    }
    
    private boolean handleBalTop(CommandSender sender) {
        // Bakiyeleri sırala
        List<Map.Entry<UUID, Double>> sortedBalances = new ArrayList<>(balances.entrySet());
        sortedBalances.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "👑 EN ZENGİNLER");
        sender.sendMessage("");
        
        int rank = 1;
        for (int i = 0; i < Math.min(10, sortedBalances.size()); i++) {
            Map.Entry<UUID, Double> entry = sortedBalances.get(i);
            String playerName = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            
            String rankColor;
            if (rank == 1) rankColor = ChatColor.GOLD + "🥇";
            else if (rank == 2) rankColor = ChatColor.GRAY + "🥈";
            else if (rank == 3) rankColor = ChatColor.GOLD + "🥉";
            else rankColor = ChatColor.WHITE + "#" + rank;
            
            sender.sendMessage(rankColor + " " + ChatColor.WHITE + playerName + 
                ChatColor.DARK_GRAY + " - " + ChatColor.GREEN + formatMoney(entry.getValue()));
            rank++;
        }
        
        sender.sendMessage("");
        return true;
    }
    
    private boolean handleEco(CommandSender sender, String[] args) {
        if (!sender.hasPermission("nexoraeconomy.admin")) {
            sender.sendMessage(ChatColor.RED + "✗ Bu komutu kullanma yetkin yok!");
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Kullanım: /eco <give|take|set> <oyuncu> <miktar>");
            return true;
        }
        
        String action = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);
        
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "✗ Oyuncu bulunamadı!");
            return true;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "✗ Geçersiz miktar!");
            return true;
        }
        
        switch (action) {
            case "give":
                addBalance(target.getUniqueId(), amount);
                sender.sendMessage(ChatColor.GREEN + "✓ " + target.getName() + " oyuncusuna " + 
                    formatMoney(amount) + " verildi!");
                target.sendMessage(ChatColor.GREEN + "✓ Hesabına " + formatMoney(amount) + " eklendi!");
                break;
                
            case "take":
                removeBalance(target.getUniqueId(), amount);
                sender.sendMessage(ChatColor.GREEN + "✓ " + target.getName() + " oyuncusundan " + 
                    formatMoney(amount) + " alındı!");
                target.sendMessage(ChatColor.RED + "✗ Hesabından " + formatMoney(amount) + " çıkarıldı!");
                break;
                
            case "set":
                setBalance(target.getUniqueId(), amount);
                sender.sendMessage(ChatColor.GREEN + "✓ " + target.getName() + " bakiyesi " + 
                    formatMoney(amount) + " olarak ayarlandı!");
                target.sendMessage(ChatColor.GREEN + "✓ Bakiyen " + formatMoney(amount) + " olarak güncellendi!");
                break;
                
            default:
                sender.sendMessage(ChatColor.RED + "✗ Geçersiz işlem! (give, take, set)");
                break;
        }
        
        return true;
    }
    
    private boolean handleDaily(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Bu komut sadece oyuncular tarafından kullanılabilir!");
            return true;
        }
        
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        
        long currentTime = System.currentTimeMillis();
        
        if (dailyCooldowns.containsKey(uuid)) {
            long lastClaim = dailyCooldowns.get(uuid);
            long timePassed = currentTime - lastClaim;
            
            if (timePassed < DAILY_COOLDOWN) {
                long remaining = DAILY_COOLDOWN - timePassed;
                long hours = remaining / (60 * 60 * 1000);
                long minutes = (remaining % (60 * 60 * 1000)) / (60 * 1000);
                
                player.sendMessage(ChatColor.RED + "✗ Günlük ödülünü zaten aldın!");
                player.sendMessage(ChatColor.GRAY + "Kalan süre: " + ChatColor.AQUA + 
                    hours + " saat " + minutes + " dakika");
                return true;
            }
        }
        
        // Günlük ödül ver
        addBalance(uuid, DAILY_REWARD);
        dailyCooldowns.put(uuid, currentTime);
        
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "🎁 GÜNLÜK ÖDÜL");
        player.sendMessage(ChatColor.GREEN + "✓ " + formatMoney(DAILY_REWARD) + " kazandın!");
        player.sendMessage(ChatColor.GRAY + "Yeni bakiyen: " + ChatColor.GREEN + 
            formatMoney(getBalance(uuid)));
        player.sendMessage("");
        
        return true;
    }
    
    private void loadData() {
        if (!dataFile.exists()) {
            return;
        }
        
        try (FileReader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, Double>>(){}.getType();
            Map<String, Double> data = gson.fromJson(reader, type);
            
            if (data != null) {
                balances.clear();
                for (Map.Entry<String, Double> entry : data.entrySet()) {
                    balances.put(UUID.fromString(entry.getKey()), entry.getValue());
                }
            }
            
            getLogger().info(balances.size() + " oyuncu bakiyesi yüklendi!");
        } catch (IOException e) {
            getLogger().severe("Bakiyeler yüklenemedi: " + e.getMessage());
        }
    }
    
    private void saveData() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            Map<String, Double> data = new HashMap<>();
            for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
                data.put(entry.getKey().toString(), entry.getValue());
            }
            
            gson.toJson(data, writer);
        } catch (IOException e) {
            getLogger().severe("Bakiyeler kaydedilemedi: " + e.getMessage());
        }
    }
}
