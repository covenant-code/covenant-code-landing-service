// admin-script.js

// ==========================
// Конфигурация
// ==========================
const API_CONFIG = {
    BASE_URL: '/api/v1/admin/clients',
    AUTH: {
        username: 'admin',
        password: 'admin123'
    }
};

const WEBSOCKET_CONFIG = {
    WS_URL: '/ws',
    MAX_RECONNECT_ATTEMPTS: 10,
    RECONNECT_DELAY: 3000
};

// ==========================
// Главный класс приложения
// ==========================
class AdminApplication {
    constructor() {
        this.applications = [];
        this.currentApplicationId = null;

        this.apiClient = new ApiClient();
        this.uiManager = new UIManager();
        this.webSocketManager = new WebSocketManager(this);
        this.logoutManager = new LogoutManager(this);
        this.modalManager = new ModalManager(this);
        this.filterManager = new FilterManager(this);

        console.log('AdminApplication initialized');
    }

    async init() {
        document.addEventListener('DOMContentLoaded', async () => {
            console.log('DOM loaded, initializing admin panel...');

            // Проверка конфигурации
            console.log('API Config:', API_CONFIG);

            this.setupEventListeners();
            this.webSocketManager.connect();

            try {
                await this.loadApplications();
            } catch (error) {
                console.error('Initial load failed:', error);
                this.uiManager.showNotification('Не удалось загрузить заявки', 'error');
            }

            this.initFilters();

            console.log('Admin panel loaded successfully');
        });
    }

    initFilters() {
        console.log('Initializing filters...');

        const filterBtn = document.getElementById('filterBtn');
        const filterPanel = document.getElementById('filterPanel');
        const closeFilters = document.getElementById('closeFilters');

        if (filterBtn && filterPanel) {
            // Открытие/закрытие панели
            filterBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                const isActive = filterPanel.classList.toggle('active');
                filterBtn.classList.toggle('active', isActive);

                // Анимация иконки
                const icon = filterBtn.querySelector('i');
                if (icon) {
                    icon.style.transform = isActive ? 'rotate(180deg)' : 'rotate(0deg)';
                }

                // Адаптация для высокого DPI
                this.adaptFiltersForHighDPI();

                // Ресайз окна
                window.addEventListener('resize', () => {
                    this.adaptFiltersForHighDPI();
                });
            });

            // Закрытие
            closeFilters?.addEventListener('click', () => {
                filterPanel.classList.remove('active');
                filterBtn.classList.remove('active');
                filterBtn.querySelector('i').style.transform = 'rotate(0deg)';
            });

            // Закрытие при клике вне
            document.addEventListener('click', (e) => {
                if (!filterPanel.contains(e.target) &&
                    !filterBtn.contains(e.target) &&
                    filterPanel.classList.contains('active')) {
                    filterPanel.classList.remove('active');
                    filterBtn.classList.remove('active');
                    filterBtn.querySelector('i').style.transform = 'rotate(0deg)';
                }
            });

            // Предотвращение закрытия при клике внутри
            filterPanel.addEventListener('click', (e) => {
                e.stopPropagation();
            });

            // Автоматическая установка дат
            this.setDefaultDates();

