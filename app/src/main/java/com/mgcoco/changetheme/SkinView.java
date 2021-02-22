package com.mgcoco.changetheme;

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

    public void apply(){
        for(SkinItem skinItem: skinItems) {
            if(skinItem.name.equals("background")) {
                if(skinItem.typeName.equals("color")){
                    if(SkinManager.getInstance().resourceIsNull()){
                        view.setBackgroundResource(SkinManager.getInstance().getColor(skinItem.getResId()));
                    }
                    else{
                        view.setBackgroundColor(SkinManager.getInstance().getColor(skinItem.getResId()));
                    }
                }
                else if (skinItem.getTypeName().equals("drawable") || skinItem.getTypeName().equals("mipmap")) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        view.setBackground(SkinManager.getInstance().getDrawable(skinItem.getResId()));
                    }
                    else{
                        view.setBackgroundDrawable(SkinManager.getInstance().getDrawable(skinItem.getResId()));
                    }
                }
            }
            else if(skinItem.getName().equals("src")){
                if (skinItem.getTypeName().equals("drawable") || skinItem.getTypeName().equals("mipmap")){
                    ((ImageView)view).setImageDrawable(SkinManager.getInstance().getDrawable(skinItem.getResId()));
                }
            }
            else if(skinItem.getName().equals("textColor")){
                if(skinItem.getEntryName() != null) {
                    int col = SkinManager.getInstance().getColor(skinItem.getResId());
                    ((TextView) view).setTextColor(col);
                }
            }
            else if(skinItem.getName().equals("actualImageResource")){
                if(skinItem.getEntryName() != null) {
                    try {
                        int drawable = SkinManager.getInstance().getDrawableId(skinItem.getResId());
                        Class<?> uriUtilClass = Class.forName("com.facebook.common.util.UriUtil");
                        Object uriUtil = uriUtilClass.newInstance();
                        Method getUriForQualifiedResource = uriUtilClass.getDeclaredMethod("getUriForQualifiedResource", String.class, int.class);
                        Object uri = getUriForQualifiedResource.invoke(uriUtil, SkinManager.getInstance().getPackageName(), drawable);

                        Method setActualImageResource = view.getClass().getDeclaredMethod("setImageURI", Uri.class);
                        setActualImageResource.invoke(view, uri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
