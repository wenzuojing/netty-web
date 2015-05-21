package org.wzj.web;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wens on 15-5-18.
 */
public class PostFormDataDecoder {

    private static final HttpDataFactory factory =
            new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk if size exceed

    static {
        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file
        // on exit (in normal
        // exit)
        DiskFileUpload.baseDirectory = null; // system temp directory
        DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on
        // exit (in normal exit)
        DiskAttribute.baseDirectory = null; // system temp directory
    }


    private HttpPostRequestDecoder decoder;

    private Map<String, List<String>> params;

    private Map<String, FileItem> files;


    public PostFormDataDecoder(FullHttpRequest httpRequest) {
        this.params = new HashMap<String, List<String>>();
        this.files = new HashMap<String, FileItem>();
        try {
            decoder = new HttpPostRequestDecoder(factory, httpRequest);
            readData();
        } catch (Exception e) {
            throw new WebException(e);
        }

    }


    private void readData() throws IOException {
        try {
            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    try {
                        readData0(data);
                    } finally {
                        data.release();
                    }
                }
            }
        } catch (HttpPostRequestDecoder.EndOfDataDecoderException e) {
            //end
        }
    }

    private void readData0(InterfaceHttpData data) throws IOException {
        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {

            Attribute attribute = (Attribute) data;
            String name = attribute.getName();
            String value = attribute.getValue();

            List<String> values = params.get(name);

            if (values == null) {
                values = new ArrayList<String>(1);
                params.put(name, values);
            }
            values.add(value);

        } else if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {

            FileUpload fileUpload = (FileUpload) data;
            String name = fileUpload.getName();

            FileItem fileItem = new FileItem(fileUpload.getFilename());

            if (fileUpload.isInMemory()) {
                fileItem.setInMemery(true);
                fileItem.setData(fileUpload.get());
            } else {
                fileItem.setInMemery(false);
                fileItem.setFile(fileUpload.getFile());
            }

            files.put(name, fileItem);
        }

    }

    public void release() {
        if (decoder != null) {
            decoder.destroy();
        }
    }

    public Map<String, List<String>> getParams() {
        return params;
    }

    public Map<String, FileItem> getFiles() {
        return files;
    }


}
