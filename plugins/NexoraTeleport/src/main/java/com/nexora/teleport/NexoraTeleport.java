package com.nexora.teleport;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NexoraTeleport extends JavaPlugin implements Listener {
    
    private Map<UUID, TeleportRequest> pendingRequests = new HashMap<>();
    private static final long REQUEST_TIMEOUT = 60000; // 60 saniye
    
    @Override
    public void onEnable() {
        getLogger().info("Nexora Teleport Plugin aktif!");
        Bukkit.getPluginManager().registerEvents(this, this);
        
        // Süresi dolan istekleri temizle (her 10 saniyede)
        Bukkit.getScheduler().runTaskTimer(this, this::cleanExpiredRequests, 200L, 200L);
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Nexora Teleport Plugin kapatıldı!");
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        pendingRequests.remove(playerId);
        
        // Bu oyuncuya gönderilen istekleri temizle
        pendingRequests.entrySet().removeIf(entry -> 
            entry.getValue().getRequester().equals(playerId));
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Bu komut sadece oyuncular tarafından kullanılabilir!");
            return true;
        }
        
        Player player = (Player) sender;
        
        switch (command.getName().toLowerCase()) {
            case "tpa":
                return handleTpa(player, args);
            case "tpaccept":
                return handleTpAccept(player);
            case "tpdeny":
                return handleTpDeny(player);
            case "tpahere":
                return handleTpaHere(player, args);
        }
        
        return false;
    }
    
    private boolean handleTpa(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Kullanım: /tpa <oyuncu>");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "✗ Oyuncu bulunamadı!");
            return true;
        }
        
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "✗ Kendine teleport isteği gönderemezsin!");
            return true;
        }
        
        // İstek gönder
        TeleportRequest request = new TeleportRequest(
            player.getUniqueId(),
            target.getUniqueId(),
            TeleportType.TO_PLAYER,
            System.currentTimeMillis()
        );
        
        pendingRequests.put(target.getUniqueId(), request);
        
        player.sendMessage(ChatColor.GREEN + "✓ " + target.getName() + " oyuncusuna teleport isteği gönderildi!");
        
        target.sendMessage("");
        target.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "📍 TELEPORT İSTEĞİ");
        target.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " sana teleport olmak istiyor!");
        target.sendMessage(ChatColor.GREEN + "/tpaccept " + ChatColor.GRAY + "- Kabul et");
        target.sendMessage(ChatColor.RED + "/tpdeny " + ChatColor.GRAY + "- Reddet");
        target.sendMessage(ChatColor.DARK_GRAY + "(60 saniye içinde cevap ver)");
        target.sendMessage("");
        
        return true;
    }
    
    private boolean handleTpaHere(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Kullanım: /tpahere <oyuncu>");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "✗ Oyuncu bulunamadı!");
            return true;
        }
        
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "✗ Kendini çağıramazsın!");
            return true;
        }
        
        // İstek gönder
        TeleportRequest request = new TeleportRequest(
            player.getUniqueId(),
            target.getUniqueId(),
            TeleportType.TO_REQUESTER,
            System.currentTimeMillis()
        );
        
        pendingRequests.put(target.getUniqueId(), request);
        
        player.sendMessage(ChatColor.GREEN + "✓ " + target.getName() + " oyuncusuna çağrı gönderildi!");
        
        target.sendMessage("");
        target.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "📍 TELEPORT ÇAĞRISI");
        target.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " seni yanına çağırıyor!");
        target.sendMessage(ChatColor.GREEN + "/tpaccept " + ChatColor.GRAY + "- Kabul et");
        target.sendMessage(ChatColor.RED + "/tpdeny " + ChatColor.GRAY + "- Reddet");
        target.sendMessage(ChatColor.DARK_GRAY + "(60 saniye içinde cevap ver)");
        target.sendMessage("");
        
        return true;
    }
    
    private boolean handleTpAccept(Player player) {
        TeleportRequest request = pendingRequests.get(player.getUniqueId());
        
        if (request == null) {
            player.sendMessage(ChatColor.RED + "✗ Bekleyen teleport isteğin yok!");
            return true;
        }
        
        // Süre kontrolü
        if (System.currentTimeMillis() - request.getTimestamp() > REQUEST_TIMEOUT) {
            pendingRequests.remove(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "✗ Teleport isteğinin süresi doldu!");
            return true;
        }
        
        Player requester = Bukkit.getPlayer(request.getRequester());
        if (requester == null || !requester.isOnline()) {
            pendingRequests.remove(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "✗ Oyuncu artık çevrimiçi değil!");
            return true;
        }
        
        // Teleport işlemi
        Location targetLocation;
        String message;
        
        if (request.getType() == TeleportType.TO_PLAYER) {
            // İstek sahibi, kabul edene teleport olacak
            targetLocation = player.getLocation();
            message = player.getName() + " yanına teleport oldu!";
            requester.teleport(targetLocation);
            requester.sendMessage(ChatColor.GREEN + "✓ " + player.getName() + " teleport isteğini kabul etti!");
        } else {
            // Kabul eden, istek sahibine teleport olacak
            targetLocation = requester.getLocation();
            message = requester.getName() + " yanına teleport oldun!";
            player.teleport(targetLocation);
            requester.sendMessage(ChatColor.GREEN + "✓ " + player.getName() + " çağrını kabul etti!");
        }
        
        player.sendMessage(ChatColor.GREEN + "✓ " + message);
        
        pendingRequests.remove(player.getUniqueId());
        return true;
    }
    
    private boolean handleTpDeny(Player player) {
        TeleportRequest request = pendingRequests.get(player.getUniqueId());
        
        if (request == null) {
            player.sendMessage(ChatColor.RED + "✗ Bekleyen teleport isteğin yok!");
            return true;
        }
        
        Player requester = Bukkit.getPlayer(request.getRequester());
        if (requester != null && requester.isOnline()) {
            requester.sendMessage(ChatColor.RED + "✗ " + player.getName() + " teleport isteğini reddetti!");
        }
        
        player.sendMessage(ChatColor.GREEN + "✓ Teleport isteği reddedildi!");
        pendingRequests.remove(player.getUniqueId());
        
        return true;
    }
    
    private void cleanExpiredRequests() {
        long currentTime = System.currentTimeMillis();
        pendingRequests.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().getTimestamp() > REQUEST_TIMEOUT);
    }
    
    private enum TeleportType {
        TO_PLAYER,      // İstek sahibi, hedef oyuncuya gidecek
        TO_REQUESTER    // Hedef oyuncu, istek sahibine gelecek
    }
    
    private static class TeleportRequest {
        private final UUID requester;
        private final UUID target;
        private final TeleportType type;
        private final long timestamp;
        
        public TeleportRequest(UUID requester, UUID target, TeleportType type, long timestamp) {
            this.requester = requester;
            this.target = target;
            this.type = type;
            this.timestamp = timestamp;
        }
        
        public UUID getRequester() { return requester; }
        public UUID getTarget() { return target; }
        public TeleportType getType() { return type; }
        public long getTimestamp() { return timestamp; }
    }
}
