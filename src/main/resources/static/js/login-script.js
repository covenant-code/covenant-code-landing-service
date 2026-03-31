// ======================
// Конфигурация
// ======================
const CONFIG = {
    SESSION_DURATION: 24 * 60 * 60 * 1000, // 24 часа в миллисекундах
    STORAGE_KEY: 'covenant_admin_session',
    API_ENDPOINT: '/api/admin/login',
    DEMO_CREDENTIALS: {
        email: 'admin@covenantcode.ru',
        password: 'admin123'
    }
};

// ======================
// Инициализация
// ======================
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
    initializeForm();
    checkAuthStatus(); // Добавляем проверку
});

// ======================
// Инициализация формы
// ======================
function initializeForm() {
    const session = getSession();
    // Пробуем найти поле email по разным ID
    const emailInput = document.getElementById('email') || document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const rememberMeInput = document.getElementById('rememberMe');

    if (session?.email && emailInput) {
        emailInput.value = session.email;
        if (rememberMeInput) rememberMeInput.checked = !!session.expiresAt;
    }

    // Подставляем демо-данные на localhost
    if ((window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') && emailInput && passwordInput) {
        emailInput.value = CONFIG.DEMO_CREDENTIALS.email;
        passwordInput.value = CONFIG.DEMO_CREDENTIALS.password;
    }
}

// ======================
// Настройка событий
// ======================
function setupEventListeners() {
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLoginSubmit);
        // Убираем стандартное поведение формы
        loginForm.action = 'javascript:void(0);';
    }

    const togglePassword = document.getElementById('togglePassword');
    if (togglePassword) togglePassword.addEventListener('click', togglePasswordVisibility);

    const forgotPassword = document.getElementById('forgotPassword');
    if (forgotPassword) forgotPassword.addEventListener('click', handleForgotPassword);

    document.querySelectorAll('.close-modal').forEach(btn => btn.addEventListener('click', closeAllModals));
    document.querySelectorAll('.modal').forEach(modal =>
        modal.addEventListener('click', e => { if (e.target === modal) closeAllModals(); })
    );

    document.querySelectorAll('.btn-google, .btn-github').forEach(btn => btn.addEventListener('click', handleSocialLogin));

    const sendRecoveryBtn = document.getElementById('sendRecovery');
    if (sendRecoveryBtn) sendRecoveryBtn.addEventListener('click', handlePasswordRecovery);
}

// ======================
// Отправка формы login
// ======================
async function handleLoginSubmit(event) {
    event.preventDefault();
    event.stopPropagation();

    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const csrfTokenInput = document.getElementById('csrfToken');
    const rememberMeInput = document.getElementById('rememberMe');

    if (!usernameInput || !passwordInput) {
        showNotification('Ошибка: не найдены поля формы', 'error');
        return;
    }

    const username = usernameInput.value.trim();
    const password = passwordInput.value;
    const csrfToken = csrfTokenInput ? csrfTokenInput.value : '';
    const rememberMe = rememberMeInput?.checked ?? false;

    // Валидация
    if (!validateForm(username, password)) return;

    const submitBtn = document.getElementById('submitBtn');
    if (!submitBtn) return;

    const originalText = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Вход...';

    try {
        // Создаем FormData для отправки через стандартную форму
        const formData = new FormData();
        formData.append('username', username);
        formData.append('password', password);
        if (csrfToken) {
            formData.append('_csrf', csrfToken);
        }
        if (rememberMe) {
            formData.append('remember-me', 'true');
        }

        // Отправляем запрос
        const response = await fetch('/api/admin/login', {
            method: 'POST',
            body: formData,
            credentials: 'include' // Важно для сохранения сессии
        });

        if (response.redirected) {
            // Spring Security сделал редирект
            window.location.href = response.url;
            return;
        }

        if (response.ok) {
            // Успешный вход
            showNotification('Вход выполнен успешно!', 'success');

            // Сохраняем сессию в localStorage
            saveSession(username, rememberMe);

            // Перенаправляем на админку
            setTimeout(() => {
                window.location.href = '/admin';
            }, 1000);

        } else {
            // Проверяем, может быть это JSON ответ с ошибкой
            try {
                const data = await response.json();
                throw new Error(data.message || `Ошибка ${response.status}`);
            } catch {
                throw new Error(`Ошибка сервера: ${response.status}`);
            }
        }

    } catch (error) {
        console.error('Ошибка входа:', error);

        let errorMessage = error.message;
        if (error.message.includes('Failed to fetch') || error.message.includes('NetworkError')) {
            errorMessage = 'Ошибка сети. Проверьте подключение.';
        }

        showNotification(errorMessage, 'error');

        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
    }
}

