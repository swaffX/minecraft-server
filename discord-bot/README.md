# Minecraft Discord Bot

## Kurulum

### 1. Discord Bot Oluştur

1. https://discord.com/developers/applications adresine git
2. "New Application" > İsim ver > Create
3. Sol menüden "Bot" > "Add Bot"
4. "Reset Token" > Token'ı kopyala
5. "Privileged Gateway Intents" > Hepsini aç
6. "OAuth2" > "URL Generator":
   - Scopes: `bot`
   - Bot Permissions: `Send Messages`, `Embed Links`, `Read Message History`
7. Oluşan URL'yi tarayıcıda aç ve sunucuna ekle

### 2. VPS'de Kur

```bash
cd /opt
git clone https://github.com/swaffX/minecraft-server.git minecraft-discord-bot
cd minecraft-discord-bot

# Bot dosyalarını kopyala
npm install

# Config düzenle
nano bot.js
# BOT_TOKEN kısmına Discord bot token'ını yapıştır

# Başlat
node bot.js
```

### 3. Systemd Servisi (Otomatik Başlatma)

```bash
nano /etc/systemd/system/minecraft-bot.service
```

İçine:
```ini
[Unit]
Description=Minecraft Discord Bot
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/minecraft-discord-bot
ExecStart=/usr/bin/node bot.js
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Başlat:
```bash
systemctl daemon-reload
systemctl start minecraft-bot
systemctl enable minecraft-bot
systemctl status minecraft-bot
```

## Özellikler

- ✅ Sunucu bilgilerini otomatik günceller (30 saniyede bir)
- ✅ Oyuncu giriş/çıkış logları
- ✅ Sunucu durumu (online/offline)
- ✅ Oyuncu sayısı, ping, uptime
