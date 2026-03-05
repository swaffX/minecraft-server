package com.nexora.achievements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nexora.economy.NexoraEconomy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NexoraAchievements extends JavaPlugin implements Listener {
    
    private NexoraEconomy economy;
    private Map<UUID, Set<String>> playerAchievements = new ConcurrentHashMap<>();
    private Map<String, Achievement> achievements = new HashMap<>();
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private File dataFile;
    
    @Override
    public void onEnable() {
        getLogger().info("Nexora Achievements Plugin aktif!");
        
        // Economy plugin'i al
        economy = (NexoraEconomy) Bukkit.getPluginManager().getPlugin("NexoraEconomy");
        if (economy == null) {
            getLogger().severe("NexoraEconomy bulunamadı! Plugin devre dışı bırakılıyor.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        // Data klasörü oluştur
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        
        dataFile = new File(getDataFolder(), "achievements.json");
        
        // Başarıları yükle
        loadAchievements();
        
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
        getLogger().info("Nexora Achievements Plugin kapatıldı!");
    }
    
    private void loadAchievements() {
        // İlk Adımlar
        achievements.put("first_join", new Achievement("first_join", "İlk Adım", 
            "Sunucuya ilk kez katıl", 100, AchievementType.SPECIAL));
        
        // Blok Kırma
        achievements.put("break_100", new Achievement("break_100", "Madenci", 
            "100 blok kır", 200, AchievementType.MINING));
        achievements.put("break_1000", new Achievement("break_1000", "Usta Madenci", 
            "1000 blok kır", 500, AchievementType.MINING));
        achievements.put("break_10000", new Achievement("break_10000", "Efsane Madenci", 
            "10000 blok kır", 2000, AchievementType.MINING));
        
        // Elmas
        achievements.put("diamond_1", new Achievement("diamond_1", "İlk Elmas", 
            "İlk elmasını bul", 300, AchievementType.MINING));
        achievements.put("diamond_64", new Achievement("diamond_64", "Elmas Avcısı", 
            "64 elmas topla", 1000, AchievementType.MINING));
        
        // Mob Öldürme
        achievements.put("kill_10", new Achievement("kill_10", "Savaşçı", 
            "10 mob öldür", 200, AchievementType.COMBAT));
        achievements.put("kill_100", new Achievement("kill_100", "Usta Savaşçı", 
            "100 mob öldür", 500, AchievementType.COMBAT));
        achievements.put("kill_1000", new Achievement("kill_1000", "Efsane Savaşçı", 
            "1000 mob öldür", 2000, AchievementType.COMBAT));
        
        // Özel Moblar
        achievements.put("kill_ender_dragon", new Achievement("kill_ender_dragon", "Ejderha Avcısı", 
            "Ender Dragon'u öldür", 5000, AchievementType.COMBAT));
        achievements.put("kill_wither", new Achievement("kill_wither", "Wither Katili", 
            "Wither'ı öldür", 5000, AchievementType.COMBAT));
        
        // Ekonomi
        achievements.put("money_10000", new Achievement("money_10000", "Zengin", 
            "10000₺ biriktir", 500, AchievementType.ECONOMY));
        achievements.put("money_100000", new Achievement("money_100000", "Milyoner", 
            "100000₺ biriktir", 2000, AchievementType.ECONOMY));
        
        // Oyun Süresi
        achievements.put("playtime_1h", new Achievement("playtime_1h", "Yeni Başlayan", 
            "1 saat oyna", 200, AchievementType.SPECIAL));
        achievements.put("playtime_10h", new Achievement("playtime_10h", "Deneyimli", 
            "10 saat oyna", 500, AchievementType.SPECIAL));
        achievements.put("playtime_100h", new Achievement("playtime_100h", "Veteran", 
            "100 saat oyna", 2000, AchievementType.SPECIAL));
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // İlk kez giriş yapıyorsa
        if (!playerAchievements.containsKey(player.getUniqueId())) {
            playerAchievements.put(player.getUniqueId(), new HashSet<>());
            unlockAchievement(player, "first_join");
        }
        
        // Oyun süresi başarıları
        checkPlaytimeAchievements(player);
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        // Toplam blok kırma
        int totalBlocks = player.getStatistic(Statistic.MINE_BLOCK, event.getBlock().getType()) + 1;
        int allBlocks = 0;
        for (Material mat : Material.values()) {
            if (mat.isBlock()) {
                allBlocks += player.getStatistic(Statistic.MINE_BLOCK, mat);
            }
        }
        
        checkBlockAchievements(player, allBlocks);
        
        // Elmas başarıları
        if (event.getBlock().getType() == Material.DIAMOND_ORE || 
            event.getBlock().getType() == Material.DEEPSLATE_DIAMOND_ORE) {
            int diamonds = player.getStatistic(Statistic.MINE_BLOCK, Material.DIAMOND_ORE) + 
                          player.getStatistic(Statistic.MINE_BLOCK, Material.DEEPSLATE_DIAMOND_ORE) + 1;
            checkDiamondAchievements(player, diamonds);
        }
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        
        Player player = event.getEntity().getKiller();
        EntityType type = event.getEntityType();
        
        // Toplam mob öldürme
        int totalKills = 0;
        for (EntityType entityType : EntityType.values()) {
            if (entityType.isAlive() && entityType != EntityType.PLAYER) {
                try {
                    totalKills += player.getStatistic(Statistic.KILL_ENTITY, entityType);
                } catch (Exception ignored) {}
            }
        }
        totalKills++; // Şu anki öldürme
        
        checkKillAchievements(player, totalKills);
        
        // Özel mob başarıları
        if (type == EntityType.ENDER_DRAGON) {
            unlockAchievement(player, "kill_ender_dragon");
        } else if (type == EntityType.WITHER) {
            unlockAchievement(player, "kill_wither");
        }
    }
    
    private void checkBlockAchievements(Player player, int blocks) {
        if (blocks >= 100 && !hasAchievement(player, "break_100")) {
            unlockAchievement(player, "break_100");
        }
        if (blocks >= 1000 && !hasAchievement(player, "break_1000")) {
            unlockAchievement(player, "break_1000");
        }
        if (blocks >= 10000 && !hasAchievement(player, "break_10000")) {
            unlockAchievement(player, "break_10000");
        }
    }
    
    private void checkDiamondAchievements(Player player, int diamonds) {
        if (diamonds >= 1 && !hasAchievement(player, "diamond_1")) {
            unlockAchievement(player, "diamond_1");
        }
        if (diamonds >= 64 && !hasAchievement(player, "diamond_64")) {
            unlockAchievement(player, "diamond_64");
        }
    }
    
    private void checkKillAchievements(Player player, int kills) {
        if (kills >= 10 && !hasAchievement(player, "kill_10")) {
            unlockAchievement(player, "kill_10");
        }
        if (kills >= 100 && !hasAchievement(player, "kill_100")) {
            unlockAchievement(player, "kill_100");
        }
        if (kills >= 1000 && !hasAchievement(player, "kill_1000")) {
            unlockAchievement(player, "kill_1000");
        }
    }
    
    private void checkPlaytimeAchievements(Player player) {
        int ticks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        int hours = ticks / 72000; // 72000 tick = 1 saat
        
        if (hours >= 1 && !hasAchievement(player, "playtime_1h")) {
            unlockAchievement(player, "playtime_1h");
        }
        if (hours >= 10 && !hasAchievement(player, "playtime_10h")) {
            unlockAchievement(player, "playtime_10h");
        }
        if (hours >= 100 && !hasAchievement(player, "playtime_100h")) {
            unlockAchievement(player, "playtime_100h");
        }
    }
    
    private void checkMoneyAchievements(Player player) {
        double balance = economy.getBalance(player.getUniqueId());
        
        if (balance >= 10000 && !hasAchievement(player, "money_10000")) {
            unlockAchievement(player, "money_10000");
        }
        if (balance >= 100000 && !hasAchievement(player, "money_100000")) {
            unlockAchievement(player, "money_100000");
        }
    }
    
    private void unlockAchievement(Player player, String achievementId) {
        Achievement achievement = achievements.get(achievementId);
        if (achievement == null) return;
        
        Set<String> playerAchs = playerAchievements.get(player.getUniqueId());
        if (playerAchs == null) {
            playerAchs = new HashSet<>();
            playerAchievements.put(player.getUniqueId(), playerAchs);
        }
        
        playerAchs.add(achievementId);
        
        // Ödül ver
        economy.addBalance(player.getUniqueId(), achievement.getReward());
        
        // Duyuru
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "🏆 BAŞARI AÇILDI!");
        Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " başarısını açtı: " + 
            ChatColor.GREEN + achievement.getName());
        Bukkit.broadcastMessage(ChatColor.GRAY + achievement.getDescription());
        Bukkit.broadcastMessage(ChatColor.GREEN + "Ödül: " + ChatColor.GOLD + economy.formatMoney(achievement.getReward()));
        Bukkit.broadcastMessage("");
        
        saveData();
    }
    
    private boolean hasAchievement(Player player, String achievementId) {
        Set<String> playerAchs = playerAchievements.get(player.getUniqueId());
        return playerAchs != null && playerAchs.contains(achievementId);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("achievements")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Bu komut sadece oyuncular tarafından kullanılabilir!");
                return true;
            }
            
            Player player = (Player) sender;
            Set<String> playerAchs = playerAchievements.get(player.getUniqueId());
            if (playerAchs == null) {
                playerAchs = new HashSet<>();
            }
            
            // Para başarılarını kontrol et
            checkMoneyAchievements(player);
            
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "🏆 BAŞARILAR");
            player.sendMessage(ChatColor.GRAY + "Açılan: " + ChatColor.GREEN + playerAchs.size() + 
                ChatColor.GRAY + "/" + ChatColor.YELLOW + achievements.size());
            player.sendMessage("");
            
            // Kategorilere göre göster
            showAchievementsByType(player, playerAchs, AchievementType.SPECIAL, "⭐ Özel");
            showAchievementsByType(player, playerAchs, AchievementType.MINING, "⛏ Madencilik");
            showAchievementsByType(player, playerAchs, AchievementType.COMBAT, "⚔ Savaş");
            showAchievementsByType(player, playerAchs, AchievementType.ECONOMY, "💰 Ekonomi");
            
            player.sendMessage("");
            
            return true;
        }
        
        return false;
    }
    
    private void showAchievementsByType(Player player, Set<String> playerAchs, 
                                       AchievementType type, String title) {
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + title);
        
        for (Achievement ach : achievements.values()) {
            if (ach.getType() == type) {
                boolean unlocked = playerAchs.contains(ach.getId());
                String status = unlocked ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗";
                
                player.sendMessage(status + " " + ChatColor.WHITE + ach.getName() + 
                    ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ach.getDescription() + 
                    ChatColor.DARK_GRAY + " (" + ChatColor.GOLD + economy.formatMoney(ach.getReward()) + 
                    ChatColor.DARK_GRAY + ")");
            }
        }
        
        player.sendMessage("");
    }
    
    private void loadData() {
        if (!dataFile.exists()) {
            return;
        }
        
        try (FileReader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, Set<String>>>(){}.getType();
            Map<String, Set<String>> data = gson.fromJson(reader, type);
            
            if (data != null) {
                playerAchievements.clear();
                for (Map.Entry<String, Set<String>> entry : data.entrySet()) {
                    playerAchievements.put(UUID.fromString(entry.getKey()), entry.getValue());
                }
            }
            
            getLogger().info(playerAchievements.size() + " oyuncu başarısı yüklendi!");
        } catch (IOException e) {
            getLogger().severe("Başarılar yüklenemedi: " + e.getMessage());
        }
    }
    
    private void saveData() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            Map<String, Set<String>> data = new HashMap<>();
            for (Map.Entry<UUID, Set<String>> entry : playerAchievements.entrySet()) {
                data.put(entry.getKey().toString(), entry.getValue());
            }
            
            gson.toJson(data, writer);
        } catch (IOException e) {
            getLogger().severe("Başarılar kaydedilemedi: " + e.getMessage());
        }
    }
    
    private enum AchievementType {
        SPECIAL, MINING, COMBAT, ECONOMY
    }
    
    private static class Achievement {
        private final String id;
        private final String name;
        private final String description;
        private final double reward;
        private final AchievementType type;
        
        public Achievement(String id, String name, String description, double reward, AchievementType type) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.reward = reward;
            this.type = type;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public double getReward() { return reward; }
        public AchievementType getType() { return type; }
    }
}
