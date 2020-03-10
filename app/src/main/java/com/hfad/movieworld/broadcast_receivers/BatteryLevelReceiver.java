package com.hfad.movieworld.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.widget.Toast;

import com.hfad.movieworld.R;

public class BatteryLevelReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(Intent.ACTION_BATTERY_LOW)) {
                String warn = context.getString(R.string.low_battery_warn);
                SpannableStringBuilder biggerText = new SpannableStringBuilder(warn);
                biggerText.setSpan(new RelativeSizeSpan(1.35f), 0, warn.length(), 0);
                Toast.makeText(context, biggerText, Toast.LENGTH_LONG).show();
            }
        }
    }
}
