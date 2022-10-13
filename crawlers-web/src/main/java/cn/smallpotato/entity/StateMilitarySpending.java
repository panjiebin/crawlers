package cn.smallpotato.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * @author panjb
 */
@TableName("state_military_spending")
public class StateMilitarySpending {
    @TableId(type = IdType.AUTO)
    private int id;
    private String country;
    private String fileName;
    private String url;

    public StateMilitarySpending() {
    }

    public StateMilitarySpending(String country, String fileName, String url) {
        this.country = country;
        this.fileName = fileName;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
