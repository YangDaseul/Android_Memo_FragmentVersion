package com.example.memo_fragmentversion;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NoteFragment.EventListener {
    ViewPager viewPager;

    ArrayList<MemoItem> items = new ArrayList<MemoItem>();

    MemoListFragment memoListFragment;
    NoteFragment noteFragment;

    public static Context context;
    String[] permission_list = {
             Manifest.permission.READ_EXTERNAL_STORAGE
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.mainPager);
        viewPager.setOffscreenPageLimit(2); // 페이지 개수

        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());

        memoListFragment = new MemoListFragment();
        adapter.addItem(memoListFragment);

        noteFragment = NoteFragment.newInstance();
        noteFragment.setEventListener(MainActivity.this);
        adapter.addItem(noteFragment);

        viewPager.setAdapter(adapter);

        context = this;
        
        //권한 확인
        checkPermission();

        // 하단 탭
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.tab1_menuList:
                        //getSupportFragmentManager().beginTransaction().replace(R.id.mainPager, memoListFragment).commit();
                        viewPager.setCurrentItem(0); // viewPage와 연결
                        return true;

                    case R.id.tab2_menuNote:
                        //getSupportFragmentManager().beginTransaction().replace(R.id.mainPager, noteFragment).commit();
                        viewPager.setCurrentItem(1);

                        String type = "insert";
                        Bundle bundle = new Bundle();
                        bundle.putString("type", "insert");
                        noteFragment.setArguments(bundle);
                        Log.d("type", type);
                        return true;
                }
                return false;
            }
        });
    }

    //권한 확인
    public void checkPermission(){
        //현재 안드로이드 버전이 6.0미만이면 메서드를 종료한다.
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;

        for(String permission : permission_list){
            //권한 허용 여부를 확인한다.
            int chk = checkCallingOrSelfPermission(permission);

            if(chk == PackageManager.PERMISSION_DENIED){
                //권한 허용을여부를 확인하는 창을 띄운다
                requestPermissions(permission_list,0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==0)
        {
            for(int i=0; i<grantResults.length; i++)
            {
                //허용됬다면
                if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                }
                else {
                    Toast.makeText(getApplicationContext(),"앱권한설정하세요",Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }





    @Override
    public void OnShowList(Bundle bundle) { // 리스트뷰 출력
        Bundle mBundle = bundle;
        String type = mBundle.getString("type");

        if(type.equals("insert")){ // 작성 후 완료 버튼 클릭 시 리스트뷰 출력
            viewPager.setCurrentItem(0); // memoListFragment
            memoListFragment.listData();

        }else if(type.equals("update")){ // 수정, 삭제 시
            viewPager.setCurrentItem(1); // noteFragment

            int listId = mBundle.getInt("listId");
            String listPicture = mBundle.getString("listPicture");
            String listContents = mBundle.getString("listContents");

            try{
                ContentResolver resolver = getContentResolver();
                Uri uri = Uri.fromFile(new File(listPicture));
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(resolver, uri);
                noteFragment.imageView.setImageBitmap(bitmap);

            }catch (Exception e){
                e.printStackTrace();
            }
            noteFragment.contentsText.setText(listContents);
            //noteFragment.imageView.setImageBitmap(BitmapFactory.decodeFile(listPicture));
            Log.d("listPicture", listPicture);

            mBundle.putString("listId", String.valueOf(listId));
            NoteFragment.newInstance();


        }

    }


    class MyPagerAdapter extends FragmentStatePagerAdapter {
        ArrayList<Fragment> itemsFragment = new ArrayList<Fragment>();

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public void addItem(Fragment item) {
            itemsFragment.add(item);
        }

        @Override
        public Fragment getItem(int position) {
            return itemsFragment.get(position);
        }

        @Override
        public int getCount() {
            return itemsFragment.size();
        }
    }
}