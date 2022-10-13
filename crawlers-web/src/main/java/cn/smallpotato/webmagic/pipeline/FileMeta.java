package cn.smallpotato.webmagic.pipeline;

/**
 * @author panjb
 */
public class FileMeta {
    private String fileUrl;
    private String diskPath;

    public FileMeta(String fileUrl, String diskPath) {
        this.fileUrl = fileUrl;
        this.diskPath = diskPath;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getDiskPath() {
        return diskPath;
    }

    public void setDiskPath(String diskPath) {
        this.diskPath = diskPath;
    }
}