            // Подсветка активных фильтров
            this.setupFilterHighlighting();
        }
    }

    adaptFiltersForHighDPI() {
        const isHighDPI = window.devicePixelRatio >= 2;
        const isUltraWide = window.innerWidth >= 2560;
        const priorityFilters = document.querySelector('.priority-filters');

        if (!priorityFilters) return;

        if (isHighDPI && isUltraWide) {
            // Для 3K+ экранов
            priorityFilters.classList.add('high-dpi');
            priorityFilters.classList.remove('compact');

            // Увеличиваем минимальную ширину
            const labels = priorityFilters.querySelectorAll('.checkbox-label');
            labels.forEach(label => {
                label.style.minWidth = '90px';
                label.style.padding = '6px 10px';
            });
        } else {
            // Для обычных экранов
            priorityFilters.classList.remove('high-dpi');

            const labels = priorityFilters.querySelectorAll('.checkbox-label');
            labels.forEach(label => {
                label.style.minWidth = '';
                label.style.padding = '';
            });
        }

        // Проверяем переполнение
        this.checkCheckboxOverflow();
    }

    checkCheckboxOverflow() {
        const labels = document.querySelectorAll('.checkbox-label');
        labels.forEach(label => {
            const checkmark = label.querySelector('.checkmark');
            const priorityDot = label.querySelector('.priority-dot');
            const textSpan = label.querySelector('span:not(.checkmark):not(.priority-dot)');

            if (checkmark && priorityDot && textSpan) {
                const labelRect = label.getBoundingClientRect();
                const checkmarkRect = checkmark.getBoundingClientRect();
                const dotRect = priorityDot.getBoundingClientRect();

                // Если элементы выходят за границы
                if (checkmarkRect.left < labelRect.left ||
                    dotRect.right > labelRect.right ||
                    textSpan.scrollWidth > textSpan.clientWidth) {

                    label.classList.add('overflow-detected');
                    this.applyOverflowFix(label);
                } else {
                    label.classList.remove('overflow-detected');
                }
            }
        });
    }

    applyOverflowFix(label) {
        // Если переполнение обнаружено, применяем компактный стиль
        const priorityFilters = label.closest('.priority-filters');

        if (!priorityFilters.classList.contains('compact')) {
            priorityFilters.classList.add('compact');

            // Меняем HTML структуру для компактности
            const textSpan = label.querySelector('span:not(.checkmark):not(.priority-dot)');
            if (textSpan) {
                const shortText = textSpan.textContent.substring(0, 3); // Берем первые 3 буквы
                textSpan.textContent = shortText + '.';
                textSpan.title = textSpan.textContent + ' (клик для подробностей)';
            }
        }
    }

    setDefaultDates() {
        // Установить сегодняшнюю дату как конец периода
        const today = new Date().toISOString().split('T')[0];
        const endDateInput = document.getElementById('endDate');
        if (endDateInput) {
            endDateInput.value = today;
            endDateInput.max = today; // Нельзя выбрать будущее
        }

        // Установить начало месяца как начало периода
        const startDateInput = document.getElementById('startDate');
        if (startDateInput) {
            const firstDayOfMonth = new Date();
            firstDayOfMonth.setDate(1);
            const firstDayStr = firstDayOfMonth.toISOString().split('T')[0];
            startDateInput.value = firstDayStr;
            startDateInput.max = today;
        }
    }

    setupFilterHighlighting() {
        // Подсветка фильтров при изменении
        const inputs = document.querySelectorAll('.date-input, .select-input, .priority-filters input[type="checkbox"]');
        inputs.forEach(input => {
            input.addEventListener('change', () => {
                this.updatePriorityCount();
                this.updateFilterGroupState();
            });
        });

        // Инициализация счетчика
        this.updatePriorityCount();
    }

    updatePriorityCount() {
        const priorityFilters = document.querySelector('.priority-filters');
        if (!priorityFilters) return;

        const checkboxes = priorityFilters.querySelectorAll('input[type="checkbox"]');
        const checkedCount = Array.from(checkboxes).filter(cb => cb.checked).length;

        // Устанавливаем data-атрибут для CSS
        priorityFilters.setAttribute('data-count', checkedCount);

        // Добавляем/убираем классы для стилизации
        if (checkedCount === 3) {
            priorityFilters.classList.add('all-checked');
            priorityFilters.classList.remove('none-checked');
        } else if (checkedCount === 0) {
            priorityFilters.classList.add('none-checked');
            priorityFilters.classList.remove('all-checked');
        } else {
            priorityFilters.classList.remove('all-checked', 'none-checked');
        }
    }

    updateFilterGroupState() {
        const groups = document.querySelectorAll('.filter-group');
        groups.forEach(group => {
            let hasValue = false;

            if (group.classList.contains('priority-group')) {
                // Проверяем чекбоксы
                const checkboxes = group.querySelectorAll('input[type="checkbox"]');
                hasValue = Array.from(checkboxes).some(cb => cb.checked);
            } else {
                // Проверяем инпуты и селекты
                const input = group.querySelector('input[type="date"], select');
                if (input) {
                    hasValue = input.value && input.value !== 'all';
                }
            }

            if (hasValue) {
                group.classList.add('active');
            } else {
                group.classList.remove('active');
            }
        });
    }

    setupEventListeners() {
        console.log('Setting up event listeners...');

        // Основные кнопки
        document.getElementById('refreshBtn')?.addEventListener('click', () => this.loadApplications());

        // Кнопки фильтров (УЖЕ ЕСТЬ, но добавим логирование)
        const applyFiltersBtn = document.getElementById('applyFilters');
        const resetFiltersBtn = document.getElementById('resetFilters');

        if (applyFiltersBtn) {
            applyFiltersBtn.addEventListener('click', (e) => {
                e.preventDefault();
                console.log('Apply filters button clicked');
                this.filterManager.applyFilters();
            });
        } else {
            console.error('Apply filters button not found!');
        }

        if (resetFiltersBtn) {
            resetFiltersBtn.addEventListener('click', (e) => {
                e.preventDefault();
                console.log('Reset filters button clicked');
                this.filterManager.resetFilters();
            });
        } else {
            console.error('Reset filters button not found!');
        }

        // Обработчики для модалок (уже есть)
        document.getElementById('confirmStatusChange')?.addEventListener('click', () => this.modalManager.changeApplicationStatus());
        document.getElementById('confirmDelete')?.addEventListener('click', () => this.modalManager.deleteApplication());
        document.getElementById('confirmEdit')?.addEventListener('click', () => this.modalManager.saveApplicationChanges());

        // Закрытие модалок
        document.querySelectorAll('.close-modal').forEach(btn =>
            btn.addEventListener('click', () => this.modalManager.closeAllModals())
        );
    }

    async loadApplications() {
        try {
            this.uiManager.showLoading();
            console.log('📥 Loading applications...');

            const result = await this.apiClient.fetchApplications();

            // result теперь уже извлечен из ResponseWrapper
            this.applications = Array.isArray(result) ? result : [];
            console.log(`✅ Loaded ${this.applications.length} applications`);

            if (this.applications.length > 0) {
                console.log('First application:', this.applications[0]);
                console.log('Has uiManager?', !!this.uiManager);
                console.log('Has renderApplications?', typeof this.uiManager.renderApplications);
                console.log('Has createApplicationCard?', typeof this.uiManager.createApplicationCard);
            }

            // Проверяем что uiManager существует
            if (!this.uiManager) {
                throw new Error('uiManager is not defined');
            }

            // Проверяем что метод существует
            if (typeof this.uiManager.renderApplications !== 'function') {
                throw new Error('renderApplications is not a function');
            }

            this.uiManager.renderApplications(this.applications);
            this.uiManager.updateColumnCounts(this.applications);
            await this.loadStats();

            this.uiManager.showNotification(`Загружено ${this.applications.length} заявок`, 'success');

        } catch (error) {
            console.error('❌ Error loading applications:', error);
            console.error('Error stack:', error.stack);

            // Подробная информация об ошибке
            if (error.message.includes('createApplicationCard')) {
                console.error('UI Manager state:', {
                    uiManager: this.uiManager,
                    createApplicationCard: this.uiManager?.createApplicationCard,
                    typeof: typeof this.uiManager?.createApplicationCard
                });
            }

            this.uiManager.showNotification(`Ошибка загрузки заявок: ${error.message}`, 'error');

            // В случае ошибки показываем пустую доску
            this.applications = [];
            this.uiManager.renderApplications([]);
            this.uiManager.updateColumnCounts([]);
        } finally {
            this.uiManager.hideLoading();
        }
    }

    async loadStats() {
        try {
            const stats = await this.apiClient.fetchStats();
            this.uiManager.updateStatsDisplay(stats);
        } catch (error) {
            console.error('Error loading stats:', error);
        }
    }

    // WebSocket callbacks
    onApplicationCreated(application) {
        this.syncApplication(application);
    }

    onApplicationUpdated(application) {
        this.syncApplication(application);
    }

    onApplicationDeleted(applicationId) {
        this.removeApplication(applicationId);
    }

    onStatsUpdated(stats) {
        this.uiManager.updateStatsDisplay(stats);
    }

    // Методы для работы с заявками
    syncApplication(application) {
        console.log(`Syncing application ${application.id}, status: ${application.status}`);

        // Находим и обновляем заявку в массиве
        const index = this.applications.findIndex(app => app.id === application.id);

        if (index !== -1) {
            // Обновляем существующую заявку
            this.applications[index] = application;
            console.log(`Updated in array at index ${index}`);
        } else {
            // Добавляем новую заявку в начало
            this.applications.unshift(application);
            console.log('Added new application to array');
        }

        // Сортируем по дате (сначала новые)
        this.applications.sort((a, b) => {
            const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
            const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
            return dateB - dateA;
        });

        // Обновляем UI - используем правильный контекст
        this.uiManager.updateApplicationCard(application);
        this.uiManager.updateColumnCounts(this.applications);
    }

    // WebSocket callbacks - переименуем для ясности
    handleApplicationCreated(application) {
        this.syncApplication(application);
    }

    handleApplicationUpdated(application) {
        this.syncApplication(application);
    }

    handleApplicationDeleted(applicationId) {
        this.removeApplication(applicationId);
    }

    handleStatsUpdated(stats) {
        this.uiManager.updateStatsDisplay(stats);
    }

    removeApplication(applicationId) {
        this.applications = this.applications.filter(app => app.id !== applicationId);
        this.uiManager.removeApplicationCard(applicationId);
        this.uiManager.updateColumnCounts(this.applications);
    }

    getApplicationById(id) {
        return this.applications.find(app => app.id === id);
    }

    getApplicationsByStatus(status) {
        return this.applications.filter(app => app.status === status);
    }
}

// ==========================
// Класс для работы с API
// ==========================
class ApiClient {
    constructor() {
        this.baseUrl = API_CONFIG.BASE_URL;
    }

    async fetchApplications() {
        try {
            const response = await this.request(`${this.baseUrl}`, 'GET');
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`HTTP ${response.status}: ${errorText}`);
            }

            const result = await response.json();

