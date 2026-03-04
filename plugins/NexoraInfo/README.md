# Nexora Info Plugin

Minecraft sunucusu için özel bilgi paneli ve hoşgeldin mesajları plugini.

## Özellikler

### 📊 Scoreboard (Sağ Panel)
- 👥 Çevrimiçi oyuncu sayısı
- 📅 Dünya günü
- 🕐 Zaman dilimi (Sabah/Öğle/Akşam/Gece)
- 📍 Oyuncu koordinatları
- 📶 Ping bilgisi
- 🌐 Sunucu IP

### 🎉 Hoşgeldin Mesajları
- Ekranda beliren title mesajı
- Chat'te hoşgeldin mesajı
- Renkli ve modern tasarım

## Kurulum

### 1. Plugin'i Derle

```bash
cd plugins/NexoraInfo
mvn clean package
```

### 2. JAR Dosyasını Kopyala

```bash
cp target/NexoraInfo-1.0.0.jar /opt/minecraft/plugins/
```

### 3. Plugin'i Yükle

Sunucuda:
```bash
# Oyun içinde (OP olarak)
/reload confirm

# Veya RCON ile
rcon-cli reload confirm
```

## Özelleştirme

`NexoraInfo.java` dosyasını düzenleyerek:
- Renkleri değiştirebilirsin
- Yeni bilgiler ekleyebilirsin
- Hoşgeldin mesajını özelleştirebilirsin

## Gereksinimler

- Paper/Spigot 1.21+
- Java 21+
- Maven 3.6+

## Komutlar

Plugin otomatik çalışır, komut gerektirmez.

## İzinler

Özel izin gerektirmez, tüm oyuncular görebilir.
