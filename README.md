# 🎮 Nexora Minecraft Server

Modern ve özelleştirilmiş Minecraft sunucu projesi. Custom pluginler, Discord botu ve web paneli içerir.

## 🌍 Sunucu Bilgileri

- **IP:** 194.105.5.37
- **Port:** 25565
- **Versiyon:** Paper MC 1.21.1
- **Oyun Modu:** Survival

## 📦 Pluginler

### NexoraInfo
Scoreboard ve TAB list sistemi.
- Oyuncu sayısı, dünya günü, koordinatlar, ping
- Modern TAB list tasarımı
- Hoşgeldin mesajları

### NexoraChat
Gelişmiş sohbet sistemi.
- Rütbe tabanlı chat formatı
- Anti-spam sistemi (1 saniye cooldown)
- Renk kodu desteği (&c, &a, &b)
- `/clearchat`, `/mutechat` komutları

### NexoraKits
Başlangıç kit sistemi.
- İlk girişte otomatik starter kit
- Taş aletler, yemek, odun, meşale

### NexoraRanks
Otomatik rütbe sistemi.
- Oyun süresine göre otomatik rütbe
- ROOKIE (0-5h), WARRIOR (5h+), CHAMPION (20h+), MASTER (50h+), LEGEND (100h+)
- Manuel rütbeler: DEVELOPER, VIP
- LuckPerms entegrasyonu
- `/rank`, `/setrank` komutları

### NexoraEconomy
Ekonomi sistemi.
- Başlangıç parası: 1000₺
- Günlük ödül: 500₺ (24 saat cooldown)
- `/balance`, `/pay`, `/baltop`, `/eco`, `/daily`
- JSON veri saklama
- API diğer pluginler için

### NexoraShop
GUI mağaza sistemi.
- 6 kategori: Bloklar, Yemek, Aletler, Silahlar, Zırh, Diğer
- Alış/satış fiyatları
- Toplu alım/satım desteği
- `/shop`, `/sell`, `/sellall`
- Ekonomi entegrasyonu

### NexoraDaily
Günlük görev sistemi.
- Her gün 3 rastgele görev
- Görev tipleri: Blok kırma, Mob öldürme
- Ekonomi ödülleri (200-1000₺)
- Tüm görevleri tamamlayınca bonus: 500₺
- Otomatik günlük sıfırlama (00:00)
- `/quests` komutu

### NexoraAchievements
Başarı sistemi.
- 17 farklı başarı
- 4 kategori: Özel, Madencilik, Savaş, Ekonomi
- Otomatik başarı takibi
- Ekonomi ödülleri (100-5000₺)
- Sunucu geneli duyurular
- `/achievements` komutu

### NexoraParty
Parti sistemi.
- Maksimum 5 kişilik partiler
- PvP koruması (parti üyeleri birbirine zarar veremez)
- Hologram göstergesi (kafanın üstünde)
- Lider ve üye rolleri
- `/party create|invite|accept|leave|kick|list`

### NexoraAnnouncer
Otomatik duyuru sistemi.
- Her 5 dakikada bir duyuru
- Komutlar hakkında bilgilendirme
- Sunucu özellikleri tanıtımı
- 12 farklı duyuru mesajı

## 🤖 Discord Bot

- Sunucu durumu gösterimi (otomatik güncelleme)
- Oyuncu sayısı, ping, uptime
- `/stats` komutu ile oyuncu istatistikleri
- Discord-Minecraft hesap bağlama
- Oyuncu avatarı gösterimi
- RCON entegrasyonu

## 🌐 Web Panel

- Discord OAuth2 girişi (sadece yetkili kullanıcı)
- Gerçek zamanlı sunucu durumu
- Oyuncu yönetimi (Kick/Ban/OP)
- Sunucu kontrolleri (Start/Stop/Restart)
- Canlı log görüntüleme (WebSocket)
- Hızlı komutlar
- Sunucu ayarları editörü
- Sistem monitörü (CPU, RAM, Disk)
- Performans grafikleri (Chart.js)
- Modern glassmorphism tasarım

## 🛠️ Kurulum

### Gereksinimler
- Java 17+
- Maven
- Node.js 18+
- Paper MC 1.21.1

### Plugin Derleme
```bash
cd plugins/PluginAdı
mvn clean package
cp target/PluginAdı-1.0.0.jar /opt/minecraft/plugins/
```

