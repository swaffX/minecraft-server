package com.nexora.daily;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nexora.economy.NexoraEconomy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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

public class NexoraDaily extends JavaPlugin implements Listener {
    
    private NexoraEconomy economy;
    private Map<UUID, List<Quest>> playerQuests = new ConcurrentHashMap<>();
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private File dataFile;
    
    @Override
    public void onEnable() {
        getLogger().info("Nexora Daily Plugin aktif!");
        
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
        
        dataFile = new File(getDataFolder(), "quests.json");
        
        // Verileri yükle
        loadData();
        
        // Event listener
        Bukkit.getPluginManager().registerEvents(this, this);
        
        // Günlük görev sıfırlama (her gün 00:00'da)
        Bukkit.getScheduler().runTaskTimer(this, this::checkDailyReset, 1200L, 1200L); // Her dakika kontrol
        
        // Otomatik kaydetme (her 5 dakikada)
        Bukkit.getScheduler().runTaskTimer(this, this::saveData, 6000L, 6000L);
    }
    
    @Override
    public void onDisable() {
        saveData();
        getLogger().info("Nexora Daily Plugin kapatıldı!");
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // İlk kez giriş yapıyorsa görevler oluştur
        if (!playerQuests.containsKey(player.getUniqueId())) {
            generateDailyQuests(player.getUniqueId());
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material block = event.getBlock().getType();
        
        List<Quest> quests = playerQuests.get(player.getUniqueId());
        if (quests == null) return;
        
        for (Quest quest : quests) {
            if (quest.getType() == QuestType.MINE_BLOCKS && !quest.isCompleted()) {
                if (quest.getTargetMaterial() == block) {
                    quest.addProgress(1);
                    
                    if (quest.isCompleted()) {
                        completeQuest(player, quest);
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "⚒ Görev İlerlemesi: " + 
                            ChatColor.WHITE + String.valueOf(quest.getProgress()) + "/" + String.valueOf(quest.getTarget()) + 
                            ChatColor.GRAY + " (" + quest.getDescription() + ")");
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        
        Player player = event.getEntity().getKiller();
        EntityType entityType = event.getEntityType();
        
        List<Quest> quests = playerQuests.get(player.getUniqueId());
        if (quests == null) return;
        
        for (Quest quest : quests) {
            if (quest.getType() == QuestType.KILL_MOBS && !quest.isCompleted()) {
                if (quest.getTargetEntity() == entityType) {
                    quest.addProgress(1);
                    
                    if (quest.isCompleted()) {
                        completeQuest(player, quest);
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "⚔ Görev İlerlemesi: " + 
                            ChatColor.WHITE + String.valueOf(quest.getProgress()) + "/" + String.valueOf(quest.getTarget()) + 
                            ChatColor.GRAY + " (" + quest.getDescription() + ")");
                    }
                }
            }
        }
    }
    
    private void completeQuest(Player player, Quest quest) {
        quest.setCompleted(true);
        economy.addBalance(player.getUniqueId(), quest.getReward());
        
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "✓ GÖREV TAMAMLANDI!");
        player.sendMessage(ChatColor.GRAY + quest.getDescription());
        player.sendMessage(ChatColor.GREEN + "Ödül: " + ChatColor.GOLD + economy.formatMoney(quest.getReward()));
        player.sendMessage("");
        
        // Tüm görevler tamamlandı mı?
        boolean allCompleted = true;
        for (Quest q : playerQuests.get(player.getUniqueId())) {
            if (!q.isCompleted()) {
                allCompleted = false;
                break;
            }
        }
        
        if (allCompleted) {
            double bonus = 500.0;
            economy.addBalance(player.getUniqueId(), bonus);
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "🎉 TÜM GÖREVLER TAMAMLANDI!");
            player.sendMessage(ChatColor.GREEN + "Bonus Ödül: " + ChatColor.GOLD + economy.formatMoney(bonus));
        }
        
        saveData();
    }
    
    private void generateDailyQuests(UUID uuid) {
        List<Quest> quests = new ArrayList<>();
        
        // 3 rastgele görev oluştur
        Random random = new Random();
        
        // Görev 1: Blok kırma
        Material[] mineBlocks = {Material.STONE, Material.COBBLESTONE, Material.OAK_LOG, 
            Material.COAL_ORE, Material.IRON_ORE, Material.DIRT};
        Material mineBlock = mineBlocks[random.nextInt(mineBlocks.length)];
        int mineAmount = 50 + random.nextInt(100); // 50-150
        double mineReward = 200 + random.nextInt(300); // 200-500
        
        quests.add(new Quest(
            QuestType.MINE_BLOCKS,
            mineBlock.name().toLowerCase().replace("_", " ") + " kır",
            mineAmount,
            mineReward,
            mineBlock,
            null
        ));
        
        // Görev 2: Mob öldürme
        EntityType[] killMobs = {EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, 
            EntityType.CREEPER, EntityType.ENDERMAN};
        EntityType killMob = killMobs[random.nextInt(killMobs.length)];
        int killAmount = 10 + random.nextInt(20); // 10-30
        double killReward = 300 + random.nextInt(300); // 300-600
        
        quests.add(new Quest(
            QuestType.KILL_MOBS,
            killMob.name().toLowerCase() + " öldür",
            killAmount,
            killReward,
            null,
            killMob
        ));
        
        // Görev 3: Farklı blok kırma
        Material[] mineBlocks2 = {Material.DIAMOND_ORE, Material.GOLD_ORE, Material.LAPIS_ORE, 
            Material.REDSTONE_ORE, Material.EMERALD_ORE};
        Material mineBlock2 = mineBlocks2[random.nextInt(mineBlocks2.length)];
        int mineAmount2 = 5 + random.nextInt(10); // 5-15
        double mineReward2 = 500 + random.nextInt(500); // 500-1000
        
        quests.add(new Quest(
            QuestType.MINE_BLOCKS,
            mineBlock2.name().toLowerCase().replace("_", " ") + " kır",
            mineAmount2,
            mineReward2,
            mineBlock2,
            null
        ));
        
        playerQuests.put(uuid, quests);
        saveData();
    }
    
    private void checkDailyReset() {
        // Her gün 00:00'da görevleri sıfırla
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        
        // 00:00 - 00:01 arası
        if (hour == 0 && minute == 0) {
            for (UUID uuid : playerQuests.keySet()) {
                generateDailyQuests(uuid);
                
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    player.sendMessage(ChatColor.GOLD + "✓ Günlük görevlerin yenilendi! " + 
                        ChatColor.YELLOW + "/quests " + ChatColor.GRAY + "ile görebilirsin.");
                }
            }
            
            getLogger().info("Günlük görevler sıfırlandı!");
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("quests")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Bu komut sadece oyuncular tarafından kullanılabilir!");
                return true;
            }
            
            Player player = (Player) sender;
            List<Quest> quests = playerQuests.get(player.getUniqueId());
            
            if (quests == null || quests.isEmpty()) {
                generateDailyQuests(player.getUniqueId());
                quests = playerQuests.get(player.getUniqueId());
            }
            
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "📋 GÜNLÜK GÖREVLER");
            player.sendMessage("");
            