            // Проверяем формат ResponseWrapper
            if (result && typeof result === 'object') {
                if (result.success === true) {
                    console.log('✅ ResponseWrapper: success, result:', result.result);
                    return result.result || [];
                } else if (result.success === false) {
                    console.error('❌ ResponseWrapper: error', result.error);
                    throw new Error(result.error?.message || 'Ошибка сервера');
                }
            }

            // Если не ResponseWrapper, возвращаем как есть (для совместимости)
            console.warn('⚠️ Response не в формате ResponseWrapper:', result);
            return Array.isArray(result) ? result : [];

        } catch (error) {
            console.error('Error in fetchApplications:', error);
            throw error;
        }
    }

    async fetchApplicationById(id) {
        try {
            const response = await this.request(`${this.baseUrl}/${id}`, 'GET');
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`HTTP ${response.status}: ${errorText}`);
            }

            const result = await response.json();

            // Проверяем формат ResponseWrapper
            if (result && typeof result === 'object') {
                if (result.success === true) {
                    console.log('✅ ResponseWrapper (by id): success, result:', result.result);
                    return result.result || null;
                } else if (result.success === false) {
                    console.error('❌ ResponseWrapper (by id): error', result.error);
                    throw new Error(result.error?.message || 'Ошибка сервера');
                }
            }

            return result || null;

        } catch (error) {
            console.error('Error in fetchApplicationById:', error);
            throw error;
        }
    }

    async fetchStats() {
        try {
            const response = await this.request(`${this.baseUrl}/stats`, 'GET');
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            const result = await response.json();

            // Проверяем формат ResponseWrapper
            if (result && typeof result === 'object') {
                if (result.success === true) {
                    return result.result || {};
                } else if (result.success === false) {
                    console.error('❌ ResponseWrapper (stats): error', result.error);
                    return {};
                }
            }

            return result || {};

        } catch (error) {
            console.error('Error in fetchStats:', error);
            return {};
        }
    }

    async updateApplication(id, data) {
        try {
            const response = await this.request(`${this.baseUrl}/${id}`, 'PUT', data);
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`HTTP ${response.status}: ${errorText}`);
            }

            const result = await response.json();

            // Проверяем формат ResponseWrapper
            if (result && typeof result === 'object') {
                if (result.success === true) {
                    return result.result || null;
                } else if (result.success === false) {
                    console.error('❌ ResponseWrapper (update): error', result.error);
                    throw new Error(result.error?.message || 'Ошибка сервера');
                }
            }

            return result || null;

        } catch (error) {
            console.error('Error in updateApplication:', error);
            throw error;
        }
    }

    async updateApplicationStatus(id, statusData) {
        try {
            const response = await this.request(`${this.baseUrl}/${id}/status`, 'PUT', statusData);
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`HTTP ${response.status}: ${errorText}`);
            }

            const result = await response.json();

            // Проверяем формат ResponseWrapper
            if (result && typeof result === 'object') {
                if (result.success === true) {
                    return result.result || null;
                } else if (result.success === false) {
                    console.error('❌ ResponseWrapper (updateStatus): error', result.error);
                    throw new Error(result.error?.message || 'Ошибка сервера');
                }
            }

            return result || null;

        } catch (error) {
            console.error('Error in updateApplicationStatus:', error);
            throw error;
        }
    }

    async deleteApplication(id) {
        try {
            const response = await this.request(`${this.baseUrl}/${id}`, 'DELETE');
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`HTTP ${response.status}: ${errorText}`);
            }

            const result = await response.json();

            // Проверяем формат ResponseWrapper
            if (result && typeof result === 'object') {
                if (result.success === true) {
                    return true; // Успешное удаление
                } else if (result.success === false) {
                    console.error('❌ ResponseWrapper (delete): error', result.error);
                    throw new Error(result.error?.message || 'Ошибка сервера');
                }
            }

            return true; // Для совместимости со старым форматом

        } catch (error) {
            console.error('Error in deleteApplication:', error);
            throw error;
        }
    }

    async request(url, method, data = null) {
        const options = {
            method: method,
            headers: this.createHeaders(method)
        };

        if (data && ['POST', 'PUT', 'PATCH'].includes(method)) {
            options.body = JSON.stringify(data);
        }

        return await fetch(url, options);
    }

    createHeaders(method) {
        const headers = {
            'Authorization': 'Basic ' + btoa(`${API_CONFIG.AUTH.username}:${API_CONFIG.AUTH.password}`),
            'Content-Type': 'application/json'
        };

        // Добавляем CSRF токен для методов, которые изменяют данные
        if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(method)) {
            const csrfToken = this.getCsrfToken();
            const csrfHeaderName = this.getCsrfHeaderName();
            if (csrfToken) {
                headers[csrfHeaderName] = csrfToken;
            }
        }

        return headers;
    }

    getCsrfToken() {
        const csrfMeta = document.querySelector('meta[name="_csrf"]');
        if (csrfMeta && csrfMeta.content) {
            return csrfMeta.content;
        }

        const csrfInput = document.getElementById('_csrf');
        if (csrfInput && csrfInput.value) {
            return csrfInput.value;
        }

        return null;
    }

    getCsrfHeaderName() {
        const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
        if (csrfHeaderMeta && csrfHeaderMeta.content) {
            return csrfHeaderMeta.content;
        }
        return 'X-CSRF-TOKEN';
    }
}

// ==========================
// Класс для управления UI
// ==========================
class UIManager {
    constructor() {
        this.columns = {
            'NEW': 'newApplications',
            'PROCESSED': 'processedApplications',
            'DONE': 'doneApplications'
        };

        // Привязываем методы к контексту
        this.createApplicationCard = this.createApplicationCard.bind(this);
        this.addApplicationCard = this.addApplicationCard.bind(this);
        this.updateApplicationCard = this.updateApplicationCard.bind(this);
        this.removeApplicationCard = this.removeApplicationCard.bind(this);
        this.renderApplications = this.renderApplications.bind(this);
        this.updateColumnCounts = this.updateColumnCounts.bind(this);
        this.updateStatsDisplay = this.updateStatsDisplay.bind(this);
    }

