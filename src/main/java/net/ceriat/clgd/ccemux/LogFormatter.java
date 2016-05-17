package net.ceriat.clgd.ccemux;

import javax.swing.text.DateFormatter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {
    /**
     * Formats a log record like this:
     * <code>[LEVEL][yyyy/MM/dd HH:mm:ss z] (message)</code>
     *
     * Example:
     * <code>[INFO][2016/05/17 16:09:22 CEST] Hello World!</code>
     *
     * @param record The log record.
     * @return The formatted log record.
     */
    public String format(LogRecord record) {
        StringBuilder buf = new StringBuilder();

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(record.getMillis());
        Date date = cal.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");

        buf.append("[" + record.getLevel().toString() + "]");
        buf.append("[" + sdf.format(date) + "] " + record.getMessage());
        buf.append("\n");

        return buf.toString();
    }
}
