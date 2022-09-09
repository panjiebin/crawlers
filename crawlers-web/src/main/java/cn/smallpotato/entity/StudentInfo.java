package cn.smallpotato.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

/**
 * @author panjb
 */
public class StudentInfo {

    @TableId(type = IdType.AUTO)
    private int id;
    private String name;
    private String phone;
    private String className;

    public StudentInfo() {
    }

    public StudentInfo(String name, String phone, String className) {
        this.name = name;
        this.phone = phone;
        this.className = className;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
