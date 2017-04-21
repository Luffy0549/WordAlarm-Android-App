package tedking.wordalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.Calendar;
import java.util.EventListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DealReceiver extends BroadcastReceiver {
    private AlarmManager manager;
    public DealReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        Intent intent1 = new Intent(context,MyService.class);
        context.startService(intent1);
        //Toast.makeText(context,"jieshoudao",Toast.LENGTH_LONG).show();
    }
}
