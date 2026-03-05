# NexoraRanks - Rütbe Sistemi

## Özellikler
- ✅ Oyun süresine göre otomatik rütbe
- ✅ Manuel rütbe verme
- ✅ Rütbe bilgisi görüntüleme
- ✅ Chat entegrasyonu (NexoraChat ile)

## Komutlar
- `/rank` - Kendi rütbeni göster
- `/rank <oyuncu>` - Oyuncunun rütbesini göster
- `/setrank <oyuncu> <rütbe>` - Manuel rütbe ver (OP)

## Otomatik Rütbeler (Oyun Süresine Göre)
1. **ROOKIE** - 0-5 saat (Gri)
2. **WARRIOR** - 5+ saat (Yeşil, Kalın)
3. **CHAMPION** - 20+ saat (Mavi, Kalın)
4. **MASTER** - 50+ saat (Mor, Kalın)
5. **LEGEND** - 100+ saat (Açık Mor, Kalın)

## Manuel Rütbeler
- **DEVELOPER** - Kırmızı koyu, kalın (Geliştirici)
- **VIP** - Aqua, kalın

## Yetkiler
- `nexoraranks.setrank` - Manuel rütbe verme (varsayılan: op)
- `nexora.developer` - Developer rütbesi
- `nexora.vip` - VIP rütbesi

## Kurulum
1. JAR dosyasını plugins klasörüne at
2. LuckPerms yükle (yetki yönetimi için)
3. Sunucuyu yeniden başlat
4. Rütbeler otomatik olarak verilir

## Rütbe Verme
```
/setrank <oyuncu> developer
/setrank <oyuncu> vip
```

## Not
Manuel rütbe verilen oyunculara otomatik rütbe verilmez.
