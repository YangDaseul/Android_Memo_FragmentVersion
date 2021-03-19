package com.example.memo_fragmentversion;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class MemoListFragment extends Fragment {
    public static final int REQUEST_CODE_MENU1 = 1; // 작성 페이지로 이동하여 작성 데이터 가져오기
    public static final int REQUEST_CODE_MENU2 = 2; // 데이터 삭제, 수정

    DatabaseHelper dbHelper;
    SQLiteDatabase database;

    ListViewAdapter adapter;

    MainActivity activity;



    ArrayList<MemoItem> items = new ArrayList<>();

    public interface EventListener {
        void OnShowList(Bundle bundle);
    }

    public static MemoListFragment newInstance() {
        MemoListFragment memoListFragment = new MemoListFragment();
        Bundle bundle = new Bundle();
        memoListFragment.setArguments(bundle);
        return memoListFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_memo_list, container, false);

        //DB 연결
        dbHelper = new DatabaseHelper(getContext());
        database = dbHelper.getWritableDatabase();

        adapter = new ListViewAdapter(items, getContext()); // adapter 생성
        ListView listView = rootView.findViewById(R.id.listView); // listView 생성
        listView.setAdapter(adapter); // adapter 지정

        activity = (MainActivity)getActivity();

        listData();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // 수정, 삭제

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MemoItem memoItem = items.get(position);

                // 해당 아이템 정보 넘기기
                Bundle bundle = new Bundle();
                int listId = memoItem.get_id();
                String listPicture = memoItem.getPicture();
                String listContents = memoItem.getContents();
                String type = "update";

                bundle.putString("listId", String.valueOf(listId));
                bundle.putString("listPicture", listPicture);
                bundle.putString("listContents", listContents);
                bundle.putString("type", type);
                activity.OnShowList(bundle);

            }
        });



        // 조회기능
        EditText editTextFilter = rootView.findViewById(R.id.editTextFilter);
        editTextFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable edit) {

                String filterText = edit.toString();
                if (filterText.length() > 0) {
                    listView.setFilterText(filterText);
                } else {
                    listView.clearTextFilter();
                }
            }
        });



        return rootView;
    }



    public void listData() { // 리스트 출력
        String sql = "SELECT * FROM " + DatabaseHelper.TABLE_NAME;
        Cursor cursor = database.rawQuery(sql, null);

        items.clear(); // 리스트 비우기

        int count = cursor.getCount();

        for (int i = 0; i < count; i++) {
            cursor.moveToNext();

            int _id = cursor.getInt(0);
            String picture = cursor.getString(1);
            String contents = cursor.getString(2);
            String date = cursor.getString(3);

            items.add(new MemoItem(_id, picture, contents, date));
        }
        adapter.notifyDataSetChanged();

    }

}