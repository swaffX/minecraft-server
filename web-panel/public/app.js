// State
let ws = null;
let statusInterval = null;

// Elements
const loginScreen = document.getElementById('login-screen');
const dashboard = document.getElementById('dashboard');
const discordLoginBtn = document.getElementById('discord-login-btn');
const loginError = document.getElementById('login-error');
const logoutBtn = document.getElementById('logout-btn');

// Check URL params for errors
const urlParams = new URLSearchParams(window.location.search);
if (urlParams.get('error')) {
    const errorMap = {
        'no_code': 'Giriş kodu alınamadı!',
        'unauthorized': 'Bu panele erişim yetkiniz yok!',
        'auth_failed': 'Discord girişi başarısız!'
    };
    loginError.textContent = errorMap[urlParams.get('error')] || 'Bir hata oluştu!';
    loginError.classList.add('show');
}

// Check if already logged in
checkAuth();

async function checkAuth() {
    try {
        const response = await fetch('/api/user');
        if (response.ok) {
            const user = await response.json();
            showDashboard(user);
        }
    } catch (error) {
        console.log('Not authenticated');
    }
}

// Discord Login
discordLoginBtn.addEventListener('click', () => {
    window.location.href = '/auth/discord';
});

// Logout
logoutBtn.addEventListener('click', async () => {
    await fetch('/api/logout', { method: 'POST' });
    location.reload();
});

// Show Dashboard
function showDashboard(user) {
    loginScreen.style.display = 'none';
    dashboard.style.display = 'block';
    
    // Set user info
    document.getElementById('user-name').textContent = user.username;
    if (user.avatar) {
        document.getElementById('user-avatar').src = user.avatar;
    }
    
    initDashboard();
}

// Dashboard
function initDashboard() {
    updateStatus();
    updateLogs();
    updateSystem();
    
    // Auto refresh
    statusInterval = setInterval(updateStatus, 5000);
    setInterval(updateSystem, 10000);
    
    // WebSocket for real-time logs
    connectWebSocket();
    
    // Button handlers
    document.getElementById('start-btn').addEventListener('click', () => serverAction('start'));
    document.getElementById('restart-btn').addEventListener('click', () => serverAction('restart'));
    document.getElementById('stop-btn').addEventListener('click', () => serverAction('stop'));
    document.getElementById('refresh-logs').addEventListener('click', updateLogs);
    document.getElementById('clear-logs').addEventListener('click', () => {
        document.getElementById('logs-content').textContent = '';
    });
}

async function updateStatus() {
    try {
        const response = await fetch('/api/status');
        const data = await response.json();
        
        const statusEl = document.getElementById('server-status');
        const playerCountEl = document.getElementById('player-count');
        const pingEl = document.getElementById('server-ping');
        const versionEl = document.getElementById('server-version');
        const playersListEl = document.getElementById('players-list');
        
        if (data.online) {
            statusEl.textContent = '🟢 Çevrimiçi';
            statusEl.className = 'stat-value status-online';
            playerCountEl.textContent = `${data.players.online}/${data.players.max}`;
            pingEl.textContent = `${data.ping}ms`;
            versionEl.textContent = data.version;
            
            // Players list
            if (data.players.list && data.players.list.length > 0) {
                playersListEl.innerHTML = data.players.list.map(player => `
                    <div class="player-item">
                        <img src="https://mc-heads.net/avatar/${player.name}/40" 
                             alt="${player.name}" 
                             class="player-avatar">
                        <span>${player.name}</span>
                    </div>
                `).join('');
            } else if (data.players.online > 0) {
                playersListEl.innerHTML = `
                    <div class="empty-state">
                        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                            <circle cx="9" cy="7" r="4"></circle>
                            <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                            <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                        </svg>
                        <p>${data.players.online} oyuncu çevrimiçi</p>
                    </div>
                `;
            } else {
                playersListEl.innerHTML = `
                    <div class="empty-state">
                        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                            <circle cx="9" cy="7" r="4"></circle>
                            <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                            <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                        </svg>
                        <p>Oyuncu yok</p>
                    </div>
                `;
            }
        } else {
            statusEl.textContent = '🔴 Çevrimdışı';
            statusEl.className = 'stat-value status-offline';
            playerCountEl.textContent = '-/-';
            pingEl.textContent = '- ms';
            versionEl.textContent = '-';
            playersListEl.innerHTML = `
                <div class="empty-state">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                        <circle cx="12" cy="12" r="10"></circle>
                        <line x1="15" y1="9" x2="9" y2="15"></line>
                        <line x1="9" y1="9" x2="15" y2="15"></line>
                    </svg>
                    <p>Sunucu çevrimdışı</p>
                </div>
            `;
        }
    } catch (error) {
        console.error('Status update error:', error);
    }
}

async function updateLogs() {
    try {
        const response = await fetch('/api/logs?lines=100');
        const data = await response.json();
        document.getElementById('logs-content').textContent = data.logs;
        
        // Scroll to bottom
        const logsContainer = document.getElementById('logs-container');
        logsContainer.scrollTop = logsContainer.scrollHeight;
    } catch (error) {
        console.error('Logs update error:', error);
    }
}

async function updateSystem() {
    try {
        const response = await fetch('/api/system');
        const data = await response.json();
        
        if (data.memory) {
            const usedPercent = (data.memory.used / data.memory.total * 100).toFixed(1);
            document.getElementById('ram-progress').style.width = `${usedPercent}%`;
            document.getElementById('ram-text').textContent = 
                `${data.memory.used}MB / ${data.memory.total}MB (${usedPercent}%)`;
        }
        
        if (data.disk) {
            document.getElementById('disk-usage').textContent = data.disk;
        }
    } catch (error) {
        console.error('System update error:', error);
    }
}

async function serverAction(action) {
    const messages = {
        start: 'başlatmak',
        restart: 'yeniden başlatmak',
        stop: 'durdurmak'
    };
    
    if (!confirm(`Sunucuyu ${messages[action]} istediğinize emin misiniz?`)) {
        return;
    }
    
    try {
        const response = await fetch(`/api/server/${action}`, { method: 'POST' });
        const data = await response.json();
        
        if (data.success) {
            showNotification(data.message, 'success');
            setTimeout(updateStatus, 2000);
        } else {
            showNotification('Hata: ' + data.error, 'error');
        }
    } catch (error) {
        showNotification('İşlem başarısız!', 'error');
    }
}

function showNotification(message, type) {
    // Simple notification (you can enhance this)
    alert(message);
}

function connectWebSocket() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    ws = new WebSocket(`${protocol}//${window.location.host}`);
    
    ws.onmessage = (event) => {
        const data = JSON.parse(event.data);
        if (data.type === 'log') {
            const logsContent = document.getElementById('logs-content');
            logsContent.textContent += data.data;
            
            // Auto scroll
            const logsContainer = document.getElementById('logs-container');
            if (logsContainer.scrollTop + logsContainer.clientHeight >= logsContainer.scrollHeight - 100) {
                logsContainer.scrollTop = logsContainer.scrollHeight;
            }
        }
    };
    
    ws.onclose = () => {
        setTimeout(connectWebSocket, 5000);
    };
}
