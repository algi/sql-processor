package org.sqlproc.sample.simple.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlproc.engine.SqlControl;
import org.sqlproc.engine.SqlCrudEngine;
import org.sqlproc.engine.SqlEngineFactory;
import org.sqlproc.engine.SqlQueryEngine;
import org.sqlproc.engine.SqlSessionFactory;
import org.sqlproc.engine.impl.SqlStandardControl;
import org.sqlproc.sample.simple.model.Performer;

public class PerformerDao {
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  private SqlEngineFactory sqlEngineFactory;
  private SqlSessionFactory sqlSessionFactory;
    	
  public PerformerDao(SqlEngineFactory sqlEngineFactory, SqlSessionFactory sqlSessionFactory) {
    this.sqlEngineFactory = sqlEngineFactory;
    this.sqlSessionFactory = sqlSessionFactory;
  }
  
  
  public Performer insert(Performer performer, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
      logger.trace("insert performer: " + performer + " " + sqlControl);
    }
    SqlCrudEngine sqlInsertPerformer = sqlEngineFactory.getCrudEngine("INSERT_PERFORMER");
    int count = sqlInsertPerformer.insert(sqlSessionFactory.getSqlSession(), performer);
    if (logger.isTraceEnabled()) {
      logger.trace("insert performer result: " + count + " " + performer);
    }
    return (count > 0) ? performer : null;
  }
  
  public Performer insert(Performer performer) {
    return insert(performer, null);
  }
  
  public Performer get(Performer performer, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
      logger.trace("get get: " + performer + " " + sqlControl);
    }
    SqlCrudEngine sqlEnginePerformer = sqlEngineFactory.getCrudEngine("GET_PERFORMER");
    //sqlControl = getMoreResultClasses(performer, sqlControl);
    Performer performerGot = sqlEnginePerformer.get(sqlSessionFactory.getSqlSession(), Performer.class, performer, sqlControl);
    if (logger.isTraceEnabled()) {
      logger.trace("get performer result: " + performerGot);
    }
    return performerGot;
  }
  	
  public Performer get(Performer performer) {
    return get(performer, null);
  }
  
  public int update(Performer performer, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
      logger.trace("update performer: " + performer + " " + sqlControl);
    }
    SqlCrudEngine sqlEnginePerformer = sqlEngineFactory.getCrudEngine("UPDATE_PERFORMER");
    int count = sqlEnginePerformer.update(sqlSessionFactory.getSqlSession(), performer);
    if (logger.isTraceEnabled()) {
      logger.trace("update performer result count: " + count);
    }
    return count;
  }
  
  public int update(Performer performer) {
    return update(performer, null);
  }
  
  public int delete(Performer performer, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
      logger.trace("delete performer: " + performer + " " + sqlControl);
    }
    SqlCrudEngine sqlEnginePerformer = sqlEngineFactory.getCrudEngine("DELETE_PERFORMER");
    int count = sqlEnginePerformer.delete(sqlSessionFactory.getSqlSession(), performer);
    if (logger.isTraceEnabled()) {
      logger.trace("delete performer result count: " + count);
    }
    return count;
  }
  
  public int delete(Performer performer) {
    return delete(performer, null);
  }
  
  public List<Performer> list(Performer performer, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
      logger.trace("list performer: " + performer + " " + sqlControl);
    }
    SqlQueryEngine sqlEnginePerformer = sqlEngineFactory.getQueryEngine("SELECT_PERFORMER");
    //sqlControl = getMoreResultClasses(performer, sqlControl);
    List<Performer> performerList = sqlEnginePerformer.query(sqlSessionFactory.getSqlSession(), Performer.class, performer, sqlControl);
    if (logger.isTraceEnabled()) {
      logger.trace("list performer size: " + ((performerList != null) ? performerList.size() : "null"));
    }
    return performerList;
  }
  
  public List<Performer> list(Performer performer) {
    return list(performer, null);
  }
}