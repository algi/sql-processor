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
import org.sqlproc.sample.simple.model.Library;

public class LibraryDao {
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  private SqlEngineFactory sqlEngineFactory;
  private SqlSessionFactory sqlSessionFactory;
    	
  public LibraryDao(SqlEngineFactory sqlEngineFactory, SqlSessionFactory sqlSessionFactory) {
    this.sqlEngineFactory = sqlEngineFactory;
    this.sqlSessionFactory = sqlSessionFactory;
  }
  
  
  public Library insert(Library library, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
      logger.trace("insert library: " + library + " " + sqlControl);
    }
    SqlCrudEngine sqlInsertLibrary = sqlEngineFactory.getCheckedCrudEngine("INSERT_LIBRARY");
    int count = sqlInsertLibrary.insert(sqlSessionFactory.getSqlSession(), library);
    if (logger.isTraceEnabled()) {
      logger.trace("insert library result: " + count + " " + library);
    }
    return (count > 0) ? library : null;
  }
  
  public Library insert(Library library) {
    return insert(library, null);
  }
  
  public Library get(Library library, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
      logger.trace("get get: " + library + " " + sqlControl);
    }
    SqlCrudEngine sqlGetEngineLibrary = sqlEngineFactory.getCheckedCrudEngine("GET_LIBRARY");
    //sqlControl = getMoreResultClasses(library, sqlControl);
    Library libraryGot = sqlGetEngineLibrary.get(sqlSessionFactory.getSqlSession(), Library.class, library, sqlControl);
    if (logger.isTraceEnabled()) {
      logger.trace("get library result: " + libraryGot);
    }
    return libraryGot;
  }
  	
  public Library get(Library library) {
    return get(library, null);
  }
  
  public int update(Library library, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
      logger.trace("update library: " + library + " " + sqlControl);
    }
    SqlCrudEngine sqlUpdateEngineLibrary = sqlEngineFactory.getCheckedCrudEngine("UPDATE_LIBRARY");
    int count = sqlUpdateEngineLibrary.update(sqlSessionFactory.getSqlSession(), library);
    if (logger.isTraceEnabled()) {
      logger.trace("update library result count: " + count);
    }
    return count;
  }
  
  public int update(Library library) {
    return update(library, null);
  }
  
  public int delete(Library library, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
      logger.trace("delete library: " + library + " " + sqlControl);
    }
    SqlCrudEngine sqlDeleteEngineLibrary = sqlEngineFactory.getCheckedCrudEngine("DELETE_LIBRARY");
    int count = sqlDeleteEngineLibrary.delete(sqlSessionFactory.getSqlSession(), library);
    if (logger.isTraceEnabled()) {
      logger.trace("delete library result count: " + count);
    }
    return count;
  }
  
  public int delete(Library library) {
    return delete(library, null);
  }
  
  public List<Library> list(Library library, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
      logger.trace("list library: " + library + " " + sqlControl);
    }
    SqlQueryEngine sqlEngineLibrary = sqlEngineFactory.getCheckedQueryEngine("SELECT_LIBRARY");
    //sqlControl = getMoreResultClasses(library, sqlControl);
    List<Library> libraryList = sqlEngineLibrary.query(sqlSessionFactory.getSqlSession(), Library.class, library, sqlControl);
    if (logger.isTraceEnabled()) {
      logger.trace("list library size: " + ((libraryList != null) ? libraryList.size() : "null"));
    }
    return libraryList;
  }
  
  public List<Library> list(Library library) {
    return list(library, null);
  }
}
