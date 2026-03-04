const { Client, GatewayIntentBits, EmbedBuilder } = require('discord.js');
const { status } = require('minecraft-server-util');
const fs = require('fs');
const path = require('path');
require('dotenv').config();

// Config
const CONFIG = {
    BOT_TOKEN: process.env.DISCORD_BOT_TOKEN || 'YOUR_TOKEN_HERE',
    SERVER_INFO_CHANNEL: '1478864682948231219',
    MC_SERVER_IP: '194.105.5.37',
    MC_SERVER_PORT: 25565,
    UPDATE_INTERVAL: 30000, // 30 saniye
    MESSAGE_ID_FILE: './message_id.txt'
};

const client = new Client({
    intents: [GatewayIntentBits.Guilds]
});

let serverInfoMessage = null;
let serverStartTime = Date.now();

// Sunucu bilgilerini güncelle
async function updateServerInfo() {
    try {
        const response = await status(CONFIG.MC_SERVER_IP, CONFIG.MC_SERVER_PORT);
        
        const uptime = Math.floor((Date.now() - serverStartTime) / 1000 / 60 / 60 / 24);
        
        const embed = new EmbedBuilder()
            .setColor('#00FF00')
            .setTitle('🎮 Minecraft Sunucu Bilgileri')
            .setImage('https://cdn.discordapp.com/attachments/531892263652032522/1464235225818075147/standard_2.gif')
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
        
        // Mesaj ID'sini dosyadan oku
        let messageId = null;
        if (fs.existsSync(CONFIG.MESSAGE_ID_FILE)) {
            messageId = fs.readFileSync(CONFIG.MESSAGE_ID_FILE, 'utf8').trim();
        }
        
        if (messageId) {
            try {
                const message = await channel.messages.fetch(messageId);
                await message.edit({ embeds: [embed] });
                serverInfoMessage = message;
            } catch (error) {
                // Mesaj bulunamadı, yeni oluştur
                serverInfoMessage = await channel.send({ embeds: [embed] });
                fs.writeFileSync(CONFIG.MESSAGE_ID_FILE, serverInfoMessage.id);
            }
        } else {
            serverInfoMessage = await channel.send({ embeds: [embed] });
            fs.writeFileSync(CONFIG.MESSAGE_ID_FILE, serverInfoMessage.id);
        }
    } catch (error) {
        console.error('Sunucu bilgisi güncellenirken hata:', error);
        
        const embed = new EmbedBuilder()
            .setColor('#FF0000')
            .setTitle('🎮 Minecraft Sunucu Bilgileri')
            .setImage('https://cdn.discordapp.com/attachments/531892263652032522/1464235225818075147/standard_2.gif')
            .addFields(
                { name: '🔴 Durum', value: 'Çevrimdışı', inline: true },
                { name: '🌍 Sunucu IP', value: `\`${CONFIG.MC_SERVER_IP}:${CONFIG.MC_SERVER_PORT}\``, inline: true }
            )
            .setFooter({ text: `Son güncelleme: ${new Date().toLocaleString('tr-TR')}` })
            .setTimestamp();

        const channel = await client.channels.fetch(CONFIG.SERVER_INFO_CHANNEL);
        
        // Mesaj ID'sini dosyadan oku
        let messageId = null;
        if (fs.existsSync(CONFIG.MESSAGE_ID_FILE)) {
            messageId = fs.readFileSync(CONFIG.MESSAGE_ID_FILE, 'utf8').trim();
        }
        
        if (messageId) {
            try {
                const message = await channel.messages.fetch(messageId);
                await message.edit({ embeds: [embed] });
                serverInfoMessage = message;
            } catch (error) {
                // Mesaj bulunamadı, yeni oluştur
                serverInfoMessage = await channel.send({ embeds: [embed] });
                fs.writeFileSync(CONFIG.MESSAGE_ID_FILE, serverInfoMessage.id);
            }
        } else {
            serverInfoMessage = await channel.send({ embeds: [embed] });
            fs.writeFileSync(CONFIG.MESSAGE_ID_FILE, serverInfoMessage.id);
        }
    }
}

client.once('ready', async () => {
    console.log(`Bot giriş yaptı: ${client.user.tag}`);
    
    // Bot durumunu ayarla
    client.user.setPresence({
        activities: [{
            name: 'https://www.twitch.tv/swaffval',
            type: 1, // 1 = Streaming
            url: 'https://www.twitch.tv/swaffval'
        }],
        status: 'online'
    });
    
    // İlk güncelleme
    await updateServerInfo();
    
    // Periyodik güncelleme
    setInterval(updateServerInfo, CONFIG.UPDATE_INTERVAL);
});

client.login(CONFIG.BOT_TOKEN);
