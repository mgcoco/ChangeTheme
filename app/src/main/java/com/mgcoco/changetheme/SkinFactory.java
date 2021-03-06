package com.mgcoco.changetheme;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkinFactory implements LayoutInflater.Factory2 {

    private static final String[] PREFIX_LIST = {
            "android.widget.",
            "android.view.",
            "android.webkit."
    };

    private static final String[] NATIVE_ATTRIBUTE_NAME = {
            "textColor",
            "background",
            "src"
    };

    private static final int[] NATIVE_ATTRIBUTE_ID = {
            android.R.attr.textColor,
            android.R.attr.background,
            android.R.attr.src
    };

    private static final String[] THIRDPARTY_ATTRIBUTE_NAME = {
//            "actualImageResource",
            "tabIndicator",
            "tabIndicatorColor"
    };

    private List<SkinView> parseViewList = new ArrayList<>();

    private List<SkinView> dynamicViewList = new ArrayList<>();

    private static final String[] THIRDPARTY_VIEW = {
//            "com.facebook.drawee.view.SimpleDraweeView",
            "com.google.android.material.tabs.TabLayout"
    };

    private LayoutInflater.Factory mViewCreateFactory;
    private LayoutInflater.Factory2 mViewCreateFactory2;

    public void setInterceptFactory(LayoutInflater.Factory factory) {
        mViewCreateFactory = factory;
    }
    public void setInterceptFactory2(LayoutInflater.Factory2 factory) {
        mViewCreateFactory2 = factory;
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View view = null;
        String supportedName = getSupportedName(name);
        if(supportedName != null){
            view = onCreateView(supportedName, context, attrs);
            parserView(view, supportedName, attrs);
        }
        else if(name.startsWith("android")){
            view = onCreateView(name, context, attrs);
            parserView(view, name, attrs);
        }
        else if(name.contains(".")) {
            //with package name
            onCreateView(name, context, attrs);
        }
        else{
            //without package name
            for(String s: PREFIX_LIST) {
                String viewName = s + name;
                view = onCreateView(viewName, context, attrs);
                if(view != null){
                    break;
                }
            }
            if(view != null){
                parserView(view, name, attrs);
            }
        }
        return view;
    }

    public void apply(){
        for(SkinView view: parseViewList) {
            view.apply();
        }

        for(SkinView view: dynamicViewList) {
            view.apply();
        }
    }

    public List<SkinView> getDynamicViewList(){
        return dynamicViewList;
    }

    public SkinView getDynamicSkinView(View view){
        for(SkinView skinView: dynamicViewList) {
            if(skinView.view == view){
                return skinView;
            }
        }
        return null;
    }

    private String getSupportedName(String viewName){
        for(SkinCustomView view: SkinManager.getInstance().getCustomViewList()){
            if(viewName.equals(view.name)){
                return view.fullName;
            }
        }
        for(String name: THIRDPARTY_VIEW){
            if(viewName.contains(name)){
                return name;
            }
        }
        return null;
    }

    private void parserView(View view, String name, AttributeSet attrs) {
        if(view != null){
            List<SkinItem> skinItems = new ArrayList<>();
            for (int i = 0; i < attrs.getAttributeCount(); i++) {
                String attrName = attrs.getAttributeName(i);

                for(SkinCustomView customView: SkinManager.getInstance().getCustomViewList()) {
                    if(!customView.getFullName().equals(name)){
                        continue;
                    }
                    if(Arrays.asList(customView.getFieldName()).contains(attrName)){
                        String attrValue = attrs.getAttributeValue(i);
                        if(attrValue.startsWith("@")){
                            addSkinItem(view, attrName, Integer.parseInt(attrValue.substring(1)), skinItems);
                        }
                    }
                }
                for(String nativeAttrName: NATIVE_ATTRIBUTE_NAME) {
                    if(attrName.contains(nativeAttrName)) {
                        String attrValue = attrs.getAttributeValue(i);
                        if(attrValue.startsWith("@")){
                            addSkinItem(view, attrName, Integer.parseInt(attrValue.substring(1)), skinItems);
                        }
                    }
                }
                for(String thirdPartyAttrName: THIRDPARTY_ATTRIBUTE_NAME) {
                    if(attrName.contains(thirdPartyAttrName)) {
                        String attrValue = attrs.getAttributeValue(i);
                        if(attrValue.startsWith("@")){
                            addSkinItem(view, attrName, Integer.parseInt(attrValue.substring(1)), skinItems);
                        }
                    }
                }
                if(attrName.contains("style")){
                    try {
                        TypedArray typedArray = view.getContext().obtainStyledAttributes(attrs.getStyleAttribute(), NATIVE_ATTRIBUTE_ID);
                        if (typedArray.length() > 0){
                            for(int ti = 0; ti < typedArray.length(); ti++){
                                try{
                                    if(typedArray.hasValue(ti)){
                                        int resId = typedArray.getResourceId(ti, -1);
                                        if(resId != -1) {
                                            addSkinItem(view, NATIVE_ATTRIBUTE_NAME[ti], resId, skinItems);
                                        }
                                    }
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        typedArray.recycle();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if(skinItems.size() > 0){
                SkinView skinView = new SkinView(view, skinItems);
                parseViewList.add(skinView);
                skinView.apply();
            }
        }
    }

    private void addSkinItem(View view, String attrName, int resId, List<SkinItem> skinItems){
        String resourceTypeName = view.getResources().getResourceTypeName(resId);
        String resourceEntryName = view.getResources().getResourceEntryName(resId);
        SkinItem skinItem = new SkinItem(attrName, resourceTypeName, resourceEntryName, resId);
        skinItems.add(skinItem);
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        View view = null;
        try {
            Class<?> aClass = context.getClassLoader().loadClass(name);
            Constructor<? extends View> constructor = (Constructor<? extends View>) aClass.getConstructor(Context.class, AttributeSet.class);
            view = constructor.newInstance(context, attrs);

            if(view == null) {
                if (mViewCreateFactory2 != null) {
                    view = mViewCreateFactory2.onCreateView(name, context, attrs);
                    if (view == null) {
                        view = mViewCreateFactory2.onCreateView(null, name, context, attrs);
                    }
                } else if (mViewCreateFactory != null) {
                    view = mViewCreateFactory.onCreateView(name, context, attrs);
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return view;
    }

    public void unbindedDestroyedView(){
        new Thread(() -> {
            for(int i = 0; i < parseViewList.size(); i++) {
                SkinView view = parseViewList.get(i);
                if(!view.view.isAttachedToWindow()){
                    parseViewList.remove(view);
                }
            }

            for(int i = 0; i < dynamicViewList.size(); i++) {
                SkinView view = dynamicViewList.get(i);
                if(!view.view.isAttachedToWindow()){
                    dynamicViewList.remove(view);
                }
            }
        }).start();
    }

    public void resetApply(){
        for(SkinView view: parseViewList) {
            view.setApply(false);
        }

        for(SkinView view: dynamicViewList) {
            view.setApply(false);
        }
    }
}