package com.esri.squadleader.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.res.AssetManager;

/**
 * A class for useful static methods that don't really belong anywhere else.
 */
public class Utilities {
    
    /**
     * Copies the specified asset to a destination directory, whether the asset is a file or a directory.<br/>
     * <br/>
     * For example, if assetName is foo and destDir is /mnt/sdcard/bar, then a copy of foo will be made
     * and will be called /mnt/sdcard/bar/foo, whether foo is a file or a directory. 
     * @param assetManager the AssetManager from which to copy the asset.
     * @param assetName the name of the asset, as a relative path from the assets directory. The asset
     *                  can be a file or a directory. If it is a directory, all contents will be copied.
     * @param destDir the destination of the files.
     * @throws IOException 
     */
    public static void copyAssetToDir(AssetManager assetManager, String assetName, String destDir) throws IOException {
        File dir = new File(destDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        String dirLastName = assetName;
        int lastIndex = dirLastName.lastIndexOf(File.separator);
        if (-1 < lastIndex) {
            dirLastName = dirLastName.substring(lastIndex + 1);
        }
        String destSubDir = destDir + File.separator + dirLastName;

        String[] assets = assetManager.list(assetName);
        if (0 == assets.length) {
            //It's a file
            copyFileAsset(assetManager, assetName, destDir);
        } else {
            //It's a directory
            for (String asset : assets) {
                copyAssetToDir(assetManager, assetName + File.separator + asset, destSubDir);
            }
        }
    }
    
    private static void copyFileAsset(AssetManager assetManager, String assetName, String destDir) throws IOException {
        String assetLastName = assetName;
        int lastIndex = assetLastName.lastIndexOf(File.separator);
        if (-1 < lastIndex) {
            assetLastName = assetLastName.substring(lastIndex + 1);
        }
        
        InputStream in = assetManager.open(assetName);
        String newFileName = destDir + File.separator + assetLastName;
        OutputStream out = new FileOutputStream(newFileName);

        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();
        in = null;
        out.flush();
        out.close();
        out = null;
    }

}
