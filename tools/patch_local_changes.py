from pathlib import Path

profile = Path(
    "TMessagesProj/src/main/java/org/telegram/ui/ProfileActivity.java"
)

text = profile.read_text(encoding="utf-8")

MENU_MARKER = "CUSTOMGRAM_LOCAL_CHANGES_MENU"
CLICK_MARKER = "CUSTOMGRAM_LOCAL_CHANGES_CLICK"

# 1. Добавляем пункт "Изменить локально"
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
                // {MENU_MARKER}
                if (user != null && !UserObject.isUserSelf(user)) {{
                    otherItem.addSubItem(
                            9001,
                            R.drawable.msg_edit,
                            "Изменить локально"
                    );
                }}

                {anchor}
"""

    text = text.replace(anchor, insert, 1)

# 2. Добавляем обработчик нажатия
if CLICK_MARKER not in text:
    handler_anchor = "public void onItemClick(int id) {"

    pos = text.find(handler_anchor)

    if pos == -1:
        raise SystemExit(
            "Не найден обработчик ActionBar menu"
        )

    body_start = pos + len(handler_anchor)

    handler = f"""

                // {CLICK_MARKER}
                if (id == 9001) {{
                    TLRPC.User customGramUser =
                            getMessagesController().getUser(userId);

                    if (customGramUser != null
                            && !UserObject.isUserSelf(customGramUser)) {{
                        presentFragment(
                                new CustomGramLocalChangesActivity(
                                        customGramUser.id
                                )
                        );
                    }}
                    return;
                }}
"""

    text = (
        text[:body_start]
        + handler
        + text[body_start:]
    )

profile.write_text(text, encoding="utf-8")

print("CustomGram Local Changes patch applied successfully.")
