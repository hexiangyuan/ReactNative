package com.daigou.selfstation;

import com.facebook.react.LazyReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.ModuleSpec;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.module.model.ReactModuleInfoProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

/**
 * Creator:HeXiangYuan
 * Date  : 17-1-16
 */

public class MyReactPackage extends LazyReactPackage {
    @Override
    public List<ModuleSpec> getNativeModules(final ReactApplicationContext reactContext) {
        return Arrays.asList(new ModuleSpec(StartActivityModule.class, new Provider<NativeModule>() {
            @Override
            public NativeModule get() {
                return new StartActivityModule(reactContext);
            }
        }));
    }

    @Override
    public ReactModuleInfoProvider getReactModuleInfoProvider() {
        return LazyReactPackage.getReactModuleInfoProviderViaReflection(this);
    }

    @Override
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }
}
