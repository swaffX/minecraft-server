# Minecraft Web Panel

Modern ve güvenli Minecraft sunucu yönetim paneli.

## Özellikler

- ✅ Gerçek zamanlı sunucu durumu
- ✅ Oyuncu listesi ve avatarları
- ✅ Sunucu kontrolleri (Başlat/Durdur/Restart)
- ✅ Canlı log izleme (WebSocket)
- ✅ Sistem bilgileri (RAM, Disk)
- ✅ Güvenli giriş sistemi (bcrypt)
- ✅ Modern ve responsive tasarım

## Kurulum

### 1. Şifre Hash Oluştur

```bash
node -e "const bcrypt = require('bcrypt'); bcrypt.hash('ŞİFRENİZ', 10, (err, hash) => console.log(hash));"
```

### 2. .env Dosyası Oluştur

```bash
cp .env.example .env
nano .env
```

İçine:
```
PORT=3000
ADMIN_USERNAME=admin
ADMIN_PASSWORD_HASH=yukarıdaki_hash
SESSION_SECRET=random-secret-key-123456
```

### 3. Bağımlılıkları Kur

```bash
npm install
```

### 4. Başlat

```bash
npm start
```

Panel: http://VPS-IP:3000

## Systemd Servisi

```bash
nano /etc/systemd/system/minecraft-panel.service
```

İçine:
```ini
[Unit]
Description=Minecraft Web Panel
After=network.target

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
```

## Firewall

```bash
sudo ufw allow 3000/tcp
```

## Güvenlik

- Sadece güvenilir IP'lerden erişim sağlayın
- Güçlü şifre kullanın
- HTTPS kullanın (Nginx reverse proxy önerilir)
