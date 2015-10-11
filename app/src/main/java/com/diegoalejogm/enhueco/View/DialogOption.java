package com.diegoalejogm.enhueco.View;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.diegoalejogm.enhueco.R;

import java.util.List;

/**
 * Created by Diego on 10/10/15.
 */
public class DialogOption
{
    Image optionImage;
    String optionMessage;


    public DialogOption(String message, Image image)
    {
        this.optionImage = image;
        this.optionMessage = message;
    }

    public Image getOptionImage()
    {
        return optionImage;
    }

    public String getOptionMessage()
    {
        return optionMessage;
    }

    static class DialogOptionArrayAdapter extends ArrayAdapter<DialogOption>
    {
        Context context;
        List<DialogOption> objects;
        public DialogOptionArrayAdapter(Context context, int resource, List<DialogOption> objects)
        {
            super(context, resource, objects);
            this.context = context; this.objects = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            DialogOption option = objects.get(position);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.item_dialog_option_image, null);
            TextView tv1 = (TextView) view.findViewById(R.id.dialogTextView);
            tv1.setText(option.getOptionMessage());

            return view;
        }
    }
}
