package mg.biznako.gestion;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.activity.result.ActivityResult;
import androidx.core.app.NotificationManagerCompat;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@CapacitorPlugin(name = "BiznakoNative", permissions = {
    @Permission(alias = "notifications", strings = {Manifest.permission.POST_NOTIFICATIONS})
})
public class BiznakoNativePlugin extends Plugin {
    private WebView printView;

    @PluginMethod public void saveFile(PluginCall call) {
        String data = call.getString("data");
        String filename = call.getString("filename", "Biznako.json");
        String mime = call.getString("mime", "application/json");
        if (data == null || data.length() > 12 * 1024 * 1024) { call.reject("Fichier absent ou trop volumineux."); return; }
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mime.startsWith("text/csv") ? "text/csv" : "application/json");
        intent.putExtra(Intent.EXTRA_TITLE, filename.replaceAll("[^a-zA-Z0-9._-]", "_"));
        startActivityForResult(call, intent, "fileCreated");
    }
    @ActivityCallback private void fileCreated(PluginCall call, ActivityResult result) {
        if (call == null) return;
        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null || result.getData().getData() == null) {
            call.reject("Enregistrement annulé."); return;
        }
        try (OutputStream output = getContext().getContentResolver().openOutputStream(result.getData().getData())) {
            if (output == null) throw new java.io.IOException("Destination inaccessible.");
            output.write(call.getString("data", "").getBytes(StandardCharsets.UTF_8));
            JSObject value = new JSObject(); value.put("saved", true); call.resolve(value);
        } catch (Exception e) { call.reject("Impossible d’enregistrer ce fichier.", e); }
    }
    @PluginMethod public void printDocument(PluginCall call) {
        String html = call.getString("html");
        if (html == null || html.length() > 5 * 1024 * 1024) { call.reject("Document absent ou trop volumineux."); return; }
        getActivity().runOnUiThread(() -> {
            try {
                if (printView != null) printView.destroy();
                printView = new WebView(getContext());
                printView.getSettings().setJavaScriptEnabled(false);
                printView.getSettings().setAllowFileAccess(false);
                printView.getSettings().setBlockNetworkLoads(true);
                printView.setWebViewClient(new WebViewClient() {
                    @Override public void onPageFinished(WebView view, String url) {
                        PrintManager manager = (PrintManager) getContext().getSystemService(Context.PRINT_SERVICE);
                        manager.print("Biznako", view.createPrintDocumentAdapter("Biznako"), new PrintAttributes.Builder().build());
                        call.resolve();
                    }
                });
                printView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
            } catch (Exception e) { call.reject("Impression indisponible.", e); }
        });
    }
    @PluginMethod public void scheduleReminder(PluginCall call) {
        if (Build.VERSION.SDK_INT >= 33 && getPermissionState("notifications") != PermissionState.GRANTED) {
            requestPermissionForAlias("notifications", call, "notificationPermission");
        } else scheduleGranted(call);
    }
    @PermissionCallback private void notificationPermission(PluginCall call) {
        if (getPermissionState("notifications") != PermissionState.GRANTED) { call.reject("Notifications refusées. Vous pouvez les autoriser dans les réglages Android."); return; }
        scheduleGranted(call);
    }
    private void scheduleGranted(PluginCall call) {
        if (!NotificationManagerCompat.from(getContext()).areNotificationsEnabled()) { call.reject("Notifications désactivées dans Android."); return; }
        Long at = call.getLong("at");
        if (at == null || at < System.currentTimeMillis() || at > System.currentTimeMillis() + 7L * 86400000L) { call.reject("Date de rappel invalide."); return; }
        Intent i = new Intent(getContext(), ReminderReceiver.class);
        i.putExtra("title", call.getString("title", "Biznako"));
        i.putExtra("body", call.getString("body", "Enregistrez vos opérations."));
        PendingIntent p = PendingIntent.getBroadcast(getContext(), 1001, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager manager = (AlarmManager)getContext().getSystemService(Context.ALARM_SERVICE);
        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, p);
        call.resolve();
    }
    @PluginMethod public void cancelReminder(PluginCall call) {
        Intent i = new Intent(getContext(), ReminderReceiver.class);
        PendingIntent p = PendingIntent.getBroadcast(getContext(),1001,i,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        ((AlarmManager)getContext().getSystemService(Context.ALARM_SERVICE)).cancel(p);
        NotificationManagerCompat.from(getContext()).cancel(1001);
        call.resolve();
    }
    @PluginMethod public void exitApp(PluginCall call) { call.resolve(); getActivity().runOnUiThread(() -> getActivity().finish()); }
    @Override protected void handleOnDestroy() { if (printView != null) { printView.destroy(); printView = null; } }
}
