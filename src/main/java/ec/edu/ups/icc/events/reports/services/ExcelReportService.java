package ec.edu.ups.icc.events.reports.services;

public interface ExcelReportService {
    byte[] generateEventRegistrationsReport(Long eventId);
}