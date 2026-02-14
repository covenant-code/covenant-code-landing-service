package ru.covenant.code.landing.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter @Setter
public class AdminPermissionException extends BusinessException {

    private final String requiredPermission;
    private final String currentPermission;

    public AdminPermissionException(String requiredPermission, String currentPermission) {
        super("ADMIN_PERMISSION_DENIED",
                "Недостаточно прав",
                "Требуется роль '%s', текущая роль '%s'".formatted(requiredPermission, currentPermission),
                HttpStatus.FORBIDDEN,
                Map.of("requiredPermission", requiredPermission,
                        "currentPermission", currentPermission));
        this.requiredPermission = requiredPermission;
        this.currentPermission = currentPermission;
    }
}
