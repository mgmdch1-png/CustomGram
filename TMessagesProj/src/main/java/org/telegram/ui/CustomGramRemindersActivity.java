package org.telegram.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.CustomGramReminders;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

/** First CustomGram smart-reminders editor. Preview is intentionally live. */
public class CustomGramRemindersActivity extends BaseFragment {
    private EditText textField;
    private EditText minutesField;
    private TextView previewBody;
    private TextView previewWhen;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Умные напоминания");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override public void onItemClick(int id) { if (id == -1) finishFragment(); }
        });

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(32));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));

        addHeader(context, root, "Предпросмотр");
        LinearLayout preview = new LinearLayout(context);
        preview.setOrientation(LinearLayout.VERTICAL);
        preview.setPadding(AndroidUtilities.dp(18), AndroidUtilities.dp(16), AndroidUtilities.dp(18), AndroidUtilities.dp(14));
        preview.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(22), Theme.getColor(Theme.key_windowBackgroundWhite)));
        TextView app = text(context, "CustomGram", 15, true, Theme.key_windowBackgroundWhiteBlackText);
        preview.addView(app);
        previewBody = text(context, "Попей воду 💧", 17, true, Theme.key_windowBackgroundWhiteBlackText);
        preview.addView(previewBody, LayoutHelper.createLinear(-1, -2, 0, 7, 0, 0));
        previewWhen = text(context, "Через 30 минут", 13, false, Theme.key_windowBackgroundWhiteGrayText);
        preview.addView(previewWhen, LayoutHelper.createLinear(-1, -2, 0, 4, 0, 10));
        TextView actions = text(context, "Выполнено     Отложить", 14, true, Theme.key_windowBackgroundWhiteBlueText);
        preview.addView(actions);
        root.addView(preview, LayoutHelper.createLinear(-1, -2, 0, 0, 0, 18));

        addHeader(context, root, "Новое напоминание");
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(AndroidUtilities.dp(14), AndroidUtilities.dp(12), AndroidUtilities.dp(14), AndroidUtilities.dp(14));
        card.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(20), Theme.getColor(Theme.key_windowBackgroundWhite)));
        root.addView(card, LayoutHelper.createLinear(-1, -2));

        textField = field(context, "Что напомнить", "Попей воду 💧", false);
        card.addView(textField, LayoutHelper.createLinear(-1, 54));
        minutesField = field(context, "Через сколько минут", "30", true);
        card.addView(minutesField, LayoutHelper.createLinear(-1, 54, 0, 8, 0, 0));

        TextView repeatHint = text(context, "Повторы, дни недели, временные окна и режим «до выполнения» будут на этом же экране — без перехода в технические меню.", 13, false, Theme.key_windowBackgroundWhiteGrayText);
        repeatHint.setPadding(AndroidUtilities.dp(4), AndroidUtilities.dp(10), AndroidUtilities.dp(4), AndroidUtilities.dp(4));
        card.addView(repeatHint);

        TextView save = text(context, "Создать напоминание", 16, true, Theme.key_featuredStickers_buttonText);
        save.setGravity(Gravity.CENTER);
        save.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(15), Theme.getColor(Theme.key_featuredStickers_addButton)));
        save.setOnClickListener(v -> saveReminder());
        root.addView(save, LayoutHelper.createLinear(-1, 52, 0, 16, 0, 0));

        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) { }
            public void onTextChanged(CharSequence s, int st, int b, int c) { updatePreview(); }
            public void afterTextChanged(Editable e) { }
        };
        textField.addTextChangedListener(watcher);
        minutesField.addTextChangedListener(watcher);
        updatePreview();
        fragmentView = scroll;
        return fragmentView;
    }

    private void saveReminder() {
        String body = textField.getText().toString().trim();
        if (body.isEmpty()) return;
        int minutes = parseMinutes();
        long at = System.currentTimeMillis() + minutes * 60_000L;
        CustomGramReminders.save(new CustomGramReminders.Item(System.currentTimeMillis(), body, at, 0, true));
        previewWhen.setText("Сохранено • через " + minutesLabel(minutes));
    }

    private void updatePreview() {
        if (previewBody == null || textField == null) return;
        String body = textField.getText().toString().trim();
        previewBody.setText(body.isEmpty() ? "Текст напоминания" : body);
        int minutes = parseMinutes();
        previewWhen.setText("Через " + minutesLabel(minutes));
    }

    private int parseMinutes() {
        try { return Math.max(1, Integer.parseInt(minutesField.getText().toString().trim())); }
        catch (Exception e) { return 1; }
    }

    private String minutesLabel(int m) {
        if (m % 60 == 0) { int h = m / 60; return h + (h == 1 ? " час" : " ч"); }
        return m + " мин";
    }

    private EditText field(Context c, String hint, String value, boolean number) {
        EditText e = new EditText(c);
        e.setHint(hint); e.setText(value); e.setTextSize(16);
        e.setSingleLine(true); e.setPadding(AndroidUtilities.dp(12), 0, AndroidUtilities.dp(12), 0);
        e.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        e.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        e.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(13), Theme.getColor(Theme.key_windowBackgroundGray)));
        e.setInputType(number ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        return e;
    }

    private void addHeader(Context c, LinearLayout root, String value) {
        TextView h = text(c, value, 14, true, Theme.key_windowBackgroundWhiteBlueHeader);
        h.setPadding(AndroidUtilities.dp(6), 0, 0, AndroidUtilities.dp(8));
        root.addView(h, LayoutHelper.createLinear(-1, -2));
    }

    private TextView text(Context c, String value, int size, boolean bold, int colorKey) {
        TextView t = new TextView(c); t.setText(value); t.setTextSize(size);
        if (bold) t.setTypeface(AndroidUtilities.bold());
        t.setTextColor(Theme.getColor(colorKey)); return t;
    }
}
