// State
let ws = null;
let statusInterval = null;

// Elements
const loginScreen = document.getElementById('login-screen');
const dashboard = document.getElementById('dashboard');
const loginForm = document.getElementById('login-form');
const loginError = document.getElementById('login-error');
const logoutBtn = document.getElementById('logout-btn');

// Login
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    try {
        const response = await fetch('/api/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        
        if (response.ok) {
            loginScreen.style.display = 'none';
            dashboard.style.display = 'block';
            initDashboard();
        } else {
            loginError.textContent = 'Hatalı kullanıcı adı veya şifre!';
        }
    } catch (error) {
        loginError.textContent = 'Bağlantı hatası!';
    }
});

// Logout
logoutBtn.addEventListener('click', async () => {
    await fetch('/api/logout', { method: 'POST' });
    location.reload();
});

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
            statusEl.className = 'status-value status-online';
            playerCountEl.textContent = `${data.players.online}/${data.players.max}`;
            pingEl.textContent = `${data.ping}ms`;
            versionEl.textContent = data.version;
            
            // Players list
            if (data.players.list && data.players.list.length > 0) {
                playersListEl.innerHTML = data.players.list.map(player => `
                    <div class="player-item">
                        <img src="https://mc-heads.net/avatar/${player.name}/32" 
                             alt="${player.name}" 
                             class="player-avatar">
                        <span>${player.name}</span>
                    </div>
                `).join('');
            } else if (data.players.online > 0) {
                playersListEl.innerHTML = `<p class="empty-state">${data.players.online} oyuncu çevrimiçi</p>`;
            } else {
                playersListEl.innerHTML = '<p class="empty-state">Oyuncu yok</p>';
            }
        } else {
            statusEl.textContent = '🔴 Çevrimdışı';
            statusEl.className = 'status-value status-offline';
            playerCountEl.textContent = '-/-';
            pingEl.textContent = '- ms';
            versionEl.textContent = '-';
            playersListEl.innerHTML = '<p class="empty-state">Sunucu çevrimdışı</p>';
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
    if (!confirm(`Sunucuyu ${action === 'start' ? 'başlatmak' : action === 'restart' ? 'yeniden başlatmak' : 'durdurmak'} istediğinize emin misiniz?`)) {
        return;
    }
    
    try {
        const response = await fetch(`/api/server/${action}`, { method: 'POST' });
        const data = await response.json();
        
        if (data.success) {
            alert(data.message);
            setTimeout(updateStatus, 2000);
        } else {
            alert('Hata: ' + data.error);
        }
    } catch (error) {
        alert('İşlem başarısız!');
    }
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
