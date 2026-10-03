package org.telegram.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

/** Main home for CustomGram-only features. */
public class CustomGramSettingsActivity extends BaseFragment {

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Настройки CustomGram");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override public void onItemClick(int id) { if (id == -1) finishFragment(); }
        });

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(22), AndroidUtilities.dp(16), AndroidUtilities.dp(32));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));

        TextView logo = new TextView(context);
        logo.setText("C");
        logo.setGravity(Gravity.CENTER);
        logo.setTextSize(38);
        logo.setTypeface(Typeface.DEFAULT_BOLD);
        logo.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        logo.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(36), Theme.getColor(Theme.key_featuredStickers_addButton)));
        root.addView(logo, LayoutHelper.createLinear(72, 72, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 12));

        TextView title = new TextView(context);
        title.setText("CustomGram");
        title.setGravity(Gravity.CENTER);
        title.setTextSize(30);
        title.setTypeface(AndroidUtilities.bold());
        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        root.addView(title, LayoutHelper.createLinear(-1, -2));

        TextView subtitle = new TextView(context);
        subtitle.setText("Свои функции и внешний вид клиента");
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setTextSize(14);
        subtitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        root.addView(subtitle, LayoutHelper.createLinear(-1, -2, 0, 2, 0, 22));

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(AndroidUtilities.dp(4), AndroidUtilities.dp(6), AndroidUtilities.dp(4), AndroidUtilities.dp(6));
        card.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(20), Theme.getColor(Theme.key_windowBackgroundWhite)));
        root.addView(card, LayoutHelper.createLinear(-1, -2));

        addRow(context, card, "◉", "Внешний вид", "Оформление, анимации и интерфейс", null);
        addRow(context, card, "☰", "Навигация", "Вкладки, панели и жесты", null);
        addRow(context, card, "☵", "Чаты", "Меню сообщений, реакции и цитаты", null);
        addRow(context, card, "✎", "Локальные изменения", "Имя, username, bio и статус", v -> presentFragment(new CustomGramLocalChangesListActivity()));
        addRow(context, card, "⏱", "Умные напоминания", "Расписание, повторы и действия", v -> presentFragment(new CustomGramRemindersActivity()));

        TextView hint = new TextView(context);
        hint.setText("CustomGram развивается как отдельный слой возможностей поверх Telegram. Визуальные настройки получают интерактивный предпросмотр.");
        hint.setTextSize(13);
        hint.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        hint.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(14), AndroidUtilities.dp(12), 0);
        root.addView(hint, LayoutHelper.createLinear(-1, -2));

        fragmentView = scroll;
        return fragmentView;
    }

    private void addRow(Context context, LinearLayout parent, String icon, String title, String subtitle, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(AndroidUtilities.dp(14), AndroidUtilities.dp(10), AndroidUtilities.dp(14), AndroidUtilities.dp(10));
        row.setMinimumHeight(AndroidUtilities.dp(74));
        if (listener != null) row.setOnClickListener(listener);

        TextView badge = new TextView(context);
        badge.setText(icon);
        badge.setTextSize(22);
        badge.setGravity(Gravity.CENTER);
        badge.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        badge.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(14), Theme.getColor(Theme.key_featuredStickers_addButton)));
        row.addView(badge, LayoutHelper.createLinear(46, 46, Gravity.CENTER_VERTICAL, 0, 0, 14, 0));

        LinearLayout texts = new LinearLayout(context);
        texts.setOrientation(LinearLayout.VERTICAL);
        TextView t = new TextView(context);
        t.setText(title);
        t.setTextSize(17);
        t.setTypeface(AndroidUtilities.bold());
        t.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        texts.addView(t, LayoutHelper.createLinear(-1, -2));
        TextView s = new TextView(context);
        s.setText(subtitle);
        s.setTextSize(14);
        s.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        texts.addView(s, LayoutHelper.createLinear(-1, -2, 0, 2, 0, 0));
        row.addView(texts, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView arrow = new TextView(context);
        arrow.setText(listener == null ? "скоро" : "›");
        arrow.setTextSize(listener == null ? 12 : 28);
        arrow.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        row.addView(arrow, LayoutHelper.createLinear(-2, -2));
        parent.addView(row, LayoutHelper.createLinear(-1, -2));
    }
}
