# 171208newCrawler
Java自用爬虫框架之巨潮网站A股爬虫

一、软件简介使用：
程序主要完成对巨潮A股公告的入库，公告关联文件的下载，关联文件转化成文本并入库功能。实现了每日半小时与网站公布信息更新。
作为每日自动更新程序，配合windows或者linux执行计划完成自动操作，没有界面操作。所有操作都集装成jar包进行调用。比较容易更改的几个参数放在执行程序统一路径下，供程序读取使用。在发布信息格式不更改、需求不变更的情况下是一次配置，永久运行程序。
在指定路径下，会有每次程序执行情况的日志写入。单次执行日志正常运行情况下，写四行日志。第一行日志记录总信息数，第二行日志记录入库转换行数，第三行日志记录成功下载行数，第四行日志记录文件转换成功行数。日志如下：
 

二、软件配置
配置方案：使用windows的执行计划固定时间执行bat文件，bat文件包括执行jar文件的命令，之所以需要bat文件执行jar文件，是因为jar文件执行时默认的文件执行格式不是utf-8（不是utf-8中文会出现乱码），需要在外部强制utf-8执行。
Config.properties文件为程序执行时的数据库参数文件，在更新服务器的时候需要改动

由于是java写成的可执行jar文件，所以需要Java环境来执行，需要jdk和jre，具体操作详情链接：
http://jingyan.baidu.com/article/6dad5075d1dc40a123e36ea3.html
每天固定时间执行操作的步骤使用了window的执行计划，设置步骤如下：
http://jingyan.baidu.com/article/a3a3f811e12f278da3eb8a4f.html
（其中教程有一容易出错的地方为：建立触发器的时候起始于选项应填写上bat文件的路径）

Bat文件中执行代码如下：（标红名称为程序打包成的jar包名称）
java -Dfile.encoding=utf-8 -jar juchaoagu.jar

配置文件config.properties中有8个参数变量，名称不可修改，值可以修改：
 
参数含义如下：
DRIVER：驱动名称，在不修改数据库的情况下，不用修改。
URL：数据库地址，其中有ip、端口信息、数据库名称，在部署时需要把所操作的数据库地址修正，其他位置不用改变。
USERNAME：数据库登录名称
PASSWORD：登录密码
logPath：日志路径，需要把文件名称写全，不可只写一个路径
table1：操作表1
table2：操作表2
filePath：文件下载根路径
三、程序执行相关流程信息
部署在执行计划中，所以没有人工操作，下面几张图作为程序的三个步骤展示
①	多线程入库操作
 
②	单线程下载（服务器请求过快会响应失败，多线程提升不够大，且容易出错）
 
③	Pdf转换入库，乱码输出是pdf转文本时出现的警告。
 
四、其他事项
两张表的结构如下
CREATE TABLE `jl_ext_3009` (
`id` BIGINT(20) NOT NULL,
`Comcode` VARCHAR(40) NULL DEFAULT NULL,
`Stockcode` VARCHAR(10) NULL DEFAULT NULL,
`F3009_021` DATE NULL DEFAULT NULL,
`F3009_022` VARCHAR(80) NULL DEFAULT NULL,
`F3009_023` INT(11) NULL DEFAULT NULL,
`F3009_024` INT(11) NULL DEFAULT NULL,
`F3009_025` VARCHAR(128) NULL DEFAULT NULL,
`F3009_026` VARCHAR(255) NULL DEFAULT NULL,
`content` LONGTEXT NULL,
`F3009_027` VARCHAR(255) NULL DEFAULT NULL,
`Inputtime` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
`updatetime` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`),
INDEX `Stockcode` (`Stockcode`),
INDEX `F3009_021` (`F3009_021`)
)
COLLATE='utf8_general_ci'

CREATE TABLE `jl_ext_3009_content` (
`id` BIGINT(20) NOT NULL,
`content` LONGTEXT NULL,
PRIMARY KEY (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;
