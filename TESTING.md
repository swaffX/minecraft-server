# 🧪 Testing Guide

Bu rehber, tüm yeni özellikleri test etmek için adım adım talimatlar içerir.

## 🎯 Test Senaryoları

### 1. Ekonomi Sistemi Testi

#### Başlangıç
```
1. Sunucuya ilk kez katıl
2. Hoşgeldin mesajını gör
3. /balance → 1000₺ olmalı
```

#### Günlük Ödül
```
1. /daily → 500₺ al
2. /balance → 1500₺ olmalı
3. /daily tekrar → "Zaten aldın" mesajı
```

#### Para Transferi
```
1. İkinci oyuncu ile test et
2. /pay OyuncuAdı 100
3. Her iki oyuncunun bakiyesini kontrol et
```

#### En Zenginler
```
1. /baltop
2. Sıralı liste görmeli
3. Kendi sıran görünmeli
```

### 2. Mağaza Sistemi Testi

#### GUI Mağaza
```
1. /shop
2. Kategorileri gör (6 kategori)
3. Bir kategoriye tıkla
4. Eşya bilgilerini gör (alış/satış fiyatı)
```

#### Alış İşlemi
```
1. Sol tık → 1 adet al
2. Sağ tık → 64 adet al
3. Bakiye azalmalı
4. Envantere eşya gelmeli
```

#### Satış İşlemi
```
1. Shift + Sol tık → 1 adet sat
2. Shift + Sağ tık → 64 adet sat
3. Bakiye artmalı
4. Envanterden eşya gitmeli
```

#### Hızlı Satış
```
1. Elinde satılabilir eşya tut
2. /sell → Elindeki sat
3. /sellall → Tüm envanteri sat
```

### 3. Günlük Görevler Testi

#### Görev Görüntüleme
```
1. /quests
2. 3 görev görmeli
3. Her görevin ilerlemesi 0/hedef olmalı
```

#### Görev Tamamlama
```
1. Blok kırma görevi varsa → Blok kır
2. Mob öldürme görevi varsa → Mob öldür
3. İlerleme mesajı görmeli
4. Görev tamamlanınca ödül al
```

#### Tüm Görevler
```
1. 3 görevi de tamamla
2. 500₺ bonus al
3. Yarın yeni görevler gelecek
```

### 4. Başarı Sistemi Testi

#### Başarı Görüntüleme
```
1. /achievements
2. 17 başarı görmeli
3. Kategorilere ayrılmış olmalı
4. Açılan/açılmayan durumu görmeli
```

#### Otomatik Başarılar
```
1. İlk katılım → "İlk Adım" başarısı
2. 100 blok kır → "Madenci" başarısı
3. 10 mob öldür → "Savaşçı" başarısı
4. 10000₺ biriktir → "Zengin" başarısı
```

#### Başarı Duyurusu
```
1. Başarı açıldığında
2. Sunucu geneli duyuru görmeli
3. Ödül otomatik verilmeli
```

### 5. Parti Sistemi Testi

#### Parti Oluşturma
```
1. /party create
2. "Parti oluşturuldu" mesajı
3. Kafanın üstünde "★ Parti Lideri" hologramı
```

#### Davet Etme
```
1. /party invite OyuncuAdı
2. Davet gönderildi mesajı
3. Diğer oyuncu davet mesajı almalı
```

#### Daveti Kabul Etme
```
1. Davet alan oyuncu: /party accept
2. "Partiye katıldın" mesajı
3. Kafanın üstünde "✓ Parti Üyesi" hologramı
4. Parti üyeleri birbirinin hologramını görmeli
```

#### PvP Koruması
```
1. Parti üyesi olarak birbirine vur
2. Hasar gitmemeli
3. "Parti üyelerine zarar veremezsin" mesajı
```

#### Parti Listesi
```
1. /party list
2. Tüm üyeleri gör
3. Lider işaretli olmalı
4. Çevrimiçi durumu görmeli
```

#### Partiden Ayrılma
```
1. Üye: /party leave → Sadece kendisi ayrılır
2. Lider: /party leave → Parti dağılır
```

### 6. Otomatik Duyurular Testi

#### Duyuru Sistemi
```
1. Sunucuda bekle
2. 5 dakikada bir duyuru görmeli
3. Farklı duyurular sırayla gelmeli
4. Komutlar hakkında bilgi içermeli
```

#### Duyuru İçeriği
```
- /daily komutu
- /shop komutu
- /quests komutu
- /achievements komutu
- /party komutu
- Sunucu IP
- Renk kodları
- Rütbe sistemi
```

### 7. Discord Bot Testi

#### /stats Komutu
```
1. Discord'da /stats oyuncu:OyuncuAdın
2. İstatistik kartı görmeli
3. Oyuncu avatarı görmeli
4. Çevrimiçi/çevrimdışı durumu görmeli
```

