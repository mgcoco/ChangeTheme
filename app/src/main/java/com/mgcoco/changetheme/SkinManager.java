package com.mgcoco.changetheme;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;

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
        PackageManager packageManager = context.getPackageManager();
//        System.out.println(path);
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
     * @param layoutInflater
     */
    public void inflate(LayoutInflater layoutInflater){
        setLayoutInflaterFactory(layoutInflater);
        //androidx
        try {
            Class<?> LayoutInflaterCompat = Class.forName("androidx.core.view.LayoutInflaterCompat");
            Method setFactory2 = LayoutInflaterCompat.getDeclaredMethod("setFactory2", LayoutInflater.class, LayoutInflater.Factory2.class);
            setFactory2.invoke(LayoutInflaterCompat, layoutInflater, skinFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void apply(){
        skinFactory.apply();
    }

    public Resources getResources(Resources defaultResources){
        if(resourceIsNull())
            return defaultResources;
        return resources;
    }

    private void setLayoutInflaterFactory(LayoutInflater inflater){
        LayoutInflater layoutInflater = inflater;
        try {
            Field mFactorySet = LayoutInflater.class.getDeclaredField("mFactorySet");
            mFactorySet.setAccessible(true);
            mFactorySet.set(layoutInflater, false);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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

    public boolean resourceIsNull(){
        if(resources == null)
            return true;
        return false;
    }
}