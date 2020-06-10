package postgres;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException; 
import org.hibernate.Query;
import org.hibernate.Session; 
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.google.gson.Gson;

import vnmsTrails.tableclasses.Model_detail;


public class HibernateMain {
	private void addTestTable(){
		SessionFactory factory;
		try {
	        factory = new Configuration().configure().buildSessionFactory();
	     } catch (Throwable ex) { 
	        System.err.println("Failed to create sessionFactory object." + ex);
	        throw new ExceptionInInitializerError(ex); 
	     }	
		 Session session = factory.openSession();
		 Transaction tx = session.beginTransaction();
		 try {			 	       
			 
			 
			 Criteria cr = session.createCriteria(Model_detail.class);			 
			 List<Model_detail> results = (List<Model_detail>)cr.list();
			 for(Model_detail t : results){
			 System.out.println(t.getModel_id()+"<<>>"+t.getModel_name()+"<<>>"+t.getCreated_time()+"<<>>"+t.getLast_updated()+"<<>>"+t.getQos_parameter());
			  }
			 
			 /*Criteria cr = session.createCriteria(Test.class);
			 cr.add(Restrictions.eq("name", "dffdfd"));
			 List<Test> results = (List<Test>)cr.list();
			 for(Test t : results)
				 System.out.println(t.getName());*/
			 
			 /*Criteria cr = session.createCriteria(Test.class);
			 cr.setProjection(Projections.property("name"));
			 List<String> results = (List<String>)cr.list();
			 for(String t : results)
				 System.out.println(t);*/
			 
			 
/*	       Query q = session.createQuery("from Test");	      
	       Test e = (Test)q.list().get(0);
	       System.out.println(e.getName());
*/	        tx.commit();
	     } catch (HibernateException e) {
	        if (tx!=null) tx.rollback();
	        e.printStackTrace(); 
	     }finally {
	        session.close(); 
	     }
	}
	private void addTest_Byte(){
		SessionFactory factory;
		try {
	        factory = new Configuration().configure().buildSessionFactory();
	     } catch (Throwable ex) { 
	        System.err.println("Failed to create sessionFactory object." + ex);
	        throw new ExceptionInInitializerError(ex); 
	     }	
		 Session session = factory.openSession();
		 Transaction tx = null;
		 try {			 
	        tx = session.beginTransaction();
	        Test_Bytea tb = new Test_Bytea();
	        tb.setId(4);
	        
	        File f = new File("D:\\mm.txt");
	        byte[] data = new byte[(int)f.length()];
	       FileInputStream fin = new FileInputStream(f);
	       fin.read(data);
	       String s = new String(data);
	       System.out.println("File content: " + s);
	       tb.setData(data);
	       session.save(tb);
	        tx.commit();
	     } catch (HibernateException e) {
	        if (tx!=null) tx.rollback();
	        e.printStackTrace(); 
	     } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
	        session.close(); 
	     }
	}
	public static void main(String[] args) {
		HibernateMain mm = new HibernateMain();
		mm.addTestTable();
		//mm.addTest_Byte();
	}
	 
}