// ======================
// Создание реальной сессии (fallback)
// ======================
async function createRealSession(email, password) {
    try {
        // Создаем форму для отправки через Spring Security
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/perform_login';
        form.style.display = 'none';

        const usernameField = document.createElement('input');
        usernameField.type = 'hidden';
        usernameField.name = 'username';
        usernameField.value = email;

        const passwordField = document.createElement('input');
        passwordField.type = 'hidden';
        passwordField.name = 'password';
        passwordField.value = password;

        form.appendChild(usernameField);
        form.appendChild(passwordField);
        document.body.appendChild(form);

        // Отправляем асинхронно
        const response = await fetch('/perform_login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `username=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`,
            credentials: 'include'
        });

        if (response.ok) {
            console.log('Spring Security сессия создана');
        }

        // Удаляем форму
        form.remove();

    } catch (error) {
        console.warn('Не удалось создать Spring Security сессию:', error);
    }
}

// ======================
// Добавление fallback кнопки для стандартной формы
// ======================
function addFallbackLoginButton(email, password) {
    const formActions = document.querySelector('.form-actions');
    if (!formActions) return;

    // Проверяем, не добавлена ли уже кнопка
    if (document.getElementById('fallbackLoginBtn')) return;

    const fallbackBtn = document.createElement('button');
    fallbackBtn.id = 'fallbackLoginBtn';
    fallbackBtn.type = 'button';
    fallbackBtn.className = 'btn btn-outline btn-block';
    fallbackBtn.style.marginTop = '10px';
    fallbackBtn.innerHTML = '<i class="fas fa-history"></i> Попробовать стандартный вход';
    fallbackBtn.addEventListener('click', () => {
        submitStandardForm(email, password);
    });

    formActions.appendChild(fallbackBtn);
}

// ======================
// Отправка стандартной формы Spring Security
// ======================
function submitStandardForm(email, password) {
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/perform_login';
    form.style.display = 'none';

    const usernameField = document.createElement('input');
    usernameField.type = 'hidden';
    usernameField.name = 'username';
    usernameField.value = email;

    const passwordField = document.createElement('input');
    passwordField.type = 'hidden';
    passwordField.name = 'password';
    passwordField.value = password;

    form.appendChild(usernameField);
    form.appendChild(passwordField);
    document.body.appendChild(form);
    form.submit();
}

// ======================
// Валидация формы
// ======================
function validateForm(email, password) {
    let isValid = true;
    clearErrors();

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!email) {
        showError('username', 'Email обязателен');
        isValid = false;
    } else if (!emailRegex.test(email)) {
        showError('username', 'Введите корректный email');
        isValid = false;
    }

    if (!password) {
        showError('password', 'Пароль обязателен');
        isValid = false;
    }

    return isValid;
}

// ======================
// Ошибки формы
// ======================
function showError(fieldId, message) {
    // Пробуем найти поле по email, если не найдено - по username
    let field = document.getElementById(fieldId);
    if (!field && fieldId === 'email') {
        field = document.getElementById('username');
    }

    if (!field) return;

    const parent = field.parentElement;
    field.classList.add('error');

    // Удаляем старые ошибки для этого поля
    const existingError = parent.querySelector('.error-message');
    if (existingError) existingError.remove();

    const errorElement = document.createElement('div');
    errorElement.className = 'error-message';
    errorElement.innerHTML = `<i class="fas fa-exclamation-circle"></i><span>${message}</span>`;
    parent.appendChild(errorElement);
}

