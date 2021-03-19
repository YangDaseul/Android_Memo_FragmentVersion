package com.example.memo_fragmentversion;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter implements Filterable {
    //Adapter에 추가된 데이터를 저장하기 위한 ArrayList (원본 데이터 리스트)
    ArrayList<MemoItem> items;
    // 필터링된 결과 데이터를 저장하기 위한 ArrayList. 최초에는 전체 리스트 보유
    ArrayList<MemoItem> filteredItems;

    Context context;

    Filter listFilter;

    MainActivity activity;

    public ListViewAdapter(ArrayList<MemoItem> items, Context context) {
        this.items = items;
        this.context = context;
        filteredItems = items;

    }

    //Adapter에 사용되는 데이터 개수 리턴
    @Override
    public int getCount() {
        return filteredItems.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //position에 위치한 데이터를 화면에 출력하는데 사용된 View 리턴
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        activity = new MainActivity();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }

        ImageView imageView = (ImageView)convertView.findViewById(R.id.imageView);
        TextView contentsTextView = (TextView)convertView.findViewById(R.id.contentsTextView);
        TextView dateTextView = (TextView)convertView.findViewById(R.id.dateTextView);


        //filteredItems에서 position에 위치한 데이터 참조 획득
        MemoItem memoItem = filteredItems.get(position);

        String picturePath = memoItem.getPicture(); // 사진 경로
        if(picturePath != null && !picturePath.equals("")){ // 사진 경로가 존재하면
            try{
                Log.d("pictureParth", picturePath);
                //Uri uri = Uri.fromFile(new File(picturePath));
                //ContentResolver resolver = context.getContentResolver();
                //Bitmap bitmap = MediaStore.Images.Media.getBitmap(resolver, uri);
                Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                imageView.setImageBitmap(bitmap);

            }catch (Exception e){
                e.printStackTrace();
            }


        }else{ // 사진 경로가 없다면
            imageView.setVisibility(View.GONE); // imageView 출력X
        }

        contentsTextView.setText(memoItem.getContents()); // 메모 내용 출력
        dateTextView.setText(memoItem.getDate()); // 날짜 출력

        return convertView;

    }


    @Override
    public Filter getFilter() {
        if (listFilter == null) {
            listFilter = new ListFilter();
        }
        return listFilter;
    }

    private class ListFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                results.values = items;
                results.count = items.size();
            } else {
                ArrayList<MemoItem> itemList = new ArrayList<MemoItem>();

                for (MemoItem item : items) {
                    if (item.getContents().toUpperCase().contains(constraint.toString().toUpperCase())) {

                        itemList.add(item);

                    }
                }
                results.values = itemList;
                results.count = itemList.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            filteredItems = (ArrayList<MemoItem>) results.values;

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }


        }
    }


}
