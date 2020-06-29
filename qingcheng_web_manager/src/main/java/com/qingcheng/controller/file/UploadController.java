package com.qingcheng.controller.file;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.qingcheng.aliyun.MyOssClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private MyOssClient myOssClient;

    /**
     * 本地存储上传文件
     * @param file
     * @return
     */
    @PostMapping("/native")
    public String nativeUpload(@RequestParam("file") MultipartFile file) {
        //1.先获取图片上传之后存在本地的绝对路径地址：
        String realPath = request.getSession().getServletContext().getRealPath("img");
        //2.图片存储的绝对路径+图片的名称：
        String filePath = realPath +"/"+file.getOriginalFilename();
        //3.创建file文件对象
        File desFile = new File(filePath);
        //4.判断当前路径文件是否存在
        if (!desFile.getParentFile().exists()){
            //4.1 当路径文件不存在时，创建文件
            desFile.mkdirs();
        }
        //5.将文件已流保存
        try {
            file.transferTo(desFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //6.返回文件的真实路径已经图片名称
        return "http://localhost:9101/img/"+file.getOriginalFilename();
    }

    /**
     * 阿里云OSS存储图片文件
     * @param file
     * @param folder
     * @return
     */
    @PostMapping("/oss")
    public String ossUpload(@RequestParam("file") MultipartFile file,String folder){
        //1.获取自定义类返回的：OSS接口对象
        OSS ossClient = myOssClient.getOss();
        //2.设置bucket名称：于阿里OSS存储对象中，开辟的名称
        String bucket = "mr-liu-qingcheng";
        //3.设置文件名称：采用：目录+uuid+上传文件名
        String filePath = folder+"/"+ UUID.randomUUID() +file.getOriginalFilename();
        try {
            //4.调用oss上传对象，传入空间名称,文件路径，并把获取的文件流已字节流上传至服务器
            ossClient.putObject(bucket, filePath, file.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //5.返回上传成功路径
        return "https://"+bucket+".oss-cn-beijing.aliyuncs.com/"+filePath;
    }


}
