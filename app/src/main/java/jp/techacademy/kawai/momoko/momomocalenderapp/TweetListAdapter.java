package jp.techacademy.kawai.momoko.momomocalenderapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ajd4jp.*;
/**
 * Created by momon on 2017/09/07.
 */

public class TweetListAdapter extends BaseAdapter {
    private LayoutInflater mLayoutInflater = null;
    private List<Tweet> mTweetArrayList;

    public TweetListAdapter(Context context) {
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mTweetArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mTweetArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_tweets, parent, false);
        }
        String typeStr;
        switch (mTweetArrayList.get(position).getType()) {
            case 0:
                typeStr = "(T) ";
                convertView.setBackgroundColor((Color.parseColor("#fdfdda")));
                break;
            case 1:
                typeStr = "(R) ";
                convertView.setBackgroundColor((Color.parseColor("#fddfda")));
                break;
            case 2:
                typeStr = "(F) ";
                convertView.setBackgroundColor((Color.parseColor("#f5d9ef")));
                break;
            default:
                typeStr = "";
                convertView.setBackgroundColor((Color.parseColor("#ffffff")));
                break;
        }
        TextView titleText = (TextView) convertView.findViewById(R.id.userName);
        titleText.setText(typeStr + mTweetArrayList.get(position).getUserName());

        TextView nameText = (TextView) convertView.findViewById(R.id.text);
        nameText.setText(mTweetArrayList.get(position).getText());

        Date date = mTweetArrayList.get(position).getDate();
        AJD day = new AJD(date);
        Holiday holiday = Holiday.getHoliday(day);
        String holidayStr = "";
        if (holiday != null) {
            holidayStr = holidayStr + "・祝";
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime( mTweetArrayList.get(position).getDate());
        String weekdayStr = "";
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                weekdayStr = "(日" + holidayStr + ")";
                break;
            case Calendar.MONDAY:
                weekdayStr = "(月" + holidayStr + ")";
                break;
            case Calendar.TUESDAY:
                weekdayStr = "(火" + holidayStr + ")";
                break;
            case Calendar.WEDNESDAY:
                weekdayStr = "(水" + holidayStr + ")";
                break;
            case Calendar.THURSDAY:
                weekdayStr = "(木" + holidayStr + ")";
                break;
            case Calendar.FRIDAY:
                weekdayStr = "(金" + holidayStr + ")";
                break;
            case Calendar.SATURDAY:
                weekdayStr = "(土" + holidayStr + ")";
                break;
        }

        TextView resText = (TextView) convertView.findViewById(R.id.date);
        Date resNum = mTweetArrayList.get(position).getDate();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd");
        resText.setText(String.valueOf(fmt.format(resNum)) + weekdayStr);
//
//        byte[] bytes = mTweetArrayList.get(position).getImageBytes();
//        if (bytes.length != 0) {
//            Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length).copy(Bitmap.Config.ARGB_8888, true);
//            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
//            imageView.setImageBitmap(image);
//        }

        return convertView;
    }

    public void setTweetArrayList(List<Tweet> tweetArrayList) {
        mTweetArrayList = tweetArrayList;
    }
}
