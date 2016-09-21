package io.github.dantetam.utilmath;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.github.dantetam.android.TextureHelper;

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
    /*public static void addImageSpan(Context context, TextView textView, String imageName) {
        String stringy = textView.getText() + "<{" + imageName + "}>";
        textView.setText(stringy);
        processImageSpan(context, textView);
    }

    public static void processImageSpan(Context context, TextView textView) {
        SpannableString ss = new SpannableString(textView.getText());
        for (int i = 0; i < textView.getText().length() - 1; i++) {
            boolean tagBeginFirstType = textView.getText().charAt(i) == '<' && textView.getText().charAt(i + 1) == '{';
            boolean tagBeginSecondType = textView.getText().charAt(i) == '-' && textView.getText().charAt(i + 1) == '{';
            if (tagBeginFirstType || tagBeginSecondType) {
                boolean foundTag = false;
                int j = i;
                for (; j < textView.getText().length() - 1; j++) {
                    boolean tagEndFirstType = textView.getText().charAt(j) == '}' && textView.getText().charAt(j + 1) == '>';
                    boolean tagEndSecondType = textView.getText().charAt(j) == '}' && textView.getText().charAt(j + 1) == '-';
                    if (tagEndFirstType || tagEndSecondType) {
                        foundTag = true;
                        break;
                    }
                }
                if (foundTag) {
                    String drawableName = textView.getText().toString().substring(i + 2, j);

                    Drawable drawable = TextureHelper.drawablesById.get(drawableName);
                    if (drawable == null) {
                        System.out.println("Call to find drawable " + drawableName);
                        int resId = context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName());
                        drawable = context.getResources().getDrawable(resId);
                        TextureHelper.drawablesById.put(drawableName, drawable);
                    }

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
        //textView.setText(ss);
    }*/

    private static HashMap<String, ImageSpan> imageSpansById = new HashMap<>();
    public static void processImageSpan(Context context, TextView textView) {
        String text = textView.getText().toString();
        SpannableString ss = new SpannableString(text);
        for (int i = 0; i < textView.getText().length() - 1; i++) {
            if ((textView.getText().charAt(i) == '<' && text.charAt(i + 1) == '{') ||
                    (textView.getText().charAt(i) == '-' && text.charAt(i + 1) == '{')) {
                boolean foundTag = false;
                int j = i;
                for (; j < textView.getText().length() - 1; j++) {
                    if ((textView.getText().charAt(j) == '}' && text.charAt(j + 1) == '>') ||
                            (textView.getText().charAt(j) == '}' && text.charAt(j + 1) == '-')) {
                        foundTag = true;
                        break;
                    }
                }
                if (foundTag) {
                    String drawableName = text.substring(i + 2, j);

                    ImageSpan span = imageSpansById.get(drawableName);
                    if (span == null) {
                        int resId = context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName());
                        //Drawable drawable = getResources().getDrawable(resId);

                        Bitmap bitmap = decodeSampledBitmapFromResource(context.getResources(), resId, 64, 64);
                        Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);

                        Rect bounds = new Rect();
                        textView.getPaint().getTextBounds(textView.getText().toString(), 0, textView.getText().length(), bounds);

                        drawable.setBounds(0, 0, bounds.height(), bounds.height());
                        span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);

                        imageSpansById.put(drawableName, span);
                    }

                    ss.setSpan(span, i, j + 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                    /*int resId = getResources().getIdentifier(drawableName, "drawable", this.getPackageName());
                    Drawable drawable = getResources().getDrawable(resId);*/

                    //textView.setText(ss);
                } else {
                    System.err.println("Could not find end tag ( }> ) to image declared");
                }
            }
        }
        textView.setText(ss);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

}


