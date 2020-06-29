package com.qingcheng.aliyun;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

public class MyOssClient {

    private OSS oss;

    public MyOssClient(String endpoint,String accessKeyId,String accessKeySecret){
        this.oss = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    public OSS getOss(){
        return this.oss;
    }
}
