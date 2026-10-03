package org.telegram.ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.CustomGramConfig;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

/**
 * Functional category screen. No placeholder controls are allowed here.
 */
public class CustomGramCategoryActivity extends BaseFragment {
    public static final int APPEARANCE = 1;
    public static final int NAVIGATION = 2;
    public static final int CHATS = 3;
    public static final int SERVICES = 4;

    private final int category;

    public CustomGramCategoryActivity(int category) {
        this.category = category;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(category == APPEARANCE ? "Внешний вид" : "CustomGram");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override public void onItemClick(int id) { if (id == -1) finishFragment(); }
        });

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, dp(8), 0, dp(28));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));

        if (category == APPEARANCE) {
            buildAppearance(context, root);
        } else {
            TextView unavailable = new TextView(context);
            unavailable.setText("Этот раздел временно скрыт из основного меню: он появится только после подключения реальной логики.");
            unavailable.setTextSize(15);
            unavailable.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            unavailable.setPadding(dp(20), dp(24), dp(20), dp(24));
            root.addView(unavailable, LayoutHelper.createLinear(-1, -2));
        }

        fragmentView = scroll;
        return fragmentView;
    }

    private void buildAppearance(Context context, LinearLayout root) {
        section(context, root, "Шапка чата");
        toggle(context, root,
                "Чистая шапка",
                "Убирает круглые/капсульные фоны у кнопки назад и кнопок Action Bar по всему клиенту.",
                CustomGramConfig.cleanActionBar(),
                CustomGramConfig::setCleanActionBar);
        toggle(context, root,
                "Скрыть кнопку звонка",
                "Убирает отдельную кнопку звонка из верхней панели личного чата. Сам звонок остаётся доступен из меню профиля.",
                CustomGramConfig.hideChatCallButton(),
                CustomGramConfig::setHideChatCallButton);

        section(context, root, "Главный экран");
        TextView titleLabel = new TextView(context);
        titleLabel.setText("Заголовок главного экрана");
        titleLabel.setTextSize(16);
        titleLabel.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        titleLabel.setPadding(dp(20), dp(12), dp(20), dp(6));
        root.addView(titleLabel, LayoutHelper.createLinear(-1, -2));

        EditText title = new EditText(context);
        title.setSingleLine(true);
        title.setText(CustomGramConfig.getMainTitle());
        title.setHint("CustomGram");
        title.setTextSize(16);
        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        title.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        title.setPadding(dp(14), 0, dp(14), 0);
        title.setBackground(Theme.createRoundRectDrawable(dp(12), Theme.getColor(Theme.key_windowBackgroundGray)));
        root.addView(title, LayoutHelper.createLinear(-1, 50, 20, 0, 20, 0));
        title.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                CustomGramConfig.setMainTitle(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        TextView note = new TextView(context);
        note.setText("Изменения шапки и заголовка применяются при следующем открытии соответствующего экрана.");
        note.setTextSize(13);
        note.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        note.setPadding(dp(20), dp(12), dp(20), dp(12));
        root.addView(note, LayoutHelper.createLinear(-1, -2));
    }

    private interface BooleanSetter { void set(boolean value); }

    private void toggle(Context context, LinearLayout root, String title, String subtitle, boolean checked, BooleanSetter setter) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(20), dp(10), dp(14), dp(10));
        row.setMinimumHeight(dp(72));
        row.setBackground(Theme.getSelectorDrawable(false));

        LinearLayout texts = new LinearLayout(context);
        texts.setOrientation(LinearLayout.VERTICAL);
        TextView t = new TextView(context);
        t.setText(title);
        t.setTextSize(16);
        t.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        texts.addView(t, LayoutHelper.createLinear(-1, -2));
        TextView s = new TextView(context);
        s.setText(subtitle);
        s.setTextSize(13);
        s.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        texts.addView(s, LayoutHelper.createLinear(-1, -2, 0, 3, 0, 0));
        row.addView(texts, new LinearLayout.LayoutParams(0, -2, 1f));

        Switch toggle = new Switch(context);
        toggle.setChecked(checked);
        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> setter.set(isChecked));
        row.addView(toggle, LayoutHelper.createLinear(-2, -2, Gravity.CENTER_VERTICAL));
        row.setOnClickListener(v -> toggle.setChecked(!toggle.isChecked()));

        root.addView(row, LayoutHelper.createLinear(-1, -2));
    }

    private void section(Context c, LinearLayout root, String value) {
        TextView h = new TextView(c);
        h.setText(value);
        h.setTextSize(14);
        h.setTypeface(AndroidUtilities.bold());
        h.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        h.setPadding(dp(20), dp(16), dp(20), dp(8));
        root.addView(h, LayoutHelper.createLinear(-1, -2));
    }

    private int dp(int v) { return AndroidUtilities.dp(v); }
}
