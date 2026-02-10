// src/main/resources/static/js/login-stats.js

// ======================
// Модуль статистики для страницы логина
// ======================
class LoginStatsManager {
    constructor() {
        this.wsClient = null;
        this.isConnected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 3000;

        this.stats = {
            todayApplications: 0,
            successRate: 0,
            totalApplications: 0,
            successfulApplications: 0
        };
    }

    // Инициализация
    init() {
        console.log('Initializing Login Stats Manager...');

        // Сначала загружаем статистику
        this.fetchStats();

        // Проверяем наличие Stomp перед подключением
        if (typeof Stomp !== 'undefined') {
            this.connectWebSocket();
        } else {
            console.warn('Stomp.js not loaded, using polling only');
            this.setupPolling();
        }

        // Обновляем UI
        this.updateStatsUI();

        console.log('Login Stats Manager initialized');
    }

    // Получение статистики через REST API
    async fetchStats() {
        try {
            console.log('Fetching login stats...');

            const response = await fetch('/api/v1/clients/login/stats', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }
            });

            console.log('Response status:', response.status, response.statusText);

            if (response.ok) {
                const data = await response.json();
                console.log('Login stats received:', data);

                // ✅ ОБНОВЛЕНИЕ: Проверяем формат ResponseWrapper
                let statsData = data;

                if (data && typeof data === 'object' && 'success' in data) {
                    // Это ResponseWrapper
                    console.log('ResponseWrapper detected, success:', data.success);

                    if (data.success === true) {
                        // Извлекаем данные из result
                        statsData = data.result || {};
                        console.log('Extracted stats from ResponseWrapper:', statsData);
                    } else if (data.success === false) {
                        console.error('ResponseWrapper error:', data.error);
                        this.setDefaultStats();
                        return null;
                    }
                }

                // Обновляем статистику
                this.updateStats(statsData);

                // Обновляем UI
                this.updateStatsUI();

                return statsData;
            } else {
                // Пробуем прочитать тело ошибки
                try {
                    const errorData = await response.json();
                    console.error('Error response body:', errorData);

                    // ✅ ОБНОВЛЕНИЕ: Проверяем ResponseWrapper формат ошибки
                    if (errorData && typeof errorData === 'object' && 'success' in data) {
                        if (errorData.success === false) {
                            console.error('ResponseWrapper error:', errorData.error);
                        }
                    }
                } catch (e) {
                    console.error('Could not read error response:', e);
                }

                console.error('Failed to fetch stats:', response.status, response.statusText);
                this.setDefaultStats();
                return null;
            }
        } catch (error) {
            console.error('Error fetching login stats:', error);
            this.setDefaultStats();
            return null;
        }
    }

    // Обновление статистики
    updateStats(newStats) {
        // Проверяем, что newStats не является ResponseWrapper
        let statsData = newStats;

        if (newStats && typeof newStats === 'object') {
            // Если это ResponseWrapper
            if ('success' in newStats) {
                if (newStats.success === true) {
                    statsData = newStats.result || {};
                } else {
                    console.error('Error in updateStats:', newStats.error);
                    return;
                }
            }
        }

        // Сохраняем старые значения для анимации
        const oldStats = { ...this.stats };

        // Обновляем значения
        this.stats = {
            todayApplications: statsData.todayApplications || 0,
            successRate: statsData.successRate || 0,
            totalApplications: statsData.totalApplications || 0,
            successfulApplications: statsData.successfulApplications || 0
        };

        // Логируем изменения
        console.log('Stats updated:', {
            old: oldStats,
            new: this.stats
        });
    }

    // Установка значений по умолчанию
    setDefaultStats() {
        // Временно показываем данные для тестирования UI
        this.stats = {
            todayApplications: Math.floor(Math.random() * 20) + 5, // 5-25
            successRate: Math.floor(Math.random() * 20) + 80,      // 80-100%
            totalApplications: Math.floor(Math.random() * 200) + 100, // 100-300
            successfulApplications: Math.floor(Math.random() * 180) + 90 // 90-270
        };

        // Добавляем индикацию что это тестовые данные
        const statsContainer = document.querySelector('.login-stats');
        if (statsContainer) {
            statsContainer.classList.add('demo-data');
            statsContainer.title = 'Тестовые данные (ожидание ответа сервера)';
        }

        this.updateStatsUI();
    }

    // Подключение к WebSocket
    connectWebSocket() {
        try {
            console.log('Connecting to WebSocket...');

            const socket = new SockJS('/ws');
            this.wsClient = Stomp.over(socket);

            // Отключаем debug логи
            this.wsClient.debug = null;

            this.wsClient.connect({}, (frame) => {
                console.log('✅ WebSocket connected for login stats');
                this.isConnected = true;
                this.reconnectAttempts = 0;

                // Подписываемся на обновления статистики
                this.wsClient.subscribe('/topic/login-stats', (message) => {
                    try {
                        const data = JSON.parse(message.body);
                        console.log('📨 WebSocket message received:', data);

                        if (data.type === 'LOGIN_STATS_UPDATED') {
                            this.handleStatsUpdate(data.stats);
                        }
                    } catch (error) {
                        console.error('❌ Error parsing WebSocket message:', error);
                    }
                });

            }, (error) => {
                console.error('❌ WebSocket connection error:', error);
                this.isConnected = false;
                this.handleReconnect();
            });

        } catch (error) {
            console.warn('⚠️ WebSocket not available:', error);
            this.isConnected = false;
        }
    }

    // Обработка обновления статистики
    handleStatsUpdate(statsData) {
        // ✅ ОБНОВЛЕНИЕ: Проверяем формат данных
        let actualStats = statsData;

        if (statsData && typeof statsData === 'object') {
            // Если это ResponseWrapper
            if ('success' in statsData) {
                if (statsData.success === true) {
                    actualStats = statsData.result || {};
                } else {
                    console.error('WebSocket ResponseWrapper error:', statsData.error);
                    return;
                }
            }
        }

        // Обновляем статистику
        this.updateStats(actualStats);

        // Обновляем UI с анимацией
        this.animateStatsUpdate();

        // Показываем уведомление о обновлении
        this.showStatsUpdateNotification();
    }

    handleWebSocketMessage(message) {
        try {
            const data = JSON.parse(message.body);
            console.log('📨 WebSocket message received:', data);

            // ✅ ОБНОВЛЕНИЕ: Проверяем формат ResponseWrapper
            let messageData = data;

            if (data && typeof data === 'object') {
                // Если это ResponseWrapper
                if ('success' in data) {
                    if (data.success === true) {
                        messageData = data.result || data;
                    } else {
                        console.error('WebSocket ResponseWrapper error:', data.error);
                        return;
                    }
                }
            }

            // Обрабатываем тип сообщения
            if (messageData.type === 'LOGIN_STATS_UPDATED' || messageData.type === 'STATS_UPDATED') {
                const stats = messageData.stats || messageData.payload || messageData;
                this.handleStatsUpdate(stats);
            }

        } catch (error) {
            console.error('❌ Error parsing WebSocket message:', error);
        }
    }

    // Анимация обновления статистики
    animateStatsUpdate() {
        const todayElement = document.getElementById('stat-today-applications');
        const successElement = document.getElementById('stat-success-rate');

        // Добавляем класс анимации
        if (todayElement) {
            todayElement.classList.add('stat-updating');
            setTimeout(() => todayElement.classList.remove('stat-updating'), 500);
        }

        if (successElement) {
            successElement.classList.add('stat-updating');
            setTimeout(() => successElement.classList.remove('stat-updating'), 500);
        }

        // Обновляем значения с анимацией
        this.animateNumber('stat-today-applications', this.stats.todayApplications);
        this.animateNumber('stat-success-rate', this.stats.successRate);
    }

    // Анимация чисел
    animateNumber(elementId, targetValue) {
        const element = document.getElementById(elementId);
        if (!element) return;

        // Извлекаем текущее числовое значение (убираем символ % если есть)
        let currentText = element.textContent;
        let isPercentage = currentText.includes('%');
        let currentValue = parseInt(currentText.replace('%', '')) || 0;

        // Если значение не изменилось, не анимируем
        if (currentValue === targetValue) return;

        const duration = 1000; // 1 секунда
        const steps = 60; // 60 кадров
        const increment = (targetValue - currentValue) / steps;
        let currentStep = 0;

        const animate = () => {
            currentStep++;
            const newValue = currentValue + (increment * currentStep);

            if (currentStep >= steps) {
                // Конечное значение
                element.textContent = isPercentage ? targetValue + '%' : targetValue;
                return;
            }

            // Промежуточное значение
            element.textContent = isPercentage ?
                Math.round(newValue) + '%' :
                Math.round(newValue);
            requestAnimationFrame(animate);
        };

        animate();
    }

    // Обновление UI
    updateStatsUI() {
        // Находим элементы статистики
        let todayElement = document.getElementById('stat-today-applications');
        let successElement = document.getElementById('stat-success-rate');

        // Если элементы не существуют, создаем их
        if (!todayElement || !successElement) {
            this.createStatsElements();
            todayElement = document.getElementById('stat-today-applications');
            successElement = document.getElementById('stat-success-rate');
        }

        // Обновляем значения
        if (todayElement) {
            todayElement.textContent = this.stats.todayApplications;
        }

        if (successElement) {
            successElement.textContent = this.stats.successRate + '%';
        }
    }

    // Создание элементов статистики
    createStatsElements() {
        const statsContainer = document.querySelector('.login-stats');
        if (!statsContainer) return;

        // Очищаем контейнер
        statsContainer.innerHTML = '';

        // Создаем элемент для сегодняшних заявок
        const todayStat = document.createElement('div');
        todayStat.className = 'stat';
        todayStat.innerHTML = `
            <div class="stat-number" id="stat-today-applications">${this.stats.todayApplications}</div>
            <div class="stat-label">Новых заявок за сегодня</div>
        `;

        // Создаем элемент для успешных обращений
        const successStat = document.createElement('div');
        successStat.className = 'stat';
        successStat.innerHTML = `
            <div class="stat-number" id="stat-success-rate">${this.stats.successRate}%</div>
            <div class="stat-label">Успешных обращений</div>
        `;

        // Добавляем элементы в контейнер
        statsContainer.appendChild(todayStat);
        statsContainer.appendChild(successStat);

        // Добавляем CSS класс для анимаций
        statsContainer.classList.add('stats-loaded');
    }

    // Fallback: polling каждые 30 секунд
    setupPolling() {
        // Обновляем каждые 30 секунд, если WebSocket не подключен
        setInterval(() => {
            if (!this.isConnected) {
                this.fetchStats();
            }
        }, 30000);
    }

    // Попытка переподключения WebSocket
    handleReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);

            setTimeout(() => {
                this.connectWebSocket();
            }, this.reconnectDelay);
        } else {
            console.warn('Max reconnection attempts reached. Using polling.');
        }
    }

    // Показать уведомление об обновлении
    showStatsUpdateNotification() {
        // Создаем мигающий эффект для контейнера статистики
        const statsContainer = document.querySelector('.login-stats');
        if (statsContainer) {
            statsContainer.classList.add('stats-updated');
            setTimeout(() => {
                statsContainer.classList.remove('stats-updated');
            }, 1000);
        }
    }

    // Очистка
    cleanup() {
        if (this.wsClient && this.isConnected) {
            this.wsClient.disconnect();
            console.log('WebSocket disconnected');
        }
    }
}

