from pathlib import Path


def read(path):
    return Path(path).read_text(encoding="utf-8")


def write(path, text):
    Path(path).write_text(text, encoding="utf-8")


def replace_once(text, old, new, label):
    if old not in text:
        raise SystemExit(f"Не найдено место для патча: {label}")
    return text.replace(old, new, 1)


# -----------------------------------------------------------------------------
# ProfileActivity: меню, открытие редактора, bio и статус
# -----------------------------------------------------------------------------
profile_path = "TMessagesProj/src/main/java/org/telegram/ui/ProfileActivity.java"
profile = read(profile_path)

if "import org.telegram.messenger.CustomGramLocalChanges;" not in profile:
    profile = replace_once(
        profile,
        "import org.telegram.messenger.ContactsController;",
        "import org.telegram.messenger.ContactsController;\nimport org.telegram.messenger.CustomGramLocalChanges;",
        "ProfileActivity import"
    )

if "CUSTOMGRAM_LOCAL_CHANGES_MENU" not in profile:
    anchor = """            } else {
                if (user.bot && user.bot_can_edit) {"""
    insert = """            } else {
                // CUSTOMGRAM_LOCAL_CHANGES_MENU
                otherItem.addSubItem(
                        9001,
                        R.drawable.msg_edit,
                        "Изменить локально"
                );

                if (user.bot && user.bot_can_edit) {"""
    profile = replace_once(profile, anchor, insert, "пункт меню профиля")

if "CUSTOMGRAM_LOCAL_CHANGES_CLICK" not in profile:
    anchor = "public void onItemClick(final int id) {"
    insert = """public void onItemClick(final int id) {
                // CUSTOMGRAM_LOCAL_CHANGES_CLICK
                if (id == 9001) {
                    TLRPC.User customGramUser = getMessagesController().getUser(userId);
                    if (customGramUser != null && !UserObject.isUserSelf(customGramUser)) {
                        presentFragment(new CustomGramLocalChangesActivity(customGramUser.id));
                    }
                    return;
                }
"""
    profile = replace_once(profile, anchor, insert, "обработчик меню профиля")

if "CUSTOMGRAM_LOCAL_STATUS_PROFILE" not in profile:
    old = "newString2 = LocaleController.formatUserStatus(currentAccount, user, isOnline, shortStatus ? new boolean[1] : null);"
    new = """// CUSTOMGRAM_LOCAL_STATUS_PROFILE
                newString2 = LocaleController.formatUserStatus(currentAccount, user, isOnline, shortStatus ? new boolean[1] : null);"""
    profile = replace_once(profile, old, new, "статус профиля")

# Локальное bio должно появляться даже если оригинальный bio пустой.
profile = profile.replace(
    "userInfo != null && !TextUtils.isEmpty(userInfo.about)",
    "userInfo != null && (!TextUtils.isEmpty(userInfo.about) || CustomGramLocalChanges.hasAboutOverride(userId))"
)

profile = profile.replace(
    "aboutLinkCell.setTextAndValue(userInfo.about, LocaleController.getString(R.string.UserBio), addlinks);",
    "aboutLinkCell.setTextAndValue(CustomGramLocalChanges.getAbout(userId, userInfo.about), LocaleController.getString(R.string.UserBio), addlinks);"
)

profile = profile.replace(
    "value = userInfo == null ? LocaleController.getString(R.string.Loading) : userInfo.about;",
    "value = userInfo == null ? LocaleController.getString(R.string.Loading) : CustomGramLocalChanges.getAbout(userId, userInfo.about);"
)

profile = profile.replace(
    "text = userInfo != null ? userInfo.about : null;",
    "text = userInfo != null ? CustomGramLocalChanges.getAbout(userId, userInfo.about) : null;"
)

write(profile_path, profile)


# -----------------------------------------------------------------------------
# UserObject: локальное имя и username работают по всему клиенту
# -----------------------------------------------------------------------------
user_object_path = "TMessagesProj/src/main/java/org/telegram/messenger/UserObject.java"
user_object = read(user_object_path)

if "CUSTOMGRAM_LOCAL_NAME_GLOBAL" not in user_object:
    old = "String name = AndroidUtilities.removeRTL(AndroidUtilities.removeDiacritics(ContactsController.formatName(user.first_name, user.last_name)));"
    new = """// CUSTOMGRAM_LOCAL_NAME_GLOBAL
        String customFirstName = CustomGramLocalChanges.getFirstName(user.id, user.first_name);
        String customLastName = CustomGramLocalChanges.getLastName(user.id, user.last_name);
        String name = AndroidUtilities.removeRTL(AndroidUtilities.removeDiacritics(ContactsController.formatName(customFirstName, customLastName)));"""
    user_object = replace_once(user_object, old, new, "глобальное локальное имя")

if "CUSTOMGRAM_LOCAL_USERNAME_GLOBAL" not in user_object:
    old = """        if (!TextUtils.isEmpty(user.username)) {
            return user.username;
        }"""
    new = """        // CUSTOMGRAM_LOCAL_USERNAME_GLOBAL
        if (CustomGramLocalChanges.hasUsernameOverride(user.id)) {
            String localUsername = CustomGramLocalChanges.getUsername(user.id, user.username);
            return TextUtils.isEmpty(localUsername) ? null : localUsername;
        }
        if (!TextUtils.isEmpty(user.username)) {
            return user.username;
        }"""
    user_object = replace_once(user_object, old, new, "глобальный локальный username")

