package com.artyommameev.sunflowerplayer.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;

import androidx.annotation.Nullable;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Objects;

import lombok.val;

/**
 * Utility class for helping to get a full path from tree {@link Uri}.
 *
 * @author Augendiagnose
 * <a>https://github.com/jeisfeld/Augendiagnose/blob/master/AugendiagnoseIdea/augendiagnoseLib/src/main/java/de/jeisfeld/augendiagnoselib/util/imagefile/FileUtil.java</a>
 * <p>
 * Licensed under the GNU General Public License, version 2
 * <a>https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html</a>
 */
public final class TreeUriHelper {

    private static final String PRIMARY_VOLUME_NAME = "primary";

    /**
     * Get the full path of a document from its tree URI.
     *
     * @param treeUri The tree URI.
     * @param context The application context
     * @return The path (without trailing file separator).
     */
    @Nullable
    public static String getFullPathFromTreeUri(@Nullable final Uri treeUri,
                                                Context context) {
        if (treeUri == null) {
            return null;
        }

        String volumePath = getVolumePath(getVolumeIdFromTreeUri(
                treeUri), context);

        if (volumePath == null) {
            return File.separator;
        }

        if (volumePath.endsWith(File.separator)) {
            volumePath = volumePath.substring(0, volumePath.length() - 1);
        }

        String documentPath = getDocumentPathFromTreeUri(treeUri);

        if (documentPath.endsWith(File.separator))
            documentPath = documentPath.substring(0,
                    documentPath.length() - 1);

        if (documentPath.length() > 0) {
            if (documentPath.startsWith(File.separator)) {
                return volumePath + documentPath;
            } else {
                return volumePath + File.separator + documentPath;
            }
        } else return volumePath;
    }


    @SuppressWarnings("JavaReflectionMemberAccess")
    @SuppressLint("ObsoleteSdkInt")
    private static String getVolumePath(final String volumeId,
                                        Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }

        try {
            val storageManager = (StorageManager) context
                    .getSystemService(Context.STORAGE_SERVICE);
            val storageVolumeClazz = Class.forName(
                    "android.os.storage.StorageVolume");

            val getVolumeList = storageManager.getClass()
                    .getMethod("getVolumeList");
            val getUuid = storageVolumeClazz.getMethod(
                    "getUuid");
            val getPath = storageVolumeClazz.getMethod(
                    "getPath");
            val isPrimary = storageVolumeClazz
                    .getMethod("isPrimary");
            val result = getVolumeList.invoke(storageManager);

            final int length = Array.getLength(Objects.requireNonNull(result));
            for (int i = 0; i < length; i++) {
                val storageVolumeElement = Array.get(result, i);
                val uuid = (String) getUuid.invoke(storageVolumeElement);
                val primary = (Boolean) isPrimary.invoke(storageVolumeElement);

                // primary volume?
                if (Objects.requireNonNull(primary) &&
                        PRIMARY_VOLUME_NAME.equals(volumeId)) {
                    return (String) getPath.invoke(storageVolumeElement);
                }

                // other volumes?
                if (uuid != null && uuid.equals(volumeId)) {
                    return (String) getPath.invoke(storageVolumeElement);
                }
            }
            // not found.
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getVolumeIdFromTreeUri(final Uri treeUri) {
        val docId = DocumentsContract.getTreeDocumentId(treeUri);
        val split = docId.split(":");
        if (split.length > 0) return split[0];
        else return null;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getDocumentPathFromTreeUri(final Uri treeUri) {
        val docId = DocumentsContract.getTreeDocumentId(treeUri);
        val split = docId.split(":");
        if ((split.length >= 2) && (split[1] != null)) return split[1];
        else return File.separator;
    }

}


