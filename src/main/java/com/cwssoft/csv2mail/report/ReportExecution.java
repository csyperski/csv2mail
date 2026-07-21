package com.cwssoft.csv2mail.report;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.dupageroe.vroe.model.auth.UserDto;
import org.dupageroe.vroe.reports.LiteReportDto;
import org.dupageroe.vroe.reports.Report;
import org.dupageroe.vroe.reports.ReportExecution;
import org.dupageroe.vroe.reports.ReportParam;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.dupageroe.vroe.reports.ReportParam.TYPE_BOOLEAN;
import static org.dupageroe.vroe.reports.ReportParam.TYPE_LONG;

public class ReportRunner  {



    @Override
    public ReportExecution execute(int reportType, int uuid, UserDto userDto, List<ReportParam> params) {
        if (userDto == null || !userDto.isEnabled()) {
            return new ReportExecution(false, null, "Unable to start, either user or report UUID is invalid!", 0l);
        }

        boolean forcedCsv = params != null && params.stream().anyMatch( p -> p.getKey().equalsIgnoreCase("dataonly") && p.isValueBoolean() );

        // let's add a couple 'Special' params
        if ( params == null ) {
            params = new ArrayList<>();
        }

        final ReportParam reportParam = new ReportParam("userId", "User ID", "This is the user running this report", TYPE_LONG, false);
        reportParam.setValueLong(userDto.getId());
        params.add(reportParam);

        final List<LiteReportDto> nonDbReports = reportService.getReportsAvailableForUser(userDto);
        final List<LiteReportDto> dbReports = dbReportService.getReportsAvailableForUser(userDto);

        boolean isNonDb = nonDbReports.stream().anyMatch( r -> r.getUuid() == uuid );
        boolean isDb = dbReports.stream().anyMatch( r -> r.getUuid() == uuid );

        int realReportType = reportType;
        if ( realReportType == Report.TYPE_PDF ) {
            if ( forcedCsv ) {
                realReportType = Report.TYPE_CSV;
            }
        }

        if ( isDb ) {
            log.info("Report is a DB based report, executing now: {}", uuid );
            return dbReportService.execute(realReportType, uuid, userDto, params);
        } else if ( isNonDb ) {
            log.info("Report is a non-db base report, executing now: {}", uuid);
            return reportService.execute(realReportType, uuid, userDto, params);
        } else {
            log.warn("Unable to locate report with UUID: {}", uuid);
            ReportExecution reportExecution = new ReportExecution();
            reportExecution.setExecutionInMillis(0);
            reportExecution.setData(null);
            reportExecution.setError("Unable to locate report with UUID of " + uuid);
            reportExecution.setSuccessful(false);
            return reportExecution;
        }
    }
}
