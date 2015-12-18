package com.file;
import java.io.File;
import java.nio.file.FileSystems;
import java.util.Date;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import comm.Util;

/**
 * @author xinghl
 * 博猪自己写了个简单的例子，可以对一个文件夹内的内容进行索引的创建，并根据关键字筛选文件，并读取其中的内容。
 * 英文支持可以  IKAnalyzer中文不行
 */

//索引管理
public class IndexManager{
//    文件内容
    private static String content="";
//    放索引目录
    private static String INDEX_DIR = "D:\\luceneIndex";
//    文件目录
    private static String DATA_DIR = "D:\\luceneData";
//    词法分析器
    private static Analyzer analyzer = null;
//    索引文件存储的位置
    private static Directory directory = null;
//    创建IndexWriter，进行索引文件的写入
    private static IndexWriter indexWriter = null;
    
    /**
     * 为当前文件目录下的所有文件创建索引
     * @param path 当前文件目录路径
     * @return 是否成功
     */
    public static boolean createIndex(String path){
        Date date1 = new Date();
        List<File> fileList = Util.getFileList(path);
        for (File file : fileList) {
            content = "";
            //获取文件后缀
            String type = file.getName().substring(file.getName().lastIndexOf(".")+1);
            if("txt".equalsIgnoreCase(type)){
                content += Util.txt2String(file);
            }else if("doc".equalsIgnoreCase(type)){
                content += Util.doc2String(file);
            }else if("xls".equalsIgnoreCase(type)){
                content += Util.xls2String(file);
            }
            
            System.out.println("name :"+file.getName());
            System.out.println("path :"+file.getPath());
//            System.out.println("content :"+content);
            
            try{
            	analyzer = new StandardAnalyzer();
//            	analyzer = new IKAnalyzer();//中文 不行
            	File indexFile = new File(INDEX_DIR);
            	if (!indexFile.exists()) {
            		indexFile.mkdirs();
            	}
                directory = FSDirectory.open(FileSystems.getDefault().getPath(INDEX_DIR));
                IndexWriterConfig config = new IndexWriterConfig(analyzer);
                indexWriter = new IndexWriter(directory, config);
                Document document = new Document();
                document.add(new TextField("filename", file.getName(), Store.YES));
                document.add(new TextField("content", content, Store.YES));
                document.add(new TextField("path", file.getPath(), Store.YES));
                
//                mmseg4j 用法
                indexWriter.addDocument(document);
                indexWriter.commit();
                indexWriter.close();
            }catch(Exception e){
                e.printStackTrace();
            }
//            每个文件后content清零
            content = "";
        }
        Date date2 = new Date();
        System.out.println("创建索引-----耗时：" + (date2.getTime() - date1.getTime()) + "ms\n");
        return true;
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
        	
//        	analyzer = new StandardAnalyzer();
//        	analyzer = new IKAnalyzer();//中文不行
        	analyzer = new SimpleAnalyzer();//中文 
            DirectoryReader directoryReader = DirectoryReader.open(directory);
            IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
    
//            QueryParser parser = new QueryParser("fieldname", analyzer);
//            前面创建索引时，放入了3个标签 "filename" "content" "path" 现在查询时也要对应起来
            QueryParser parser = new QueryParser("content", analyzer);
            Query query = parser.parse(text);
            ScoreDoc[] hits = indexSearcher.search(query, null, 1000).scoreDocs;
        
            for (int i = 0; i < hits.length; i++) {
                Document hitDoc = indexSearcher.doc(hits[i].doc);
                System.out.println("____________匹配结果________________");
                System.out.println(hitDoc.get("filename"));
//                System.out.println(hitDoc.get("content"));
                System.out.println(hitDoc.get("path"));
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
        createIndex(DATA_DIR);
    	
//        searchIndex("霍乱");
        searchIndex("爱情");
//        searchIndex("阿里萨");
//        searchIndex("KING CLAUDIUS");
    }
}