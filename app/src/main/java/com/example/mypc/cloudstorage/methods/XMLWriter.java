package com.example.mypc.cloudstorage.methods;

/**
 * Created by My PC on 2018/3/1.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * XML写入对象，内部运用StringBuilder和BufferedWriter来优化写入性能，同时简化写入
 * @author catch
 *
 */
public class XMLWriter {

    private static final String START_START = "<";
    private static final String END = ">";
    private static final String END_START = "</";
    private static final String START_DATA = "<![CDATA[";
    private static final String END_DATA = "]]>";
    private String charset = "utf-8";
    private StringBuilder sb = new StringBuilder();
    private BufferedWriter writer;

    /**
     * XMLWriter对象，默认编码为utf-8
     * @param file	要写入的文件
     * @throws FileNotFoundException
     */
    public XMLWriter(File file) throws FileNotFoundException {
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * XMLWriter对象
     * @param file	要写入的文件
     * @param charset 编码
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     */
    public XMLWriter(File file, String charset) throws UnsupportedEncodingException, FileNotFoundException {
        //this(file);
        this.charset = charset;
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
    }

    /**
     * 写开始标签
     * @param tag	标签名称
     */
    public void writeStartTAG(String tag){
        sb.append(START_START);
        sb.append(tag);
        sb.append(END);
    }

    /**
     * 写结束标签
     * @param tag	标签名称
     */
    public void writeEndTAG(String tag){
        sb.append(END_START);
        sb.append(tag);
        sb.append(END);
    }

    /**
     * 写入文本
     * @param text
     */
    public void writeText(String text){
        sb.append(text);
    }

    /**
     * 写入带<![CDATA[ ]]>的文本
     * @param text
     */
    public void writeTextData(String text){
        sb.append(START_DATA);
        sb.append(text);
        sb.append(END_DATA);
    }

    /**
     * 文本标签一起写入
     * @param text
     * @param tag
     */
    public void writeText(String text, String tag){
        writeStartTAG(tag);
        writeText(text);
        writeEndTAG(tag);
    }

    /**
     * 文本标签一起写入并带有<![CDATA[ ]]>
     * @param text
     * @param tag
     */
    public void writeTextData(String text, String tag){
        writeStartTAG(tag);
        writeTextData(text);
        writeEndTAG(tag);
    }

    /**
     * 写头文件
     */
    public void writeDocument(){
        sb.append("<?xml version=\"1.0\" encoding=\"" + charset + "\" standalone=\"yes\" ?>");
    }

    /**
     * 将StringBuilder中的数据刷新到BufferedWriter中，并清空StringBuidler
     * @throws IOException
     */
    public void flush() throws IOException{
        writer.write(sb.toString());
        sb.delete(0, sb.length());
    }

    /**
     * 关闭BufferedWriter资源，在之前会调用flush()方法
     * @throws IOException
     */
    public void close() throws IOException{
        flush();
        writer.close();
    }

}

