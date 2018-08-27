package com.huawei.bigdata.hbase.examples;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import sun.tools.jconsole.Tab;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerHandler {

    private TableName tableName;
    private Connection conn;

    public ServerHandler(TableName tableName,Connection conn) throws  Exception{

        this.tableName = tableName;
        this.conn = conn;
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress("0.0.0.0",4000), 0);
        server.createContext("/callback", new TestHandler());
        server.start();
    }

    public  class TestHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time_t =df.format(new Date());
                        String response = "Post Succeed\n";
                        //获得查询字符串(get)
                        String queryString =  exchange.getRequestURI().getQuery();
                        //获得表单提交数据(post)
                        String postString = IOUtils.toString(exchange.getRequestBody());

                        System.out.println(time_t+" RequestBody:" +  postString);
                        Qiniu_Callback_put(time_t,postString);

                        exchange.sendResponseHeaders(200,0);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    }catch (IOException ie) {
                        ie.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }


    public void Qiniu_Callback_put(String time,String value ) throws  Exception{
        byte[] familyName = Bytes.toBytes("info");
        // Specify the column name.
        byte[] qualifiers = Bytes.toBytes("data");
        Table table = null;
        table = conn.getTable(tableName);
        Put put = new Put(Bytes.toBytes(time));
        //this is put
        put.addColumn(familyName, qualifiers, Bytes.toBytes(value));
        table.put(put);
        System.out.println("Put Hbase Succeed");

    }
}
