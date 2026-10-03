from pathlib import Path


def patch(path, old, new, marker):
    p = Path(path)
    text = p.read_text(encoding="utf-8")
    if marker in text:
        return
    if old not in text:
        raise SystemExit(f"CustomGram UI patch anchor not found: {path} / {marker}")
    p.write_text(text.replace(old, new, 1), encoding="utf-8")

settings = "TMessagesProj/src/main/java/org/telegram/ui/SettingsActivity.java"
row = 'items.add(SettingCell.Factory.of(10, IconBackgroundColors.PURPLE.top, IconBackgroundColors.PURPLE.bottom, R.drawable.settings_language, getString(R.string.SettingsLanguage), LocaleController.getCurrentLanguageName()));'
patch(settings, row, row + '''\n\n        // CUSTOMGRAM_SETTINGS_HUB_ROW\n        items.add(SettingCell.Factory.of(90, IconBackgroundColors.BLUE.top, IconBackgroundColors.PURPLE.bottom, R.drawable.settings_features, "Настройки CustomGram", "Функции и внешний вид клиента"));''', "CUSTOMGRAM_SETTINGS_HUB_ROW")

click = '''            case 11:\n                presentSettingFragment(new PremiumPreviewFragment("settings"));'''
patch(settings, click, '''            // CUSTOMGRAM_SETTINGS_HUB_CLICK\n            case 90:\n                presentSettingFragment(new CustomGramSettingsActivity());\n                break;\n\n            case 11:\n                presentSettingFragment(new PremiumPreviewFragment("settings"));''', "CUSTOMGRAM_SETTINGS_HUB_CLICK")

# Standalone is the APK target we care about. Register local-only receivers there.
manifest = "TMessagesProj/config/release/AndroidManifest_standalone.xml"
anchor = "</application>"
receivers = '''        <!-- CUSTOMGRAM_REMINDER_RECEIVERS -->\n        <receiver android:name="org.telegram.messenger.CustomGramReminderReceiver" android:exported="false" />\n        <receiver android:name="org.telegram.messenger.CustomGramReminderActionReceiver" android:exported="false" />\n    </application>'''
patch(manifest, anchor, receivers, "CUSTOMGRAM_REMINDER_RECEIVERS")

print("CustomGram UI/reminder integration patches applied.")
