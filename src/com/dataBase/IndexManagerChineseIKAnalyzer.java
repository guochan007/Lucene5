package com.dataBase;
import java.io.File;
import java.nio.file.FileSystems;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import comm.JdbcUtil;
import comm.Util;


/**
 * 基于数据库的lucene索引和基于文件的索引   其实差不多  通过jdbc拿到表数据，放入 Document中，类似 文件中  读取文件信息放入Document中
 * 
 * 该方案 没有实现  索引文件实时随着数据库表的变化而变化 
 */

//索引管理
public class IndexManagerChineseIKAnalyzer{
//    字段内容
    private static String content="";
//    放索引目录
    private static String INDEX_DIR = "D:\\luceneIndex";
//    词法分析器
    private static Analyzer analyzer = null;
//    索引文件存储的位置
    private static Directory directory = null;
//    创建IndexWriter，进行索引文件的写入
    private static IndexWriter indexWriter = null;
    
    private static Connection conn = null;
    private static Statement statement = null;
	private static  ResultSet resultSet = null; 
	
    /**
     * 为当前表创建索引
     * @return 是否成功
     */
    public static void createIndex(){
        Date date1 = new Date();
        conn = JdbcUtil.getConnection();   
		if(conn == null) {   
			System.out.println("数据库连接失败！");
		}   
		String sql = "select id, username, address from lucenetest";   
            
        try{
			statement = conn.createStatement();   
			resultSet = statement.executeQuery(sql); 
			
        	analyzer = new IKAnalyzer(true);//中文 
        	File indexFile = new File(INDEX_DIR);
        	if (!indexFile.exists()) {
        		indexFile.mkdirs();
        	}
            directory = FSDirectory.open(FileSystems.getDefault().getPath(INDEX_DIR));
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            indexWriter = new IndexWriter(directory, config);
            
			while(resultSet.next()) {   
				content = "";
				Document document = new Document();
				document.add(new TextField("id", String.valueOf(resultSet.getInt("id")), Store.YES));
//    				document.add(new TextField("username", rs.getString("username") == null ? "" : rs.getString("username"), Store.YES));
//    				document.add(new TextField("address", rs.getString("address") == null ? "" : rs.getString("address"), Store.YES));
				
//    				将所有字段信息 放入一个content中
				String username=resultSet.getString("username") == null ? "" : resultSet.getString("username");
				String address=resultSet.getString("address") == null ? "" : resultSet.getString("address");
				content=content+username+address;
				System.out.println("content----"+content);
				
				document.add(new TextField("content",content, Store.YES));
				indexWriter.addDocument(document);
			} 
            
            indexWriter.commit();
            indexWriter.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
			try {
				resultSet.close();
				statement.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
        Date date2 = new Date();
        System.out.println("创建索引-----耗时：" + (date2.getTime() - date1.getTime()) + "ms\n");
    }
    
    
    /**
     * 查找索引，返回符合条件的文件
     * @param text 查找的字符串
     * @return 符合条件的文件List
     */
    @SuppressWarnings("deprecation")
	public static void searchIndex(String text){
        Date date1 = new Date();
        try{
        	directory = FSDirectory.open(FileSystems.getDefault().getPath(INDEX_DIR));
        	
        	analyzer = new IKAnalyzer(true);//中文
        	IndexReader directoryReader = DirectoryReader.open(directory);
        	IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
    
//            QueryParser parser = new QueryParser("fieldname", analyzer);
//            前面创建索引时，放入了3个标签 "filename" "content" "path" 现在查询时也要对应起来
            
            /*
            String[] fields = { "id", "title" };
            QueryParser qp = new MultiFieldQueryParser(fields, analyzer);
            */
            
            QueryParser parser = new QueryParser("content", analyzer);
            parser.setDefaultOperator(QueryParser.AND_OPERATOR);
            Query query = parser.parse(text);
            ScoreDoc[] hits = indexSearcher.search(query, null, 1000).scoreDocs;
        
            for (int i = 0; i < hits.length; i++) {
                Document hitDoc = indexSearcher.doc(hits[i].doc);
                System.out.println("____________匹配结果________________");
                System.out.println(hitDoc.get("id"));
                System.out.println(hitDoc.get("content"));
                System.out.println("____________________________");
            }
            directoryReader.close();
            directory.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        Date date2 = new Date();
        System.out.println("查看索引-----耗时：" + (date2.getTime() - date1.getTime()) + "ms\n");
    }
    
   
    public static void main(String[] args){
        File fileIndex = new File(INDEX_DIR);
        if(Util.deleteDir(fileIndex)){
            fileIndex.mkdir();
        }else{
            fileIndex.mkdir();
        }
        
        createIndex();
    	
//        searchIndex("大毛");
        searchIndex("大同");
    }
}