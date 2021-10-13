package com.neokii.ntune;

public class Feature {

    public static boolean FEATURE_UNIVERSAL = true;

    public static boolean is_tici() {
        return SettingUtil.getBoolean(MyApp.getContext(), "is_tici", false);
    }
}
