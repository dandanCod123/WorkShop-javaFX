package model.services;

import java.util.ArrayList;
import java.util.List;

import model.dao.DaoFactory;
import model.dao.DepartmentDao;
import model.entities.Department;


public class DepartmentServices {

	private DepartmentDao dao = DaoFactory.createDepartmentDao();
	
	
	public List<Department> findAll(){
		
		return dao.findAll();
	}
	
	public void saverOrUpdate(Department obj) { //AUTUALIZAR UM NOVO DEPARTAMENTO OU CRIA UM NOVO
		
		if(obj.getId() == null) {
			dao.insert(obj);
		}
		else {
			dao.update(obj);
		}
	}
	
	
	public void Remove(Department obj) { // REMOVER UM DEPARTAMENTO NO BANCO DE DADOS, SE RELACIONANDO COM UM ATRIBUTO NO DEPARTMENTLISTCONTROLLER
		dao.deleteById(obj.getId());
	}
	
}