    createApplicationCard(app) {
        const card = document.createElement('div');
        card.className = 'application-card';
        card.dataset.id = app.id;
        card.dataset.status = app.status;

        // Форматирование даты
        let formattedDate = 'Дата не указана';
        if (app.createdAt) {
            try {
                const date = new Date(app.createdAt);
                if (!isNaN(date.getTime())) {
                    formattedDate = date.toLocaleDateString('ru-RU', {
                        day: 'numeric',
                        month: 'long',
                        year: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit'
                    });
                }
            } catch (e) {
                console.warn('Error formatting date:', e);
            }
        }

        // Приоритет
        const priorityInfo = this.getPriorityInfo(app.priority, app.priorityLabel);

        // Статус
        const statusDisplay = app.statusLabel || this.getStatusDisplay(app.status);

        // Тип курса
        const courseTypeDisplay = app.courseTypeLabel || app.courseType || 'Не выбран';

        // Формируем HTML
        card.innerHTML = `
        <div class="application-header">
            <div>
                <div class="application-id">#${app.id ? app.id.substring(0, 8).toUpperCase() : 'N/A'}</div>
                <div class="application-date">${formattedDate}</div>
            </div>
            <div class="application-priority ${priorityInfo.class}">${priorityInfo.text}</div>
        </div>
        <div class="application-body">
            <h4 class="application-name">${app.name || 'Без имени'}</h4>
            <div class="application-email"><i class="fas fa-envelope"></i>${app.email || 'Не указан'}</div>
            <div class="application-phone"><i class="fas fa-phone"></i>${app.phone || 'Не указан'}</div>
            <div class="application-course"><i class="fas fa-graduation-cap"></i>${courseTypeDisplay}</div>
            ${app.message ? `<div class="application-message"><i class="fas fa-comment"></i>${app.message.substring(0,50)}${app.message.length > 50 ? '...' : ''}</div>` : ''}
            ${app.source ? `<div class="application-source-small"><i class="fas fa-${this.getSourceIcon(app.source)}"></i>${app.source}</div>` : ''}
        </div>
        <div class="application-footer">
            <div class="application-status">
                <span class="status-badge ${(app.status || '').toLowerCase()}">${statusDisplay}</span>
            </div>
            <div class="application-actions">
                <button class="action-btn edit-full" onclick="adminApp.modalManager.showEditModal('${app.id}')" title="Полное редактирование">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="action-btn edit-status" onclick="adminApp.modalManager.showStatusModal('${app.id}','${app.name || 'N/A'}')" title="Изменить статус">
                    <i class="fas fa-exchange-alt"></i>
                </button>
                <button class="action-btn delete" onclick="adminApp.modalManager.showDeleteModal('${app.id}','${app.name || 'N/A'}')" title="Удалить">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        </div>
    `;
        return card;
    }

    renderApplications(applications) {
        // Очищаем все колонки
        Object.values(this.columns).forEach(columnId => {
            const column = document.getElementById(columnId);
            if (column) column.innerHTML = '';
        });

        // Сортируем по дате (сначала новые)
        const sortedApplications = [...applications].sort((a, b) =>
            new Date(b.createdAt) - new Date(a.createdAt)
        );

        // Рендерим в соответствующие колонки
        sortedApplications.forEach(app => {
            this.addApplicationCard(app);
        });

        this.updateEmptyMessages();
    }

    addApplicationCard(application) {
        const columnId = this.columns[application.status];
        const column = document.getElementById(columnId);

        if (column) {
            const card = this.createApplicationCard(application);
            column.appendChild(card);
        }
    }

    updateApplicationCard(application) {
        const oldCard = document.querySelector(`.application-card[data-id="${application.id}"]`);

        if (oldCard) {
            // Сохраняем старый статус для проверки изменения
            const oldStatus = oldCard.dataset.status;
            const newStatus = application.status;

            // Если статус изменился, перемещаем карточку в другую колонку
            if (oldStatus !== newStatus) {
                console.log(`Status changed: ${oldStatus} -> ${newStatus}, moving card...`);

                // Создаем новую карточку с обновленными данными
                const newCard = this.createApplicationCard(application);

                // Удаляем старую и добавляем новую в правильную колонку
                this.moveCardToColumn(application.id, newStatus, newCard);
            } else {
                // Если статус не изменился, просто обновляем карточку на месте
                const newCard = this.createApplicationCard(application);
                oldCard.replaceWith(newCard);
                newCard.style.animation = 'highlightUpdate 1.5s ease';
                setTimeout(() => newCard.style.animation = '', 1500);
            }
        } else {
            // Новая карточка
            console.log(`Adding new card for application ${application.id}`);
            this.addApplicationCard(application);
            // Показываем анимацию для новой карточки
            const addedCard = document.querySelector(`.application-card[data-id="${application.id}"]`);
            if (addedCard) {
                addedCard.style.animation = 'highlightNew 2s ease';
                setTimeout(() => addedCard.style.animation = '', 2000);
            }
        }

        // Обновляем статистику колонок
        if (window.adminApp) {
            this.updateColumnCounts(window.adminApp.applications);
        }
    }

    removeApplicationCard(applicationId) {
        const card = document.querySelector(`.application-card[data-id="${applicationId}"]`);
        if (card) {
            card.style.animation = 'fadeOut 0.3s ease-out';
            setTimeout(() => card.remove(), 300);
        }
    }

    moveCardToColumn(applicationId, newStatus, newCard = null) {
        // Сначала удаляем старую карточку из DOM, если она существует
        const oldCard = document.querySelector(`.application-card[data-id="${applicationId}"]`);
        if (oldCard) {
            oldCard.remove();
        }

        // Теперь создаем новую карточку и добавляем ее в правильную колонку
        const targetColumnId = this.columns[newStatus];
        const targetColumn = document.getElementById(targetColumnId);

        if (!targetColumn) {
            console.error(`Target column not found for status ${newStatus}`);
            return;
        }

        console.log(`Moving card ${applicationId} to column ${targetColumnId}`);

        // Если передали готовую карточку, используем ее, иначе создаем новую
        let cardToAdd = newCard;
        if (!cardToAdd) {
            // Нужно получить данные заявки для создания карточки
            const app = window.adminApp?.getApplicationById(applicationId);
            if (app) {
                cardToAdd = this.createApplicationCard(app);
            } else {
                console.error(`Application ${applicationId} not found for creating card`);
                return;
            }
        }

        // Добавляем карточку в новую колонку
        targetColumn.prepend(cardToAdd);

        // Анимация
        cardToAdd.style.animation = 'slideIn 0.5s ease';
        setTimeout(() => cardToAdd.style.animation = '', 500);
    }

    reateApplicationCard(app) {
        const card = document.createElement('div');
        card.className = 'application-card';
        card.dataset.id = app.id;
        card.dataset.status = app.status;

        // Форматирование даты
        const date = app.createdAt ? new Date(app.createdAt) : new Date();
        const formattedDate = date.toLocaleDateString('ru-RU', {
            day: 'numeric',
            month: 'long',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });

        // Приоритет
        const priorityInfo = this.getPriorityInfo(app.priority, app.priorityLabel);

        // Статус
        const statusDisplay = app.statusLabel || this.getStatusDisplay(app.status);

        // Тип курса
        const courseTypeDisplay = app.courseTypeLabel || app.courseType || 'Не выбран';

        // Формируем HTML
        card.innerHTML = `
        <div class="application-header">
            <div>
                <div class="application-id">#${app.id ? app.id.substring(0, 8).toUpperCase() : 'N/A'}</div>
                <div class="application-date">${formattedDate}</div>
            </div>
            <div class="application-priority ${priorityInfo.class}">${priorityInfo.text}</div>
        </div>
        <div class="application-body">
            <h4 class="application-name">${app.name || 'Без имени'}</h4>
            <div class="application-email"><i class="fas fa-envelope"></i>${app.email || 'Не указан'}</div>
            <div class="application-phone"><i class="fas fa-phone"></i>${app.phone || 'Не указан'}</div>
            <div class="application-course"><i class="fas fa-graduation-cap"></i>${courseTypeDisplay}</div>
            ${app.message ? `<div class="application-message"><i class="fas fa-comment"></i>${app.message.substring(0,50)}${app.message.length > 50 ? '...' : ''}</div>` : ''}
            ${app.source ? `<div class="application-source-small"><i class="fas fa-${this.getSourceIcon(app.source)}"></i>${app.source}</div>` : ''}
        </div>
        <div class="application-footer">
            <div class="application-status">
                <span class="status-badge ${(app.status || '').toLowerCase()}">${statusDisplay}</span>
            </div>
            <div class="application-actions">
                <button class="action-btn edit-full" onclick="adminApp.modalManager.showEditModal('${app.id}')" title="Полное редактирование">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="action-btn edit-status" onclick="adminApp.modalManager.showStatusModal('${app.id}','${app.name || 'N/A'}')" title="Изменить статус">
                    <i class="fas fa-exchange-alt"></i>
                </button>
                <button class="action-btn delete" onclick="adminApp.modalManager.showDeleteModal('${app.id}','${app.name || 'N/A'}')" title="Удалить">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        </div>
    `;
        return card;
    }

