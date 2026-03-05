package com.nexora.party;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NexoraParty extends JavaPlugin implements Listener {
    
    private Map<UUID, Party> parties = new ConcurrentHashMap<>();
    private Map<UUID, UUID> playerToParty = new ConcurrentHashMap<>();
    private Map<UUID, UUID> invites = new ConcurrentHashMap<>();
    private Map<UUID, ArmorStand> holograms = new ConcurrentHashMap<>();
    
    @Override
    public void onEnable() {
        getLogger().info("Nexora Party Plugin aktif!");
        
        // Event listener
        Bukkit.getPluginManager().registerEvents(this, this);
        
        // Hologram ve action bar güncelleme (her saniye)
        Bukkit.getScheduler().runTaskTimer(this, this::updateHolograms, 20L, 20L);
        Bukkit.getScheduler().runTaskTimer(this, this::updateActionBars, 20L, 20L);
    }
    
    @Override
    public void onDisable() {
        // Tüm hologramları temizle
        for (ArmorStand hologram : holograms.values()) {
            hologram.remove();
        }
        holograms.clear();
        
        getLogger().info("Nexora Party Plugin kapatıldı!");
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        createHologram(player);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Hologramı kaldır
        removeHologram(player);
        
        // Partiden çık
        UUID partyId = playerToParty.get(playerId);
        if (partyId != null) {
            Party party = parties.get(partyId);
            if (party != null) {
                party.removeMember(playerId);
                
                if (party.getLeader().equals(playerId)) {
                    // Lider ayrıldı, partiyi dağıt
                    for (UUID memberId : party.getMembers()) {
                        playerToParty.remove(memberId);
                        Player member = Bukkit.getPlayer(memberId);
                        if (member != null) {
                            member.sendMessage(ChatColor.RED + "✗ Parti lideri ayrıldı, parti dağıtıldı!");
                        }
                    }
                    parties.remove(partyId);
                } else {
                    // Üye ayrıldı
                    party.broadcast(ChatColor.YELLOW + player.getName() + " partiden ayrıldı!");
                }
            }
            playerToParty.remove(playerId);
        }
        
        // Davetleri temizle
        invites.remove(playerId);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Hologramı güncelle
        updatePlayerHologram(event.getPlayer());
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;
        
        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        
        // Aynı partide mi?
        UUID attackerPartyId = playerToParty.get(attacker.getUniqueId());
        UUID victimPartyId = playerToParty.get(victim.getUniqueId());
        
        if (attackerPartyId != null && attackerPartyId.equals(victimPartyId)) {
            event.setCancelled(true);
            attacker.sendMessage(ChatColor.RED + "✗ Parti üyelerine zarar veremezsin!");
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("party")) {
            return false;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Bu komut sadece oyuncular tarafından kullanılabilir!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
            case "oluştur":
                return handleCreate(player);
                
            case "invite":
            case "davet":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /party invite <oyuncu>");
                    return true;
                }
                return handleInvite(player, args[1]);
                
            case "accept":
            case "kabul":
                return handleAccept(player);
                
            case "leave":
            case "ayrıl":
                return handleLeave(player);
                
            case "kick":
            case "at":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /party kick <oyuncu>");
                    return true;
                }
                return handleKick(player, args[1]);
                
            case "list":
            case "liste":
                return handleList(player);
                
            default:
                sendHelp(player);
                return true;
        }
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "👥 PARTİ KOMUTLARI");
        player.sendMessage(ChatColor.YELLOW + "/party create " + ChatColor.GRAY + "- Parti oluştur");
        player.sendMessage(ChatColor.YELLOW + "/party invite <oyuncu> " + ChatColor.GRAY + "- Oyuncu davet et");
        player.sendMessage(ChatColor.YELLOW + "/party accept " + ChatColor.GRAY + "- Daveti kabul et");
        player.sendMessage(ChatColor.YELLOW + "/party leave " + ChatColor.GRAY + "- Partiden ayrıl");
        player.sendMessage(ChatColor.YELLOW + "/party kick <oyuncu> " + ChatColor.GRAY + "- Oyuncuyu at");
        player.sendMessage(ChatColor.YELLOW + "/party list " + ChatColor.GRAY + "- Parti üyelerini göster");
        player.sendMessage("");
    }
    
    private boolean handleCreate(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (playerToParty.containsKey(playerId)) {
            player.sendMessage(ChatColor.RED + "✗ Zaten bir partidesin!");
            return true;
        }
        
        UUID partyId = UUID.randomUUID();
        Party party = new Party(partyId, playerId);
        parties.put(partyId, party);
        playerToParty.put(playerId, partyId);
        
        player.sendMessage(ChatColor.GREEN + "✓ Parti oluşturuldu!");
        player.sendMessage(ChatColor.GRAY + "Oyuncu davet etmek için: " + 
            ChatColor.YELLOW + "/party invite <oyuncu>");
        
        return true;
    }
    
    private boolean handleInvite(Player player, String targetName) {
        UUID playerId = player.getUniqueId();
        UUID partyId = playerToParty.get(playerId);
        
        if (partyId == null) {
            player.sendMessage(ChatColor.RED + "✗ Bir partide değilsin! " + 
                ChatColor.YELLOW + "/party create " + ChatColor.RED + "ile parti oluştur.");
            return true;
        }
        
        Party party = parties.get(partyId);
        if (!party.getLeader().equals(playerId)) {
            player.sendMessage(ChatColor.RED + "✗ Sadece parti lideri davet edebilir!");
            return true;
        }
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "✗ Oyuncu bulunamadı!");
            return true;
        }
        
        if (target.getUniqueId().equals(playerId)) {
            player.sendMessage(ChatColor.RED + "✗ Kendini davet edemezsin!");
            return true;
        }
        
        if (playerToParty.containsKey(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "✗ Bu oyuncu zaten bir partidedir!");
            return true;
        }
        
        if (party.getMembers().size() >= 5) {
            player.sendMessage(ChatColor.RED + "✗ Parti dolu! (Maksimum 5 kişi)");
            return true;
        }
        
        invites.put(target.getUniqueId(), partyId);
        
        player.sendMessage(ChatColor.GREEN + "✓ " + target.getName() + " oyuncusuna davet gönderildi!");
        
        target.sendMessage("");
        target.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "👥 PARTİ DAVETİ");
        target.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " seni partisine davet etti!");
        target.sendMessage(ChatColor.GREEN + "/party accept " + ChatColor.GRAY + "ile kabul et");
        target.sendMessage("");
        
        return true;
    }
    
    private boolean handleAccept(Player player) {
        UUID playerId = player.getUniqueId();
        UUID partyId = invites.get(playerId);
        
        if (partyId == null) {
            player.sendMessage(ChatColor.RED + "✗ Bekleyen davetiniz yok!");
            return true;
        }
        
        Party party = parties.get(partyId);
        if (party == null) {
            player.sendMessage(ChatColor.RED + "✗ Parti artık mevcut değil!");
            invites.remove(playerId);
            return true;
        }
        
        if (party.getMembers().size() >= 5) {
            player.sendMessage(ChatColor.RED + "✗ Parti dolu!");
            invites.remove(playerId);
            return true;
        }
        
        party.addMember(playerId);
        playerToParty.put(playerId, partyId);
        invites.remove(playerId);
        
        player.sendMessage(ChatColor.GREEN + "✓ Partiye katıldın!");
        party.broadcast(ChatColor.GREEN + "✓ " + player.getName() + " partiye katıldı!");
        
        return true;
    }
    
    private boolean handleLeave(Player player) {
        UUID playerId = player.getUniqueId();
        UUID partyId = playerToParty.get(playerId);
        
        if (partyId == null) {
            player.sendMessage(ChatColor.RED + "✗ Bir partide değilsin!");
            return true;
        }
        
        Party party = parties.get(partyId);
        
        if (party.getLeader().equals(playerId)) {
            // Lider ayrılıyor, partiyi dağıt
            for (UUID memberId : party.getMembers()) {
                playerToParty.remove(memberId);
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && !member.getUniqueId().equals(playerId)) {
                    member.sendMessage(ChatColor.RED + "✗ Parti lideri ayrıldı, parti dağıtıldı!");
                }
            }
            parties.remove(partyId);
            player.sendMessage(ChatColor.GREEN + "✓ Partiden ayrıldın ve parti dağıtıldı!");
        } else {
            // Üye ayrılıyor
            party.removeMember(playerId);
            playerToParty.remove(playerId);
            player.sendMessage(ChatColor.GREEN + "✓ Partiden ayrıldın!");
            party.broadcast(ChatColor.YELLOW + player.getName() + " partiden ayrıldı!");
        }
        
        return true;
    }
    
    private boolean handleKick(Player player, String targetName) {
        UUID playerId = player.getUniqueId();
        UUID partyId = playerToParty.get(playerId);
        
        if (partyId == null) {
            player.sendMessage(ChatColor.RED + "✗ Bir partide değilsin!");
            return true;
        }
        
        Party party = parties.get(partyId);
        if (!party.getLeader().equals(playerId)) {
            player.sendMessage(ChatColor.RED + "✗ Sadece parti lideri oyuncu atabilir!");
            return true;
        }
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "✗ Oyuncu bulunamadı!");
            return true;
        }
        
        UUID targetId = target.getUniqueId();
        
        if (!party.getMembers().contains(targetId)) {
            player.sendMessage(ChatColor.RED + "✗ Bu oyuncu partinde değil!");
            return true;
        }
        
        if (targetId.equals(playerId)) {
            player.sendMessage(ChatColor.RED + "✗ Kendini atamazsın!");
            return true;
        }
        
        party.removeMember(targetId);
        playerToParty.remove(targetId);
        
        player.sendMessage(ChatColor.GREEN + "✓ " + target.getName() + " partiden atıldı!");
        target.sendMessage(ChatColor.RED + "✗ Partiden atıldın!");
        party.broadcast(ChatColor.YELLOW + target.getName() + " partiden atıldı!");
        
        return true;
    }
    
    private boolean handleList(Player player) {
        UUID playerId = player.getUniqueId();
        UUID partyId = playerToParty.get(playerId);
        
        if (partyId == null) {
            player.sendMessage(ChatColor.RED + "✗ Bir partide değilsin!");
            return true;
        }
        
        Party party = parties.get(partyId);
        
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "👥 PARTİ ÜYELERİ");
        player.sendMessage("");
        
        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                String role = memberId.equals(party.getLeader()) ? 
                    ChatColor.GOLD + "★ Lider" : ChatColor.GRAY + "Üye";
                String status = member.isOnline() ? 
                    ChatColor.GREEN + "●" : ChatColor.RED + "●";
                
                player.sendMessage(status + " " + ChatColor.WHITE + member.getName() + 
                    ChatColor.DARK_GRAY + " - " + role);
            }
        }
        
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "Toplam: " + ChatColor.YELLOW + 
            party.getMembers().size() + ChatColor.GRAY + "/5");
        player.sendMessage("");
        
        return true;
    }
    
    private void createHologram(Player player) {
        removeHologram(player);
        
        ArmorStand hologram = (ArmorStand) player.getWorld().spawnEntity(
            player.getLocation().add(0, 2.3, 0), EntityType.ARMOR_STAND);
        
        hologram.setVisible(false);
        hologram.setGravity(false);
        hologram.setCustomNameVisible(true);
        hologram.setMarker(true);
        hologram.setInvulnerable(true);
        
        holograms.put(player.getUniqueId(), hologram);
        updatePlayerHologram(player);
    }
    
    private void removeHologram(Player player) {
        ArmorStand hologram = holograms.remove(player.getUniqueId());
        if (hologram != null) {
            hologram.remove();
        }
    }
    
    private void updatePlayerHologram(Player player) {
        ArmorStand hologram = holograms.get(player.getUniqueId());
        if (hologram == null || !hologram.isValid()) {
            createHologram(player);
            hologram = holograms.get(player.getUniqueId());
        }
        
        if (hologram != null) {
            hologram.teleport(player.getLocation().add(0, 2.5, 0));
            
            UUID partyId = playerToParty.get(player.getUniqueId());
            if (partyId != null) {
                Party party = parties.get(partyId);
                if (party != null) {
                    boolean isLeader = party.getLeader().equals(player.getUniqueId());
                    String text = isLeader ? 
                        ChatColor.GOLD + "★ " + ChatColor.YELLOW + player.getName() :
                        ChatColor.GREEN + "✓ " + ChatColor.AQUA + player.getName();
                    hologram.setCustomName(text);
                    hologram.setCustomNameVisible(true);
                    hologram.setGlowing(true);
                } else {
                    hologram.setCustomName("");
                    hologram.setCustomNameVisible(false);
                    hologram.setGlowing(false);
                }
            } else {
                hologram.setCustomName("");
                hologram.setCustomNameVisible(false);
                hologram.setGlowing(false);
            }
        }
    }
    
    private void updateHolograms() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerHologram(player);
        }
    }
    
    private void updateActionBars() {
        for (UUID partyId : parties.keySet()) {
            Party party = parties.get(partyId);
            if (party == null) continue;
            
            for (UUID memberId : party.getMembers()) {
                Player player = Bukkit.getPlayer(memberId);
                if (player == null || !player.isOnline()) continue;
                
                // Parti üyelerinin mesafelerini hesapla
                StringBuilder actionBar = new StringBuilder();
                actionBar.append(ChatColor.GOLD).append("👥 Parti: ");
                
                boolean first = true;
                for (UUID otherMemberId : party.getMembers()) {
                    if (otherMemberId.equals(memberId)) continue;
                    
                    Player otherMember = Bukkit.getPlayer(otherMemberId);
                    if (otherMember == null || !otherMember.isOnline()) continue;
                    
                    if (!first) {
                        actionBar.append(ChatColor.DARK_GRAY).append(" | ");
                    }
                    first = false;
                    
                    // Mesafe hesapla
                    double distance = player.getLocation().distance(otherMember.getLocation());
                    String distanceColor;
                    if (distance < 10) {
                        distanceColor = ChatColor.GREEN + "";
                    } else if (distance < 50) {
                        distanceColor = ChatColor.YELLOW + "";
                    } else {
                        distanceColor = ChatColor.RED + "";
                    }
                    
                    // Yön hesapla (pusula)
                    String direction = getDirection(player.getLocation(), otherMember.getLocation());
                    
                    actionBar.append(ChatColor.WHITE).append(otherMember.getName())
                            .append(" ").append(distanceColor).append(String.format("%.0fm", distance))
                            .append(" ").append(direction);
                }
                
                // Action bar gönder
                player.sendActionBar(Component.text(actionBar.toString()));
            }
        }
    }
    
    private String getDirection(Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double angle = Math.toDegrees(Math.atan2(dz, dx)) - 90;
        
        if (angle < 0) {
            angle += 360;
        }
        
        // 8 yön
        if (angle >= 337.5 || angle < 22.5) {
            return ChatColor.AQUA + "↑"; // Kuzey
        } else if (angle >= 22.5 && angle < 67.5) {
            return ChatColor.AQUA + "↗"; // Kuzeydoğu
        } else if (angle >= 67.5 && angle < 112.5) {
            return ChatColor.AQUA + "→"; // Doğu
        } else if (angle >= 112.5 && angle < 157.5) {
            return ChatColor.AQUA + "↘"; // Güneydoğu
        } else if (angle >= 157.5 && angle < 202.5) {
            return ChatColor.AQUA + "↓"; // Güney
        } else if (angle >= 202.5 && angle < 247.5) {
            return ChatColor.AQUA + "↙"; // Güneybatı
        } else if (angle >= 247.5 && angle < 292.5) {
            return ChatColor.AQUA + "←"; // Batı
        } else {
            return ChatColor.AQUA + "↖"; // Kuzeybatı
        }
    }
    
    private static class Party {
        private final UUID id;
        private final UUID leader;
        private final Set<UUID> members;
        
        public Party(UUID id, UUID leader) {
            this.id = id;
            this.leader = leader;
            this.members = new HashSet<>();
            this.members.add(leader);
        }
        
        public UUID getId() { return id; }
        public UUID getLeader() { return leader; }
        public Set<UUID> getMembers() { return members; }
        
        public void addMember(UUID playerId) {
            members.add(playerId);
        }
        
        public void removeMember(UUID playerId) {
            members.remove(playerId);
        }
        
        public void broadcast(String message) {
            for (UUID memberId : members) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null) {
                    member.sendMessage(message);
                }
            }
        }
    }
}
