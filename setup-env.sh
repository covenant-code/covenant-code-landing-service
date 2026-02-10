#!/bin/bash
# setup-env.sh - Скрипт для настройки переменных окружения

set -e  # Выход при ошибке

echo "=== Настройка окружения для Covenant Landing ==="
echo ""

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Имена файлов
ENV_EXAMPLE=".env.example"
ENV_FILE=".env"

# Проверка наличия примера
if [ ! -f "$ENV_EXAMPLE" ]; then
    echo -e "${RED}❌ Файл $ENV_EXAMPLE не найден!${NC}"
    echo "Текущая директория: $(pwd)"
    echo "Содержимое:"
    ls -la | grep -E "\.env|credential"
    exit 1
fi

echo -e "${GREEN}✓ Найден файл примера: $ENV_EXAMPLE${NC}"

# Проверка и создание .env
if [ ! -f "$ENV_FILE" ]; then
    echo -e "${YELLOW}📝 Создание $ENV_FILE из шаблона...${NC}"
    cp "$ENV_EXAMPLE" "$ENV_FILE"

    echo ""
    echo -e "${YELLOW}⚠️  ВАЖНО! Файл $ENV_FILE создан.${NC}"
    echo -e "${YELLOW}   Отредактируйте его и замените все значения:${NC}"
    echo ""

    # Показываем какие значения нужно заменить
    echo "Значения для замены:"
    grep -E "your_|changeme|password|secret" "$ENV_FILE" || true

    echo ""
    echo -e "${GREEN}   Редактирование: nano $ENV_FILE${NC}"

    # Предлагаем открыть файл для редактирования
    read -p "Открыть файл для редактирования? (y/n): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        if command -v nano &> /dev/null; then
            nano "$ENV_FILE"
        elif command -v vim &> /dev/null; then
            vim "$ENV_FILE"
        elif command -v vi &> /dev/null; then
            vi "$ENV_FILE"
        else
            echo "Текстовые редакторы не найдены. Откройте файл вручную."
            open "$ENV_FILE" 2>/dev/null || echo "Используйте: open $ENV_FILE"
        fi
    fi
else
    echo -e "${GREEN}✓ Файл $ENV_FILE уже существует${NC}"
fi

echo ""
echo -e "${YELLOW}🔍 Проверка заполнения переменных...${NC}"

# Проверка на оставшиеся значения по умолчанию
if grep -q -E "your_|changeme|REPLACE_ME" "$ENV_FILE" 2>/dev/null; then
    echo -e "${RED}❌ Обнаружены незаполненные переменные:${NC}"
    grep -E "your_|changeme|REPLACE_ME" "$ENV_FILE"
    echo ""
    echo -e "${RED}   Отредактируйте $ENV_FILE перед запуском!${NC}"
    echo -e "${RED}   Используйте: nano $ENV_FILE${NC}"
    exit 1
fi

# Проверка минимальной длины паролей
check_password() {
    local var_name=$1
    local password=$(grep "^${var_name}=" "$ENV_FILE" 2>/dev/null | cut -d'=' -f2-)

    if [ -n "$password" ] && [ "${#password}" -lt 8 ]; then
        echo -e "${YELLOW}⚠️  Внимание: $var_name слишком короткий (меньше 8 символов)${NC}"
        return 1
    fi
    return 0
}

echo ""
echo -e "${GREEN}✅ Окружение настроено корректно!${NC}"
echo ""
echo "Следующие шаги:"
echo "1. Сборка проекта: mvn clean package -DskipTests"
echo "2. Запуск: docker-compose up --build"
echo "3. Приложение будет доступно по: http://localhost:8082"
echo ""
echo "Текущие настройки (без значений):"
grep -E "^[A-Z_]+=" "$ENV_FILE" | cut -d'=' -f1 | sort
echo ""