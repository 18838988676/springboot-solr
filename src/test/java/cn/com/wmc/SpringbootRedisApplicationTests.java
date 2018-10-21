package cn.com.wmc;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.AnalysisParams;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import cn.com.wmc.entity.PrescriptionVO;
import cn.com.wmc.entity.RegistertypeSolr;
import cn.com.wmc.service.impl.PrescriptionServiceImpl;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootRedisApplicationTests {
	
	@Autowired  
    private SolrClient client;
	
	
	@Autowired  
	public  HttpSolrClient httpSolrClient;
	
	@Test
	public void testName() throws Exception {
		System.out.println(httpSolrClient);
	}
	
	
	//add  没有这个字段的话  自动添加；
	@Test
	public void testadd() throws Exception {
		 SolrInputDocument doc = new SolrInputDocument();
         doc.setField("id", "1dfafda2321");
         doc.setField("content_ik", "我是fdafadsfas中国人, 我爱中国");
         client.add("wmcsl", doc);
         //client.commit();
         client.commit("wmcsl");
	}
	
	//delete
	@Test
	public void testdelete() throws Exception {
		  client.deleteById("wmcsl", "12321");
          client.commit("wmcsl");
	}
	
	//删除所有
	@Test
	public void testdeleteall() throws Exception {
		
		
		  client.deleteByQuery("wmcsl","*:*");
          client.commit("wmcsl");
	}
	
	@Test
	public void testFind() throws Exception {
		 List<RegistertypeSolr> aa=	getDocment("我们的祖国", "registertypeCore", true, 1, 10);
		 for (RegistertypeSolr registertypeSolr : aa) {
			System.out.println(registertypeSolr);
		}
	}
	
	
	public List<RegistertypeSolr>  getDocment(String keys,String core,Boolean isHighlighting,Integer page,Integer rows) {
		 SolrQuery solrQuery= new SolrQuery(); // 构造搜索条件
		solrQuery.setStart((Math.max(page, 1) - 1) * rows);
         solrQuery.setRows(rows);
		
         if(isHighlighting){setHighlight(solrQuery);}
         
		 //进行分词
		 String splitWords = null;
		 QueryResponse solrResults =null;
		try {
			if(keys!=""||!keys.isEmpty()){
				splitWords = splitWords(httpSolrClient,keys);
				solrQuery.set("q","typename:"+splitWords,"note:"+splitWords);
			}
			System.out.println(solrQuery+"----------");
			solrResults = httpSolrClient.query(solrQuery);
			
		} catch (Exception e) {
			System.out.println("solrCrud:"+e.getMessage());
		}
		
		 List<RegistertypeSolr> registertypes=solrResults.getBeans(RegistertypeSolr.class);
		 for (RegistertypeSolr registertype : registertypes) {
				System.out.println(registertype);
			}
		 
		 
		if(isHighlighting){setHighlightToObject(solrResults,registertypes);}
		 
		 for (RegistertypeSolr registertype : registertypes) {
				System.out.println(registertype);
			}
		 
		return registertypes;
	}
	
	//高亮进入实体
	
		public static void setHighlightToObject( QueryResponse solrResults , List<RegistertypeSolr> registertypes){
			// 将高亮的标题数据写回到数据对象中
	        Map<String, Map<String, List<String>>> map = solrResults.getHighlighting();
	        for (Map.Entry<String, Map<String, List<String>>> hightDatas : map.entrySet()) {
	            for (RegistertypeSolr em : registertypes) {
	                if (!hightDatas.getKey().equals(em.getId().toString())) {
	                	continue;
	                }
	                
	                if(hightDatas.getValue().get("typecode") != null){
	                   em.setTypecode(hightDatas.getValue().get("typecode")+"");  
	                }
	                
	                if(hightDatas.getValue().get("typename") != null){
	                    em.setTypename(hightDatas.getValue().get("typename")+"");
	                }
	                
	                if(hightDatas.getValue().get("typesum") != null){
//	                    em.setTypesume(hightDatas);
	                	
	                }
	                
	                if(hightDatas.getValue().get("note") != null){
	                    em.setNote(hightDatas.getValue().get("note")+"");
	                }
	                
	                if(hightDatas.getValue().get("id") != null){
	                  System.out.println("cn.com.his.core.solr.SolrCrud:id:"+hightDatas.getValue().get("id"));
//	                     em.setId(hightDatas.getValue().get("id")+"");
	                }
	               //下面全是if
	                break;
	            }
	        }
			
			
		}
	
	//设置高亮
		public static void setHighlight(SolrQuery solrQuery) {
			// 设置高亮
	        solrQuery.setHighlight(true); // 开启高亮组件
	        solrQuery.addHighlightField("id");
	        solrQuery.addHighlightField("typecode");
	        solrQuery.addHighlightField("typename");
	        solrQuery.addHighlightField("typesum");
	        solrQuery.addHighlightField("note");
	        solrQuery.addHighlightField("isvalid");
	        solrQuery.setHighlightSimplePre("<span style='color:red;'>");// 标记，高亮关键字前缀
	        solrQuery.setHighlightSimplePost("</span>");// 后缀
			
		}
	
		//分词器
	    public String splitWords(HttpSolrClient solrClient,String keywords) throws Exception{
	        SolrQuery query = new SolrQuery();
System.out.println(solrClient+"====");
System.err.println("查询的语句:"+keywords);
	        query.add(CommonParams.QT, "/analysis/field"); // query type

	        query.add(AnalysisParams.FIELD_VALUE, keywords);

	        query.add(AnalysisParams.FIELD_TYPE, "text_ik");
	        QueryResponse response=solrClient.query(query);

	        NamedList<Object> analysis =  (NamedList<Object>) response.getResponse().get("analysis");// analysis node

	        NamedList<Object> field_types =  (NamedList<Object>) analysis.get("field_types");// field_types node

	        NamedList<Object> text_ik =  (NamedList<Object>) field_types.get("text_ik");// text_chinese node

	        NamedList<Object> index =  (NamedList<Object>) text_ik.get("index");// index node

	        List<SimpleOrderedMap<String>> list =  (ArrayList<SimpleOrderedMap<String>>) index.get("org.wltea.analyzer.lucene.IKTokenizer");// tokenizer node

	        String nextQuery="";
	        for(Iterator<SimpleOrderedMap<String>> iter = list.iterator(); iter.hasNext();)

	        {
	            nextQuery += iter.next().get("text") + "  ";
	        }
	        System.out.println();
	        return nextQuery.trim();
	    }

}
