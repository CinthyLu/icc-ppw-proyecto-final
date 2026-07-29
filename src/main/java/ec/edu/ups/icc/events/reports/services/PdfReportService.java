package ec.edu.ups.icc.events.reports.services;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import ec.edu.ups.icc.events.core.exceptions.ResourceNotFoundException;
import ec.edu.ups.icc.events.events.entities.EventEntity;
import ec.edu.ups.icc.events.events.repositories.EventRepository;
import ec.edu.ups.icc.events.registrations.entities.RegistrationEntity;
import ec.edu.ups.icc.events.registrations.repositories.RegistrationRepository;
import ec.edu.ups.icc.events.reports.utils.ReportDateTimeUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PdfReportService {

    private static final String LOGO_PATH =
            "static/images/ups-logo.png";

    private static final Color UPS_RED =
            new Color(175, 30, 45);

    private static final Color DARK_BLUE =
            new Color(35, 63, 92);

    private static final Color LIGHT_GRAY =
            new Color(242, 242, 242);

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;

    public PdfReportService(
            EventRepository eventRepository,
            RegistrationRepository registrationRepository
    ) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
    }

    public byte[] generateEventRegistrationsReport(
            Long eventId
    ) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Evento no encontrado con id: "
                                        + eventId
                        )
                );

        List<RegistrationEntity> registrations =
                registrationRepository
                        .findByEventIdOrderByRegistrationDateAsc(
                                eventId
                        );

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        Document document = new Document(
                PageSize.A4.rotate(),
                35,
                35,
                30,
                30
        );

        try {
            PdfWriter.getInstance(
                    document,
                    outputStream
            );

            document.open();

            addLogo(document);
            addInstitutionHeader(document);
            addReportTitle(
                    document,
                    "REPORTE DE PARTICIPANTES INSCRITOS"
            );

            document.add(
                    createEventInformationTable(
                            event,
                            registrations.size()
                    )
            );

            Paragraph spacing = new Paragraph(" ");
            spacing.setSpacingAfter(5);
            document.add(spacing);

            document.add(
                    createRegistrationsTable(registrations)
            );

            addFooterInformation(document);

            document.close();

            return outputStream.toByteArray();

        } catch (DocumentException exception) {
            if (document.isOpen()) {
                document.close();
            }

            throw new IllegalStateException(
                    "No se pudo generar el reporte PDF",
                    exception
            );
        }
    }

    public byte[] generateRegistrationCertificate(
            Long registrationId
    ) {
        RegistrationEntity registration =
                registrationRepository
                        .findById(registrationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Inscripción no encontrada "
                                                + "con id: "
                                                + registrationId
                                )
                        );

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        Document document = new Document(
                PageSize.A4,
                55,
                55,
                45,
                45
        );

        try {
            PdfWriter.getInstance(
                    document,
                    outputStream
            );

            document.open();

            addLogo(document);
            addInstitutionHeader(document);
            addReportTitle(
                    document,
                    "COMPROBANTE DE INSCRIPCIÓN"
            );

            Paragraph introduction = new Paragraph(
                    "La Universidad Politécnica Salesiana "
                            + "certifica que la siguiente persona "
                            + "se encuentra registrada en el evento "
                            + "académico detallado a continuación.",
                    bodyFont()
            );

            introduction.setAlignment(
                    Element.ALIGN_JUSTIFIED
            );
            introduction.setSpacingBefore(15);
            introduction.setSpacingAfter(20);

            document.add(introduction);

            document.add(
                    createCertificateInformationTable(
                            registration
                    )
            );

            Paragraph verificationTitle = new Paragraph(
                    "Código de verificación",
                    labelFont()
            );

            verificationTitle.setAlignment(
                    Element.ALIGN_CENTER
            );
            verificationTitle.setSpacingBefore(25);
            verificationTitle.setSpacingAfter(8);

            document.add(verificationTitle);

            Paragraph verificationCode = new Paragraph(
                    createVerificationCode(registration),
                    verificationFont()
            );

            verificationCode.setAlignment(
                    Element.ALIGN_CENTER
            );
            verificationCode.setSpacingAfter(20);

            document.add(verificationCode);

            Paragraph emission = new Paragraph(
                    "Documento emitido el "
                            + ReportDateTimeUtils.format(
                                    ReportDateTimeUtils.now()
                            )
                            + " en la zona horaria "
                            + "America/Guayaquil.",
                    smallFont()
            );

            emission.setAlignment(Element.ALIGN_CENTER);
            emission.setSpacingBefore(15);

            document.add(emission);

            document.close();

            return outputStream.toByteArray();

        } catch (DocumentException exception) {
            if (document.isOpen()) {
                document.close();
            }

            throw new IllegalStateException(
                    "No se pudo generar el certificado PDF",
                    exception
            );
        }
    }

    private void addLogo(Document document)
            throws DocumentException {

        ClassPathResource logoResource =
                new ClassPathResource(LOGO_PATH);

        if (!logoResource.exists()) {
            return;
        }

        try (
                InputStream inputStream =
                        logoResource.getInputStream()
        ) {
            Image logo = Image.getInstance(
                    inputStream.readAllBytes()
            );

            logo.scaleToFit(120, 65);
            logo.setAlignment(Element.ALIGN_CENTER);
            logo.setSpacingAfter(7);

            document.add(logo);

        } catch (Exception ignored) {
            // El documento continúa generándose
            // aunque el logo no pueda cargarse.
        }
    }

    private void addInstitutionHeader(
            Document document
    ) throws DocumentException {

        Paragraph institution = new Paragraph(
                "UNIVERSIDAD POLITÉCNICA SALESIANA",
                institutionFont()
        );

        institution.setAlignment(Element.ALIGN_CENTER);
        institution.setSpacingAfter(5);

        document.add(institution);
    }

    private void addReportTitle(
            Document document,
            String title
    ) throws DocumentException {

        Paragraph titleParagraph = new Paragraph(
                title,
                titleFont()
        );

        titleParagraph.setAlignment(Element.ALIGN_CENTER);
        titleParagraph.setSpacingAfter(18);

        document.add(titleParagraph);
    }

    private PdfPTable createEventInformationTable(
            EventEntity event,
            int totalRegistrations
    ) throws DocumentException {

        PdfPTable table = new PdfPTable(2);

        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.4f, 4.6f});
        table.setSpacingAfter(12);

        addInformationRow(
                table,
                "Evento",
                text(event.getTitle())
        );

        addInformationRow(
                table,
                "Modalidad",
                text(event.getModality())
        );

        addInformationRow(
                table,
                "Ubicación",
                event.getLocation() == null
                        ? "No especificada"
                        : event.getLocation()
        );

        addInformationRow(
                table,
                "Fecha de inicio",
                ReportDateTimeUtils.format(
                        event.getStartDate()
                )
        );

        addInformationRow(
                table,
                "Fecha de finalización",
                ReportDateTimeUtils.format(
                        event.getEndDate()
                )
        );

        addInformationRow(
                table,
                "Total de inscritos",
                String.valueOf(totalRegistrations)
        );

        addInformationRow(
                table,
                "Fecha de emisión",
                ReportDateTimeUtils.format(
                        ReportDateTimeUtils.now()
                )
        );

        addInformationRow(
                table,
                "Zona horaria",
                "America/Guayaquil"
        );

        return table;
    }

    private PdfPTable createRegistrationsTable(
            List<RegistrationEntity> registrations
    ) throws DocumentException {

        PdfPTable table = new PdfPTable(6);

        table.setWidthPercentage(100);
        table.setWidths(
                new float[]{
                        0.7f,
                        1.2f,
                        2.2f,
                        3.2f,
                        1.4f,
                        2.1f
                }
        );

        table.setHeaderRows(1);

        addHeaderCell(table, "No.");
        addHeaderCell(table, "ID");
        addHeaderCell(table, "Nombre");
        addHeaderCell(table, "Correo electrónico");
        addHeaderCell(table, "Estado");
        addHeaderCell(table, "Fecha de inscripción");

        if (registrations.isEmpty()) {
            PdfPCell emptyCell = new PdfPCell(
                    new Phrase(
                            "No existen participantes inscritos "
                                    + "en este evento.",
                            bodyFont()
                    )
            );

            emptyCell.setColspan(6);
            emptyCell.setPadding(10);
            emptyCell.setHorizontalAlignment(
                    Element.ALIGN_CENTER
            );

            table.addCell(emptyCell);

            return table;
        }

        int number = 1;

        for (RegistrationEntity registration
                : registrations) {

            addBodyCell(
                    table,
                    String.valueOf(number++)
            );

            addBodyCell(
                    table,
                    String.valueOf(registration.getId())
            );

            addBodyCell(
                    table,
                    registration.getUser().getName()
            );

            addBodyCell(
                    table,
                    registration.getUser().getEmail()
            );

            addBodyCell(
                    table,
                    text(registration.getStatus())
            );

            addBodyCell(
                    table,
                    ReportDateTimeUtils.format(
                            registration.getRegistrationDate()
                    )
            );
        }

        return table;
    }

    private PdfPTable createCertificateInformationTable(
            RegistrationEntity registration
    ) throws DocumentException {

        PdfPTable table = new PdfPTable(2);

        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.7f, 4.3f});
        table.setSpacingBefore(10);
        table.setSpacingAfter(15);

        addInformationRow(
                table,
                "ID de inscripción",
                String.valueOf(registration.getId())
        );

        addInformationRow(
                table,
                "Nombre del participante",
                registration.getUser().getName()
        );

        addInformationRow(
                table,
                "Correo electrónico",
                registration.getUser().getEmail()
        );

        addInformationRow(
                table,
                "Evento",
                registration.getEvent().getTitle()
        );

        addInformationRow(
                table,
                "Modalidad",
                text(
                        registration
                                .getEvent()
                                .getModality()
                )
        );

        addInformationRow(
                table,
                "Ubicación",
                registration.getEvent().getLocation() == null
                        ? "No especificada"
                        : registration
                                .getEvent()
                                .getLocation()
        );

        addInformationRow(
                table,
                "Inicio del evento",
                ReportDateTimeUtils.format(
                        registration
                                .getEvent()
                                .getStartDate()
                )
        );

        addInformationRow(
                table,
                "Fin del evento",
                ReportDateTimeUtils.format(
                        registration
                                .getEvent()
                                .getEndDate()
                )
        );

        addInformationRow(
                table,
                "Fecha de inscripción",
                ReportDateTimeUtils.format(
                        registration.getRegistrationDate()
                )
        );

        addInformationRow(
                table,
                "Estado",
                text(registration.getStatus())
        );

        return table;
    }

    private void addInformationRow(
            PdfPTable table,
            String label,
            String value
    ) {
        PdfPCell labelCell = new PdfPCell(
                new Phrase(label, labelFont())
        );

        labelCell.setPadding(7);
        labelCell.setBackgroundColor(LIGHT_GRAY);
        labelCell.setBorderColor(Color.LIGHT_GRAY);

        PdfPCell valueCell = new PdfPCell(
                new Phrase(value, bodyFont())
        );

        valueCell.setPadding(7);
        valueCell.setBorderColor(Color.LIGHT_GRAY);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addHeaderCell(
            PdfPTable table,
            String value
    ) {
        PdfPCell cell = new PdfPCell(
                new Phrase(value, tableHeaderFont())
        );

        cell.setPadding(7);
        cell.setBackgroundColor(DARK_BLUE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(Color.WHITE);

        table.addCell(cell);
    }

    private void addBodyCell(
            PdfPTable table,
            String value
    ) {
        PdfPCell cell = new PdfPCell(
                new Phrase(
                        value == null ? "" : value,
                        smallFont()
                )
        );

        cell.setPadding(6);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(Color.LIGHT_GRAY);

        table.addCell(cell);
    }

    private void addFooterInformation(
            Document document
    ) throws DocumentException {

        Paragraph footer = new Paragraph(
                "Reporte emitido el "
                        + ReportDateTimeUtils.format(
                                ReportDateTimeUtils.now()
                        )
                        + " | Zona horaria: "
                        + "America/Guayaquil",
                smallFont()
        );

        footer.setAlignment(Element.ALIGN_RIGHT);
        footer.setSpacingBefore(15);

        document.add(footer);
    }

    private String createVerificationCode(
            RegistrationEntity registration
    ) {
        return String.format(
                "UPS-E%06d-R%06d-U%06d",
                registration.getEvent().getId(),
                registration.getId(),
                registration.getUser().getId()
        );
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Font institutionFont() {
        return new Font(
                Font.HELVETICA,
                14,
                Font.BOLD,
                UPS_RED
        );
    }

    private Font titleFont() {
        return new Font(
                Font.HELVETICA,
                17,
                Font.BOLD,
                DARK_BLUE
        );
    }

    private Font labelFont() {
        return new Font(
                Font.HELVETICA,
                10,
                Font.BOLD,
                Color.BLACK
        );
    }

    private Font bodyFont() {
        return new Font(
                Font.HELVETICA,
                10,
                Font.NORMAL,
                Color.BLACK
        );
    }

    private Font smallFont() {
        return new Font(
                Font.HELVETICA,
                8,
                Font.NORMAL,
                Color.DARK_GRAY
        );
    }

    private Font tableHeaderFont() {
        return new Font(
                Font.HELVETICA,
                9,
                Font.BOLD,
                Color.WHITE
        );
    }

    private Font verificationFont() {
        return new Font(
                Font.COURIER,
                14,
                Font.BOLD,
                UPS_RED
        );
    }
}