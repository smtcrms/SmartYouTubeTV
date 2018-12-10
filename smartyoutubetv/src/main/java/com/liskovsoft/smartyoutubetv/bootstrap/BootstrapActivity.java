package com.liskovsoft.smartyoutubetv.bootstrap;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.crashlytics.android.Crashlytics;
import com.liskovsoft.smartyoutubetv.BuildConfig;
import com.liskovsoft.smartyoutubetv.R;
import com.liskovsoft.smartyoutubetv.common.helpers.Helpers;
import com.liskovsoft.smartyoutubetv.common.helpers.LangUpdater;
import com.liskovsoft.smartyoutubetv.common.prefs.SmartPreferences;
import com.liskovsoft.smartyoutubetv.dialogs.GenericSelectorDialog;
import com.liskovsoft.smartyoutubetv.flavors.exoplayer.SmartYouTubeTV4K;
import com.liskovsoft.smartyoutubetv.flavors.exoplayer.SmartYouTubeTV4KAlt;
import com.liskovsoft.smartyoutubetv.flavors.exoplayer.player.dialogs.RestrictCodecDataSource;
import com.liskovsoft.smartyoutubetv.flavors.webview.SmartYouTubeTV1080Activity;
import com.liskovsoft.smartyoutubetv.flavors.xwalk.SmartYouTubeTV1080AltActivity;
import com.liskovsoft.smartyoutubetv.widgets.BootstrapCheckButton;
import io.fabric.sdk.android.Fabric;

public class BootstrapActivity extends ActivityBase {
    public static final String FROM_BOOTSTRAP = "FROM_BOOTSTRAP";
    public static final String SKIP_RESTORE = "skip_restore";
    private SmartPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // do it before view instantiation
        initPrefs();
        setupLang();
        tryToRestoreLastActivity();

        super.onCreate(savedInstanceState);
        setupFonSize();
        setContentView(R.layout.activity_bootstrap);
        initButtons();
        initVersion();

        setupCrashLogs();
    }

    private void initVersion() {
        TextView title = findViewById(R.id.bootstrap_title_message);
        CharSequence oldTitle = title.getText();
        String finalTitle = String.format("%s (%s)", oldTitle, BuildConfig.VERSION_NAME);
        title.setText(finalTitle);
    }

    private void initPrefs() {
        if (mPrefs == null) {
            mPrefs = SmartPreferences.instance(this);
        }
    }

    private void initCheckbox(int id, boolean isChecked) {
        BootstrapCheckButton chkbox = findViewById(id);
        chkbox.setChecked(isChecked);
    }

    public void onClick(View button) {
        switch (button.getId()) {
            case R.id.btn_select_lang:
                GenericSelectorDialog.create(this, new LanguageDataSource(this));
                break;
            case R.id.btn_send_crash_report:
                Toast.makeText(this, "Dummy crash report message", Toast.LENGTH_LONG).show();
                break;
            case R.id.btn_preferred_codec:
                GenericSelectorDialog.create(this, new RestrictCodecDataSource(this));
                break;
        }
    }

    private void initButtons() {
        initCheckbox(R.id.chk_save_selection, mPrefs.getBootstrapSaveSelection());
        initCheckbox(R.id.chk_autoframerate, mPrefs.getBootstrapAutoframerate());
        initCheckbox(R.id.chk_old_ui, mPrefs.getBootstrapOldUI());
        initCheckbox(R.id.chk_update_check, mPrefs.getBootstrapUpdateCheck());
        initCheckbox(R.id.chk_endcards, mPrefs.getEnableEndCards());
    }

    public void onCheckedChanged(BootstrapCheckButton checkBox, boolean b) {
        switch (checkBox.getId()) {
            case R.id.chk_save_selection:
                mPrefs.setBootstrapSaveSelection(b);
                break;
            case R.id.chk_autoframerate:
                mPrefs.setBootstrapAutoframerate(b);
                break;
            case R.id.chk_update_check:
                mPrefs.setBootstrapUpdateCheck(b);
                break;
            case R.id.chk_old_ui:
                mPrefs.setBootstrapOldUI(b);
                break;
            case R.id.chk_endcards:
                mPrefs.setEndCards(b);
                break;
        }
    }

    private void tryToRestoreLastActivity() {
        boolean skipRestore = getIntent().getBooleanExtra(SKIP_RESTORE, false);
        if (skipRestore) {
            return;
        }

        String bootstrapActivityName = mPrefs.getBootstrapActivityName();
        boolean isChecked = mPrefs.getBootstrapSaveSelection();
        boolean activityHasName = bootstrapActivityName != null;
        if (isChecked && activityHasName) {
            startActivity(this, bootstrapActivityName);
        }
    }

    /**
     * Detect {@link Crashlytics} from the property file. See <em>build.gradle</em>. <a href="https://docs.fabric.io/android/crashlytics/build-tools.html">More info</a>
     */
    private void setupCrashLogs() {
        if (BuildConfig.CRASHLYTICS_ENABLED) {
            Fabric.with(this, new Crashlytics());
        }
    }

    private void setupLang() {
        new LangUpdater(this).update();
    }

    public void selectFlavour(View view) {
        Class clazz = SmartYouTubeTV1080Activity.class;
        switch (view.getId()) {
            case R.id.button_webview:
                clazz = SmartYouTubeTV1080Activity.class;
                break;
            case R.id.button_xwalk:
                clazz = SmartYouTubeTV1080AltActivity.class;
                break;
            case R.id.button_exo:
                clazz = SmartYouTubeTV4K.class;
                break;
            case R.id.button_exo2:
                clazz = SmartYouTubeTV4KAlt.class;
                break;
        }

        mPrefs.setBootstrapActivityName(clazz.getCanonicalName());
        startActivity(this, clazz);
    }

    private void startActivity(Context ctx, String clazz) {
        Intent intent = getIntent(); // modify original intent
        // value used in StateUpdater class
        intent.putExtra(FROM_BOOTSTRAP, true);
        // NOTE: make activity transparent (non-reachable from launcher or resents)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setClassName(ctx, clazz);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) { // activity's name changed (choose again)
            e.printStackTrace();
        }
    }

    private void startActivity(Context ctx, Class clazz) {
        Intent intent = getIntent(); // modify original intent
        // NOTE: make activity transparent (non-reachable from launcher or from resent list)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setClass(ctx, clazz);
        
        startActivity(intent);
    }

    private void setupFonSize() {
        Helpers.adjustFontScale(getResources().getConfiguration(), this);
    }
}
