package cn.dujc.v1;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * emoji适配工具
 * Created by lucky on 2018/3/1.
 */
public class EmojiCompat {

    private EmojiCompat() {}

    private static final Map<String, Integer> ID_CACHE = new HashMap<String, Integer>(1300);
    private static final Pattern EMOJI_V1_0_PATTERN = Pattern.compile("[\\u2194-\\ud83e\\udd84|\\ud83e\\uddc0|\\u00a9|\\u00ae|\\u203c|\\u2049|\\u2122|\\u2139]");

    /**
     * 通过emoji的字符串名称获取对应的图片id
     */
    private static int getEmotionResourceId(Context context, String name) {
        final String drawableResName = name.toLowerCase().replace('\\', '_');
        Integer resourceId = ID_CACHE.get(drawableResName);
        if (resourceId == null) {
            resourceId = context.getResources().getIdentifier(drawableResName, "drawable", context.getPackageName());
            ID_CACHE.put(drawableResName, resourceId);
        }
        return resourceId;
    }

    /**
     * 通过id获取drawable
     */
    private static final Drawable getDrawable(Context context, int id) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            return context.getDrawable(id);
        } else {
            return context.getResources().getDrawable(id);
        }
    }

    /**
     * 生成替换后的spannable，并通过lineHeight设置drawable大小，lineHeight<=0时使用drawable中比较小的一边
     */
    private static CharSequence generate(Context context, CharSequence text, int lineHeight){
        final Spannable source = new SpannableString(text == null ? "" : text);
        final Matcher matcher = EMOJI_V1_0_PATTERN.matcher(source);
        while (matcher.find()) {
            final String group = matcher.group();
            final int resourceId = getEmotionResourceId(context, unicodeLike(group));
            if (resourceId != 0) {
                final Drawable drawable = getDrawable(context, resourceId);
                if (lineHeight <= 0) lineHeight = Math.min(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                drawable.setBounds(0, 0, lineHeight, lineHeight);
                source.setSpan(new ImageSpan(drawable), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return source;
    }

    /**
     * 将字符串转换为unicode样式的\\uxxxx的字符串
     */
    private static String unicodeLike(String string) {
        StringBuffer unicode = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            // 取出每一个字符
            char c = string.charAt(i);
            // 转换为unicode
            unicode.append("\\u").append(Integer.toHexString(c));
        }
        return unicode.toString();
    }

    /**
     * 生成替换emoji后的spannable
     */
    public static CharSequence generate(Context context, CharSequence text){
        return generate(context, text, 0);
    }

    /**
     * 直接设置textView格式化后的字符串，并返回
     */
    public static CharSequence setText(TextView textView, CharSequence text){
        if (textView == null) return text;
        final CharSequence generate = generate(textView.getContext(), text, textView.getLineHeight());
        textView.setText(generate);
        return generate;
    }
}
