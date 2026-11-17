package mg.biznako.gestion;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.AlertDialog;
import android.text.InputType;
import android.text.InputFilter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;
import android.content.ClipData;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.activity.result.ActivityResult;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.ContextCompat;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
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
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

@CapacitorPlugin(name = "BiznakoNative", permissions = {
    @Permission(alias = "notifications", strings = {Manifest.permission.POST_NOTIFICATIONS})
})
public class BiznakoNativePlugin extends Plugin {
    private WebView printView;

    private static final String SECURITY_PREFS = "biznako_security";
    private static final String PIN_KEY_ALIAS = "biznako_pin_hmac_v1";

    @PluginMethod public void ensureUnlocked(PluginCall call) {
        getActivity().runOnUiThread(() -> {
            if (!hasPin()) { showPinSetup(call, false); return; }
            authenticate(call, () -> { JSObject result = new JSObject(); result.put("unlocked", true); call.resolve(result); });
        });
    }

    @PluginMethod public void changePin(PluginCall call) {
        getActivity().runOnUiThread(() -> {
            if (!hasPin()) { showPinSetup(call, false); return; }
            authenticate(call, () -> showPinSetup(call, true));
        });
    }

    private boolean hasPin() {
        return getContext().getSharedPreferences(SECURITY_PREFS, Context.MODE_PRIVATE).contains("pin_hash");
    }

