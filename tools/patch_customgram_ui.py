from pathlib import Path


def replace_required(path, old, new, label):
    p = Path(path)
    text = p.read_text(encoding="utf-8")
    if new in text:
        return
    if old not in text:
        raise SystemExit(f"CustomGram UI patch anchor not found: {label}")
    p.write_text(text.replace(old, new, 1), encoding="utf-8")


def add_import(path, anchor, import_line, label):
    p = Path(path)
    text = p.read_text(encoding="utf-8")
    if import_line in text:
        return
    if anchor not in text:
        raise SystemExit(f"CustomGram import anchor not found: {label}")
    p.write_text(text.replace(anchor, anchor + "\n" + import_line, 1), encoding="utf-8")


# -----------------------------------------------------------------------------
# SettingsActivity: CustomGram hub + results in Telegram's normal settings search
# -----------------------------------------------------------------------------
settings = "TMessagesProj/src/main/java/org/telegram/ui/SettingsActivity.java"
add_import(settings, "import org.telegram.messenger.ContactsController;" if "import org.telegram.messenger.ContactsController;" in Path(settings).read_text(encoding="utf-8") else "import org.telegram.messenger.ChatThemeController;", "import org.telegram.messenger.CustomGramConfig;", "SettingsActivity CustomGramConfig")

legacy_row = '''        // CUSTOMGRAM_LOCAL_CHANGES_SETTINGS_ROW
        items.add(SettingCell.Factory.of(
                90,
                IconBackgroundColors.PURPLE.top,
                IconBackgroundColors.BLUE_ALT.bottom,
                R.drawable.msg_edit,
                "Локальные изменения",
                "Локальная подмена профилей и статусов"
        ));'''
hub_row = '''        // CUSTOMGRAM_SETTINGS_HUB_ROW
        items.add(SettingCell.Factory.of(
                90,
                IconBackgroundColors.BLUE.top,
                IconBackgroundColors.PURPLE.bottom,
                R.drawable.settings_features,
                "Настройки CustomGram",
                "Функции и внешний вид клиента"
        ));'''
replace_required(settings, legacy_row, hub_row, "settings hub row")

legacy_click = '''            // CUSTOMGRAM_LOCAL_CHANGES_SETTINGS_CLICK
            case 90:
                presentSettingFragment(new CustomGramLocalChangesListActivity());
                break;'''
hub_click = '''            // CUSTOMGRAM_SETTINGS_HUB_CLICK
            case 90:
                presentSettingFragment(new CustomGramSettingsActivity());
                break;'''
replace_required(settings, legacy_click, hub_click, "settings hub click")

search_old = '''        if (searchItem.isSearchFieldVisible2()) {
            items.add(UItem.asSpace(ActionBar.getCurrentActionBarHeight()));
            search.fillItems(items);
            return;
        }'''
search_new = '''        if (searchItem.isSearchFieldVisible2()) {
            items.add(UItem.asSpace(ActionBar.getCurrentActionBarHeight()));
            // CUSTOMGRAM_SETTINGS_SEARCH
            if (CustomGramConfig.matchesSearch(query)) {
                items.add(SettingCell.Factory.of(
                        90,
                        IconBackgroundColors.BLUE.top,
                        IconBackgroundColors.PURPLE.bottom,
                        R.drawable.settings_features,
                        "Настройки CustomGram",
                        "Найдено среди функций CustomGram"
                ));
            }
            search.fillItems(items);
            return;
        }'''
replace_required(settings, search_old, search_new, "standard settings search integration")


# -----------------------------------------------------------------------------
# Global ActionBar cleanup: remove round/capsule touch backgrounds when enabled.
# This changes the actual controls, not an overlay/plugin imitation.
# -----------------------------------------------------------------------------
actionbar = "TMessagesProj/src/main/java/org/telegram/ui/ActionBar/ActionBar.java"
add_import(actionbar, "import org.telegram.messenger.AndroidUtilities;", "import org.telegram.messenger.CustomGramConfig;", "ActionBar CustomGramConfig")
replace_required(
    actionbar,
    "backButtonImageView.setBackgroundDrawable(Theme.createSelectorDrawable(itemsBackgroundColor));",
    '''// CUSTOMGRAM_CLEAN_ACTIONBAR_BACK
        if (!CustomGramConfig.cleanActionBar()) {
            backButtonImageView.setBackgroundDrawable(Theme.createSelectorDrawable(itemsBackgroundColor));
        } else {
            backButtonImageView.setBackground(null);
        }''',
    "clean back button background"
)

