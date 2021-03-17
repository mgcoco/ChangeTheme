package com.mgcoco.changetheme;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.view.LayoutInflaterCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SkinManager {

    private static SkinManager sSkinManager;

    private Context context;

    private Resources resources;

    private String packageName;

    private SkinFactory skinFactory = new SkinFactory();

    private List<SkinCustomView> customViewList = new ArrayList<>();

    public static synchronized SkinManager getInstance() {
        synchronized (SkinManager.class) {
            if (sSkinManager == null) {
                sSkinManager = new SkinManager();
            }
        }
        return sSkinManager;
    }

    /**
     * initial context
     * @param context
     */
    public void init(Context context){
        this.context = context;
    }


    public void loadSkinApk(String path) {
        skinFactory.resetApply();
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageArchiveInfo = packageManager.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if(packageArchiveInfo != null) {
            packageName = packageArchiveInfo.packageName;
            try {
                AssetManager assetManager = AssetManager.class.newInstance();
                Method addAssetPath = assetManager.getClass().getDeclaredMethod("addAssetPath", String.class);
                addAssetPath.invoke(assetManager, path);
                resources = new Resources(assetManager, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Must called before setContentView
     * @param inflater
     */
    public void inflate(LayoutInflater inflater){
        Class<LayoutInflaterCompat> compatClass = LayoutInflaterCompat.class;
        Class<LayoutInflater> inflaterClass = LayoutInflater.class;
        try {
            Field sCheckedField = compatClass.getDeclaredField("sCheckedField");
            sCheckedField.setAccessible(true);
            sCheckedField.setBoolean(inflater, false);
            Field mFactory = inflaterClass.getDeclaredField("mFactory");
            mFactory.setAccessible(true);
            Field mFactory2 = inflaterClass.getDeclaredField("mFactory2");
            mFactory2.setAccessible(true);
            if (inflater.getFactory2() != null) {
                skinFactory.setInterceptFactory2(inflater.getFactory2());
            } else if (inflater.getFactory() != null) {
                skinFactory.setInterceptFactory(inflater.getFactory());
            }
            mFactory2.set(inflater, skinFactory);
            mFactory.set(inflater, skinFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Must called before setContentView
     * @param layoutInflater
     */
    public void inflate(LayoutInflater layoutInflater, Lifecycle lifecycle){
        inflate(layoutInflater);
        lifecycle.addObserver((LifecycleEventObserver) (source, event) -> {
            if(event == Lifecycle.Event.ON_RESUME){
                apply();
            }
            else if(event == Lifecycle.Event.ON_DESTROY) {
                skinFactory.unbindedDestroyedView();
            }
        });
    }

    public void apply(){
        skinFactory.apply();
    }

    public Resources getResources(Resources defaultResources){
        if(resourceIsNull())
            return defaultResources;
        return resources;
    }

    public String getPackageName(){
        return packageName;
    }

    public void addCustomView(SkinCustomView skinView){
        customViewList.add(skinView);
    }

    public List<SkinCustomView> getCustomViewList(){
        return customViewList;
    }

    public SkinCustomView getCustomView(View customView){
        for (SkinCustomView skinCustomView: getCustomViewList()){
            if(customView.getClass().getName().equals(skinCustomView.name) || customView.getClass().getSimpleName().equals(skinCustomView.name)){
                return skinCustomView;
            }
        }
        return null;
    }

    public int getDrawableId(int resId) {
        if(resourceIsNull()){
            return 0;
        }
        return getIdentifier(resId);
    }

    public Drawable getDrawable(int resId){
        if(resourceIsNull()){
            return context.getDrawable(resId);
        }
        int identifier = getIdentifier(resId);
        if(identifier == 0){
            return context.getDrawable(resId);
        }
        try {
            return resources.getDrawable(identifier);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return context.getDrawable(resId);
    }

    public int getColor(int resId) {
        if(resourceIsNull()){
            return context.getResources().getColor(resId);
        }
        int identifier = getIdentifier(resId);
        if (identifier == 0)
            return context.getResources().getColor(resId);
        return resources.getColor(identifier);
    }

    private int getIdentifier(int resId){
        String resourceTypeName = context.getResources().getResourceTypeName(resId);
        String resourceEntryName = context.getResources().getResourceEntryName(resId);
        return resources.getIdentifier(resourceEntryName, resourceTypeName, packageName);
    }

    public void putDynamicGroup(View view, String attribute, String typeName, int resId){
        SkinView skinView = skinFactory.getDynamicSkinView(view);
        if(skinView != null) {
            for(SkinItem skinItem: skinView.skinItems){
                if(skinItem.name.equals(attribute)){
                    return;
                }
            }
            skinView.skinItems.add(new SkinItem(attribute, typeName, null, resId));
        }
        else {
            List<SkinItem> itemList = new ArrayList<>();
            itemList.add(new SkinItem(attribute, typeName, null, resId));
            skinView = new SkinView(view, itemList);
            skinFactory.getDynamicViewList().add(skinView);
        }
        skinView.apply();
    }

    public boolean resourceIsNull(){
        if(resources == null)
            return true;
        return false;
    }

    public void clearTheme(){
        resources = null;
        skinFactory.resetApply();
        apply();
    }
}