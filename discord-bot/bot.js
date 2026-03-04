const { Client, GatewayIntentBits, EmbedBuilder } = require('discord.js');
const { status } = require('minecraft-server-util');
const fs = require('fs');
const path = require('path');
require('dotenv').config();

// Config
const CONFIG = {
    BOT_TOKEN: process.env.DISCORD_BOT_TOKEN || 'YOUR_TOKEN_HERE',
    SERVER_INFO_CHANNEL: '1478864682948231219',
    LOG_CHANNEL: '1478865297351114835',
    MC_SERVER_IP: '194.105.5.37',
    MC_SERVER_PORT: 25565,
    UPDATE_INTERVAL: 30000, // 30 saniye
    LOG_FILE: '/opt/minecraft/logs/latest.log'
};

const client = new Client({
    intents: [GatewayIntentBits.Guilds]
});

let serverInfoMessage = null;
let lastLogPosition = 0;
let serverStartTime = Date.now();

// Sunucu bilgilerini güncelle
async function updateServerInfo() {
    try {
        const response = await status(CONFIG.MC_SERVER_IP, CONFIG.MC_SERVER_PORT);
        
        const uptime = Math.floor((Date.now() - serverStartTime) / 1000 / 60 / 60 / 24);
        
        const embed = new EmbedBuilder()
            .setColor('#00FF00')
            .setTitle('🎮 Minecraft Sunucu Bilgileri')
            .addFields(
                { name: '📊 Oyuncu Sayısı', value: `${response.players.online}/${response.players.max}`, inline: true },
                { name: '🌍 Sunucu IP', value: `\`${CONFIG.MC_SERVER_IP}:${CONFIG.MC_SERVER_PORT}\``, inline: true },
                { name: '🎯 Oyun Modu', value: 'Survival', inline: true },
                { name: '📅 Sunucu Günü', value: `${uptime} gün`, inline: true },
                { name: '🟢 Durum', value: 'Çevrimiçi', inline: true },
                { name: '📡 Ping', value: `${response.roundTripLatency}ms`, inline: true }
            )
            .setFooter({ text: `Son güncelleme: ${new Date().toLocaleString('tr-TR')}` })
            .setTimestamp();

        const channel = await client.channels.fetch(CONFIG.SERVER_INFO_CHANNEL);
        
        if (serverInfoMessage) {
            await serverInfoMessage.edit({ embeds: [embed] });
        } else {
            serverInfoMessage = await channel.send({ embeds: [embed] });
        }
    } catch (error) {
        console.error('Sunucu bilgisi güncellenirken hata:', error);
        
        const embed = new EmbedBuilder()
            .setColor('#FF0000')
            .setTitle('🎮 Minecraft Sunucu Bilgileri')
            .addFields(
                { name: '🔴 Durum', value: 'Çevrimdışı', inline: true },
                { name: '🌍 Sunucu IP', value: `\`${CONFIG.MC_SERVER_IP}:${CONFIG.MC_SERVER_PORT}\``, inline: true }
            )
            .setFooter({ text: `Son güncelleme: ${new Date().toLocaleString('tr-TR')}` })
            .setTimestamp();

        const channel = await client.channels.fetch(CONFIG.SERVER_INFO_CHANNEL);
        
        if (serverInfoMessage) {
            await serverInfoMessage.edit({ embeds: [embed] });
        } else {
            serverInfoMessage = await channel.send({ embeds: [embed] });
        }
    }
}

// Log dosyasını izle
async function checkLogs() {
    try {
        if (!fs.existsSync(CONFIG.LOG_FILE)) return;
        
        const stats = fs.statSync(CONFIG.LOG_FILE);
        if (stats.size < lastLogPosition) {
            lastLogPosition = 0; // Log dosyası yeniden başlamış
        }
        
        const stream = fs.createReadStream(CONFIG.LOG_FILE, {
            start: lastLogPosition,
            encoding: 'utf8'
        });
        
        let buffer = '';
        stream.on('data', (chunk) => {
            buffer += chunk;
        });
        
        stream.on('end', async () => {
            const lines = buffer.split('\n');
            const logChannel = await client.channels.fetch(CONFIG.LOG_CHANNEL);
            
            for (const line of lines) {
                if (!line.trim()) continue;
                
                // Oyuncu giriş
                if (line.includes('joined the game')) {
                    const match = line.match(/\]: (.+?) joined the game/);
                    if (match) {
                        const embed = new EmbedBuilder()
                            .setColor('#00FF00')
                            .setDescription(`✅ **${match[1]}** sunucuya katıldı!`)
                            .setTimestamp();
                        await logChannel.send({ embeds: [embed] });
                    }
                }
                
                // Oyuncu çıkış
                if (line.includes('left the game')) {
                    const match = line.match(/\]: (.+?) left the game/);
                    if (match) {
                        const embed = new EmbedBuilder()
                            .setColor('#FF0000')
                            .setDescription(`❌ **${match[1]}** sunucudan ayrıldı!`)
                            .setTimestamp();
                        await logChannel.send({ embeds: [embed] });
                    }
                }
            }
            
            lastLogPosition = stats.size;
        });
    } catch (error) {
        console.error('Log okuma hatası:', error);
    }
}

client.once('ready', async () => {
    console.log(`Bot giriş yaptı: ${client.user.tag}`);
    
    // İlk güncelleme
    await updateServerInfo();
    
    // Periyodik güncelleme
    setInterval(updateServerInfo, CONFIG.UPDATE_INTERVAL);
    
    // Log kontrolü (5 saniyede bir)
    setInterval(checkLogs, 5000);
});

client.login(CONFIG.BOT_TOKEN);
