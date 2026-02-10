package ru.covenant.code.landing.mapper;

import ru.covenant.code.landing.dto.client.response.ClientsStatsRsDto;

public interface StatsMapper {

    ClientsStatsRsDto mapStats(
            long total, long newCount, long processedCount, long doneCount,
            long todayCount, long fullstackCount, long frontendCount,
            long backendCount, long highPriorityCount, long mediumPriorityCount,
            long lowPriorityCount);
}