package model.services;

import java.util.ArrayList;
import java.util.List;

import model.dao.DaoFactory;
import model.dao.SellerDao;
import model.entities.Seller;


public class SellerServices {

	private SellerDao dao = DaoFactory.createSellerDao();
	
	
	public List<Seller> findAll(){
		
		return dao.findAll();
	}
	
	public void saverOrUpdate(Seller obj) { //AUTUALIZAR UM NOVO SELLER OU CRIA UM NOVO
		
		if(obj.getId() == null) {
			dao.insert(obj);
		}
		else {
			dao.update(obj);
		}
	}
	
	
	public void Remove(Seller obj) { // REMOVER UM SELLER NO BANCO DE DADOS, SE RELACIONANDO COM UM ATRIBUTO 
		dao.deleteById(obj.getId());
	}
	
}
