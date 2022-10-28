package com.artyommameev.sunflowerplayer.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.artyommameev.sunflowerplayer.R;
import com.artyommameev.sunflowerplayer.adapter.FileAdapter;
import com.artyommameev.sunflowerplayer.comparator.FileNameComparator;
import com.artyommameev.sunflowerplayer.database.Database;
import com.artyommameev.sunflowerplayer.domain.Tag;
import com.artyommameev.sunflowerplayer.domain.VideoClip;
import com.artyommameev.sunflowerplayer.storage.FileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.val;

/**
 * A main activity of the application, which represents a file manager in
 * which the user can select a {@link VideoClip} to view or edit
 * {@link Tag} information.
 *
 * @author Artyom Mameev
 */
public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int REQUEST_READ_PERMISSION = 9999;

    private static final int EDIT_TAGS_ID = 0;
    private static final int ALBUM_SAME_AS_TITLE_ID = 1;
    private static final int FIND_ALBUM_ID = 2;

    private static final String LIST_STATE = "listState";

    private ListView listView;
    private Parcelable listState = null;

    private List<File> currentFiles;
    private File currentFile;

    private FileAdapter fileAdapter;

    private Database database;
    private FileManager fileManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        listView = findViewById(R.id.list);

        setSupportActionBar(toolbar);

        currentFiles = new ArrayList<>();

        database = new Database(this);

        fileManager = new FileManager(database);

        checkReadPermission();
    }

    @Override
    public void onBackPressed() {
        if (!fileManager.isParentDirectoryExists()) {
            return;
        }

        fileManager.toParentDirectory();

        updateList();

        listView.setSelectionAfterHeaderView(); // scroll to top
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_settings) {
            startSettingsActivity();

            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_READ_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                setUpList();
            } else {
                showPermissionDialog();
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle state) {
        super.onRestoreInstanceState(state);

        //restore the list scroll position
        listState = state.getParcelable(LIST_STATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (listState != null) {
            listView.onRestoreInstanceState(listState);
        }

        listState = null;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);

        listState = listView.onSaveInstanceState();

        //save the list scroll position
        state.putParcelable(LIST_STATE, listState);
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view,
                                    ContextMenu.ContextMenuInfo contextMenuInfo) {
        createListContextMenu(contextMenu, view,
                (AdapterView.AdapterContextMenuInfo) contextMenuInfo);
    }

    private void createListContextMenu(ContextMenu contextMenu, View view,
                                       AdapterView.AdapterContextMenuInfo
                                               contextMenuInfo) {
        if (view.getId() != R.id.list) {
            return;
        }

        int contextPosition = contextMenuInfo.position;

        boolean isDirectory = currentFiles.get(contextPosition).isDirectory();
        boolean isNotVideoClip = !(currentFiles.get(contextPosition)
                instanceof VideoClip);

        if (isDirectory || isNotVideoClip) {
            return;
        }

        contextMenu.add(Menu.NONE, EDIT_TAGS_ID, 0,
                getString(R.string.edit_tags));
        contextMenu.add(Menu.NONE, ALBUM_SAME_AS_TITLE_ID, 1,
                getString(R.string.album_same_as_the_title));
        contextMenu.add(Menu.NONE, FIND_ALBUM_ID, 2,
                getString(R.string.find_album));
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        val adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo)
                menuItem.getMenuInfo();

        val position = adapterContextMenuInfo.position;

        val videoClip = (VideoClip) currentFiles.get(position);

        switch (menuItem.getItemId()) {
            case EDIT_TAGS_ID:
                showEditTagsDialog(videoClip);

                return true;

            case ALBUM_SAME_AS_TITLE_ID:
                showSetAlbumSameAsTitleDialog(videoClip);

                return true;

            case FIND_ALBUM_ID:
                val albumsArray = database.findAlbumsByArtist(
                        videoClip.getArtist());

                showFindAlbumDialog(videoClip, albumsArray);

                return true;

            default:
                return super.onContextItemSelected(menuItem);
        }
    }

    private void updateList() {
        currentFiles.clear();

        currentFiles.addAll(fileManager.getFiles(new FileNameComparator()));

        fileAdapter.notifyDataSetChanged();
    }

    private void checkReadPermission() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            setUpList();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READ_PERMISSION);
        }
    }

    private void setUpList() {
        val sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        val defaultDirKey = getString(R.string.default_dir_key);

        val downloadsPath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).getPath();

        /*if no default directory is specified, 'downloads' become
        the default directory*/
        val defaultDirectory = Objects.requireNonNull(
                sharedPreferences.getString(defaultDirKey, downloadsPath));

        fileManager.toDirectory(new File(defaultDirectory));

        currentFiles = fileManager.getFiles(new FileNameComparator());

        fileAdapter = new FileAdapter(currentFiles, this);

        listView.setAdapter(fileAdapter);
        listView.setOnItemClickListener(this::onListViewItemClick);

        registerForContextMenu(listView);
    }

    private void onListViewItemClick(AdapterView<?> arg0, View arg1,
                                     int position, long arg3) {
        currentFile = currentFiles.get(position);

        if (currentFile.isDirectory()) {
            fileManager.toDirectory(currentFile);

            updateList();
        }

        if (currentFile instanceof VideoClip) {
            startPlayerActivity();
        }
    }

    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.need_read_permission)
                .setMessage(R.string.grant_permission_q)
                .setCancelable(true)
                .setPositiveButton("OK", (dialogInterface, i) ->
                        checkReadPermission())
                .setNegativeButton(R.string.cancel, (dialogInterface, i) ->
                        dialogInterface.dismiss())
                .show();
    }

    private void showEditTagsDialog(VideoClip videoClip) {
        val layoutInflater = LayoutInflater.from(MainActivity.this);

        val prompt = layoutInflater.inflate(R.layout.edit_tags_dialog,
                null);

        final EditText artistEditText = prompt.findViewById(R.id.artist_tag);
        final EditText albumEditText = prompt.findViewById(R.id.album_tag);
        final EditText titleEditText = prompt.findViewById(R.id.title_tag);
        final CheckBox sameAsTitleCheckBox = prompt.findViewById(
                R.id.sameAsTitleCheckBox);

        titleEditText.setText(videoClip.getTitle());
        artistEditText.setText(videoClip.getArtist());
        albumEditText.setText(videoClip.getAlbum().isPresent() ?
                videoClip.getAlbum().get() : "");

        sameAsTitleCheckBox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (isChecked) {
                        albumEditText.setText(titleEditText.getText());
                    } else {
                        albumEditText.setText(videoClip.getAlbum().isPresent() ?
                                videoClip.getAlbum().get() : "");
                    }
                });

        new AlertDialog.Builder(this)
                .setView(prompt)
                .setCancelable(true)
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    val newArtist = artistEditText.getText().toString()
                            .trim();
                    val newTitle = titleEditText.getText().toString()
                            .trim();
                    val newAlbum = albumEditText.getText().toString()
                            .trim();

                    updateTag(videoClip, newArtist, newTitle, newAlbum);
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) ->
                        dialogInterface.cancel())
                .show();
    }

    private void showFindAlbumDialog(VideoClip videoClip, String[] albumsArray) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.find_album)
                .setItems(albumsArray, (dialog, which) -> {
                    val album = albumsArray[which];

                    updateTag(videoClip, videoClip.getArtist(),
                            videoClip.getTitle(), album);

                    dialog.dismiss();
                })
                .show();
    }

    private void showSetAlbumSameAsTitleDialog(VideoClip videoClip) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.album_same_as_the_title)
                .setMessage(R.string.are_you_sure)
                .setCancelable(true)
                .setPositiveButton("OK", (dialogInterface, i) ->
                        updateTag(videoClip, videoClip.getArtist(),
                                videoClip.getTitle(), videoClip.getTitle()))
                .setNegativeButton(R.string.cancel, (dialogInterface, i) ->
                        dialogInterface.dismiss())
                .show();
    }

    private void updateTag(VideoClip videoClip, String newArtist,
                           String newTitle, String newAlbum) {
        if (newArtist.isEmpty() || newAlbum.isEmpty() || newTitle.isEmpty()) {
            Toast.makeText(MainActivity.this,
                    getString(R.string.some_field_is_empty),
                    Toast.LENGTH_LONG)
                    .show();

            return;
        }

        Tag tag = database.findTagByFileName(videoClip.getFileName());

        if (tag == null) { // create a new tag
            tag = new Tag(videoClip.getFileName(), newArtist, newTitle,
                    newAlbum);

            database.insertTag(tag);
        } else { // update tag
            if (tag.getArtist().equals(newArtist) &&
                    tag.getTitle().equals(newTitle) &&
                    tag.getAlbum().equals(newAlbum)) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.nothing_to_change),
                        Toast.LENGTH_LONG)
                        .show();

                return;
            }

            tag.setArtist(newArtist);
            tag.setTitle(newTitle);
            tag.setAlbum(newAlbum);

            database.updateTag(tag);
        }

        updateTagInList(tag);

        fileAdapter.notifyDataSetChanged();
    }

    private void updateTagInList(Tag tag) {
        for (val file : currentFiles) {
            if (file instanceof VideoClip) {
                val videoClip = (VideoClip) file;

                if (videoClip.getFileName().equals(tag.getFileName())) {
                    videoClip.setArtist(tag.getArtist());
                    videoClip.setTitle(tag.getTitle());
                    videoClip.setAlbum(tag.getAlbum());
                }
            }
        }
    }

    private void startPlayerActivity() {
        val playerIntent = new Intent(getBaseContext(),
                PlayerActivity.class);

        playerIntent.putExtra(getString(R.string.clip_key), currentFile);

        startActivity(playerIntent);
    }

    private void startSettingsActivity() {
        val settingsIntent = new Intent(getBaseContext(),
                SettingsActivity.class);

        startActivity(settingsIntent);
    }
}