    getPriorityInfo(priority, priorityLabel) {
        if (!priority && !priorityLabel) {
            return { text: 'Средний', class: 'priority-medium' };
        }

        if (priority === 'HIGH' || priorityLabel === 'Высокий') {
            return { text: 'Высокий', class: 'priority-high' };
        } else if (priority === 'LOW' || priorityLabel === 'Низкий') {
            return { text: 'Низкий', class: 'priority-low' };
        } else {
            return { text: 'Средний', class: 'priority-medium' };
        }
    }

    getSourceIcon(source) {
        switch(source) {
            case 'Лендинг': return 'globe';
            case 'Социальные сети': return 'share-alt';
            case 'Рекомендация': return 'user-friends';
            default: return 'question-circle';
        }
    }

    getStatusDisplay(status) {
        switch(status) {
            case 'NEW': return 'Новая';
            case 'PROCESSED': return 'В обработке';
            case 'DONE': return 'Завершена';
            default: return 'Неизвестно';
        }
    }

    updateColumnCounts(applications) {
        const counts = { NEW: 0, PROCESSED: 0, DONE: 0 };
        applications.forEach(app => {
            if (counts[app.status] !== undefined) counts[app.status]++;
        });

        document.getElementById('newColumnCount').textContent = counts.NEW;
        document.getElementById('processedColumnCount').textContent = counts.PROCESSED;
        document.getElementById('doneColumnCount').textContent = counts.DONE;

        this.updateEmptyMessages();
    }

    updateEmptyMessages() {
        Object.values(this.columns).forEach(columnId => {
            const column = document.getElementById(columnId);
            const existing = column.querySelector('.empty-message');

            if (column.children.length === 0 && !existing) {
                const message = document.createElement('div');
                message.className = 'empty-message';
                message.innerHTML = `<i class="fas fa-inbox"></i><p>Нет заявок</p>`;
                column.appendChild(message);
            } else if (column.children.length > 0 && existing) {
                existing.remove();
            }
        });
    }

    updateStatsDisplay(stats) {
        document.getElementById('newCount').textContent = stats.newCount || 0;
        document.getElementById('processedCount').textContent = stats.processedCount || 0;
        document.getElementById('doneCount').textContent = stats.doneCount || 0;
        document.getElementById('totalCount').textContent = stats.total || 0;
    }

    showNotification(message, type = 'info') {
        let notification = document.querySelector('.notification');
        if (!notification) {
            notification = document.createElement('div');
            notification.className = 'notification';
            document.body.appendChild(notification);
        }

        notification.textContent = message;
        notification.className = `notification ${type} show`;

        setTimeout(() => {
            notification.classList.remove('show');
        }, 3000);
    }

    showLoading() {
        document.getElementById('loadingOverlay')?.classList.add('active');
    }

    hideLoading() {
        document.getElementById('loadingOverlay')?.classList.remove('active');
    }

    updateWebSocketStatus(connected) {
        const el = document.getElementById('wsStatus');
        if (el) {
            el.textContent = connected ? '🟢 WS подключен' : '🔴 WS отключен';
        }
    }
}

// ==========================
// Класс для управления WebSocket
// ==========================
class WebSocketManager {
    constructor(app) {
        this.app = app;
        this.stompClient = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = WEBSOCKET_CONFIG.MAX_RECONNECT_ATTEMPTS;
        this.reconnectDelay = WEBSOCKET_CONFIG.RECONNECT_DELAY;
        this.isConnected = false;
    }

    connect() {
        if (this.stompClient) {
            try {
                this.stompClient.disconnect();
            } catch(e) {
                console.warn('Error disconnecting WebSocket:', e);
            }
        }

        const socket = new SockJS(WEBSOCKET_CONFIG.WS_URL);
        this.stompClient = Stomp.over(socket);
        this.stompClient.debug = null;

        this.stompClient.connect({}, frame => {
            console.log('✅ WebSocket connected:', frame);
            this.isConnected = true;
            this.reconnectAttempts = 0;
            this.app.uiManager.updateWebSocketStatus(true);

            this.stompClient.subscribe('/topic/applications', message => {
                try {
                    const data = JSON.parse(message.body);
                    console.log('📨 WebSocket message received:', data);
                    this.handleWebSocketMessage(data);
                } catch (error) {
                    console.error('❌ WS message parse error:', error, 'Raw:', message.body);
                }
            });
        }, error => {
            console.error('❌ WebSocket error:', error);
            this.app.uiManager.updateWebSocketStatus(false);
            this.handleReconnect();
        });
    }

    handleReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);
            setTimeout(() => this.connect(), this.reconnectDelay);
        } else {
            this.app.uiManager.showNotification('Потеряно соединение с WS. Используйте кнопку "Обновить"', 'warning');
        }
    }

    handleWebSocketMessage(data) {
        console.log('Processing WebSocket message type:', data.type, 'Full data:', data);

        // Проверяем формат ResponseWrapper
        if (data && typeof data === 'object') {
            // Если это ResponseWrapper, извлекаем данные
            if (data.success !== undefined) {
                if (data.success === true) {
                    // Извлекаем данные из result
                    data = data.result || data;
                } else {
                    console.error('WebSocket error response:', data.error);
                    return;
                }
            }
        }

        // Обработка разных форматов сообщений
        switch(data.type) {
            case 'APPLICATION_CREATED':
                const newApp = data.application || data.payload || data;
                if (newApp && newApp.id) {
                    console.log('New application via WS:', newApp);
                    // Используем правильное имя метода
                    this.app.handleApplicationCreated(newApp);
                    this.app.uiManager.showNotification(`Новая заявка от ${newApp.name}`, 'info');
                } else {
                    console.error('No application data in message:', data);
                }
                break;

            case 'APPLICATION_UPDATED':
                const updatedApp = data.application || data.payload || data;
                if (updatedApp && updatedApp.id) {
                    console.log('Updated application via WS:', updatedApp);
                    // Используем правильное имя метода
                    this.app.handleApplicationUpdated(updatedApp);
                    this.app.uiManager.showNotification(`Заявка ${updatedApp.name} обновлена`, 'success');
                }
                break;

            case 'APPLICATION_DELETED':
                const appId = data.applicationId || data.payload || data.id;
                if (appId) {
                    console.log('Deleted application via WS:', appId);
                    // Используем правильное имя метода
                    this.app.handleApplicationDeleted(appId);
                    this.app.uiManager.showNotification('Заявка удалена', 'warning');
                }
                break;

            case 'STATS_UPDATED':
                const stats = data.stats || data.payload || data;
                if (stats) {
                    console.log('Stats updated via WS:', stats);
                    // Используем правильное имя метода
                    this.app.handleStatsUpdated(stats);
                }
                break;

            default:
                console.warn('Unknown WS message type:', data.type, 'Full:', data);
        }
    }

    disconnect() {
        if (this.stompClient) {
            this.stompClient.disconnect();
            this.stompClient = null;
            this.isConnected = false;
        }
    }
}

