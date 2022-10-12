package cn.smallpotato.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * @author panjb
 */
@TableName("nato_doc")
public class NatoDoc {
    @TableId(type = IdType.AUTO)
    private int id;
    private String title;
    private String date;
    private String content;
    private String url;

    public NatoDoc() {
    }

    public NatoDoc(String title, String date, String content, String url) {
        this.title = title;
        this.date = date;
        this.content = content;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
