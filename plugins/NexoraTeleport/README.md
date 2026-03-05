# NexoraTeleport

Minecraft sunucusu için gelişmiş teleport istek sistemi.

## Özellikler

- 🚀 Oyunculara teleport isteği gönderme
- 📍 Oyuncuları yanına çağırma
- ⏱️ 60 saniyelik istek zaman aşımı
- ✅ İstek kabul/red sistemi
- 🔔 Güzel bildirim mesajları

## Komutlar

| Komut | Açıklama | Kullanım |
|-------|----------|----------|
| `/tpa <oyuncu>` | Oyuncuya teleport isteği gönder | `/tpa Steve` |
| `/tpaccept` | Teleport isteğini kabul et | `/tpaccept` |
| `/tpdeny` | Teleport isteğini reddet | `/tpdeny` |
| `/tpahere <oyuncu>` | Oyuncuyu yanına çağır | `/tpahere Steve` |

## Alternatif Komutlar

- `/tpa` → `/tprequest`
- `/tpaccept` → `/tpyes`, `/tpkabul`
- `/tpdeny` → `/tpno`, `/tpred`

## Nasıl Çalışır?

1. **Teleport İsteği Gönderme:**
   - `/tpa <oyuncu>` komutuyla bir oyuncuya teleport isteği gönderirsin
   - Hedef oyuncu isteği kabul ederse, sen onun yanına teleport olursun

2. **Oyuncuyu Çağırma:**
   - `/tpahere <oyuncu>` komutuyla bir oyuncuyu yanına çağırırsın
   - Oyuncu kabul ederse, o senin yanına teleport olur

3. **İstek Yanıtlama:**
   - Sana gelen istekleri `/tpaccept` ile kabul edebilirsin
   - `/tpdeny` ile reddedebilirsin
   - İstekler 60 saniye sonra otomatik olarak iptal olur

## Örnekler

```
/tpa Notch
→ Notch'a teleport isteği gönderir

/tpahere Steve
→ Steve'i yanına çağırır

/tpaccept
→ Bekleyen isteği kabul eder

/tpdeny
→ Bekleyen isteği reddeder
```

## Kurulum

1. Plugin JAR dosyasını `plugins/` klasörüne kopyalayın
2. Sunucuyu yeniden başlatın
3. Plugin otomatik olarak aktif olacaktır

## Derleme

```bash
cd plugins/NexoraTeleport
mvn clean package
cp target/NexoraTeleport-1.0.0.jar ../../
```

## Gereksinimler

- Minecraft 1.21+
- Spigot/Paper sunucu
- Java 17+

## Lisans

Bu plugin Nexora sunucusu için özel olarak geliştirilmiştir.
