import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.ObjectUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Main on 2016/11/20.
 */
public class GetInfo {
    private static String USERNAME;  //数据库用户名
    private static String PASSWORD;  //数据库密码
    private static String DRIVER;  //驱动信息
    private static String URL;  //数据库地址
    private static String table1; //操作表1
    private static String table2; //操作表2
    static String filePath;
    static String logPath;
    static String[] type={"category_yjdbg_szsh","category_ndbg_szsh","category_bndbg_szsh","category_sjdbg_szsh","category_scgkfx_szsh"
                              ,"category_pg_szsh","category_zf_szsh","category_kzhz_szsh","category_qzxg_szsh","category_qtrz_szsh","category_jy_szsh","category_gqbd_szsh"
                            ,"category_qyfpxzcs_szsh","category_gddh_szsh","category_cqfxyj_szsh","category_zjjg_szsh","category_qtzdsx_szsh",
                           "category_jshgg_szsh","category_dshgg_szsh","category_zqgg_szsh","category_bcgz_szsh","category_tbclts_szsh","category_ssgszd_szsh"
                            ,"category_tzzgx_szsh"};

    static float num=0;
    static int totalElements=0;
    static LocalDateTime logTime;

    public static void main(String[] args) {

        readProps();
//每日入库操作
        startGet(getDay(),getDay());
        for(int i=0; i<24;i++) {
            int pageNum = gettypepages(i, getDay(), getDay());
            MyThread mt_1 = new MyThread(i, pageNum, getDay(), getDay());
            for(int threadNum = 0; threadNum<5; threadNum++){
                new Thread(mt_1).start();
            }

        }

        download(getDay());
        transfer(getDay());

    }


    /**
     * 获取config.properties中的参数信息
     * 前四个参数为数据库参数
     * 最后一个参数是日志文件放置地址
     */
    static void readProps(){
        Properties props = new Properties();
        try(Reader reader = new InputStreamReader(new FileInputStream("config.properties"),"GBk");){
            props.load(reader);
            USERNAME = props.getProperty("USERNAME");
            PASSWORD = props.getProperty("PASSWORD");
            DRIVER = props.getProperty("DRIVER");
            URL = props.getProperty("URL");
            logPath = props.getProperty("logPath");
            table1 = props.getProperty("table1");
            table2 = props.getProperty("table2");
            filePath = props.getProperty("filePath");
        }catch (Exception e){
            e.printStackTrace();
        }
        Iterator<String> it=props.stringPropertyNames().iterator();
         while(it.hasNext()){
             String key=it.next();
             System.out.println(key+":"+props.getProperty(key));
         }
    }

