package com.github.vogelb.tools.odem.model;

public class ToplevelPackage {
    public final String name;
    public final String packagePrefix;
    public final int numComponents;

    public ToplevelPackage(String aName, String aPrefix, int components) {
        name = aName;
        packagePrefix = aPrefix;
        numComponents = components;
    }

}
