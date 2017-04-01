# updateApp实现phonegap的android更新
安装的步骤：

phonegap plugin add https://github.com/samulle/updateApp.git

移除：

phonegap plugin remove com-samulle-plugin-updateApp

其中服务器需要配置一个xml，其格式为：
![image](https://github.com/samulle/updateApp/blob/master/version.png)

前端调用：

UpdateApp.update(url);

其中url是服务器配置的xml地址
