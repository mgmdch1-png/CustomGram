from pathlib import Path

profile = Path(
    "TMessagesProj/src/main/java/org/telegram/ui/ProfileActivity.java"
)

text = profile.read_text(encoding="utf-8")

MENU_MARKER = "CUSTOMGRAM_LOCAL_CHANGES_MENU"
CLICK_MARKER = "CUSTOMGRAM_LOCAL_CHANGES_CLICK"

# ------------------------------------------------------------
# 1. Добавляем пункт "Изменить локально"
# ------------------------------------------------------------

if MENU_MARKER not in text:
    anchor = (
        "otherItem.addSubItem(add_contact, "
        "R.drawable.msg_addcontact, "
        "LocaleController.getString(R.string.AddContact));"
    )

    if anchor not in text:
        raise SystemExit(
            "Не найдено место для пункта меню Local Changes"
        )

    insert = f"""