            int questNum = 1;
            for (Quest quest : quests) {
                String status = quest.isCompleted() ? 
                    ChatColor.GREEN + "✓ Tamamlandı" : 
                    ChatColor.YELLOW + quest.getProgress() + "/" + quest.getTarget();
                
                player.sendMessage(ChatColor.GOLD + "#" + questNum + " " + 
                    ChatColor.WHITE + quest.getDescription());
                player.sendMessage(ChatColor.GRAY + "  İlerleme: " + status);
                player.sendMessage(ChatColor.GRAY + "  Ödül: " + ChatColor.GREEN + 
                    economy.formatMoney(quest.getReward()));
                player.sendMessage("");
                
                questNum++;
            }
            
            // Tüm görevler tamamlandı mı?
            boolean allCompleted = true;
            for (Quest q : quests) {
                if (!q.isCompleted()) {
                    allCompleted = false;
                    break;
                }
            }
            
            if (allCompleted) {
                player.sendMessage(ChatColor.GOLD + "🎉 Tüm görevleri tamamladın! Yarın yeni görevler gelecek.");
            } else {
                player.sendMessage(ChatColor.GRAY + "Tüm görevleri tamamlarsan bonus ödül kazanırsın!");
            }
            
            player.sendMessage("");
            
            return true;
        }
        
        return false;
    }
    
    private void loadData() {
        if (!dataFile.exists()) {
            return;
        }
        
        try (FileReader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, List<Quest>>>(){}.getType();
            Map<String, List<Quest>> data = gson.fromJson(reader, type);
            
            if (data != null) {
                playerQuests.clear();
                for (Map.Entry<String, List<Quest>> entry : data.entrySet()) {
                    playerQuests.put(UUID.fromString(entry.getKey()), entry.getValue());
                }
            }
            
            getLogger().info(playerQuests.size() + " oyuncu görevi yüklendi!");
        } catch (IOException e) {
            getLogger().severe("Görevler yüklenemedi: " + e.getMessage());
        }
    }
    
    private void saveData() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            Map<String, List<Quest>> data = new HashMap<>();
            for (Map.Entry<UUID, List<Quest>> entry : playerQuests.entrySet()) {
                data.put(entry.getKey().toString(), entry.getValue());
            }
            
            gson.toJson(data, writer);
        } catch (IOException e) {
            getLogger().severe("Görevler kaydedilemedi: " + e.getMessage());
        }
    }
    
    private enum QuestType {
        MINE_BLOCKS,
        KILL_MOBS
    }
    
    private static class Quest {
        private QuestType type;
        private String description;
        private int target;
        private int progress;
        private double reward;
        private boolean completed;
        private Material targetMaterial;
        private EntityType targetEntity;
        
        public Quest(QuestType type, String description, int target, double reward, 
                    Material targetMaterial, EntityType targetEntity) {
            this.type = type;
            this.description = description;
            this.target = target;
            this.progress = 0;
            this.reward = reward;
            this.completed = false;
            this.targetMaterial = targetMaterial;
            this.targetEntity = targetEntity;
        }
        
        public QuestType getType() { return type; }
        public String getDescription() { return description; }
        public int getTarget() { return target; }
        public int getProgress() { return progress; }
        public double getReward() { return reward; }
        public boolean isCompleted() { return completed; }
        public Material getTargetMaterial() { return targetMaterial; }
        public EntityType getTargetEntity() { return targetEntity; }
        
        public void addProgress(int amount) {
            this.progress += amount;
            if (this.progress >= this.target) {
                this.progress = this.target;
                this.completed = true;
            }
        }
        
        public void setCompleted(boolean completed) {
            this.completed = completed;
        }
    }
}