// ======================
// Инициализация при загрузке страницы
// ======================
document.addEventListener('DOMContentLoaded', () => {
    // Создаем экземпляр менеджера
    window.loginStatsManager = new LoginStatsManager();

    // Инициализируем
    setTimeout(() => {
        window.loginStatsManager.init();
    }, 1000);

    // Добавляем CSS стили
    addStatsStyles();
});

// ======================
// Добавление CSS стилей
// ======================
function addStatsStyles() {
    const styles = `
        /* ... существующие стилы ... */
        
        /* Индикация ошибок */
        .stat.error {
            border-left: 4px solid var(--error-color);
            opacity: 0.7;
        }
        
        .stat.error .stat-number {
            color: var(--error-color);
        }
        
        .stat.error::before {
            content: '⚠️';
            position: absolute;
            top: 10px;
            left: 10px;
            font-size: 12px;
        }
        
        /* Индикация загрузки */
        .stat.loading .stat-number {
            background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
            background-size: 200% 100%;
            animation: loading 1.5s infinite;
            color: transparent;
            border-radius: 4px;
            min-width: 40px;
            display: inline-block;
        }
        
        @keyframes loading {
            0% { background-position: 200% 0; }
            100% { background-position: -200% 0; }
        }
    `;

    const styleSheet = document.createElement('style');
    styleSheet.textContent = styles;
    document.head.appendChild(styleSheet);
}

// Экспорт для тестирования
if (typeof module !== 'undefined' && module.exports) {
    module.exports = LoginStatsManager;
}