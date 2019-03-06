package com.pinyougou.shop.controller;

import com.pinyougou.until.FastDFSClient;
import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;//文件服务器地址


    @RequestMapping("/upload")
    public Result upload(MultipartFile file) {
        //获取文件的扩展名
        String filename = file.getOriginalFilename();
        String name = filename.substring(filename.lastIndexOf(".") + 1);

        try {
            //创建一个fastDFS的客户端
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
            //执行上传处理,上传返回一个储存的路径
            String path = fastDFSClient.uploadFile(file.getBytes(), name);
            //拼接返回的 url 和 ip 地址，拼装成完整的url
            String url = FILE_SERVER_URL + path;


            return new Result(true, url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }


    }
}
