package com.example.pi.dao.support;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;


public class NameParametersJdbcDaoSupportClass extends NamedParameterJdbcDaoSupport {
	@Autowired
	public void setDataSource1(DataSource dataSource) {
		setDataSource(dataSource);
	}


}