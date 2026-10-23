package framework.mg.itu.files;

public class MapFile {
    private String filename;
    private String type;
    private String extension;
    private byte[] bytes;

    public MapFile() {
    }

    public MapFile(String filename, byte[] bytes) {
        this.filename = filename;
        this.bytes = bytes;
        if (filename != null && filename.contains(".")) {
            int lastIndex = filename.lastIndexOf('.');
            this.extension = filename.substring(lastIndex + 1);
        }
        this.type = null;
    }

    public MapFile(String filename, String type, byte[] bytes) {
        this.filename = filename;
        this.type = type;
        this.bytes = bytes;
        if (filename != null && filename.contains(".")) {
            int lastIndex = filename.lastIndexOf('.');
            this.extension = filename.substring(lastIndex + 1);
        }
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
        if (filename != null && filename.contains(".")) {
            int lastIndex = filename.lastIndexOf('.');
            this.extension = filename.substring(lastIndex + 1);
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
