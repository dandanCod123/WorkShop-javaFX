package gui; //CLASSE PRA CONTROLAR O DEPARTMENTFORM 

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entities.Department;
import model.exceptions.ValidationException;
import model.services.DepartmentServices;


public class DepartmentFormController implements Initializable{

	private DepartmentServices services;
	
	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();
	
	private Department entity;
	
	
   @FXML
   private TextField txtid;
	
	@FXML
	private TextField txtName;
	
	@FXML
	private Label labelErrorName;
	
	@FXML
	private Button btSave;
	
	@FXML
	private Button btCancel;
	
	public void setDepartment(Department entity) {
		this.entity = entity;
	}
	
	public void setDepartmentServices(DepartmentServices services) {
		this.services = services;
	}
	
	public void SubscribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}
	
	
	
	
	@FXML
	private void onBtSaveAction(ActionEvent event) {
		if(entity == null) {
			throw new IllegalStateException("ENTITY IS NULL");
		}
		if(services == null) {
			throw new IllegalStateException("SEVICES IS NULL");
		}
		
		try {
		entity = getFormData();
		services.saverOrUpdate(entity);
		notifyDataListeners();
		Utils.currenteStage(event).close();
		}
		catch(ValidationException e) {
			setErrorMessages(e.getErrors());
		}
		catch(DbException e) {
			Alerts.showAlert("ERRO SAVING OBJECT",null , e.getMessage(), AlertType.ERROR);
		}
		
}
		
	
	
	private void notifyDataListeners() {
		for(DataChangeListener listeners : dataChangeListeners) {
			listeners.onDataChange();
		}
	}

	private Department getFormData() {
		Department obj = new Department();
		
		ValidationException exception = new ValidationException("validation error");
		
		obj.setId(Utils.tryParseToInt(txtid.getText()));
		
		if(txtName.getText() == null || txtName.getText().trim().equals("")) {
		    exception.addError("name", "field can't be empty");
		}
		
		if(exception.getErrors().size() > 0) {
		    throw exception;	
		}
			
		obj.setName(txtName.getText());
		
		return obj;
		
	}

	@FXML
	private void onBtCancelAction(ActionEvent event) {
		Utils.currenteStage(event).close();
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	    initializaNodes();
	}

	
	private void initializaNodes() {
		Constraints.setTextFieldInteger(txtid);
		Constraints.setTextFieldMaxLength(txtName, 30);
		
	}
	
	public void updateFormData() {
		if(entity == null) {
			throw new IllegalStateException("ERROR ENTITY IS  NULL");
		}
		
		txtid.setText(String.valueOf(entity.getId()));
		txtName.setText(entity.getName());
	}
	
	private void setErrorMessages(Map<String,String> error) {
		Set<String> fields = error.keySet();
		
		if(fields.contains("name")) {
			labelErrorName.setText("Name can't be empty");
		}
	}
	
	
	
}
