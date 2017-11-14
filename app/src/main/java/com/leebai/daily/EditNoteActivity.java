package com.leebai.daily;

//use github richEditText to meet the edit of text and image
//there is problem to use android EditText with SpannableString when delete the picture, so discard

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.leebai.daily.database.DatabaseHelper;
import com.leebai.daily.database.NotesProvider;
import com.leebai.daily.utils.CommonUtils;
import com.leebai.daily.utils.ImageUtils;
import com.leebai.daily.utils.NoteInfo;
import com.leebai.daily.utils.ScreenUtils;
import com.leebai.daily.utils.StringUtils;
import com.leebai.daily.xrichtext.RichTextEditor;
import com.leebai.daily.xrichtext.SDCardUtil;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.PhotoPreview;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class EditNoteActivity extends Activity implements View.OnClickListener {

    private ImageButton mDone;
    private ImageButton mBack;
    private ImageButton mHandWrite;
    private ImageButton mPaint;
    private ImageButton mRecord;
    private ImageButton mPhotos;
    private ImageButton mCamera;
    private ImageButton mAttachment;
    private EditText mTitleET;


    private RichTextEditor mRichTextEditor;
    private RichTextEditor.onImageClickListener mImageClickListener;
    private Subscription subsLoading;
    private Subscription subsInsert;

    private NoteInfo mNoteInfo;

    private final int NEW_NOTE = 0;
    private final int EDIT_NOTE = 1;

    private final int REQUEST_GET_PHOTOS = 1;
    private final int REQUEST_TAKE_PICTURE = 2;

    //isEdit or isNew
    private boolean isEdit;

    private String mTitle;
    private String mOriginalText;
    private long mId;

    //for camera
    private String mCameraPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_note);

        mRichTextEditor = findViewById(R.id.content_edit);


        mTitleET = findViewById(R.id.title);
        mDone = findViewById(R.id.done);
        mBack = findViewById(R.id.back);
        mHandWrite = findViewById(R.id.handwrite);
        mPaint = findViewById(R.id.paint);
        mRecord = findViewById(R.id.record);
        mPhotos = findViewById(R.id.photos);
        mCamera = findViewById(R.id.camera);
        mAttachment = findViewById(R.id.attachment);

        mDone.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mHandWrite.setOnClickListener(this);
        mPaint.setOnClickListener(this);
        mRecord.setOnClickListener(this);
        mPhotos.setOnClickListener(this);
        mCamera.setOnClickListener(this);
        mAttachment.setOnClickListener(this);

        mNoteInfo = new NoteInfo();

        isEdit = getIntent().getFlags() == EDIT_NOTE;
        if (isEdit) {
            mId = getIntent().getLongExtra("id", 0);
            mOriginalText = getIntent().getStringExtra("original_text");
            mTitle = getIntent().getStringExtra("title");
            mTitleET.setText(mTitle);

            mRichTextEditor.post(new Runnable() {
                @Override
                public void run() {
                    mRichTextEditor.clearAllLayout();
                    showDataSync(mOriginalText);
                }
            });
        }

        mImageClickListener = new RichTextEditor.onImageClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                startPhotoPreview(view, position);
            }

            @Override
            public void onItemLongClicked(View view, int position) {

            }
        };
        mRichTextEditor.setOnImageClickListener(mImageClickListener);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.handwrite:
                break;
            case R.id.paint:
                break;
            case R.id.photos:
                startPickPhoto();
                break;
            case R.id.camera:
                startTakePhoto();
                break;
            case R.id.record:
                break;
            case R.id.attachment:
                break;
            case R.id.done:
                String originalText = getEditData();
                if (TextUtils.isEmpty(originalText)) {
                    Toast.makeText(this, "content is empty,,,", Toast.LENGTH_LONG);
                    return;
                }
                saveContent(originalText);
                finish();
                break;
            case R.id.back:
                finish();
                break;
            default:
                break;
        }
    }

    //RichText

    /**
     * 异步方式显示数据
     *
     * @param html
     */
    private void showDataSync(final String html) {
        // loadingDialog.show();

        subsLoading = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                showEditData(subscriber, html);
            }
        })
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())//生产事件在io
                .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        // loadingDialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        // loadingDialog.dismiss();
                        Toast.makeText(EditNoteActivity.this, "解析错误：图片不存在或已损坏", Toast.LENGTH_SHORT);
                    }

                    @Override
                    public void onNext(String text) {
                        if (text.contains(SDCardUtil.getPictureDir())) {
                            mRichTextEditor.addImageViewAtIndex(mRichTextEditor.getLastIndex(), text);
                        } else {
                            mRichTextEditor.addEditTextAtIndex(mRichTextEditor.getLastIndex(), text);
                        }
                    }
                });
    }

    //RichText

    /**
     * 显示数据
     */
    protected void showEditData(Subscriber<? super String> subscriber, String html) {
        try {
            List<String> textList = StringUtils.cutStringByImgTag(html);
            for (int i = 0; i < textList.size(); i++) {
                String text = textList.get(i);
                if (text.contains("<img")) {
                    String imagePath = StringUtils.getImgSrc(text);
                    if (new File(imagePath).exists()) {
                        subscriber.onNext(imagePath);
                    } else {
                        Toast.makeText(EditNoteActivity.this, "图片" + i + "已丢失，请重新插入！", Toast.LENGTH_SHORT);
                    }
                } else {
                    subscriber.onNext(text);
                }

            }
            subscriber.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
            subscriber.onError(e);
        }
    }

    //RichText


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_GET_PHOTOS:
                    Uri photosUri = intent.getData();
                    String imagePath = CommonUtils.getRealFilePath(EditNoteActivity.this, photosUri);
                    insertImagesSync(imagePath);
                    break;

                case REQUEST_TAKE_PICTURE:
//                    Bitmap bitmap1 = (Bitmap) intent.getExtras().get("data");
                    insertImagesSync(mCameraPhotoPath);
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * 异步方式插入图片
     *
     * @param path
     */
    private void insertImagesSync(final String path) {
        // insertDialog.show();

        subsInsert = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    mRichTextEditor.measure(0, 0);
                    int width = ScreenUtils.getScreenWidth(EditNoteActivity.this);
                    int height = ScreenUtils.getScreenHeight(EditNoteActivity.this);

                    Bitmap bitmap = ImageUtils.getSmallBitmap(path, width, height);//压缩图片
                    // String imagePath = path;

                    //ArrayList<String> photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                    //可以同时插入多张图片
                    // for (String imagePath : photos) {


                    //bitmap = BitmapFactory.decodeFile(imagePath);
                    String imagePath = SDCardUtil.saveToSdCard(bitmap);
                    Log.i("bai", "###imagePath=" + imagePath);
                    subscriber.onNext(imagePath);
                    // }
                    subscriber.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        })
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())//生产事件在io
                .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        //insertDialog.dismiss();
                       // mRichTextEditor.addEditTextAtIndex(mRichTextEditor.getLastIndex(), " ");
                        Toast.makeText(EditNoteActivity.this, "图片插入成功", Toast.LENGTH_SHORT);
                    }

                    @Override
                    public void onError(Throwable e) {
                        // insertDialog.dismiss();
                        Toast.makeText(EditNoteActivity.this, "图片插入失败:" + e.getMessage(), Toast.LENGTH_SHORT);
                    }

                    @Override
                    public void onNext(String imagePath) {
                        Log.d("baill", "1 = " + mRichTextEditor.getMeasuredWidth());
                        Log.d("baill", "imagePath = " + imagePath);
                        mRichTextEditor.insertImage(imagePath, mRichTextEditor.getMeasuredWidth());
                    }
                });
    }

    private void saveContent(String originalText) {
        if (isEdit && originalText.equals(mOriginalText) && mTitleET.getText().toString().equals(mTitle)) {
            return;
        }


        setNoteInfo(originalText);
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.TITLE, mNoteInfo.getTitle());
        cv.put("content", mNoteInfo.getContent());
        cv.put(DatabaseHelper.ORIGINAL_TEXT, mNoteInfo.getOriginalText());
        cv.put(DatabaseHelper.TIME_MODIFIED, mNoteInfo.getModifiedTime());
        cv.put(DatabaseHelper.DISPLAY_TEXT, mNoteInfo.getDisplayText());
        if (isEdit) {
            getContentResolver().update(NotesProvider.CONTENT_URI, cv, "_id = ?", new String[]{String.valueOf(mId)});
        } else {
            getContentResolver().insert(NotesProvider.CONTENT_URI, cv);
        }
    }

    private void setNoteInfo(String originalText) {
        mNoteInfo.setOriginalText(originalText);
        mNoteInfo.setTitle(mTitleET.getText().toString().trim().equals("") ? "no title" : mTitleET.getText().toString());
        mNoteInfo.setContent(originalText);//to delete or modify, is the same as originaltext
        Log.d("baill", "ori str = " + originalText);
        String displayText = originalText.replaceAll("<img src=\".*?\"/>", "[image]");
        Log.d("baill", "displayText = " + displayText);
        mNoteInfo.setDisplayText(displayText);

        Long time_modified = System.currentTimeMillis();
        mNoteInfo.setModifiedTime(time_modified);
    }


    /**
     * 负责处理编辑数据提交等事宜，请自行实现
     */
    private String getEditData() {
        List<RichTextEditor.EditData> editList = mRichTextEditor.buildEditData();
        StringBuffer textOriginal = new StringBuffer();
        for (RichTextEditor.EditData itemData : editList) {
            if (itemData.inputStr != null) {
                textOriginal.append(itemData.inputStr);
                //Log.d("RichEditor", "commit inputStr=" + itemData.inputStr);
            } else if (itemData.imagePath != null) {
                textOriginal.append("<img src=\"").append(itemData.imagePath).append("\"/>");
                //Log.d("RichEditor", "commit imgePath=" + itemData.imagePath);
                //imageList.add(itemData.imagePath);
            }
        }

        return textOriginal.toString();
    }

    private void startPhotoPreview(View view, int position) {
        int index = 0;
        List<RichTextEditor.EditData> dataList = mRichTextEditor.buildEditData();
        ArrayList<String> imagePaths = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).imagePath != null) {
                imagePaths.add(dataList.get(i).imagePath);
                if (i == position) {
                    index = imagePaths.size();
                }
            }
        }
        PhotoPreview.builder()
                .setPhotos(imagePaths)
                .setCurrentItem(index)
                .setShowDeleteButton(false)
                .start(EditNoteActivity.this);
    }

    private void startPickPhoto() {
        Intent pickPhoto = new Intent(Intent.ACTION_GET_CONTENT);
        pickPhoto.addCategory(Intent.CATEGORY_OPENABLE);
        pickPhoto.setType("image/*");
        startActivityForResult(pickPhoto, REQUEST_GET_PHOTOS);
    }

    private void startTakePhoto() {
        Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mCameraPhotoPath = SDCardUtil.getCameraPhotoDir() + System.currentTimeMillis() + ".jpg";
        File file = new File(mCameraPhotoPath);
        if (Build.VERSION.SDK_INT < 24) {
            takePhoto.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        } else {
            Uri uri = FileProvider.getUriForFile(EditNoteActivity.this, getPackageName() + ".provider", file);
            grantUriPermission(getPackageName(), uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            takePhoto.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            takePhoto.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        startActivityForResult(takePhoto, REQUEST_TAKE_PICTURE);
    }

    private String getFirstLineContent(TextView textView) {
        Layout layout = textView.getLayout();
        StringBuilder stringBuilder = new StringBuilder(textView.getText().toString());
        String content = stringBuilder.subSequence(layout.getLineStart(0), layout.getLineEnd(0)).toString();
        return content;
    }
}
