package ec.edu.ups.icc.events.reports.services;

public interface ReportAccessService {
    void verifyEventReportAccess(Long eventId);
    void verifyCertificateAccess(Long registrationId);
}
