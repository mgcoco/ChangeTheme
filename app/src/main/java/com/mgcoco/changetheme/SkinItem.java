package com.mgcoco.changetheme;

public class SkinItem {
    String name;
    String typeName;
    String entryName;
    int resId;

    public SkinItem(String name, String typeName, String entryName, int resId) {
        this.name = name;
        this.typeName = typeName;
        this.entryName = entryName;
        this.resId = resId;
    }

    public String getName() {
        return name;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getEntryName() {
        return entryName;
    }

    public int getResId() {
        return resId;
    }
}
