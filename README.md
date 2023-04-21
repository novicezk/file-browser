# File Browser 文件浏览工具

开放某个目录下所有文件，供用户访问、查阅等

## 主要特性
- 便捷的文件、目录信息浏览
- 支持文件在线预览、下载
- 支持新建文件夹、删除文件、上传文件（登陆后）
- 可跟踪日志类型文件的持续追加内容
- 自适应移动端
- 使用spring-boot，可直接jar包运行

## 示例

![file-browser](https://novicezk.github.io/file-browser.png)

## 功能描述
- ![folder](https://novicezk.github.io/folder2-open.svg) 目录，单击进入该目录
- ![file](https://novicezk.github.io/file-text.svg) 浏览器支持预览的文件，单击文件名查看
- ![file](https://novicezk.github.io/file-x.svg) 不可预览的文件
- ![download](https://novicezk.github.io/download.svg) 点击下载
- ![tail](https://novicezk.github.io/camera-video.svg) 跟踪文件的持续追加内容

## 注意事项
- 基于java17开发
- 文件跟踪功能，追加内容输出到页面上有几秒的延迟

## 配置项
- `fb.root` 开放的主目录
- `fb.access-authenticated` 文件访问是否需要登录，默认false
- `fb.modifiable` 是否允许更改文件（登陆后），包括上传文件、删除文件、创建目录等，默认true
- `fb.username` 用户名，默认 admin
- `fb.password` 密码，默认 novice@2021
- `fb.preview-file-exts` 可预览的文件类型，默认配置了常见的类型
- `fb.tail-file-exts` 可监控的文件类型，默认配置了txt,log
- `spring.servlet.multipart.max-file-size` 单个文件上传的限制大小，默认10M
- `spring.servlet.multipart.max-request-size` 文件上传的限制大小，默认10M


## docker方式启动

1. 下载项目
```shell
git clone https://github.com/novicezk/file-browser
```
2. 构建镜像
```shell
cd file-browser
./build-image.sh
```
3. 启动容器示例
```shell
docker run -d --name file-browser \
 -p 8080:8080 \
 -v /home/homolo/data:/home/spring/data \
 -v /home/homolo/file-browser/logs:/home/spring/logs \
 -e fb.username=homolo \
 -e fb.password=Homolo@2023 \
 file-browser:1.2-SNAPSHOT
```
