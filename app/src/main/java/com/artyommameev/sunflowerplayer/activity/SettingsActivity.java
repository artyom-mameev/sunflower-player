package com.artyommameev.sunflowerplayer.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.artyommameev.sunflowerplayer.BuildConfig;
import com.artyommameev.sunflowerplayer.R;
import com.artyommameev.sunflowerplayer.database.Database;
import com.artyommameev.sunflowerplayer.domain.Tag;
import com.artyommameev.sunflowerplayer.util.TreeUriHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

import lombok.Cleanup;
import lombok.val;

/**
 * The activity to change the application settings.
 *
 * @author Artyom Mameev
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Toolbar toolbar = findViewById(R.id.toolbarSettings);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * The {@link Fragment} for working with {@link Preference} objects, which
     * make up the settings menu.
     *
     * @author Artyom Mameev
     */
    public static class SettingsFragment extends PreferenceFragmentCompat {

        private final int REQUEST_DEFAULT_DIRECTORY = 9999;
        private final int REQUEST_LOAD_BACKUP = 9998;

        private Database database;
        private Preference defaultDirectoryPreference;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState,
                                        String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            database = new Database(requireActivity());

            defaultDirectoryPreference =
                    findPreference(getString(R.string.default_dir));

            setUpCreateBackupPreference(
                    findPreference(getString(R.string.create_backup)));
            setUpLoadBackupPreference(
                    findPreference(getString(R.string.load_backup)));
            setUpClearDatabasePreference(
                    findPreference(getString(R.string.clear_database)));
            setUpDefaultDirectoryPreference(defaultDirectoryPreference);
            setUpResetDefaultDirectoryPreference(
                    findPreference(getString(R.string.reset_default_dir)));
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode,
                                     Intent data) {
            Uri uri = null;

            if (data != null) {
                uri = data.getData();
            }

            switch (requestCode) {
                case REQUEST_LOAD_BACKUP:
                    showLoadBackupDialog(uri);
                    break;

                case REQUEST_DEFAULT_DIRECTORY:
                    changeDefaultDirectory(uri);
                    break;
            }
        }

        private void setUpResetDefaultDirectoryPreference(
                Preference resetDefaultDirectoryPreference) {
            if (resetDefaultDirectoryPreference == null) {
                throw new RuntimeException("resetDefaultDirectoryPreference " +
                        "cannot be null!");
            }

            resetDefaultDirectoryPreference.setOnPreferenceClickListener(
                    preference -> {
                        showResetDefaultDirDialog();

                        return false;
                    });

        }

        private void setUpDefaultDirectoryPreference(
                Preference defaultDirectoryPreference) {
            if (defaultDirectoryPreference == null) {
                throw new RuntimeException("defaultDirectoryPreference " +
                        "cannot be null!");
            }

            if (Objects.equals(defaultDirectoryPreference.getSummary(),
                    getString(R.string.default_string))) {
                val sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(
                                requireActivity());

                val defaultDirKey = getString(R.string.default_dir_key);

                val downloadsPath =
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS)
                                .getPath();

                defaultDirectoryPreference.setSummary(
                        sharedPreferences.getString(defaultDirKey,
                                downloadsPath));
            }

            defaultDirectoryPreference.setOnPreferenceClickListener(
                    preference -> {
                        showDefaultDirectoryChooser();

                        return false;
                    });

        }

        private void setUpClearDatabasePreference(
                Preference clearDatabasePreference) {
            if (clearDatabasePreference == null) {
                throw new RuntimeException("clearDatabasePreference " +
                        "cannot be null!");
            }

            clearDatabasePreference.setOnPreferenceClickListener(
                    preference -> {
                        showClearDatabaseDialog();

                        return true;
                    });
        }

        private void setUpLoadBackupPreference(Preference loadBackupPreference) {
            if (loadBackupPreference == null) {
                throw new RuntimeException("loadBackupPreference " +
                        "cannot be null!");
            }

            loadBackupPreference.setOnPreferenceClickListener(preference -> {
                showLoadBackupChooser();

                return true;
            });
        }

        private void setUpCreateBackupPreference(
                Preference createBackupPreference) {
            if (createBackupPreference == null) {
                throw new RuntimeException("createBackupPreference " +
                        "cannot be null!");
            }

            createBackupPreference.setOnPreferenceClickListener(
                    preference -> {
                        val backupJson = new Gson().toJson(
                                database.findAll());

                        val backupFile = createTempBackupFile(backupJson);

                        shareBackup(backupFile);

                        return true;
                    });
        }

        private void showResetDefaultDirDialog() {
            buildSimpleAlertDialog(getString(R.string.reset_default_dir),
                    getString(R.string.are_you_sure), true)
                    .setPositiveButton("OK", (dialogInterface, i) -> {
                        resetDefaultDirectory();

                        buildSimpleAlertDialog(
                                getString(R.string.reset_default_dir),
                                getString(R.string.default_dir_was_reset),
                                false)
                                .show();
                    })
                    .show();
        }

        private void showClearDatabaseDialog() {
            buildSimpleAlertDialog(getString(R.string.clear_database),
                    getString(R.string.are_you_sure), true)
                    .setPositiveButton("OK", (dialog, which) -> {
                        database.deleteAllTags();

                        showDatabaseClearedDialog();
                    })
                    .show();
        }

        private void showDatabaseClearedDialog() {
            buildSimpleAlertDialog(getString(R.string.clear_database),
                    getString(R.string.database_cleared), false)
                    .setPositiveButton("OK", (dialog1, which1) ->
                            reloadMainActivity())
                    .show();
        }

        private void showLoadBackupDialog(Uri uri) {
            if (uri == null) {
                return;
            }

            buildSimpleAlertDialog(getString(R.string.load_backup),
                    getString(R.string.are_you_sure), true)
                    .setPositiveButton("OK", (dialog, which) ->
                            loadBackup(uri))
                    .show();
        }

        private void showBackupLoadedDialog() {
            buildSimpleAlertDialog(getString(R.string.load_backup),
                    getString(R.string.backup_loaded), false)
                    .setPositiveButton("OK", (dialog1, which1) ->
                            reloadMainActivity())
                    .show();
        }

        private void showLoadBackupChooser() {
            val intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");

            startActivityForResult(Intent.createChooser(intent,
                    getString(R.string.choose_file)), REQUEST_LOAD_BACKUP);
        }

        private void showDefaultDirectoryChooser() {
            val intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

            intent.addCategory(Intent.CATEGORY_DEFAULT);

            startActivityForResult(Intent.createChooser(intent,
                    getString(R.string.choose_directory)), REQUEST_DEFAULT_DIRECTORY);
        }

        private void reloadMainActivity() {
            val intent = new Intent(getActivity(), MainActivity.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
        }

        private File createTempBackupFile(String backupJson) {
            val tempFolder = new File(requireActivity().getFilesDir(),
                    "temp");

            if (!tempFolder.exists()) {
                @SuppressWarnings("unused")
                val ignored = tempFolder.mkdir();
            }

            try {
                val file = new File(tempFolder, "tags-backup.json");

                @Cleanup
                val writer = new BufferedWriter(new FileWriter(file));

                writer.write(backupJson);

                return file;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void shareBackup(File backupFile) {
            val shareIntent = new Intent(Intent.ACTION_SEND);

            shareIntent.setType("text/json");

            val fileUri = FileProvider.getUriForFile(requireActivity(),
                    BuildConfig.APPLICATION_ID + ".provider", backupFile);

            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent,
                    getString(R.string.share_via)));
        }

        private void loadBackup(Uri uri) {
            try {
                @Cleanup
                val inputStream = requireActivity()
                        .getContentResolver().openInputStream(uri);

                @Cleanup
                val bufferedReader = new BufferedReader(new InputStreamReader(
                        inputStream));

                val tagListType = new TypeToken<List<Tag>>() {
                }.getType();

                List<Tag> tags = new Gson().fromJson(bufferedReader, tagListType);

                database.saveAll(tags);

                showBackupLoadedDialog();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void changeDefaultDirectory(Uri uri) {
            if (uri == null) {
                throw new RuntimeException("uri cannot be null!");
            }

            val defaultDirectoryDocUri = DocumentsContract
                    .buildDocumentUriUsingTree(uri,
                            DocumentsContract.getTreeDocumentId(uri));

            val defaultDirectory = new File(Objects.requireNonNull(
                    TreeUriHelper.getFullPathFromTreeUri(defaultDirectoryDocUri,
                            getActivity())));

            defaultDirectoryPreference.setSummary(
                    defaultDirectory.getAbsolutePath());

            val sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(requireActivity());

            val editor = sharedPreferences.edit();

            editor.putString(getString(R.string.default_dir_key),
                    defaultDirectory.getAbsolutePath());
            editor.apply();

            reloadMainActivity();
        }

        private void resetDefaultDirectory() {
            defaultDirectoryPreference.setSummary(
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS)
                            .getPath());

            val sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(requireActivity());

            val editor = sharedPreferences.edit();

            editor.putString(getString(R.string.default_dir_key),
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS)
                            .getPath());
            editor.apply();

            reloadMainActivity();
        }

        private AlertDialog.Builder buildSimpleAlertDialog(String title,
                                                           String message,
                                                           boolean cancelable) {
            val alertDialogBuilder =
                    new AlertDialog.Builder(requireActivity())
                            .setTitle(title)
                            .setMessage(message)
                            .setPositiveButton("OK", (dialog, which) ->
                                    dialog.dismiss());

            if (cancelable) {
                alertDialogBuilder.setCancelable(true)
                        .setNegativeButton(getString(R.string.cancel),
                                (dialog, which) -> dialog.dismiss());
            }

            return alertDialogBuilder;
        }
    }
}