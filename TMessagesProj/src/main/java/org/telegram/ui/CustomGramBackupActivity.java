package org.telegram.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.CustomGramConfig;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

/** Functional backup/import for CustomGram-owned settings. */
public class CustomGramBackupActivity extends BaseFragment {
    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Бэкап CustomGram");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override public void onItemClick(int id) { if (id == -1) finishFragment(); }
        });

        ScrollView scroll = new ScrollView(context);
        scroll.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));

        section(context, root, "Резервная копия");
        row(context, root, "Скопировать бэкап", "Копирует настройки CustomGram в JSON. Аккаунты и сессии Telegram не экспортируются.", v -> copyBackup(context));
        row(context, root, "Импортировать из буфера", "Вставляет ранее скопированный JSON и применяет настройки.", v -> importFromClipboard(context));

        TextView note = new TextView(context);
        note.setText("После импорта некоторые визуальные изменения применятся при следующем открытии соответствующего экрана.");
        note.setTextSize(13);
        note.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        note.setPadding(dp(20), dp(14), dp(20), dp(20));
        root.addView(note, LayoutHelper.createLinear(-1, -2));

        fragmentView = scroll;
        return fragmentView;
    }

    private void copyBackup(Context context) {
        String json = CustomGramConfig.exportJson();
        if (json.isEmpty()) {
            Toast.makeText(context, "Не удалось создать бэкап", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("CustomGram backup", json));
        Toast.makeText(context, "Бэкап скопирован", Toast.LENGTH_SHORT).show();
    }

    private void importFromClipboard(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (!clipboard.hasPrimaryClip() || clipboard.getPrimaryClip() == null || clipboard.getPrimaryClip().getItemCount() == 0) {
            Toast.makeText(context, "В буфере нет бэкапа", Toast.LENGTH_SHORT).show();
            return;
        }
        CharSequence text = clipboard.getPrimaryClip().getItemAt(0).coerceToText(context);
        if (text == null || text.length() == 0) {
            Toast.makeText(context, "В буфере нет бэкапа", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(context, getResourceProvider())
                .setTitle("Импортировать настройки?")
                .setMessage("Текущие значения CustomGram будут заменены значениями из бэкапа.")
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Импортировать", (dialog, which) -> {
                    boolean ok = CustomGramConfig.importJson(text.toString());
                    Toast.makeText(context, ok ? "Настройки импортированы" : "Неверный бэкап CustomGram", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void section(Context c, LinearLayout root, String title) {
        TextView h = new TextView(c);
        h.setText(title);
        h.setTextSize(14);
        h.setTypeface(AndroidUtilities.bold());
        h.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        h.setPadding(dp(20), dp(18), dp(20), dp(8));
        root.addView(h, LayoutHelper.createLinear(-1, -2));
    }

    private void row(Context c, LinearLayout root, String title, String subtitle, View.OnClickListener click) {
        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(20), dp(12), dp(20), dp(12));
        row.setMinimumHeight(dp(68));
        row.setOnClickListener(click);
        row.setBackground(Theme.getSelectorDrawable(false));

        TextView t = new TextView(c);
        t.setText(title);
        t.setTextSize(16);
        t.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        row.addView(t, LayoutHelper.createLinear(-1, -2));

        TextView s = new TextView(c);
        s.setText(subtitle);
        s.setTextSize(13);
        s.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        row.addView(s, LayoutHelper.createLinear(-1, -2, 0, 3, 0, 0));
        root.addView(row, LayoutHelper.createLinear(-1, -2));
    }

    private int dp(int value) { return AndroidUtilities.dp(value); }
}
