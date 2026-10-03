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
 * Category shell for the CustomGram settings architecture.
 * Keeps related features together while they are progressively wired to Telegram internals.
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
        actionBar.setTitle(title());
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override public void onItemClick(int id) { if (id == -1) finishFragment(); }
        });

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(14), dp(16), dp(30));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));

        if (category == APPEARANCE) {
            section(context, root, "Оформление");
            item(context, root, "Solar Icons", "Единый набор иконок во всём CustomGram");
            item(context, root, "Форма аватаров", "Системная, круглая или свой радиус");
            item(context, root, "Скругления интерфейса", "Карточки, формы и элементы управления");
            item(context, root, "Material Design 3", "Слайдеры, переключатели, загрузка, FAB и заголовки");
            item(context, root, "Шрифты и эмодзи", "Системные или встроенные Telegram");
            item(context, root, "Истории и плавающие кнопки", "Показывать или скрывать отдельные элементы");
            item(context, root, "Заголовок главного экрана", "По умолчанию CustomGram, можно изменить");
            item(context, root, "Темы отдельных чатов", "Сохранять выбранную тему для каждого чата");
        } else if (category == NAVIGATION) {
            section(context, root, "Главная навигация");
            item(context, root, "Нижняя панель", "Менять порядок вкладок и скрывать ненужные");
            item(context, root, "Меню настроек", "Менять порядок и скрывать разделы; CustomGram сверху по умолчанию");
            item(context, root, "Папки чатов", "Расположение сверху или снизу");
            item(context, root, "Архив", "Видимость, жесты и способ открытия");
            item(context, root, "Жесты", "Настраиваемые действия свайпов и переходов");
            section(context, root, "Карты");
            item(context, root, "Провайдер карт", "Telegram / Google / Яндекс");
            item(context, root, "Яндекс Карты внутри Telegram", "Выбор точки, поиск мест и открытие геопозиции");
        } else if (category == CHATS) {
            section(context, root, "Сообщения");
            item(context, root, "Размер стикеров", "Ползунок с живым предпросмотром");
            item(context, root, "Форма стикеров", "Обычная, скруглённая или в стиле сообщения");
            item(context, root, "Время на стикерах", "Показывать или скрывать");
            item(context, root, "Реакции", "Видимость и поведение реакций");
            item(context, root, "Меню сообщения", "Группировка и настройка действий");
            item(context, root, "Жесты сообщений", "Отдельно для входящих и исходящих");
            item(context, root, "Индикатор онлайна", "Показывать рядом с сообщением");
            item(context, root, "Метка «изменено»", "Текст или компактная иконка");
            item(context, root, "Мини-аватары отправителей", "Для групп и супергрупп");
            item(context, root, "Точные числа и время", "Без округления, при желании с секундами");
            item(context, root, "Фильтр Zalgo", "Очищать искажающие combining-символы");
        } else {
            section(context, root, "Перевод");
            item(context, root, "Перевод сообщений", "Кнопка перевода и перевод чата целиком");
            item(context, root, "Сервис перевода", "В том числе Яндекс и выбор целевого языка");
            section(context, root, "Расшифровка");
            item(context, root, "Deepgram API", "Собственный ключ хранится локально");
            item(context, root, "Голосовые сообщения", "Ручная или автоматическая расшифровка");
            item(context, root, "Кружки", "Локальный текст под кружком или отдельное сообщение");
            section(context, root, "Дополнительно");
            item(context, root, "Относительное время онлайна", "Например: был 12 минут назад");
            item(context, root, "Telegram ID", "Показывать ID пользователей, групп и каналов");
            item(context, root, "Вибрация", "Глобальное управление haptic feedback");
        }

        TextView note = new TextView(context);
        note.setText("Настройки в этом разделе собраны по смыслу. Функции подключаются поэтапно и не будут разбросаны по разным экранам.");
        note.setTextSize(13);
        note.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        note.setPadding(dp(8), dp(14), dp(8), 0);
        root.addView(note, LayoutHelper.createLinear(-1, -2));

        fragmentView = scroll;
        return fragmentView;
    }

    private String title() {
        if (category == APPEARANCE) return "Внешний вид";
        if (category == NAVIGATION) return "Навигация";
        if (category == CHATS) return "Чаты";
        return "Сервисы и инструменты";
    }

    private void section(Context c, LinearLayout root, String value) {
        TextView h = new TextView(c);
        h.setText(value);
        h.setTextSize(14);
        h.setTypeface(AndroidUtilities.bold());
        h.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        h.setPadding(dp(6), dp(12), dp(6), dp(8));
        root.addView(h, LayoutHelper.createLinear(-1, -2));
    }

    private void item(Context c, LinearLayout root, String title, String subtitle) {
        LinearLayout card = new LinearLayout(c);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(12), dp(16), dp(12));
        card.setBackground(Theme.createRoundRectDrawable(dp(16), Theme.getColor(Theme.key_windowBackgroundWhite)));

        TextView t = new TextView(c);
        t.setText(title);
        t.setTextSize(16);
        t.setTypeface(AndroidUtilities.bold());
        t.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        card.addView(t, LayoutHelper.createLinear(-1, -2));

        TextView s = new TextView(c);
        s.setText(subtitle);
        s.setTextSize(13);
        s.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        card.addView(s, LayoutHelper.createLinear(-1, -2, 0, 3, 0, 0));

        root.addView(card, LayoutHelper.createLinear(-1, -2, 0, 0, 0, 8));
    }

    private int dp(int v) { return AndroidUtilities.dp(v); }
}