function clearErrors() {
    // Ищем поля с классом error среди всех input
    document.querySelectorAll('input').forEach(input => {
        if (input.classList.contains('error')) {
            input.classList.remove('error');
        }
    });

    // Удаляем все сообщения об ошибках
    document.querySelectorAll('.error-message').forEach(el => el.remove());
}

// ======================
// Toggle password
// ======================
function togglePasswordVisibility(event) {
    const passwordInput = document.getElementById('password');
    if (!passwordInput) return;

    const toggleIcon = event.currentTarget.querySelector('i');
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        toggleIcon.classList.replace('fa-eye', 'fa-eye-slash');
        event.currentTarget.setAttribute('aria-label', 'Скрыть пароль');
    } else {
        passwordInput.type = 'password';
        toggleIcon.classList.replace('fa-eye-slash', 'fa-eye');
        event.currentTarget.setAttribute('aria-label', 'Показать пароль');
    }
}

// ======================
// "Забыли пароль"
// ======================
function handleForgotPassword(event) {
    event.preventDefault();
    const modal = document.getElementById('passwordModal');
    if (modal) {
        modal.classList.add('active');
        // Автозаполнение email из формы
        const emailInput = document.getElementById('email') || document.getElementById('username');
        const recoveryEmail = document.getElementById('recoveryEmail');
        if (emailInput && recoveryEmail) {
            recoveryEmail.value = emailInput.value;
        }
    }
}

// ======================
// Восстановление пароля
// ======================
async function handlePasswordRecovery() {
    const emailInput = document.getElementById('recoveryEmail');
    if (!emailInput) return;

    const email = emailInput.value.trim();
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!email || !emailRegex.test(email)) {
        showNotification('Введите корректный email', 'error');
        return;
    }

    const sendBtn = document.getElementById('sendRecovery');
    if (!sendBtn) return;

    const originalText = sendBtn.textContent;
    sendBtn.disabled = true;
    sendBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Отправка...';

    try {
        // Имитация запроса
        await new Promise(resolve => setTimeout(resolve, 1500));

        closeAllModals();
        showNotification('Инструкции по восстановлению пароля отправлены на email ' + email, 'success');

    } catch {
        showNotification('Ошибка при отправке инструкций', 'error');
    } finally {
        sendBtn.disabled = false;
        sendBtn.textContent = originalText;
    }
}

// ======================
// Социальный вход
// ======================
function handleSocialLogin(event) {
    event.preventDefault();
    const provider = event.currentTarget.classList.contains('btn-google') ? 'Google' : 'GitHub';
    showNotification(`Инициализация входа через ${provider}...`, 'info');

    setTimeout(() => {
        showNotification('Социальный вход временно недоступен. Используйте форму входа.', 'warning');
    }, 1500);
}

// ======================
// Модалки
// ======================
function closeAllModals() {
    document.querySelectorAll('.modal').forEach(m => m.classList.remove('active'));
}

// ======================
// LocalStorage session
// ======================
function getSession() {
    try {
        const sessionData = localStorage.getItem(CONFIG.STORAGE_KEY);
        if (!sessionData) return null;

        const session = JSON.parse(sessionData);

        // Проверка срока действия
        if (session.expiresAt && Date.now() > session.expiresAt) {
            clearSession(); // Используем функцию очистки
            return null;
        }

        return session;
    } catch (error) {
        console.error('Ошибка чтения сессии:', error);
        clearSession(); // Используем функцию очистки
        return null;
    }
}

function saveSession(email, rememberMe = false) {
    try {
        const now = Date.now();
        const expiresAt = rememberMe ? now + CONFIG.SESSION_DURATION : null;
        const session = {
            email,
            loggedInAt: now,
            expiresAt,
            isValid: true
        };

        localStorage.setItem(CONFIG.STORAGE_KEY, JSON.stringify(session));
        console.log('Сессия сохранена в localStorage');
    } catch (error) {
        console.error('Ошибка сохранения сессии:', error);
    }
}

// НОВАЯ ФУНКЦИЯ: Очистка сессии
function clearSession() {
    try {
        localStorage.removeItem(CONFIG.STORAGE_KEY);
        console.log('Сессия удалена из localStorage');
    } catch (error) {
        console.error('Ошибка удаления сессии:', error);
    }
}

