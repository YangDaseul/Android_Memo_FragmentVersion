package com.example.memo_fragmentversion;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class NoteFragment extends Fragment {
    private static final int PICK_IMAGE = 1; // 갤러리 연동
    private static final int IMAGE_CAPTURE = 2; // 카메라 연동   

    DatabaseHelper dbHelper;
    SQLiteDatabase database;

    String picturePath;
    public EditText contentsText;
    TextView dateTextView;

    public ImageView imageView;

    ImageButton imageAdd, imageDelete;

    int listId;
    String type;

    MainActivity activity;

    File file;

    private EventListener eventListener;

    public interface EventListener {
        void OnShowList(Bundle bundle);
    }

    private NoteFragment() {

    }

    public static NoteFragment newInstance() {
        NoteFragment noteFragment = new NoteFragment();
        Bundle bundle = new Bundle();
        noteFragment.setArguments(bundle);
        return noteFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_note, container, false);

        activity = (MainActivity) getActivity();

        dbHelper = new DatabaseHelper(getContext());
        database = dbHelper.getWritableDatabase();

        contentsText = rootView.findViewById(R.id.contentsText);
        dateTextView = rootView.findViewById(R.id.dateTextView);

        TextView deleteBtn = rootView.findViewById(R.id.deleteBtn);
        imageAdd = rootView.findViewById(R.id.imageAdd);
        imageDelete = rootView.findViewById(R.id.imageDelete);
        ImageButton cameraIcon = rootView.findViewById(R.id.cameraIcon);

        deleteBtn.setOnClickListener(this::onClickBtn);
        imageAdd.setOnClickListener(this::onClickBtn);
        imageDelete.setOnClickListener(this::onClickBtn);
        cameraIcon.setOnClickListener(this::onClickBtn);

        imageView = rootView.findViewById(R.id.imageView);

        TextView saveBtn = rootView.findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() { // 완료 버튼 눌렀을 때
            @Override
            public void onClick(View v) {
                Bundle bundle = getArguments();
                if (bundle != null) {
                    type = bundle.getString("type");
                    listId = bundle.getInt("listId");

                    if (type.equals("insert")) { // 글 작성 버튼 눌렀을 때
                        insert();

                    } else if (type.equals("update")) { // 아이템 클릭하여 수정할 때
                        modify(listId);
                    }
                }
            }
        });

        //카메라 권한 허용 체크
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) { // 권한 허용 상태인지 체크
            //프래그먼트에서 요청할 때
            requestPermissions(new String[]{Manifest.permission.CAMERA}, IMAGE_CAPTURE);
        }


        return rootView;
    }

    public void onClickBtn(View view) {
        switch (view.getId()) {
            case R.id.deleteBtn:
                delete(listId);
                break;
            case R.id.imageAdd:
                openGallery();
                break;
            case R.id.imageDelete:
                imageDelete();
                break;
            case R.id.cameraIcon:
                takePicture();

        }
    }

    public void imageDelete() { // 추가한 이미지 삭제 시 photo 아이콘 표시
        imageView.setImageBitmap(null);

        picturePath = null;

        imageDelete.setVisibility(View.GONE);
        imageAdd.setVisibility(View.VISIBLE);

    }

    // 현재 날짜, 시간 출력
    private String getDate() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm"); // date 출력 방식

        return dateFormat.format(date);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { // 카메라 연동 권한 요청

        if (requestCode == IMAGE_CAPTURE) { // 카메라 요청
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (NoteFragment.class != null) {
                    Intent intent = new Intent(getActivity(), NoteFragment.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void takePicture() { // 카메라 어플 연동
        if (file == null) {
            file = createFile();
        }
        
        //File 객체 -> Uri 객체
        Uri uri = FileProvider.getUriForFile(getContext(), "com.example.memo.fileprovider", file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            startActivityForResult(intent, IMAGE_CAPTURE); // 사진 찍기 화면 띄우기
        }

    }

    private File createFile() {
        String fileName = "capture.jpg" + getDate(); // capture.jsp 라는 이름으로 파일 저장

        File storageDir = Environment.getExternalStorageDirectory();
        File outFile = new File(storageDir, fileName);

        return outFile;
    }

    public void openGallery() { // 갤러리 연동
        Intent intent = new Intent();
        intent.setType("image/*"); // 모든 종류의 이미지 타입
        intent.setAction(Intent.ACTION_GET_CONTENT); // 이미지 가져오기

        startActivityForResult(intent, PICK_IMAGE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 갤러리 연동
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE) {

                try {
                    ContentResolver resolver = activity.getContentResolver();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(resolver, data.getData());
                    imageView.setImageBitmap(bitmap);

                    Cursor cursor = resolver.query(data.getData(), null, null, null, null);
                    cursor.moveToNext();

                    String[] filePathColumn = {MediaStore.MediaColumns.DATA};
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    picturePath = cursor.getString(columnIndex);
                    Log.d("picture", picturePath);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if(requestCode == IMAGE_CAPTURE){
                // 이미지 파일을 Bitmap 객체로 만들기
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;
                picturePath = file.getAbsolutePath();

                Bitmap bitmap = BitmapFactory.decodeFile(picturePath, options);
                imageView.setImageBitmap(bitmap);
            }
        }
    }


    private void insert() {
        String picture = picturePath; // 파일 경로
        String contents = contentsText.getText().toString(); // 메모 내용
        String date = dateTextView.getText().toString(); // 날짜

        println("insert 호출됨");

        if (database == null) {
            println("데이터베이스를 먼저 생성");
            return;
        }

        if (picture != null || contents.length() > 0) { //입력 값이 있을 때
            database.execSQL("insert into " + DatabaseHelper.TABLE_NAME +
                    "(picture, contents, date) values(" +
                    "'" + picture + "', " +
                    "'" + contents + "', " +
                    "'" + date + "')");

            Toast.makeText(getContext(), "저장 완료", Toast.LENGTH_LONG).show();

            Bundle bundle = new Bundle();
            bundle.putString("type", "insert");
            activity.OnShowList(bundle);


        } else { // 입력 값이 없을 때
            Toast.makeText(getContext(), "내용을 입력하시오.", Toast.LENGTH_LONG).show();
        }


    }

    private void modify(int listId) {
        String modifyPicture = picturePath;
        String modifyContents = contentsText.getText().toString(); // 메모 내용
        String modifyDate = dateTextView.getText().toString(); // 날짜


        if (modifyContents != null || modifyPicture != null) { //입력 값이 있을 때
            String sql = "UPDATE " + DatabaseHelper.TABLE_NAME
                    + " SET "
                    + " picture = '" + modifyPicture + "'"
                    + " ,contents = '" + modifyContents + "'"
                    + " ,date = '" + modifyDate + "'"
                    + " WHERE "
                    + " _id = " + listId;

            database.execSQL(sql);

            Toast.makeText(getContext(), "수정 완료. ", Toast.LENGTH_LONG).show();


        } else { // 입력 값이 없을 때
            Toast.makeText(getContext(), "내용을 입력하시오.", Toast.LENGTH_LONG).show();

        }


    }


    private void delete(int listId) {
        String sql = "DELETE FROM " + DatabaseHelper.TABLE_NAME
                + " WHERE "
                + " _id = " + listId;

        database.execSQL(sql);

        Log.d("list_id", String.valueOf(listId));

        Toast.makeText(getContext(), "삭제 완료. ", Toast.LENGTH_LONG).show();


    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void println(String data) {
        Log.d("data", data);
    }
}