// ==========================
// Класс для управления модальными окнами
// ==========================
class ModalManager {
    constructor(app) {
        this.app = app;
    }

    showStatusModal(id, name) {
        this.app.currentApplicationId = id;
        document.getElementById('applicationIdText').textContent = name;
        document.getElementById('statusModal').classList.add('active');
    }

    showDeleteModal(id, name) {
        this.app.currentApplicationId = id;
        document.getElementById('deleteApplicationId').textContent = name;
        document.getElementById('deleteModal').classList.add('active');
    }

    async showEditModal(id) {
        try {
            this.app.currentApplicationId = id;
            const application = await this.app.apiClient.fetchApplicationById(id);
            this.fillEditModal(application);
        } catch (error) {
            console.warn('Error fetching application for edit:', error);
            const app = this.app.getApplicationById(id);
            if (app) {
                this.fillEditModal(app);
            } else {
                this.app.uiManager.showNotification('Ошибка загрузки заявки', 'error');
                return;
            }
        }

        document.getElementById('editModal').classList.add('active');
    }

    fillEditModal(application) {
        document.getElementById('editName').value = application.name || '';
        document.getElementById('editEmail').value = application.email || '';
        document.getElementById('editPhone').value = application.phone || '';
        document.getElementById('editMessage').value = application.message || '';
        document.getElementById('editCourseType').value = application.courseType || '';
        document.getElementById('editStatus').value = application.status || 'NEW';
        document.getElementById('editPriority').value = application.priority || 'MEDIUM';
        document.getElementById('editSource').value = application.source || 'Лендинг';

        // Для отладки можно добавить логирование
        console.log('Application data for edit:', application);
        console.log('Status:', application.status);
        console.log('Status display:', application.statusDisplay);
    }

    async changeApplicationStatus() {
        const newStatus = document.querySelector('input[name="newStatus"]:checked')?.value;
        if (!newStatus) {
            this.app.uiManager.showNotification('Выберите новый статус', 'error');
            return;
        }

        const statusData = {
            status: newStatus,
            processedBy: 'Администратор'
        };

        try {
            const updatedApp = await this.app.apiClient.updateApplicationStatus(
                this.app.currentApplicationId,
                statusData
            );

            this.app.syncApplication(updatedApp);
            this.app.uiManager.showNotification('Статус успешно изменен', 'success');
            this.closeAllModals();
        } catch (error) {
            console.error('Error changing status:', error);
            this.app.uiManager.showNotification(`Ошибка изменения статуса: ${error.message}`, 'error');
        }
    }

    async deleteApplication() {
        try {
            await this.app.apiClient.deleteApplication(this.app.currentApplicationId);

            this.app.removeApplication(this.app.currentApplicationId);
            this.app.uiManager.showNotification('Заявка успешно удалена', 'success');
            this.closeAllModals();
        } catch (error) {
            console.error('Error deleting application:', error);
            this.app.uiManager.showNotification(`Ошибка удаления: ${error.message}`, 'error');
        }
    }

    async saveApplicationChanges() {
        if (!this.app.currentApplicationId) {
            this.app.uiManager.showNotification('Ошибка: ID заявки не найден', 'error');
            return;
        }

        const updatedData = {
            name: document.getElementById('editName').value,
            email: document.getElementById('editEmail').value,
            phone: document.getElementById('editPhone').value,
            message: document.getElementById('editMessage').value,
            courseType: document.getElementById('editCourseType').value,
            status: document.getElementById('editStatus').value,
            priority: document.getElementById('editPriority').value,
            source: document.getElementById('editSource').value,
            processedBy: 'Администратор'
        };

        try {
            const updatedApp = await this.app.apiClient.updateApplication(
                this.app.currentApplicationId,
                updatedData
            );

            this.app.syncApplication(updatedApp);
            this.app.uiManager.showNotification('Заявка успешно обновлена', 'success');
            this.closeAllModals();
        } catch (error) {
            console.error('Error updating application:', error);
            this.app.uiManager.showNotification(`Ошибка обновления заявки: ${error.message}`, 'error');
        }
    }

    closeAllModals() {
        document.querySelectorAll('.modal').forEach(modal =>
            modal.classList.remove('active')
        );
    }
}

// ==========================
// Класс для управления фильтрами
// ==========================
class FilterManager {
    constructor(app) {
        this.app = app;
        this.activeFilters = {
            dateRange: { start: null, end: null },
            courseType: 'all',
            priorities: ['high', 'medium', 'low']
        };
    }

    applyFilters() {
        console.log('Applying filters...');

        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;
        const courseType = document.getElementById('courseType').value;

        // Получаем выбранные приоритеты
        const priorityCheckboxes = document.querySelectorAll('.priority-filters input[type="checkbox"]');
        const activePriorities = Array.from(priorityCheckboxes)
            .filter(cb => cb.checked)
            .map(cb => {
                const label = cb.closest('label');
                const priorityDot = label.querySelector('.priority-dot');
                return priorityDot?.classList[1] || ''; // high/medium/low
            })
            .filter(p => p); // Убираем пустые значения

        console.log('Filter settings:', {
            startDate,
            endDate,
            courseType,
            activePriorities
        });

        // Если нет активных приоритетов, показываем всё
        if (activePriorities.length === 0) {
            this.app.uiManager.renderApplications(this.app.applications);
            return;
        }

        // Фильтрация
        let filtered = this.app.applications.filter(app => {
            // Фильтр по дате
            if (startDate) {
                const appDate = new Date(app.createdAt);
                const filterStart = new Date(startDate);
                if (appDate < filterStart) return false;
            }

            if (endDate) {
                const appDate = new Date(app.createdAt);
                const filterEnd = new Date(endDate);
                filterEnd.setHours(23, 59, 59);
                if (appDate > filterEnd) return false;
            }

            // Фильтр по типу курса
            if (courseType && courseType !== 'all') {
                const appCourse = app.courseType?.toLowerCase() || '';
                const filterCourse = courseType.toLowerCase();

                // Сравниваем тип курса
                if (appCourse !== filterCourse) {
                    // Проверяем display значение
                    const courseDisplay = app.courseTypeDisplay?.toLowerCase() || '';
                    if (!courseDisplay.includes(filterCourse)) {
                        return false;
                    }
                }
            }

            // Фильтр по приоритетам
            const appPriority = app.priority?.toLowerCase() || 'medium';
            if (!activePriorities.includes(appPriority)) return false;

            return true;
        });

        console.log(`Filtered ${filtered.length} applications from ${this.app.applications.length}`);

        // Обновляем отображение
        this.app.uiManager.renderApplications(filtered);

        // Закрываем панель фильтров
        document.getElementById('filterPanel')?.classList.remove('active');
    }