// ======================
// Уведомления
// ======================
function showNotification(message, type = 'info') {
    const container = document.getElementById('notificationContainer');
    if (!container) return;

    // Удаляем старые уведомления
    const oldNotifications = container.querySelectorAll('.notification');
    oldNotifications.forEach(notification => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    });

    const notification = document.createElement('div');
    notification.className = `notification ${type}`;

    const icons = {
        success: 'fa-check-circle',
        error: 'fa-exclamation-circle',
        warning: 'fa-exclamation-triangle',
        info: 'fa-info-circle'
    };

    const iconClass = icons[type] || 'fa-info-circle';

    notification.innerHTML = `
        <div class="notification-content">
            <div class="notification-icon">
                <i class="fas ${iconClass}"></i>
            </div>
            <div class="notification-message">${message}</div>
        </div>
        <button class="notification-close" aria-label="Закрыть">
            <i class="fas fa-times"></i>
        </button>
    `;

    container.appendChild(notification);

    // Закрытие по клику
    const closeBtn = notification.querySelector('.notification-close');
    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            notification.classList.remove('show');
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.remove();
                }
            }, 300);
        });
    }

    // Автоматическое закрытие для success и error
    if (type === 'success' || type === 'error') {
        setTimeout(() => {
            if (notification.parentNode) {
                notification.classList.remove('show');
                setTimeout(() => {
                    if (notification.parentNode) {
                        notification.remove();
                    }
                }, 300);
            }
        }, 5000);
    }

    // Показываем с анимацией
    setTimeout(() => notification.classList.add('show'), 10);
}

// ======================
// Выход из системы
// ======================
function setupLogout() {
    const logoutLink = document.querySelector('.nav-link.logout');
    if (logoutLink) {
        logoutLink.addEventListener('click', function(e) {
            e.preventDefault();
            performLogout();
        });
    }
}

// Функция для выхода
function performLogout() {
    showConfirmation('Вы уверены, что хотите выйти из системы?', 'Выйти', 'Отмена')
        .then(confirmed => {
            if (confirmed) {
                // Создаем форму для отправки CSRF токена
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = '/logout';

                // Добавляем CSRF токен
                const csrfInput = document.createElement('input');
                csrfInput.type = 'hidden';
                csrfInput.name = csrfParameterName || '_csrf';
                csrfInput.value = csrfToken || getCsrfTokenFromMeta();

                form.appendChild(csrfInput);
                document.body.appendChild(form);
                form.submit();
            }
        });
}

// Вспомогательная функция для получения CSRF токена
function getCsrfTokenFromMeta() {
    const metaTag = document.querySelector('meta[name="_csrf"]');
    return metaTag ? metaTag.getAttribute('content') : '';
}

// Функция показа подтверждения
function showConfirmation(message, confirmText, cancelText) {
    return new Promise((resolve) => {
        const modal = document.getElementById('logoutModal');
        if (!modal) {
            resolve(confirm('Вы уверены, что хотите выйти?'));
            return;
        }

        const confirmBtn = document.getElementById('confirmLogout');
        const cancelBtn = modal.querySelector('.close-modal');

        // Обновляем текст
        modal.querySelector('.modal-body p').textContent = message;
        if (confirmBtn) confirmBtn.textContent = confirmText;
        if (cancelBtn) cancelBtn.textContent = cancelText;

        // Показываем модальное окно
        modal.style.display = 'block';

        // Обработчики
        const confirmHandler = () => {
            cleanup();
            resolve(true);
        };

        const cancelHandler = () => {
            cleanup();
            resolve(false);
        };

        const closeHandler = (e) => {
            if (e.target === modal) {
                cleanup();
                resolve(false);
            }
        };

        function cleanup() {
            modal.style.display = 'none';
            confirmBtn.removeEventListener('click', confirmHandler);
            cancelBtn.removeEventListener('click', cancelHandler);
            modal.removeEventListener('click', closeHandler);
        }

        confirmBtn.addEventListener('click', confirmHandler);
        cancelBtn.addEventListener('click', cancelHandler);
        modal.addEventListener('click', closeHandler);
    });
}

