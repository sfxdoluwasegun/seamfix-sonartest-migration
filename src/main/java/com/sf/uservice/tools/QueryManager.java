package com.sf.uservice.tools;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.NamingException;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.logging.Logger;

import com.sf.unical.HibernateUtils;
import com.sf.unical.ServiceLocator;
import com.sf.unical.entity.main.alumni.Alumni;
import com.sf.unical.entity.main.docs.Docs;
import com.sf.unical.entity.main.settings.Settings;
import com.sf.unical.entity.request.requesthistory.RequestHistory;
import com.sf.unical.entity.request.requestpassport.RequestPassport;
import com.sf.unical.service.UnicalService;

/**
 * Manage Hibernate/JPA queries for the application 
 * 
 * @author KOMOO
 */
@Stateless
@TransactionManagement(value=TransactionManagementType.CONTAINER)
public class QueryManager {

	private final Logger log = Logger.getLogger(getClass());
	
	/**
	 * Fetch {@link Docs} with binary data i.e non-null document
	 * 
	 * @return docs
	 */
	public List<Docs> getDocumentsWithBinaryData(){
		Session session = null;
		
		try{
			session = getSession();
			Criteria criteria = session.createCriteria(Docs.class);

			criteria.add(Restrictions.isNotNull("document"));
			criteria.setFetchSize(10);
			criteria.setMaxResults(10);

			List<Docs> docs = (List<Docs>) criteria.list();
			
			return docs;
		} catch (HibernateException | NamingException ex) {
			throw new HibernateException("QueryManager.getDocumentsWithBinaryData", new Exception(ex.toString(), ex));
		} finally{
			closeSession(session);
		}
	}
	
	/**
	 * Fetch {@link Alumni} by {@link Docs} id
	 * 
	 * @param documentId
	 * 
	 * @return alumus
	 * @throws HibernateException
	 * @throws NamingException
	 */
	public Alumni getAlumniByDocumentId(long documentId) throws HibernateException, NamingException{
		Session session = null;

		try {
			session = getSession();
			Criteria criteria = session.createCriteria(Alumni.class);

			criteria.createAlias("docs", "document");
			criteria.add(Restrictions.eq("document.id", documentId));

			Alumni alumni = (Alumni) criteria.uniqueResult();
			return alumni;
		} catch (HibernateException | NamingException ex) {
			throw new HibernateException("QueryManager.getAlumniByDocumentId: "+documentId, new Exception(ex.toString(), ex));
		} finally{
			closeSession(session);
		}
	}
	
	/**
	 * Fetch {@link Alumni} with digitized records by matricNo
	 * 
	 * @param matricNo
	 * 
	 * @return alumus
	 * @throws HibernateException
	 * @throws NamingException
	 */
	public Alumni getAlumniByMatricNo(String matricNo) throws HibernateException, NamingException{
		Session session = null;

		try {
			session = getSession();
			Criteria criteria = session.createCriteria(Alumni.class);

			criteria.add(Restrictions.eq("matricNo", matricNo));

			Alumni alumni = (Alumni) criteria.uniqueResult();
			return alumni;
		} catch (HibernateException | NamingException ex) {
			throw new HibernateException("QueryManager.getAlumniByMatricNo: "+matricNo, new Exception(ex.toString(), ex));
		} finally{
			closeSession(session);
		}
	}
	
	/**
	 * Get alumni by orbita ID
	 * 
	 * @param orbitaId
	 * @return alumnus
	 * @throws HibernateException
	 * @throws NamingException
	 */
	public Alumni getAlumniByOrbitaId(Long orbitaId) throws HibernateException, NamingException {
		Session session = null;

		try {
			session = getSession();
			Criteria criteria = session.createCriteria(Alumni.class);

			criteria.add(Restrictions.eq("orbitaId", orbitaId));

			Alumni alumni = (Alumni) criteria.uniqueResult();
			return alumni;
		} catch (HibernateException | NamingException ex) {
			throw new HibernateException("QueryManager.getAlumniByOrbitaId: "+orbitaId, new Exception(ex.toString(), ex));
		} finally{
			closeSession(session);
		}
	}
	
