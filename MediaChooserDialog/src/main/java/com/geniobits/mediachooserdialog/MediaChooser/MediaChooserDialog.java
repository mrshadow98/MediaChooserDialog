package com.geniobits.mediachooserdialog.MediaChooser;

import android.Manifest;
import android.content.Context;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.CodeBoy.MediaFacer.MediaFacer;
import com.CodeBoy.MediaFacer.PictureGet;
import com.CodeBoy.MediaFacer.VideoGet;
import com.CodeBoy.MediaFacer.mediaHolders.pictureContent;
import com.CodeBoy.MediaFacer.mediaHolders.pictureFolderContent;
import com.CodeBoy.MediaFacer.mediaHolders.videoContent;
import com.CodeBoy.MediaFacer.mediaHolders.videoFolderContent;
import com.geniobits.mediachooserdialog.BottomNav.BottomDialog;
import com.geniobits.mediachooserdialog.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

/*MediaChooserDialog mediaChooserDialog = new MediaChooserDialog(MainActivity.this, new MediaChooserDialog.MediaSelectionListener() {
        @Override
        public void onMediaSelected(Uri uri, String path) {

        }
});
mediaChooserDialog.showVideoDialog();*/
public class MediaChooserDialog {
    private final Context context;
    private MediaSelectionListener mediaSelectionListener = null;
    private final View view;
    private ArrayList<videoContent> allVideos;
    private final RecyclerView media_recyclerview;
    private BottomDialog bottomDialogSection;
    private videoRecycleAdapter videoAdapter;
    private ArrayList<pictureContent> allPhotos;
    private imageRecycleAdapter pictureAdapter;

