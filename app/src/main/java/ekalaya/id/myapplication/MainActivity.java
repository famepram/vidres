package ekalaya.id.myapplication;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_TAKE_GALLERY_VIDEO = 100;

    Button btnImport;

    private Uri selectedImageUri;

    private FFmpeg ffmpeg;

    private String filePath;

    String TAG = "RESEARCH.EXTRACT.IMAGE";

    File DEST_DIR = Environment.getExternalStoragePublicDirectory("img_extractor");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!DEST_DIR.isDirectory()){
            DEST_DIR.mkdir();
        }

        btnImport = (Button) findViewById(R.id.btn_import);
        btnImport.setOnClickListener(this);
        loadFFMpegBinary();
    }

    private void uploadVideo() {
        try {
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
        } catch (Exception e) {

        }
    }

    @Override
    public void onClick(View view) {
        uploadVideo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            try {
                File f = new File(DEST_DIR.getAbsolutePath());
                delete(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
            selectedImageUri = data.getData();
            extractImagesVideo();
        }
    }

    private void loadFFMpegBinary() {
        try {
            if (ffmpeg == null) {
                Log.d(TAG, "ffmpeg : era nulo");
                ffmpeg = FFmpeg.getInstance(this);

                ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                    @Override
                    public void onFailure() {
                        showUnsupportedExceptionDialog();
                    }

                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "ffmpeg : correct Loaded");
                    }
                });
            }

        } catch (FFmpegNotSupportedException e) {
            showUnsupportedExceptionDialog();
        } catch (Exception e) {
            Log.d(TAG, "EXception no controlada : " + e);
        }
    }

    private void showUnsupportedExceptionDialog(){
        new AlertDialog.Builder(MainActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                    }
                })
                .create()
                .show();
    }

    //private void extractImagesVideo(int startMs, int endMs) {

    private void extractImagesVideo() {
        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
        );

        String filePrefix = "extract_picture";
        String fileExtn = ".jpg";
        String yourRealPath = getPath(MainActivity.this, selectedImageUri);
        //String yourRealPath = Uri.parse(selectedImageUri.getPath()).toString();
        //File dir = new File(moviesDir, "VideoEditor");
        int fileNo = 0;
//        while (dir.exists()) {
//            fileNo++;
//            dir = new File(moviesDir, "VideoEditor" + fileNo);
//
//        }
//        dir.mkdir();
        //filePath = dir.getAbsolutePath();
        File dest = new File(DEST_DIR, filePrefix + "%03d" + fileExtn);
        filePath = dest.getAbsolutePath();

        Log.d(TAG, "startTrim: src: " + yourRealPath);
        Log.d(TAG, "startTrim: dest: " + dest.getAbsolutePath());

        String[] complexCommand = {
                "-y",
                "-i", yourRealPath,
                "-an", "-r", "1/10",
//                "-ss", "" + startMs / 1000,
//                "-t", "" + (endMs - startMs) / 1000,
                dest.getAbsolutePath()};

        execFFmpegBinary(complexCommand);

    }

    private void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.d(TAG, "FAILED with output : " + s);
                }

                @Override
                public void onSuccess(String s) {
                    Log.d(TAG, "SUCCESS with output : " + s);
//                    if (choice == 1 || choice == 2 || choice == 5 || choice == 6 || choice == 7) {
//                        Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
//                        intent.putExtra(FILEPATH, filePath);
//                        startActivity(intent);
//                    } else if (choice == 3) {
//                        Intent intent = new Intent(MainActivity.this, PreviewImageActivity.class);
//                        intent.putExtra(FILEPATH, filePath);
//                        startActivity(intent);
//                    } else if (choice == 4) {
//                        Intent intent = new Intent(MainActivity.this, AudioPreviewActivity.class);
//                        intent.putExtra(FILEPATH, filePath);
//                        startActivity(intent);
//                    }
                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "Started command : ffmpeg " + command);
                    //progressDialog.setMessage("progress : " + s);
                    Log.d(TAG, "progress : " + s);
                }

                @Override
                public void onStart() {
                    Log.d(TAG, "Started command : ffmpeg " + command);
                    //progressDialog.setMessage("Processing...");
                    //progressDialog.show();
                }

                @Override
                public void onFinish() {
                    for(final File child : DEST_DIR.listFiles()) {
                        File filetoscan = new File(child.getAbsolutePath());
                        Intent sintent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        sintent.setData(Uri.fromFile(filetoscan));
                        getApplicationContext().sendBroadcast(sintent);
                    }
//                    Log.d(TAG, "Finished command : ffmpeg " + filePath);
//                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
                    //progressDialog.dismiss();
//                    File filetoscan = new File(DEST_DIR.getAbsolutePath());
//                    Intent sintent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                    sintent.setData(Uri.fromFile(filetoscan));
//                    getApplicationContext().sendBroadcast(sintent);

                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
    }

        @TargetApi(Build.VERSION_CODES.KITKAT)
    private String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private String getDataColumn(Context context, Uri uri, String selection,
                                 String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        } else if (f.getAbsolutePath().endsWith("FIR")) {
            if (!f.delete()) {
                new FileNotFoundException("Failed to delete file: " + f);
            }
        }
    }
}