    resetFilters() {
        console.log('Resetting filters...');

        // Сбрасываем значения фильтров
        const startDateInput = document.getElementById('startDate');
        const endDateInput = document.getElementById('endDate');
        const courseTypeSelect = document.getElementById('courseType');

        if (startDateInput) startDateInput.value = '';
        if (endDateInput) endDateInput.value = '';
        if (courseTypeSelect) courseTypeSelect.value = 'all';

        // Сбрасываем чекбоксы приоритетов
        const priorityCheckboxes = document.querySelectorAll('.priority-filters input[type="checkbox"]');
        priorityCheckboxes.forEach(cb => cb.checked = true);

        // Показываем все заявки
        this.app.uiManager.renderApplications(this.app.applications);

        // Закрываем панель фильтров
        document.getElementById('filterPanel')?.classList.remove('active');
    }
}

// ==========================
// Класс для управления выходом из системы
// ==========================
class LogoutManager {
    constructor(app) {
        this.app = app;
    }

    init() {
        console.log('Initializing logout system...');
        this.setupLogoutHandlers();
        console.log('Logout system initialized');
    }

    setupLogoutHandlers() {
        console.log('Setting up logout handlers...');

        // 1. Кнопка "Выйти" в сайдбаре
        const logoutLink = document.getElementById('logoutLink');
        if (logoutLink) {
            console.log('Found logout link:', logoutLink);

            // Исправляем курсор
            logoutLink.style.cursor = 'pointer';

            // Удаляем старые обработчики и добавляем новый
            logoutLink.onclick = null; // Очищаем onclick
            logoutLink.addEventListener('click', (event) => {
                event.preventDefault();
                event.stopPropagation();
                console.log('Logout link clicked');
                this.openLogoutModal();
                return false;
            });
        } else {
            console.error('Logout link not found!');
        }

        // 2. Кнопка "Да, выйти" в модальном окне
        const confirmLogoutButton = document.getElementById('confirmLogout');
        if (confirmLogoutButton) {
            console.log('Found confirm logout button:', confirmLogoutButton);

            confirmLogoutButton.onclick = null; // Очищаем onclick
            confirmLogoutButton.addEventListener('click', (event) => {
                event.preventDefault();
                event.stopPropagation();
                console.log('Confirm logout clicked');
                this.confirmLogout();
                return false;
            });
        } else {
            console.error('Confirm logout button not found!');
        }

        // 3. Кнопки закрытия модалок (крестики)
        document.querySelectorAll('.close-modal').forEach(btn => {
            btn.onclick = null;
            btn.addEventListener('click', () => {
                console.log('Close modal clicked');
                this.closeLogoutModal();
            });
        });

        // 4. Закрытие при клике на фон модалки
        const logoutModal = document.getElementById('logoutModal');
        if (logoutModal) {
            logoutModal.onclick = null;
            logoutModal.addEventListener('click', (event) => {
                if (event.target === logoutModal) {
                    console.log('Background click - closing modal');
                    this.closeLogoutModal();
                }
            });
        }

        console.log('Logout handlers setup complete');
    }

    openLogoutModal() {
        console.log('Opening logout modal...');
        const modal = document.getElementById('logoutModal');
        if (modal) {
            modal.classList.add('active');
            console.log('Modal opened successfully');
        } else {
            console.error('Logout modal not found!');
        }
    }

    closeLogoutModal() {
        const modal = document.getElementById('logoutModal');
        if (modal) {
            modal.classList.remove('active');
            console.log('Modal closed');
        }
    }

    confirmLogout() {
        console.log('Confirming logout...');
        this.closeLogoutModal();
        this.executeLogout();
    }

    executeLogout() {
        console.log('Executing logout...');

        // Показываем уведомление
        if (this.app.uiManager) {
            this.app.uiManager.showNotification('Выполняется выход из системы...', 'info');
        }

        // Создаем форму для выхода
        this.createLogoutForm();
    }

    createLogoutForm() {
        console.log('Creating logout form...');

        // Получаем CSRF токен
        const csrfMeta = document.querySelector('meta[name="_csrf"]');
        const csrfParamMeta = document.querySelector('meta[name="_csrf_parameter"]');

        const csrfToken = csrfMeta ? csrfMeta.content : null;
        const csrfParam = csrfParamMeta ? csrfParamMeta.content : '_csrf';

        console.log('CSRF token:', csrfToken ? 'found' : 'not found');

        // Создаем форму
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/logout';
        form.style.display = 'none';

        // Добавляем CSRF токен если есть
        if (csrfToken) {
            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = csrfParam;
            csrfInput.value = csrfToken;
            form.appendChild(csrfInput);
            console.log('CSRF input added');
        } else {
            console.warn('CSRF token not found!');
        }

        // Добавляем форму в документ
        document.body.appendChild(form);

        // Отправляем форму
        setTimeout(() => {
            console.log('Submitting logout form...');
            form.submit();
        }, 300);
    }
}

// ==========================
// Инициализация приложения
// ==========================
// Создаем глобальный экземпляр приложения
const adminApp = new AdminApplication();

// Инициализируем приложение
adminApp.init();

// Делаем глобальным для доступа из HTML атрибутов onclick в карточках
window.adminApp = adminApp;

// Дебаг функции для тестирования
window.debugCsrf = () => {
    console.log('CSRF Token:', adminApp.apiClient.getCsrfToken());
    console.log('Document cookie:', document.cookie);
};

window.testLogoutDirect = function() {
    console.log('=== DIRECT LOGOUT TEST ===');
    if (adminApp.logoutManager) {
        adminApp.logoutManager.openLogoutModal();
    } else {
        console.error('Logout manager not available!');
    }
};

// Проверка после загрузки
setTimeout(() => {
    console.log('=== ADMIN PANEL STATUS ===');
    console.log('Admin app:', adminApp);
    console.log('Logout link:', document.getElementById('logoutLink'));
    console.log('Logout modal:', document.getElementById('logoutModal'));
    console.log('Test logout in console: testLogoutDirect()');
}, 2000);

// В конце файла admin-script.js, перед закрывающей скобкой, добавьте:

// Фолбэк и дополнительные обработчики
document.addEventListener('DOMContentLoaded', function() {
    // Ждем немного, чтобы основной код успел инициализироваться
    setTimeout(function() {
        console.log('=== SETTING UP FALLBACK HANDLERS ===');

        const logoutLink = document.getElementById('logoutLink');
        const confirmLogoutButton = document.getElementById('confirmLogout');
        const logoutModal = document.getElementById('logoutModal');

        // 1. Обработчик для кнопки "Выйти"
        if (logoutLink) {
            console.log('Found logout link:', logoutLink);

            // Удаляем все старые обработчики (дополнительная безопасность)
            const newLogoutLink = logoutLink.cloneNode(true);
            logoutLink.parentNode.replaceChild(newLogoutLink, logoutLink);

            // Добавляем новый обработчик
            newLogoutLink.addEventListener('click', function(event) {
                event.preventDefault();
                event.stopPropagation();
                console.log('Fallback: Logout link clicked');

                if (window.adminApp && window.adminApp.logoutManager) {
                    console.log('Using OOP logout manager');
                    window.adminApp.logoutManager.openLogoutModal();
                } else if (logoutModal) {
                    console.log('Opening modal directly');
                    logoutModal.classList.add('active');
                } else {
                    console.error('Cannot open logout modal!');
                }
                return false;
            });
        } else {
            console.error('Logout link not found!');
        }

        // 2. Обработчик для кнопки "Да, выйти"
        if (confirmLogoutButton) {
            console.log('Found confirm logout button:', confirmLogoutButton);

            const newConfirmButton = confirmLogoutButton.cloneNode(true);
            confirmLogoutButton.parentNode.replaceChild(newConfirmButton, confirmLogoutButton);

            newConfirmButton.addEventListener('click', function(event) {
                event.preventDefault();
                event.stopPropagation();
                console.log('Fallback: Confirm logout clicked');

                // Закрываем модалку
                if (logoutModal) {
                    logoutModal.classList.remove('active');
                }

                // Выполняем выход
                if (window.adminApp && window.adminApp.logoutManager) {
                    console.log('Using OOP logout manager for logout');
                    window.adminApp.logoutManager.executeLogout();
                } else {
                    console.log('Executing direct logout');
                    executeDirectLogout();
                }
                return false;
            });
        } else {
            console.error('Confirm logout button not found!');
        }

        // 3. Обработчики для закрытия модалки
        document.querySelectorAll('.close-modal').forEach(function(btn) {
            btn.addEventListener('click', function() {
                console.log('Closing all modals (fallback)');
                document.querySelectorAll('.modal').forEach(function(modal) {
                    modal.classList.remove('active');
                });
            });
        });

        // 4. Закрытие при клике на фон модалки
        if (logoutModal) {
            logoutModal.addEventListener('click', function(event) {
                if (event.target === logoutModal) {
                    console.log('Closing modal on background click');
                    logoutModal.classList.remove('active');
                }
            });
        }

    }, 500);
});

// Простая функция для прямого выхода
function executeDirectLogout() {
    console.log('Executing direct logout...');

    // Получаем CSRF токен
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfParamMeta = document.querySelector('meta[name="_csrf_parameter"]');

    const csrfToken = csrfMeta ? csrfMeta.content : null;
    const csrfParam = csrfParamMeta ? csrfParamMeta.content : '_csrf';

    console.log('Direct logout CSRF:', {
        token: csrfToken ? csrfToken.substring(0, 20) + '...' : 'not found',
        param: csrfParam
    });

    // Создаем форму
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/logout';
    form.style.display = 'none';

    if (csrfToken) {
        const csrfInput = document.createElement('input');
        csrfInput.type = 'hidden';
        csrfInput.name = csrfParam;
        csrfInput.value = csrfToken;
        form.appendChild(csrfInput);
        console.log('CSRF input added to form');
    }

    document.body.appendChild(form);

    // Отправляем форму
    setTimeout(function() {
        console.log('Submitting logout form...');
        form.submit();
    }, 100);
}

// Создаем глобальные функции для тестирования
window.testLogoutModal = function() {
    console.log('Testing logout modal...');
    const modal = document.getElementById('logoutModal');
    if (modal) {
        modal.classList.add('active');
        console.log('Modal opened');
    } else {
        console.error('Modal not found!');
    }
};

window.testDirectLogout = function() {
    console.log('Testing direct logout...');
    executeDirectLogout();
};

// Тестовый код для проверки
// setTimeout(() => {
//     console.log('=== FILTER SYSTEM CHECK ===');
//     console.log('Filter button:', document.getElementById('filterBtn'));
//     console.log('Filter panel:', document.getElementById('filterPanel'));
//     console.log('Apply filters button:', document.getElementById('applyFilters'));
//     console.log('Reset filters button:', document.getElementById('resetFilters'));
//     console.log('Start date input:', document.getElementById('startDate'));
//     console.log('End date input:', document.getElementById('endDate'));
//     console.log('Course type select:', document.getElementById('courseType'));
//     console.log('Priority checkboxes:', document.querySelectorAll('.priority-filters input').length);
//
//     // Тестовая функция для ручного открытия фильтров
//     window.testFilters = function() {
//         const panel = document.getElementById('filterPanel');
//         if (panel) {
//             panel.classList.add('active');
//             console.log('Filters opened via test function');
//         }
//     };
// }, 1500);

// // Вспомогательная функция для отладки ResponseWrapper
// window.debugResponse = async function() {
//     try {
//         console.log('=== DEBUG RESPONSEWRAPPER ===');
//         const response = await fetch('/api/v1/admin/clients');
//         const data = await response.json();
//         console.log('Full response:', data);
//         console.log('Success:', data.success);
//         console.log('Result type:', typeof data.result);
//         console.log('Result length:', Array.isArray(data.result) ? data.result.length : 'N/A');
//         console.log('Error:', data.error);
//
//         if (Array.isArray(data.result) && data.result.length > 0) {
//             console.log('First application:', data.result[0]);
//             console.log('Application keys:', Object.keys(data.result[0]));
//         }
//     } catch (error) {
//         console.error('Debug error:', error);
//     }
// };
//
// // Тест загрузки заявок
// window.testLoadApplications = async function() {
//     console.log('=== TEST LOAD APPLICATIONS ===');
//     await adminApp.loadApplications();
// };
//
// // Проверка конфигурации
// window.checkConfig = function() {
//     console.log('=== CHECK CONFIG ===');
//     console.log('API Base URL:', API_CONFIG.BASE_URL);
//     console.log('Auth username:', API_CONFIG.AUTH.username);
//     console.log('CSRF Token:', adminApp.apiClient.getCsrfToken());
// };
//
// // В конце файла перед инициализацией
// window.debugUIManager = function() {
//     console.log('=== DEBUG UI MANAGER ===');
//     console.log('adminApp:', adminApp);
//     console.log('uiManager:', adminApp?.uiManager);
//     console.log('UIManager methods:', {
//         createApplicationCard: typeof adminApp?.uiManager?.createApplicationCard,
//         renderApplications: typeof adminApp?.uiManager?.renderApplications,
//         updateApplicationCard: typeof adminApp?.uiManager?.updateApplicationCard
//     });
//
//     if (adminApp?.uiManager?.createApplicationCard) {
//         console.log('createApplicationCard is a function, testing with sample data...');
//         const sampleApp = {
//             id: 'test-id-123',
//             name: 'Тестовый клиент',
//             email: 'test@example.com',
//             phone: '+79161234567',
//             status: 'NEW',
//             statusLabel: 'Новая',
//             priority: 'MEDIUM',
//             priorityLabel: 'Средний',
//             courseType: 'FULLSTACK',
//             courseTypeLabel: 'Фуллстек разработка',
//             source: 'Лендинг',
//             message: 'Тестовое сообщение',
//             createdAt: new Date().toISOString()
//         };
//
//         try {
//             const card = adminApp.uiManager.createApplicationCard(sampleApp);
//             console.log('Card created successfully:', card);
//             console.log('Card HTML:', card.outerHTML.substring(0, 200) + '...');
//         } catch (e) {
//             console.error('Error creating card:', e);
//         }
//     } else {
//         console.error('createApplicationCard is not available!');
//     }
// };