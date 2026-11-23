package com.teo.quietfeed;

import android.accessibilityservice.AccessibilityService;
import android.media.AudioManager;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Every time the Instagram/TikTok feed scrolls (= a new video comes into view),
 * the media stream is muted. Pressing volume-up unmutes it for the video you're on.
 * The next scroll mutes again, so sound never "carries over" down the feed.
 * When you leave the app, the sound is restored for everything else.
 */
public class QuietService extends AccessibilityService {

    static final Set<String> TARGETS = new HashSet<>(Arrays.asList(
            "com.instagram.android",
            "com.zhiliaoapp.musically",   // TikTok
            "com.ss.android.ugc.trill"    // TikTok (some regions)
    ));

    // Windows that pop up on top of the app without meaning "you left it".
    static final Set<String> IGNORE = new HashSet<>(Arrays.asList(
            "com.android.systemui",
            "android",
            "com.samsung.android.honeyboard",
            "com.google.android.inputmethod.latin",
            "com.teo.quietfeed"
    ));

    private AudioManager audio;
    private boolean inTarget = false;
    private boolean weMuted = false;

    @Override
    protected void onServiceConnected() {
        audio = (AudioManager) getSystemService(AUDIO_SERVICE);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) {
        if (e == null || e.getPackageName() == null || audio == null) return;
        String pkg = e.getPackageName().toString();
        int type = e.getEventType();

        if (TARGETS.contains(pkg)) {
            if (type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && !inTarget) {
                inTarget = true;
                mute();
                Toast.makeText(this, "🔇 Quiet Feed: volume-up to hear just this one",
                        Toast.LENGTH_SHORT).show();
            } else if (type == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
                inTarget = true;
                mute();
            }
            return;
        }

        if (type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && inTarget
                && !IGNORE.contains(pkg) && !isKeyboard(pkg)) {
            inTarget = false;
            restore();
        }
    }

    private void mute() {
        if (!audio.isStreamMute(AudioManager.STREAM_MUSIC)) {
            audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
        }
        weMuted = true;
    }

    private void restore() {
        if (weMuted && audio.isStreamMute(AudioManager.STREAM_MUSIC)) {
            audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
        }
        weMuted = false;
    }

    private boolean isKeyboard(String pkg) {
        String ime = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD);
        return ime != null && ime.startsWith(pkg + "/");
    }

    @Override
    public void onInterrupt() { }

    @Override
    public boolean onUnbind(android.content.Intent intent) {
        if (audio != null) restore();
        return super.onUnbind(intent);
    }
}
