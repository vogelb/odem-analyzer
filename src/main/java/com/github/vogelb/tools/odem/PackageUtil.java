package com.github.vogelb.tools.odem;

import com.github.vogelb.tools.odem.model.ToplevelPackage;

public abstract class PackageUtil {

    public static String getTopLevelPackage(String dependencyPackageName, final ToplevelPackage... tlps) {
        
        for (ToplevelPackage tlp : tlps) {
            if (dependencyPackageName.startsWith(tlp.packagePrefix)) {
                String result = tlp.name; 
                if (tlp.numComponents > 0) {
                    String remainder = dependencyPackageName.substring(tlp.packagePrefix.length());
                    if (remainder.startsWith(".")) remainder = remainder.substring(1);
                    result += ".";
                    String[] parts = remainder.split("\\.");
                    for (int i = 0; i < parts.length && i < tlp.numComponents; ++i) {
                        result += parts[i];
                    }
                }
                return result;
            }
        }
        return dependencyPackageName;
    }

}
