package net.ceriat.clgd.ccemux;

import javax.swing.text.DateFormatter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {
    public String format(LogRecord record) {
        StringBuffer buf = new StringBuffer();

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
