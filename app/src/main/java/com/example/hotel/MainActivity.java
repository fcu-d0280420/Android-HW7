package com.example.hotel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private PetArrayAdapter adapter = null;
    private static final int LIST_HOTELS = 1;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LIST_HOTELS: {
                    List<Hotel> hotels = (List<Hotel>)msg.obj;
                    refreshPetList(hotels);
                    break;
                }
            }
        }
    };

    private void refreshPetList(List<Hotel> hotels) {
        adapter.clear();
        adapter.addAll(hotels);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView lvHotel = (ListView)findViewById(R.id.listview_hotel);

        adapter = new PetArrayAdapter(this, new ArrayList<Hotel>());
        lvHotel.setAdapter(adapter);

        getPetsFromFirebase();
    }

    private void getPetsFromFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                new FirebaseThread(dataSnapshot).start();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v("Hotel", databaseError.getMessage());
            }
        });
    }

    class FirebaseThread extends Thread {
        private DataSnapshot dataSnapshot;
        public FirebaseThread(DataSnapshot dataSnapshot) {
            this.dataSnapshot = dataSnapshot;
        }
        @Override
        public void run() {
            List<Hotel> lsHotel = new ArrayList<>();
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                DataSnapshot dsName = ds.child("Name");
                DataSnapshot dsAdd = ds.child("Add");
                DataSnapshot dsServiceinfo = ds.child("Serviceinfo");
                DataSnapshot dsTel = ds.child("Tel");

                String name = (String)dsName.getValue();
                String add = (String)dsAdd.getValue();
                String serviceinfo = (String)dsServiceinfo.getValue();
                String tel = (String)dsTel.getValue();

                DataSnapshot dsImg = ds.child("Picture1");
                String imgUrl = (String) dsImg.getValue();

                Bitmap hotelImg = getImgBitmap(imgUrl);

                Hotel aHotel = new Hotel();

                aHotel.setName(name);
                aHotel.setAdd(add);
                aHotel.setServiceinfo(serviceinfo);
                aHotel.setTel(tel);

                aHotel.setImgUrl(hotelImg);
                lsHotel.add(aHotel);

                Log.v("Hotel", name + ";" + add + ";" + serviceinfo + ";" + tel +"\n" + imgUrl );
            }
            Message msg = new Message();
            msg.what = LIST_HOTELS;
            msg.obj = lsHotel;
            handler.sendMessage(msg);
        }
    }

    private Bitmap getImgBitmap(String imgUrl) {
        try {
            URL url = new URL(imgUrl);
            Bitmap bm = BitmapFactory.decodeStream(url.openConnection()
                    .getInputStream());
            return bm;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    class PetArrayAdapter extends ArrayAdapter<Hotel> {
        Context context;
        public PetArrayAdapter(Context context, List<Hotel> items) {
            super(context, 0, items);
            this.context = context;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            LinearLayout itemlayout = null;
            if (convertView == null) {
                itemlayout = (LinearLayout) inflater.inflate(R.layout.hotle_item, null);
            } else {
                itemlayout = (LinearLayout) convertView;
            }
            Hotel item = (Hotel) getItem(position);
            TextView tvName = (TextView) itemlayout.findViewById(R.id.tv_name);
            tvName.setText(item.getName());
            TextView tvAdd = (TextView) itemlayout.findViewById(R.id.tv_add);
            tvAdd.setText(item.getAdd());
            TextView tvServiceinfo = (TextView) itemlayout.findViewById(R.id.tv_serviceinfo);
            tvServiceinfo.setText(item.getServiceinfo());
            TextView tvTel = (TextView) itemlayout.findViewById(R.id.tv_tel);
            tvTel.setText(item.getTel());
            ImageView ivPet = (ImageView) itemlayout.findViewById(R.id.iv_hotel);
            ivPet.setImageBitmap(item.getImgUrl());
            return itemlayout;
        }
    }
}
