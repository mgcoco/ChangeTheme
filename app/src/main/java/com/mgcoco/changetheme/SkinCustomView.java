package com.mgcoco.changetheme;

public class SkinCustomView {

    String name;

    String fullName;

    String[] fieldName;

    CustomViewAttributeApplyListener customViewAttributeApplyListener;

    public SkinCustomView(String name, String fullName, String[] fieldName, CustomViewAttributeApplyListener customViewAttributeApplyListener) {
        this.name = name;
        this.fullName = fullName;
        this.fieldName = fieldName;
        this.customViewAttributeApplyListener = customViewAttributeApplyListener;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String[] getFieldName() {
        return fieldName;
    }

    public void setFieldName(String[] fieldName) {
        this.fieldName = fieldName;
    }

    public CustomViewAttributeApplyListener getCustomViewAttributeApplyListener() {
        return customViewAttributeApplyListener;
    }

    public void setCustomViewAttributeApplyListener(CustomViewAttributeApplyListener customViewAttributeApplyListener) {
        this.customViewAttributeApplyListener = customViewAttributeApplyListener;
    }
}