    public MediaChooserDialog(Context context, MediaSelectionListener mediaSelectionListener){
        this.context = context;
        this.mediaSelectionListener = mediaSelectionListener;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.video_chooser, null);
        media_recyclerview = view.findViewById(R.id.video_recycler);
        media_recyclerview.hasFixedSize();
        media_recyclerview.setHasFixedSize(true);
        media_recyclerview.setItemViewCacheSize(20);
        media_recyclerview.setDrawingCacheEnabled(true);
        media_recyclerview.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        int numOfColumns = calculateNoOfColumns(context,120);
        Log.e("clicked fab","showVideoDialog"+numOfColumns);
        media_recyclerview.setLayoutManager(new GridLayoutManager(context,numOfColumns));
    }


    public void showVideoDialog() {
        Log.e("clicked fab","showVideoDialog");

        allVideos = new ArrayList<>();
        if(hasPermissions())
        setupFolderSelector();
    }

    private boolean hasPermissions() {
        final boolean[] granted = {false};
        Dexter.withContext(context)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override public void onPermissionsChecked(MultiplePermissionsReport report) {/* ... */
                        granted[0] =report.areAllPermissionsGranted();
                    }
                    @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */
                        token.continuePermissionRequest();
                    }
                }).check();
        return granted[0];
    }

    private void launch_dialog(String title) {
        bottomDialogSection = new BottomDialog.Builder(context)
                .setTitle(title)
                .autoDismiss(true)
                .setCustomView(view).show();
    }

    int calculateNoOfColumns(Context context, float columnWidthDp) { // For example columnWidthdp=180
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (screenWidthDp / columnWidthDp + 0.5);
    }

    private void setupFolderSelector(){

        Spinner folder_selector = view.findViewById(R.id.video_folder_selector);

        final ArrayList<videoFolderContent> videoFolders = new ArrayList<>();
        videoFolders.add(new videoFolderContent("all","*All*"));
        videoFolders.addAll(MediaFacer.withVideoContex(context).getVideoFolders(VideoGet.externalContentUri));

        final ArrayList<String> folders = new ArrayList<>();
        for(int i = 0;i < videoFolders.size();i++){
            folders.add(videoFolders.get(i).getFolderName());
        }

        ArrayAdapter seletorAdapter = new ArrayAdapter<String>(context,R.layout.support_simple_spinner_dropdown_item, folders);
        folder_selector.setAdapter(seletorAdapter);
        allVideos = MediaFacer
                .withVideoContex(context)
                .getAllVideoContent(VideoGet.externalContentUri);
        setuUpAndDisplayVideos();
        folder_selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(folders.get(position).equals("*All*")){
                    allVideos = MediaFacer
                            .withVideoContex(context)
                            .getAllVideoContent(VideoGet.externalContentUri);
                    if(videoAdapter!=null)
                        videoAdapter.notifyDataSet(allVideos);
                }else {
                    allVideos = MediaFacer
                            .withVideoContex(context)
                            .getAllVideoContentByBucket_id(videoFolders.get(position).getBucket_id());
                    if(videoAdapter!=null)
                    videoAdapter.notifyDataSet(allVideos);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setuUpAndDisplayVideos(){
        Log.e("clicked fab","setuUpAndDisplayVideos");
        videoRecycleAdapter.videoActionListener videoActionListener = new videoRecycleAdapter.videoActionListener() {
            @Override
            public void onVideoItemClicked(int position) {
                mediaSelectionListener.onMediaSelected(Uri.parse(allVideos.get(position).getAssetFileStringUri()), allVideos.get(position).getPath());
                dismiss();
            }

            @Override
            public void onVideoItemLongClicked(int position) {
                //show video information
            }
        };

        videoAdapter = new videoRecycleAdapter(context,allVideos,videoActionListener);
        media_recyclerview.setAdapter(videoAdapter);
        launch_dialog("Choose Video");
    }

    public void dismiss(){
        bottomDialogSection.dismiss();
    }

    public interface MediaSelectionListener {
        void onMediaSelected(Uri uri, String path);
    }

    public void showPictureDialog() {
        allPhotos = new ArrayList<>();
        if(hasPermissions())
        setUpFolderSelector();
    }

    private void setUpFolderSelector(){
        Spinner folderSelector = view.findViewById(R.id.video_folder_selector);

        final ArrayList<pictureFolderContent> pictureFolders = new ArrayList<>();
        pictureFolders.add(new pictureFolderContent("all","*All*"));
        pictureFolders.addAll(MediaFacer.withPictureContex(context).getPictureFolders());

        final ArrayList<String> folders = new ArrayList<>();
        for(int i = 0;i < pictureFolders.size();i++){
            folders.add(pictureFolders.get(i).getFolderName());
        }

        ArrayAdapter<String> seletorAdapter = new ArrayAdapter<String>(context,R.layout.support_simple_spinner_dropdown_item, folders);
        folderSelector.setAdapter(seletorAdapter);
        allPhotos = MediaFacer
                .withPictureContex(context)
                .getAllPictureContents(PictureGet.externalContentUri);
        setUpAndDisplayPictures();
        folderSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(folders.get(position).equals("*All*")){
                    allPhotos = MediaFacer
                            .withPictureContex(context)
                            .getAllPictureContents(PictureGet.externalContentUri);
                    if(pictureAdapter!=null)
                        pictureAdapter.notifyDataSet(allPhotos);
                }else {

                    allPhotos = MediaFacer
                            .withPictureContex(context)
                            .getAllPictureContentByBucket_id(pictureFolders.get(position).getBucket_id());
                    if(pictureAdapter!=null)
                        pictureAdapter.notifyDataSet(allPhotos);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    void setUpAndDisplayPictures(){
        imageRecycleAdapter.pictureActionListrener actionListener = new imageRecycleAdapter.pictureActionListrener() {
            @Override
            public void onPictureItemClicked(int position) {
                //show picture information
                mediaSelectionListener.onMediaSelected(Uri.parse(allPhotos.get(position).getAssertFileStringUri()), allPhotos.get(position).getPicturePath());
                dismiss();
            }

            @Override
            public void onPictureItemLongClicked(int position) {
                //show picture in fragment
            }
        };
        pictureAdapter = new imageRecycleAdapter(context,allPhotos,actionListener);
        media_recyclerview.setAdapter(pictureAdapter);
        launch_dialog("Choose picture");
    }
}
