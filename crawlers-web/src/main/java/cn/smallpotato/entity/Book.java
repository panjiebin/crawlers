package cn.smallpotato.entity;

import cn.smallpotato.common.model.Element;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

/**
 * @author panjb
 */
public class Book implements Element {
    @TableId(type = IdType.AUTO)
    private int id;
    private String name;
    private String author;
    private String publish;
    private String number;
    private String url;

    public Book() {
    }

    public Book(String name, String author, String publish, String number, String url) {
        this.name = name;
        this.author = author;
        this.publish = publish;
        this.number = number;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublish() {
        return publish;
    }

    public void setPublish(String publish) {
        this.publish = publish;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
