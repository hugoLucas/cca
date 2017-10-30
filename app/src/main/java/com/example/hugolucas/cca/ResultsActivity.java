package com.example.hugolucas.cca;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Presents the results of the classification to the User. Allows the user to access MapFragment
 * and ExchangeFragment.
 *
 * Created by hugo on 10/25/17.
 */

public class ResultsActivity extends AppCompatActivity{

    private static final String CODE = "results_activity_code";
    private static final String VAL = "results_activity_value";
    private static final String PIC = "results_activity_picture";

    private static String mSourceCurrencyCode;
    private static String mSourceCurrencyValue;
    private static String mSourceCurrencyPicPath;
    private static String mTargetCurrency;

    @BindView(R.id.banknote_preview) ImageView mBanknoteView;
    @BindView(R.id.country_result_text_view) TextView mCountryTextView;
    @BindView(R.id.value_result_text_view) TextView mValueTextView;

    public static Intent buildIntent(Context context, String sourceCurrency,
                                     String sourceCurrencyValue, String sourceCurrencyPic){
        Intent intent = new Intent(context, ResultsActivity.class);
        intent.putExtra(CODE, sourceCurrency);
        intent.putExtra(VAL, sourceCurrencyValue);
        intent.putExtra(PIC, sourceCurrencyPic);

        return intent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent startingIntent = getIntent();
        mSourceCurrencyCode = startingIntent.getStringExtra(CODE);
        mSourceCurrencyValue = startingIntent.getStringExtra(VAL);
        mSourceCurrencyPicPath = startingIntent.getStringExtra(PIC);

        setContentView(R.layout.activity_results);
        ButterKnife.bind(this);

        mCountryTextView.setText(mSourceCurrencyCode);
        mValueTextView.setText(mSourceCurrencyValue);
        mBanknoteView.setImageBitmap(loadImageBitmap());
    }

    public Bitmap loadImageBitmap(){
        Mat image = Highgui.imread(mSourceCurrencyPicPath);
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGB);

        Bitmap bitmap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(image, bitmap);

        return bitmap;
    }

    @OnClick(R.id.map_button)
    public void startMapFragment(){

    }

    @OnClick(R.id.exchange_button)
    public void startExchangeButton(){

    }
}
