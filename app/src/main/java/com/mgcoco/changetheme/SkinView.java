package com.mgcoco.changetheme;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.List;

public class SkinView {

    View view;
    List<SkinItem> skinItems;

    public SkinView(View view, List<SkinItem> skinItems) {
        this.view = view;
        this.skinItems = skinItems;
    }

    public void apply() {
        SkinCustomView skinCustomView = SkinManager.getInstance().getCustomView(view);
        if(skinCustomView != null){
            for (SkinItem skinItem : skinItems) {
                skinCustomView.customViewAttributeApplyListener.apply(view, skinItem.getTypeName(), skinItem.getResId());
            }
        }
        for (SkinItem skinItem : skinItems) {
            try {
                switch (skinItem.getName()) {
                    case "background":
                        if (skinItem.getTypeName().equals("color")) {
                            if (SkinManager.getInstance().resourceIsNull()) {
                                view.setBackgroundResource(skinItem.getResId());
                            } else {
                                view.setBackgroundColor(SkinManager.getInstance().getColor(skinItem.getResId()));
                            }
                        } else if (skinItem.getTypeName().equals("drawable") || skinItem.getTypeName().equals("mipmap")) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                view.setBackground(SkinManager.getInstance().getDrawable(skinItem.getResId()));
                            } else {
                                view.setBackgroundDrawable(SkinManager.getInstance().getDrawable(skinItem.getResId()));
                            }
                        }
                        break;
                    case "src":
                        if (skinItem.getTypeName().equals("drawable") || skinItem.getTypeName().equals("mipmap")) {
                            ((ImageView) view).setImageDrawable(SkinManager.getInstance().getDrawable(skinItem.getResId()));
                        }
                        break;
                    case "textColor":
                        int col = SkinManager.getInstance().getColor(skinItem.getResId());
                        ((TextView) view).setTextColor(col);
                        break;
                    case "tabIndicator":
                        Method setSelectedTabIndicator = view.getClass().getDeclaredMethod("setSelectedTabIndicator", Drawable.class);
                        setSelectedTabIndicator.invoke(view, SkinManager.getInstance().getDrawable(skinItem.getResId()));
                        break;
                    case "tabIndicatorColor":
                        Method setSelectedTabIndicatorColor = view.getClass().getDeclaredMethod("setSelectedTabIndicatorColor", int.class);
                        setSelectedTabIndicatorColor.invoke(view, SkinManager.getInstance().getColor(skinItem.getResId()));
                        break;
                    case "actualImageResource":
                        int drawableId = SkinManager.getInstance().getDrawableId(skinItem.getResId());
                        if(drawableId != 0) {
                            Class<?> uriUtilClass = Class.forName("com.facebook.common.util.UriUtil");
                            Object uriUtil = uriUtilClass.newInstance();
                            Method getUriForQualifiedResource = uriUtilClass.getDeclaredMethod("getUriForQualifiedResource", String.class, int.class);
                            Object uri = getUriForQualifiedResource.invoke(uriUtil, SkinManager.getInstance().getPackageName(), drawableId);
                            Method setActualImageResource = view.getClass().getDeclaredMethod("setImageURI", Uri.class);
                            setActualImageResource.invoke(view, uri);
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}