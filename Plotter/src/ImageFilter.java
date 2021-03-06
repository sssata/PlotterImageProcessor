import java.io.File;
import javax.swing.filechooser.FileFilter;

/* ImageFilter.java is used by FileChooserDemo2.java. */
public class ImageFilter extends FileFilter {

    //Accept all directories and all gif, jpg, tiff, or png files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String name = f.getName();
        String extension = name.substring(name.lastIndexOf(".") + 1).toLowerCase();

        if (extension != null) {
            if (extension.equals("gif") ||
                extension.equals("jpeg") ||
                extension.equals("jpg") ||
                extension.equals("png") ||
                extension.equals("bmp") ||
                extension.equals("wbmp")) {
                    return true;
            } else {
                return false;
            }
        }

        return false;
    }

    //The description of this filter
    public String getDescription() {
        return "Images (\"jpg\", \"jpeg\", \"png\", \"gif\", \"bmp\", \"wbmp\")";
    }
}