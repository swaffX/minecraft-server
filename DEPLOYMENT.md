# 🚀 VPS Deployment Guide

Bu rehber, tüm pluginleri VPS'e deploy etmek için adım adım talimatlar içerir.

## 📋 Ön Hazırlık

### 1. Yerel Değişiklikleri Commit Et
```bash
cd minecraft-server-new
git add .
git commit -m "Tüm pluginler tamamlandı: Shop, Daily, Achievements, Party, Announcer"
git push origin main
```

## 🔧 VPS'de Deployment

### 2. VPS'e Bağlan
```bash
ssh root@194.105.5.37
```

### 3. Sunucuyu Durdur
```bash
systemctl stop minecraft
```

### 4. Güncellemeleri Çek
```bash
cd /opt/minecraft
git pull origin main
```

### 5. Tüm Pluginleri Derle

#### NexoraShop
```bash
cd /opt/minecraft/plugins/NexoraShop
mvn clean package
cp target/NexoraShop-1.0.0.jar /opt/minecraft/plugins/
```

#### NexoraDaily
```bash
cd /opt/minecraft/plugins/NexoraDaily
mvn clean package
cp target/NexoraDaily-1.0.0.jar /opt/minecraft/plugins/
```

#### NexoraAchievements
```bash
cd /opt/minecraft/plugins/NexoraAchievements
mvn clean package
cp target/NexoraAchievements-1.0.0.jar /opt/minecraft/plugins/
```

#### NexoraParty
```bash
cd /opt/minecraft/plugins/NexoraParty
mvn clean package
cp target/NexoraParty-1.0.0.jar /opt/minecraft/plugins/
```

#### NexoraAnnouncer
```bash
cd /opt/minecraft/plugins/NexoraAnnouncer
mvn clean package
cp target/NexoraAnnouncer-1.0.0.jar /opt/minecraft/plugins/
```

#### Mevcut Pluginleri Güncelle
```bash
cd /opt/minecraft/plugins/NexoraInfo
mvn clean package
cp target/NexoraInfo-1.0.0.jar /opt/minecraft/plugins/

cd /opt/minecraft/plugins/NexoraChat
mvn clean package
cp target/NexoraChat-1.0.0.jar /opt/minecraft/plugins/

cd /opt/minecraft/plugins/NexoraKits
mvn clean package
cp target/NexoraKits-1.0.0.jar /opt/minecraft/plugins/

cd /opt/minecraft/plugins/NexoraRanks
mvn clean package
cp target/NexoraRanks-1.0.0.jar /opt/minecraft/plugins/

cd /opt/minecraft/plugins/NexoraEconomy
mvn clean package
cp target/NexoraEconomy-1.0.0.jar /opt/minecraft/plugins/
```

### 6. Discord Bot Güncelle
```bash
cd /opt/minecraft/discord-bot
npm install
systemctl restart minecraft-bot
systemctl status minecraft-bot
```

### 7. Sunucuyu Başlat
```bash
systemctl start minecraft
systemctl status minecraft
```

### 8. Logları Kontrol Et
```bash
tail -f /opt/minecraft/logs/latest.log
```

## ✅ Kontrol Listesi

Plugin yükleme kontrolü:
```bash
# Minecraft konsolunda
plugins
```

Görmemiz gerekenler:
- ✅ NexoraInfo
- ✅ NexoraChat
- ✅ NexoraKits
- ✅ NexoraRanks
- ✅ NexoraEconomy
- ✅ NexoraShop
- ✅ NexoraDaily
- ✅ NexoraAchievements
- ✅ NexoraParty
- ✅ NexoraAnnouncer

## 🧪 Test Komutları

Oyun içinde test et:
```
/balance
/shop
/quests
/achievements
/party create
/rank
```

Discord'da test et:
```
/stats oyuncu:OyuncuAdın
```

## 🔄 Hızlı Güncelleme Scripti

Gelecekteki güncellemeler için:

```bash
#!/bin/bash
# update-plugins.sh

cd /opt/minecraft
systemctl stop minecraft

git pull origin main

# Tüm pluginleri derle
for plugin in NexoraInfo NexoraChat NexoraKits NexoraRanks NexoraEconomy NexoraShop NexoraDaily NexoraAchievements NexoraParty NexoraAnnouncer; do
    echo "Derleniyor: $plugin"
    cd /opt/minecraft/plugins/$plugin
    mvn clean package -q
    cp target/*.jar /opt/minecraft/plugins/
done

cd /opt/minecraft/discord-bot
npm install --silent

systemctl restart minecraft-bot
systemctl start minecraft

echo "✅ Tüm pluginler güncellendi!"
```

Kullanım:
```bash
chmod +x update-plugins.sh
./update-plugins.sh
```

## 🐛 Sorun Giderme

### Plugin yüklenmiyor
```bash
# Log kontrolü
tail -f /opt/minecraft/logs/latest.log | grep -i error

# Plugin klasörü kontrolü
ls -la /opt/minecraft/plugins/

# İzinleri düzelt
chmod 644 /opt/minecraft/plugins/*.jar
```

### Discord bot çalışmıyor
```bash
# Bot durumu
systemctl status minecraft-bot

# Bot logları
journalctl -u minecraft-bot -f

# .env dosyası kontrolü
cat /opt/minecraft/discord-bot/.env
```

### Ekonomi çalışmıyor
```bash
# NexoraEconomy'nin yüklendiğinden emin ol
# Diğer pluginler NexoraEconomy'ye bağımlı

# Veri dosyası kontrolü
ls -la /opt/minecraft/plugins/NexoraEconomy/balances.json
```

## 📊 Performans İzleme

```bash
# CPU ve RAM kullanımı
htop

# Sunucu TPS
# Oyun içinde: /tps

# Disk kullanımı
df -h

# Log boyutu
du -sh /opt/minecraft/logs/
```

## 🔐 Güvenlik

### Firewall Kuralları
```bash
# Sadece gerekli portlar açık olmalı
ufw status

# Minecraft: 25565
# RCON: 25575 (sadece localhost)
# Web Panel: 3000 (opsiyonel)
```

### Yedekleme
```bash
# Düzenli yedekleme scripti
#!/bin/bash
# backup.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/opt/backups"

mkdir -p $BACKUP_DIR

# World yedekle
tar -czf $BACKUP_DIR/world_$DATE.tar.gz /opt/minecraft/world/

# Plugin verileri yedekle
tar -czf $BACKUP_DIR/plugin_data_$DATE.tar.gz /opt/minecraft/plugins/*/

# Eski yedekleri temizle (30 günden eski)
find $BACKUP_DIR -name "*.tar.gz" -mtime +30 -delete

echo "✅ Yedekleme tamamlandı: $DATE"
```

Cron job ekle:
```bash
crontab -e
# Her gün saat 03:00'da yedekle
0 3 * * * /opt/minecraft/backup.sh
```

## 📞 Destek

Sorun yaşarsan:
1. Logları kontrol et
2. Plugin bağımlılıklarını kontrol et
3. Sunucu versiyonunu kontrol et (Paper MC 1.21.1)
4. Java versiyonunu kontrol et (Java 17+)

## 🎉 Başarılı Deployment!

Tüm adımları tamamladıysan, sunucun artık tüm özelliklerle çalışıyor olmalı!

Test et ve keyfini çıkar! 🎮
