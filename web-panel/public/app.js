// State
let ws = null;
let statusInterval = null;
let selectedPlayer = null;
let serverOnline = false;

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
    loadServerSettings();
    initCharts();
    
    // Auto refresh
    statusInterval = setInterval(updateStatus, 5000);
    setInterval(updateSystem, 10000);
    setInterval(updateTPS, 5000);
    
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
    
    // Console command handler
    const sendCommandBtn = document.getElementById('send-command');
    const consoleCommand = document.getElementById('console-command');
    
    if (sendCommandBtn) {
        sendCommandBtn.addEventListener('click', sendCommand);
    }
    
    if (consoleCommand) {
        consoleCommand.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                sendCommand();
            }
        });
    }
    
    // Modal handlers
    const modal = document.getElementById('player-modal');
    const closeBtn = modal.querySelector('.modal-close');
    
    closeBtn.addEventListener('click', () => {
        modal.classList.remove('show');
        selectedPlayer = null;
    });
    
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.classList.remove('show');
            selectedPlayer = null;
        }
    });
    
    // Player action buttons
    document.querySelectorAll('.modal-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const action = btn.dataset.action;
            handlePlayerAction(action);
        });
    });
    
    // Quick command buttons (both compact and simple)
    document.querySelectorAll('.quick-btn-compact, .quick-btn-simple').forEach(btn => {
        btn.addEventListener('click', () => {
            const action = btn.dataset.action;
            const value = btn.dataset.value;
            handleQuickCommand(action, value);
        });
    });
    
    // Save settings button
    document.getElementById('save-settings').addEventListener('click', saveServerSettings);
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
        
        if (!statusEl || !playerCountEl || !playersListEl) {
            console.error('Required elements not found');
            return;
        }
        
        serverOnline = data.online;
        updateButtonStates();
        
        if (data.online) {
            statusEl.textContent = '🟢 Çevrimiçi';
            statusEl.className = 'stat-value status-online';
            playerCountEl.textContent = `${data.players.online}/${data.players.max}`;
            if (pingEl) pingEl.textContent = `${data.ping}ms`;
            if (versionEl) versionEl.textContent = data.version;
            
            // Players list
            if (data.players.list && data.players.list.length > 0) {
                playersListEl.innerHTML = data.players.list.map(player => `
                    <div class="player-item-compact">
                        <div class="player-info">
                            <img src="https://mc-heads.net/avatar/${player.name}/48" 
                                 alt="${player.name}" 
                                 class="player-avatar-compact">
                            <span class="player-name">${player.name}</span>
                        </div>
                        <div class="player-actions">
                            <button class="player-action-btn kick" onclick="quickPlayerAction('kick', '${player.name}')">
                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <line x1="18" y1="6" x2="6" y2="18"></line>
                                    <line x1="6" y1="6" x2="18" y2="18"></line>
                                </svg>
                                At
                            </button>
                            <button class="player-action-btn ban" onclick="quickPlayerAction('ban', '${player.name}')">
                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <circle cx="12" cy="12" r="10"></circle>
                                    <line x1="4.93" y1="4.93" x2="19.07" y2="19.07"></line>
                                </svg>
                                Ban
                            </button>
                            <button class="player-action-btn op" onclick="quickPlayerAction('op', '${player.name}')">
                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                                </svg>
                                OP
                            </button>
                        </div>
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
            if (pingEl) pingEl.textContent = '- ms';
            if (versionEl) versionEl.textContent = '-';
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
        serverOnline = false;
        updateButtonStates();
    }
}

async function updateLogs() {
    try {
        const response = await fetch('/api/logs?lines=100');
        const data = await response.json();
        const logsContent = document.getElementById('logs-content');
        const logsContainer = document.getElementById('logs-container');
        
        if (logsContent) {
            logsContent.textContent = data.logs;
        }
        
        // Scroll to bottom
        if (logsContainer) {
            logsContainer.scrollTop = logsContainer.scrollHeight;
        }
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
            const ramProgress = document.getElementById('ram-progress');
            const ramText = document.getElementById('ram-text');
            const ramUsageText = document.getElementById('ram-usage-text');
            
            if (ramProgress) {
                ramProgress.style.width = `${usedPercent}%`;
            }
            
            if (ramText) {
                ramText.textContent = `${data.memory.used}MB / ${data.memory.total}MB (${usedPercent}%)`;
            }
            
            if (ramUsageText) {
                ramUsageText.textContent = `${data.memory.used} MB`;
            }
            
            // Update charts
            const cpu = data.cpu || 0;
            updateCharts(20, cpu, data.memory.used, data.memory.total);
        }
        
        if (data.disk) {
            const diskUsage = document.getElementById('disk-usage');
            if (diskUsage) {
                diskUsage.textContent = data.disk;
            }
        }
        
        if (data.cpu !== undefined) {
            const serverCpu = document.getElementById('server-cpu');
            if (serverCpu) {
                serverCpu.textContent = `${data.cpu}%`;
            }
        }
    } catch (error) {
        console.error('System update error:', error);
    }
}

async function updateTPS() {
    try {
        const response = await fetch('/api/server/stats');
        const data = await response.json();
        
        if (data.success && data.tps) {
            const tpsMatch = data.tps.match(/(\d+\.\d+)/);
            if (tpsMatch) {
                const tps = parseFloat(tpsMatch[0]);
                const serverTps = document.getElementById('server-tps');
                if (serverTps) {
                    serverTps.textContent = tps.toFixed(1);
                }
            }
        }
    } catch (error) {
        console.error('TPS update error:', error);
    }
}

async function handleQuickCommand(action, value) {
    try {
        const response = await fetch(`/api/quick/${action}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ [action]: value })
        });
        
        const data = await response.json();
        
        if (data.success) {
            showNotification('Başarılı!', data.message, 'success');
        } else {
            showNotification('Hata!', data.error, 'error');
        }
    } catch (error) {
        showNotification('Hata!', 'İşlem başarısız!', 'error');
    }
}

