package com.example.memo_fragmentversion;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NoteFragment.EventListener {
    ViewPager viewPager;

    ArrayList<MemoItem> items = new ArrayList<MemoItem>();

    MemoListFragment memoListFragment;
    NoteFragment noteFragment;

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


    @Override
    public void OnShowList(Bundle bundle) { // 리스트뷰 출력
        Bundle mBundle = bundle;
        String type = mBundle.getString("type");

        if(type.equals("insert")){ // 작성 후 완료 버튼 클릭 시 리스트뷰 출력
            viewPager.setCurrentItem(0); // memoListFragment
            memoListFragment.listData();

        }else if(type.equals("modify") || type.equals("delete")){
            viewPager.setCurrentItem(1); // noteFragment

            int listId = mBundle.getInt("listId");
            String listPicture = mBundle.getString("listPicture");
            String listContents = mBundle.getString("listContents");

            noteFragment.contentsText.setText(listContents);
            noteFragment.imageView.setImageBitmap(BitmapFactory.decodeFile(listPicture));

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