package com.comp576.soundhealth;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.Toast;

import com.google.type.Date;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import de.siegmar.fastcsv.writer.CsvWriter;
/**
 * Class to handle generating a csv file from the room database.
 * Saves csv to external storage and passes list of data rows to main activity to
 * display in the share dialog
 */
public class Exporter extends AppCompatActivity {
    DataRepository repo;
    Context context;
    File file;
    List<String[]> csvData;

    public Exporter(DataRepository repo,Context context) {
        this.repo = repo;
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }
/**
 * generate csv and write to external storage
 */
    public File saveCSV(){
        Cursor cursor = repo.getCursor();
        csvData = new ArrayList<>();
        csvData.add(Arrays.copyOfRange(cursor.getColumnNames(),0,7));
        while (cursor.moveToNext()){
            csvData.add(new String[]{String.valueOf(cursor.getLong(0)),cursor.getString(1),cursor.getString(2),cursor.getString(3),String.valueOf(cursor.getDouble(4)),String.valueOf(cursor.getDouble(5)),String.valueOf(cursor.getDouble(6))});
        }

        File exportPath = context.getExternalCacheDir();
        file = new File(exportPath +"/"+ "SH_data_"+ new SimpleDateFormat("dd-MM-yy").format(Calendar.getInstance().getTime()) +".csv");
        CsvWriter csvWriter = new CsvWriter();

        try {
            csvWriter.write(file, StandardCharsets.UTF_8, csvData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public List<String[]> getCSV(){
        return csvData;
    }
}
