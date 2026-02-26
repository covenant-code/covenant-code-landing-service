package ru.covenant.code.landing.testDTO;

import ru.covenant.code.landing.validation.constraints.ValidDateRange;

import java.time.LocalDate;


@ValidDateRange(startDateField = "startDate", endDateField = "endDate")
public class DateRangeDto {

    private LocalDate startDate;
    private LocalDate endDate;

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
