from pathlib import Path


def replace_required(path, old, new, label):
    p = Path(path)
    text = p.read_text(encoding="utf-8")
    if new in text:
        return
    if old not in text:
        raise SystemExit(f"CustomGram UI patch anchor not found: {label}")
    p.write_text(text.replace(old, new, 1), encoding="utf-8")

settings = "TMessagesProj/src/main/java/org/telegram/ui/SettingsActivity.java"
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

manifest = "TMessagesProj/config/release/AndroidManifest_standalone.xml"
p = Path(manifest)
text = p.read_text(encoding="utf-8")
if "CUSTOMGRAM_REMINDER_RECEIVERS" not in text:
    anchor = "</application>"
    if anchor not in text:
        raise SystemExit("CustomGram reminder manifest anchor not found")
    text = text.replace(anchor, '''        <!-- CUSTOMGRAM_REMINDER_RECEIVERS -->
        <receiver android:name="org.telegram.messenger.CustomGramReminderReceiver" android:exported="false" />
        <receiver android:name="org.telegram.messenger.CustomGramReminderActionReceiver" android:exported="false" />
    </application>''', 1)
    p.write_text(text, encoding="utf-8")

print("CustomGram UI/reminder integration patches applied.")
