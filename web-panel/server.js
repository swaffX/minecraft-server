const express = require('express');
const session = require('express-session');
const axios = require('axios');
const { exec } = require('child_process');
const { status } = require('minecraft-server-util');
const fs = require('fs');
const WebSocket = require('ws');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

// Config
const CONFIG = {
    MC_SERVER_IP: '194.105.5.37',
    MC_SERVER_PORT: 25565,
    LOG_FILE: '/opt/minecraft/logs/latest.log',
    ALLOWED_USER_ID: '315875588906680330',
    DISCORD_CLIENT_ID: process.env.DISCORD_CLIENT_ID,
    DISCORD_CLIENT_SECRET: process.env.DISCORD_CLIENT_SECRET,
    DISCORD_REDIRECT_URI: process.env.DISCORD_REDIRECT_URI || 'http://194.105.5.37:3000/auth/callback'
};

// Middleware
app.use(express.json());
app.use(express.static('public'));
app.use(session({
    secret: process.env.SESSION_SECRET || 'minecraft-panel-secret',
    resave: false,
    saveUninitialized: false,
    cookie: { maxAge: 7 * 24 * 60 * 60 * 1000 } // 7 gün
}));

// Auth middleware
function requireAuth(req, res, next) {
    if (req.session.user && req.session.user.id === CONFIG.ALLOWED_USER_ID) {
        next();
    } else {
        res.status(401).json({ error: 'Unauthorized' });
    }
}

// Discord OAuth Routes
app.get('/auth/discord', (req, res) => {
    const authUrl = `https://discord.com/api/oauth2/authorize?client_id=${CONFIG.DISCORD_CLIENT_ID}&redirect_uri=${encodeURIComponent(CONFIG.DISCORD_REDIRECT_URI)}&response_type=code&scope=identify`;
    res.redirect(authUrl);
});

app.get('/auth/callback', async (req, res) => {
    const code = req.query.code;
    
    if (!code) {
        return res.redirect('/?error=no_code');
    }
    
    try {
        // Exchange code for token
        const tokenResponse = await axios.post('https://discord.com/api/oauth2/token', 
            new URLSearchParams({
                client_id: CONFIG.DISCORD_CLIENT_ID,
                client_secret: CONFIG.DISCORD_CLIENT_SECRET,
                grant_type: 'authorization_code',
                code: code,
                redirect_uri: CONFIG.DISCORD_REDIRECT_URI
            }),
            {
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
            }
        );
        
        const accessToken = tokenResponse.data.access_token;
        
        // Get user info
        const userResponse = await axios.get('https://discord.com/api/users/@me', {
            headers: { Authorization: `Bearer ${accessToken}` }
        });
        
        const user = userResponse.data;
        
        // Check if user is allowed
        if (user.id === CONFIG.ALLOWED_USER_ID) {
            req.session.user = {
                id: user.id,
                username: user.username,
                avatar: user.avatar ? `https://cdn.discordapp.com/avatars/${user.id}/${user.avatar}.png` : null
            };
            res.redirect('/?success=true');
        } else {
            res.redirect('/?error=unauthorized');
        }
    } catch (error) {
        console.error('Discord auth error:', error);
        res.redirect('/?error=auth_failed');
    }
});

app.get('/api/user', (req, res) => {
    if (req.session.user) {
        res.json(req.session.user);
    } else {
        res.status(401).json({ error: 'Not authenticated' });
    }
});

app.post('/api/logout', (req, res) => {
    req.session.destroy();
    res.json({ success: true });
});

// Server API Routes
app.get('/api/status', requireAuth, async (req, res) => {
    try {
        const response = await status(CONFIG.MC_SERVER_IP, CONFIG.MC_SERVER_PORT);
        res.json({
            online: true,
            players: {
                online: response.players.online,
                max: response.players.max,
                list: response.players.sample || []
            },
            version: response.version.name,
            motd: response.motd.clean,
            ping: response.roundTripLatency
        });
    } catch (error) {
        res.json({ online: false });
    }
});

app.get('/api/logs', requireAuth, (req, res) => {
    try {
        const lines = parseInt(req.query.lines) || 100;
        exec(`tail -n ${lines} ${CONFIG.LOG_FILE}`, (error, stdout) => {
            if (error) {
                return res.status(500).json({ error: 'Log okunamadı' });
            }
            res.json({ logs: stdout });
        });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

app.post('/api/server/restart', requireAuth, (req, res) => {
    exec('systemctl restart minecraft', (error) => {
        if (error) {
            return res.status(500).json({ error: 'Restart başarısız' });
        }
        res.json({ success: true, message: 'Sunucu yeniden başlatılıyor...' });
    });
});

app.post('/api/server/stop', requireAuth, (req, res) => {
    exec('systemctl stop minecraft', (error) => {
        if (error) {
            return res.status(500).json({ error: 'Stop başarısız' });
        }
        res.json({ success: true, message: 'Sunucu durduruluyor...' });
    });
});

app.post('/api/server/start', requireAuth, (req, res) => {
    exec('systemctl start minecraft', (error) => {
        if (error) {
            return res.status(500).json({ error: 'Start başarısız' });
        }
        res.json({ success: true, message: 'Sunucu başlatılıyor...' });
    });
});

app.get('/api/system', requireAuth, (req, res) => {
    exec('free -m && df -h / && uptime', (error, stdout) => {
        if (error) {
            return res.status(500).json({ error: 'Sistem bilgisi alınamadı' });
        }
        
        const lines = stdout.split('\n');
        const memLine = lines[1].split(/\s+/);
        const diskLine = lines.find(l => l.includes('/dev/'));
        
        res.json({
            memory: {
                total: parseInt(memLine[1]),
                used: parseInt(memLine[2]),
                free: parseInt(memLine[3])
            },
            disk: diskLine ? diskLine.split(/\s+/)[4] : 'N/A',
            uptime: lines[lines.length - 1]
        });
    });
});

// WebSocket
const wss = new WebSocket.Server({ noServer: true });

wss.on('connection', (ws) => {
    let logWatcher;
    
    if (fs.existsSync(CONFIG.LOG_FILE)) {
        logWatcher = fs.watch(CONFIG.LOG_FILE, (eventType) => {
            if (eventType === 'change') {
                exec(`tail -n 1 ${CONFIG.LOG_FILE}`, (error, stdout) => {
                    if (!error && ws.readyState === WebSocket.OPEN) {
                        ws.send(JSON.stringify({ type: 'log', data: stdout }));
                    }
                });
            }
        });
    }
    
    ws.on('close', () => {
        if (logWatcher) logWatcher.close();
    });
});

const server = app.listen(PORT, () => {
    console.log(`🎮 Minecraft Panel: http://localhost:${PORT}`);
});

server.on('upgrade', (request, socket, head) => {
    wss.handleUpgrade(request, socket, head, (ws) => {
        wss.emit('connection', ws, request);
    });
});