if "CUSTOMGRAM_LOCAL_FIRST_NAME_GLOBAL" not in user_object:
    old = """        String name = user.first_name;
        if (TextUtils.isEmpty(name)) {
            name = user.last_name;
        } else if (!allowShort && name.length() <= 2) {
            return ContactsController.formatName(user.first_name, user.last_name);
        }"""
    new = """        // CUSTOMGRAM_LOCAL_FIRST_NAME_GLOBAL
        String localFirstName = CustomGramLocalChanges.getFirstName(user.id, user.first_name);
        String localLastName = CustomGramLocalChanges.getLastName(user.id, user.last_name);
        String name = localFirstName;
        if (TextUtils.isEmpty(name)) {
            name = localLastName;
        } else if (!allowShort && name.length() <= 2) {
            return ContactsController.formatName(localFirstName, localLastName);
        }"""
    user_object = replace_once(user_object, old, new, "локальное короткое имя")

if "CUSTOMGRAM_LOCAL_FORCED_FIRST_NAME_GLOBAL" not in user_object:
    old = """        String name = user.first_name;
        if (TextUtils.isEmpty(name)) {
            name = user.last_name;
        }
        if (name == null) {"""
    new = """        // CUSTOMGRAM_LOCAL_FORCED_FIRST_NAME_GLOBAL
        String name = CustomGramLocalChanges.getFirstName(user.id, user.first_name);
        if (TextUtils.isEmpty(name)) {
            name = CustomGramLocalChanges.getLastName(user.id, user.last_name);
        }
        if (name == null) {"""
    user_object = replace_once(user_object, old, new, "локальное forced first name")

if "CUSTOMGRAM_LOCAL_HAS_USERNAME_GLOBAL" not in user_object:
    old = """        if (username.equalsIgnoreCase(user.username)) {
            return true;
        }"""
    new = """        // CUSTOMGRAM_LOCAL_HAS_USERNAME_GLOBAL
        if (CustomGramLocalChanges.hasUsernameOverride(user.id)) {
            String localUsername = CustomGramLocalChanges.getUsername(user.id, user.username);
            return !TextUtils.isEmpty(localUsername) && username.equalsIgnoreCase(localUsername);
        }
        if (username.equalsIgnoreCase(user.username)) {
            return true;
        }"""
    user_object = replace_once(user_object, old, new, "локальная проверка username")

write(user_object_path, user_object)


# -----------------------------------------------------------------------------
# LocaleController: локальный статус применяется во всех стандартных местах,
# которые используют Telegram formatUserStatus().
# -----------------------------------------------------------------------------
locale_path = "TMessagesProj/src/main/java/org/telegram/messenger/LocaleController.java"
locale = read(locale_path)

if "CUSTOMGRAM_LOCAL_STATUS_GLOBAL" not in locale:
    signature = "public static String formatUserStatus(int currentAccount, TLRPC.User user, boolean[] isOnline, boolean[] madeShorter) {"
    replacement = """// CUSTOMGRAM_LOCAL_STATUS_GLOBAL
    public static String formatUserStatus(int currentAccount, TLRPC.User user, boolean[] isOnline, boolean[] madeShorter) {
        String original = formatUserStatusOriginal(currentAccount, user, isOnline, madeShorter);
        return CustomGramLocalChanges.formatStatus(currentAccount, user, original);
    }

    private static String formatUserStatusOriginal(int currentAccount, TLRPC.User user, boolean[] isOnline, boolean[] madeShorter) {"""
    locale = replace_once(locale, signature, replacement, "глобальный локальный статус")

write(locale_path, locale)


# -----------------------------------------------------------------------------
# SettingsActivity: отдельный раздел «Локальные изменения»
# -----------------------------------------------------------------------------
settings_path = "TMessagesProj/src/main/java/org/telegram/ui/SettingsActivity.java"
settings = read(settings_path)

if "CUSTOMGRAM_LOCAL_CHANGES_SETTINGS_ROW" not in settings:
    anchor = "items.add(SettingCell.Factory.of(10, IconBackgroundColors.PURPLE.top, IconBackgroundColors.PURPLE.bottom, R.drawable.settings_language, getString(R.string.SettingsLanguage), LocaleController.getCurrentLanguageName()));"
    insert = anchor + """

        // CUSTOMGRAM_LOCAL_CHANGES_SETTINGS_ROW
        items.add(SettingCell.Factory.of(
                90,
                IconBackgroundColors.PURPLE.top,
                IconBackgroundColors.BLUE_ALT.bottom,
                R.drawable.msg_edit,
                "Локальные изменения",
                "Локальная подмена профилей и статусов"
        ));"""
    settings = replace_once(settings, anchor, insert, "раздел в настройках")

if "CUSTOMGRAM_LOCAL_CHANGES_SETTINGS_CLICK" not in settings:
    anchor = """            case 11:
                presentSettingFragment(new PremiumPreviewFragment("settings"));"""
    insert = """            // CUSTOMGRAM_LOCAL_CHANGES_SETTINGS_CLICK
            case 90:
                presentSettingFragment(new CustomGramLocalChangesListActivity());
                break;

            case 11:
                presentSettingFragment(new PremiumPreviewFragment("settings"));"""
    settings = replace_once(settings, anchor, insert, "обработчик раздела настроек")

write(settings_path, settings)

print("CustomGram Local Changes: все патчи успешно применены.")