	/**
	 * Get {@link Settings} by name
	 * 
	 * @param name
	 * @return
	 * @throws HibernateException
	 * @throws NamingException
	 */
	public Settings getSettingsByName(String name) throws HibernateException, NamingException {
		Session session = null;
		try{
			session = getSession();
			Criteria criteria = session.createCriteria(Settings.class);
			
			criteria.add(Restrictions.eq("name", name));
			
			Settings settings = (Settings) criteria.uniqueResult();
			return settings;
		} catch (HibernateException | NamingException ex) {
			log.warn("Check that Setting with name '"+ name +"' exists");
			throw new HibernateException("QueryManager.getSettingsByName: "+name, new Exception(ex.toString(), ex));
		} finally{
			closeSession(session);
		}
	}
	
	/**
	 * Create portal {@link Settings}
	 * 
	 * @param name
	 * @param value
	 * @param description
	 * @return
	 */
	public Settings createSettings(String name,
			String value,
			String description) throws HibernateException, NamingException {
		
		Settings setting = getSettingsByName(name);
		if(setting != null)
			return setting;
		
		setting = new Settings();
		setting.setName(name);
		setting.setValue(value);
		setting.setDescription(description);
		
		return (Settings) getService().createNewRecord(setting);
		
	}
	
	/**
	 * Fetch {@link Alumni} by matricNo and fetcg {@linkplain Docs} eagerly
	 * 
	 * @param matricNo
	 * @return
	 * @throws HibernateException
	 * @throws NamingException
	 */
	public Alumni getAlumniByMatricNoAndEagerDocument(String matricNo) throws HibernateException, NamingException{
		Alumni alumni = null;
		Session session = null;

		try {
			session = getSession();
			Criteria criteria = session.createCriteria(Alumni.class);

			criteria.add(Restrictions.eq("matricNo", matricNo));

			criteria.setFetchMode("docs", FetchMode.JOIN);

			alumni = (Alumni) criteria.uniqueResult();
			return alumni;
		} catch (HibernateException | NamingException e) {
			throw new HibernateException("QueryManager.getAlumniByMatricNoAndEagerDocument: "+matricNo, new Exception(e.getMessage(), e));
			
		} finally{
			closeSession(session);
		}
	}
	
	/**
	 * Fetch {@link RequestPassport} by {@linkplain Alumni}
	 * 
	 * @param alumni
	 * @return
	 * @throws HibernateException
	 * @throws NamingException
	 */
	public RequestPassport getRequestPassportByAlumni(Alumni alumni) throws HibernateException, NamingException{
		Session session = null;

		try {
			session = getSession();

			Criteria criteria = session.createCriteria(RequestPassport.class);

			criteria.add(Restrictions.eq("alumni", alumni));

			RequestPassport passport = (RequestPassport) criteria.uniqueResult();
			
			return passport;
		} catch (HibernateException | NamingException e) {
			throw new HibernateException("ServiceBeanImpl.getRequestPassportByAlumni ", new Exception(e.toString(), e));
			
		} finally{
			closeSession(session);
		}
	}
	
	/**
	 * Fetch {@link RequestHistory} by {@link RequestPassport}
	 * 
	 * @param requestPassport
	 * @return
	 */
	public List<RequestHistory> getRequestHistoryByRequestPassport(RequestPassport requestPassport){
		Session session = null;
		try {
			session = getSession();
			Criteria criteria = session.createCriteria(RequestHistory.class);

			criteria.add(Restrictions.eq("requestPassport", requestPassport));

			criteria.addOrder(Order.asc("date"));

			criteria.setFetchMode("shippingContinent", FetchMode.JOIN);
			criteria.setFetchMode("shippingMode", FetchMode.JOIN);

			List<RequestHistory> requestHistory =  criteria.list();
			return requestHistory;
			
		} catch (HibernateException | NamingException e) {
			// TODO Auto-generated catch block
			throw new HibernateException("ServiceBeanImpl.getRequestHistoryByRequestPassport ", new Exception(e.toString(), e));
		} finally{
			closeSession(session);
		}
	}
	
	
	public UnicalService getService(){
		ServiceLocator serviceLocator = ServiceLocator.getInstance();
		UnicalService unicalService = serviceLocator.getUnicalService();
		
		return unicalService;
	}
	
	protected static Session getSession() throws HibernateException, NamingException {
		return HibernateUtils.getSessionFactory().openSession();
	}

	protected static void closeSession(Session session) throws HibernateException {
		if (session != null){
			session.close();
		}
	}

}
