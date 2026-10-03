package org.telegram.ui;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.CustomGramReminders;
import org.telegram.messenger.CustomGramReminderScheduler;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class CustomGramRemindersActivity extends BaseFragment {
    private EditText textField, minutesField;
    private TextView previewBody, previewWhen, repeatButton;
    private LinearLayout activeList;
    private int repeatMinutes;

    @Override public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Умные напоминания");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() { @Override public void onItemClick(int id) { if (id == -1) finishFragment(); }});
        ScrollView scroll = new ScrollView(context); scroll.setFillViewport(true); scroll.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        LinearLayout root = new LinearLayout(context); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(16), dp(16), dp(16), dp(32));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));

        header(context, root, "Предпросмотр");
        LinearLayout preview = card(context); preview.setPadding(dp(18), dp(16), dp(18), dp(14));
        preview.addView(text(context, "CustomGram", 14, true, Theme.key_windowBackgroundWhiteGrayText));
        previewBody = text(context, "Попей воду 💧", 18, true, Theme.key_windowBackgroundWhiteBlackText); preview.addView(previewBody, lp(0,7,0,0));
        previewWhen = text(context, "Через 30 мин", 13, false, Theme.key_windowBackgroundWhiteGrayText); preview.addView(previewWhen, lp(0,4,0,12));
        preview.addView(text(context, "Выполнено        Отложить на 10 мин", 14, true, Theme.key_windowBackgroundWhiteBlueText));
        root.addView(preview, lp(0,0,0,18));

        header(context, root, "Новое напоминание");
        LinearLayout editor = card(context); editor.setPadding(dp(14), dp(12), dp(14), dp(14)); root.addView(editor);
        textField = field(context, "Что напомнить", "Попей воду 💧", false); editor.addView(textField, LayoutHelper.createLinear(-1,54));
        minutesField = field(context, "Через сколько минут", "30", true); editor.addView(minutesField, lp(0,8,0,0));

        LinearLayout presets = new LinearLayout(context); presets.setOrientation(LinearLayout.HORIZONTAL); presets.setGravity(Gravity.CENTER_VERTICAL);
        addPreset(context, presets, "5 мин", 5); addPreset(context, presets, "30 мин", 30); addPreset(context, presets, "1 час", 60); addPreset(context, presets, "3 часа", 180);
        editor.addView(presets, lp(0,10,0,0));

        repeatButton = pill(context, "Не повторять"); repeatButton.setOnClickListener(v -> cycleRepeat()); editor.addView(repeatButton, lp(0,10,0,0));
        TextView hint = text(context, "Работает локально. После срабатывания можно выполнить задачу или отложить её прямо из системного уведомления.", 13, false, Theme.key_windowBackgroundWhiteGrayText); hint.setPadding(dp(4),dp(10),dp(4),0); editor.addView(hint);

        TextView save = pill(context, "Создать напоминание"); save.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText)); save.setBackground(Theme.createRoundRectDrawable(dp(15), Theme.getColor(Theme.key_featuredStickers_addButton))); save.setOnClickListener(v -> saveReminder());
        root.addView(save, LayoutHelper.createLinear(-1,52,0,16,0,18));

        header(context, root, "Активные"); activeList = new LinearLayout(context); activeList.setOrientation(LinearLayout.VERTICAL); root.addView(activeList, LayoutHelper.createLinear(-1,-2)); rebuildActive(context);

        TextWatcher watcher = new TextWatcher() { public void beforeTextChanged(CharSequence s,int a,int b,int c){} public void onTextChanged(CharSequence s,int a,int b,int c){ updatePreview(); } public void afterTextChanged(Editable e){} };
        textField.addTextChangedListener(watcher); minutesField.addTextChangedListener(watcher); updatePreview(); fragmentView = scroll; return fragmentView;
    }

    @Override public void onResume() {
        super.onResume();
        rebuildActive(getContext());
    }

    private void cycleRepeat() { if (repeatMinutes == 0) repeatMinutes = 60; else if (repeatMinutes == 60) repeatMinutes = 24*60; else repeatMinutes = 0; repeatButton.setText(repeatMinutes == 0 ? "Не повторять" : repeatMinutes == 60 ? "Повторять каждый час" : "Повторять каждый день"); updatePreview(); }
    private void addPreset(Context c, LinearLayout p, String label, int value) { TextView b = pill(c,label); b.setTextSize(13); b.setOnClickListener(v -> { minutesField.setText(String.valueOf(value)); minutesField.setSelection(minutesField.length()); }); p.addView(b,new LinearLayout.LayoutParams(0,dp(40),1f)); }

    private void saveReminder() { String body=textField.getText().toString().trim(); if(body.isEmpty())return; int minutes=parseMinutes(); long id=System.currentTimeMillis(); CustomGramReminders.Item item=new CustomGramReminders.Item(id,body,id+minutes*60_000L,repeatMinutes,true); CustomGramReminders.save(item); CustomGramReminderScheduler.schedule(item); previewWhen.setText("Создано • через "+minutesLabel(minutes)); rebuildActive(getContext()); }
    private void rebuildActive(Context c) {
        if(activeList==null||c==null)return;
        activeList.removeAllViews();
        List<CustomGramReminders.Item> items=CustomGramReminders.getAll();
        if(items.isEmpty()){
            LinearLayout empty = card(c);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(20), dp(20), dp(20), dp(20));
            TextView e=text(c,"Активных напоминаний пока нет",15,false,Theme.key_windowBackgroundWhiteGrayText);
            e.setGravity(Gravity.CENTER);
            empty.addView(e, LayoutHelper.createLinear(-1, -2));
            activeList.addView(empty, LayoutHelper.createLinear(-1,-2));
            return;
        }
        for(CustomGramReminders.Item item:items){
            LinearLayout row=card(c);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(16),dp(12),dp(12),dp(12));
            LinearLayout words=new LinearLayout(c);
            words.setOrientation(LinearLayout.VERTICAL);
            TextView a=text(c,item.text,16,true,Theme.key_windowBackgroundWhiteBlackText);
            a.setMaxLines(2);
            words.addView(a);
            String when=DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(new Date(item.triggerAt));
            TextView b=text(c,when+(item.repeatMinutes>0?" • повтор":""),12,false,Theme.key_windowBackgroundWhiteGrayText);
            words.addView(b,lp(0,3,0,0));
            row.addView(words,new LinearLayout.LayoutParams(0,-2,1f));
            TextView del=pill(c,"Удалить");
            del.setTextSize(12);
            del.setTextColor(Theme.getColor(Theme.key_text_RedBold));
            del.setOnClickListener(v->{CustomGramReminderScheduler.cancel(item.id);CustomGramReminders.remove(item.id);rebuildActive(c);});
            row.addView(del,LayoutHelper.createLinear(82,40, Gravity.CENTER_VERTICAL));
            activeList.addView(row,lp(0,0,0,8));
        }
    }
    private void updatePreview(){ if(previewBody==null||textField==null)return; String body=textField.getText().toString().trim(); previewBody.setText(body.isEmpty()?"Текст напоминания":body); String suffix=repeatMinutes==0?"":repeatMinutes==60?" • каждый час":" • каждый день"; previewWhen.setText("Через "+minutesLabel(parseMinutes())+suffix); }
    private int parseMinutes(){try{return Math.max(1,Integer.parseInt(minutesField.getText().toString().trim()));}catch(Exception e){return 1;}}
    private String minutesLabel(int m){if(m%60==0){int h=m/60;return h+" ч";}return m+" мин";}
    private EditText field(Context c,String hint,String value,boolean number){EditText e=new EditText(c);e.setHint(hint);e.setText(value);e.setTextSize(16);e.setSingleLine(true);e.setPadding(dp(12),0,dp(12),0);e.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));e.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));e.setBackground(Theme.createRoundRectDrawable(dp(13),Theme.getColor(Theme.key_windowBackgroundGray)));e.setInputType(number?InputType.TYPE_CLASS_NUMBER:InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);return e;}
    private LinearLayout card(Context c){LinearLayout x=new LinearLayout(c);x.setOrientation(LinearLayout.VERTICAL);x.setBackground(Theme.createRoundRectDrawable(dp(20),Theme.getColor(Theme.key_windowBackgroundWhite)));return x;}
    private TextView pill(Context c,String s){TextView t=text(c,s,14,true,Theme.key_windowBackgroundWhiteBlueText);t.setGravity(Gravity.CENTER);t.setBackground(Theme.createRoundRectDrawable(dp(13),Theme.getColor(Theme.key_windowBackgroundGray)));return t;}
    private void header(Context c,LinearLayout root,String s){TextView h=text(c,s,14,true,Theme.key_windowBackgroundWhiteBlueHeader);h.setPadding(dp(6),0,0,dp(8));root.addView(h);}
    private TextView text(Context c,String s,int z,boolean bold,int key){TextView t=new TextView(c);t.setText(s);t.setTextSize(z);if(bold)t.setTypeface(AndroidUtilities.bold());t.setTextColor(Theme.getColor(key));return t;}
    private LinearLayout.LayoutParams lp(int l,int t,int r,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}
    private int dp(int v){return AndroidUtilities.dp(v);}
}
