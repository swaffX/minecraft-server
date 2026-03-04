const express = require('express');
const session = require('express-session');
const bcrypt = require('bcrypt');
const { exec } = require('child_process');
const { status } = require('minecraft-server-util');
const fs = require('fs');
const path = require('path');
const WebSocket = require('ws');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

// Config
const CONFIG = {
    MC_SERVER_IP: '194.105.5.37',
    MC_SERVER_PORT: 25565,
    LOG_FILE: '/opt/minecraft/logs/latest.log',
    ADMIN_USERNAME: process.env.ADMIN_USERNAME || 'admin',
    ADMIN_PASSWORD_HASH: process.env.ADMIN_PASSWORD_HASH // bcrypt hash
};

// Middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(express.static('public'));
app.use(session({
    secret: process.env.SESSION_SECRET || 'minecraft-panel-secret-key',
    resave: false,
    saveUninitialized: false,
    cookie: { maxAge: 24 * 60 * 60 * 1000 } // 24 saat
}));

// Auth middleware
function requireAuth(req, res, next) {
    if (req.session.authenticated) {
        next();
    } else {
        res.status(401).json({ error: 'Unauthorized' });
    }
}

// Routes
app.post('/api/login', async (req, res) => {
    const { username, password } = req.body;
    
    if (username === CONFIG.ADMIN_USERNAME) {
        const match = await bcrypt.compare(password, CONFIG.ADMIN_PASSWORD_HASH);
        if (match) {
            req.session.authenticated = true;
            req.session.username = username;
            return res.json({ success: true });
        }
    }
    
    res.status(401).json({ error: 'Invalid credentials' });
});

app.post('/api/logout', (req, res) => {
    req.session.destroy();
    res.json({ success: true });
});

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

// WebSocket for real-time logs
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
    console.log(`Web panel çalışıyor: http://localhost:${PORT}`);
});

server.on('upgrade', (request, socket, head) => {
    wss.handleUpgrade(request, socket, head, (ws) => {
        wss.emit('connection', ws, request);
    });
});
