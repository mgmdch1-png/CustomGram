package org.telegram.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.CustomGramLocalChanges;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.List;

public class CustomGramLocalChangesListActivity extends BaseFragment {

    private LinearLayout container;

    @Override
    public View createView(Context context) {
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
        scrollView.setFillViewport(true);

        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(16), AndroidUtilities.dp(24));
        scrollView.addView(container, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        fragmentView = scrollView;
        rebuild(context);
        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (container != null && getContext() != null) {
            rebuild(getContext());
        }
    }

    private void rebuild(Context context) {
        container.removeAllViews();

        TextView info = new TextView(context);
        info.setText("Здесь хранятся только локальные изменения. Они не отправляются в Telegram и видны только в CustomGram на этом устройстве.");
        info.setTextSize(14);
        info.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        info.setPadding(0, 0, 0, AndroidUtilities.dp(16));
        container.addView(info, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        List<Long> ids = CustomGramLocalChanges.getModifiedUserIds();
        if (ids.isEmpty()) {
            TextView empty = new TextView(context);
            empty.setText("Локальных изменений пока нет");
            empty.setTextSize(16);
            empty.setGravity(Gravity.CENTER);
            empty.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            container.addView(empty, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 160));
            return;
        }

        TextView header = new TextView(context);
        header.setText("Изменённые профили");
        header.setTextSize(14);
        header.setTypeface(Typeface.DEFAULT_BOLD);
        header.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        header.setPadding(0, 0, 0, AndroidUtilities.dp(8));
        container.addView(header, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        for (Long id : ids) {
            TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(id);
            if (user == null) {
                continue;
            }

            TextView row = new TextView(context);
            String username = CustomGramLocalChanges.getUsername(id, UserObject.getPublicUsername(user));
            String title = UserObject.getUserName(user);
            if (username != null && !username.isEmpty()) {
                title += "\n@" + username;
            }
            row.setText(title);
            row.setTextSize(16);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            row.setPadding(AndroidUtilities.dp(14), AndroidUtilities.dp(10), AndroidUtilities.dp(14), AndroidUtilities.dp(10));
            row.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(14), Theme.getColor(Theme.key_windowBackgroundWhite)));
            row.setOnClickListener(v -> presentFragment(new CustomGramLocalChangesActivity(id)));
            container.addView(row, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 60, 0, 0, 0, 8));
        }

        TextView resetAll = new TextView(context);
        resetAll.setText("Сбросить все локальные изменения");
        resetAll.setTextSize(15);
        resetAll.setTypeface(Typeface.DEFAULT_BOLD);
        resetAll.setGravity(Gravity.CENTER);
        resetAll.setTextColor(Theme.getColor(Theme.key_text_RedBold));
        resetAll.setPadding(0, AndroidUtilities.dp(14), 0, AndroidUtilities.dp(14));
        resetAll.setOnClickListener(v -> {
            CustomGramLocalChanges.clearAll();
            rebuild(context);
        });
        container.addView(resetAll, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 12, 0, 0));
    }
}
