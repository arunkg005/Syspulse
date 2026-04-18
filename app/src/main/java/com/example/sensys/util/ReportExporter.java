package com.example.sensys.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportExporter {
    public static File exportTextReport(Context context, String reportText) throws IOException {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (dir == null) throw new IOException("Cannot access external storage");
        String timestamp = new SimpleDateFormat("hh-mm-ss-a_yyyy-MM-dd", Locale.US).format(new Date());
        File file = new File(dir, "report_" + timestamp + ".txt");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(reportText.getBytes(StandardCharsets.UTF_8));
        }
        return file;
    }
}
