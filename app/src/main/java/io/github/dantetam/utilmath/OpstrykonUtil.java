package io.github.dantetam.utilmath;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Dante on 8/20/2016.
 */
public class OpstrykonUtil {

    //Sort elements (k,v) of a mapping by a comparator over v1, v2, ... in descending order
    public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map)
    {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        //Note that below we reverse the compareTo operation so that this is a descending sort.
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry: list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    //Add a template for an inline image where imageName is a single name (no R or raw/drawable/etc.)
    public static void addImageSpan(Context context, TextView textView, String imageName) {
        String stringy = textView.getText() + "<{" + imageName + "}>";
        textView.setText(stringy);
        processImageSpan(context, textView);
    }

    public static void processImageSpan(Context context, TextView textView) {
        SpannableString ss = new SpannableString(textView.getText());
        for (int i = 0; i < textView.getText().length() - 1; i++) {
            if (textView.getText().charAt(i) == '<' && textView.getText().charAt(i + 1) == '{') {
                boolean foundTag = false;
                int j = i;
                for (; j < textView.getText().length() - 1; j++) {
                    if (textView.getText().charAt(j) == '}' && textView.getText().charAt(j + 1) == '>') {
                        foundTag = true;
                        break;
                    }
                }
                if (foundTag) {
                    String drawableName = textView.getText().toString().substring(i + 2, j);
                    int resId = context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName());
                    Drawable drawable = context.getResources().getDrawable(resId);

                    Rect bounds = new Rect();
                    textView.getPaint().getTextBounds(textView.getText().toString(), 0, textView.getText().length(), bounds);

                    drawable.setBounds(0, 0, bounds.height(), bounds.height());
                    ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
                    ss.setSpan(span, i, j + 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                    textView.setText(ss);
                } else {
                    System.err.println("Could not find end tag ( }> ) to image declared");
                }
            }
        }
        textView.setText(ss);
    }

}
