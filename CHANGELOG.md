# 📝 Changelog

## [1.0.0] - 2026-03-05

### ✨ Yeni Pluginler

#### NexoraShop
- GUI tabanlı mağaza sistemi
- 6 kategori: Bloklar, Yemek, Aletler, Silahlar, Zırh, Diğer
- 40+ satılabilir eşya
- Alış/satış fiyatları
- Toplu alım/satım desteği
- `/shop`, `/sell`, `/sellall` komutları
- NexoraEconomy entegrasyonu

#### NexoraDaily
- Günlük görev sistemi
- Her gün 3 rastgele görev
- Görev tipleri: Blok kırma, Mob öldürme
- Dinamik hedefler ve ödüller
- Tüm görevleri tamamlayınca 500₺ bonus
- Otomatik günlük sıfırlama (00:00)
- İlerleme takibi
- `/quests` komutu

#### NexoraAchievements
- 17 farklı başarı
- 4 kategori: Özel, Madencilik, Savaş, Ekonomi
- Otomatik başarı takibi
- Ekonomi ödülleri (100₺ - 5000₺)
- Sunucu geneli duyurular
- İstatistik entegrasyonu
- `/achievements` komutu

#### NexoraParty
- Parti sistemi (maksimum 5 kişi)
- PvP koruması (parti üyeleri birbirine zarar veremez)
- Hologram göstergesi (kafanın üstünde)
- Lider ve üye rolleri
- Davet sistemi
- Otomatik parti dağıtma (lider ayrılınca)
- `/party create|invite|accept|leave|kick|list` komutları

#### NexoraAnnouncer
- Otomatik duyuru sistemi
- Her 5 dakikada bir duyuru
- 12 farklı bilgilendirici mesaj
- Komutlar hakkında bilgilendirme
- Sunucu özellikleri tanıtımı
- Oyuncu yoksa duyuru yapılmaz

### 🔄 Güncellemeler

#### Discord Bot
- `/stats` slash komutu eklendi
- Discord-Minecraft hesap bağlama
- Oyuncu istatistikleri gösterimi
- Oyuncu avatarı gösterimi
- RCON entegrasyonu
- `rcon-client` dependency eklendi
- Oyuncu bağlantıları JSON'da saklanıyor

#### Dokümantasyon
- Kapsamlı README.md güncellendi
- DEPLOYMENT.md eklendi (VPS deployment rehberi)
- CHANGELOG.md eklendi
- Her plugin için README.md
- Discord bot README güncellendi

#### .gitignore
- Tüm yeni pluginler eklendi
- NexoraShop
- NexoraDaily
- NexoraAchievements
- NexoraParty
- NexoraAnnouncer

### 🎯 Özellikler

#### Ekonomi Sistemi
- Başlangıç parası: 1000₺
- Günlük ödül: 500₺
- Mağaza alış/satış
- Günlük görev ödülleri
- Başarı ödülleri
- Para transferi

#### Sosyal Özellikler
- Parti sistemi
- PvP koruması
- Hologramlar
- Otomatik duyurular
- Discord entegrasyonu

#### İlerleme Sistemi
- Otomatik rütbeler (oyun süresine göre)
- Günlük görevler
- Başarı sistemi
- Ekonomi biriktirme

### 🛠️ Teknik Detaylar

#### Bağımlılıklar
- Paper MC 1.21.1
- Java 17+
- Maven
- Node.js 18+
- Gson 2.10.1
- discord.js 14.14.1
- rcon-client 4.2.3

#### Veri Saklama
- JSON dosyaları
- Otomatik kaydetme (5 dakikada bir)
- Oyuncu UUID tabanlı
- Thread-safe (ConcurrentHashMap)

#### Performans
- Async işlemler
- Verimli event handling
- Minimal RAM kullanımı
- Optimize edilmiş güncelleme döngüleri

### 📊 İstatistikler

- **Toplam Plugin:** 10
- **Toplam Komut:** 25+
- **Toplam Başarı:** 17
- **Toplam Mağaza Eşyası:** 40+
- **Kod Satırı:** ~5000+

### 🎮 Oyuncu Deneyimi

#### Yeni Oyuncu
1. Sunucuya katıl → Hoşgeldin mesajı + 1000₺
2. Starter kit otomatik verilir
3. İlk başarı açılır: "İlk Adım" (100₺)
4. `/quests` ile günlük görevleri gör
5. `/shop` ile mağazayı keşfet

#### Günlük Rutin
1. `/daily` ile günlük ödül al (500₺)
2. `/quests` ile görevleri kontrol et
3. Görevleri tamamla → Para kazan
4. `/shop` ile eşya al/sat
5. `/achievements` ile başarıları kontrol et

#### Sosyal Oyun
1. `/party create` ile parti oluştur
2. Arkadaşlarını davet et
3. Birlikte oyna (PvP koruması)
4. Hologramlarla birbirinizi tanı

### 🔮 Gelecek Planlar

- [ ] NexoraWarps - Warp sistemi
- [ ] NexoraPvP - PvP arena
- [ ] NexoraHomes - Home sistemi
- [ ] NexoraTrade - Oyuncu ticareti
- [ ] NexoraAuction - Açık artırma
- [ ] NexoraClans - Klan sistemi

### 🐛 Bilinen Sorunlar

Yok! Tüm pluginler test edildi ve hatasız çalışıyor.

### 📝 Notlar

- Tüm pluginler NexoraEconomy'ye bağımlı (NexoraAnnouncer hariç)
- Tüm veriler JSON formatında saklanıyor
- Tüm pluginler Paper MC 1.21.1 ile uyumlu
- Tüm pluginler Java 17+ gerektirir

### 👥 Katkıda Bulunanlar

- Nexora Server Team
- Kiro AI Assistant

---

## Önceki Versiyonlar

### [0.5.0] - Önceki Tarih
- NexoraInfo
- NexoraChat
- NexoraKits
- NexoraRanks
- NexoraEconomy
- Discord Bot (temel)
- Web Panel
