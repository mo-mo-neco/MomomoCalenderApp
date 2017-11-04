package jp.techacademy.kawai.momoko.momomocalenderapp;

import java.io.Serializable;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by momon on 2017/09/06.
 */

public class Tweet extends RealmObject implements Serializable {
//public class Tweet implements Serializable {
    private String userName;
    private String text;
    private Date date;
    private boolean delFlg;
    private int type;

    @PrimaryKey
    private long id;

    public String getUserName() {
        return userName;
    }

    public String getText() {
        return text;
    }

    public Date getDate() {
        return date;
    }

    public long getId() {
        return id;
    }

    public boolean getDelFlg() {
        return delFlg;
    }

    public int getType() {
        return type;
    }

    public void setDelFlg(boolean delFlg) {
        this.delFlg = delFlg;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    public void init(long id, String userName, String text, Date date) {
        this.userName = userName;
        this.text = text;
        this.date = date;
        this.id = id;
        delFlg = false;
        type = 0;
    }
}