// ======================
// Дополнительные утилиты
// ======================
// Проверка авторизации при загрузке страницы
function checkAuthStatus() {
    const currentPage = window.location.pathname;

    // Проверяем, не на странице ли мы выхода
    if (currentPage.includes('login.html') && window.location.search.includes('logout=true')) {
        clearSession(); // Очищаем сессию при разлогинивании
        showNotification('Вы успешно вышли из системы', 'success');
        // Убираем параметр logout из URL
        window.history.replaceState({}, document.title, window.location.pathname);
        return;
    }

    // Проверяем, не на странице ли мы выхода с параметром logout
    if (window.location.search.includes('logout')) {
        clearSession();
        // Убираем параметры из URL
        window.history.replaceState({}, document.title, window.location.pathname);
        return;
    }

    const session = getSession();

    // Если на странице логина и есть активная сессия - спрашиваем что делать
    if (currentPage.includes('login.html') && session?.isValid) {
        // Вместо автоматического перенаправления показываем сообщение
        showNotification('У вас есть активная сессия. Хотите продолжить?', 'info');

        // Добавляем кнопки выбора
        setTimeout(() => {
            const container = document.getElementById('notificationContainer');
            if (!container) return;

            const choiceNotification = document.createElement('div');
            choiceNotification.className = 'notification info';
            choiceNotification.innerHTML = `
                <div class="notification-content">
                    <div class="notification-icon">
                        <i class="fas fa-info-circle"></i>
                    </div>
                    <div class="notification-message">
                        <p>У вас есть активная сессия.</p>
                        <div class="session-actions">
                            <button id="continueSession" class="btn-small btn-primary">
                                <i class="fas fa-arrow-right"></i> Продолжить
                            </button>
                            <button id="logoutSession" class="btn-small btn-outline">
                                <i class="fas fa-sign-out-alt"></i> Выйти
                            </button>
                        </div>
                    </div>
                </div>
                <button class="notification-close" aria-label="Закрыть">
                    <i class="fas fa-times"></i>
                </button>
            `;

            container.appendChild(choiceNotification);
            setTimeout(() => choiceNotification.classList.add('show'), 10);

            // Обработчики кнопок
            document.getElementById('continueSession')?.addEventListener('click', () => {
                window.location.href = 'admin.html';
            });

            document.getElementById('logoutSession')?.addEventListener('click', () => {
                clearSession();
                // Отправляем запрос на сервер для выхода
                fetch('/logout', {
                    method: 'POST',
                    credentials: 'include'
                }).then(() => {
                    choiceNotification.remove();
                    showNotification('Сессия завершена. Войдите снова.', 'success');
                });
            });

            // Закрытие уведомления
            choiceNotification.querySelector('.notification-close')?.addEventListener('click', () => {
                choiceNotification.classList.remove('show');
                setTimeout(() => choiceNotification.remove(), 300);
            });
        }, 500);

        return;
    }

    // Если на странице админки и нет сессии - перенаправляем на логин
    if (currentPage.includes('admin.html') && !session?.isValid) {
        window.location.href = 'login.html';
    }
}

// Добавьте этот CSS для кнопок в уведомлении
const style = document.createElement('style');
style.textContent = `
    .session-actions {
        display: flex;
        gap: 8px;
        margin-top: 8px;
    }
    
    .btn-small {
        padding: 6px 12px;
        font-size: 12px;
        border-radius: 4px;
        cursor: pointer;
        border: 1px solid transparent;
        font-weight: 500;
        transition: all 0.2s;
    }
    
    .btn-small.btn-primary {
        background-color: #3b82f6;
        color: white;
    }
    
    .btn-small.btn-primary:hover {
        background-color: #2563eb;
    }
    
    .btn-small.btn-outline {
        background-color: transparent;
        border-color: #d1d5db;
        color: #4b5563;
    }
    
    .btn-small.btn-outline:hover {
        background-color: #f3f4f6;
    }
`;
document.head.appendChild(style);

// Экспорт для тестирования (опционально)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        CONFIG,
        validateForm,
        saveSession,
        getSession,
        clearSession,
        showNotification
    };
}