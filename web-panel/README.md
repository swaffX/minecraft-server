# Nexora Minecraft Web Panel

Ultra modern glassmorphism tasarımlı Minecraft sunucu yönetim paneli.

## Özellikler

- ✅ Discord OAuth2 girişi
- ✅ Sadece yetkili kullanıcı erişimi
- ✅ Gerçek zamanlı sunucu durumu
- ✅ Oyuncu yönetimi (Kick, Ban, OP, DEOP)
- ✅ Konsol komutları (RCON)
- ✅ Sunucu kontrolleri (Başlat/Durdur/Restart)
- ✅ Canlı log izleme (WebSocket)
- ✅ Sistem bilgileri (RAM, Disk)
- ✅ Ultra modern glassmorphism tasarım
- ✅ Animasyonlu arka plan
- ✅ Responsive tasarım

## Discord Bot Kurulumu

### 1. Discord Developer Portal

1. https://discord.com/developers/applications
2. "New Application" > İsim ver > Create
3. "OAuth2" > "General":
   - Client ID'yi kopyala
   - Client Secret oluştur ve kopyala
   - Redirects: `http://194.105.5.37:3000/auth/callback` ekle

## Kurulum

### 1. Minecraft Sunucusunda RCON'u Aktifleştir

`/opt/minecraft/server.properties` dosyasını düzenle:

```properties
enable-rcon=true
rcon.port=25575
rcon.password=güçlü_bir_şifre_buraya
```

Sunucuyu yeniden başlat:
```bash
systemctl restart minecraft
```

### 2. .env Dosyası Oluştur

```bash
cd /opt/minecraft/web-panel
cp .env.example .env
nano .env
```

İçine:
```
PORT=3000
DISCORD_CLIENT_ID=discord_client_id_buraya
DISCORD_CLIENT_SECRET=discord_client_secret_buraya
DISCORD_REDIRECT_URI=http://194.105.5.37:3000/auth/callback
SESSION_SECRET=random-secret-key-123456
RCON_PASSWORD=server.properties_deki_rcon_şifresi
```

### 3. Bağımlılıkları Kur

```bash
npm install
```

### 4. Başlat

```bash
npm start
```

Panel: http://194.105.5.37:3000

## Systemd Servisi

```bash
nano /etc/systemd/system/minecraft-panel.service
```

İçine:
```ini
[Unit]
Description=Minecraft Web Panel
After=network.target minecraft.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/minecraft/web-panel
ExecStart=/usr/bin/node server.js
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Başlat:
```bash
systemctl daemon-reload
systemctl start minecraft-panel
systemctl enable minecraft-panel
systemctl status minecraft-panel
```

## Firewall

```bash
sudo ufw allow 3000/tcp
sudo ufw allow 25575/tcp  # RCON portu
```

## Güvenlik

- Discord OAuth2 ile güvenli giriş
- Sadece belirlenen Discord ID erişebilir
- Session tabanlı kimlik doğrulama
- RCON şifresi ile güvenli komut gönderimi
- HTTPS kullanımı önerilir (Nginx reverse proxy)

## Tasarım

- Modern glassmorphism efektleri
- Animasyonlu 3D arka plan
- Grid layout oyuncu kartları
- Smooth transitions ve hover efektleri
- Responsive ve mobile-friendly
- Inter font ailesi
- Gradient renkler ve glow efektleri

## Teknolojiler

- Node.js + Express
- Discord OAuth2
- Minecraft RCON (minecraft-server-util)
- WebSocket (canlı loglar)
- Glassmorphism CSS
