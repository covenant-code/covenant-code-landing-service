// landing.js

// Мобильное меню
function setupMobileMenu() {
    const menuBtn = document.getElementById('menuBtn');
    const headerNav = document.querySelector('.header__nav');

    if (menuBtn && headerNav) {
        menuBtn.addEventListener('click', () => {
            headerNav.classList.toggle('active');
        });
    }
}

// Плавная прокрутка
function setupSmoothScroll() {
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('href');
            if(targetId === '#') return;

            const targetElement = document.querySelector(targetId);
            if(targetElement) {
                window.scrollTo({
                    top: targetElement.offsetTop - 80,
                    behavior: 'smooth'
                });

                // Закрываем мобильное меню
                const headerNav = document.querySelector('.header__nav');
                if (headerNav) {
                    headerNav.classList.remove('active');
                }
            }
        });
    });
}

// Маска для телефона
function applyPhoneMask(input) {
    let value = input.value.replace(/\D/g, '');

    // Если первый символ 8, меняем на +7
    if (value.startsWith('8') && value.length === 11) {
        value = '7' + value.substring(1);
    }

    // Если номер начинается не с 7, добавляем +7
    if (!value.startsWith('7') && value.length > 0) {
        value = '7' + value;
    }

    let formattedValue = '+7';

    if (value.length > 1) {
        formattedValue += ' (' + value.substring(1, 4);
    }
    if (value.length >= 4) {
        formattedValue += ') ' + value.substring(4, 7);
    }
    if (value.length >= 7) {
        formattedValue += '-' + value.substring(7, 9);
    }
    if (value.length >= 9) {
        formattedValue += '-' + value.substring(9, 11);
    }

    // Убираем лишние символы если пользователь удаляет
    if (formattedValue === '+7') {
        formattedValue = '';
    }

    input.value = formattedValue;

    // Позиционируем курсор в конец
    setTimeout(() => {
        input.selectionStart = input.selectionEnd = formattedValue.length;
    }, 0);
}

// Очистка телефонного номера перед отправкой
function cleanPhoneNumber(phone) {
    return phone.replace(/\D/g, '').replace(/^7/, '7');
}

// Настройка маски телефона
function setupPhoneMask() {
    const phoneInput = document.getElementById('phone');

    if (phoneInput) {
        // Применяем маску при вводе
        phoneInput.addEventListener('input', function() {
            applyPhoneMask(this);
        });

        // При фокусе добавляем +7 если поле пустое
        phoneInput.addEventListener('focus', function() {
            if (!this.value) {
                this.value = '+7 (';
                setTimeout(() => {
                    this.selectionStart = this.selectionEnd = 4;
                }, 0);
            }
        });

        // Не даем удалить +7
        phoneInput.addEventListener('keydown', function(e) {
            if (e.key === 'Backspace' && this.value === '+7 (') {
                e.preventDefault();
                this.value = '';
            }
        });

        // Устанавливаем плейсхолдер
        if (!phoneInput.value) {
            phoneInput.placeholder = '+7 (999) 999-99-99';
        }
    }
}

// Отправка формы заявки
async function submitApplicationForm(formData) {
    try {
        const response = await fetch('/api/v1/clients', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData)
        });

        if (response.ok) {
            const result = await response.json();
            return { success: true, message: 'Заявка успешно отправлена! Мы свяжемся с вами в ближайшее время.' };
        } else {
            return { success: false, message: 'Ошибка при отправке заявки. Пожалуйста, попробуйте еще раз.' };
        }
    } catch (error) {
        console.error('Error:', error);
        return { success: false, message: 'Ошибка сети. Пожалуйста, проверьте соединение и попробуйте еще раз.' };
    }
}

// Настройка формы
function setupApplicationForm() {
    const form = document.getElementById('applicationForm');

    if (form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();

            // Очищаем телефон от маски
            let phoneValue = document.getElementById('phone').value;
            let cleanedPhone = phoneValue ? cleanPhoneNumber(phoneValue) : null;

            // Проверяем валидность телефона
            if (cleanedPhone && cleanedPhone.length !== 11) {
                alert('Пожалуйста, введите корректный номер телефона (+7 XXX XXX-XX-XX)');
                document.getElementById('phone').focus();
                return;
            }

            const formData = {
                name: document.getElementById('name').value,
                email: document.getElementById('email').value,
                phone: cleanedPhone,
                courseType: 'FULLSTACK',
                message: 'Заявка с лендинга',
                source: 'Лендинг'
            };

            // Показываем индикатор загрузки
            const submitBtn = form.querySelector('button[type="submit"]');
            const originalText = submitBtn.innerHTML;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Отправка...';
            submitBtn.disabled = true;

            try {
                const result = await submitApplicationForm(formData);
                alert(result.message);

                if (result.success) {
                    form.reset();
                }
            } finally {
                // Восстанавливаем кнопку
                submitBtn.innerHTML = originalText;
                submitBtn.disabled = false;
            }
        });
    }
}

// Инициализация всех функций при загрузке DOM
document.addEventListener('DOMContentLoaded', function() {
    console.log('Initializing landing page scripts...');

    setupMobileMenu();
    setupSmoothScroll();
    setupPhoneMask();
    setupApplicationForm();

    console.log('Landing page scripts initialized successfully');
});

// Экспорт функций для использования в консоли (отладка)
window.Landing = {
    applyPhoneMask,
    cleanPhoneNumber,
    setupPhoneMask
};