async function loadServerSettings() {
    try {
        const response = await fetch('/api/server/properties');
        const data = await response.json();
        
        if (data.success) {
            const config = data.config;
            const maxPlayers = document.getElementById('max-players');
            const viewDistance = document.getElementById('view-distance');
            const pvpMode = document.getElementById('pvp-mode');
            const spawnProtection = document.getElementById('spawn-protection');
            
            if (maxPlayers) maxPlayers.value = config['max-players'] || 20;
            if (viewDistance) viewDistance.value = config['view-distance'] || 10;
            if (pvpMode) pvpMode.value = config['pvp'] || 'true';
            if (spawnProtection) spawnProtection.value = config['spawn-protection'] || 16;
        }
    } catch (error) {
        console.error('Settings load error:', error);
    }
}

async function saveServerSettings() {
    const maxPlayers = document.getElementById('max-players');
    const viewDistance = document.getElementById('view-distance');
    const pvpMode = document.getElementById('pvp-mode');
    const spawnProtection = document.getElementById('spawn-protection');
    
    if (!maxPlayers || !viewDistance || !pvpMode) {
        showNotification('Hata!', 'Ayar elementleri bulunamadı!', 'error');
        return;
    }
    
    const settings = {
        'max-players': maxPlayers.value,
        'view-distance': viewDistance.value,
        'pvp': pvpMode.value,
        'spawn-protection': spawnProtection ? spawnProtection.value : '16'
    };
    
    try {
        for (const [key, value] of Object.entries(settings)) {
            const response = await fetch('/api/server/properties', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ key, value })
            });
            
            const data = await response.json();
            if (!data.success) {
                throw new Error(data.error);
            }
        }
        
        showNotification('Başarılı!', 'Ayarlar kaydedildi! Restart gerekli.', 'success');
    } catch (error) {
        showNotification('Hata!', 'Ayarlar kaydedilemedi!', 'error');
    }
}

function showNotification(title, message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `notification-toast ${type}`;
    
    const icons = {
        success: '✓',
        error: '✗',
        warning: '⚠'
    };
    
    notification.innerHTML = `
        <div class="notification-icon">${icons[type]}</div>
        <div class="notification-content">
            <div class="notification-title">${title}</div>
            <div class="notification-message">${message}</div>
        </div>
    `;
    
    document.body.appendChild(notification);
    
    setTimeout(() => {
        notification.style.animation = 'slideInRight 0.4s reverse';
        setTimeout(() => notification.remove(), 400);
    }, 3000);
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

// Button state management
function updateButtonStates() {
    const startBtn = document.getElementById('start-btn');
    const stopBtn = document.getElementById('stop-btn');
    const restartBtn = document.getElementById('restart-btn');
    
    if (!startBtn || !stopBtn || !restartBtn) return;
    
    if (serverOnline) {
        startBtn.disabled = true;
        stopBtn.disabled = false;
        restartBtn.disabled = false;
    } else {
        startBtn.disabled = false;
        stopBtn.disabled = true;
        restartBtn.disabled = true;
    }
}

// Player modal
function openPlayerModal(playerName) {
    selectedPlayer = playerName;
    const modal = document.getElementById('player-modal');
    const modalTitle = document.getElementById('modal-player-name');
    
    modalTitle.textContent = `${playerName} - Oyuncu İşlemleri`;
    modal.classList.add('show');
}

// Player actions
async function handlePlayerAction(action) {
    if (!selectedPlayer) return;
    
    const actions = {
        kick: { endpoint: '/api/player/kick', message: 'atılıyor' },
        ban: { endpoint: '/api/player/ban', message: 'banlanıyor' },
        op: { endpoint: '/api/player/op', message: 'OP yapılıyor' },
        deop: { endpoint: '/api/player/deop', message: 'OP\'liği alınıyor' }
    };
    
    const actionData = actions[action];
    if (!actionData) return;
    
    const confirmMessages = {
        kick: 'sunucudan atmak',
        ban: 'banlamak',
        op: 'OP yapmak',
        deop: 'OP\'liğini almak'
    };
    
    if (!confirm(`${selectedPlayer} oyuncusunu ${confirmMessages[action]} istediğinize emin misiniz?`)) {
        return;
    }
    
    try {
        const response = await fetch(actionData.endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ player: selectedPlayer })
        });
        
        const data = await response.json();
        
        if (data.success) {
            showNotification(data.message, 'success');
            document.getElementById('player-modal').classList.remove('show');
            selectedPlayer = null;
            setTimeout(updateStatus, 1000);
        } else {
            showNotification('Hata: ' + data.error, 'error');
        }
    } catch (error) {
        showNotification('İşlem başarısız!', 'error');
    }
}