    private void authenticate(PluginCall call, Runnable onSuccess) {
        int status = BiometricManager.from(getContext()).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
        if (status != BiometricManager.BIOMETRIC_SUCCESS) { showPinDialog(call, onSuccess); return; }
        BiometricPrompt prompt = new BiometricPrompt(getActivity(), ContextCompat.getMainExecutor(getContext()), new BiometricPrompt.AuthenticationCallback() {
            @Override public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) { onSuccess.run(); }
            @Override public void onAuthenticationError(int errorCode, CharSequence errString) {
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) showPinDialog(call, onSuccess);
                else call.reject("Déverrouillage annulé. Réessayez pour ouvrir Biznako.");
            }
            @Override public void onAuthenticationFailed() { }
        });
        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
            .setTitle("Déverrouiller Biznako")
            .setSubtitle("La biométrie est prioritaire ; utilisez le PIN si nécessaire.")
            .setNegativeButtonText("Utiliser mon PIN")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build();
        prompt.authenticate(info);
    }

    private void showPinSetup(PluginCall call, boolean changing) {
        LinearLayout box = new LinearLayout(getContext()); box.setOrientation(LinearLayout.VERTICAL);
        int pad = (int)(20 * getContext().getResources().getDisplayMetrics().density); box.setPadding(pad, pad, pad, 0);
        EditText pin = new EditText(getContext()); pin.setHint("PIN · 4 à 12 chiffres"); pin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD); pin.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        EditText confirm = new EditText(getContext()); confirm.setHint("Confirmer le PIN"); confirm.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD); confirm.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        box.addView(pin); box.addView(confirm);
        AlertDialog dialog = new AlertDialog.Builder(getActivity()).setTitle(changing ? "Nouveau code PIN" : "Protéger Biznako")
            .setMessage("La biométrie sera proposée en priorité. Le PIN reste le secours.")
            .setView(box).setNegativeButton("Annuler", (d,w) -> call.reject("Configuration du PIN annulée."))
            .setPositiveButton("Enregistrer", null).create();
        dialog.setOnShowListener(v -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String a = pin.getText().toString(), b = confirm.getText().toString();
            if (!a.matches("[0-9]{4,12}")) { pin.setError("Saisissez 4 à 12 chiffres."); return; }
            if (!a.equals(b)) { confirm.setError("Les deux codes ne correspondent pas."); return; }
            try { storePin(a); dialog.dismiss(); JSObject result = new JSObject(); result.put("configured", true); call.resolve(result); }
            catch (Exception e) { pin.setError("Enregistrement sécurisé indisponible."); }
        }));
        dialog.show();
    }

    private void showPinDialog(PluginCall call, Runnable onSuccess) {
        EditText pin = new EditText(getContext()); pin.setHint("Code PIN"); pin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        pin.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        AlertDialog dialog = new AlertDialog.Builder(getActivity()).setTitle("Code PIN de secours")
            .setMessage("La biométrie n’est pas disponible ou a été annulée.")
            .setView(pin).setNegativeButton("Annuler", (d,w) -> call.reject("Déverrouillage annulé."))
            .setPositiveButton("Déverrouiller", null).create();
        dialog.setOnShowListener(v -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            try { verifyPin(pin.getText().toString()); dialog.dismiss(); onSuccess.run(); }
            catch (Exception e) { pin.setError(e.getMessage()); pin.setText(""); }
        }));
        dialog.show();
    }

    private void storePin(String pin) throws Exception {
        byte[] salt = new byte[16]; new SecureRandom().nextBytes(salt);
        byte[] hash = pinMac(salt, pin);
        getContext().getSharedPreferences(SECURITY_PREFS, Context.MODE_PRIVATE).edit()
            .putString("pin_salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString("pin_hash", Base64.encodeToString(hash, Base64.NO_WRAP))
            .putInt("pin_failures", 0).putLong("pin_blocked_until", 0).apply();
    }

    private void verifyPin(String pin) throws Exception {
        android.content.SharedPreferences prefs = getContext().getSharedPreferences(SECURITY_PREFS, Context.MODE_PRIVATE);
        long blocked = prefs.getLong("pin_blocked_until", 0);
        if (System.currentTimeMillis() < blocked) throw new Exception("Trop d’essais. Réessayez plus tard.");
        byte[] salt = Base64.decode(prefs.getString("pin_salt", ""), Base64.NO_WRAP);
        byte[] expected = Base64.decode(prefs.getString("pin_hash", ""), Base64.NO_WRAP);
        boolean valid = pin.matches("[0-9]{4,12}") && MessageDigest.isEqual(expected, pinMac(salt, pin));
        if (valid) { prefs.edit().putInt("pin_failures", 0).putLong("pin_blocked_until", 0).apply(); return; }
        int failures = prefs.getInt("pin_failures", 0) + 1;
        long delay = failures < 5 ? 0 : Math.min(300000L, 30000L * (failures - 4));
        prefs.edit().putInt("pin_failures", failures).putLong("pin_blocked_until", System.currentTimeMillis() + delay).apply();
        throw new Exception(failures >= 5 ? "PIN incorrect. Accès temporairement limité." : "PIN incorrect.");
    }

    private byte[] pinMac(byte[] salt, String pin) throws Exception {
        KeyStore store = KeyStore.getInstance("AndroidKeyStore"); store.load(null);
        SecretKey key = (SecretKey)store.getKey(PIN_KEY_ALIAS, null);
        if (key == null) {
            KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_HMAC_SHA256, "AndroidKeyStore");
            generator.init(new KeyGenParameterSpec.Builder(PIN_KEY_ALIAS, KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                .setDigests(KeyProperties.DIGEST_SHA256).build());
            key = generator.generateKey();
        }
        Mac mac = Mac.getInstance("HmacSHA256"); mac.init(key); mac.update(salt);
        return mac.doFinal(pin.getBytes(StandardCharsets.UTF_8));
    }

    @PluginMethod public void shareFile(PluginCall call) {
        String data = call.getString("data");
        String filename = call.getString("filename", "Biznako.json");
        String mime = call.getString("mime", "application/json");
        if (data == null) { call.reject("Fichier absent."); return; }
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > 12 * 1024 * 1024) { call.reject("Fichier trop volumineux."); return; }
        String safeName = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (safeName.isEmpty() || safeName.equals(".") || safeName.equals("..")) safeName = "Biznako.json";
        String safeMime = mime.equals("application/json") ? mime : "application/octet-stream";
        try {
            File dir = new File(getContext().getCacheDir(), "biznako-share");
            if (!dir.exists() && !dir.mkdirs()) throw new java.io.IOException("Dossier temporaire indisponible.");
            File[] stale = dir.listFiles();
            if (stale != null) {
                long cutoff = System.currentTimeMillis() - 24L * 60 * 60 * 1000;
                for (File old : stale) if (old.isFile() && old.lastModified() < cutoff) old.delete();
            }
            File file = new File(dir, safeName);
            try (FileOutputStream output = new FileOutputStream(file)) { output.write(bytes); }
            Uri uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", file);
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType(safeMime);
            send.putExtra(Intent.EXTRA_STREAM, uri);
            send.setClipData(ClipData.newUri(getContext().getContentResolver(), safeName, uri));
            send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent chooser = Intent.createChooser(send, "Partager la sauvegarde Biznako");
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            getActivity().runOnUiThread(() -> {
                try {
                    getActivity().startActivity(chooser);
                    JSObject result = new JSObject(); result.put("shared", true); call.resolve(result);
                } catch (Exception e) {
                    call.reject("Partage indisponible sur cet appareil.", e);
                }
            });
        } catch (Exception e) {
            call.reject("Partage indisponible sur cet appareil.", e);
        }
    }


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
