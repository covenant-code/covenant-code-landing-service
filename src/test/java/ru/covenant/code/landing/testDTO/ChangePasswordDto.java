package ru.covenant.code.landing.testDTO;

import ru.covenant.code.landing.validation.constraints.PasswordMatch;

@PasswordMatch
public class ChangePasswordDto {
    private String newPassword;
    private String confirmPassword;


    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public ChangePasswordDto(String newPassword, String confirmPassword) {
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }
}