menu_item = "TMessagesProj/src/main/java/org/telegram/ui/ActionBar/ActionBarMenuItem.java"
add_import(menu_item, "import org.telegram.messenger.ChatObject;", "import org.telegram.messenger.CustomGramConfig;", "ActionBarMenuItem CustomGramConfig")
replace_required(
    menu_item,
    '''        if (backgroundColor != 0) {
            setBackgroundDrawable(Theme.createSelectorDrawable(backgroundColor, text ? 5 : 1));
        }''',
    '''        // CUSTOMGRAM_CLEAN_ACTIONBAR_ITEMS
        if (backgroundColor != 0 && !CustomGramConfig.cleanActionBar()) {
            setBackgroundDrawable(Theme.createSelectorDrawable(backgroundColor, text ? 5 : 1));
        } else if (CustomGramConfig.cleanActionBar()) {
            setBackground(null);
        }''',
    "clean action bar item backgrounds"
)


# -----------------------------------------------------------------------------
# ChatActivity: real hide-call setting in the actual chat header.
# -----------------------------------------------------------------------------
chat = "TMessagesProj/src/main/java/org/telegram/ui/ChatActivity.java"
add_import(chat, "import org.telegram.messenger.ContactsController;", "import org.telegram.messenger.CustomGramConfig;", "ChatActivity CustomGramConfig")
replace_required(
    chat,
    "if (chatMode == 0 && (threadMessageId == 0 || isTopic) && !UserObject.isReplyUser(currentUser) && !isReport()) {",
    "if (chatMode == 0 && (threadMessageId == 0 || isTopic) && !UserObject.isReplyUser(currentUser) && !isReport() && !CustomGramConfig.hideChatCallButton()) {",
    "hide chat call button"
)


# -----------------------------------------------------------------------------
# DialogsActivity: configurable main title (defaults to CustomGram).
# -----------------------------------------------------------------------------
dialogs = "TMessagesProj/src/main/java/org/telegram/ui/DialogsActivity.java"
add_import(dialogs, "import org.telegram.messenger.ContactsController;", "import org.telegram.messenger.CustomGramConfig;", "DialogsActivity CustomGramConfig")
replace_required(
    dialogs,
    '''                SpannableStringBuilder ssb = new SpannableStringBuilder(getString(R.string.AppName));
                ssb.setSpan(new ImageSpan(logoDrawable), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                actionBar.setTitle(ssb, statusDrawable);''',
    '''                // CUSTOMGRAM_MAIN_TITLE
                CharSequence customGramTitle = CustomGramConfig.getMainTitle();
                if ("Telegram".contentEquals(customGramTitle)) {
                    SpannableStringBuilder ssb = new SpannableStringBuilder(getString(R.string.AppName));
                    ssb.setSpan(new ImageSpan(logoDrawable), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    actionBar.setTitle(ssb, statusDrawable);
                } else {
                    actionBar.setTitle(customGramTitle, statusDrawable);
                }''',
    "CustomGram main title"
)


# -----------------------------------------------------------------------------
# Reminder receivers: install into every release manifest variant, not standalone only.
# -----------------------------------------------------------------------------
for manifest in Path("TMessagesProj/config/release").glob("AndroidManifest*.xml"):
    text = manifest.read_text(encoding="utf-8")
    if "CUSTOMGRAM_REMINDER_RECEIVERS" in text:
        continue
    anchor = "</application>"
    if anchor not in text:
        continue
    text = text.replace(anchor, '''        <!-- CUSTOMGRAM_REMINDER_RECEIVERS -->
        <receiver android:name="org.telegram.messenger.CustomGramReminderReceiver" android:exported="false" />
        <receiver android:name="org.telegram.messenger.CustomGramReminderActionReceiver" android:exported="false" />
    </application>''', 1)
    manifest.write_text(text, encoding="utf-8")

print("CustomGram functional UI/runtime integration patches applied.")
