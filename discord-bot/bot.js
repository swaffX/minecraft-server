const { Client, GatewayIntentBits, EmbedBuilder, SlashCommandBuilder, REST, Routes } = require('discord.js');
const { status } = require('minecraft-server-util');
const fs = require('fs');
const path = require('path');
const Rcon = require('rcon-client').Rcon;
require('dotenv').config();

// Config
const CONFIG = {
    BOT_TOKEN: process.env.DISCORD_BOT_TOKEN || 'YOUR_TOKEN_HERE',
    SERVER_INFO_CHANNEL: '1478864682948231219',
    MC_SERVER_IP: '194.105.5.37',
    MC_SERVER_PORT: 25565,
    RCON_PORT: 25575,
    RCON_PASSWORD: process.env.RCON_PASSWORD || 'your_rcon_password',
    UPDATE_INTERVAL: 30000, // 30 saniye
    MESSAGE_ID_FILE: './message_id.txt',
    PLAYER_LINKS_FILE: './player_links.json'
};

const client = new Client({
    intents: [GatewayIntentBits.Guilds]
});

let serverInfoMessage = null;
let serverStartTime = Date.now();
let playerLinks = {};

// Oyuncu bağlantılarını yükle
function loadPlayerLinks() {
    if (fs.existsSync(CONFIG.PLAYER_LINKS_FILE)) {
        try {
            const data = fs.readFileSync(CONFIG.PLAYER_LINKS_FILE, 'utf8');
            playerLinks = JSON.parse(data);
        } catch (error) {
            console.error('Oyuncu bağlantıları yüklenemedi:', error);
            playerLinks = {};
        }
    }
}

// Oyuncu bağlantılarını kaydet
function savePlayerLinks() {
    try {
        fs.writeFileSync(CONFIG.PLAYER_LINKS_FILE, JSON.stringify(playerLinks, null, 2));
    } catch (error) {
        console.error('Oyuncu bağlantıları kaydedilemedi:', error);
    }
}

// RCON komutu çalıştır
async function executeRconCommand(command) {
    try {
        const rcon = await Rcon.connect({
            host: CONFIG.MC_SERVER_IP,
            port: CONFIG.RCON_PORT,
            password: CONFIG.RCON_PASSWORD
        });
        
        const response = await rcon.send(command);
        await rcon.end();
        
        return response;
    } catch (error) {
        console.error('RCON hatası:', error);
        return null;
    }
}

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
    
    // Oyuncu bağlantılarını yükle
    loadPlayerLinks();
    
    // Slash komutlarını kaydet
    const commands = [
        new SlashCommandBuilder()
            .setName('stats')
            .setDescription('Minecraft oyuncu istatistiklerini göster')
            .addStringOption(option =>
                option.setName('oyuncu')
                    .setDescription('Minecraft oyuncu adı (ilk kullanımda gerekli)')
                    .setRequired(false)
            )
    ].map(command => command.toJSON());
    
    const rest = new REST({ version: '10' }).setToken(CONFIG.BOT_TOKEN);
    
    try {
        console.log('Slash komutları kaydediliyor...');
        await rest.put(
            Routes.applicationCommands(client.user.id),
            { body: commands }
        );
        console.log('Slash komutları başarıyla kaydedildi!');
    } catch (error) {
        console.error('Slash komutları kaydedilemedi:', error);
    }
    
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

// Slash komutlarını dinle
client.on('interactionCreate', async interaction => {
    if (!interaction.isChatInputCommand()) return;
    
    if (interaction.commandName === 'stats') {
        await handleStatsCommand(interaction);
    }
});

async function handleStatsCommand(interaction) {
    const discordId = interaction.user.id;
    let minecraftName = interaction.options.getString('oyuncu');
    
    // Eğer oyuncu adı verilmediyse, kayıtlı olanı kullan
    if (!minecraftName) {
        if (playerLinks[discordId]) {
            minecraftName = playerLinks[discordId];
        } else {
            await interaction.reply({
                content: '❌ İlk kullanımda Minecraft oyuncu adını belirtmelisin!\nKullanım: `/stats oyuncu:OyuncuAdın`',
                ephemeral: true
            });
            return;
        }
    }
    
    // Oyuncu bağlantısını kaydet
    playerLinks[discordId] = minecraftName;
    savePlayerLinks();
    
    await interaction.deferReply();
    
    try {
        // Oyuncu istatistiklerini al (RCON ile)
        const playtimeResponse = await executeRconCommand(`minecraft:scoreboard players get ${minecraftName} playtime`);
        const deathsResponse = await executeRconCommand(`minecraft:scoreboard players get ${minecraftName} deaths`);
        
        // Basit istatistikler (gerçek sunucuda daha fazla veri toplanabilir)
        const embed = new EmbedBuilder()
            .setColor('#00FF00')
            .setTitle(`📊 ${minecraftName} - Oyuncu İstatistikleri`)
            .setThumbnail(`https://mc-heads.net/avatar/${minecraftName}/100`)
            .addFields(
                { name: '🎮 Oyuncu Adı', value: minecraftName, inline: true },
                { name: '🔗 Discord', value: `<@${discordId}>`, inline: true },
                { name: '🌍 Sunucu', value: CONFIG.MC_SERVER_IP, inline: true }
            )
            .setFooter({ text: 'Nexora Minecraft Server' })
            .setTimestamp();
        
        // Sunucu durumunu kontrol et
        try {
            const serverStatus = await status(CONFIG.MC_SERVER_IP, CONFIG.MC_SERVER_PORT);
            
            // Oyuncu çevrimiçi mi?
            const isOnline = serverStatus.players.sample?.some(p => p.name === minecraftName) || false;
            
            embed.addFields(
                { name: '🟢 Durum', value: isOnline ? 'Çevrimiçi' : 'Çevrimdışı', inline: true }
            );
        } catch (error) {
            embed.addFields(
                { name: '🔴 Durum', value: 'Sunucu Çevrimdışı', inline: true }
            );
        }
        
        embed.addFields(
            { name: '💡 Bilgi', value: 'İstatistikler sunucudan gerçek zamanlı olarak alınır.\nDaha detaylı istatistikler için oyun içinde `/achievements` komutunu kullan.', inline: false }
        );
        
        await interaction.editReply({ embeds: [embed] });
        
    } catch (error) {
        console.error('Stats komutu hatası:', error);
        await interaction.editReply({
            content: '❌ Oyuncu istatistikleri alınırken bir hata oluştu. Oyuncu adının doğru olduğundan emin ol.'
        });
    }
}

client.login(CONFIG.BOT_TOKEN);
