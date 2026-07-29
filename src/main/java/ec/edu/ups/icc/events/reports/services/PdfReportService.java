package ec.edu.ups.icc.events.reports.services;

public interface PdfReportService {
    byte[] generateEventRegistrationsReport(Long eventId);
    byte[] generateRegistrationCertificate(Long registrationId);
}