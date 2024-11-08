package net.aopacloud.superbi.common.core.utils.file;

import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Objects;

/**
 * Image process utils
 */
public class ImageUtils {
    private static final Logger log = LoggerFactory.getLogger(ImageUtils.class);

    /**
     * get image byte array
     *
     * @param imagePath image path
     * @return image byte array
     */
    public static byte[] getImage(String imagePath) {
        InputStream is = getFile(imagePath);
        try {
            return IOUtils.toByteArray(is);
        } catch (Exception e) {
            log.error("image load error {}", e);
            return null;
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * get file stream
     *
     * @param imagePath image path
     * @return file stream
     */
    public static InputStream getFile(String imagePath) {
        try {
            byte[] result = readFile(imagePath);
            if(Objects.nonNull(result)) {
                result = Arrays.copyOf(result, result.length);
                return new ByteArrayInputStream(result);
            }
        } catch (Exception e) {
            log.error("get file stream error {}", e);
        }
        return null;
    }

    /**
     * read file byte array data
     *
     * @param url
     * @return byte array
     */
    public static byte[] readFile(String url) {
        InputStream in = null;
        try {
            // 网络地址
            URL urlObj = new URL(url);
            URLConnection urlConnection = urlObj.openConnection();
            urlConnection.setConnectTimeout(30 * 1000);
            urlConnection.setReadTimeout(60 * 1000);
            urlConnection.setDoInput(true);
            in = urlConnection.getInputStream();
            return IOUtils.toByteArray(in);
        } catch (Exception e) {
            log.error("read file error {}", e);
            return null;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