### Discord Bot
```bash
cd discord-bot
npm install
# .env dosyasını düzenle
npm start
```

### Web Panel
```bash
cd web-panel
npm install
# .env dosyasını düzenle
npm start
```

## 📝 Komutlar

### Ekonomi
- `/balance [oyuncu]` - Bakiye göster
- `/pay <oyuncu> <miktar>` - Para gönder
- `/baltop` - En zenginler listesi
- `/daily` - Günlük ödül al
- `/eco <give|take|set> <oyuncu> <miktar>` - Admin komutu

### Mağaza
- `/shop` - Mağazayı aç
- `/sell` - Elindeki eşyayı sat
- `/sellall` - Tüm satılabilir eşyaları sat

### Görevler & Başarılar
- `/quests` - Günlük görevleri göster
- `/achievements` - Başarıları göster

### Parti
- `/party create` - Parti oluştur
- `/party invite <oyuncu>` - Oyuncu davet et
- `/party accept` - Daveti kabul et
- `/party leave` - Partiden ayrıl
- `/party kick <oyuncu>` - Oyuncuyu at (lider)
- `/party list` - Parti üyelerini göster

### Chat
- `/clearchat` - Sohbeti temizle
- `/mutechat` - Sohbeti kapat/aç

### Rütbe
- `/rank` - Rütbeni göster
- `/setrank <oyuncu> <rütbe>` - Rütbe ver (Admin)

## 🔧 Yapılandırma

### server.properties
```properties
enable-rcon=true
rcon.port=25575
rcon.password=your_password
```

### Discord Bot .env
```env
DISCORD_BOT_TOKEN=your_token
RCON_PASSWORD=your_password
```

### Web Panel .env
```env
DISCORD_CLIENT_ID=your_client_id
DISCORD_CLIENT_SECRET=your_secret
DISCORD_REDIRECT_URI=http://your-ip:3000/auth/callback
SESSION_SECRET=random_string
RCON_HOST=194.105.5.37
RCON_PORT=25575
RCON_PASSWORD=your_password
ALLOWED_USER_ID=315875588906680330
```

## 📊 Özellikler

- ✅ Otomatik rütbe sistemi (oyun süresine göre)
- ✅ Ekonomi sistemi (Türk Lirası ₺)
- ✅ Günlük görevler (rastgele 3 görev)
- ✅ Başarı sistemi (17 başarı)
- ✅ Parti sistemi (PvP koruması + hologramlar)
- ✅ GUI mağaza (6 kategori)
- ✅ Discord entegrasyonu (/stats komutu)
- ✅ Web panel (Discord OAuth2)
- ✅ Anti-spam (1 saniye cooldown)
- ✅ Renk kodu desteği
- ✅ Otomatik duyurular (5 dakikada bir)
- ✅ Canlı loglar (WebSocket)
- ✅ Performans grafikleri

## 🚀 VPS Güncelleme

```bash
cd /opt/minecraft
git pull

# Tüm pluginleri derle ve kopyala
cd plugins/NexoraInfo && mvn clean package && cp target/*.jar /opt/minecraft/plugins/
cd ../NexoraChat && mvn clean package && cp target/*.jar /opt/minecraft/plugins/
cd ../NexoraKits && mvn clean package && cp target/*.jar /opt/minecraft/plugins/
cd ../NexoraRanks && mvn clean package && cp target/*.jar /opt/minecraft/plugins/
cd ../NexoraEconomy && mvn clean package && cp target/*.jar /opt/minecraft/plugins/
cd ../NexoraShop && mvn clean package && cp target/*.jar /opt/minecraft/plugins/
cd ../NexoraDaily && mvn clean package && cp target/*.jar /opt/minecraft/plugins/
cd ../NexoraAchievements && mvn clean package && cp target/*.jar /opt/minecraft/plugins/
cd ../NexoraParty && mvn clean package && cp target/*.jar /opt/minecraft/plugins/
cd ../NexoraAnnouncer && mvn clean package && cp target/*.jar /opt/minecraft/plugins/

# Sunucuyu yeniden başlat
systemctl restart minecraft

# Discord bot güncelle
cd /opt/minecraft/discord-bot
npm install
systemctl restart minecraft-bot

# Web panel güncelle
cd /opt/minecraft/web-panel
npm install
systemctl restart minecraft-panel
```

## 📄 Lisans

Bu proje özel kullanım içindir.

## 👨‍💻 Geliştirici

Nexora Server Team
