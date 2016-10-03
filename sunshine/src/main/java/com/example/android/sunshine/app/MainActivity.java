package com.example.android.sunshine.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;

public class MainActivity extends Activity implements DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private TextView mDateView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private ImageView mWeatherIcon;

    private static final String DATA_TODAY = "/dataToday";
    public final static String WEARABLE_HIGH_TEMP = "highTemp";
    public final static String WEARABLE_LOW_TEMP = "lowTemp";
    public final static String WEARABLE_IMAGE_RES = "imageRes";
    public final static String WEARABLE_DATE = "dateToday";
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDateView = (TextView) findViewById(R.id.today_date);
        mHighTempView = (TextView) findViewById(R.id.temp_high);
        mLowTempView = (TextView) findViewById(R.id.temp_low);
        mWeatherIcon = (ImageView) findViewById(R.id.weather_icon);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
//                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo(DATA_TODAY) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    updateView(dataMap.getString(WEARABLE_DATE),
                            dataMap.getString(WEARABLE_HIGH_TEMP),
                            dataMap.getString(WEARABLE_LOW_TEMP),
                            dataMap.getAsset(WEARABLE_IMAGE_RES));
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }

    }

    public void updateView(String date, String maxTemp, String minTemp, Asset asset){
        Log.v(LOG_TAG, "views updated");
        mDateView.setText(date);
        mHighTempView.setText(maxTemp);
        mLowTempView.setText(minTemp);
        InputStream inputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient,asset).await().getInputStream();

        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        mWeatherIcon.setImageBitmap(bitmap);

        MyWatchFace.mDate = date;
        MyWatchFace.mMaxTemp = maxTemp;
        MyWatchFace.mMinTemp = minTemp;
        MyWatchFace.mWeatherIcon = Bitmap.createScaledBitmap(bitmap,
                (int)getResources().getDimension(R.dimen.icon_width),
                (int)getResources().getDimension(R.dimen.icon_height),
                false);
    }
}
