package ec.edu.ups.icc.events.reports.controllers;

import ec.edu.ups.icc.events.reports.services.ExcelReportService;
import ec.edu.ups.icc.events.reports.services.PdfReportService;
import ec.edu.ups.icc.events.reports.services.ReportAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@Tag(name = "Reportes", description = "Descarga de reportes en Excel y PDF")
public class ReportController {

    private static final MediaType EXCEL_MEDIA_TYPE =
            MediaType.parseMediaType(
                    "application/vnd.openxmlformats-"
                            + "officedocument.spreadsheetml.sheet"
            );

    private final ExcelReportService excelReportService;
    private final PdfReportService pdfReportService;
    private final ReportAccessService reportAccessService;

    public ReportController(
            ExcelReportService excelReportService,
            PdfReportService pdfReportService,
            ReportAccessService reportAccessService
    ) {
        this.excelReportService = excelReportService;
        this.pdfReportService = pdfReportService;
        this.reportAccessService = reportAccessService;
    }

    @Operation(summary = "Descargar reporte Excel de inscripciones", description = "Genera un archivo Excel con las inscripciones del evento indicado.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Archivo generado", content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping(
            "/api/reports/events/{eventId}/registrations.xlsx"
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<byte[]> downloadEventRegistrationsExcel(
            @Parameter(description = "Identificador del evento") @PathVariable Long eventId
    ) {
        reportAccessService.verifyEventReportAccess(eventId);

        byte[] report =
                excelReportService
                        .generateEventRegistrationsReport(eventId);

        return createDownloadResponse(
                report,
                "evento-" + eventId + "-inscripciones.xlsx",
                EXCEL_MEDIA_TYPE
        );
    }

    @Operation(summary = "Descargar reporte PDF de inscripciones", description = "Genera un archivo PDF con las inscripciones del evento indicado.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Archivo generado", content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping(
            "/api/reports/events/{eventId}/registrations.pdf"
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<byte[]> downloadEventRegistrationsPdf(
            @Parameter(description = "Identificador del evento") @PathVariable Long eventId
    ) {
        reportAccessService.verifyEventReportAccess(eventId);

        byte[] report =
                pdfReportService
                        .generateEventRegistrationsReport(eventId);

        return createDownloadResponse(
                report,
                "evento-" + eventId + "-inscripciones.pdf",
                MediaType.APPLICATION_PDF
        );
    }

    @Operation(summary = "Descargar certificado de inscripción", description = "Genera un comprobante PDF para una inscripción específica.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Certificado generado", content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping(
            "/api/registrations/{id}/certificate.pdf"
    )
    @PreAuthorize("hasAnyRole('PARTICIPANT', 'ADMIN')")
    public ResponseEntity<byte[]> downloadRegistrationCertificate(
            @Parameter(description = "Identificador de la inscripción") @PathVariable Long id
    ) {
        reportAccessService.verifyCertificateAccess(id);

        byte[] certificate =
                pdfReportService
                        .generateRegistrationCertificate(id);

        return createDownloadResponse(
                certificate,
                "comprobante-inscripcion-" + id + ".pdf",
                MediaType.APPLICATION_PDF
        );
    }

    private ResponseEntity<byte[]> createDownloadResponse(
            byte[] content,
            String filename,
            MediaType mediaType
    ) {
        String contentDisposition =
                ContentDisposition
                        .attachment()
                        .filename(
                                filename,
                                StandardCharsets.UTF_8
                        )
                        .build()
                        .toString();

        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .contentLength(content.length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition
                )
                .body(content);
    }
}