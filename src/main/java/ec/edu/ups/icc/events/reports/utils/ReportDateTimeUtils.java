package ec.edu.ups.icc.events.reports.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class ReportDateTimeUtils {

    public static final ZoneId BUSINESS_ZONE =
            ZoneId.of("America/Guayaquil");

    private static final ZoneId DATABASE_ZONE =
            ZoneOffset.UTC;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ReportDateTimeUtils() {
        // Evita crear instancias de esta clase de utilidad.
    }

    /**
     * Interpreta la fecha recibida como UTC y la convierte
     * a la zona horaria de negocio America/Guayaquil.
     */
    public static ZonedDateTime toBusinessZone(
            LocalDateTime utcDateTime
    ) {
        if (utcDateTime == null) {
            return null;
        }

        return utcDateTime
                .atZone(DATABASE_ZONE)
                .withZoneSameInstant(BUSINESS_ZONE);
    }

    /**
     * Devuelve un LocalDateTime ajustado para escribirlo
     * directamente en una celda de Excel.
     */
    public static LocalDateTime toBusinessLocalDateTime(
            LocalDateTime utcDateTime
    ) {
        ZonedDateTime businessDateTime =
                toBusinessZone(utcDateTime);

        return businessDateTime == null
                ? null
                : businessDateTime.toLocalDateTime();
    }

    public static String format(
            LocalDateTime utcDateTime
    ) {
        ZonedDateTime businessDateTime =
                toBusinessZone(utcDateTime);

        return businessDateTime == null
                ? ""
                : businessDateTime.format(
                        DATE_TIME_FORMATTER
                );
    }

    public static String format(
            ZonedDateTime dateTime
    ) {
        if (dateTime == null) {
            return "";
        }

        return dateTime
                .withZoneSameInstant(BUSINESS_ZONE)
                .format(DATE_TIME_FORMATTER);
    }

    public static ZonedDateTime now() {
        return ZonedDateTime.now(BUSINESS_ZONE);
    }
}