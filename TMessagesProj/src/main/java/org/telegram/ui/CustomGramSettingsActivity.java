package org.telegram.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

/**
 * CustomGram settings home. Important rule: no showcase-only rows.
 * Every visible destination contains working behavior.
 */
public class CustomGramSettingsActivity extends BaseFragment {

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("CustomGram");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override public void onItemClick(int id) { if (id == -1) finishFragment(); }
        });

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, AndroidUtilities.dp(8), 0, AndroidUtilities.dp(32));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));

        section(context, root, "Интерфейс");
        row(context, root, "Внешний вид", "Чистая шапка, звонок и заголовок главного экрана", v ->
                presentFragment(new CustomGramCategoryActivity(CustomGramCategoryActivity.APPEARANCE)));

        section(context, root, "Функции");
        row(context, root, "Локальные изменения", "Локальная подмена имени, username, bio и статуса", v ->
                presentFragment(new CustomGramLocalChangesListActivity()));
        row(context, root, "Умные напоминания", "Создание, повторы, действия и активные напоминания", v ->
                presentFragment(new CustomGramRemindersActivity()));

        section(context, root, "Данные");
        row(context, root, "Бэкап CustomGram", "Экспорт и импорт только настроек CustomGram", v ->
                presentFragment(new CustomGramBackupActivity()));

        TextView note = new TextView(context);
        note.setText("В CustomGram больше не показываются пустые пункты. Новая функция появляется в этом меню только после подключения реальной логики.");
        note.setTextSize(13);
        note.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        note.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(18), AndroidUtilities.dp(20), 0);
        root.addView(note, LayoutHelper.createLinear(-1, -2));

        fragmentView = scroll;
        return fragmentView;
    }

    private void section(Context context, LinearLayout root, String text) {
        TextView header = new TextView(context);
        header.setText(text);
        header.setTextSize(14);
        header.setTypeface(AndroidUtilities.bold());
        header.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        header.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(16), AndroidUtilities.dp(20), AndroidUtilities.dp(8));
        root.addView(header, LayoutHelper.createLinear(-1, -2));
    }

    private void row(Context context, LinearLayout root, String title, String subtitle, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(11), AndroidUtilities.dp(14), AndroidUtilities.dp(11));
        row.setMinimumHeight(AndroidUtilities.dp(66));
        row.setBackground(Theme.getSelectorDrawable(false));
        row.setOnClickListener(listener);

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

        TextView arrow = new TextView(context);
        arrow.setText("›");
        arrow.setTextSize(25);
        arrow.setGravity(Gravity.CENTER);
        arrow.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        row.addView(arrow, LayoutHelper.createLinear(28, 44));

        root.addView(row, LayoutHelper.createLinear(-1, -2));
    }
}
