package com.android.victory.schedule.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.victory.schedule.R;
import com.android.victory.schedule.data.Client;
import com.android.victory.schedule.data.CloudDatabase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    public SQLiteDatabase db;
    public Cursor res;
    public ArrayList<Client> clientList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        clientList = new ArrayList<>();
        try {
            final CloudDatabase cloudDatabase = new CloudDatabase(MapsActivity.this);
            db = cloudDatabase.getWritableDatabase();
            Cursor cursor = db.rawQuery("select * from " + CloudDatabase.TABLE_CLIENT_NAME,null);
            if (cursor.moveToFirst()){
                while(!cursor.isAfterLast()){
                    Client client = new Client();
                    client.setRow_id(cursor.getString(cursor.getColumnIndex(CloudDatabase.CLIENT_ROW_ID)));
                    client.setClient_code(cursor.getString(cursor.getColumnIndex(CloudDatabase.CLIENT_CODE)));
                    client.setClient_name(cursor.getString(cursor.getColumnIndex(CloudDatabase.CLIENT_NAME)));
                    client.setAddr(cursor.getString(cursor.getColumnIndex(CloudDatabase.CLIENT_ADDRESS)));
                    client.setPhone(cursor.getString(cursor.getColumnIndex(CloudDatabase.CLIENT_PHONE)));
                    client.setHP(cursor.getString(cursor.getColumnIndex(CloudDatabase.CLIENT_HP)));
                    client.setGeo_latt(cursor.getString(cursor.getColumnIndex(CloudDatabase.CLIENT_GEO_LATT)));
                    client.setGeo_long(cursor.getString(cursor.getColumnIndex(CloudDatabase.CLIENT_GEO_LONG)));
                    client.setKet(cursor.getString(cursor.getColumnIndex(CloudDatabase.CLIENT_KET)));
                    client.setUpdate_user(cursor.getString(cursor.getColumnIndex(CloudDatabase.CLIENT_UPDATE_USER)));
                    client.setUpdate_date(cursor.getString(cursor.getColumnIndex(CloudDatabase.CLIENT_UPDATE_DATE)));
                    clientList.add(client);
                    cursor.moveToNext();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney, Australia, and move the camera.
        if (clientList.size() > 0){
            for (int i = 0 ; i < clientList.size() ; i++){
                LatLng point = new LatLng(Double.parseDouble(clientList.get(i).getGeo_latt()), Double.parseDouble(clientList.get(i).getGeo_long()));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
                mMap.addMarker(new MarkerOptions().position(point)
                        .title(clientList.get(i).getAddr()));
                final int count = i;
                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        View markerView = ((LayoutInflater) MapsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_layout, null);
                        TextView mapClientName = (TextView)markerView.findViewById(R.id.map_client_name);
                        ImageView mapSyncStateImage = (ImageView)markerView.findViewById(R.id.sync_map_state_imageview);
                        TextView updateTimeTextView = (TextView)markerView.findViewById(R.id.current_map_time_textview);
                        mapClientName.setText(clientList.get(count).getClient_name());
                        updateTimeTextView.setText(clientList.get(count).getUpdate_date());
                        return markerView;
                    }
                });
            }
        }
    }

    // Convert a view to bitmap
    private Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, 107, 117);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    public static View createMarkerView(Context context,String clientName, String updateTimeString) {
        View markerView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_layout, null);
        TextView mapClientName = (TextView)markerView.findViewById(R.id.map_client_name);
        ImageView mapSyncStateImage = (ImageView)markerView.findViewById(R.id.sync_map_state_imageview);
        TextView updateTimeTextView = (TextView)markerView.findViewById(R.id.current_map_time_textview);
        mapClientName.setText(clientName);
        updateTimeTextView.setText(updateTimeString);
        return markerView;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(MapsActivity.this, MainActivity.class));
        finish();
    }
}
