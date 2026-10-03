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
        root.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(18), AndroidUtilities.dp(16), AndroidUtilities.dp(32));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));

        TextView title = new TextView(context);
        title.setText("CustomGram");
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        title.setTextSize(28);
        title.setTypeface(AndroidUtilities.bold());
        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        root.addView(title, LayoutHelper.createLinear(-1, -2));

        TextView subtitle = new TextView(context);
        subtitle.setText("Настройте клиент под себя");
        subtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        subtitle.setTextSize(14);
        subtitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        root.addView(subtitle, LayoutHelper.createLinear(-1, -2, 0, 2, 0, 18));

        header(context, root, "Интерфейс");
        LinearLayout uiCard = card(context);
        addRow(context, uiCard, "Внешний вид", "Иконки, аватары, MD3, шрифты, эмодзи и темы", v -> presentFragment(new CustomGramCategoryActivity(CustomGramCategoryActivity.APPEARANCE)));
        addRow(context, uiCard, "Навигация", "Вкладки, папки, меню настроек, архив, жесты и карты", v -> presentFragment(new CustomGramCategoryActivity(CustomGramCategoryActivity.NAVIGATION)));
        addRow(context, uiCard, "Чаты", "Сообщения, стикеры, реакции, жесты и меню", v -> presentFragment(new CustomGramCategoryActivity(CustomGramCategoryActivity.CHATS)));
        root.addView(uiCard, LayoutHelper.createLinear(-1, -2));

        header(context, root, "Функции");
        LinearLayout featuresCard = card(context);
        addRow(context, featuresCard, "Локальные изменения", "Имя, username, bio и статус", v -> presentFragment(new CustomGramLocalChangesListActivity()));
        addRow(context, featuresCard, "Умные напоминания", "Расписание, повторы и действия", v -> presentFragment(new CustomGramRemindersActivity()));
        addRow(context, featuresCard, "Сервисы и инструменты", "Перевод, Deepgram, расшифровка, ID и другое", v -> presentFragment(new CustomGramCategoryActivity(CustomGramCategoryActivity.SERVICES)));
        root.addView(featuresCard, LayoutHelper.createLinear(-1, -2));

        TextView hint = new TextView(context);
        hint.setText("Связанные настройки собраны в одном месте. В следующих сборках каждая функция будет подключаться внутри своего раздела, без дублирования и разброса по меню.");
        hint.setTextSize(13);
        hint.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        hint.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(14), AndroidUtilities.dp(8), 0);
        root.addView(hint, LayoutHelper.createLinear(-1, -2));

        fragmentView = scroll;
        return fragmentView;
    }

    private void header(Context context, LinearLayout root, String text) {
        TextView header = new TextView(context);
        header.setText(text);
        header.setTextSize(14);
        header.setTypeface(AndroidUtilities.bold());
        header.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        header.setPadding(AndroidUtilities.dp(6), AndroidUtilities.dp(10), 0, AndroidUtilities.dp(8));
        root.addView(header, LayoutHelper.createLinear(-1, -2));
    }

    private LinearLayout card(Context context) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(AndroidUtilities.dp(4), AndroidUtilities.dp(4), AndroidUtilities.dp(4), AndroidUtilities.dp(4));
        card.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(18), Theme.getColor(Theme.key_windowBackgroundWhite)));
        return card;
    }

    private void addRow(Context context, LinearLayout parent, String title, String subtitle, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(11), AndroidUtilities.dp(14), AndroidUtilities.dp(11));
        row.setMinimumHeight(AndroidUtilities.dp(66));
        row.setOnClickListener(listener);

        LinearLayout texts = new LinearLayout(context);
        texts.setOrientation(LinearLayout.VERTICAL);
        TextView t = new TextView(context);
        t.setText(title);
        t.setTextSize(16);
        t.setTypeface(AndroidUtilities.bold());
        t.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        texts.addView(t, LayoutHelper.createLinear(-1, -2));
        TextView s = new TextView(context);
        s.setText(subtitle);
        s.setTextSize(13);
        s.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        texts.addView(s, LayoutHelper.createLinear(-1, -2, 0, 2, 0, 0));
        row.addView(texts, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView arrow = new TextView(context);
        arrow.setText("›");
        arrow.setTextSize(26);
        arrow.setGravity(Gravity.CENTER);
        arrow.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        row.addView(arrow, LayoutHelper.createLinear(24, 40));
        parent.addView(row, LayoutHelper.createLinear(-1, -2));
    }
}
