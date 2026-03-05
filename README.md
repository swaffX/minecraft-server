# 🎮 Nexora Minecraft Server

Modern ve özelleştirilmiş Minecraft sunucu projesi.

## 📦 İçerik

### Pluginler
- **NexoraInfo** - Scoreboard ve TAB listesi
- **NexoraChat** - Gelişmiş chat sistemi
- **NexoraKits** - Başlangıç kit sistemi
- **NexoraRanks** - Otomatik rütbe sistemi

### Discord Bot
- Sunucu durumu gösterimi
- Oyuncu sayısı takibi
- Otomatik güncelleme

### Web Panel
- Discord OAuth2 girişi
- Sunucu kontrolleri (Start/Stop/Restart)
- Oyuncu yönetimi (Kick/Ban/OP)
- Canlı loglar
- Sistem monitörü
- Performans grafikleri

## 🚀 Kurulum

### Gereksinimler
- Java 21
- Maven
- Node.js 18+
- Paper MC 1.21.1

### Pluginleri Derleme

```bash
# Her plugin için:
cd plugins/[PluginAdı]
mvn clean package
cp target/[PluginAdı]-1.0.0.jar /opt/minecraft/plugins/
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

## 📝 Lisans

Bu proje özel kullanım içindir.

## 👤 Yazar

Nexora Team