    /**
     * 写日志函数
     * 参数为文件地址
     * 每次log时会在行前加上时间
     * 文件所在路径如果不存在，会自动创建文件路径
     */
    static void log(String str){
        File file=new File(logPath);

        if(!file.exists())
            try{
                if(!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }catch (Exception e){
                e.printStackTrace();
            }

        try{
            logTime = LocalDateTime.now();
            FileOutputStream out=new FileOutputStream(file,true);
            StringBuffer sb=new StringBuffer();
            sb.append(logTime+" ");
            sb.append(str);
            System.out.println(sb.toString());
            out.write(sb.toString().getBytes("utf-8"));
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取网站初始任务量
     * 会写日志
     */
    static void startGet( String startDate, String endDate){
        JSONObject announcements=null;
                    String ans = HttpClient.instance().executeToken("http://www.cninfo.com.cn/cninfo-new/announcement/query",
                            "column=szse&&columnTitle=历史公告查询&pageNum=0&pageSize=30&tabName=fulltext&seDate="+startDate+" ~ "+ endDate);

                try {
                    JSONObject jsonObject = JSONObject.fromObject(ans);
                   // announcements = (JSONObject) jsonObject.get("announcements");
                    totalElements = Integer.parseInt(jsonObject.get("totalAnnouncement").toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                System.out.println("获取" + startDate+"~"+endDate + "公告" + "\n"+"totalElements:" + totalElements);
                log("获取" + startDate+"~"+endDate + " 公告  共有公告个数:" + totalElements+"\n");
    }

    /**
     * 获取单个类型页数量
     * 不写日志
     */
    static int gettypepages(int disclosureType, String startDate, String endDate){
        JSONArray announcements=null;
        int pagesForOneType = 0;
        String ans = HttpClient.instance().executeToken("http://www.cninfo.com.cn/cninfo-new/announcement/query",
                "column=szse&category="+type[disclosureType]+"&columnTitle=历史公告查询&pageNum=0&pageSize=30&tabName=fulltext&seDate="+startDate+" ~ "+ endDate);

        try {
            JSONObject jsonObject = JSONObject.fromObject(ans);
            // announcements = (JSONObject) jsonObject.get("announcements");
            totalElements = Integer.parseInt(jsonObject.get("totalAnnouncement").toString());
            double temp=totalElements;
             pagesForOneType = (int)(Math.ceil(temp/30));
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return pagesForOneType;
        }

    }

    /**
     * 获取单个类型单页数据
     * 不写日志
     */
    static JSONArray getInfo(int disclosureType,int page, String startDate, String endDate){
        JSONArray announcements=null;
        String ans = HttpClient.instance().executeToken("http://www.cninfo.com.cn/cninfo-new/announcement/query",
                "column=szse&category="+type[disclosureType]+"&columnTitle=历史公告查询&pageNum="+page+"&pageSize=30&tabName=fulltext&seDate="+startDate+" ~ "+ endDate);

        if(ans==null)
            return null;
        try {
            JSONObject jsonObject = JSONObject.fromObject(ans);
            announcements = (JSONArray) jsonObject.get("announcements");
        } catch (Exception e) {
            System.out.println("error");
            e.printStackTrace();
        }
        return announcements;
    }


    /**
     * 某类型报告入库函数
     * 使用getInfo函数获取每页信息
     * 写日志
     */
    static class MyThread implements Runnable {
        private Lock myLock=new ReentrantLock();
        private int totalPages;
        private int isNewTree;
        private String startDate;
        private String endDate;
        // private int disclosureType;
        private int disclosureType;
        private int dalei;

        public MyThread(int disclosureType,int totalPages,String startDate,String endDate) {
            this.totalPages = totalPages;
            this.isNewTree = isNewTree;
            this.startDate = startDate;
            this.endDate = endDate;
            this.disclosureType = disclosureType;
            if (disclosureType<7)
            {
                dalei=1;
            }
            else {
                dalei = 2;
            }
        }
        public void run(){
            JdbcUtils dbConn = new JdbcUtils(DRIVER,URL,USERNAME,PASSWORD);//创建数据库连接对象对象
            while(totalPages>0){
                int requestPage=0;
                JSONArray item=null;
                myLock.lock();
                try{
                    requestPage=totalPages;
                    totalPages--;
                }finally {
                    myLock.unlock();
                }

                item = getInfo(disclosureType,requestPage,startDate,endDate);

                for(int j=0;j<item.size();j++) {

                    String announcementId=((JSONObject)item.get(j)).get("announcementId").toString();
                    String secCode=((JSONObject)item.get(j)).get("secCode").toString();
                    String announcementTitle=((JSONObject)item.get(j)).get("announcementTitle").toString();
                    String adjunctType=((JSONObject)item.get(j)).get("adjunctType").toString();
                    String adjunctUrl="http://www.cninfo.com.cn/"+((JSONObject)item.get(j)).get("adjunctUrl").toString();
                    String Time =((JSONObject)item.get(j)).get("announcementTime").toString();
                    String orgId =((JSONObject)item.get(j)).get("orgId").toString();
                    Long It =new Long(Time);
                    Date date =new Date(It);
                    String cnnouncementTime=new SimpleDateFormat("yyyy-MM-dd").format(date);

                    String sql = "replace into "+table1+" (Id,StockCode,F3009_025,F3009_021,F3009_022,F3009_023,F3009_024,F3009_026,F3009_027) " + "values("
                            +announcementId+","
                            +"\""+secCode+"\""+","
                            +"\""+announcementTitle+"\""+","
                            +"\""+cnnouncementTime+"\""+","
                            +"13"+","
                            +dalei+","
                            //                        +orgId+","
                            +disclosureType+","
                            +"\""+adjunctUrl+"\""+","
                            +"\""+filePath+"/"+getDay().substring(0,4)+"/"+getDay().substring(5,7)+"/"+getDay().substring(8,10)+"/"+announcementId+"."
                            +""+adjunctType+""+"\""
                            +")";

                    myLock.lock();
                    try{                                                                        //,inputtime


                        dbConn.updateByPreparedStatement(sql,null);
                        num++;
                        float a=num;
                        System.out.println(sql);
                        System.out.println(a+"type="+disclosureType+"page="+totalPages+"num="+j);

                    }catch (SQLException e) {
                        num--;
                        e.printStackTrace();
                        log(sql+" 语句报错！\n");
                    }
                    finally {
                        myLock.unlock();
                    }


                }

            }
            dbConn.releaseConn();//关闭连接
        }
    };

    /**
     * 每日下载函数
     * 写日志
     */
    static void download(String startDate) {

        long rowCount = 0;
        String fileDownloadDir =filePath + "/" + startDate.substring(0, 4) + "/" + startDate.substring(5, 7) + "/" + startDate.substring(8, 10) + "/";

        String insertCountSQL = "select count(*) as rowCount from " + table1 + " where F3009_021 between '" + startDate + "' and  '" + startDate + "'";
        JdbcUtils dbConn = new JdbcUtils(DRIVER, URL, USERNAME, PASSWORD);//创建数据库连接对象对象
        System.out.println(insertCountSQL);
        try {
            Map<String, Object> insertCountMap = dbConn.findSimpleResult(insertCountSQL, null);
            rowCount = (long) insertCountMap.get("rowCount");
//            System.out.println("入库"+startDate+"信息，实际获取量:" + rowCount + "\n");
            log("入库"+startDate+"信息，实际获取量:" + rowCount + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String downloadUrlSql = "select F3009_026 ,F3009_027 from " + table1 + " where F3009_021 between '" + startDate + "' and  '" + startDate + "'";
        System.out.println(downloadUrlSql);

        int count = 0;
        try {
            List<Map<String, Object>> downloadUrlList = dbConn.findModeResult(downloadUrlSql, null);
            DownloadURLFile down_file = new DownloadURLFile();
            downloadUrlList.stream().filter(
                    downloadUrl -> !isFileExists(String.valueOf(downloadUrl.get("F3009_027"))))
                    .forEach(downloadUrl -> {
                        String AnnounLink = String.valueOf(downloadUrl.get("F3009_026"));
                        String localPath = String.valueOf(downloadUrl.get("F3009_027"));

                        //访问速度过快服务器会出现响应失败情况，每次下载前间隔一段时间
                        try {
                            Thread.currentThread().sleep(1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //超时处理，当一个文件下载过程中超过一定时间，则放弃下载本文件
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        FutureTask<String> future =
                                new FutureTask<String>(new Callable<String>() {//使用Callable接口作为构造参数
                                    public String call() {
                                        String res = down_file.downloadFromUrl(AnnounLink, fileDownloadDir);
                                        System.out.println( AnnounLink+ " -> " + fileDownloadDir);
                                        return res;

                                    }
                                });
                        executor.execute(future);
                        //在这里可以做别的任何事情
                        try {
                            String res = future.get(100000, TimeUnit.MILLISECONDS); //取得结果，同时设置超时执行时间为5秒。同样可以用future.get()，不设置执行超时时间取得结果

                        } catch (Exception e) {
                            log(AnnounLink + "下载超时!\n");
//                            System.out.println(AnnounLink + "下载超时!\n");
                            e.printStackTrace();
                        } finally {
                            executor.shutdown();
                        }

                    });

//            System.out.println("总文件数量：" + rowCount + " 实际下载："+fileCountForDir(fileDownloadDir));
            log("下载"+startDate+"文件，总文件数量：" + rowCount + " 实际下载："+fileCountForDir(fileDownloadDir)+"\n");


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取日期函数
     * 返回格式“2017-12-25”
     * 无参数，返回今天的日期
     * return type:String
     */
    static String getDay(){
        LocalDate temDate = LocalDate.now();
        return temDate.toString();
    }
    /**
     * 获取日期函数
     * 返回格式“2017-12-25”
     * 有一个int型参数，返回今天作为基点，加上int的日期。int可以为负，即当日日期减去几天
     * return type:String
     */
    static String getDay(int daysToAdd){
        LocalDate temDate = LocalDate.now();
        temDate = temDate.plusDays(daysToAdd);
        return temDate.toString();
    }

    /**
     * 判断文件是否存在
     * @param filePath 判断文件路径
     * @return boolean
     * return type:String
     */
    static boolean isFileExists(String filePath){
        File file=new File(filePath);
        return file.exists();
    }

    static int fileCountForDir(String Dir){
        File fileDir=new File(Dir);
        if(fileDir.isDirectory()&&fileDir.exists()) {
            return fileDir.list().length;
        }else return 0;
    }

    /**
     * pdf转换成String函数
     * 参数为本地pdf文件
     * 正常解析返回pdf解析出的文字
     * 文件没有找到返回“404”
     * 文件存在但没有解析成功返回“400”
     */
    public static String getText(String file) throws Exception {
        // 是否排序
        boolean sort = false;
        // 开始提取页数
        int startPage = 1;
        // 结束提取页数
        int endPage = Integer.MAX_VALUE;
        PDDocument document = null;
        try {
            document = PDDocument.load(new File(file));
        } catch (Exception e) {

            System.out.println("file not found");
            document.close();
            return "404";//file not found
        }
        try {
            // 采用PDFTextStripper提取文本
            PDFTextStripper stripper = new PDFTextStripper();
            // 设置是否排序
            stripper.setSortByPosition(sort);
            // 设置起始页
            stripper.setStartPage(startPage);
            // 设置结束页
            stripper.setEndPage(endPage);
            String text = stripper.getText(document);
            //尝试把后边接有空白字符的换行符换成其他的文字，然后把换行符替换掉，之后再把其他文字换成换行符
            text = text.replaceAll("\\r\\n\\s", "许相虎");
            text = text.replaceAll("\\s\\r\\n", "许相虎");
            text = text.replaceAll("\\n|\\r", "");
            text = text.replaceAll("许相虎", "\r\n");
            text = text.replaceAll("\\s{4,}", "\r\n");
            text = text.replaceAll("'", "‘");
            text = text.replaceAll(";", "；");
            return text;
        } catch (Exception e2) {
//            e2.printStackTrace();
            System.out.println("file can not convert to String");
            return "400";// bad request
        } finally {
            document.close();
        }
    }


    /**
     * 获取jl_ext_3009中的id和本地pdf路径
     * 每行数据调用updateContent函数
     * @params startDate,endDate
     */
    static void transfer(String startDate) {
        long rowToTransCount = 0;
        long rowSuccessTransCount = 0;
        String CountByDateSQL = "select count(*) as rowCount  from " + table1 + " where F3009_021 between '" + startDate + "' and  '" + startDate + "'";
        System.out.println(CountByDateSQL);
        JdbcUtils selectByDateConn = new JdbcUtils(DRIVER,URL,USERNAME,PASSWORD);

        try {
            Map<String, Object> selectByDateCount = selectByDateConn.findSimpleResult(CountByDateSQL,null);
            rowToTransCount = (long)selectByDateCount.get("rowCount");
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            selectByDateConn.releaseConn();
        }


        String idAndPathByDateSQL = "select id,F3009_027  from " + table1 + " where F3009_021 between '" + startDate + "' and  '" + startDate + "'";//SQL语句
        System.out.println(idAndPathByDateSQL);

        try {
            List<Map<String, Object>> idAndPathList = selectByDateConn.findModeResult(idAndPathByDateSQL, null);
            idAndPathList.stream().filter(
                    idAndPath->isSatisfied(String.valueOf(idAndPath.get("id")),String.valueOf(idAndPath.get("F3009_027"))))
                    .forEach(idAndPath->System.out.println(updateContent(String.valueOf(idAndPath.get("id")),String.valueOf(idAndPath.get("F3009_027")))));

        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            selectByDateConn.releaseConn();
        }

        String CountSuccessByDateSQL = "select count(*) as rowCount  from " + table1 +"as t1 ,"+table2 +" as t2  where t1.id = t2.id and t2.F3009_021 between '" + startDate + "' and  '" + startDate + "'";
        System.out.println(CountByDateSQL);
        JdbcUtils rowSuccessByDateConn = new JdbcUtils(DRIVER,URL,USERNAME,PASSWORD);

        try {
            Map<String, Object> selectByDateCount = selectByDateConn.findSimpleResult(CountByDateSQL,null);
            rowSuccessTransCount = (long)selectByDateCount.get("rowCount");
            log("转换"+startDate+"pdf数据，共有条数"+rowToTransCount+"。转换成功条数："+rowSuccessTransCount+"\n");
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            selectByDateConn.releaseConn();
        }

    }

    /**
     * 函数判断两个条件：
     * ①本id是否已经被解析过入到jl_ext_3009_content库中了；②此id对应的本地文件是否存在；
     * 当id不在jl_ext_3009_content表中或者id在表中但是content字段为null，且本地文在存在的情况下，返回true
     */
    static boolean isSatisfied(String id, String path) {
        //先判断id是否已经被解析过入到jl_ext_3009_content库,insertcount=1的情况下表示已经被插入了一条信息
        String isInsertedSQL = "select count(*) as rowCount from " + table2 + " where id =" + "\"" + id + "\" and content is not null";//SQL语句
        System.out.println(isInsertedSQL);
        JdbcUtils selectByDateConn = new JdbcUtils(DRIVER,URL,USERNAME,PASSWORD);

        long insertCount = 1;
        try {
            Map<String, Object> isInserted = selectByDateConn.findSimpleResult(isInsertedSQL,null);
            insertCount = (long)isInserted.get("rowCount");
            if (insertCount != 0) {
                System.out.println(id + "已被解析过");
                return false;
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            return false;
        }

        //再判断path路径下是否存在本行文件
        if (isFileExists(path)) {
            //库中不存在解析过的记录且文件存在的情况下返回true
            return true;
        }
        else{
            System.out.println(id + "file hasn't been downloaded yet!");
            return false;
        }

    }

    /**
     * 用数据库某条数据id和本地路径path
     * 使用gettext函数返回一个string来填写content字段
     */
    static String updateContent(String id, String path) {
        String content = "";
        try {
            content = getText(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!content.equals("404")) {
            //pdf解析文件的时候返回400说明解析失败，通过对返回结果来看解析成功的里面也有全是换行符的，所以有了对长度的限制。
            if (content.equals("400") || content.length() < 50)
                content = "";

            String replaceSQL = "replace into " + table2 + " (id,content) values( '" + id + "','" + content + "')";
//            System.out.println(replaceSQL);
            JdbcUtils replaceConn = new JdbcUtils(DRIVER,URL,USERNAME,PASSWORD);
            try {
                replaceConn.updateByPreparedStatement(replaceSQL,null);
            } catch (Exception e2) {
                e2.printStackTrace();
                return id + "插入失败";
            } finally {
                replaceConn.releaseConn();
            }
            return id + "插入成功";
        }
        return id + "插入失败";
    }
}
