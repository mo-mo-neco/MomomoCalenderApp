package jp.techacademy.kawai.momoko.momomocalenderapp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;

import org.joda.time.DateTime;

import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterListener;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;


public class MainActivity extends AppCompatActivity {
    private final String PREF_FILE_NAME = "twitter_test";
    private final String PREF_TOKEN = "token";
    private final String PREF_SECRET = "secret";

    private AsyncTwitter mTwitter;
    private RequestToken mReqToken;
    private Handler mHandler;
    private ListView mListView;
    private ArrayList<Tweet> mTweetArrayList;
    private TweetListAdapter mAdapter;
    private int mPosition;
    private Realm mRealm;

    private final TwitterListener mListener = new TwitterAdapter() {
        @Override
        public void gotOAuthRequestToken(RequestToken token) {
            mReqToken = token;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mReqToken.getAuthorizationURL()));
            startActivity(intent);
        }

        @Override
        public void gotOAuthAccessToken(AccessToken token) {
            SharedPreferences pref = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(PREF_TOKEN, token.getToken());
            editor.putString(PREF_SECRET, token.getTokenSecret());
            editor.commit();
            mTwitter.setOAuthAccessToken(new AccessToken(token.getToken(), token.getTokenSecret()));
        }

        @Override
        public void gotFavorites(ResponseList<Status> statuses) {
            String log = "";
            mTweetArrayList.clear();
            for (Status status : statuses) {
                User user = status.getUser();
                Date date = getDateFromText(status.getText(), status.getCreatedAt());
                if (date != null){
                    Tweet tweet = new Tweet();
                    tweet.init(status.getId(), user.getName() + "@" + user.getScreenName() , status.getText() + "\n", date);
                    tweet.setType(2);
                    mTweetArrayList.add(tweet);
                }
            }
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mTwitter.getUserTimeline();
                }

            });
        }
        @Override
        public void gotUserTimeline(ResponseList<Status> statuses)  {
            for (Status status : statuses) {
                int type = 0;
                if (status.isRetweet()){
                    status = status.getRetweetedStatus();
                    type = 1;
                }
                User user = status.getUser();
                Date date = getDateFromText(status.getText(), status.getCreatedAt());
                if (date != null){
                    Tweet tweet = new Tweet();
                    tweet.init(status.getId(), user.getName() + "@" + user.getScreenName() , status.getText() + "\n", date);
                    tweet.setType(type);
                    mTweetArrayList.add(tweet);
                }
            }
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mAdapter.setTweetArrayList(mTweetArrayList);
                    //mAdapter.notifyDataSetChanged();
                    mRealm = Realm.getDefaultInstance();
                    if (mRealm != null){
                        for (Tweet tweet : mTweetArrayList)
                        {
                            RealmResults<Tweet> results = mRealm.where(Tweet.class).equalTo("id", tweet.getId()).findAll();
                            if (results.isEmpty()){
                                mRealm.beginTransaction();
                                mRealm.copyToRealmOrUpdate(tweet);
                                mRealm.commitTransaction();
                            }
                        }
                        mRealm.close();
                    }
                    reloadListView();
                }

            });
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new TweetListAdapter(this);
        mTweetArrayList = new ArrayList<Tweet>();
        mAdapter.setTweetArrayList(mTweetArrayList);
        mListView.setAdapter(mAdapter);
        mHandler = new Handler();

        // Realmの設定
        mRealm = Realm.getDefaultInstance();

        // Twitterの設定
        mTwitter = new AsyncTwitterFactory().getInstance();
        mTwitter.addListener(mListener);
        mTwitter.setOAuthConsumer(getResources().getString(R.string.API_KEY), getResources().getString(R.string.API_SECRET));

        AccessToken token = getAccessToken();
        if (token == null) {
            mTwitter.getOAuthRequestTokenAsync("momo://momomo.twitter.com");
        } else {
            mTwitter.setOAuthAccessToken(token);
        }

        //ListViewをTapした時の処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPosition = position;
                showDatePickerDialog();
            }
        });

        // ListViewを長押ししたときの処理
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Tweet tweet = (Tweet) parent.getAdapter().getItem(position);

                // ダイアログを表示する
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("削除");
                builder.setMessage("このアイテムを削除しますか");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mRealm.beginTransaction();
                        RealmResults<Tweet> results = mRealm.where(Tweet.class).equalTo("id", tweet.getId()).findAll();
                        for (Tweet tweet : results) {
                            tweet.setDelFlg(true);
                            mRealm.copyToRealmOrUpdate(tweet);
                        }
                        mRealm.commitTransaction();
                        reloadListView();
                    }
                });
                builder.setNegativeButton("CANCEL", null);

                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();

        AccessToken token = getAccessToken();
        if (token != null) {
            mTwitter.getFavorites();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mRealm != null){
            mRealm.close();
        }
    }

    public AccessToken getAccessToken() {
        SharedPreferences pref = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        String token = pref.getString(PREF_TOKEN, null);
        String secret = pref.getString(PREF_SECRET, null);

        if (token != null && secret != null) {
            return new AccessToken(token, secret);
        } else {
            return null;
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        //ブラウザからのコールバックで呼ばれる
        final Uri uri = intent.getData();
        if (uri != null){
            final String verifier = uri.getQueryParameter("oauth_verifier");
            if (verifier != null) {
                mTwitter.getOAuthAccessTokenAsync(mReqToken, verifier);
                mTwitter.getFavorites();
            }
        }
    }


    private Date getDateFromText(String iText, Date tweetDate){
        String text = Normalizer.normalize(iText, Normalizer.Form.NFKC);
        Date date = getDate1(text, tweetDate);
        if (date == null){
            date = getDate2(text, tweetDate);
        }
        return date;
    }

    private Date getDate1(String iText, Date tweetDate){
        String regex ="[01]?[0-9]/[0123]?[0-9]";

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(iText);
        if( m.find()){
            SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy");
            String strDate = String.valueOf(sdFormat.format(tweetDate)) + "/" + m.group();
            sdFormat = new SimpleDateFormat("yyyy/MM/dd");
            try {
                Date date = sdFormat.parse(strDate);
                return date;
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }else{
            return null;
        }
    }

    private Date getDate2(String iText, Date tweetDate){
        String regex ="[01]?[0-9]月[0123]?[0-9]日";

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(iText);
        if( m.find()){
            SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy");
            String strDate = String.valueOf(sdFormat.format(tweetDate)) + "年" + m.group();
            sdFormat = new SimpleDateFormat("yyyy年M月d日");
            try {
                Date date = sdFormat.parse(strDate);
                return date;
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }else{
            return null;
        }
    }

    private void reloadListView() {
        // Realmデータベースから削除済みでないデータを取得
        RealmResults<Tweet> tweetRealmResults = mRealm.where(Tweet.class).equalTo("delFlg", false).findAll();
        // 新しい日時順に並べ替え
        List<Tweet> tweetRealmShow = tweetRealmResults.sort("date", Sort.DESCENDING);
        mTweetArrayList.clear();
        for (Tweet tweet : tweetRealmShow) {
            mTweetArrayList.add(tweet);
        }
        mAdapter.setTweetArrayList(mTweetArrayList);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    //日付変更Dialog表示
    private void showDatePickerDialog() {
        Tweet tweet = mTweetArrayList.get(mPosition);
        DateTime dateTime = new DateTime(tweet.getDate());
        int oldYear = dateTime.getYear();
        int oldMonth = dateTime.getMonthOfYear()-1;//なぜか-1が必要・・・DateTimeとDatePickerDialogの仕様違いか？
        int oldDay = dateTime.getDayOfMonth();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Tweet tweet = mTweetArrayList.get(mPosition);
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);
                        Date date = calendar.getTime();
                        mRealm.beginTransaction();
                        tweet.setDate(date);
                        mRealm.copyToRealmOrUpdate(tweet);
                        mRealm.commitTransaction();
                        mAdapter.setTweetArrayList(mTweetArrayList);
                        mAdapter.notifyDataSetChanged();
                    }
                },
                oldYear,
                oldMonth,
                oldDay);
        datePickerDialog.show();
    }
}
