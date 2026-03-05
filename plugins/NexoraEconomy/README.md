# NexoraEconomy - Para Sistemi

## Özellikler
- ✅ Oyuncu bakiye sistemi
- ✅ Para transferi
- ✅ Günlük ödül sistemi
- ✅ En zenginler listesi
- ✅ Admin para yönetimi
- ✅ JSON ile veri saklama
- ✅ Otomatik kaydetme

## Komutlar

### Oyuncu Komutları
- `/balance` veya `/bal` - Bakiyeni göster
- `/balance <oyuncu>` - Oyuncunun bakiyesini göster
- `/pay <oyuncu> <miktar>` - Para gönder
- `/baltop` - En zenginler listesi (Top 10)
- `/daily` - Günlük ödül al (500₺, 24 saat cooldown)

### Admin Komutları
- `/eco give <oyuncu> <miktar>` - Para ver
- `/eco take <oyuncu> <miktar>` - Para al
- `/eco set <oyuncu> <miktar>` - Bakiye ayarla

## Özellikler

### Başlangıç Parası
- İlk giriş: 1000₺

### Günlük Ödül
- Miktar: 500₺
- Cooldown: 24 saat

### Para Formatı
- Türk Lirası (₺) sembolü
- Örnek: 1,500.00₺

## API Kullanımı

Diğer pluginler için:

```java
NexoraEconomy economy = NexoraEconomy.getInstance();

// Bakiye kontrol
double balance = economy.getBalance(player.getUniqueId());

// Para ekle
economy.addBalance(player.getUniqueId(), 100.0);

// Para çıkar
economy.removeBalance(player.getUniqueId(), 50.0);

// Yeterli para var mı?
boolean hasEnough = economy.hasBalance(player.getUniqueId(), 100.0);

// Para formatla
String formatted = economy.formatMoney(1500.0); // "1500.00₺"
```

## Veri Saklama
- Dosya: `plugins/NexoraEconomy/balances.json`
- Format: JSON
- Otomatik kaydetme: Her 5 dakikada
- Manuel kaydetme: Sunucu kapanışında

## Kurulum
1. JAR dosyasını plugins klasörüne at
2. Sunucuyu başlat
3. Oyuncular otomatik olarak 1000₺ ile başlar
