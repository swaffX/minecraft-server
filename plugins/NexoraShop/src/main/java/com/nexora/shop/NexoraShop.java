package com.nexora.shop;

import com.nexora.economy.NexoraEconomy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class NexoraShop extends JavaPlugin implements Listener {
    
    private NexoraEconomy economy;
    private Map<Material, ShopItem> shopItems = new HashMap<>();
    
    @Override
    public void onEnable() {
        getLogger().info("Nexora Shop Plugin aktif!");
        
        // Economy plugin'i al
        economy = (NexoraEconomy) Bukkit.getPluginManager().getPlugin("NexoraEconomy");
        if (economy == null) {
            getLogger().severe("NexoraEconomy bulunamadı! Plugin devre dışı bırakılıyor.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        // Event listener
        Bukkit.getPluginManager().registerEvents(this, this);
        
        // Mağaza eşyalarını yükle
        loadShopItems();
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Nexora Shop Plugin kapatıldı!");
    }
    
    private void loadShopItems() {
        // BLOKLAR
        addShopItem(Material.STONE, 10, 5, "Bloklar");
        addShopItem(Material.COBBLESTONE, 5, 2, "Bloklar");
        addShopItem(Material.OAK_LOG, 20, 10, "Bloklar");
        addShopItem(Material.SPRUCE_LOG, 20, 10, "Bloklar");
        addShopItem(Material.BIRCH_LOG, 20, 10, "Bloklar");
        addShopItem(Material.DIRT, 2, 1, "Bloklar");
        addShopItem(Material.SAND, 8, 4, "Bloklar");
        addShopItem(Material.GRAVEL, 6, 3, "Bloklar");
        addShopItem(Material.GLASS, 15, 7, "Bloklar");
        addShopItem(Material.BRICKS, 30, 15, "Bloklar");
        
        // YEMEK
        addShopItem(Material.COOKED_BEEF, 25, 12, "Yemek");
        addShopItem(Material.COOKED_PORKCHOP, 25, 12, "Yemek");
        addShopItem(Material.BREAD, 15, 7, "Yemek");
        addShopItem(Material.APPLE, 10, 5, "Yemek");
        addShopItem(Material.GOLDEN_APPLE, 200, 100, "Yemek");
        addShopItem(Material.COOKED_CHICKEN, 20, 10, "Yemek");
        addShopItem(Material.COOKED_MUTTON, 20, 10, "Yemek");
        addShopItem(Material.BAKED_POTATO, 12, 6, "Yemek");
        
        // ALETLER
        addShopItem(Material.IRON_PICKAXE, 150, 75, "Aletler");
        addShopItem(Material.IRON_AXE, 150, 75, "Aletler");
        addShopItem(Material.IRON_SHOVEL, 100, 50, "Aletler");
        addShopItem(Material.IRON_HOE, 100, 50, "Aletler");
        addShopItem(Material.DIAMOND_PICKAXE, 800, 400, "Aletler");
        addShopItem(Material.DIAMOND_AXE, 800, 400, "Aletler");
        addShopItem(Material.DIAMOND_SHOVEL, 600, 300, "Aletler");
        
        // SİLAHLAR
        addShopItem(Material.IRON_SWORD, 150, 75, "Silahlar");
        addShopItem(Material.DIAMOND_SWORD, 800, 400, "Silahlar");
        addShopItem(Material.BOW, 200, 100, "Silahlar");
        addShopItem(Material.ARROW, 2, 1, "Silahlar");
        addShopItem(Material.SHIELD, 100, 50, "Silahlar");
        
        // ZIRH
        addShopItem(Material.IRON_HELMET, 100, 50, "Zırh");
        addShopItem(Material.IRON_CHESTPLATE, 200, 100, "Zırh");
        addShopItem(Material.IRON_LEGGINGS, 150, 75, "Zırh");
        addShopItem(Material.IRON_BOOTS, 100, 50, "Zırh");
        addShopItem(Material.DIAMOND_HELMET, 500, 250, "Zırh");
        addShopItem(Material.DIAMOND_CHESTPLATE, 1000, 500, "Zırh");
        addShopItem(Material.DIAMOND_LEGGINGS, 750, 375, "Zırh");
        addShopItem(Material.DIAMOND_BOOTS, 500, 250, "Zırh");
        
        // DİĞER
        addShopItem(Material.TORCH, 5, 2, "Diğer");
        addShopItem(Material.BUCKET, 50, 25, "Diğer");
        addShopItem(Material.WATER_BUCKET, 75, 37, "Diğer");
        addShopItem(Material.LAVA_BUCKET, 100, 50, "Diğer");
        addShopItem(Material.ENDER_PEARL, 150, 75, "Diğer");
        addShopItem(Material.TNT, 200, 100, "Diğer");
    }
    
    private void addShopItem(Material material, double buyPrice, double sellPrice, String category) {
        shopItems.put(material, new ShopItem(material, buyPrice, sellPrice, category));
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("shop")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Bu komut sadece oyuncular tarafından kullanılabilir!");
                return true;
            }
            
            openShopMenu((Player) sender);
            return true;
        }
        
        if (command.getName().equalsIgnoreCase("sell")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Bu komut sadece oyuncular tarafından kullanılabilir!");
                return true;
            }
            
            return handleSell((Player) sender);
        }
        
        if (command.getName().equalsIgnoreCase("sellall")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Bu komut sadece oyuncular tarafından kullanılabilir!");
                return true;
            }
            
            return handleSellAll((Player) sender);
        }
        
        return false;
    }
    
    private void openShopMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "" + ChatColor.BOLD + "🛒 MAĞAZA");
        
        // Kategoriler
        ItemStack blocks = createMenuItem(Material.GRASS_BLOCK, ChatColor.GREEN + "Bloklar", 
            ChatColor.GRAY + "İnşaat malzemeleri");
        ItemStack food = createMenuItem(Material.COOKED_BEEF, ChatColor.YELLOW + "Yemek", 
            ChatColor.GRAY + "Yiyecekler");
        ItemStack tools = createMenuItem(Material.DIAMOND_PICKAXE, ChatColor.AQUA + "Aletler", 
            ChatColor.GRAY + "Kazma, balta, kürek");
        ItemStack weapons = createMenuItem(Material.DIAMOND_SWORD, ChatColor.RED + "Silahlar", 
            ChatColor.GRAY + "Kılıç, yay, ok");
        ItemStack armor = createMenuItem(Material.DIAMOND_CHESTPLATE, ChatColor.LIGHT_PURPLE + "Zırh", 
            ChatColor.GRAY + "Koruyucu ekipman");
        ItemStack other = createMenuItem(Material.ENDER_PEARL, ChatColor.WHITE + "Diğer", 
            ChatColor.GRAY + "Çeşitli eşyalar");
        
        inv.setItem(10, blocks);
        inv.setItem(12, food);
        inv.setItem(14, tools);
        inv.setItem(16, weapons);
        inv.setItem(28, armor);
        inv.setItem(30, other);
        
        // Bilgi
        ItemStack info = createMenuItem(Material.BOOK, ChatColor.GOLD + "Bilgi", 
            ChatColor.GRAY + "Elindeki eşyayı satmak için:",
            ChatColor.YELLOW + "/sell " + ChatColor.GRAY + "- Elindeki eşyayı sat",
            ChatColor.YELLOW + "/sellall " + ChatColor.GRAY + "- Tüm eşyaları sat");
        inv.setItem(49, info);
        
        player.openInventory(inv);
    }
    
    private void openCategoryMenu(Player player, String category) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "" + ChatColor.BOLD + "🛒 " + category.toUpperCase());
        
        int slot = 0;
        for (ShopItem item : shopItems.values()) {
            if (item.getCategory().equals(category) && slot < 45) {
                ItemStack displayItem = new ItemStack(item.getMaterial());
                ItemMeta meta = displayItem.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + getItemName(item.getMaterial()));
                
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.GRAY + "Alış: " + ChatColor.GREEN + economy.formatMoney(item.getBuyPrice()));
                lore.add(ChatColor.GRAY + "Satış: " + ChatColor.YELLOW + economy.formatMoney(item.getSellPrice()));
                lore.add("");
                lore.add(ChatColor.YELLOW + "Sol Tık: " + ChatColor.WHITE + "1 adet al");
                lore.add(ChatColor.YELLOW + "Sağ Tık: " + ChatColor.WHITE + "64 adet al");
                lore.add(ChatColor.YELLOW + "Shift + Sol Tık: " + ChatColor.WHITE + "1 adet sat");
                lore.add(ChatColor.YELLOW + "Shift + Sağ Tık: " + ChatColor.WHITE + "64 adet sat");
                lore.add("");
                
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
                
                inv.setItem(slot, displayItem);
                slot++;
            }
        }
        
        // Geri dön butonu
        ItemStack back = createMenuItem(Material.ARROW, ChatColor.RED + "Geri Dön", 
            ChatColor.GRAY + "Ana menüye dön");
        inv.setItem(49, back);
        
        player.openInventory(inv);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        if (!title.contains("MAĞAZA")) {
            return;
        }
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }
        
        // Ana menü
        if (title.equals(ChatColor.GOLD + "" + ChatColor.BOLD + "🛒 MAĞAZA")) {
            String itemName = clicked.getItemMeta().getDisplayName();
            
            if (itemName.contains("Bloklar")) {
                openCategoryMenu(player, "Bloklar");
            } else if (itemName.contains("Yemek")) {
                openCategoryMenu(player, "Yemek");
            } else if (itemName.contains("Aletler")) {
                openCategoryMenu(player, "Aletler");
            } else if (itemName.contains("Silahlar")) {
                openCategoryMenu(player, "Silahlar");
            } else if (itemName.contains("Zırh")) {
                openCategoryMenu(player, "Zırh");
            } else if (itemName.contains("Diğer")) {
                openCategoryMenu(player, "Diğer");
            }
            return;
        }
        
        // Kategori menüsü
        if (clicked.getItemMeta().getDisplayName().contains("Geri Dön")) {
            openShopMenu(player);
            return;
        }
        
        Material material = clicked.getType();
        ShopItem shopItem = shopItems.get(material);
        
        if (shopItem == null) {
            return;
        }
        
        boolean isShiftClick = event.isShiftClick();
        boolean isLeftClick = event.isLeftClick();
        
        int amount = (isLeftClick || !isShiftClick) ? 1 : 64;
        
        if (isShiftClick) {
            // Satış
            sellItem(player, shopItem, amount);
        } else {
            // Alış
            buyItem(player, shopItem, amount);
        }
    }
    
    private void buyItem(Player player, ShopItem item, int amount) {
        double totalPrice = item.getBuyPrice() * amount;
        
        if (!economy.hasBalance(player.getUniqueId(), totalPrice)) {
            player.sendMessage(ChatColor.RED + "✗ Yetersiz bakiye! Gereken: " + 
                ChatColor.GOLD + economy.formatMoney(totalPrice));
            return;
        }
        
        // Envanter kontrolü
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ChatColor.RED + "✗ Envanterinde yer yok!");
            return;
        }
        
        economy.removeBalance(player.getUniqueId(), totalPrice);
        player.getInventory().addItem(new ItemStack(item.getMaterial(), amount));
        
        player.sendMessage(ChatColor.GREEN + "✓ " + amount + "x " + getItemName(item.getMaterial()) + 
            " satın aldın! (" + ChatColor.GOLD + economy.formatMoney(totalPrice) + ChatColor.GREEN + ")");
    }
    
    private void sellItem(Player player, ShopItem item, int amount) {
        // Oyuncunun envanterinde bu eşyadan kaç tane var?
        int playerAmount = 0;
        for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem != null && invItem.getType() == item.getMaterial()) {
                playerAmount += invItem.getAmount();
            }
        }
        
        if (playerAmount < amount) {
            player.sendMessage(ChatColor.RED + "✗ Yeterli eşyan yok! Sende: " + playerAmount);
            return;
        }
        
        // Eşyaları kaldır
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack invItem = player.getInventory().getItem(i);
            if (invItem != null && invItem.getType() == item.getMaterial()) {
                int removeAmount = Math.min(remaining, invItem.getAmount());
                invItem.setAmount(invItem.getAmount() - removeAmount);
                remaining -= removeAmount;
                
                if (invItem.getAmount() == 0) {
                    player.getInventory().setItem(i, null);
                }
                
                if (remaining == 0) {
                    break;
                }
            }
        }
        
        double totalPrice = item.getSellPrice() * amount;
        economy.addBalance(player.getUniqueId(), totalPrice);
        
        player.sendMessage(ChatColor.GREEN + "✓ " + amount + "x " + getItemName(item.getMaterial()) + 
            " sattın! (" + ChatColor.GOLD + "+" + economy.formatMoney(totalPrice) + ChatColor.GREEN + ")");
    }
    
    private boolean handleSell(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        
        if (hand == null || hand.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "✗ Elinde satılacak bir eşya yok!");
            return true;
        }
        
        ShopItem shopItem = shopItems.get(hand.getType());
        if (shopItem == null) {
            player.sendMessage(ChatColor.RED + "✗ Bu eşya satılamaz!");
            return true;
        }
        
        int amount = hand.getAmount();
        double totalPrice = shopItem.getSellPrice() * amount;
        
        economy.addBalance(player.getUniqueId(), totalPrice);
        player.getInventory().setItemInMainHand(null);
        
        player.sendMessage(ChatColor.GREEN + "✓ " + amount + "x " + getItemName(hand.getType()) + 
            " sattın! (" + ChatColor.GOLD + "+" + economy.formatMoney(totalPrice) + ChatColor.GREEN + ")");
        
        return true;
    }
    
    private boolean handleSellAll(Player player) {
        double totalEarned = 0;
        int totalSold = 0;
        
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                ShopItem shopItem = shopItems.get(item.getType());
                if (shopItem != null) {
                    int amount = item.getAmount();
                    double price = shopItem.getSellPrice() * amount;
                    
                    totalEarned += price;
                    totalSold += amount;
                    
                    player.getInventory().setItem(i, null);
                }
            }
        }
        
        if (totalSold == 0) {
            player.sendMessage(ChatColor.RED + "✗ Satılabilir eşya bulunamadı!");
            return true;
        }
        
        economy.addBalance(player.getUniqueId(), totalEarned);
        
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "💰 TOPLU SATIŞ");
        player.sendMessage(ChatColor.GREEN + "✓ " + totalSold + " eşya satıldı!");
        player.sendMessage(ChatColor.GREEN + "✓ Kazanç: " + ChatColor.GOLD + economy.formatMoney(totalEarned));
        player.sendMessage("");
        
        return true;
    }
    
    private ItemStack createMenuItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    private String getItemName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
    
    private static class ShopItem {
        private final Material material;
        private final double buyPrice;
        private final double sellPrice;
        private final String category;
        
        public ShopItem(Material material, double buyPrice, double sellPrice, String category) {
            this.material = material;
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
            this.category = category;
        }
        
        public Material getMaterial() { return material; }
        public double getBuyPrice() { return buyPrice; }
        public double getSellPrice() { return sellPrice; }
        public String getCategory() { return category; }
    }
}