// Console command
async function sendCommand() {
    const input = document.getElementById('console-command');
    if (!input) return;
    
    const command = input.value.trim();
    
    if (!command) return;
    
    try {
        const response = await fetch('/api/command', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ command })
        });
        
        const data = await response.json();
        
        if (data.success) {
            showNotification('Başarılı!', 'Komut gönderildi!', 'success');
            input.value = '';
        } else {
            showNotification('Hata!', data.error, 'error');
        }
    } catch (error) {
        showNotification('Hata!', 'Komut gönderilemedi!', 'error');
    }
}



// Charts
let performanceChart = null;
let resourceChart = null;
const performanceData = {
    tps: [],
    cpu: [],
    labels: []
};

function initCharts() {
    // Performance Chart (TPS & CPU)
    const perfCtx = document.getElementById('performanceChart');
    if (perfCtx) {
        performanceChart = new Chart(perfCtx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: 'TPS',
                    data: [],
                    borderColor: '#00d9ff',
                    backgroundColor: 'rgba(0, 217, 255, 0.1)',
                    tension: 0.4,
                    fill: true
                }, {
                    label: 'CPU %',
                    data: [],
                    borderColor: '#f59e0b',
                    backgroundColor: 'rgba(245, 158, 11, 0.1)',
                    tension: 0.4,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        labels: { color: '#fff', font: { size: 14, weight: '600' } }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 100,
                        grid: { color: 'rgba(255, 255, 255, 0.1)' },
                        ticks: { color: '#fff' }
                    },
                    x: {
                        grid: { color: 'rgba(255, 255, 255, 0.1)' },
                        ticks: { color: '#fff' }
                    }
                }
            }
        });
    }
    
    // Resource Chart (RAM & Disk)
    const resCtx = document.getElementById('resourceChart');
    if (resCtx) {
        resourceChart = new Chart(resCtx, {
            type: 'doughnut',
            data: {
                labels: ['Kullanılan RAM', 'Boş RAM'],
                datasets: [{
                    data: [0, 100],
                    backgroundColor: [
                        'rgba(124, 58, 237, 0.8)',
                        'rgba(255, 255, 255, 0.1)'
                    ],
                    borderColor: [
                        '#7c3aed',
                        'rgba(255, 255, 255, 0.2)'
                    ],
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        labels: { color: '#fff', font: { size: 14, weight: '600' } }
                    }
                }
            }
        });
    }
}

function updateCharts(tps, cpu, ramUsed, ramTotal) {
    const now = new Date().toLocaleTimeString();
    
    // Performance chart
    if (performanceChart) {
        performanceChart.data.labels.push(now);
        performanceChart.data.datasets[0].data.push(parseFloat(tps) || 20);
        performanceChart.data.datasets[1].data.push(parseFloat(cpu) || 0);
        
        // Keep last 20 data points
        if (performanceChart.data.labels.length > 20) {
            performanceChart.data.labels.shift();
            performanceChart.data.datasets[0].data.shift();
            performanceChart.data.datasets[1].data.shift();
        }
        
        performanceChart.update('none');
    }
    
    // Resource chart
    if (resourceChart && ramUsed && ramTotal) {
        const used = parseInt(ramUsed);
        const total = parseInt(ramTotal);
        resourceChart.data.datasets[0].data = [used, total - used];
        resourceChart.update('none');
    }
}


// Quick player actions
async function quickPlayerAction(action, playerName) {
    const actions = {
        kick: { endpoint: '/api/player/kick', message: 'atılıyor', confirm: 'atmak' },
        ban: { endpoint: '/api/player/ban', message: 'banlanıyor', confirm: 'banlamak' },
        op: { endpoint: '/api/player/op', message: 'OP yapılıyor', confirm: 'OP yapmak' },
        deop: { endpoint: '/api/player/deop', message: 'OP\'liği alınıyor', confirm: 'OP\'liğini almak' }
    };
    
    const actionData = actions[action];
    if (!actionData) return;
    
    if (!confirm(`${playerName} oyuncusunu ${actionData.confirm} istediğinize emin misiniz?`)) {
        return;
    }
    
    try {
        const response = await fetch(actionData.endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ player: playerName })
        });
        
        const data = await response.json();
        
        if (data.success) {
            showNotification('Başarılı!', data.message, 'success');
            setTimeout(updateStatus, 1000);
        } else {
            showNotification('Hata!', data.error, 'error');
        }
    } catch (error) {
        showNotification('Hata!', 'İşlem başarısız!', 'error');
    }
}
