package ec.edu.ups.icc.events.reports.services;

import ec.edu.ups.icc.events.core.exceptions.ResourceNotFoundException;
import ec.edu.ups.icc.events.events.entities.EventEntity;
import ec.edu.ups.icc.events.events.repositories.EventRepository;
import ec.edu.ups.icc.events.registrations.entities.RegistrationEntity;
import ec.edu.ups.icc.events.registrations.repositories.RegistrationRepository;
import ec.edu.ups.icc.events.reports.services.ExcelReportService;
import ec.edu.ups.icc.events.reports.utils.ReportDateTimeUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ExcelReportServiceImpl implements ExcelReportService {

    private static final String[] HEADERS = {
            "N.º",
            "ID inscripción",
            "Nombre",
            "Correo electrónico",
            "Estado",
            "Fecha de inscripción"
    };

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;

    public ExcelReportServiceImpl(
            EventRepository eventRepository,
            RegistrationRepository registrationRepository
    ) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
    }

    @Override
    public byte[] generateEventRegistrationsReport(Long eventId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Evento no encontrado con id: " + eventId
                        )
                );

        List<RegistrationEntity> registrations =
                registrationRepository
                        .findByEventIdOrderByRegistrationDateAsc(eventId);

        try (
                Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {
            Sheet sheet = workbook.createSheet("Inscritos");

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle labelStyle = createLabelStyle(workbook);
            CellStyle valueStyle = createValueStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle bodyStyle = createBodyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            createTitle(sheet, titleStyle);

            createEventInformation(
                    sheet,
                    event,
                    registrations.size(),
                    labelStyle,
                    valueStyle,
                    dateStyle
            );

            int headerRowIndex = 12;

            createTableHeader(
                    sheet,
                    headerRowIndex,
                    headerStyle
            );

            createRegistrationRows(
                    sheet,
                    headerRowIndex + 1,
                    registrations,
                    bodyStyle,
                    dateStyle
            );

            configureColumns(sheet);

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "No se pudo generar el reporte Excel",
                    exception
            );
        }
    }

    private void createTitle(
            Sheet sheet,
            CellStyle titleStyle
    ) {
        Row universityRow = sheet.createRow(0);
        Cell universityCell = universityRow.createCell(0);

        universityCell.setCellValue(
                "UNIVERSIDAD POLITÉCNICA SALESIANA"
        );
        universityCell.setCellStyle(titleStyle);

        sheet.addMergedRegion(
                new CellRangeAddress(
                        0,
                        0,
                        0,
                        HEADERS.length - 1
                )
        );

        Row reportRow = sheet.createRow(1);
        Cell reportCell = reportRow.createCell(0);

        reportCell.setCellValue(
                "Reporte de participantes inscritos"
        );
        reportCell.setCellStyle(titleStyle);

        sheet.addMergedRegion(
                new CellRangeAddress(
                        1,
                        1,
                        0,
                        HEADERS.length - 1
                )
        );
    }

    private void createEventInformation(
            Sheet sheet,
            EventEntity event,
            int registrationCount,
            CellStyle labelStyle,
            CellStyle valueStyle,
            CellStyle dateStyle
    ) {
        writeTextPair(
                sheet,
                3,
                "Evento:",
                event.getTitle(),
                labelStyle,
                valueStyle
        );

        writeTextPair(
                sheet,
                4,
                "ID del evento:",
                String.valueOf(event.getId()),
                labelStyle,
                valueStyle
        );

        writeTextPair(
                sheet,
                5,
                "Modalidad:",
                String.valueOf(event.getModality()),
                labelStyle,
                valueStyle
        );

        writeTextPair(
                sheet,
                6,
                "Ubicación:",
                event.getLocation() == null
                        ? "No especificada"
                        : event.getLocation(),
                labelStyle,
                valueStyle
        );

        writeUtcDatePair(
                sheet,
                7,
                "Fecha de inicio:",
                event.getStartDate(),
                labelStyle,
                dateStyle
        );

        writeUtcDatePair(
                sheet,
                8,
                "Fecha de finalización:",
                event.getEndDate(),
                labelStyle,
                dateStyle
        );

        writeLocalDatePair(
                sheet,
                9,
                "Fecha de emisión:",
                ReportDateTimeUtils.now().toLocalDateTime(),
                labelStyle,
                dateStyle
        );

        writeTextPair(
                sheet,
                10,
                "Zona horaria:",
                "America/Guayaquil",
                labelStyle,
                valueStyle
        );

        writeTextPair(
                sheet,
                11,
                "Total de inscripciones:",
                String.valueOf(registrationCount),
                labelStyle,
                valueStyle
        );
    }

    private void createTableHeader(
            Sheet sheet,
            int rowIndex,
            CellStyle headerStyle
    ) {
        Row row = sheet.createRow(rowIndex);

        for (int column = 0;
             column < HEADERS.length;
             column++) {

            Cell cell = row.createCell(column);
            cell.setCellValue(HEADERS[column]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void createRegistrationRows(
            Sheet sheet,
            int firstRowIndex,
            List<RegistrationEntity> registrations,
            CellStyle bodyStyle,
            CellStyle dateStyle
    ) {
        int rowIndex = firstRowIndex;
        int number = 1;

        for (RegistrationEntity registration : registrations) {
            Row row = sheet.createRow(rowIndex++);

            createTextCell(
                    row,
                    0,
                    String.valueOf(number++),
                    bodyStyle
            );

            createTextCell(
                    row,
                    1,
                    String.valueOf(registration.getId()),
                    bodyStyle
            );

            createTextCell(
                    row,
                    2,
                    registration.getUser().getName(),
                    bodyStyle
            );

            createTextCell(
                    row,
                    3,
                    registration.getUser().getEmail(),
                    bodyStyle
            );

            createTextCell(
                    row,
                    4,
                    String.valueOf(registration.getStatus()),
                    bodyStyle
            );

            Cell dateCell = row.createCell(5);

            LocalDateTime businessDate =
                    ReportDateTimeUtils
                            .toBusinessLocalDateTime(
                                    registration.getRegistrationDate()
                            );

            if (businessDate != null) {
                dateCell.setCellValue(businessDate);
            }

            dateCell.setCellStyle(dateStyle);
        }
    }

    private void writeTextPair(
            Sheet sheet,
            int rowIndex,
            String label,
            String value,
            CellStyle labelStyle,
            CellStyle valueStyle
    ) {
        Row row = sheet.createRow(rowIndex);

        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value == null ? "" : value);
        valueCell.setCellStyle(valueStyle);

        sheet.addMergedRegion(
                new CellRangeAddress(
                        rowIndex,
                        rowIndex,
                        1,
                        HEADERS.length - 1
                )
        );
    }

    private void writeUtcDatePair(
            Sheet sheet,
            int rowIndex,
            String label,
            LocalDateTime utcDateTime,
            CellStyle labelStyle,
            CellStyle dateStyle
    ) {
        LocalDateTime businessDate =
                ReportDateTimeUtils
                        .toBusinessLocalDateTime(utcDateTime);

        writeLocalDatePair(
                sheet,
                rowIndex,
                label,
                businessDate,
                labelStyle,
                dateStyle
        );
    }

    private void writeLocalDatePair(
            Sheet sheet,
            int rowIndex,
            String label,
            LocalDateTime dateTime,
            CellStyle labelStyle,
            CellStyle dateStyle
    ) {
        Row row = sheet.createRow(rowIndex);

        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);

        Cell valueCell = row.createCell(1);

        if (dateTime != null) {
            valueCell.setCellValue(dateTime);
        }

        valueCell.setCellStyle(dateStyle);

        sheet.addMergedRegion(
                new CellRangeAddress(
                        rowIndex,
                        rowIndex,
                        1,
                        HEADERS.length - 1
                )
        );
    }

    private void createTextCell(
            Row row,
            int columnIndex,
            String value,
            CellStyle style
    ) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value == null ? "" : value);
        cell.setCellStyle(style);
    }

    private CellStyle createTitleStyle(
            Workbook workbook
    ) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 15);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    private CellStyle createLabelStyle(
            Workbook workbook
    ) {
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle style = createBorderedStyle(workbook);
        style.setFont(font);

        return style;
    }

    private CellStyle createValueStyle(
            Workbook workbook
    ) {
        return createBorderedStyle(workbook);
    }

    private CellStyle createHeaderStyle(
            Workbook workbook
    ) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());

        CellStyle style = createBorderedStyle(workbook);
        style.setFont(font);
        style.setFillForegroundColor(
                IndexedColors.DARK_BLUE.getIndex()
        );
        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    private CellStyle createBodyStyle(
            Workbook workbook
    ) {
        CellStyle style = createBorderedStyle(workbook);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    private CellStyle createDateStyle(
            Workbook workbook
    ) {
        CellStyle style = createBorderedStyle(workbook);

        short format = workbook
                .getCreationHelper()
                .createDataFormat()
                .getFormat("dd/mm/yyyy hh:mm");

        style.setDataFormat(format);

        return style;
    }

    private CellStyle createBorderedStyle(
            Workbook workbook
    ) {
        CellStyle style = workbook.createCellStyle();

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    private void configureColumns(Sheet sheet) {
        for (int column = 0;
             column < HEADERS.length;
             column++) {

            sheet.autoSizeColumn(column);

            int adjustedWidth =
                    sheet.getColumnWidth(column) + 1000;

            sheet.setColumnWidth(
                    column,
                    Math.min(adjustedWidth, 15000)
            );
        }

        sheet.createFreezePane(0, 13);
    }
}
