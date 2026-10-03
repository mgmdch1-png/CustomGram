package org.telegram.ui;

import android.content.Context;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.CustomGramLocalChanges;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class CustomGramLocalChangesActivity extends BaseFragment {

    private final long userId;
    private TLRPC.User user;

    private EditText firstNameField;
    private EditText lastNameField;
    private EditText usernameField;
    private EditText aboutField;
    private EditText statusTextField;
    private EditText statusTemplateField;

    private TextView statusOriginal;
    private TextView statusStatic;
    private TextView statusDynamic;
    private TextView previewText;

    private int statusMode;

    public CustomGramLocalChangesActivity(long userId) {
        this.userId = userId;
    }

    @Override
    public View createView(Context context) {
        user = MessagesController.getInstance(currentAccount).getUser(userId);

        if (user == null || CustomGramLocalChanges.isOwnUser(currentAccount, user)) {
            finishFragment();
            return new View(context);
        }

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Локальные изменения");

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        ScrollView scrollView = new ScrollView(context);

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(
                AndroidUtilities.dp(16),
                AndroidUtilities.dp(12),
                AndroidUtilities.dp(16),
                AndroidUtilities.dp(24)
        );

        scrollView.addView(
                container,
                new ScrollView.LayoutParams(
                        ScrollView.LayoutParams.MATCH_PARENT,
                        ScrollView.LayoutParams.WRAP_CONTENT
                )
        );

        addSectionTitle(context, container, "Профиль");

        firstNameField = addField(
                context,
                container,
                "Имя",
                CustomGramLocalChanges.getFirstName(userId, user.first_name)
        );

        lastNameField = addField(
                context,
                container,
                "Фамилия",
                CustomGramLocalChanges.getLastName(userId, user.last_name)
        );

        usernameField = addField(
                context,
                container,
                "Имя пользователя",
                CustomGramLocalChanges.getUsername(userId, user.username)
        );

        aboutField = addField(
                context,
                container,
                "Описание профиля",
                CustomGramLocalChanges.getAbout(userId, "")
        );

        addSectionTitle(context, container, "Статус");

        LinearLayout selector = new LinearLayout(context);
        selector.setOrientation(LinearLayout.HORIZONTAL);
        selector.setGravity(Gravity.CENTER_VERTICAL);

        statusOriginal = createStatusButton(context, "Оригинальный");
        statusStatic = createStatusButton(context, "Свой текст");
        statusDynamic = createStatusButton(context, "Текст + время");

        selector.addView(statusOriginal, new LinearLayout.LayoutParams(0, AndroidUtilities.dp(44), 1f));
        selector.addView(statusStatic, new LinearLayout.LayoutParams(0, AndroidUtilities.dp(44), 1f));
        selector.addView(statusDynamic, new LinearLayout.LayoutParams(0, AndroidUtilities.dp(44), 1f));

        container.addView(
                selector,
                LayoutHelper.createLinear(
                        LayoutHelper.MATCH_PARENT,
                        LayoutHelper.WRAP_CONTENT,
                        0,
                        0,
                        0,
                        12
                )
        );

        statusTextField = addField(
                context,
                container,
                "Свой статус",
                CustomGramLocalChanges.getStatusText(userId)
        );

        statusTemplateField = addField(
                context,
                container,
                "Шаблон статуса",
                CustomGramLocalChanges.getStatusTemplate(userId)
        );

        TextView help = new TextView(context);
        help.setText(
                "Переменные:\n" +
                "{time} — реальное время Telegram\n" +
                "{date} — реальная дата Telegram\n\n" +
                "Пример:\n" +
                "в моём сердце в {time}"
        );
        help.setTextSize(14);
        help.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));

        container.addView(
                help,
                LayoutHelper.createLinear(
                        LayoutHelper.MATCH_PARENT,
                        LayoutHelper.WRAP_CONTENT,
                        0,
                        0,
                        0,
                        16
                )
        );

        addSectionTitle(context, container, "Предпросмотр");

        previewText = new TextView(context);
        previewText.setTextSize(16);
        previewText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        previewText.setPadding(
                AndroidUtilities.dp(14),
                AndroidUtilities.dp(14),
                AndroidUtilities.dp(14),
                AndroidUtilities.dp(14)
        );
        previewText.setBackground(
                Theme.createRoundRectDrawable(
                        AndroidUtilities.dp(14),
                        Theme.getColor(Theme.key_windowBackgroundGray)
                )
        );

        container.addView(
                previewText,
                LayoutHelper.createLinear(
                        LayoutHelper.MATCH_PARENT,
                        LayoutHelper.WRAP_CONTENT,
                        0,
                        0,
                        0,
                        18
                )
        );

        TextView saveButton = createActionButton(context, "Сохранить", false);
        saveButton.setOnClickListener(v -> save());

        container.addView(
                saveButton,
                LayoutHelper.createLinear(
                        LayoutHelper.MATCH_PARENT,
                        52,
                        0,
                        0,
                        0,
                        10
                )
        );

        TextView resetButton = createActionButton(context, "Вернуть оригинал", true);
        resetButton.setOnClickListener(v -> {
            CustomGramLocalChanges.clearUser(userId);
            finishFragment();
        });

        container.addView(
                resetButton,
                LayoutHelper.createLinear(
                        LayoutHelper.MATCH_PARENT,
                        52
                )
        );

        statusMode = CustomGramLocalChanges.getStatusMode(userId);

        statusOriginal.setOnClickListener(v -> {
            statusMode = CustomGramLocalChanges.STATUS_ORIGINAL;
            updateStatusUi();
        });

        statusStatic.setOnClickListener(v -> {
            statusMode = CustomGramLocalChanges.STATUS_STATIC;
            updateStatusUi();
        });

        statusDynamic.setOnClickListener(v -> {
            statusMode = CustomGramLocalChanges.STATUS_REAL_TIME;
            updateStatusUi();
        });

        View.OnFocusChangeListener previewUpdater = (v, hasFocus) -> {
            if (!hasFocus) {
                updatePreview();
            }
        };

        statusTextField.setOnFocusChangeListener(previewUpdater);
        statusTemplateField.setOnFocusChangeListener(previewUpdater);

        updateStatusUi();
        updatePreview();

        fragmentView = scrollView;
        return fragmentView;
    }

    private void save() {
        CustomGramLocalChanges.setFirstName(
                userId,
                firstNameField.getText().toString().trim()
        );

        CustomGramLocalChanges.setLastName(
                userId,
                lastNameField.getText().toString().trim()
        );

        CustomGramLocalChanges.setUsername(
                userId,
                usernameField.getText().toString().trim()
        );

        CustomGramLocalChanges.setAbout(
                userId,
                aboutField.getText().toString()
        );

        CustomGramLocalChanges.setStatusMode(userId, statusMode);

        CustomGramLocalChanges.setStatusText(
                userId,
                statusTextField.getText().toString()
        );

        CustomGramLocalChanges.setStatusTemplate(
                userId,
                statusTemplateField.getText().toString()
        );

        finishFragment();
    }

    private void updateStatusUi() {
        boolean staticMode =
                statusMode == CustomGramLocalChanges.STATUS_STATIC;

        boolean dynamicMode =
                statusMode == CustomGramLocalChanges.STATUS_REAL_TIME;

        statusTextField.setVisibility(staticMode ? View.VISIBLE : View.GONE);
        statusTemplateField.setVisibility(dynamicMode ? View.VISIBLE : View.GONE);

        updateStatusButtonState(
                statusOriginal,
                statusMode == CustomGramLocalChanges.STATUS_ORIGINAL
        );

        updateStatusButtonState(statusStatic, staticMode);
        updateStatusButtonState(statusDynamic, dynamicMode);

        updatePreview();
    }

    private void updatePreview() {
        String preview;

        if (statusMode == CustomGramLocalChanges.STATUS_ORIGINAL) {
            preview = "Оригинальный статус Telegram";

        } else if (statusMode == CustomGramLocalChanges.STATUS_STATIC) {
            preview = statusTextField.getText().toString();

            if (preview.trim().isEmpty()) {
                preview = "Введите свой статус";
            }

        } else {
            String template = statusTemplateField.getText().toString();

            if (template.trim().isEmpty()) {
                template = "в моём сердце в {time}";
            }

            preview = template
                    .replace("{time}", "04:37")
                    .replace("{date}", "03.10.2026");
        }

        previewText.setText(preview);
    }

    private void addSectionTitle(
            Context context,
            LinearLayout container,
            String text
    ) {
        TextView title = new TextView(context);

        title.setText(text);
        title.setTextSize(14);
        title.setTypeface(AndroidUtilities.bold());
        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));

        container.addView(
                title,
                LayoutHelper.createLinear(
                        LayoutHelper.MATCH_PARENT,
                        LayoutHelper.WRAP_CONTENT,
                        0,
                        12,
                        0,
                        8
                )
        );
    }

    private EditText addField(
            Context context,
            LinearLayout container,
            String hint,
            String value
    ) {
        EditText field = new EditText(context);

        field.setHint(hint);
        field.setText(value == null ? "" : value);
        field.setTextSize(16);

        field.setTextColor(
                Theme.getColor(Theme.key_windowBackgroundWhiteBlackText)
        );

        field.setHintTextColor(
                Theme.getColor(Theme.key_windowBackgroundWhiteHintText)
        );

        field.setSingleLine(false);
        field.setMinLines(1);
        field.setMaxLines(4);

        field.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        );

        container.addView(
                field,
                LayoutHelper.createLinear(
                        LayoutHelper.MATCH_PARENT,
                        LayoutHelper.WRAP_CONTENT,
                        0,
                        0,
                        0,
                        10
                )
        );

        return field;
    }

    private TextView createStatusButton(Context context, String text) {
        TextView view = new TextView(context);
        view.setText(text);
        view.setGravity(Gravity.CENTER);
        view.setTextSize(13);
        return view;
    }

    private void updateStatusButtonState(TextView view, boolean selected) {
        if (selected) {
            view.setTextColor(
                    Theme.getColor(Theme.key_featuredStickers_buttonText)
            );

            view.setBackground(
                    Theme.createRoundRectDrawable(
                            AndroidUtilities.dp(12),
                            Theme.getColor(Theme.key_featuredStickers_addButton)
                    )
            );

        } else {
            view.setTextColor(
                    Theme.getColor(Theme.key_windowBackgroundWhiteBlackText)
            );

            view.setBackground(
                    Theme.createRoundRectDrawable(
                            AndroidUtilities.dp(12),
                            Theme.getColor(Theme.key_windowBackgroundGray)
                    )
            );
        }
    }

    private TextView createActionButton(
            Context context,
            String text,
            boolean destructive
    ) {
        TextView button = new TextView(context);

        button.setText(text);
        button.setGravity(Gravity.CENTER);
        button.setTextSize(16);
        button.setTypeface(AndroidUtilities.bold());

        if (destructive) {
            button.setTextColor(
                    Theme.getColor(Theme.key_text_RedBold)
            );

            button.setBackground(
                    Theme.createRoundRectDrawable(
                            AndroidUtilities.dp(14),
                            Theme.getColor(Theme.key_windowBackgroundGray)
                    )
            );

        } else {
            button.setTextColor(
                    Theme.getColor(Theme.key_featuredStickers_buttonText)
            );

            button.setBackground(
                    Theme.createRoundRectDrawable(
                            AndroidUtilities.dp(14),
                            Theme.getColor(Theme.key_featuredStickers_addButton)
                    )
            );
        }

        return button;
    }
}