#### Hesap Bağlama
```
1. İlk kullanımda oyuncu adı iste
2. Sonraki kullanımlarda otomatik
3. /stats (oyuncu adı olmadan)
```

### 8. Entegrasyon Testi

#### Ekonomi + Mağaza
```
1. Para kazan (görevler/başarılar)
2. Mağazadan eşya al
3. Eşya sat
4. Bakiye değişimlerini kontrol et
```

#### Görevler + Başarılar
```
1. Görev tamamla
2. İlgili başarı açılmalı
3. Her ikisinden de ödül al
```

#### Parti + PvP
```
1. Parti oluştur
2. Arkadaşını davet et
3. Birbirinize vurmayı dene
4. Hasar gitmemeli
```

## 🐛 Hata Kontrolü

### Log Kontrolü
```bash
tail -f /opt/minecraft/logs/latest.log
```

Kontrol edilecekler:
- ❌ Error mesajları
- ❌ Exception'lar
- ❌ Plugin yükleme hataları
- ✅ Plugin başarıyla yüklendi mesajları

### Plugin Kontrolü
```
/plugins
```

Tüm pluginler yeşil olmalı:
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

### Veri Dosyaları
```bash
ls -la /opt/minecraft/plugins/NexoraEconomy/
ls -la /opt/minecraft/plugins/NexoraDaily/
ls -la /opt/minecraft/plugins/NexoraAchievements/
```

Görmemiz gerekenler:
- balances.json
- quests.json
- achievements.json

## 📊 Performans Testi

### TPS Kontrolü
```
/tps
```
TPS 20'ye yakın olmalı.

### RAM Kullanımı
```bash
free -h
```
Sunucu 7-8GB RAM kullanmalı.

### CPU Kullanımı
```bash
htop
```
CPU %50'nin altında olmalı.

## ✅ Test Checklist

### Ekonomi
- [ ] İlk katılımda 1000₺
- [ ] /daily çalışıyor
- [ ] /pay çalışıyor
- [ ] /baltop çalışıyor
- [ ] /eco admin komutu çalışıyor

### Mağaza
- [ ] /shop GUI açılıyor
- [ ] Kategoriler çalışıyor
- [ ] Alış işlemi çalışıyor
- [ ] Satış işlemi çalışıyor
- [ ] /sell çalışıyor
- [ ] /sellall çalışıyor

### Görevler
- [ ] /quests görevleri gösteriyor
- [ ] Görev ilerlemesi takip ediliyor
- [ ] Görev tamamlanınca ödül veriliyor
- [ ] Tüm görevler bonusu çalışıyor
- [ ] Günlük sıfırlama çalışıyor

### Başarılar
- [ ] /achievements başarıları gösteriyor
- [ ] Otomatik başarı takibi çalışıyor
- [ ] Başarı duyuruları çalışıyor
- [ ] Ödüller veriliyor
- [ ] Kategoriler düzgün

### Parti
- [ ] /party create çalışıyor
- [ ] /party invite çalışıyor
- [ ] /party accept çalışıyor
- [ ] /party leave çalışıyor
- [ ] /party kick çalışıyor
- [ ] /party list çalışıyor
- [ ] PvP koruması çalışıyor
- [ ] Hologramlar görünüyor

### Duyurular
- [ ] 5 dakikada bir duyuru geliyor
- [ ] Farklı mesajlar gösteriliyor
- [ ] Oyuncu yoksa duyuru yapılmıyor

### Discord
- [ ] /stats komutu çalışıyor
- [ ] Hesap bağlama çalışıyor
- [ ] Oyuncu avatarı gösteriliyor
- [ ] Durum gösterimi çalışıyor

## 🎉 Test Tamamlandı!

Tüm testler başarılı ise, sunucu production'a hazır!

### Son Kontroller
1. ✅ Tüm pluginler yüklü
2. ✅ Tüm komutlar çalışıyor
3. ✅ Veri dosyaları oluşuyor
4. ✅ Performans iyi
5. ✅ Hata yok

### Oyunculara Duyuru
```
Sunucuya yeni özellikler eklendi!

🛒 /shop - Mağaza sistemi
📋 /quests - Günlük görevler
🏆 /achievements - Başarılar
👥 /party - Parti sistemi
💰 /daily - Günlük ödül

Keşfet ve eğlen! 🎮
```

## 📞 Sorun Bildirimi

Sorun bulursan:
1. Log dosyasını kontrol et
2. Plugin versiyonunu kontrol et
3. Bağımlılıkları kontrol et
4. Sunucu versiyonunu kontrol et

Detaylı log:
```bash
tail -n 100 /opt/minecraft/logs/latest.log > error.log
```
