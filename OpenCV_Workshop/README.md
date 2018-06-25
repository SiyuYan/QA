# Appium-Opencv

## 环境
### 下载代码
git clone https://github.com/twqa/OpenCV_Workshop.git
### 在intellijIdea中打开项目，然后把OpenCV添加进项目，前提是OpenCV已按照文档提前装好
project structure  -->  Modules  -->  1_test  --> click "+" at the bottom  --> JARs or directories...
-->  add /your/path/opencv-310.jar (/opt/local/share/OpenCV/java/opencv-310.jar)
### 配置VMoptions
Edit Configurations... -->  default  -->  Application  -->  Configuration  -->  add -Djava.library.path=/your/path/OpenCV/java/ (-Djava.library.path=/opt/local/share/OpenCV/java/) to VM options

Edit Configurations... -->  default  -->  Junit  -->  Configuration  -->  add -Djava.library.path=/your/path/OpenCV/java/ (-Djava.library.path=/opt/local/share/OpenCV/java/) to VM options


## task
### 一 模版匹配
refer http://docs.opencv.org/2.4/doc/tutorials/imgproc/histograms/template_matching/template_matching.html#template-matching

#### 1 识别lena的脸
1.1完成matchAndFindLoc函数，获取最佳匹配位置

1.2完成getMatchArea函数，画出匹配区域

#### 2 练习与Appium结合，识别login，实现微信的登录

### 二 颜色识别和轮廓勾勒
refer http://docs.opencv.org/2.4/doc/tutorials/imgproc/shapedescriptors/bounding_rects_circles/bounding_rects_circles.html#bounding-rects-circles





