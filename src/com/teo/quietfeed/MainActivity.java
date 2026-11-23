package com.teo.quietfeed;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView status;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        int p = (int) (24 * getResources().getDisplayMetrics().density);
        root.setPadding(p, p * 2, p, p);
        root.setBackgroundColor(Color.rgb(20, 20, 24));

        TextView title = new TextView(this);
        title.setText("Quiet Feed");
        title.setTextSize(28);
        title.setTextColor(Color.WHITE);
        root.addView(title);

        TextView how = new TextView(this);
        how.setText("\nEvery scroll in Instagram / TikTok mutes the sound.\n\n"
                + "Want to hear one video? Press VOLUME UP while it's on screen. "
                + "The next scroll mutes again.\n\n"
                + "Leaving the app gives your sound back.\n");
        how.setTextSize(16);
        how.setTextColor(Color.rgb(200, 200, 205));
        root.addView(how);

        status = new TextView(this);
        status.setTextSize(18);
        root.addView(status);

        Button open = new Button(this);
        open.setText("Open Accessibility settings");
        open.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        root.addView(open);

        TextView hint = new TextView(this);
        hint.setText("\nSamsung: Settings → Accessibility → Installed apps → Quiet Feed → On.\n\n"
                + "If it's greyed out: Settings → Apps → Quiet Feed → ⋮ (top right) → "
                + "Allow restricted settings, then try again.");
        hint.setTextSize(14);
        hint.setTextColor(Color.rgb(150, 150, 160));
        root.addView(hint);

        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String enabled = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        boolean on = enabled != null && enabled.contains(getPackageName() + "/");
        status.setText(on ? "● Active" : "○ Not enabled yet");
        status.setTextColor(on ? Color.rgb(90, 220, 120) : Color.rgb(255, 120, 120));
    }
}
