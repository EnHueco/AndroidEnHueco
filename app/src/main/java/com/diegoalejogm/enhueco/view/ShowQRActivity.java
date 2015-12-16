package com.diegoalejogm.enhueco.view;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.widget.ImageView;
import com.diegoalejogm.enhueco.R;
import net.glxn.qrgen.android.QRCode;
import com.diegoalejogm.enhueco.Model.MainClasses.System;


public class ShowQRActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_qr);

        String encodedUser = System.getInstance().getAppUser().getEncodedRepresentation();
        Bitmap myBitmap = QRCode.from(encodedUser).withSize(width,width).bitmap();
        ImageView myImage = (ImageView) findViewById(R.id.imageView);
        myImage.setImageBitmap(myBitmap);
    }
}
